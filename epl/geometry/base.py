"""Base geometry class and utilities

Note: a third, z, coordinate value may be used when constructing
geometry objects, but has no effect on geometric analysis. All
operations are performed in the x-y plane. Thus, geometries with
different z values may intersect or be equal.
"""
from __future__ import annotations

import importlib
import random

from binascii import a2b_hex
from ctypes import pointer, c_size_t, c_char_p, c_void_p
import sys
from warnings import warn
from functools import wraps

from typing import Iterable, Iterator
from shapely.coords import CoordinateSequence
from shapely.geos import WKBWriter, WKTWriter
from shapely.geos import lgeos
from shapely.impl import DefaultImplementation, delegated
from epl.protobuf.v1 import geometry_pb2
from epl import geometry as geometry_init
from shapely.wkb import loads as shapely_loads_wkb

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
                 proj: geometry_pb2.ProjectionData = None):
    # Abstract geometry factory for use with topological methods below
    if not g:
        raise ValueError("No Shapely geometry can be created from null value")
    ob = BaseGeometry(proj=proj)
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


# def geom_from_wkt(data, proj: geometry_pb2.ProjectionData):
#     warnings.warn("`geom_from_wkt` is deprecated. Use `geos.wkt_reader.read(data)`.", DeprecationWarning)
#     if sys.version_info[0] >= 3:
#         data = data.encode('ascii')
#     geom = lgeos.GEOSGeomFromWKT(c_char_p(data))
#     if not geom:
#         raise ValueError(
#             "Could not create geometry because of errors while reading input.")
#     return geom_factory(geom, proj=proj)


def geom_to_wkt(ob):
    warn("`geom_to_wkt` is deprecated. Use `geos.wkt_writer.write(ob)`.", DeprecationWarning)
    if ob is None or ob._geom is None:
        raise ValueError("Null geometry supports no operations")
    return lgeos.GEOSGeomToWKT(ob._geom)


def deserialize_wkb(data):
    geom = lgeos.GEOSGeomFromWKB_buf(c_char_p(data), c_size_t(len(data)))
    if not geom:
        raise ValueError(
            "Could not create geometry because of errors while reading input.")
    return geom


# def geom_from_wkb(data):
#     warn("`geom_from_wkb` is deprecated. Use `geos.wkb_reader.read(data)`.",
#          DeprecationWarning)
#     return geom_factory(deserialize_wkb(data))


def geom_to_wkb(ob):
    warn("`geom_to_wkb` is deprecated. Use `geos.wkb_writer.write(ob)`.",
         DeprecationWarning)
    if ob is None or ob._geom is None:
        raise ValueError("Null geometry supports no operations")
    size = c_size_t()
    return lgeos.GEOSGeomToWKB_buf(c_void_p(ob._geom), pointer(size))


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


