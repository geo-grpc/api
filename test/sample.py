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

import grpc
from shapely.wkt import loads
from shapely.geometry import LineString
from shapely.geometry import Polygon
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

    def test_buffer(self):
        self.assertTrue(True)
        polygon = Polygon([(0, 0), (1, 1), (1, 0)])

        serviceGeom = ServiceGeometry()
        serviceGeom.geometry_binary.extend([bytes(polygon.wkb)])
        serviceGeom.geometry_encoding_type = GeometryEncodingType.Value('wkb')

        opRequest = OperatorRequest(left_geometry=serviceGeom,
                                    operator_type=ServiceOperatorType.Value('Buffer'),
                                    buffer_distances=[1.2])


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
            operation_spatial_reference=outputSpatialReference)

        print("make project request")
        response2 = stub.ExecuteOperation(opRequestProject)
        print("Client received project response:\n", response2)

        expected = "MULTILINESTRING ((9 0, 8.101251062924646 0.904618578893133, 9.898748937075354 -0.904618578893133))"
        actual = response2.geometry.geometry_string[0]
        self.assertEqual(expected, actual)
