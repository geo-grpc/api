import os

import grpc

from .base import CAP_STYLE, JOIN_STYLE
from .geo import box, shape, asShape, mapping
from .point import Point, asPoint
from .linestring import LineString, asLineString
from .polygon import Polygon, asPolygon, LinearRing, asLinearRing
from .multipoint import MultiPoint, asMultiPoint
from .multilinestring import MultiLineString, asMultiLineString
from .multipolygon import MultiPolygon, asMultiPolygon
from .collection import GeometryCollection
from epl.protobuf import geometry_service_pb2_grpc

__all__ = [
    'box', 'shape', 'asShape', 'Point', 'asPoint', 'LineString',
    'asLineString', 'Polygon', 'asPolygon', 'MultiPoint', 'asMultiPoint',
    'MultiLineString', 'asMultiLineString', 'MultiPolygon', 'asMultiPolygon',
    'GeometryCollection', 'mapping', 'LinearRing', 'asLinearRing',
    'CAP_STYLE', 'JOIN_STYLE',
    'geometry_service',
]

# This needs to be called here to avoid circular references
import shapely.speedups


class __GeometryServiceStub(object):
    def __init__(self):
        address = os.getenv("GEOMETRY_SERVICE_HOST", 'localhost:8980')
        channel = grpc.insecure_channel(address)
        self._stub = geometry_service_pb2_grpc.GeometryServiceStub(channel)

    @property
    def stub(self):
        return self._stub

    def set_channel(self, channel):
        """
        This allows you to override the channel created on init, with another channel. This might be needed if multiple
        libraries are using the same channel, or if multi-threading.
        :param channel:
        :return:
        """
        self._stub = geometry_service_pb2_grpc.GeometryServiceStub(channel)


geometry_service = __GeometryServiceStub()
