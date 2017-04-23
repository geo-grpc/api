from git root project:
```bash
cd geometry-client-python/geometry_client/
```

Then make/update the python proto classes:
```bash
python -mgrpc_tools.protoc -I=../../src/main/proto/ --python_out=./ --grpc_python_out=./ ../../src/main/proto/geometry_operators.proto
```

If the geometry-client-python directory is not relative to the geometry-service-java project then the proto files won't be found and the project won't build.