import os
import sys
import importlib

import grpc

from abc import ABC
from binascii import a2b_hex

from shapely import geos
from ctypes import c_char_p, c_size_t

from epl.protobuf import geometry_pb2
from epl.grpc import geometry_service_pb2_grpc
from shapely.geometry import base as shapely_base

EMPTY = shapely_base.deserialize_wkb(a2b_hex(b'010700000000000000'))

class CAP_STYLE(object):
    round = 1
    flat = 2
    square = 3


class JOIN_STYLE(object):
    round = 1
    mitre = 2
    bevel = 3


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
        result = geom_factory(geom, spatial_reference=self._geometry_data.spatial_reference)
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

GEOMETRY_MODULES = {
    'LineString': 'epl.shapelier.geometry.linestring',
    'LinearRing': 'epl.shapelier.geometry.polygon',
    'Polygon': 'epl.shapelier.geometry.polygon',
    'MultiPolygon': 'epl.shapelier.geometry.multipolygon'
}


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

    mod = importlib.import_module(name=GEOMETRY_MODULES[geom_type])
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
        op_request = geometry_pb2.GeometryRequest(geometry=self.export_protobuf(),
                                                  operator_type=geometry_pb2.Buffer,
                                                  buffer_params=geometry_pb2.BufferParams(distance=distance),
                                                  result_encoding_type=geometry_pb2.wkb)

        geometry_response = self._stub.GeometryOperationUnary(op_request)
        return BaseGeometry.import_protobuf(geometry_response.geometry)

    def remote_project(self, to_spatial_reference: geometry_pb2.SpatialReferenceData):
        op_request = geometry_pb2.GeometryRequest(geometry=self.export_protobuf(),
                                                  operator_type=geometry_pb2.Project,
                                                  result_spatial_reference=to_spatial_reference)
        return BaseGeometry.import_protobuf(op_request.geometry)

    def buffer(self, distance, resolution=16, quadsegs=None,
               cap_style=CAP_STYLE.round, join_style=JOIN_STYLE.round,
               mitre_limit=5.0):
        """Returns a geometry with an envelope at a distance from the object's
        envelope

        A negative distance has a "shrink" effect. A zero distance may be used
        to "tidy" a polygon. The resolution of the buffer around each vertex of
        the object increases by increasing the resolution keyword parameter
        or second positional parameter. Note: the use of a `quadsegs` parameter
        is deprecated and will be gone from the next major release.

        The styles of caps are: CAP_STYLE.round (1), CAP_STYLE.flat (2), and
        CAP_STYLE.square (3).

        The styles of joins between offset segments are: JOIN_STYLE.round (1),
        JOIN_STYLE.mitre (2), and JOIN_STYLE.bevel (3).

        The mitre limit ratio is used for very sharp corners. The mitre ratio
        is the ratio of the distance from the corner to the end of the mitred
        offset corner. When two line segments meet at a sharp angle, a miter
        join will extend the original geometry. To prevent unreasonable
        geometry, the mitre limit allows controlling the maximum length of the
        join corner. Corners with a ratio which exceed the limit will be
        beveled.

        Example:

          >>> from shapely.wkt import loads
          >>> g = loads('POINT (0.0 0.0)')
          >>> g.buffer(1.0).area        # 16-gon approx of a unit radius circle
          3.1365484905459389
          >>> g.buffer(1.0, 128).area   # 128-gon approximation
          3.1415138011443009
          >>> g.buffer(1.0, 3).area     # triangle approximation
          3.0
          >>> list(g.buffer(1.0, cap_style='square').exterior.coords)
          [(1.0, 1.0), (1.0, -1.0), (-1.0, -1.0), (-1.0, 1.0), (1.0, 1.0)]
          >>> g.buffer(1.0, cap_style='square').area
          4.0
        """
        if quadsegs is not None:
            raise ValueError(
                "The `quadsegs` argument is deprecated. Use `resolution`.",
                DeprecationWarning)
        else:
            res = resolution
        if mitre_limit == 0.0:
            raise ValueError(
                'Cannot compute offset from zero-length line segment')
        if cap_style == CAP_STYLE.round and join_style == JOIN_STYLE.round:
            return geom_factory(self.impl['buffer'](self, distance, res))

        if 'buffer_with_style' not in self.impl:
            raise NotImplementedError("Styled buffering not available for "
                                      "GEOS versions < 3.2.")

        return geom_factory(self.impl['buffer_with_style'](self, distance, res,
                                                           cap_style,
                                                           join_style,
                                                           mitre_limit))

    @property
    def envelope_data(self):
        # """Returns minimum bounding region (minx, miny, maxx, maxy)"""
        return geometry_pb2.EnvelopeData(xmin=self.bounds[0],
                                         ymin=self.bounds[1],
                                         xmax=self.bounds[2],
                                         ymax=self.bounds[3],
                                         spatial_reference=self._spatial_reference)


