import os

import grpc

from typing import Iterable, Iterator
from .base import CAP_STYLE, JOIN_STYLE, BaseGeometry
from .geo import box, shape, asShape, mapping
from .point import Point, asPoint
from .linestring import LineString, asLineString
from .polygon import Polygon, asPolygon, LinearRing, asLinearRing
from .multipoint import MultiPoint, asMultiPoint
from .multilinestring import MultiLineString, asMultiLineString
from .multipolygon import MultiPolygon, asMultiPolygon
from .collection import GeometryCollection
from epl.protobuf.v1 import geometry_service_pb2_grpc, geometry_pb2

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

# https://github.com/grpc/grpc/blob/1edfb952d0aa12bdc154e85eab24e87621292366/doc/connection-backoff.md
MIN_CONNECT_TIMEOUT = 20
INITIAL_BACKOFF = 1
MULTIPLIER = 1.6
MAX_BACKOFF = 120
JITTER = 0.2


class __GeometryServiceStub(object):
    def __init__(self):
        address = os.getenv("GEOMETRY_SERVICE_HOST", 'localhost:8980')
        channel = grpc.insecure_channel(address)
        try:
            grpc.channel_ready_future(channel).result(timeout=10)
        except grpc.FutureTimeoutError as e:
            raise e
        else:
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

    def operate(self, operator_request: geometry_pb2.GeometryRequest, timeout=60):
        return self._stub.Operate(operator_request, timeout=timeout)

    def op_client_stream(self, geometry_iterable: Iterable, timeout=60):
        input_generator = self._gen_union(geometry_iterable)
        geometry_response = self._stub.OperateClientStream(input_generator, timeout=timeout)
        return BaseGeometry.import_protobuf(geometry_response.geometry)

    @staticmethod
    def _gen_union(geometry_iterable: Iterable) -> Iterator[geometry_pb2.GeometryRequest]:
        for geometry in geometry_iterable:
            yield geometry_pb2.GeometryRequest(geometry=geometry.geometry_data,
                                               operator=geometry_pb2.UNION)


geometry_service: __GeometryServiceStub


def _init_geometry_service():
    global geometry_service
    geometry_service = __GeometryServiceStub()
    return geometry_service


def __getattr__(name):
    if name == 'geometry_service':
        return _init_geometry_service()
    return globals()[name]
