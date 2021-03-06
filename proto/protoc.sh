#!/bin/sh

MONO_PATH=/defs/src/github.com/geo-grpc/api

#C#
protoc -I/opt/include -I="$MONO_PATH"/proto --csharp_out="$MONO_PATH"/dotnet/EplProtobuf \
  "$MONO_PATH"/proto/epl/protobuf/v1/geometry.proto \
  "$MONO_PATH"/proto/epl/protobuf/v1/query.proto  \
  "$MONO_PATH"/proto/epl/protobuf/v1/stac.proto \
  "$MONO_PATH"/proto/epl/protobuf/v1/geometry_service.proto \
  "$MONO_PATH"/proto/epl/protobuf/v1/stac_service.proto

protoc -I/opt/include -I="$MONO_PATH"/proto --plugin=protoc-gen-grpc="$(command -v grpc_csharp_plugin)" \
  --grpc_out="$MONO_PATH"/dotnet/EplProtobuf \
  "$MONO_PATH"/proto/epl/protobuf/v1/geometry_service.proto \
  "$MONO_PATH"/proto/epl/protobuf/v1/stac_service.proto
#C#


#PYTHON
protoc -I/opt/include -I="$MONO_PATH"/proto --python_out="$MONO_PATH"/python/epl_protobuf \
  "$MONO_PATH"/proto/epl/protobuf/v1/geometry.proto \
  "$MONO_PATH"/proto/epl/protobuf/v1/query.proto  \
  "$MONO_PATH"/proto/epl/protobuf/v1/stac.proto

protoc -I/opt/include -I="$MONO_PATH"/proto --plugin=protoc-gen-grpc_python="$(command -v grpc_python_plugin)" \
  --grpc_python_out="$MONO_PATH"/python/epl_protobuf \
  "$MONO_PATH"/proto/epl/protobuf/v1/geometry_service.proto \
  "$MONO_PATH"/proto/epl/protobuf/v1/stac_service.proto
#PYTHON

#GO
protoc -I/opt/include -I "$MONO_PATH"/proto --go_out=/defs/src \
  "$MONO_PATH"/proto/epl/protobuf/v1/geometry.proto \
  "$MONO_PATH"/proto/epl/protobuf/v1/query.proto  \
  "$MONO_PATH"/proto/epl/protobuf/v1/stac.proto

protoc -I/opt/include -I "$MONO_PATH"/proto --go_out=plugins=grpc:/defs/src \
  "$MONO_PATH"/proto/epl/protobuf/v1/geometry_service.proto \
  "$MONO_PATH"/proto/epl/protobuf/v1/stac_service.proto
#GO

#CPP
protoc -I/opt/include -I "$MONO_PATH"/proto --cpp_out="$MONO_PATH"/cpp/protobuf-lib \
  -I "$MONO_PATH"/proto \
  "$MONO_PATH"/proto/epl/protobuf/v1/geometry.proto \
  "$MONO_PATH"/proto/epl/protobuf/v1/query.proto  \
  "$MONO_PATH"/proto/epl/protobuf/v1/stac.proto \
  "$MONO_PATH"/proto/epl/protobuf/v1/geometry_service.proto \
  "$MONO_PATH"/proto/epl/protobuf/v1/stac_service.proto

protoc -I/opt/include -I "$MONO_PATH"/proto --grpc_out="$MONO_PATH"/cpp/protobuf-lib --plugin=protoc-gen-grpc="$(command -v grpc_cpp_plugin)" \
  -I "$MONO_PATH"/proto \
  "$MONO_PATH"/proto/epl/protobuf/v1/geometry_service.proto \
  "$MONO_PATH"/proto/epl/protobuf/v1/stac_service.proto
#CPP