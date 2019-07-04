"""Points and related utilities
"""

from ctypes import c_double

from epl.protobuf import geometry_pb2
from epl.geometry.base import BaseGeometry, geos_geom_from_py
from epl.geometry.linestring import LineString

from shapely.errors import DimensionError
from shapely.geos import lgeos
from shapely.geometry.proxy import CachingGeometryProxy

from epl import geometry as geometry_init

__all__ = ['Point', 'asPoint']


class Point(BaseGeometry):
    """
    A zero dimensional feature

    A point has zero length and zero area.

    Attributes
    ----------
    x, y, z : float
        Coordinate values

    Example
    -------
      >>> p = Point(1.0, -1.0)
      >>> print(p)
      POINT (1.0000000000000000 -1.0000000000000000)
      >>> p.y
      -1.0
      >>> p.x
      1.0
    """

    def __init__(self,
                 *args,
                 sr: geometry_pb2.SpatialReferenceData = None,
                 wkid: int = 0,
                 proj4: str = ""):
        """
        Parameters
        ----------
        There are 2 cases:

        1) 1 parameter: this must satisfy the numpy array protocol.
        2) 2 or more parameters: x, y, z : float
            Easting, northing, and elevation.
        """
        BaseGeometry.__init__(self, sr=sr, wkid=wkid, proj4=proj4)
        if len(args) > 0:
            self._set_coords(*args)

    # Coordinate getters and setters

    @property
    def x(self):
        """Return x coordinate."""
        return self.coords[0][0]

    @property
    def y(self):
        """Return y coordinate."""
        return self.coords[0][1]

    @property
    def z(self):
        """Return z coordinate."""
        if self._ndim != 3:
            raise DimensionError("This point has no z coordinate.")
        return self.coords[0][2]

    @property
    def __geo_interface__(self):
        return {
            'type': 'Point',
            'coordinates': self.coords[0]
        }

    def svg(self, scale_factor=1., fill_color=None):
        """Returns SVG circle element for the Point geometry.

        Parameters
        ==========
        scale_factor : float
            Multiplication factor for the SVG circle diameter.  Default is 1.
        fill_color : str, optional
            Hex string for fill color. Default is to use "#66cc99" if
            geometry is valid, and "#ff3333" if invalid.
        """
        if self.is_empty:
            return '<g />'
        if fill_color is None:
            fill_color = "#66cc99" if self.is_valid else "#ff3333"
        return (
            '<circle cx="{0.x}" cy="{0.y}" r="{1}" '
            'stroke="#555555" stroke-width="{2}" fill="{3}" opacity="0.6" />'
        ).format(self, 3. * scale_factor, 1. * scale_factor, fill_color)

    @property
    def ctypes(self):
        if not self._ctypes_data:
            array_type = c_double * self._ndim
            array = array_type()
            xy = self.coords[0]
            array[0] = xy[0]
            array[1] = xy[1]
            if self._ndim == 3:
                array[2] = xy[2]
            self._ctypes_data = array
        return self._ctypes_data

    def array_interface(self):
        """Provide the Numpy array protocol."""
        if self.is_empty:
            ai = {'version': 3, 'typestr': '<f8', 'shape': (0,), 'data': (c_double * 0)()}
        else:
            ai = self.array_interface_base
            ai.update({'shape': (self._ndim,)})
        return ai

    __array_interface__ = property(array_interface)

    @property
    def bounds(self):
        xy = self.coords[0]
        return (xy[0], xy[1], xy[0], xy[1])

    # Coordinate access

    def _set_coords(self, *args):
        self.empty()
        if len(args) == 1:
            self._geom, self._ndim = geos_point_from_py(args[0])
        elif len(args) > 3:
            raise TypeError("Point() takes at most 3 arguments ({} given)".format(len(args)))
        else:
            self._geom, self._ndim = geos_point_from_py(tuple(args))

    coords = property(BaseGeometry._get_coords, _set_coords)

    @property
    def xy(self):
        """Separate arrays of X and Y coordinate values

        Example:
          >>> x, y = Point(0, 0).xy
          >>> list(x)
          [0.0]
          >>> list(y)
          [0.0]
        """
        return self.coords.xy

    def geodetic_inverse(self, other):
        """
        get the azimuth from this point to other, the azimuth from other to this point, and the distance. uses
        geodetic inverse calculations based off of the flattening of the ellipsoid associated with the data provided.
        azimuths are in radians. 0 is north, PI is south, PI/2 is east, -PI/2 is west
        :param other: Point geometry (other types not accepted)
        :return: azimuth from this point to other, azimuth from other to this point, distance in meters between points
        """
        if not isinstance(other, Point):
            raise ValueError("other geometry must be point")
        op_inverse = geometry_pb2.GeometryRequest(left_geometry=self.geometry_data,
                                                  right_geometry=other.geometry_data,
                                                  operator=geometry_pb2.GEODETIC_INVERSE)
        geometry_response = geometry_init.geometry_service.stub.Operate(op_inverse)
        results = geometry_response.geodetic_inverse
        return results.az12, results.az21, results.distance

    # TODO, make this geodetic
    def midpoint(self, other, geodetic=True):
        if geodetic:
            raise NotImplementedError("geodetic not yet implemented")
        if not isinstance(other, Point):
            raise ValueError("other geometry must be point")

        if self._ndim != other._ndim:
            raise ValueError("points must have the same dimension")

        if self._ndim == 2:
            return Point((self.x + other.x) / 2.0, (self.y + other.y) / 2.0, sr=self.sr)
        return Point((self.x + other.x) / 2.0, (self.y + other.y) / 2.0, (self.z + other.z) / 2.0, sr=self.sr)


