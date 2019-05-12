import os
import sys
import importlib
import warnings

import grpc

from abc import ABC
from binascii import a2b_hex
from functools import wraps
from ctypes import c_char_p, c_size_t

from shapely.geos import lgeos
from shapely.geometry import base as shapely_base
from shapely.impl import delegated

from epl.protobuf import geometry_pb2, geometry_service_pb2_grpc

integer_types = (int,)

try:
    import numpy as np

    integer_types = integer_types + (np.integer,)
except ImportError:
    pass

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
    'Point': 'epl.geometry.point',
    'LineString': 'epl.geometry.linestring',
    'LinearRing': 'epl.geometry.polygon',
    'Polygon': 'epl.geometry.polygon',
    'MultiPoint': 'epl.geometry.multipoint',
    'MultiLineString': 'epl.geometry.multilinestring',
    'MultiPolygon': 'epl.geometry.multipolygon',
    'GeometryCollection': 'epl.geometry.collection'
}


def dump_coords(geom):
    """Dump coordinates of a geometry in the same order as data packing"""
    if not isinstance(geom, BaseGeometry):
        raise ValueError('Must be instance of a geometry class; found ' +
                         geom.__class__.__name__)
    elif geom.type in ('Point', 'LineString', 'LinearRing'):
        return geom.coords[:]
    elif geom.type == 'Polygon':
        return geom.exterior.coords[:] + [i.coords[:] for i in geom.interiors]
    elif geom.type.startswith('Multi') or geom.type == 'GeometryCollection':
        # Recursive call
        return [dump_coords(part) for part in geom]
    else:
        raise ValueError('Unhandled geometry type: ' + repr(geom.type))


def geometry_type_name(g):
    if g is None:
        raise ValueError("Null geometry has no type")
    return GEOMETRY_TYPES[lgeos.GEOSGeomTypeId(g)]


def geom_factory(g,
                 parent=None,
                 sr: geometry_pb2.SpatialReferenceData = None):
    # Abstract geometry factory for use with topological methods below
    if not g:
        raise ValueError("No Shapely geometry can be created from null value")
    ob = BaseGeometry(sr=sr)
    geom_type = geometry_type_name(g)
    # TODO: check cost of dynamic import by profiling

    mod = importlib.import_module(name=GEOMETRY_MODULES[geom_type])
    ob.__class__ = getattr(mod, geom_type)
    ob._geom = g
    ob.__p__ = parent
    if lgeos.methods['has_z'](g):
        ob._ndim = 3
    else:
        ob._ndim = 2
    ob._is_empty = False
    return ob


def deserialize_wkb(data):
    geom = lgeos.GEOSGeomFromWKB_buf(c_char_p(data), c_size_t(len(data)))
    if not geom:
        raise ValueError(
            "Could not create geometry because of errors while reading input.")
    return geom


def geos_geom_from_py(ob, create_func=None):
    """Helper function for geos_*_from_py functions in each geom type.
    If a create_func is specified the coodinate sequence is cloned and a new
    geometry is created with it, otherwise the geometry is cloned directly.
    This behaviour is useful for converting between LineString and LinearRing
    objects.
    """
    if create_func is None:
        geom = lgeos.GEOSGeom_clone(ob._geom)
    else:
        cs = lgeos.GEOSGeom_getCoordSeq(ob._geom)
        cs = lgeos.GEOSCoordSeq_clone(cs)
        geom = create_func(cs)

    N = ob._ndim

    return geom, N


def exceptNull(func):
    """Decorator which helps avoid GEOS operations on null pointers."""

    @wraps(func)
    def wrapper(*args, **kwargs):
        if not args[0]._geom or args[0].is_empty:
            raise ValueError("Null/empty geometry supports no operations")
        return func(*args, **kwargs)

    return wrapper


class CAP_STYLE(object):
    round = 1
    flat = 2
    square = 3


class JOIN_STYLE(object):
    round = 1
    mitre = 2
    bevel = 3


EMPTY = deserialize_wkb(a2b_hex(b'010700000000000000'))


