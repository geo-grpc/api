import grpc
from shapely.geometry import Polygon
from shapely.geometry import LineString
from geometry_client.geometry_operators_pb2 import *
import geometry_client.geometry_operators_pb2_grpc as geometry_grpc

import sys

polygon = Polygon([(0, 0), (1, 1), (1, 0)])
serviceGeom = ServiceGeometry(
    geometry_string=polygon.wkt,
    geometry_encoding_type=GeometryEncodingType.Value('wkt'))

opRequest = OperatorRequest(left_geometry=serviceGeom,
                                                   operator_type=ServiceOperatorType.Value('ExportToWkt'))

serviceSpatialReference = ServiceSpatialReference(wkid=32632)
outputSpatialReference = ServiceSpatialReference(wkid=4326)
polyline = LineString([(500000,       0), (400000,  100000), (600000, -100000)])

serviceGeomPolyline = ServiceGeometry(
    geometry_string=polyline.wkt,
    geometry_encoding_type=GeometryEncodingType.Value('wkt'),
    spatial_reference=serviceSpatialReference)

opRequestProject = OperatorRequest(
    left_geometry=serviceGeomPolyline,
    operator_type=ServiceOperatorType.Value('Project'),
    operation_spatial_reference=outputSpatialReference)


def run(address):
    print("connect to address: ", address)
    print("create channel")
    channel = grpc.insecure_channel(address)

    print("make stub")
    stub = geometry_grpc.GeometryOperatorsStub(channel)

    print("make wkt request")
    response = stub.ExecuteOperation(opRequest)
    # print response
    print("Client received wkt response:\n", response)

    print("make project request")
    response2 = stub.ExecuteOperation(opRequestProject)
    print("Client received project response:\n", response2)


if __name__ == "__main__":
    print("Greeter client sent:\n", serviceGeom)
    if len(sys.argv) == 1:
        address = 'localhost:8980'
    else:
        address = sys.argv[1]

    run(address)

