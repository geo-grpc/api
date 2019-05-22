"""
Geometry factories based on the geo interface
"""
from epl.protobuf.geometry_pb2 import SpatialReferenceData
from .point import Point, asPoint
from .linestring import LineString, asLineString
from .polygon import Polygon, asPolygon
from .multipoint import MultiPoint, asMultiPoint
from .multilinestring import MultiLineString, asMultiLineString
from .multipolygon import MultiPolygon, MultiPolygonAdapter
from .collection import GeometryCollection


def box(minx, miny, maxx, maxy, ccw=True):
    """Returns a rectangular polygon with configurable normal vector"""
    coords = [(maxx, miny), (maxx, maxy), (minx, maxy), (minx, miny)]
    if not ccw:
        coords = coords[::-1]
    return Polygon(coords)


def shape(context, sr: SpatialReferenceData = None, wkid: int = 0, proj4: str = ""):
    """Returns a new, independent geometry with coordinates *copied* from the
    context.
    """
    if hasattr(context, "__geo_interface__"):
        ob = context.__geo_interface__
    else:
        ob = context
    geom_type = ob.get("type").lower()
    if geom_type == "point":
        return Point(ob["coordinates"], sr=sr, wkid=wkid, proj4=proj4)
    elif geom_type == "linestring":
        return LineString(ob["coordinates"], sr=sr, wkid=wkid, proj4=proj4)
    elif geom_type == "polygon":
        if not ob["coordinates"]:
            return Polygon(sr=sr, wkid=wkid, proj4=proj4)
        else:
            return Polygon(ob["coordinates"][0], ob["coordinates"][1:], sr=sr, wkid=wkid, proj4=proj4)
    elif geom_type == "multipoint":
        return MultiPoint(ob["coordinates"], sr=sr, wkid=wkid, proj4=proj4)
    elif geom_type == "multilinestring":
        return MultiLineString(ob["coordinates"], sr=sr, wkid=wkid, proj4=proj4)
    elif geom_type == "multipolygon":
        return MultiPolygon(ob["coordinates"], context_type='geojson', sr=sr, wkid=wkid, proj4=proj4)
    elif geom_type == "geometrycollection":
        geoms = [shape(g, sr=sr, wkid=wkid, proj4=proj4) for g in ob.get("geometries", [])]
        return GeometryCollection(geoms)
    else:
        raise ValueError("Unknown geometry type: %s" % geom_type)


def asShape(context):
    """Adapts the context to a geometry interface. The coordinates remain
    stored in the context.
    """
    if hasattr(context, "__geo_interface__"):
        ob = context.__geo_interface__
    else:
        ob = context

    try:
        geom_type = ob.get("type").lower()
    except AttributeError:
        raise ValueError("Context does not provide geo interface")

    if geom_type == "point":
        return asPoint(ob["coordinates"])
    elif geom_type == "linestring":
        return asLineString(ob["coordinates"])
    elif geom_type == "polygon":
        return asPolygon(ob["coordinates"][0], ob["coordinates"][1:])
    elif geom_type == "multipoint":
        return asMultiPoint(ob["coordinates"])
    elif geom_type == "multilinestring":
        return asMultiLineString(ob["coordinates"])
    elif geom_type == "multipolygon":
        return MultiPolygonAdapter(ob["coordinates"], context_type='geojson')
    elif geom_type == "geometrycollection":
        geoms = [asShape(g) for g in ob.get("geometries", [])]
        return GeometryCollection(geoms)
    else:
        raise ValueError("Unknown geometry type: %s" % geom_type)


def mapping(ob):
    """Returns a GeoJSON-like mapping"""
    return ob.__geo_interface__
