import grpc
from shapely.geometry import Polygon
import geometry_operators_pb2
import geometry_operators_pb2_grpc

polygon = Polygon([(0, 0), (1, 1), (1, 0)])
serviceGeom = geometry_operators_pb2.ServiceGeometry(geometry_string=polygon.wkt,
                                                     geometry_encoding_type='wkt')

opRequest = geometry_operators_pb2.OperatorRequest(left_geometry=serviceGeom,
                                                   operator_type='ExportToWkt')

def run():
  channel = grpc.insecure_channel('localhost:8980')
  stub = geometry_operators_pb2_grpc.GeometryOperatorsStub(channel)
  response = stub.ExecuteOperation(opRequest)
  print response
  # print("Greeter client received: " + response.message)

run()
print serviceGeom