#!/bin/sh

for ARGUMENT in "$@"
do

    KEY=$(echo "$ARGUMENT" | cut -f1 -d=)
    VALUE=$(echo "$ARGUMENT" | cut -f2 -d=)

    case "$KEY" in
            SRC_DIR)              SRC_DIR=${VALUE} ;;
            DOTNET_DIR)              DOTNET_DIR=${VALUE} ;;
            PYTHON_DIR)              PYTHON_DIR=${VALUE} ;;
            BUILD_PROTOS)              BUILD_PROTOS=${VALUE} ;;
            GOPATH)              GOPATH=${VALUE} ;;
            CPP_DIR)    CPP_DIR=${VALUE} ;;
            *)
    esac
done

echo "DOTNET_DIR = $DOTNET_DIR"
echo "CPP_DIR = $CPP_DIR"
echo "PROTOC SRC_DIR = $SRC_DIR"
echo "BUILD_PROTOS = $BUILD_PROTOS"
echo "GOPATH = $GOPATH"
echo "PYTHON_DIR = $PYTHON_DIR"

MONO_PATH="/defs${SRC_DIR}"
echo "NAMELY SRC PATH = $MONO_PATH"


##C#
protoc -I/opt/include -I="${MONO_PATH}"/proto --csharp_out="${MONO_PATH}"/"${DOTNET_DIR}" \
  "${MONO_PATH}"/proto/"${BUILD_PROTOS}"/*.proto

protoc -I/opt/include -I="${MONO_PATH}"/proto --plugin=protoc-gen-grpc="$(command -v grpc_csharp_plugin)" \
  --grpc_out="${MONO_PATH}"/"${DOTNET_DIR}" \
  "${MONO_PATH}"/proto/"${BUILD_PROTOS}"/*_service.proto
##C#


##PYTHON
protoc -I/opt/include -I="$MONO_PATH"/proto --python_out="$MONO_PATH"/"${PYTHON_DIR}" \
  "$MONO_PATH"/proto/"${BUILD_PROTOS}"/*.proto

protoc -I/opt/include -I="$MONO_PATH"/proto --plugin=protoc-gen-grpc_python="$(command -v grpc_python_plugin)" \
  --grpc_python_out="$MONO_PATH"/"${PYTHON_DIR}" \
  "$MONO_PATH"/proto/"${BUILD_PROTOS}"/*_service.proto
#PYTHON

#GO
if [ -n "$GOPATH" ];
then
  protoc -I/opt/include -I "$MONO_PATH"/proto --go_out=/defs/src \
    "$MONO_PATH"/proto/"${BUILD_PROTOS}"/*.proto

  protoc -I/opt/include -I "$MONO_PATH"/proto --go_out=plugins=grpc:/defs/src \
    "$MONO_PATH"/proto/"${BUILD_PROTOS}"/*_service.proto

else
  echo "GOPATH is unset. not generating golang code";
fi
#GO

#CPP
protoc -I/opt/include -I "$MONO_PATH"/proto --cpp_out="$MONO_PATH"/"$CPP_DIR" \
  "$MONO_PATH"/proto/"${BUILD_PROTOS}"/*.proto

protoc -I/opt/include -I "$MONO_PATH"/proto --plugin=protoc-gen-grpc="$(command -v grpc_cpp_plugin)" \
  --grpc_out="$MONO_PATH"/"$CPP_DIR" \
  "$MONO_PATH"/proto/"${BUILD_PROTOS}"/*_service.proto
#CPP