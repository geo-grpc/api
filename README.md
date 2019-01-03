# protobuf

```bash
python3 -mgrpc_tools.protoc -I=./src/ --python_out=./ \
  ./src/epl/protobuf/geometry_operators.proto  \
  ./src/epl/protobuf/stac.proto
```
