# Copyright 2017 Echo Park Labs
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# For additional information, contact:
#
# email: info@echoparklabs.io



import os
import unittest
import random
import datetime
import grpc

from functools import partial

from shapely.wkt import loads
from shapely.wkb import loads as wkbloads
from shapely.geometry import Polygon
from shapely.geometry import LineString, Point
from shapely.ops import cascaded_union
from epl.geometry.geometry_operators_pb2 import *
import epl.geometry.geometry_operators_pb2_grpc as geometry_grpc


class TestBasic(unittest.TestCase):

    channel = None

    def setUp(self):
        # TODO setup environment variable
        address = os.getenv("GEOMETRY_SERVICE_HOST", 'localhost:8980')
        #
        # print("connect to address: ", address)
        # print("create channel")
        self.channel = grpc.insecure_channel(address)

        # options
        # https://groups.google.com/forum/#!topic/grpc-io/ZtBCw4ZqLqE
        # https://github.com/justdoit0823/grpc-resolver/blob/master/grpcresolver/registry.py

    def test_buffer(self):
        self.assertTrue(True)
        polygon = Polygon([(0, 0), (1, 1), (1, 0)])

        serviceGeom = ServiceGeometry()
        serviceGeom.geometry_binary.extend([polygon.wkb])
        serviceGeom.geometry_encoding_type = GeometryEncodingType.Value('wkb')

        opRequest = OperatorRequest(left_geometry=serviceGeom,
                                    operator_type=ServiceOperatorType.Value('Buffer'),
                                    buffer_distances=[1.2],
                                    results_encoding_type="wkt")

        print("make stub")
        stub = geometry_grpc.GeometryOperatorsStub(self.channel)

        print("make wkt request")
        response = stub.ExecuteOperation(opRequest)
        # print response
        print("Client received wkt response:\n", response)
        result_buffered = loads(response.geometry.geometry_string[0])
        self.assertTrue(result_buffered.contains(polygon))
        shapely_buffer = polygon.buffer(1.2)
        self.assertAlmostEqual(shapely_buffer.area, result_buffered.area, 2)

    def test_project(self):
        stub = geometry_grpc.GeometryOperatorsStub(self.channel)
        serviceSpatialReference = ServiceSpatialReference(wkid=32632)
        outputSpatialReference = ServiceSpatialReference(wkid=4326)
        polyline = LineString([(500000,       0), (400000,  100000), (600000, -100000)])

        serviceGeomPolyline = ServiceGeometry(
            geometry_string=[polyline.wkt],
            geometry_encoding_type=GeometryEncodingType.Value('wkt'),
            spatial_reference=serviceSpatialReference)

        opRequestProject = OperatorRequest(
            left_geometry=serviceGeomPolyline,
            operator_type=ServiceOperatorType.Value('Project'),
            operation_spatial_reference=outputSpatialReference,
            results_encoding_type="wkt")

        print("make project request")
        response2 = stub.ExecuteOperation(opRequestProject)
        print("Client received project response:\n", response2)

        expected = "MULTILINESTRING ((9 0, 8.101251062924646 0.904618578893133, 9.898748937075354 -0.904618578893133))"
        actual = response2.geometry.geometry_string[0]
        self.assertEqual(expected, actual)

    def test_union(self):
        # Build patches as in dissolved.py
        r = partial(random.uniform, -20.0, 20.0)

        points = [Point(r(), r()) for i in range(10000)]
        spots = [p.buffer(2.5) for p in points]
        shape_start = datetime.datetime.now()
        patches = cascaded_union(spots)
        shape_end = datetime.datetime.now()
        shape_delta = shape_end - shape_start
        shape_microseconds = int(shape_delta.total_seconds() * 1000)
        print(shape_microseconds)
        print(patches.wkt)
        stub = geometry_grpc.GeometryOperatorsStub(self.channel)

        serviceGeom = ServiceGeometry()
        serviceGeom.geometry_binary.extend(spots)
        serviceGeom.geometry_encoding_type = GeometryEncodingType.Value('wkb')

        opRequestUnion = OperatorRequest(left_geometry=serviceGeom,
                                         operator_type=ServiceOperatorType.Value('Union'))

        epl_start = datetime.datetime.now()
        response = stub.ExecuteOperation(opRequestUnion)
        unioned_result = wkbloads(response.geometry.geometry_binary[0])
        epl_end = datetime.datetime.now()
        epl_delta = epl_end - epl_start
        epl_microseconds = int(epl_delta.total_seconds() * 1000)
        self.assertGreater(shape_microseconds, epl_microseconds)
        # self.assertGreater(shape_microseconds / 8, epl_microseconds)

        # self.assertAlmostEqual(patches.area, unioned_result.area, 4)


        # print("make stub")
        # stub = geometry_grpc.GeometryOperatorsStub(self.channel)
        #
        # print("make wkt request")
        # response = stub.ExecuteOperation(opRequest)
        # # print response
        # print("Client received wkt response:\n", response)
        # result_buffered = loads(response.geometry.geometry_string[0])
        # self.assertTrue(result_buffered.contains(polygon))
        # shapely_buffer = polygon.buffer(1.2)
        # self.assertAlmostEqual(shapely_buffer.area, result_buffered.area, 2)