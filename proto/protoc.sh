#!/bin/sh

#I use this script and works like a charm:

for ARGUMENT in "$@"
do

    KEY=$(echo $ARGUMENT | cut -f1 -d=)
    VALUE=$(echo $ARGUMENT | cut -f2 -d=)

    case "$KEY" in
            SRC_DIR)              SRC_DIR=${VALUE} ;;
            DOTNET_DIR)              DOTNET_DIR=${VALUE} ;;
            PYTHON_DIR)              PYTHON_DIR=${VALUE} ;;
            BUILD_PROTOS)              BUILD_PROTOS=${VALUE} ;;
            CPP_DIR)    CPP_DIR=${VALUE} ;;
            *)
    esac
done

echo "DOTNET_DIR = $DOTNET_DIR"
echo "CPP_DIR = $CPP_DIR"
echo "SRC_DIR = $SRC_DIR"
echo "BUILD_PROTOS = $BUILD_PROTOS"
echo "PYTHON_DIR = $PYTHON_DIR"

MONO_PATH="/defs${SRC_DIR}"
echo $MONO_PATH
#
#
##C#
echo protoc -I/opt/include -I="${MONO_PATH}/proto" --csharp_out="${MONO_PATH}/dotnet/${DOTNET_DIR}" \
  "${MONO_PATH}/proto${BUILD_PROTOS}/*.proto"
#
#protoc -I/opt/include -I="$MONO_PATH"/proto --plugin=protoc-gen-grpc="$(command -v grpc_csharp_plugin)" \
#  --grpc_out="$MONO_PATH"/dotnet/EplProtobuf \
#  "$MONO_PATH"/proto/epl/protobuf/v1/*_service.proto
##C#
#
#
##PYTHON
#protoc -I/opt/include -I="$MONO_PATH"/proto --python_out="$MONO_PATH"/python/epl_protobuf \
#  "$MONO_PATH"/proto/epl/protobuf/v1/*.proto
#
#protoc -I/opt/include -I="$MONO_PATH"/proto --plugin=protoc-gen-grpc_python="$(command -v grpc_python_plugin)" \
#  --grpc_python_out="$MONO_PATH"/python/epl_protobuf \
#  "$MONO_PATH"/proto/epl/protobuf/v1/*_service.proto
#PYTHON
#
##GO
#protoc -I/opt/include -I "$MONO_PATH"/proto --go_out=/defs/src \
#  "$MONO_PATH"/proto/epl/protobuf/v1/geometry.proto \
#  "$MONO_PATH"/proto/epl/protobuf/v1/query.proto  \
#  "$MONO_PATH"/proto/epl/protobuf/v1/stac.proto
#
#protoc -I/opt/include -I "$MONO_PATH"/proto --go_out=plugins=grpc:/defs/src \
#  "$MONO_PATH"/proto/epl/protobuf/v1/geometry_service.proto \
#  "$MONO_PATH"/proto/epl/protobuf/v1/stac_service.proto
##GO
#
##CPP
#protoc -I/opt/include -I "$MONO_PATH"/proto --cpp_out="$MONO_PATH"/cpp/protobuf-lib \
#  -I "$MONO_PATH"/proto \
#  "$MONO_PATH"/proto/epl/protobuf/v1/geometry.proto \
#  "$MONO_PATH"/proto/epl/protobuf/v1/query.proto  \
#  "$MONO_PATH"/proto/epl/protobuf/v1/stac.proto \
#  "$MONO_PATH"/proto/epl/protobuf/v1/geometry_service.proto \
#  "$MONO_PATH"/proto/epl/protobuf/v1/stac_service.proto
#
#protoc -I/opt/include -I "$MONO_PATH"/proto --grpc_out="$MONO_PATH"/cpp/protobuf-lib --plugin=protoc-gen-grpc="$(command -v grpc_cpp_plugin)" \
#  -I "$MONO_PATH"/proto \
#  "$MONO_PATH"/proto/epl/protobuf/v1/geometry_service.proto \
#  "$MONO_PATH"/proto/epl/protobuf/v1/stac_service.proto
##CPP