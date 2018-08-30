make/update the python proto classes:
```bash
pip3 install --upgrade pip
pip3 install grpcio-tools
pip3 install protobuf

# for testing
pip3 install shapely

python3 -mgrpc_tools.protoc -I=./proto/ --python_out=./ --grpc_python_out=./ ./proto/epl/grpc/geometry/geometry_operators.proto
```