def geos_geom_from_py(ob, create_func=None):
    """Helper function for geos_*_from_py functions in each geom type.
    If a create_func is specified the coodinate sequence is cloned and a new
    geometry is created with it, otherwise the geometry is cloned directly.
    This behaviour is useful for converting between LineString and LinearRing
    objects.
    """
    if create_func is None:
        geom = geos.lgeos.GEOSGeom_clone(ob._geom)
    else:
        cs = geos.lgeos.GEOSGeom_getCoordSeq(ob._geom)
        cs = geos.lgeos.GEOSCoordSeq_clone(cs)
        geom = create_func(cs)

    N = ob._ndim

    return geom, N


class BaseMultipartGeometry(BaseGeometry):

    def shape_factory(self, *args):
        # Factory for part instances, usually a geometry class
        raise NotImplementedError("To be implemented by derived classes")

    @property
    def ctypes(self):
        raise NotImplementedError(
            "Multi-part geometries have no ctypes representations")

    @property
    def __array_interface__(self):
        """Provide the Numpy array protocol."""
        raise NotImplementedError("Multi-part geometries do not themselves "
                                  "provide the array interface")

    def _get_coords(self):
        raise NotImplementedError("Sub-geometries may have coordinate "
                                  "sequences, but collections do not")

    def _set_coords(self, ob):
        raise NotImplementedError("Sub-geometries may have coordinate "
                                  "sequences, but collections do not")

    @property
    def coords(self):
        raise NotImplementedError(
            "Multi-part geometries do not provide a coordinate sequence")

    @property
    def geoms(self):
        if self.is_empty:
            return []
        return shapely_base.GeometrySequence(self, self.shape_factory)

    def __iter__(self):
        if not self.is_empty:
            return iter(self.geoms)
        else:
            return iter([])

    def __len__(self):
        if not self.is_empty:
            return len(self.geoms)
        else:
            return 0

    def __getitem__(self, index):
        if not self.is_empty:
            return self.geoms[index]
        else:
            return ()[index]

    def __eq__(self, other):
        return (
                type(other) == type(self) and
                len(self) == len(other) and
                all(x == y for x, y in zip(self, other))
        )

    def __ne__(self, other):
        return not self.__eq__(other)

    __hash__ = None

    def svg(self, scale_factor=1., color=None):
        """Returns a group of SVG elements for the multipart geometry.
        Parameters
        ==========
        scale_factor : float
            Multiplication factor for the SVG stroke-width.  Default is 1.
        color : str, optional
            Hex string for stroke or fill color. Default is to use "#66cc99"
            if geometry is valid, and "#ff3333" if invalid.
        """
        if self.is_empty:
            return '<g />'
        if color is None:
            color = "#66cc99" if self.is_valid else "#ff3333"
        return '<g>' + \
               ''.join(p.svg(scale_factor, color) for p in self) + \
               '</g>'