class PointAdapter(CachingGeometryProxy, Point):
    _other_owned = False

    def __init__(self, context):
        self.context = context
        self.factory = geos_point_from_py

    @property
    def _ndim(self):
        try:
            # From array protocol
            array = self.context.__array_interface__
            n = array['shape'][0]
            assert n == 2 or n == 3
            return n
        except AttributeError:
            # Fall back on list
            return len(self.context)

    @property
    def __array_interface__(self):
        """Provide the Numpy array protocol."""
        try:
            return self.context.__array_interface__
        except AttributeError:
            return self.array_interface()

    _get_coords = BaseGeometry._get_coords

    def _set_coords(self, ob):
        raise NotImplementedError("Adapters can not modify their sources")

    coords = property(_get_coords)


def asPoint(context):
    """Adapt an object to the Point interface"""
    return PointAdapter(context)


def geos_point_from_py(ob, update_geom=None, update_ndim=0):
    """Create a GEOS geom from an object that is a Point, a coordinate sequence
    or that provides the array interface.

    Returns the GEOS geometry and the number of its dimensions.
    """
    if isinstance(ob, Point):
        return geos_geom_from_py(ob)

    # Accept either (x, y) or [(x, y)]
    if not hasattr(ob, '__getitem__'):  # Iterators, e.g. Python 3 zip
        ob = list(ob)

    if isinstance(ob[0], tuple):
        coords = ob[0]
    else:
        coords = ob
    n = len(coords)
    dx = c_double(coords[0])
    dy = c_double(coords[1])
    dz = None
    if n == 3:
        dz = c_double(coords[2])

    if update_geom:
        cs = lgeos.GEOSGeom_getCoordSeq(update_geom)
        if n != update_ndim:
            raise ValueError(
                "Wrong coordinate dimensions; this geometry has dimensions: "
                "%d" % update_ndim)
    else:
        cs = lgeos.GEOSCoordSeq_create(1, n)

    # Because of a bug in the GEOS C API, always set X before Y
    lgeos.GEOSCoordSeq_setX(cs, 0, dx)
    lgeos.GEOSCoordSeq_setY(cs, 0, dy)
    if n == 3:
        lgeos.GEOSCoordSeq_setZ(cs, 0, dz)

    if update_geom:
        return None
    else:
        return lgeos.GEOSGeom_createPoint(cs), n


def update_point_from_py(geom, ob):
    geos_point_from_py(ob, geom._geom, geom._ndim)
