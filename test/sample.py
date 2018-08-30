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
import math

from functools import partial

from shapely.wkt import loads
from shapely.wkb import loads as wkbloads
from shapely.geometry import Polygon
from shapely.geometry import LineString, Point
from shapely.geometry import MultiPoint
from shapely.ops import cascaded_union
from epl.grpc.geometry.geometry_operators_pb2 import *
import epl.grpc.geometry.geometry_operators_pb2_grpc as geometry_grpc
import numpy as np


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

        geometryBagData = GeometryBagData()
        geometryBagData.wkb.extend([polygon.wkb])
        # geometryBagData.geometry_binaries.extend([polygon.wkb])
        # geometryBagData.geometry_encoding_type = GeometryEncodingType.Value('wkb')

        buffer_params = BufferParams(distances=[1.2])

        opRequest = OperatorRequest(left_geometry_bag=geometryBagData,
                                    operator_type=ServiceOperatorType.Value('Buffer'),
                                    buffer_params=buffer_params,
                                    results_encoding_type=GeometryEncodingType.Value('wkt'))

        print("make stub")
        stub = geometry_grpc.GeometryOperatorsStub(self.channel)

        print("make wkt request")
        response = stub.ExecuteOperation(opRequest)
        # print response
        print("Client received wkt response:\n", response)
        result_buffered = loads(response.geometry_bag.wkt[0])
        self.assertTrue(result_buffered.contains(polygon))
        shapely_buffer = polygon.buffer(1.2)
        self.assertAlmostEqual(shapely_buffer.area, result_buffered.area, 2)

    def test_project(self):
        stub = geometry_grpc.GeometryOperatorsStub(self.channel)
        serviceSpatialReference = SpatialReferenceData(wkid=32632)
        outputSpatialReference = SpatialReferenceData(wkid=4326)
        polyline = LineString([(500000,       0), (400000,  100000), (600000, -100000)])

        a = EnvelopeData(xmin=1, ymin=2, xmax=4, ymax=6)
        serviceGeomPolyline = GeometryBagData(
            wkt=[polyline.wkt],
            spatial_reference=serviceSpatialReference)

        opRequestProject = OperatorRequest(
            left_geometry_bag=serviceGeomPolyline,
            operator_type=ServiceOperatorType.Value('Project'),
            operation_spatial_reference=outputSpatialReference,
            results_encoding_type=GeometryEncodingType.Value('wkt'))

        print("make project request")
        response2 = stub.ExecuteOperation(opRequestProject)
        print("Client received project response:\n", response2)

        expected = "MULTILINESTRING ((9 0, 8.101251062924646 0.904618578893133, 9.898748937075354 -0.904618578893133))"

        actual = response2.geometry_bag.wkt[0]

        opEquals = OperatorRequest(
            left_geometry_bag=GeometryBagData(wkt=[expected]),
            right_geometry_bag=response2.geometry_bag,
            operator_type=ServiceOperatorType.Value("Equals"),
            operation_spatial_reference=outputSpatialReference)

        response3 = stub.ExecuteOperation(opEquals)

        self.assertTrue(response3.spatial_relationship)

    def test_exception(self):
        stub = geometry_grpc.GeometryOperatorsStub(self.channel)
        serviceSpatialReference = SpatialReferenceData(wkid=32632)
        outputSpatialReference = SpatialReferenceData(wkid=4326)
        polyline = LineString([(500000,       0), (400000,  100000), (600000, -100000)])

        a = EnvelopeData(xmin=1, ymin=2, xmax=4, ymax=6)
        serviceGeomPolyline = GeometryBagData(
            wkt=[polyline.wkt],
            spatial_reference=serviceSpatialReference)

        opRequestProject = OperatorRequest(
            left_geometry_bag=serviceGeomPolyline,
            operator_type=ServiceOperatorType.Value('Project'),
            operation_spatial_reference=outputSpatialReference,
            results_encoding_type=GeometryEncodingType.Value('wkt'))

        print("make project request")
        response2 = stub.ExecuteOperation(opRequestProject)
        print("Client received project response:\n", response2)

        expected = "MULTILINESTRING ((9 0, 8.101251062924646 0.904618578893133, 9.898748937075354 -0.904618578893133))"

        actual = response2.geometry_bag.wkt[0]

        opEquals = OperatorRequest(
            left_geometry_bag=serviceGeomPolyline,
            right_geometry_bag=response2.geometry_bag,
            operator_type=ServiceOperatorType.Value("Equals"),
            operation_spatial_reference=outputSpatialReference)

        try:
            response3 = stub.ExecuteOperation(opEquals)
            self.assertTrue(False)
        except grpc.RpcError as e:
            self.assertEqual('executeOperation error : java.lang.IllegalArgumentException: either both spatial references are local or neither', e.details())



    def test_multipoint(self):
        stub = geometry_grpc.GeometryOperatorsStub(self.channel)
        serviceSpatialReference = SpatialReferenceData(wkid=4326)
        outputSpatialReference = SpatialReferenceData(wkid=3857)
        multipoints_array = []
        for longitude in range(-180, 180, 10):
            for latitude in range(-80, 80, 10):
                multipoints_array.append((longitude, latitude))

        multipoint = MultiPoint(multipoints_array)

        serviceGeomPolyline = GeometryBagData(
            wkt=[multipoint.wkt],
            spatial_reference=serviceSpatialReference)

        opRequestProject = OperatorRequest(
            left_geometry_bag=serviceGeomPolyline,
            operator_type=ServiceOperatorType.Value('Project'),
            operation_spatial_reference=outputSpatialReference)

        opRequestOuter = OperatorRequest(
            left_geometry_request=opRequestProject,
            operator_type=ServiceOperatorType.Value('Project'),
            operation_spatial_reference=serviceSpatialReference,
            results_encoding_type=GeometryEncodingType.Value('wkt'))

        print("make project request")
        response = stub.ExecuteOperation(opRequestOuter)
        print("Client received project response:\n", response)
        round_trip_result_wkt = loads(response.geometry_bag.wkt[0])

        opRequestOuter = OperatorRequest(
            left_geometry_request=opRequestProject,
            operator_type=ServiceOperatorType.Value('Project'),
            operation_spatial_reference=serviceSpatialReference,
            results_encoding_type=GeometryEncodingType.Value('wkb'))
        response = stub.ExecuteOperation(opRequestOuter)
        round_trip_result = wkbloads(response.geometry_bag.wkb[0])
        self.assertIsNotNone(round_trip_result)

        max_diff_lat = 0.0
        max_diff_lon = 0.0
        for index, projected_point in enumerate(round_trip_result):
            original_point = multipoint[index]
            if math.fabs(original_point.x - projected_point.x) > max_diff_lon:
                max_diff_lon = math.fabs(original_point.x - projected_point.x)
            if math.fabs(original_point.y - projected_point.y) > max_diff_lat:
                max_diff_lat = math.fabs(original_point.y - projected_point.y)

            self.assertLess(math.fabs(original_point.x - projected_point.x), 0.0000001)
            self.assertLess(math.fabs(original_point.y - projected_point.y), 0.0000001)

        for index, projected_point in enumerate(round_trip_result_wkt):
            original_point = multipoint[index]
            if math.fabs(original_point.x - projected_point.x) > max_diff_lon:
                max_diff_lon = math.fabs(original_point.x - projected_point.x)
            if math.fabs(original_point.y - projected_point.y) > max_diff_lat:
                max_diff_lat = math.fabs(original_point.y - projected_point.y)

            self.assertLess(math.fabs(original_point.x - projected_point.x), 0.00000001)
            self.assertLess(math.fabs(original_point.y - projected_point.y), 0.00000001)


    # unittest.skip("performance problems with union")
    # Welp, this test has non-simple geometries from shapely
    def test_union(self):
        # Build patches as in dissolved.py
        stub = geometry_grpc.GeometryOperatorsStub(self.channel)
        r = partial(random.uniform, -20.0, 20.0)
        serviceSpatialReference = SpatialReferenceData(wkid=4326)
        points = [Point(r(), r()) for i in range(10000)]
        spots = [p.buffer(2.5) for p in points]
        service_multipoint = GeometryBagData(spatial_reference=serviceSpatialReference,
                                             geometry_encoding_type=GeometryEncodingType.Value('wkb'))
        shape_start = datetime.datetime.now()
        patches = cascaded_union(spots)
        # because shapely is non-simple we need to simplify it for this to be a fair comparison
        service_multipoint.wkb.extend([patches.wkb])
        opRequestOuter = OperatorRequest(
            left_geometry_bag=service_multipoint,
            operator_type=ServiceOperatorType.Value('Simplify'),
            operation_spatial_reference=serviceSpatialReference,
            results_encoding_type=GeometryEncodingType.Value('wkb'))
        response = stub.ExecuteOperation(opRequestOuter)
        patches = wkbloads(response.geometry_bag.wkb[0])
        shape_end = datetime.datetime.now()
        shape_delta = shape_end - shape_start
        shape_microseconds = int(shape_delta.total_seconds() * 1000)
        print(shape_microseconds)
        print(patches.wkt)


        spots_wkb = [s.wkb for s in spots]
        geometryBagData = GeometryBagData()
        geometryBagData.wkb.extend(spots_wkb)

        opRequestUnion = OperatorRequest(left_geometry_bag=geometryBagData,
                                         operator_type=ServiceOperatorType.Value('Union'))

        epl_start = datetime.datetime.now()
        response = stub.ExecuteOperation(opRequestUnion)
        unioned_result = wkbloads(response.geometry_bag.wkb[0])

        epl_end = datetime.datetime.now()
        epl_delta = epl_end - epl_start
        epl_microseconds = int(epl_delta.total_seconds() * 1000)
        # TODO investigate why dev machine is faster for epl and slower for shapely and the alternative is true for test machines (memory limits?)
        # self.assertGreater(shape_microseconds, epl_microseconds)
        # self.assertGreater(shape_microseconds * 0.75, epl_microseconds)

        self.assertAlmostEqual(patches.area, unioned_result.area, 8)

    # tests exception handling for points outside the range of projection
    def test_ETRS(self):
        geometry_strings = []

        change_interval = 15
        start_lon = -180.0
        end_lon = 180.0
        start_latitude = -90.0
        end_latitude = 90.0

        m = int(((end_lon - start_lon) / change_interval) * ((end_latitude - start_latitude) / change_interval))

        D = 2  # dimensionality
        X = np.zeros((m, D))  # data matrix where each row is a single example

        idx = 0
        original_points = []
        for longitude in np.arange(-180.0, 180.0, change_interval):
            for latitude in np.arange(-90, 90, change_interval):
                X[idx] = (longitude, latitude)
                idx += 1

                point = Point(longitude, latitude)
                original_points.append(point)
                geometry_strings.append(point.wkt)

        stub = geometry_grpc.GeometryOperatorsStub(self.channel)
        serviceSpatialReference = SpatialReferenceData(wkid=4326)
        outputSpatialReference = SpatialReferenceData(wkid=3035)
        # outputSpatialReference = SpatialReferenceData(wkid=32632)

        serviceGeomPolyline = GeometryBagData(
            wkt=geometry_strings,
            geometry_encoding_type=GeometryEncodingType.Value('wkt'),
            spatial_reference=serviceSpatialReference)

        opRequestProject = OperatorRequest(
            left_geometry_bag=serviceGeomPolyline,
            operator_type=ServiceOperatorType.Value('Project'),
            operation_spatial_reference=outputSpatialReference)

        opRequestOuter = OperatorRequest(
            left_geometry_request=opRequestProject,
            operator_type=ServiceOperatorType.Value('Project'),
            operation_spatial_reference=serviceSpatialReference,
            results_encoding_type=GeometryEncodingType.Value('wkt'))

        # print("make project request")
        response = stub.ExecuteOperation(opRequestOuter)
        # print("Client received project response:\n", response)
        projected_point_array = [loads(wkt) for wkt in response.geometry_bag.wkt]

        for index, original in enumerate(original_points):
            point_projected = projected_point_array[index]
            if point_projected.wkt == 'POINT EMPTY':
                continue
            if original.x == -180 or original.x == 180:
                continue
            self.assertAlmostEqual(point_projected.x, original.x, 8)
