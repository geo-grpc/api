#!/usr/bin/env bash

if [ -n "$1" ];
then
  if [[ ${1:0:1} == "." ]]
  then
    BUILD_PROTOS="${1:1}"
  else
    BUILD_PROTOS=$1
  fi
else
  BUILD_PROTOS=/epl/protobuf/v1
fi

IFS='/' read -ra my_array <<< "${BUILD_PROTOS}"

unset CPP_DIR
for i in "${my_array[@]}"
do
    if [ -n "$CPP_DIR" ];
    then
      CPP_DIR=$CPP_DIR"-${i}"
      PYTHON_DIR=$PYTHON_DIR"_${i}"
      DOTNET_DIR=$DOTNET_DIR"$(tr '[:lower:]' '[:upper:]' <<< "${i:0:1}")${i:1}"
      BUILD_PROTOS=$BUILD_PROTOS"/${i}"
    else
      CPP_DIR=${i}
      PYTHON_DIR=${i}
      DOTNET_DIR="$(tr '[:lower:]' '[:upper:]' <<< "${i:0:1}")${i:1}"
      BUILD_PROTOS=${i}
    fi
done
DOTNET_DIR=dotnet/$DOTNET_DIR
CPP_DIR=cpp/$CPP_DIR
PYTHON_DIR=python/$PYTHON_DIR

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
echo script executing from "$SCRIPT_DIR"
SRC_DIR="$(dirname "$SCRIPT_DIR")"
echo SRC_DIR = "$SRC_DIR"

DEFS_DIR=$GOPATH
if [ -z ${GOPATH+x} ];
then
  echo "GOPATH is unset";
  DEFS_DIR=$SRC_DIR
  SRC_MINUS_GO=""
else
  echo "GOPATH is set to '$GOPATH'";
  SRC_MINUS_GO=${SRC_DIR#"$GOPATH"}
fi

echo DEFS_DIR = "$DEFS_DIR"
echo SRC_MINUS_GO = "$SRC_MINUS_GO"

### protoc.sh
docker run --rm -it -v "${DEFS_DIR}":/defs \
  --entrypoint /bin/sh namely/protoc:1.37_1 \
  -c "/defs${SRC_MINUS_GO}/proto/protoc.sh SRC_DIR=\"${SRC_MINUS_GO}\" DOTNET_DIR=\"${DOTNET_DIR}\" CPP_DIR=\"${CPP_DIR}\" PYTHON_DIR=\"${PYTHON_DIR}\" BUILD_PROTOS=\"${BUILD_PROTOS}\" GOPATH=\"$GOPATH\""
### protoc.sh
#
## copy geometry over to java
#cp -r "$(pwd)"/epl/protobuf/v1/geometry*.proto "$(pwd)"/../java/geometry-chain/epl-geometry-service/src/main/proto/epl/protobuf/v1
## copy geometry over to java
#
#docker run --rm   -v "$(pwd)/.."/docs:/out -v "$(pwd)":/protos pseudomuto/protoc-gen-doc:1.3.1 \
#  --proto_path=/protos/ "${BUILD_PROTOS}"/*.proto