class BaseGeometry(shapely_base.BaseGeometry, ABC):
    _stub = None

    def __init__(self,
                 sr: geometry_pb2.SpatialReferenceData):
        super(BaseGeometry, self).__init__()
        self._stub = _GeometryServiceStub().stub
        if sr is None:
            raise ValueError("must define a spatial reference for geometry on creation")
        self._sr = sr

    def __str__(self):
        return "{0} {1}".format(self.wkt, str(self.sr))

    @property
    def sr(self):
        return self._sr

    def export_protobuf(self) -> geometry_pb2.GeometryData:
        return geometry_pb2.GeometryData(wkb=self.wkb, sr=self._sr)

    @staticmethod
    def import_protobuf(geometry_data: geometry_pb2.GeometryData) -> geometry_pb2.SpatialReferenceData:
        rpc_reader = RPCReader(lgeos, geometry_data)
        return rpc_reader.read()

    def remote_buffer(self, distance: float):
        op_request = geometry_pb2.GeometryRequest(geometry=self.export_protobuf(),
                                                  operator=geometry_pb2.BUFFER,
                                                  buffer_params=geometry_pb2.GeometryRequest.BufferParams(
                                                      distance=distance),
                                                  result_encoding=geometry_pb2.WKB)

        geometry_response = self._stub.GeometryOperationUnary(op_request)
        return BaseGeometry.import_protobuf(geometry_response.geometry)

    def remote_project(self, to_spatial_reference: geometry_pb2.SpatialReferenceData):
        op_request = geometry_pb2.GeometryRequest(geometry=self.export_protobuf(),
                                                  operator=geometry_pb2.PROJECT,
                                                  result_sr=to_spatial_reference)
        geometry_response = self._stub.GeometryOperationUnary(op_request)
        return BaseGeometry.import_protobuf(geometry_response.geometry)

    def remote_geodetic_area(self):
        """
        get the geodesic area of a polygon
        :return: double value that is the WGS84 area of the geometry
        """
        op_area = geometry_pb2.GeometryRequest(geometry=self.export_protobuf(),
                                               operator=geometry_pb2.GEODETIC_AREA,
                                               result_sr=geometry_pb2.SpatialReferenceData(wkid=4326))
        area_response = self._stub.GeometryOperationUnary(op_area)
        return area_response.measure

    def remote_geodetic_buffer(self, distance_m):
        op_request = geometry_pb2.GeometryRequest(geometry=self.export_protobuf(),
                                                  operator=geometry_pb2.GEODESIC_BUFFER,
                                                  buffer_params=geometry_pb2.GeometryRequest.BufferParams(
                                                      distance=distance_m),
                                                  result_encoding=geometry_pb2.WKB)

        geometry_response = self._stub.GeometryOperationUnary(op_request)
        return BaseGeometry.import_protobuf(geometry_response.geometry)

    def remote_intersection(self,
                            other_geom,
                            operation_sr: geometry_pb2.SpatialReferenceData = None,
                            result_sr: geometry_pb2.SpatialReferenceData = None):
        """
        get the intersecting geometry. if the geometries intersected are in different spatial references, you'll need
        to define a result spatial reference for them both to be projected into. That result spatial reference will be
        the operation spatial reference. If you want their intersection to be in a different spatial reference than the
        results, you can define that as well
        :param other_geom: other geometry to be intersected
        :param operation_sr: the spatial reference both geometries should be projected into for the intersection operation
        :param result_sr: the resulting spatial reference of the output geometry
        :return:
        """
        op_request = geometry_pb2.GeometryRequest(left_geometry=self.export_protobuf(),
                                                  right_geometry=other_geom.export_protobuf(),
                                                  operator=geometry_pb2.INTERSECTION,
                                                  operation_sr=operation_sr,
                                                  result_sr=result_sr)
        return BaseGeometry.import_protobuf(self._stub.GeometryOperationUnary(op_request).geometry)

    @property
    def boundary(self):
        """
        Returns a lower dimension geometry that bounds the object

        The boundary of a polygon is a line, the boundary of a line is a
        collection of points. The boundary of a point is an empty (null)
        collection.
        """
        return geom_factory(self.impl['boundary'](self), sr=self.sr)

    @property
    def bounds(self):
        """Returns minimum bounding region (minx, miny, maxx, maxy)"""
        if self.is_empty:
            return ()
        else:
            return self.impl['bounds'](self)

    @property
    def centroid(self):
        """Returns the geometric center of the object"""
        return geom_factory(self.impl['centroid'](self), sr=self.sr)

    @delegated
    def representative_point(self):
        """Returns a point guaranteed to be within the object, cheaply."""
        return geom_factory(self.impl['representative_point'](self), sr=self.sr)

    @property
    def convex_hull(self):
        """Imagine an elastic band stretched around the geometry: that's a
        convex hull, more or less

        The convex hull of a three member multipoint, for example, is a
        triangular polygon.
        """
        return geom_factory(self.impl['convex_hull'](self), sr=self.sr)

    @property
    def envelope(self):
        """A figure that envelopes the geometry"""
        return geom_factory(self.impl['envelope'](self), sr=self.sr)

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
            warnings.warn(
                "The `quadsegs` argument is deprecated. Use `resolution`.", DeprecationWarning, stacklevel=2)
            res = quadsegs
        else:
            res = resolution
        if mitre_limit == 0.0:
            raise ValueError(
                'Cannot compute offset from zero-length line segment')
        if cap_style == CAP_STYLE.round and join_style == JOIN_STYLE.round:
            return geom_factory(self.impl['buffer'](self, distance, res), sr=self.sr)

        if 'buffer_with_style' not in self.impl:
            raise NotImplementedError("Styled buffering not available for "
                                      "GEOS versions < 3.2.")

        return geom_factory(self.impl['buffer_with_style'](self, distance, res,
                                                           cap_style,
                                                           join_style,
                                                           mitre_limit), sr=self.sr)

    @delegated
    def simplify(self, tolerance, preserve_topology=True):
        """Returns a simplified geometry produced by the Douglas-Peucker
        algorithm

        Coordinates of the simplified geometry will be no more than the
        tolerance distance from the original. Unless the topology preserving
        option is used, the algorithm may produce self-intersecting or
        otherwise invalid geometries.
        """
        if preserve_topology:
            op = self.impl['topology_preserve_simplify']
        else:
            op = self.impl['simplify']
        return geom_factory(op(self, tolerance), sr=self.sr)

    # Binary operations
    # -----------------

    def difference(self, other):
        """Returns the difference of the geometries"""
        return geom_factory(self.impl['difference'](self, other), sr=self.sr)

    def intersection(self, other):
        """Returns the intersection of the geometries"""
        return geom_factory(self.impl['intersection'](self, other), sr=self.sr)

    def symmetric_difference(self, other):
        """Returns the symmetric difference of the geometries
        (Shapely geometry)"""
        return geom_factory(self.impl['symmetric_difference'](self, other), sr=self.sr)

    def union(self, other):
        """Returns the union of the geometries (Shapely geometry)"""
        return geom_factory(self.impl['union'](self, other), sr=self.sr)

    @property
    def envelope_data(self):
        # """Returns minimum bounding region (minx, miny, maxx, maxy)"""
        return geometry_pb2.EnvelopeData(xmin=self.bounds[0],
                                         ymin=self.bounds[1],
                                         xmax=self.bounds[2],
                                         ymax=self.bounds[3],
                                         sr=self._sr)


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
        return GeometrySequence(self, self.shape_factory)

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


