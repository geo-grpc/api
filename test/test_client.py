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


import json
import os
import random
import unittest
import math
import requests
import warnings

import grpc

from shapely.wkt import loads
from shapely.wkb import loads as wkbloads
from epl.geometry import Point, MultiPoint, Polygon, LineString, MultiPolygon, MultiLineString, shape
from epl import geometry
from epl.protobuf.v1 import geometry_pb2
import epl.protobuf.v1.geometry_service_pb2_grpc as geometry_grpc
import numpy as np


def extract_poly_coords(geom):
    if geom.type == 'Polygon':
        exterior_coords = geom.exterior.coords[:]
        interior_coords = []
        for interior in geom.interiors:
            interior_coords += interior.coords[:]
    elif geom.type == 'MultiPolygon':
        exterior_coords = []
        interior_coords = []
        for part in geom:
            epc = extract_poly_coords(part)  # Recursive call
            exterior_coords += epc['exterior_coords']
            interior_coords += epc['interior_coords']
    else:
        raise ValueError('Unhandled geometry type: ' + repr(geom.type))
    return {'exterior_coords': exterior_coords,
            'interior_coords': interior_coords}


class TestBasic(unittest.TestCase):
    channel = None

    @classmethod
    def setUpClass(cls):
        address = os.getenv("GEOMETRY_SERVICE_HOST", 'localhost:8980')
        cls.channel = grpc.insecure_channel(address)

    def setUp(self):
        self.channel = TestBasic.channel
        geometry.geometry_service.set_channel(self.channel)

        # options
        # https://groups.google.com/forum/#!topic/grpc-io/ZtBCw4ZqLqE
        # https://github.com/justdoit0823/grpc-resolver/blob/master/grpcresolver/registry.py

    def test_buffer(self):
        polygon = Polygon([(0, 0), (1, 1), (1, 0)], proj=geometry_pb2.ProjectionData(epsg=4326))

        buffer_params = geometry_pb2.Params.Buffer(distance=1.2)

        op_request = geometry_pb2.GeometryRequest(left_geometry=polygon.geometry_data,
                                                  operator=geometry_pb2.BUFFER,
                                                  buffer_params=buffer_params,
                                                  result_encoding=geometry_pb2.WKT)

        print("make stub")
        stub = geometry_grpc.GeometryServiceStub(self.channel)

        print("make wkt request")
        response = stub.Operate(op_request)
        # print response
        print("Client received wkt response:\n", response)
        result_buffered = loads(response.geometry.wkt)
        self.assertTrue(result_buffered.contains(polygon))
        shapely_buffer = polygon.s_buffer(1.2)
        self.assertAlmostEqual(shapely_buffer.s_area, result_buffered.area, 2)

    def test_remote_buffer(self):
        polygon = Polygon([(0, 0), (1, 1), (1, 0)], proj=geometry_pb2.ProjectionData(epsg=4326))

        buffer_params = geometry_pb2.Params.Buffer(distance=1.2)

        op_request = geometry_pb2.GeometryRequest(left_geometry=polygon.geometry_data,
                                                  operator=geometry_pb2.BUFFER,
                                                  buffer_params=buffer_params,
                                                  result_encoding=geometry_pb2.WKT)

        print("make stub")
        stub = geometry_grpc.GeometryServiceStub(self.channel)

        print("make wkt request")
        response = stub.Operate(op_request)
        new_polygon = Polygon.import_protobuf(response.geometry)
        shapely_buffer = polygon.s_buffer(1.2)
        self.assertAlmostEqual(shapely_buffer.s_area, new_polygon.s_area, 2)

        another_new_polygon = polygon.s_buffer(1.2)
        self.assertAlmostEqual(shapely_buffer.s_area, another_new_polygon.s_area, 2)
        print(another_new_polygon.geometry_data)

    def test_remote_buffer_bounds(self):
        polygon = Polygon([(0, 0), (1, 1), (1, 0)], proj=geometry_pb2.ProjectionData(epsg=4326))
        buffered = polygon.s_buffer(33)
        self.assertEqual(-33, buffered.envelope_data.ymin)
        self.assertEqual(-33, buffered.envelope_data.xmin)
        self.assertEqual(34, buffered.envelope_data.xmax)
        self.assertEqual(34, buffered.envelope_data.ymax)
        buffered = polygon.s_buffer(33)
        self.assertEqual(-33, buffered.envelope_data.ymin)
        self.assertEqual(-33, buffered.envelope_data.xmin)
        self.assertEqual(34, buffered.envelope_data.xmax)
        self.assertEqual(34, buffered.envelope_data.ymax)
        self.assertEqual(4326, buffered.proj.epsg)

    def test_project(self):
        stub = geometry_grpc.GeometryServiceStub(self.channel)
        service_proj = geometry_pb2.ProjectionData(epsg=32632)
        output_proj = geometry_pb2.ProjectionData(epsg=4326)
        polyline = LineString([(500000, 0), (400000, 100000), (600000, -100000)], proj=service_proj)

        shapelypolyline = polyline.shapely_dump
        self.assertEqual(polyline.s_length, shapelypolyline.length)

        service_geom_polyline = geometry_pb2.GeometryData(
            wkt=polyline.wkt,
            proj=service_proj)

        op_request_project = geometry_pb2.GeometryRequest(
            left_geometry=service_geom_polyline,
            operator=geometry_pb2.PROJECT,
            operation_proj=output_proj,
            result_encoding=geometry_pb2.WKT)

        print("make project request")
        response2 = stub.Operate(op_request_project)
        print("Client received project response:\n", response2)

        expected = "MULTILINESTRING ((9 0, 8.101251062924646 0.904618578893133, 9.898748937075354 -0.904618578893133))"

        # actual = response2.geometry.wkt

        op_equals = geometry_pb2.GeometryRequest(
            left_geometry=geometry_pb2.GeometryData(wkt=expected),
            right_geometry=response2.geometry,
            operator=geometry_pb2.EQUALS,
            operation_proj=output_proj)

        response3 = stub.Operate(op_equals)

        self.assertTrue(response3.spatial_relationship)

    def test_remote_project(self):
        service_proj = geometry_pb2.ProjectionData(epsg=32632)
        output_proj = geometry_pb2.ProjectionData(epsg=4326)
        polyline = LineString([(500000, 0), (400000, 100000), (600000, -100000)],
                              proj=service_proj)
        print("make project request")
        projected = polyline.project(output_proj)
        print("Client received project response:\n", projected.wkt)
        print(projected.wkt)
        expected = "MULTILINESTRING ((9 0, 8.101251062924646 0.904618578893133, 9.898748937075354 -0.904618578893133))"

        op_equals = geometry_pb2.GeometryRequest(
            left_geometry=geometry_pb2.GeometryData(wkt=expected),
            right_geometry=projected.geometry_data,
            operator=geometry_pb2.EQUALS,
            operation_proj=output_proj)
        stub = geometry_grpc.GeometryServiceStub(self.channel)
        response3 = stub.Operate(op_equals)
        self.assertTrue(response3.spatial_relationship)

        op_simplify = geometry_pb2.GeometryRequest(
            geometry=geometry_pb2.GeometryData(wkb=polyline.wkb, proj=geometry_pb2.ProjectionData(epsg=32632)),
            operation_proj=output_proj,
            operator=geometry_pb2.SIMPLIFY
        )
        response4 = stub.Operate(op_simplify)
        multi_line = MultiLineString.import_protobuf(response4.geometry)
        self.assertAlmostEqual(polyline.s_length, multi_line.s_length, 8)

    def test_exception_proj(self):
        try:
            LineString([(500000, 0), (400000, 100000), (600000, -100000)])
        except ValueError as e:
            self.assertTrue(str(e).startswith("must define a spatial reference for geometry on creation"))

    def test_exception(self):
        stub = geometry_grpc.GeometryServiceStub(self.channel)
        service_proj = geometry_pb2.ProjectionData(epsg=32632)
        output_proj = geometry_pb2.ProjectionData(epsg=4326)
        polyline = LineString([(500000, 0), (400000, 100000), (600000, -100000)],
                              proj=geometry_pb2.ProjectionData(epsg=3857))

        service_geom_polyline = geometry_pb2.GeometryData(
            wkt=polyline.wkt,
            proj=service_proj)

        op_request_project = geometry_pb2.GeometryRequest(
            left_geometry=service_geom_polyline,
            operator=geometry_pb2.PROJECT,
            operation_proj=output_proj,
            result_encoding=geometry_pb2.WKT)

        print("make project request")
        response2 = stub.Operate(op_request_project)
        print("Client received project response:\n", response2)

        # expected = "MULTILINESTRING ((9 0, 8.101251062924646 0.904618578893133, " \
        #            "9.898748937075354 -0.904618578893133))"
        #
        # actual = response2.geometry.wkt

        op_equals = geometry_pb2.GeometryRequest(
            left_geometry=service_geom_polyline,
            right_geometry=geometry_pb2.GeometryData(wkt=polyline.wkt),
            operator=geometry_pb2.EQUALS,
            operation_proj=output_proj)

        try:
            _ = stub.Operate(op_equals)
            self.assertTrue(False)
        except grpc.RpcError as e:
            self.assertTrue(e.details().startswith('geometryOperationUnary error : either both spatial references are '
                                                   'local or neither'))

    def test_multipoint(self):
        stub = geometry_grpc.GeometryServiceStub(self.channel)
        service_proj = geometry_pb2.ProjectionData(epsg=4326)
        output_proj = geometry_pb2.ProjectionData(epsg=3857)
        multipoints_array = []
        for longitude in range(-180, 180, 10):
            for latitude in range(-80, 80, 10):
                multipoints_array.append((longitude, latitude))

        multipoint = MultiPoint(multipoints_array, proj=service_proj)

        service_geom_polyline = geometry_pb2.GeometryData(wkt=multipoint.wkt, proj=service_proj)
        multipointshapely = multipoint.shapely_dump
        self.assertEqual(multipoint.bounds[0], multipointshapely.bounds[0])

        op_request_project = geometry_pb2.GeometryRequest(
            left_geometry=service_geom_polyline,
            operator=geometry_pb2.PROJECT,
            operation_proj=output_proj)

        op_request_outer = geometry_pb2.GeometryRequest(
            left_geometry_request=op_request_project,
            operator=geometry_pb2.PROJECT,
            operation_proj=service_proj,
            result_encoding=geometry_pb2.WKT)

        print("make project request")
        response = stub.Operate(op_request_outer)
        print("Client received project response:\n", response)
        round_trip_result_wkt = loads(response.geometry.wkt)

        op_request_outer = geometry_pb2.GeometryRequest(
            left_geometry_request=op_request_project,
            operator=geometry_pb2.PROJECT,
            operation_proj=service_proj,
            result_encoding=geometry_pb2.WKB)
        response = stub.Operate(op_request_outer)
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
    #     service_proj = geometry_pb2.ProjectionData(epsg=4326)
    #     points = [Point(r(), r()) for i in range(10000)]
    #     spots = [p.buffer(2.5) for p in points]
    #     service_multipoint = geometry_pb2.GeometryData(proj=service_proj)
    #     shape_start = datetime.datetime.now()
    #     patches = cascaded_union(spots)
    #     # because shapely is non-simple we need to simplify it for this to be a fair comparison
    #     service_multipoint.wkb = patches.wkb
    #     op_request_outer = geometry_pb2.GeometryRequest(
    #         left_geometry=service_multipoint,
    #         operator=geometry_pb2.GeometryRequest.Simplify'),
    #         operation_proj=service_proj,
    #         result_encoding=geometry_pb2.WKB)
    #     response = stub.Operate(op_request_outer)
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
    #     response = stub.Operate(op_request_union)
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
        service_proj = geometry_pb2.ProjectionData(epsg=4326)
        output_proj = geometry_pb2.ProjectionData(epsg=3035)
        for longitude in np.arange(-180.0, 180.0, change_interval):
            for latitude in np.arange(-90, 90, change_interval):
                X[idx] = (longitude, latitude)
                idx += 1

                point = Point(longitude, latitude, proj=service_proj)

                service_geom_point = geometry_pb2.GeometryData(
                    wkt=point.wkt,
                    proj=service_proj)

                op_request_project = geometry_pb2.GeometryRequest(
                    left_geometry=service_geom_point,
                    operator=geometry_pb2.PROJECT,
                    result_proj=output_proj)

                op_request_outer = geometry_pb2.GeometryRequest(
                    left_geometry_request=op_request_project,
                    operator=geometry_pb2.PROJECT,
                    result_proj=service_proj,
                    result_encoding=geometry_pb2.WKT)

                # print("make project request")
                response = stub.Operate(op_request_outer)
                # print("Client received project response:\n", response)
                point_projected = loads(response.geometry.wkt)
                if response.geometry.wkt == 'POINT EMPTY':
                    continue
                if longitude == -180 or longitude == 180:
                    continue
                self.assertAlmostEqual(point_projected.x, longitude, 8)

    def test_get_geodetic_area(self):
        polygon = Polygon([(0, 0), (1, 1), (1, 0)], epsg=4326)
        area = polygon.geodetic_area()

        projected = polygon.project(
            to_proj=geometry_pb2.ProjectionData(
                custom=geometry_pb2.ProjectionData.Custom(
                    lon_0=polygon.envelope.centroid.x,
                    lat_0=polygon.envelope.centroid.y)))

        p_area = projected.geodetic_area()
        self.assertEqual(math.ceil(math.fabs((area - p_area))), 7)

    def test_buffer_buffer(self):
        polygon = Polygon.from_bounds(xmin=-85.43750000002495,
                                      ymin=46.68749999938329,
                                      xmax=-85.37500000014984,
                                      ymax=46.74999999925853,
                                      proj=geometry_pb2.ProjectionData(epsg=4326))
        geodetic_area = polygon.geodetic_area()
        self.assertAlmostEqual(geodetic_area, 33199429.76907527, 8)
        geodetic_side = math.sqrt(geodetic_area)
        polygon_buffered = polygon.geodetic_buffer(distance_m=(geodetic_side / 2.0))
        geodetic_area = polygon_buffered.geodetic_area()
        self.assertFalse(math.isnan(geodetic_area))

    def test_str(self):
        polygon = Polygon.from_bounds(xmin=-85,
                                      ymin=46,
                                      xmax=-85,
                                      ymax=46,
                                      proj=geometry_pb2.ProjectionData(epsg=4326))
        val = str(polygon)
        self.assertEqual(val, "POLYGON ((-85 46, -85 46, -85 46, -85 46)) epsg: 4326\n")

    def test_intersection(self):
        polygon_left = Polygon.from_bounds(xmin=-85,
                                           ymin=46,
                                           xmax=-83,
                                           ymax=48,
                                           proj=geometry_pb2.ProjectionData(epsg=4326))
        polygon_right = Polygon.from_bounds(xmin=-85.5,
                                            ymin=44,
                                            xmax=-84,
                                            ymax=47,
                                            proj=geometry_pb2.ProjectionData(epsg=4326))
        intersection_1 = polygon_left.intersection(polygon_right)
        self.assertLess(intersection_1.s_area, polygon_left.s_area)
        self.assertLess(intersection_1.s_area, polygon_right.s_area)
        self.assertEqual(intersection_1.geometry_data.proj.epsg, 4326)

        intersection_1_web = polygon_right.intersection(other_geom=polygon_left,
                                                        result_proj=geometry_pb2.ProjectionData(
                                                            epsg=3857))
        self.assertEqual(intersection_1_web.geometry_data.proj.epsg, 3857)
        self.assertAlmostEqual(intersection_1.s_area,
                               intersection_1_web.project(
                                   to_proj=geometry_pb2.ProjectionData(
                                       epsg=4326)).s_area)

        envelope_data = geometry_pb2.EnvelopeData(xmin=-85.5,
                                                  ymin=44,
                                                  xmax=-84,
                                                  ymax=47,
                                                  proj=geometry_pb2.ProjectionData(epsg=4326))
        polygon_right = Polygon.from_envelope_data(envelope_data=envelope_data)
        intersection_2 = polygon_right.intersection(polygon_left)
        self.assertEqual(intersection_1, intersection_2)

    def test_equal(self):
        polygon_right = Polygon.from_bounds(xmin=-85.5,
                                            ymin=44,
                                            xmax=-84,
                                            ymax=47,
                                            proj=geometry_pb2.ProjectionData(epsg=4326))
        polygon_lerft = Polygon.from_bounds(xmin=-85.5,
                                            ymin=44,
                                            xmax=-84,
                                            ymax=47,
                                            proj=geometry_pb2.ProjectionData(epsg=3857))
        self.assertFalse(polygon_right == polygon_lerft)

        point1 = Point(2, 3, proj=geometry_pb2.ProjectionData(epsg=4326))
        point2 = Point(2, 3, proj=geometry_pb2.ProjectionData(epsg=3857))
        self.assertFalse(point1 == point2)

    def test_buffer_envelope(self):
        # https://swiftera.atlassian.net/browse/DP-171
        env = geometry_pb2.EnvelopeData(xmin=626926.2447786715,
                                        ymin=4653522.778178136,
                                        xmax=626951.6973246232,
                                        ymax=4653539.96040726,
                                        proj=geometry_pb2.ProjectionData(epsg=32630))

        polygon = Polygon.from_envelope_data(env)
        buffered = polygon.s_buffer(100)
        self.assertTrue(buffered.s_contains(polygon))
        buffered_geodesic = polygon.geodetic_buffer(101)
        self.assertTrue(buffered_geodesic.s_contains(buffered))

    def test_type_shapelye(self):
        proj = geometry_pb2.ProjectionData(epsg=4326)
        coords = [(0, 0), (1, 1)]
        LineString(coords, proj=proj).s_contains(Point(0.5, 0.5, proj=proj))
        Point(0.5, 0.5, proj=proj).s_within(LineString(coords, proj=proj))
        env = geometry_pb2.EnvelopeData(xmin=626926.2447786715,
                                        ymin=4653522.778178136,
                                        xmax=626951.6973246232,
                                        ymax=4653539.96040726,
                                        proj=geometry_pb2.ProjectionData(epsg=32630))

        polygon = Polygon.from_envelope_data(env)
        self.assertTrue(isinstance(polygon, Polygon))
        print(polygon.exterior.coords)
        # polygon_dump = polygon.shapley_dump
        # self.assertTrue(isinstance(polygon_dump, ShapelyPolygon))

        polygon_left = Polygon.from_bounds(xmin=-85,
                                           ymin=46,
                                           xmax=-83,
                                           ymax=48,
                                           proj=geometry_pb2.ProjectionData(epsg=4326))
        polygon_left_shapely = polygon_left.shapely_dump
        self.assertEqual(polygon_left.s_area, polygon_left_shapely.area)

        polygon_right = Polygon.from_bounds(xmin=-85.5,
                                            ymin=44,
                                            xmax=-84,
                                            ymax=47,
                                            proj=geometry_pb2.ProjectionData(epsg=4326))
        intersection_1 = polygon_left.intersection(polygon_right)
        self.assertTrue(isinstance(intersection_1, MultiPolygon))

        for geom in intersection_1.geoms:
            print(geom.wkt)

        self.assertGreaterEqual(intersection_1.s_area, 0)
        multi_shape = intersection_1.shapely_dump
        self.assertEqual(multi_shape.area, intersection_1.s_area)
        print("success")

    def test_proj_params(self):
        polygon_left = Polygon.from_bounds(xmin=-85,
                                           ymin=46,
                                           xmax=-83,
                                           ymax=48,
                                           proj=geometry_pb2.ProjectionData(epsg=4326))
        polygon_right = Polygon.from_bounds(xmin=-85.5,
                                            ymin=44,
                                            xmax=-84,
                                            ymax=47,
                                            epsg=4326)
        intersection_1 = polygon_left.intersection(polygon_right)
        self.assertLess(intersection_1.s_area, polygon_left.s_area)
        self.assertLess(intersection_1.s_area, polygon_right.s_area)
        self.assertEqual(intersection_1.geometry_data.proj.epsg, 4326)

        intersection_1_web = polygon_right.intersection(other_geom=polygon_left,
                                                        result_proj=geometry_pb2.ProjectionData(
                                                            epsg=3857))
        self.assertEqual(intersection_1_web.geometry_data.proj.epsg, 3857)
        self.assertAlmostEqual(intersection_1.s_area,
                               intersection_1_web.project(
                                   to_proj=geometry_pb2.ProjectionData(
                                       epsg=4326)).s_area)

        envelope_data = geometry_pb2.EnvelopeData(xmin=-85.5,
                                                  ymin=44,
                                                  xmax=-84,
                                                  ymax=47,
                                                  proj=geometry_pb2.ProjectionData(epsg=4326))
        polygon_right = Polygon.from_envelope_data(envelope_data=envelope_data)
        intersection_2 = polygon_right.intersection(polygon_left)
        self.assertEqual(intersection_1, intersection_2)

    def test_custom_azi(self):
        polygon_right = Polygon.from_bounds(xmin=-85.5,
                                            ymin=44,
                                            xmax=-84,
                                            ymax=47,
                                            epsg=4326)
        proj = geometry_pb2.ProjectionData(custom=geometry_pb2.ProjectionData.Custom(lon_0=-84.5, lat_0=45.5))
        polyg_projected = polygon_right.project(to_proj=proj)
        print(polyg_projected)
        self.assertEqual(polyg_projected.proj.proj4, "+proj=laea +lat_0=45.500000 +lon_0=-84.500000 +x_0=4321000 "
                                                     "+y_0=3210000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m "
                                                     "+no_defs")

    def test_equals(self):
        polygon_right = Polygon.from_bounds(xmin=-85.5,
                                            ymin=44,
                                            xmax=-84,
                                            ymax=47,
                                            epsg=4326)
        polygon_left = Polygon.from_bounds(xmin=-85.5000000002,
                                           ymin=44,
                                           xmax=-84,
                                           ymax=47,
                                           epsg=4326)
        self.assertTrue(polygon_right.equals(polygon_left))
        self.assertFalse(polygon_right.s_equals(polygon_left))

    def test_envelope_data_equals(self):
        xmin = 39.99430071558862
        ymin = 19.996406537338878
        xmax = 40.00569928441138
        ymax = 20.003593250811246
        proj4326 = geometry_pb2.ProjectionData(epsg=4326)
        proj3857 = geometry_pb2.ProjectionData(epsg=3857)
        expected_data = geometry_pb2.EnvelopeData(xmin=xmin,
                                                  ymin=ymin,
                                                  xmax=xmax,
                                                  ymax=ymax,
                                                  proj=proj4326)
        polygon1 = Polygon.from_envelope_data(expected_data)
        polygon2 = Polygon.from_envelope_data(polygon1.envelope_data)
        self.assertTrue(polygon1.equals(polygon2))
        polygon3 = Polygon.from_bounds(xmin=xmin,
                                       ymin=ymin,
                                       xmax=xmax,
                                       ymax=ymax,
                                       proj=proj4326).project(to_proj=proj3857).project(to_proj=proj4326).geoms[0]
        self.assertTrue(polygon2.equals(polygon3))

        rows, columns = 5304, 7052
        gsd = 15
        px_row, px_column = rows / 2.0, columns / 2.0
        x_plus = (columns - px_column) * gsd * 0.01
        # remember origin of image is like a matrix, with y increasing as you move down the image from the origin in the
        # upper left
        y_minus = (rows - px_row) * gsd * 0.01
        x_minus = px_column * gsd * 0.01
        y_plus = px_row * gsd * 0.01

        proj = geometry_pb2.ProjectionData(custom=geometry_pb2.ProjectionData.Custom(lon_0=40, lat_0=20))
        point_center = Point(40, 20, epsg=4326).project(to_proj=proj)

        polygon4 = polygon2.project(to_proj=proj)
        polygon5 = polygon4.project(to_proj=proj4326)
        self.assertTrue(polygon5.s_buffer(0.0000001).s_contains(polygon3))

        ymax = point_center.y + y_plus
        xmax = point_center.x + x_plus
        ymin = point_center.y - y_minus
        xmin = point_center.x - x_minus
        polygon6 = Polygon.from_envelope_data(geometry_pb2.EnvelopeData(xmin=xmin,
                                                                        ymin=ymin,
                                                                        xmax=xmax,
                                                                        ymax=ymax,
                                                                        proj=proj)).project(to_proj=proj4326)
        polygon7 = Polygon.from_bounds(xmin=xmin,
                                       ymin=ymin,
                                       xmax=xmax,
                                       ymax=ymax,
                                       proj=proj)
        polygon7 = polygon7.project(to_proj=proj4326)
        self.assertTrue(polygon6.s_buffer(0.0000001).s_contains(polygon7))
        print(polygon6[0].s_area)

        print(polygon1.s_area)
        self.assertTrue(polygon7.s_buffer(0.001).contains(polygon1, operation_proj=proj4326))

    def test_spain_json(self):
        # grab the Taos, NM county outline from a geojson hosted on github
        r = requests.get("https://raw.githubusercontent.com/johan/world.geo.json/master/countries/ESP.geo.json")
        spain_geom = r.json()
        spain_shape = geometry.shape(spain_geom['features'][0]['geometry'], epsg=4326)
        self.assertEqual(spain_shape.proj.epsg, 4326)
        number = spain_shape.area()
        self.assertGreaterEqual(number, 10)

        new_geom = spain_shape.buffer(45)
        self.assertTrue(new_geom.contains(spain_shape))

    def test_union(self):
        p = Point(0, 0, epsg=4326).buffer(400)
        p2 = Point(0.2, 0.2, epsg=4326).buffer(400)
        unioned = p.union(p2)
        print(p.s_area)
        print(p2.s_area)
        print(unioned.s_area)
        self.assertEqual(p.s_area + p2.s_area, unioned.s_area)

        # TODO geodetic area densify bug
        # self.assertEqual(p.area() + p2.area(), unioned.area())

    def test_distance(self):
        point1 = Point(152.352298, -24.875975, epsg=4326)
        point2 = Point(151.960336, -24.993289, epsg=4326)
        distance = point1.distance(point2)

        # TODO, geodetic distance should be exactly equal to 41667.730
        self.assertAlmostEqual(41667.730, distance, 1)

        point3 = point2.project(to_epsg=3857)
        distance = point3.distance(point1)
        # TODO, geodetic distance should be exactly equal to 41667.730
        self.assertAlmostEqual(41667.730, distance, 1)

        distance_old = point1.distance(point2, geodetic=False)
        self.assertAlmostEqual(0.409141520796879, distance_old, 9)

    def test_shift(self):
        point1 = Point(152.352298, -24.875975, epsg=4326)
        point2 = point1.translate(1, 1, geodetic=False)
        self.assertEqual(point2.x, point1.x + 1)
        self.assertEqual(point2.y, point1.y + 1)

        point2 = point1.translate(501, 0)
        self.assertTrue(point1.disjoint(point2))
        self.assertTrue(point1.buffer(500).disjoint(point2))
        self.assertTrue(point1.buffer(501).touches(point2))
        self.assertTrue(point1.buffer(502).contains(point2))

    def test_length(self):
        polyline = LineString([(0, 0), (1, 0)], epsg=4326)
        self.assertEqual(111319.4907932264, polyline.length())

        polyline_projected = polyline.project(to_epsg=3857)
        self.assertAlmostEqual(111319.4907932264, polyline_projected.length(), 8)

        polyline = LineString([(0, 0), (1, 0)], epsg=4326)
        self.assertEqual(1, polyline.length(geodetic=False))

    def test_geojson(self):
        point1 = Point(152.352298, -24.875975, epsg=4326)
        geoj = point1.__geo_interface__
        b_pass = (json.dumps(geoj) == '{"type": "Point", "coordinates": [152.352298, -24.875975]}') or \
                 (json.dumps(geoj) == '{"coordinates": [152.352298, -24.875975], "type": "Point"}')
        self.assertTrue(b_pass)

    def test_intersection_exception(self):
        envelope_wkt = "POLYGON ((649657.9958662051 4650771.385128138, 649657.9958662051 4651419.659440621, " \
                       "650567.1344525344 4651419.659440621, 650567.1344525344 4650771.385128138, 649657.9958662051 " \
                       "4650771.385128138)) "
        proj = geometry_pb2.ProjectionData(proj4="+proj=utm +zone=30 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m "
                                                 "+no_defs")
        polygon_data_1 = geometry_pb2.GeometryData(wkt=envelope_wkt, proj=proj)
        polygon_1 = Polygon.import_protobuf(polygon_data_1)
        polygon_2_wkt = "POLYGON ((-1.19294172319649 41.83288173182827, -0.847841728602983 41.83288173182827, " \
                        "-0.847841728602983 42.00038957049547, -1.19294172319649 42.00038957049547, " \
                        "-1.19294172319649 41.83288173182827))"
        polygon_data_2 = geometry_pb2.GeometryData(wkt=polygon_2_wkt, proj=geometry_pb2.ProjectionData(epsg=4326))
        polygon_2 = Polygon.import_protobuf(polygon_data_2)
        b_warned = False
        warnings.filterwarnings('error')
        try:
            _ = polygon_1.intersection(polygon_2)
        except Warning:
            b_warned = True
        self.assertTrue(b_warned)
        warnings.filterwarnings("ignore")
        result = polygon_1.intersection(polygon_2)
        self.assertTrue(result.proj_eq(polygon_1.proj))

    def test_union_none(self):
        polygon = Point(1, 1, proj=geometry_pb2.ProjectionData(epsg=4326)).buffer(200)
        union = polygon.union(None)
        self.assertEquals(polygon, union)
        union = polygon.union(None, result_proj=geometry_pb2.ProjectionData(epsg=4326))
        self.assertEquals(polygon, union)
        union = polygon.union(None, result_proj=geometry_pb2.ProjectionData(epsg=3857))
        self.assertNotEqual(polygon, union)

    def test_generalize(self):
        polygon = Point(1, 1, proj=geometry_pb2.ProjectionData(epsg=4326)).buffer(400)
        polygon_general = polygon.generalize(percent_reduction=95)
        self.assertGreaterEqual(polygon.area(geodetic=False), polygon_general.area(geodetic=False))

        polygon_general = polygon.generalize(max_point_count=5)
        self.assertGreater(polygon.area(geodetic=False), polygon_general.area(geodetic=False))
        # todo first and last coordinate the same
        self.assertLessEqual(len(extract_poly_coords(polygon_general)['exterior_coords']), 6)

    def test_geodetic_inverse(self):
        proj = geometry_pb2.ProjectionData(epsg=4326)
        point1 = Point(0, 0, proj=proj)
        point2 = Point(-1, 0, proj=proj)
        az12, az21, distance = point1.geodetic_inverse(point2)
        self.assertAlmostEqual(111319.4907932264, distance, 14)
        self.assertAlmostEqual(-math.pi / 2, az12, 14)
        self.assertAlmostEqual(math.pi / 2, az21, 14)

    def test_midpoint(self):
        proj = geometry_pb2.ProjectionData(epsg=4326)
        point1 = Point(0, 0, proj=proj)
        point2 = Point(-1, 0, proj=proj)
        midPoint = point1.midpoint(point2, geodetic=False)
        self.assertEquals(-0.5, midPoint.x)
        self.assertEquals(0, midPoint.y)
        point1 = Point(-1.466778964645005, 42.0236178190542, 0, epsg=4326)
        point2 = Point(-1.466778964547645, 42.02353946848927, 0, epsg=4326)
        midPoint = point1.midpoint(point2, geodetic=False)
        self.assertEquals(0, midPoint.z)
        self.assertEquals(-1.466778964596325, midPoint.x)
        self.assertEquals(42.023578643771735, midPoint.y)

    def test_geodetic_inverse_wkb(self):
        bottom = Point(-1.466778964645005, 42.0236178190542, 0, epsg=4326)
        top = Point(-1.466778964547645, 42.02353946848927, 0, epsg=4326)
        azi12, _, _ = bottom.geodetic_inverse(top)
        mid_point = bottom.midpoint(top, geodetic=False).project(to_epsg=4326)
        proj4 = "+proj=omerc +lonc={0} +lat_0={1} +alpha={2} +ellps=GRS80".format(mid_point.x,
                                                                                  mid_point.y,
                                                                                  math.degrees(azi12))

        self.assertEquals(proj4, "+proj=omerc +lonc=-1.466778964596325 +lat_0=42.023578643771735 "
                                 "+alpha=179.99994701173003 +ellps=GRS80")

    def test_cascaded_union(self):
        test = []
        for i in range(0, 200):
            lon, lat = random.random(), random.random()
            point = Point(lon, lat, epsg=4326)
            buffered = point.s_buffer(1)
            test.append(buffered)

        response = Polygon.cascaded_union(test)
        buffed = response.s_buffer(0.001)
        for geom in test:
            self.assertTrue(buffed.contains(geom))

    def test_cascaded_union_compare(self):
        test_epl = []
        for i in range(0, 200):
            lon, lat = random.random(), random.random()
            point = Point(lon, lat, epsg=4326)
            buffered = point.s_buffer(1)
            test_epl.append(buffered)

        response = Polygon.cascaded_union(test_epl)
        response_2 = Polygon.s_cascaded_union(test_epl)

        self.assertTrue(response.buffer(.5).shapely_dump.contains(response_2))
        buffed = response.s_buffer(0.001)
        for geom in test_epl:
            self.assertTrue(buffed.contains(geom))

    def test_s_methods(self):
        for i in range(0, 200):
            lon, lat = random.random(), random.random()
            point = Point(lon, lat, epsg=4326)
            buffered = point.s_buffer(1)
            buffer2 = buffered.buffer(2)
            self.assertTrue(buffer2.s_convex_hull.equals(buffer2))
            self.assertTrue(buffered.convex().equals(buffered))

    def test_triangulate(self):
        from shapely.ops import triangulate
        import pprint
        points = MultiPoint([(0, 0), (1, 1), (0, 2), (2, 2), (3, 1), (1, 0)], epsg=4326)
        triangles = triangulate(points)
        pprint.pprint([triangle.wkt for triangle in triangles])

    def test_import_wkt(self):
        wkt = 'POLYGON((-97.76475265848251 30.329368555095282,-97.81075790750594 30.24754609592361,' \
              '-97.73591354715438 30.21669674922466,-97.6816685520372 30.294987720261897,-97.76475265848251 ' \
              '30.329368555095282)) '
        data = Polygon.import_wkt(wkt, epsg=4326)
        self.assertEquals(data.proj.epsg, 4326)

    def test_3857(self):
        austin_wkt = "MULTIPOLYGON (((-98.2051285429566 29.57121580080253, -97.3978573732647 29.68847714051914, " \
                     "-97.3977 29.6885, -97.4063567134265 29.72277351846413, -97.41658596096325 29.76327298830345, " \
                     "-97.74749926396314 31.07341953487447, -98.20511113234716 31.13991547322948, -98.66270777486596 " \
                     "31.07342522906833, -98.99370927257297 29.76327229595237, -99.00405966608577 29.72230390603985, " \
                     "-99.01260000000001 29.6885, -99.01244262644467 29.68847714169182, -98.2051285429566 " \
                     "29.57121580080253)))"
        polygon = Polygon.import_wkt(wkt=austin_wkt, epsg=4326)
        boundingbox = polygon.project(to_epsg=3857)
        print(boundingbox.buffer(-2000).bounds)

    def test_geodetic_inverse_multiple(self):
        pt1 = Point(0, 0, epsg=4326)
        pt2 = Point(1, 0, epsg=4326)
        az12, az21, dist = pt1.geodetic_inverse(pt2)
        self.assertEquals(math.degrees(az12), 90)
        self.assertEquals(math.degrees(az21), -90)

        pt2 = Point(0, 1, epsg=4326)
        az12, az21, dist = pt1.geodetic_inverse(pt2)
        self.assertEquals(math.degrees(az12), 0)
        self.assertEquals(math.degrees(az21), -180)

        pt2 = Point(0, -1, epsg=4326)
        az12, az21, dist = pt1.geodetic_inverse(pt2)
        self.assertEquals(math.degrees(az12), 180)
        self.assertEquals(math.degrees(az21), 0)

        pt2 = Point(-1, 0, epsg=4326)
        az12, az21, dist = pt1.geodetic_inverse(pt2)
        self.assertEquals(360 + math.degrees(az12), 270)
        self.assertEquals(math.degrees(az21), 90)

    def test_proj(self):
        pt2 = Point(-1, 0, epsg=4326)
        pt1 = Point(0, -1, epsg=3857)
        self.assertFalse(pt1.proj_eq(pt2.proj))

    def test_append(self):
        flight_path = LineString(coordinates=[(0, 0), (1, 0)], epsg=4326)
        for i in range(3, 6):
            data = flight_path.coords[:]
            data.append((i, 0))
            flight_path = LineString(coordinates=data, epsg=4326)

        self.assertGreaterEqual(flight_path.s_length, 5)

    def test_intersection_unmatched_proj(self):
        polygon_left = Polygon.from_bounds(xmin=-85,
                                           ymin=46,
                                           xmax=-83,
                                           ymax=48,
                                           proj=geometry_pb2.ProjectionData(epsg=4326))
        polygon_right = Polygon.from_bounds(xmin=-85.5,
                                            ymin=44,
                                            xmax=-84,
                                            ymax=47,
                                            proj=geometry_pb2.ProjectionData(epsg=4326))
        intersection_1 = polygon_left.intersection(polygon_right)

        projected_right = polygon_right.project(to_epsg=3857)
        intersection_2 = polygon_left.intersection(projected_right)

        self.assertAlmostEqual(intersection_1.s_area,
                               intersection_2.s_area)

    def test_union_madrid(self):
        madrid = '{"type": "Polygon","coordinates": [[[-3.698015213012695,40.39562038212791],[-3.6763429641723633,' \
                 '40.39562038212791],[-3.6763429641723633,40.409901689845285],[-3.698015213012695,' \
                 '40.409901689845285],[-3.698015213012695,40.39562038212791]]]} '
        madrid_dict = json.loads(madrid)
        wkt_austin = 'POLYGON ((-97.7352547645 30.27526474757116, ' \
                     '-97.7195692 30.27526474757116, -97.7195692 30.28532, ' \
                     '-97.7352547645 30.28532, -97.7352547645 ' \
                     '30.27526474757116))'
        madrid_poly = shape(madrid_dict, epsg=4326)
        austin_poly = Polygon.import_wkt(wkt_austin, epsg=4326)
        unioned = madrid_poly.union(austin_poly)
        self.assertTrue(unioned.s_intersects(madrid_poly))
        self.assertTrue(unioned.s_intersects(austin_poly))

    def test_random_multipoint(self):
        polygon = Polygon([(0, 0), (0, 10), (10, 10), (0, 0)], epsg=4326)
        multipoint = polygon.random_multipoint(points_per_square_km=0.3,
                                               seed=1977,
                                               result_proj=polygon.proj)
        self.assertIsNotNone(multipoint)
        self.assertEqual(len(multipoint), 182165)
        buffered_polygon = polygon.buffer(0.001)
        self.assertTrue(buffered_polygon.contains(multipoint))

    def test_random_with_holes(self):
        polygon_1 = Polygon.import_wkt("POLYGON ((0 0, 0 10, 10 10, 10 0, 0 0),(3 3, 7 3, 7 7, 3 7, 3 3))", epsg=4326)
        multipoint_1 = polygon_1.random_multipoint(0.0013, 1977)

        polygon_no_ring = Polygon.import_wkt("POLYGON((0 0, 0 10, 10 10, 10 0,0 0))", epsg=4326)
        multipoint_no_ring = polygon_no_ring.random_multipoint(0.0013, 1977)

        intersector = polygon_1.densify(max_length_m=1232535.5660433513)
        intersected = intersector.intersection(multipoint_no_ring)

        self.assertEqual(len(multipoint_1), len(intersected))

    def test_export_to_shapely(self):
        a = Polygon.import_wkt("POLYGON ((0 0, 0 10, 10 10, 10 0, 0 0),(3 3, 7 3, 7 7, 3 7, 3 3))",
                               epsg=4326)
        b = a.shapely_dump
        self.assertEqual('epl.geometry.polygon.Polygon', "{0}.{1}".format(a.__class__.__module__,
                                                                          a.__class__.__name__))
        self.assertEqual('shapely.geometry.polygon.Polygon', "{0}.{1}".format(b.__class__.__module__,
                                                                              b.__class__.__name__))
        self.assertEqual(a.s_area, b.area)

    def test_no_params(self):
        polygon = Polygon([(0, 0), (1, 1), (1, 0)], proj=geometry_pb2.ProjectionData(epsg=4326))

        op_request = geometry_pb2.GeometryRequest(left_geometry=polygon.geometry_data,
                                                  operator=geometry_pb2.BUFFER,
                                                  result_encoding=geometry_pb2.WKT)

        stub = geometry_grpc.GeometryServiceStub(self.channel)
        self.assertRaises(grpc.RpcError, stub.Operate, op_request)
