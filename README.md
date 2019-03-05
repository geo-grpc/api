make/update the python proto classes:
```bash
pip3 install --upgrade pip
pip3 install grpcio-tools
pip3 install protobuf

RUN python3 -mgrpc_tools.protoc -I=./proto/ \
    --python_out=./ ./proto/epl/protobuf/geometry.proto
RUN python3 -mgrpc_tools.protoc -I=./proto/ \
    --python_out=./ --grpc_python_out=./ ./proto/epl/grpc/geometry_operators.proto
```

for running the tests
```bash
pip3 install shapely
pip3 install pytest
pytest test/sample.py
```
