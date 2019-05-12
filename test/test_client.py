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
import math

from shapely.wkt import loads
from shapely.wkb import loads as wkbloads
from epl.geometry import Point, MultiPoint, Polygon, LineString

from epl.protobuf import geometry_pb2
import epl.protobuf.geometry_service_pb2_grpc as geometry_grpc
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
        polygon = Polygon([(0, 0), (1, 1), (1, 0)], sr=geometry_pb2.SpatialReferenceData(wkid=4326))

        buffer_params = geometry_pb2.GeometryRequest.BufferParams(distance=1.2)

        op_request = geometry_pb2.GeometryRequest(left_geometry=polygon.export_protobuf(),
                                                  operator=geometry_pb2.BUFFER,
                                                  buffer_params=buffer_params,
                                                  result_encoding=geometry_pb2.WKT)

        print("make stub")
        stub = geometry_grpc.GeometryServiceStub(self.channel)

        print("make wkt request")
        response = stub.GeometryOperationUnary(op_request)
        # print response
        print("Client received wkt response:\n", response)
        result_buffered = loads(response.geometry.wkt)
        self.assertTrue(result_buffered.contains(polygon))
        shapely_buffer = polygon.buffer(1.2)
        self.assertAlmostEqual(shapely_buffer.area, result_buffered.area, 2)

    def test_remote_buffer(self):
        polygon = Polygon([(0, 0), (1, 1), (1, 0)], sr=geometry_pb2.SpatialReferenceData(wkid=4326))

        buffer_params = geometry_pb2.GeometryRequest.BufferParams(distance=1.2)

        op_request = geometry_pb2.GeometryRequest(left_geometry=polygon.export_protobuf(),
                                                  operator=geometry_pb2.BUFFER,
                                                  buffer_params=buffer_params,
                                                  result_encoding=geometry_pb2.WKT)

        print("make stub")
        stub = geometry_grpc.GeometryServiceStub(self.channel)

        print("make wkt request")
        response = stub.GeometryOperationUnary(op_request)
        new_polygon = polygon.import_protobuf(response.geometry)
        shapely_buffer = polygon.buffer(1.2)
        self.assertAlmostEqual(shapely_buffer.area, new_polygon.area, 2)

        another_new_polygon = polygon.remote_buffer(1.2)
        self.assertAlmostEqual(shapely_buffer.area, another_new_polygon.area, 2)
        print(another_new_polygon.export_protobuf())

    def test_remote_buffer_bounds(self):
        polygon = Polygon([(0, 0), (1, 1), (1, 0)], sr=geometry_pb2.SpatialReferenceData(wkid=4326))
        buffered = polygon.remote_buffer(33)
        self.assertEqual(-33, buffered.envelope_data.ymin)
        self.assertEqual(-33, buffered.envelope_data.xmin)
        self.assertEqual(34, buffered.envelope_data.xmax)
        self.assertEqual(34, buffered.envelope_data.ymax)
        buffered = polygon.buffer(33)
        self.assertEqual(-33, buffered.envelope_data.ymin)
        self.assertEqual(-33, buffered.envelope_data.xmin)
        self.assertEqual(34, buffered.envelope_data.xmax)
        self.assertEqual(34, buffered.envelope_data.ymax)
        self.assertEqual(4326, buffered.sr.wkid)

    def test_project(self):
        stub = geometry_grpc.GeometryServiceStub(self.channel)
        service_sr = geometry_pb2.SpatialReferenceData(wkid=32632)
        output_sr = geometry_pb2.SpatialReferenceData(wkid=4326)
        polyline = LineString([(500000, 0), (400000, 100000), (600000, -100000)], sr=service_sr)

        a = geometry_pb2.EnvelopeData(xmin=1, ymin=2, xmax=4, ymax=6)
        service_geom_polyline = geometry_pb2.GeometryData(
            wkt=polyline.wkt,
            sr=service_sr)

        op_request_project = geometry_pb2.GeometryRequest(
            left_geometry=service_geom_polyline,
            operator=geometry_pb2.PROJECT,
            operation_sr=output_sr,
            result_encoding=geometry_pb2.WKT)

        print("make project request")
        response2 = stub.GeometryOperationUnary(op_request_project)
        print("Client received project response:\n", response2)

        expected = "MULTILINESTRING ((9 0, 8.101251062924646 0.904618578893133, 9.898748937075354 -0.904618578893133))"

        actual = response2.geometry.wkt

        op_equals = geometry_pb2.GeometryRequest(
            left_geometry=geometry_pb2.GeometryData(wkt=expected),
            right_geometry=response2.geometry,
            operator=geometry_pb2.EQUALS,
            operation_sr=output_sr)

        response3 = stub.GeometryOperationUnary(op_equals)

        self.assertTrue(response3.spatial_relationship)

    def test_remote_project(self):
        service_sr = geometry_pb2.SpatialReferenceData(wkid=32632)
        output_sr = geometry_pb2.SpatialReferenceData(wkid=4326)
        polyline = LineString([(500000, 0), (400000, 100000), (600000, -100000)],
                              sr=service_sr)
        print("make project request")
        projected = polyline.remote_project(output_sr)
        print("Client received project response:\n", projected.wkt)
        print(projected.wkt)
        expected = "MULTILINESTRING ((9 0, 8.101251062924646 0.904618578893133, 9.898748937075354 -0.904618578893133))"

        op_equals = geometry_pb2.GeometryRequest(
            left_geometry=geometry_pb2.GeometryData(wkt=expected),
            right_geometry=projected.export_protobuf(),
            operator=geometry_pb2.EQUALS,
            operation_sr=output_sr)
        stub = geometry_grpc.GeometryServiceStub(self.channel)
        response3 = stub.GeometryOperationUnary(op_equals)

        self.assertTrue(response3.spatial_relationship)

    def test_exception_sr(self):
        try:
            LineString([(500000, 0), (400000, 100000), (600000, -100000)])
        except ValueError as e:
            self.assertTrue(str(e).startswith("must define a spatial reference for geometry on creation"))

    def test_exception(self):
        stub = geometry_grpc.GeometryServiceStub(self.channel)
        service_sr = geometry_pb2.SpatialReferenceData(wkid=32632)
        output_sr = geometry_pb2.SpatialReferenceData(wkid=4326)
        polyline = LineString([(500000, 0), (400000, 100000), (600000, -100000)],
                              sr=geometry_pb2.SpatialReferenceData(wkid=3857))

        a = geometry_pb2.EnvelopeData(xmin=1, ymin=2, xmax=4, ymax=6)
        service_geom_polyline = geometry_pb2.GeometryData(
            wkt=polyline.wkt,
            sr=service_sr)

        op_request_project = geometry_pb2.GeometryRequest(
            left_geometry=service_geom_polyline,
            operator=geometry_pb2.PROJECT,
            operation_sr=output_sr,
            result_encoding=geometry_pb2.WKT)

        print("make project request")
        response2 = stub.GeometryOperationUnary(op_request_project)
        print("Client received project response:\n", response2)

        expected = "MULTILINESTRING ((9 0, 8.101251062924646 0.904618578893133, 9.898748937075354 -0.904618578893133))"

        actual = response2.geometry.wkt

        op_equals = geometry_pb2.GeometryRequest(
            left_geometry=service_geom_polyline,
            right_geometry=geometry_pb2.GeometryData(wkt=polyline.wkt),
            operator=geometry_pb2.EQUALS,
            operation_sr=output_sr)

        try:
            _ = stub.GeometryOperationUnary(op_equals)
            self.assertTrue(False)
        except grpc.RpcError as e:
            self.assertTrue(e.details().startswith('geometryOperationUnary error : either both spatial references are '
                                                   'local or neither'))

    def test_multipoint(self):
        stub = geometry_grpc.GeometryServiceStub(self.channel)
        service_sr = geometry_pb2.SpatialReferenceData(wkid=4326)
        output_sr = geometry_pb2.SpatialReferenceData(wkid=3857)
        multipoints_array = []
        for longitude in range(-180, 180, 10):
            for latitude in range(-80, 80, 10):
                multipoints_array.append((longitude, latitude))

        multipoint = MultiPoint(multipoints_array, sr=service_sr)

        service_geom_polyline = geometry_pb2.GeometryData(wkt=multipoint.wkt, sr=service_sr)

        op_request_project = geometry_pb2.GeometryRequest(
            left_geometry=service_geom_polyline,
            operator=geometry_pb2.PROJECT,
            operation_sr=output_sr)

        op_request_outer = geometry_pb2.GeometryRequest(
            left_geometry_request=op_request_project,
            operator=geometry_pb2.PROJECT,
            operation_sr=service_sr,
            result_encoding=geometry_pb2.WKT)

        print("make project request")
        response = stub.GeometryOperationUnary(op_request_outer)
        print("Client received project response:\n", response)
        round_trip_result_wkt = loads(response.geometry.wkt)

        op_request_outer = geometry_pb2.GeometryRequest(
            left_geometry_request=op_request_project,
            operator=geometry_pb2.PROJECT,
            operation_sr=service_sr,
            result_encoding=geometry_pb2.WKB)
        response = stub.GeometryOperationUnary(op_request_outer)
        round_trip_result = wkbloads(response.geometry.wkb)
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
    # TODO perform this test once you have streaming working.
    # def test_union(self):
    #     # Build patches as in dissolved.py
    #     stub = geometry_grpc.GeometryServiceStub(self.channel)
    #     r = partial(random.uniform, -20.0, 20.0)
    #     service_sr = geometry_pb2.SpatialReferenceData(wkid=4326)
    #     points = [Point(r(), r()) for i in range(10000)]
    #     spots = [p.buffer(2.5) for p in points]
    #     service_multipoint = geometry_pb2.GeometryData(sr=service_sr)
    #     shape_start = datetime.datetime.now()
    #     patches = cascaded_union(spots)
    #     # because shapely is non-simple we need to simplify it for this to be a fair comparison
    #     service_multipoint.wkb = patches.wkb
    #     op_request_outer = geometry_pb2.GeometryRequest(
    #         left_geometry=service_multipoint,
    #         operator=geometry_pb2.GeometryRequest.Simplify'),
    #         operation_sr=service_sr,
    #         result_encoding=geometry_pb2.WKB)
    #     response = stub.GeometryOperationUnary(op_request_outer)
    #     patches = wkbloads(response.geometry.wkb)
    #     shape_end = datetime.datetime.now()
    #     shape_delta = shape_end - shape_start
    #     shape_microseconds = int(shape_delta.total_seconds() * 1000)
    #     print(shape_microseconds)
    #     print(patches.wkt)
    #
    #     spots_wkb = [s.wkb for s in spots]
    #     geometry_data = geometry_pb2.GeometryData()
    #     geometry_data.wkb = spots_wkb
    #
    #     op_request_union = geometry_pb2.GeometryRequest(left_geometry=geometry_data,
    #                                        operator=geometry_pb2.GeometryRequest.Union'))
    #
    #     epl_start = datetime.datetime.now()
    #     response = stub.GeometryOperationUnary(op_request_union)
    #     unioned_result = wkbloads(response.geometry.wkb)
    #
    #     epl_end = datetime.datetime.now()
    #     epl_delta = epl_end - epl_start
    #     epl_microseconds = int(epl_delta.total_seconds() * 1000)
    #     # TODO investigate why dev machine is faster for epl and slower for shapely and the alternative is true for
    #     #  test machines (memory limits?) self.assertGreater(shape_microseconds, epl_microseconds)
    #     #  self.assertGreater(shape_microseconds * 0.75, epl_microseconds)
    #
    #     self.assertAlmostEqual(patches.area, unioned_result.area, 8)

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
        stub = geometry_grpc.GeometryServiceStub(self.channel)
        service_sr = geometry_pb2.SpatialReferenceData(wkid=4326)
        output_sr = geometry_pb2.SpatialReferenceData(wkid=3035)
        for longitude in np.arange(-180.0, 180.0, change_interval):
            for latitude in np.arange(-90, 90, change_interval):
                X[idx] = (longitude, latitude)
                idx += 1

                point = Point(longitude, latitude, sr=service_sr)

                service_geom_polyline = geometry_pb2.GeometryData(
                    wkt=point.wkt,
                    sr=service_sr)

                op_request_project = geometry_pb2.GeometryRequest(
                    left_geometry=service_geom_polyline,
                    operator=geometry_pb2.PROJECT,
                    operation_sr=output_sr)

                op_request_outer = geometry_pb2.GeometryRequest(
                    left_geometry_request=op_request_project,
                    operator=geometry_pb2.PROJECT,
                    operation_sr=service_sr,
                    result_encoding=geometry_pb2.WKT)

                # print("make project request")
                response = stub.GeometryOperationUnary(op_request_outer)
                # print("Client received project response:\n", response)
                point_projected = loads(response.geometry.wkt)
                if response.geometry.wkt == 'POINT EMPTY':
                    continue
                if longitude == -180 or longitude == 180:
                    continue
                self.assertAlmostEqual(point_projected.x, longitude, 8)

    def test_get_geodetic_area(self):
        polygon = Polygon([(0, 0), (1, 1), (1, 0)], sr=geometry_pb2.SpatialReferenceData(wkid=4326))
        area = polygon.remote_geodetic_area()
        projected = polygon.remote_project(
            to_spatial_reference=geometry_pb2.SpatialReferenceData(
                custom=geometry_pb2.SpatialReferenceData.Custom(
                    lon_0=polygon.envelope.centroid.x,
                    lat_0=polygon.envelope.centroid.y)))

        p_area = projected.remote_geodetic_area()
        self.assertEqual(math.ceil(math.fabs((area - p_area))), 7)

    def test_buffer_buffer(self):
        polygon = Polygon.from_bounds(xmin=-85.43750000002495,
                                      ymin=46.68749999938329,
                                      xmax=-85.37500000014984,
                                      ymax=46.74999999925853,
                                      sr=geometry_pb2.SpatialReferenceData(wkid=4326))
        geodetic_area = polygon.remote_geodetic_area()
        self.assertAlmostEqual(geodetic_area, 33199429.76907527, 8)
        geodetic_side = math.sqrt(geodetic_area)
        polygon_buffered = polygon.remote_geodetic_buffer(distance_m=(geodetic_side / 2.0))
        geodetic_area = polygon_buffered.remote_geodetic_area()
        self.assertFalse(math.isnan(geodetic_area))

    def test_str(self):
        polygon = Polygon.from_bounds(xmin=-85,
                                      ymin=46,
                                      xmax=-85,
                                      ymax=46,
                                      sr=geometry_pb2.SpatialReferenceData(wkid=4326))
        val = str(polygon)
        self.assertEqual(val, "POLYGON ((-85 46, -85 46, -85 46, -85 46)) wkid: 4326\n")

    def test_intersection(self):
        polygon_left = Polygon.from_bounds(xmin=-85,
                                           ymin=46,
                                           xmax=-83,
                                           ymax=48,
                                           sr=geometry_pb2.SpatialReferenceData(wkid=4326))
        polygon_right = Polygon.from_bounds(xmin=-85.5,
                                            ymin=44,
                                            xmax=-84,
                                            ymax=47,
                                            sr=geometry_pb2.SpatialReferenceData(wkid=4326))
        intersection_1 = polygon_left.remote_intersection(polygon_right)
        self.assertLess(intersection_1.area, polygon_left.area)
        self.assertLess(intersection_1.area, polygon_right.area)
        self.assertEqual(intersection_1.export_protobuf().sr.wkid, 4326)

        intersection_1_web = polygon_right.remote_intersection(other_geom=polygon_left,
                                                               operation_sr=geometry_pb2.SpatialReferenceData(wkid=3857))
        self.assertEqual(intersection_1_web.export_protobuf().sr.wkid, 3857)
        self.assertAlmostEqual(intersection_1.area,
                               intersection_1_web.remote_project(
                                   to_spatial_reference=geometry_pb2.SpatialReferenceData(
                                       wkid=4326)).area)
