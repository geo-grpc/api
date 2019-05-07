import os
import sys

import grpc

from abc import ABC
from binascii import a2b_hex

from shapely import geos
from ctypes import c_char_p, c_size_t

from epl.protobuf import geometry_pb2
from epl.grpc import geometry_service_pb2_grpc
from shapely.geometry import base as shapely_base


EMPTY = shapely_base.deserialize_wkb(a2b_hex(b'010700000000000000'))


class RPCReader(object):

    _lgeos = None
    _reader_wkb = None
    _reader_wkt = None
    _geometry_data = None

    def __init__(self, lgeos, geometry_data: geometry_pb2.GeometryData):
        """Create Reader"""
        self._lgeos = lgeos
        self._geometry_data = geometry_data
        if len(self._geometry_data.wkt) > 0:
            self._reader_wkt = self._lgeos.GEOSWKTReader_create()
        elif len(geometry_data.wkb) > 0:
            self._reader_wkb = self._lgeos.GEOSWKBReader_create()

    def __del__(self):
        """Destroy Reader"""
        if self._lgeos is not None:
            if self._reader_wkb is not None:
                self._lgeos.GEOSWKBReader_destroy(self._reader_wkb)
                self._reader_wkb = None
            elif self._reader_wkt is not None:
                self._lgeos.GEOSWKTReader_destroy(self._reader_wkt)
                self._reader_wkt = None
            self._lgeos = None

    def read(self):
        geom = None
        if len(self._geometry_data.wkt) > 0:
            geom = self.read_wkt()
        elif len(self._geometry_data.wkb) > 0:
            geom = self.read_wkb()
        result = geom_factory(geom)
        return result

    def read_wkb(self):
        geom = self._lgeos.GEOSWKBReader_read(
            self._reader_wkb, c_char_p(self._geometry_data.wkb), c_size_t(len(self._geometry_data.wkb)))
        if not geom:
            raise ValueError(
                "Could not create geometry because of errors "
                "while reading input.")
        return geom

    def read_wkt(self):
        """Returns geometry from WKT"""
        text = self._geometry_data.wkt
        if sys.version_info[0] >= 3:
            text = self._geometry_data.wkt.encode('ascii')
        geom = self._lgeos.GEOSWKTReader_read(self._reader_wkt, c_char_p(text))
        if not geom:
            raise ValueError(
                "Could not create geometry because of errors "
                "while reading input.")
        return geom


GEOMETRY_TYPES = [
    'Point',
    'LineString',
    'LinearRing',
    'Polygon',
    'MultiPoint',
    'MultiLineString',
    'MultiPolygon',
    'GeometryCollection',
]


class _Singleton(type):
    """
    https://sourcemaking.com/design_patterns/singleton/python/1
    """

    def __init__(cls, name, bases, attrs, **kwargs):
        super().__init__(name, bases, attrs)
        cls._instance = None

    def __call__(cls, *args, **kwargs):
        if cls._instance is None:
            cls._instance = super().__call__(*args, **kwargs)
        return cls._instance


class _GeometryServiceStub(metaclass=_Singleton):
    stub = None

    def __init__(self):
        address = os.getenv("GEOMETRY_SERVICE_HOST", 'localhost:8980')
        #
        # print("connect to address: ", address)
        # print("create channel")
        channel = grpc.insecure_channel(address)
        self.stub = geometry_service_pb2_grpc.GeometryServiceStub(channel)


def geometry_type_name(g):
    if g is None:
        raise ValueError("Null geometry has no type")
    return GEOMETRY_TYPES[geos.lgeos.GEOSGeomTypeId(g)]


def geom_factory(g,
                 parent=None,
                 spatial_reference: geometry_pb2.SpatialReferenceData = None):
    # Abstract geometry factory for use with topological methods below
    if not g:
        raise ValueError("No Shapely geometry can be created from null value")
    ob = BaseGeometry(spatial_reference=spatial_reference)
    geom_type = geometry_type_name(g)
    # TODO: check cost of dynamic import by profiling
    mod = __import__(
        'shapely.geometry',
        globals(),
        locals(),
        [geom_type],
        )
    ob.__class__ = getattr(mod, geom_type)
    ob._geom = g
    ob.__p__ = parent
    if geos.lgeos.methods['has_z'](g):
        ob._ndim = 3
    else:
        ob._ndim = 2
    ob._is_empty = False
    return ob


class BaseGeometry(shapely_base.BaseGeometry, ABC):
    _spatial_reference = None
    _stub = None

    def __init__(self,
                 spatial_reference: geometry_pb2.SpatialReferenceData):
        super(BaseGeometry, self).__init__()
        self._stub = _GeometryServiceStub().stub
        self._spatial_reference = spatial_reference

    def export_protobuf(self) -> geometry_pb2.GeometryData:
        return geometry_pb2.GeometryData(wkb=self.wkb, spatial_reference=self._spatial_reference)

    @staticmethod
    def import_protobuf(geometry_data: geometry_pb2.GeometryData) -> geometry_pb2.SpatialReferenceData:
        rpc_reader = RPCReader(geos.lgeos, geometry_data)
        return rpc_reader.read()

    def remote_buffer(self, distance: float):
        op_request = geometry_pb2.GeometryRequest(left_geometry=self.export_protobuf(),
                                                  operator_type=geometry_pb2.Buffer,
                                                  buffer_params=geometry_pb2.BufferParams(distance=distance),
                                                  result_encoding_type=geometry_pb2.wkb)

        geometry_response = self._stub.GeometryOperationUnary(op_request)
        return BaseGeometry.import_protobuf(geometry_response.geometry)