class BaseGeometry(object):
    """
    Provides GEOS spatial predicates and topological operations.

    """

    # Attributes
    # ----------
    # __geom__ : c_void_p
    #     Cached ctypes pointer to GEOS geometry. Not to be accessed.
    # _geom : c_void_p
    #     Property by which the GEOS geometry is accessed.
    # __p__ : object
    #     Parent (Shapely) geometry
    # _ctypes_data : object
    #     Cached ctypes data buffer
    # _ndim : int
    #     Number of dimensions (2 or 3, generally)
    # _crs : object
    #     Coordinate reference system. Available for Shapely extensions, but
    #     not implemented here.
    # _other_owned : bool
    #     True if this object's GEOS geometry is owned by another as in the
    #     case of a multipart geometry member.
    __geom__ = EMPTY
    __p__ = None
    _ctypes_data = None
    _ndim = None
    _crs = None
    _other_owned = False
    _is_empty = True

    # Backend config
    impl = DefaultImplementation

    # a reference to the so/dll proxy to preserve access during clean up
    _lgeos = lgeos

    def __init__(self,
                 proj: geometry_pb2.ProjectionData,
                 epsg: int = 0,
                 proj4: str = ""):
        self._proj = get_proj(proj=proj, epsg=epsg, proj4=proj4)

    def empty(self, val=EMPTY):
        # TODO: defer cleanup to the implementation. We shouldn't be
        # explicitly calling a lgeos method here.
        if not self._is_empty and not self._other_owned and self.__geom__:
            try:
                self._lgeos.GEOSGeom_destroy(self.__geom__)
            except (AttributeError, TypeError):
                pass  # _lgeos might be empty on shutdown
        self._is_empty = True
        self.__geom__ = val

    def __del__(self):
        self.empty(val=None)
        self.__p__ = None
        self._proj = None

    def __str__(self):
        return "{0} {1}".format(self.wkt, str(self.proj))

    # To support pickling
    def __reduce__(self):
        return self.__class__, (), self.wkb

    # TODO, does this get called anywhere?
    def __setstate__(self, state):
        self.empty()
        self.__geom__ = deserialize_wkb(state)
        self._is_empty = False
        if lgeos.methods['has_z'](self.__geom__):
            self._ndim = 3
        else:
            self._ndim = 2

    @property
    def _geom(self):
        return self.__geom__

    @_geom.setter
    def _geom(self, val):
        self.empty()
        self._is_empty = val in [EMPTY, None]
        self.__geom__ = val

    # Operators
    # ---------

    def __and__(self, other):
        return self.intersection(other)

    def __or__(self, other):
        return self.union(other)

    def __sub__(self, other):
        return self.difference(other)

    def __xor__(self, other):
        return self.symmetric_difference(other)

    def __eq__(self, other):
        return (
                type(other) == type(self) and
                tuple(self.coords) == tuple(other.coords) and
                self.proj_eq(other.proj)
        )

    def __ne__(self, other):
        return not self.__eq__(other)

    __hash__ = None

    def proj_eq(self, other_proj: geometry_pb2.ProjectionData):
        return proj_eq(self.proj, other_proj)

    # Array and ctypes interfaces
    # ---------------------------

    @property
    def ctypes(self):
        """Return ctypes buffer"""
        raise NotImplementedError

    @property
    def array_interface_base(self):
        if sys.byteorder == 'little':
            typestr = '<f8'
        elif sys.byteorder == 'big':
            typestr = '>f8'
        else:
            raise ValueError(
                "Unsupported byteorder: neither little nor big-endian")
        return {
            'version': 3,
            'typestr': typestr,
            'data': self.ctypes,
        }

    @property
    def __array_interface__(self):
        """Provide the Numpy array protocol."""
        raise NotImplementedError

    # Coordinate access
    # -----------------

    def _get_coords(self):
        """Access to geometry's coordinates (CoordinateSequence)"""
        if self.is_empty:
            return []
        return CoordinateSequence(self)

    def _set_coords(self, ob):
        raise NotImplementedError(
            "set_coords must be provided by derived classes")

    coords = property(_get_coords, _set_coords)

    @property
    def xy(self):
        """Separate arrays of X and Y coordinate values"""
        raise NotImplementedError

    # Python feature protocol

    @property
    def __geo_interface__(self):
        """Dictionary representation of the geometry"""
        raise NotImplementedError

    # Type of geometry and its representations
    # ----------------------------------------

    def geometryType(self):
        return geometry_type_name(self._geom)

    @property
    def type(self):
        return self.geometryType()

    def to_wkb(self):
        warn("`to_wkb` is deprecated. Use the `wkb` property.",
             DeprecationWarning)
        return geom_to_wkb(self)

    def to_wkt(self):
        warn("`to_wkt` is deprecated. Use the `wkt` property.",
             DeprecationWarning)
        return geom_to_wkt(self)

    @property
    def wkt(self):
        """WKT representation of the geometry"""
        return WKTWriter(lgeos).write(self)

    @property
    def wkb(self):
        """WKB representation of the geometry"""
        return WKBWriter(lgeos).write(self)

    @property
    def wkb_hex(self):
        """WKB hex representation of the geometry"""
        return WKBWriter(lgeos).write_hex(self)

    def svg(self, scale_factor=1., **kwargs):
        """Raises NotImplementedError"""
        raise NotImplementedError

    def _repr_svg_(self):
        """SVG representation for iPython notebook"""
        svg_top = '<svg xmlns="http://www.w3.org/2000/svg" ' \
                  'xmlns:xlink="http://www.w3.org/1999/xlink" '
        if self.is_empty:
            return svg_top + '/>'
        else:
            # Establish SVG canvas that will fit all the data + small space
            xmin, ymin, xmax, ymax = self.bounds
            if xmin == xmax and ymin == ymax:
                # This is a point; buffer using an arbitrary size
                xmin, ymin, xmax, ymax = self.s_buffer(1).bounds
            else:
                # Expand bounds by a fraction of the data ranges
                expand = 0.04  # or 4%, same as R plots
                widest_part = max([xmax - xmin, ymax - ymin])
                expand_amount = widest_part * expand
                xmin -= expand_amount
                ymin -= expand_amount
                xmax += expand_amount
                ymax += expand_amount
            dx = xmax - xmin
            dy = ymax - ymin
            width = min([max([100., dx]), 300])
            height = min([max([100., dy]), 300])
            try:
                scale_factor = max([dx, dy]) / max([width, height])
            except ZeroDivisionError:
                scale_factor = 1.
            view_box = "{} {} {} {}".format(xmin, ymin, dx, dy)
            transform = "matrix(1,0,0,-1,0,{})".format(ymax + ymin)
            return svg_top + (
                'width="{1}" height="{2}" viewBox="{0}" '
                'preserveAspectRatio="xMinYMin meet">'
                '<g transform="{3}">{4}</g></svg>'
            ).format(view_box, width, height, transform,
                     self.svg(scale_factor))

    @property
    def geom_type(self):
        """Name of the geometry's type, such as 'Point'"""
        return self.geometryType()

    # Real-valued properties and methods
    # ----------------------------------

    @property
    def s_area(self):
        """Unitless area of the geometry (float)"""
        return self.impl['area'](self)

    def s_distance(self, other):
        """Unitless distance to other geometry (float)"""
        return self.impl['distance'](self, other)

    def s_hausdorff_distance(self, other):
        """Unitless hausdorff distance to other geometry (float)"""
        return self.impl['hausdorff_distance'](self, other)

    @property
    def s_length(self):
        """Unitless length of the geometry (float)"""
        return self.impl['length'](self)

    # Topological properties
    # ----------------------

    @property
    def boundary(self):
        """
        Returns a lower dimension geometry that bounds the object

        The boundary of a polygon is a line, the boundary of a line is a
        collection of points. The boundary of a point is an empty (null)
        collection.
        """
        return geom_factory(self.impl['boundary'](self), proj=self.proj)

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
        return geom_factory(self.impl['centroid'](self), proj=self.proj)

    @delegated
    def representative_point(self):
        """Returns a point guaranteed to be within the object, cheaply."""
        return geom_factory(self.impl['representative_point'](self), proj=self.proj)

    @property
    def s_convex_hull(self):
        """Imagine an elastic band stretched around the geometry: that's a
        convex hull, more or less

        The convex hull of a three member multipoint, for example, is a
        triangular polygon.
        """
        return geom_factory(self.impl['convex_hull'](self), proj=self.proj)

    @property
    def envelope(self):
        """A figure that envelopes the geometry"""
        return geom_factory(self.impl['envelope'](self), proj=self.proj)

    def s_buffer(self, distance, resolution=16, quadsegs=None,
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
            warn(
                "The `quadsegs` argument is deprecated. Use `resolution`.", DeprecationWarning, stacklevel=2)
            res = quadsegs
        else:
            res = resolution
        if mitre_limit == 0.0:
            raise ValueError(
                'Cannot compute offset from zero-length line segment')
        if cap_style == CAP_STYLE.round and join_style == JOIN_STYLE.round:
            return geom_factory(self.impl['buffer'](self, distance, res), proj=self.proj)

        if 'buffer_with_style' not in self.impl:
            raise NotImplementedError("Styled buffering not available for "
                                      "GEOS versions < 3.2.")

        return geom_factory(self.impl['buffer_with_style'](self, distance, res,
                                                           cap_style,
                                                           join_style,
                                                           mitre_limit), proj=self.proj)

    @delegated
    def s_simplify(self, tolerance, preserve_topology=True):
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
        return geom_factory(op(self, tolerance), proj=self.proj)

    # Binary operations
    # -----------------

    def s_difference(self, other):
        """Returns the difference of the geometries"""
        return geom_factory(self.impl['difference'](self, other), proj=self.proj)

    def s_intersection(self, other):
        """Returns the intersection of the geometries"""
        return geom_factory(self.impl['intersection'](self, other), proj=self.proj)

    def s_symmetric_difference(self, other):
        """Returns the symmetric difference of the geometries
        (Shapely geometry)"""
        return geom_factory(self.impl['symmetric_difference'](self, other), proj=self.proj)

    def s_union(self, other):
        """Returns the union of the geometries (Shapely geometry)"""
        return geom_factory(self.impl['union'](self, other), proj=self.proj)

    # Unary predicates
    # ----------------

    @property
    def has_z(self):
        """True if the geometry's coordinate sequence(s) have z values (are
        3-dimensional)"""
        return bool(self.impl['has_z'](self))

    @property
    def is_empty(self):
        """True if the set of points in this geometry is empty, else False"""
        return (self._geom is None) or bool(self.impl['is_empty'](self))

    @property
    def is_ring(self):
        """True if the geometry is a closed ring, else False"""
        return bool(self.impl['is_ring'](self))

    @property
    def is_closed(self):
        """True if the geometry is closed, else False

        Applicable only to 1-D geometries."""
        if self.geom_type == 'LinearRing':
            return True
        elif self.geom_type == 'LineString':
            if 'is_closed' in self.impl:
                return bool(self.impl['is_closed'](self))
            else:
                return self.coords[0] == self.coords[-1]
        else:
            return False

    @property
    def s_is_simple(self):
        """True if the geometry is simple, meaning that any self-intersections
        are only at boundary points, else False"""
        return bool(self.impl['is_simple'](self))

    @property
    def s_is_valid(self):
        """True if the geometry is valid (definition depends on sub-class),
        else False"""
        return bool(self.impl['is_valid'](self))

    # Binary predicates
    # -----------------

    def s_relate(self, other):
        """Returns the DE-9IM intersection matrix for the two geometries
        (string)"""
        return self.impl['relate'](self, other)

    def s_covers(self, other):
        """Returns True if the geometry covers the other, else False"""
        return bool(self.impl['covers'](self, other))

    def s_contains(self, other):
        """Returns True if the geometry contains the other, else False"""
        return bool(self.impl['contains'](self, other))

    def s_crosses(self, other):
        """Returns True if the geometries cross, else False"""
        return bool(self.impl['crosses'](self, other))

    def s_disjoint(self, other):
        """Returns True if geometries are disjoint, else False"""
        return bool(self.impl['disjoint'](self, other))

    def s_equals(self, other):
        """Returns True if geometries are equal, else False

        Refers to point-set equality (or topological equality), and is equivalent to
        (self.within(other) & self.contains(other))
        """
        return bool(self.impl['equals'](self, other))

    def s_intersects(self, other):
        """Returns True if geometries intersect, else False"""
        return bool(self.impl['intersects'](self, other))

    def s_overlaps(self, other):
        """Returns True if geometries overlap, else False"""
        return bool(self.impl['overlaps'](self, other))

    def s_touches(self, other):
        """Returns True if geometries touch, else False"""
        return bool(self.impl['touches'](self, other))

    def s_within(self, other):
        """Returns True if geometry is within the other, else False"""
        return bool(self.impl['within'](self, other))

    def s_equals_exact(self, other, tolerance):
        """Returns True if geometries are equal to within a specified
        tolerance

        Refers to coordinate equality, which requires coordinates to be equal
        and in the same order for all components of a geometry
        """
        return bool(self.impl['equals_exact'](self, other, tolerance))

    def s_almost_equals(self, other, decimal=6):
        """Returns True if geometries are equal at all coordinates to a
        specified decimal place

        Refers to approximate coordinate equality, which requires coordinates be
        approximately equal and in the same order for all components of a geometry.
        """
        return self.s_equals_exact(other, 0.5 * 10 ** (-decimal))

    def s_relate_pattern(self, other, pattern):
        """Returns True if the DE-9IM string code for the relationship between
        the geometries satisfies the pattern, else False"""
        pattern = c_char_p(pattern.encode('ascii'))
        return bool(self.impl['relate_pattern'](self, other, pattern))

    # Linear referencing
    # ------------------

    @delegated
    def s_project(self, other, normalized=False):
        """Returns the distance along this geometry to a point nearest the
        specified point

        If the normalized arg is True, return the distance normalized to the
        length of the linear geometry.
        """
        if normalized:
            op = self.impl['project_normalized']
        else:
            op = self.impl['project']
        return op(self, other)

    @delegated
    @exceptNull
    def interpolate(self, distance, normalized=False):
        """Return a point at the specified distance along a linear geometry

        Negative length values are taken as measured in the reverse
        direction from the end of the geometry. Out-of-range index
        values are handled by clamping them to the valid range of values.
        If the normalized arg is True, the distance will be interpreted as a
        fraction of the geometry's length.
        """
        if normalized:
            op = self.impl['interpolate_normalized']
        else:
            op = self.impl['interpolate']
        return geom_factory(op(self, distance))

    @property
    def proj(self):
        return self._proj

    @staticmethod
    def import_protobuf(geometry_data: geometry_pb2.GeometryData):
        """
        import the geometry protobuf into a shapely geometry
        :param geometry_data: geometry_pb2.GeometryData with spatial reference defined
        :return: epl.BaseGeometry
        """
        rpc_reader = RPCReader(lgeos, geometry_data)
        return rpc_reader.read()

    @staticmethod
    def from_envelope_data(envelope_data: geometry_pb2.EnvelopeData):
        pass

    @staticmethod
    def import_wkt(wkt: str, proj: geometry_pb2.ProjectionData = None, epsg: int = 0, proj4: str = ""):
        # TODO. this is messy. should be using RPCReader for this
        proj = get_proj(proj=proj, epsg=epsg, proj4=proj4)
        return BaseGeometry.import_protobuf(geometry_pb2.GeometryData(wkt=wkt, proj=proj))

    @staticmethod
    def import_wkb(wkb: bytes, proj: geometry_pb2.ProjectionData = None, epsg: int = 0, proj4: str = ""):
        # TODO. this is messy. should be using RPCReader for this
        proj = get_proj(proj=proj, epsg=epsg, proj4=proj4)
        return BaseGeometry.import_protobuf(geometry_pb2.GeometryData(wkb=wkb, proj=proj))

    @staticmethod
    def _spat_ref_create(epsg: int = 0, proj4: str = ""):
        if epsg > 0:
            return geometry_pb2.ProjectionData(epsg=epsg)
        elif len(proj4) > 0:
            return geometry_pb2.ProjectionData(proj4=proj4)
        return None

    def buffer(self, distance: float, geodetic=True):
        """
        buffer a geometry by distance. defaults to buffering the geometry in meters using Lambert Azimuthal Equal Area
        :param distance: distance in meters to buffer. If geodetic is set to false, buffers by the unit of the geometry
        provided (for instance, if wgs84, the buffer unit is degrees)
        :param geodetic: default to true. set to false if you want to buffer a geometry in it's native unit and not
        geodetic in meters
        :return: buffered geometry in spatial reference of input
        """
        if geodetic:
            return self.geodetic_buffer(distance_m=distance)
        op_request = geometry_pb2.GeometryRequest(geometry=self.geometry_data,
                                                  operator=geometry_pb2.BUFFER,
                                                  buffer_params=geometry_pb2.Params.Buffer(
                                                      distance=distance),
                                                  result_encoding=geometry_pb2.WKB)

        geometry_response = geometry_init.geometry_service.operate(op_request)
        return BaseGeometry.import_protobuf(geometry_response.geometry)

    def project(self, to_proj: geometry_pb2.ProjectionData = None, to_epsg: int = 0, to_proj4: str = ""):
        to_proj = get_proj(proj=to_proj, epsg=to_epsg, proj4=to_proj4)
        if proj_eq(self.proj, to_proj):
            # notice, no copy made here, whereas, project always copies the data
            return self

        op_request = geometry_pb2.GeometryRequest(geometry=self.geometry_data,
                                                  operator=geometry_pb2.PROJECT,
                                                  result_proj=to_proj)
        geometry_response = geometry_init.geometry_service.operate(op_request)
        return BaseGeometry.import_protobuf(geometry_response.geometry)

    def simplify(self):
        op_request = geometry_pb2.GeometryRequest(geometry=self.geometry_data,
                                                  operator=geometry_pb2.SIMPLIFY)
        geometry_response = geometry_init.geometry_service.operate(op_request)
        return BaseGeometry.import_protobuf(geometry_response.geometry)

    def convex(self):
        op_request = geometry_pb2.GeometryRequest(geometry=self.geometry_data,
                                                  operator=geometry_pb2.CONVEX_HULL)
        geometry_response = geometry_init.geometry_service.operate(op_request)
        return BaseGeometry.import_protobuf(geometry_response.geometry)

    def densify(self, max_length_m: float, geodetic=True, result_proj: geometry_pb2.ProjectionData = None):
        """
Densify a polyline or polygon by the max_length in meters. No segment will be larger than the max_length. If geodetic is set to true the densification will use the geodesic midpoint from Rapp
        Args:
            max_length_m: the maximum length of a segment.
            geodetic: bool, if true densify considers curvature of ellipsoid
            result_proj: projection for results (otherwise uses projection of input)

        Returns: densified geometry

        """
        if self.geom_type != 'Polygon' and self.geom_type != 'LineString':
            raise ValueError("only polygon or polylines")

        params = geometry_pb2.Params.Densify(max_length=max_length_m)
        operator_type = geometry_pb2.GEODETIC_DENSIFY_BY_LENGTH
        if not geodetic:
            operator_type = geometry_pb2.DENSIFY_BY_LENGTH
        op_request = geometry_pb2.GeometryRequest(geometry=self.geometry_data,
                                                  operator=operator_type,
                                                  densify_params=params,
                                                  result_proj=result_proj)
        geometry_response = geometry_init.geometry_service.operate(op_request)
        return BaseGeometry.import_protobuf(geometry_response.geometry)

    def area(self, geodetic=True):
        """
        get the area of the polygon, defaults to geodetic area.
        :param geodetic:
        :return:
        """
        if geodetic:
            return self.geodetic_area()
        return self.s_area

    def geodetic_area(self):
        """
        get the geodesic area of a polygon.
        :return: double value that is the WGS84 area of the geometry
        """
        op_area = geometry_pb2.GeometryRequest(geometry=self.geometry_data,
                                               operator=geometry_pb2.GEODETIC_AREA,
                                               result_proj=geometry_pb2.ProjectionData(epsg=4326))
        area_response = geometry_init.geometry_service.operate(op_area)
        return area_response.measure

    def distance(self, other_geom: BaseGeometry, geodetic=True):
        if geodetic:
            return self.geodetic_distance(other_geom=other_geom)
        return self.s_distance(other_geom)

    def geodetic_distance(self, other_geom: BaseGeometry):
        # TODO, requires proper implementation in Geometry Service
        centroid = self.union(other_geom, result_proj=geometry_pb2.ProjectionData(epsg=4326)).centroid
        local_proj = geometry_pb2.ProjectionData(
            custom=geometry_pb2.ProjectionData.Custom(lon_0=centroid.x,
                                                      lat_0=centroid.y))

        op_distance = geometry_pb2.GeometryRequest(left_geometry=self.geometry_data,
                                                   right_geometry=other_geom.geometry_data,
                                                   operator=geometry_pb2.DISTANCE,
                                                   operation_proj=local_proj)
        distance_response = geometry_init.geometry_service.operate(op_distance)
        return distance_response.measure

    def geodetic_buffer(self, distance_m):
        op_request = geometry_pb2.GeometryRequest(geometry=self.geometry_data,
                                                  operator=geometry_pb2.GEODESIC_BUFFER,
                                                  buffer_params=geometry_pb2.Params.Buffer(
                                                      distance=distance_m),
                                                  result_encoding=geometry_pb2.WKB)

        geometry_response = geometry_init.geometry_service.operate(op_request)
        return BaseGeometry.import_protobuf(geometry_response.geometry)

    def symmetric_difference(self,
                             other_geom: BaseGeometry,
                             operation_proj: geometry_pb2.ProjectionData = None,
                             result_proj: geometry_pb2.ProjectionData = None):
        return self._two_geom_op(other_geom=other_geom,
                                 operator_type=geometry_pb2.SYMMETRIC_DIFFERENCE,
                                 operation_proj=operation_proj,
                                 result_proj=result_proj)

    def difference(self,
                   other_geom: BaseGeometry,
                   operation_proj: geometry_pb2.ProjectionData = None,
                   result_proj: geometry_pb2.ProjectionData = None):
        return self._two_geom_op(other_geom=other_geom,
                                 operator_type=geometry_pb2.DIFFERENCE,
                                 operation_proj=operation_proj,
                                 result_proj=result_proj)

    def intersection(self,
                     other_geom: BaseGeometry,
                     operation_proj: geometry_pb2.ProjectionData = None,
                     result_proj: geometry_pb2.ProjectionData = None):
        """
        get the intersecting geometry. if the geometries intersected are in different spatial references, you'll need
        to define a result spatial reference for them both to be projected into. That result spatial reference will be
        the operation spatial reference. If you want their intersection to be in a different spatial reference than the
        results, you can define that as well
        :param other_geom: other geometry to be intersected
        :param operation_proj: the spatial reference both geometries should be projected into for the intersection operation
        :param result_proj: the resulting spatial reference of the output geometry
        :return:
        """
        return self._two_geom_op(other_geom=other_geom,
                                 operator_type=geometry_pb2.INTERSECTION,
                                 operation_proj=operation_proj,
                                 result_proj=result_proj)

    def random_multipoint(self,
                          points_per_square_km: float,
                          seed=None,
                          result_proj: geometry_pb2.ProjectionData = None):
        """
Create a multipoint geometry where all points exist within the input polygon. Points are calculated on an equal area geometry
        Args:
            points_per_square_km: desired average points per square kilometer
            seed: mersenne seed
            result_proj: if projection is desired

        Returns: multipoint

        """
        if self.geom_type != 'Polygon' and self.geom_type != 'MultiPolygon':
            raise ValueError('only implemented for Polygon')

        if seed is None:
            seed = random.randint(0, 20000000)
            print("generated seed {}".format(seed))

        params = geometry_pb2.Params.RandomPoints(seed=seed,
                                                  points_per_square_km=points_per_square_km)
        op_request = geometry_pb2.GeometryRequest(geometry=self.geometry_data,
                                                  operator=geometry_pb2.RANDOM_POINTS,
                                                  random_points_params=params,
                                                  result_proj=result_proj)

        geometry_response = geometry_init.geometry_service.operate(op_request)
        return BaseGeometry.import_protobuf(geometry_response.geometry)

    def union(self,
              other_geom: BaseGeometry,
              operation_proj: geometry_pb2.ProjectionData = None,
              result_proj: geometry_pb2.ProjectionData = None):
        if other_geom is None:
            if result_proj:
                return self.project(to_proj=result_proj)
            return self

        return self._two_geom_op(other_geom=other_geom,
                                 operator_type=geometry_pb2.UNION,
                                 operation_proj=operation_proj,
                                 result_proj=result_proj)

    @staticmethod
    def cascaded_union(geometry_iterable: Iterable,
                       percent_reduction: int = 0,
                       max_point_count: int = 0,
                       batch_size: int = 25):
        """
        union an iterable list of geometries
        @param geometry_iterable: input list of geometries
        @param percent_reduction: if generalizing the output, how much to reduce the geometry by
        @param max_point_count: if generalizing the output, what is the max number of points
        @param batch_size: batch size for number of geometries to stream up to service
        @return: a unioned geometry
        """
        intermediate = []
        for geometry in geometry_iterable:
            intermediate.append(geometry)
            if len(intermediate) == batch_size:
                # every stream_interval items
                intermediate = [geometry_init.geometry_service.op_client_stream(intermediate)]

        result = intermediate[0]
        if len(intermediate) > 1:
            result = geometry_init.geometry_service.op_client_stream(intermediate)

        if percent_reduction == 0 and max_point_count == 0:
            # if no generalize
            return result

        return result.generalize(percent_reduction=percent_reduction, max_point_count=max_point_count)

    @staticmethod
    def s_cascaded_union(geometry_iterable: Iterable[BaseGeometry]):
        """
union geometries locally using shapely union.
        @param geometry_iterable:
        @return:
        """
        geometries = [geometry for geometry in geometry_iterable]
        projs = [geometry.proj for geometry in geometries]
        sample_proj = None
        for i, proj in enumerate(projs):
            if i == len(projs) - 2:
                sample_proj = proj
                break
            if not proj_eq(proj, projs[i]):
                raise ValueError("all geometries must have the same spatial reference")

        try:
            L = len(geometries)
        except TypeError:
            geometries = [geometries]
            L = 1
        subs = (c_void_p * L)()
        for i, g in enumerate(geometries):
            subs[i] = g._geom
        collection = lgeos.GEOSGeom_createCollection(6, subs, L)
        return geom_factory(lgeos.methods['cascaded_union'](collection), proj=sample_proj)

    def _operation_proj(self,
                        other_geom: BaseGeometry,
                        operation_proj: geometry_pb2.ProjectionData = None):
        if operation_proj is None and not self.proj_eq(other_geom.proj):
            operation_proj = self.proj
            warn("left and right geometries have different proj and operation_proj is None. defaulting "
                 "operation_proj to the left geometry proj: \n{}".format(self.proj))
        return operation_proj

    def _two_geom_op(self,
                     other_geom: BaseGeometry,
                     operator_type: geometry_pb2.OperatorType,
                     operation_proj: geometry_pb2.ProjectionData = None,
                     result_proj: geometry_pb2.ProjectionData = None):
        operation_proj = self._operation_proj(other_geom=other_geom, operation_proj=operation_proj)

        op_request = geometry_pb2.GeometryRequest(left_geometry=self.geometry_data,
                                                  right_geometry=other_geom.geometry_data,
                                                  operator=operator_type,
                                                  operation_proj=operation_proj,
                                                  result_proj=result_proj)
        return BaseGeometry.import_protobuf(geometry_init.geometry_service.operate(op_request).geometry)

    def equals(self,
               other_geom: BaseGeometry,
               operation_proj: geometry_pb2.ProjectionData = None):
        """
        Returns True if geometries are equal, else False.
        :param other_geom: other geometry
        :param operation_proj: if geometries have different spatial references, project both geometries to one spatial
        reference for execution of equality
        :return:
        """
        return self._relate(other_geom=other_geom, relate_type=geometry_pb2.EQUALS, operation_proj=operation_proj)

    def contains(self,
                 other_geom: BaseGeometry,
                 operation_proj: geometry_pb2.ProjectionData = None):
        return self._relate(other_geom=other_geom, relate_type=geometry_pb2.CONTAINS, operation_proj=operation_proj)

    def within(self,
               other_geom: BaseGeometry,
               operation_proj: geometry_pb2.ProjectionData = None):
        return self._relate(other_geom=other_geom, relate_type=geometry_pb2.WITHIN, operation_proj=operation_proj)

    def touches(self,
                other_geom: BaseGeometry,
                operation_proj: geometry_pb2.ProjectionData = None):
        return self._relate(other_geom=other_geom, relate_type=geometry_pb2.TOUCHES, operation_proj=operation_proj)

    def overlaps(self,
                 other_geom: BaseGeometry,
                 operation_proj: geometry_pb2.ProjectionData = None):
        return self._relate(other_geom=other_geom, relate_type=geometry_pb2.OVERLAPS, operation_proj=operation_proj)

    def crosses(self,
                other_geom: BaseGeometry,
                operation_proj: geometry_pb2.ProjectionData = None):
        return self._relate(other_geom=other_geom, relate_type=geometry_pb2.CROSSES, operation_proj=operation_proj)

    def disjoint(self,
                 other_geom: BaseGeometry,
                 operation_proj: geometry_pb2.ProjectionData = None):
        return self._relate(other_geom=other_geom, relate_type=geometry_pb2.DISJOINT, operation_proj=operation_proj)

    def intersects(self,
                   other_geom: BaseGeometry,
                   operation_proj: geometry_pb2.ProjectionData = None):
        return self._relate(other_geom=other_geom, relate_type=geometry_pb2.INTERSECTS, operation_proj=operation_proj)

    def _relate(self,
                other_geom: BaseGeometry,
                relate_type: geometry_pb2.OperatorType,
                operation_proj: geometry_pb2.ProjectionData = None):
        operation_proj = self._operation_proj(other_geom=other_geom, operation_proj=operation_proj)

        op_request = geometry_pb2.GeometryRequest(left_geometry=self.geometry_data,
                                                  right_geometry=other_geom.geometry_data,
                                                  operator=relate_type,
                                                  operation_proj=operation_proj)
        return geometry_init.geometry_service.operate(op_request).spatial_relationship

    def generalize(self, percent_reduction=0, max_point_count=0, remove_degenerates=True):
        generalize_by_area_params = geometry_pb2.Params.GeneralizeByArea(
            percent_reduction=percent_reduction,
            max_point_count=max_point_count,
            remove_degenerates=remove_degenerates
        )

        op_request = geometry_pb2.GeometryRequest(geometry=self.geometry_data,
                                                  operator=geometry_pb2.GENERALIZE_BY_AREA,
                                                  generalize_by_area_params=generalize_by_area_params)

        geometry_response = geometry_init.geometry_service.operate(op_request)
        return BaseGeometry.import_protobuf(geometry_response.geometry)

    @property
    def geometry_data(self) -> geometry_pb2.GeometryData:
        """
        create a GeometryData protobuf object from the current geometry
        :return: GeometryData object with a defined spatial reference
        """
        return geometry_pb2.GeometryData(wkb=self.wkb, proj=self._proj)

    @property
    def envelope_data(self):
        """
        create a EnvelopeData protobuf object from the current geometry with
        the spatial reference defined from the geometry spatial reference
        :return: EnvelopeData protobuf object
        """
        return geometry_pb2.EnvelopeData(xmin=self.bounds[0],
                                         ymin=self.bounds[1],
                                         xmax=self.bounds[2],
                                         ymax=self.bounds[3],
                                         proj=self._proj)

    @property
    def shapely_dump(self):
        """
Create a shapely geometry instance from the epl geometry instance
        @return:
        """
        return shapely_loads_wkb(self.wkb)

    @property
    def carto_geom(self):
        """
Carto was not playing nice with our wrapper of shapely
        @return:
        """
        return self.shapely_dump

    @property
    def carto_bounds(self):
        """
bounds order according to carto
        @return:
        """
        b = self.bounds
        return b[0], b[2], b[1], b[3]

    def translate(self, x_offset=0.0, y_offset=0.0, geodetic=True):
        """
        translates (or offset) a geometry by the values. If geodetic (default), then offsets are in meters, otherwise,
        offsets are in unit of spatial reference (for wgs-84, unit is degrees)
        :param x_offset: offset in x direction (or in longitude direction). Offset is added to all x values in geometry
        :param y_offset: offset in y direction (or in latitude direction). Offset is added to all y values in geometry
        :param geodetic: project geometry to wgs-84 and shift geometry by meters. If set to false, offsets are executed
        in spatial reference of geometry
        :return: new geometry
        """
        local_proj = None
        if geodetic:
            centroid = self.project(to_proj=geometry_pb2.ProjectionData(epsg=4326)).centroid
            local_proj = geometry_pb2.ProjectionData(
                custom=geometry_pb2.ProjectionData.Custom(lon_0=centroid.x,
                                                          lat_0=centroid.y))

        affine_transform_params = geometry_pb2.Params.AffineTransform(x_offset=x_offset,
                                                                      y_offset=y_offset)
        op_translate = geometry_pb2.GeometryRequest(geometry=self.geometry_data,
                                                    affine_transform_params=affine_transform_params,
                                                    operator=geometry_pb2.AFFINE_TRANSFORM,
                                                    operation_proj=local_proj,
                                                    result_proj=self.proj)
        geometry_response = geometry_init.geometry_service.operate(op_translate)
        return BaseGeometry.import_protobuf(geometry_response.geometry)

    def length(self, geodetic=True):
        if not geodetic:
            return self.s_length
        op_length = geometry_pb2.GeometryRequest(geometry=self.geometry_data,
                                                 operator=geometry_pb2.GEODETIC_LENGTH)
        geometry_response = geometry_init.geometry_service.operate(op_length)
        return geometry_response.measure


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
                all(x == y for x, y in zip(self, other)) and
                self._proj_eq__(other)
        )

    def _proj_eq__(self, other):
        return proj_eq(self.proj, other.proj)

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
        # TODO, manage geometry_data with geojson and eprojishape
        if len(self._geometry_data.wkt) > 0:
            self._reader_wkt = self._lgeos.GEOSWKTReader_create()
        elif len(geometry_data.wkb) > 0:
            self._reader_wkb = self._lgeos.GEOSWKBReader_create()
        # TODO raise an exception here

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
        result = geom_factory(geom, proj=self._geometry_data.proj)
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


def get_proj(proj: geometry_pb2.ProjectionData = None, epsg: int = 0, proj4: str = ""):
    if (proj is None or (proj.epsg == 0 and len(proj.proj4) == 0 and not proj.HasField('custom'))) and \
            epsg == 0 and len(proj4) == 0:
        raise ValueError("must define a spatial reference for geometry on creation, "
                         "must be epsg or proj4 (wkt not supported)")
    if proj is None and epsg > 0:
        proj = geometry_pb2.ProjectionData(epsg=epsg)
    elif proj is None and len(proj4) > 0:
        proj = geometry_pb2.ProjectionData(proj4=proj4)
    return proj


def proj_eq(proj: geometry_pb2.ProjectionData, other_proj: geometry_pb2.ProjectionData):
    if proj.epsg > 0:
        return proj.epsg == other_proj.epsg
    elif len(proj.proj4) > 0:
        return proj.proj4 == other_proj.proj4
    elif len(proj.wkt) > 0:
        return proj.wkt == other_proj.wkt
    return False