class GeometrySequence(object):
    """
    Iterative access to members of a homogeneous multipart geometry.
    """

    # Attributes
    # ----------
    # _factory : callable
    #     Returns instances of Shapely geometries
    # _geom : c_void_p
    #     Ctypes pointer to the parent's GEOS geometry
    # _ndim : int
    #     Number of dimensions (2 or 3, generally)
    # __p__ : object
    #     Parent (Shapely) geometry
    shape_factory = None
    _geom = None
    __p__ = None
    _ndim = None

    def __init__(self, parent, type):
        self.shape_factory = type
        self.__p__ = parent

    def _update(self):
        self._geom = self.__p__._geom
        self._ndim = self.__p__._ndim

    def _get_geom_item(self, i):
        g = self.shape_factory()
        g._other_owned = True
        g._geom = lgeos.GEOSGetGeometryN(self._geom, i)
        g._ndim = self._ndim
        g.__p__ = self
        return g

    def __iter__(self):
        self._update()
        for i in range(self.__len__()):
            yield self._get_geom_item(i)

    def __len__(self):
        self._update()
        return lgeos.GEOSGetNumGeometries(self._geom)

    def __getitem__(self, key):
        self._update()
        m = self.__len__()
        if isinstance(key, integer_types):
            if key + m < 0 or key >= m:
                raise IndexError("index out of range")
            if key < 0:
                i = m + key
            else:
                i = key
            return self._get_geom_item(i)
        elif isinstance(key, slice):
            if type(self) == HeterogeneousGeometrySequence:
                raise TypeError(
                    "Heterogenous geometry collections are not sliceable")
            res = []
            start, stop, stride = key.indices(m)
            for i in range(start, stop, stride):
                res.append(self._get_geom_item(i))
            return type(self.__p__)(res or None)
        else:
            raise TypeError("key must be an index or slice")

    @property
    def _longest(self):
        max = 0
        for g in iter(self):
            l = len(g.coords)
            if l > max:
                max = l


class HeterogeneousGeometrySequence(GeometrySequence):
    """
    Iterative access to a heterogeneous sequence of geometries.
    """

    def __init__(self, parent):
        super(HeterogeneousGeometrySequence, self).__init__(parent, None)

    def _get_geom_item(self, i):
        sub = lgeos.GEOSGetGeometryN(self._geom, i)
        g = geom_factory(sub, parent=self)
        g._other_owned = True
        return g


class EmptyGeometry(BaseGeometry):
    def __init__(self):
        """Create an empty geometry."""
        BaseGeometry.__init__(self)


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
        result = geom_factory(geom, sr=self._geometry_data.sr)
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
