#!/usr/bin/env bash

if [ -n "$1" ];
then
  SUBMITTED_PATH=$1
else
  SUBMITTED_PATH=/epl/protobuf/v1
fi

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
echo script executing from "$SCRIPT_DIR"
SRC_DIR="$(dirname "$SCRIPT_DIR")"
echo SRC_DIR = "$SRC_DIR"

TEST_INPUT=${SUBMITTED_PATH#\.}
TEST_INPUT=${TEST_INPUT#\/}
if test -f "${SCRIPT_DIR}/${TEST_INPUT}"; then
  echo "${SCRIPT_DIR}/${TEST_INPUT} is a file. submit location of proto."
  exit 1
fi

if ! test -d "${SCRIPT_DIR}/${TEST_INPUT}"; then
  echo "${SCRIPT_DIR}/${TEST_INPUT} directory does not exist."
  exit 1
fi

# remove trailing and preceding slashes
BUILD_PROTOS="${TEST_INPUT%\/}"
echo BUILD_PROTOS = $BUILD_PROTOS

# remove dots
SPLIT_NAMES=$(echo $BUILD_PROTOS | sed 's/\.//g')

# if there are underscore or dash replace with splittable /
SPLIT_NAMES=$(echo $SPLIT_NAMES | sed 's/[_-]/\//g')

# cpp dir
CPP_DIR=cpp/$(echo $SPLIT_NAMES | sed 's/[\/]/-/g')

# python dir
PYTHON_DIR=python/$(echo $SPLIT_NAMES | sed 's/[\/]/_/g')

IFS='/' read -ra directories <<< "${SPLIT_NAMES}"

# TODO there are some nice regex sed combinations for converting to PascalCase, but they don't work in Mac bash:
# https://unix.stackexchange.com/questions/196239/convert-underscore-to-pascalcase-ie-uppercamelcase
unset DOTNET_DIR
for i in "${directories[@]}"
do
    if [ -n "$DOTNET_DIR" ];
    then
      DOTNET_DIR=$DOTNET_DIR"$(tr '[:lower:]' '[:upper:]' <<< "${i:0:1}")${i:1}"
    else
      DOTNET_DIR="$(tr '[:lower:]' '[:upper:]' <<< "${i:0:1}")${i:1}"
    fi
done
DOTNET_DIR=dotnet/$DOTNET_DIR

mksrcdirs () {
  if [ ! -d "${SRC_DIR}/${1}" ]
  then
    echo "Directory ${SRC_DIR}/${1} DOES NOT exists."
    mkdir -p "${SRC_DIR}/${1}"
  else
    echo "Directory ${SRC_DIR}/${1} exists."
  fi
}

mksrcdirs "${CPP_DIR}"
mksrcdirs "${PYTHON_DIR}"

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
echo SRC_MINUS_GO = \""$SRC_MINUS_GO"\"

### protoc.sh
docker run --rm -it -v "${DEFS_DIR}":/defs \
  --entrypoint /bin/sh namely/protoc:1.37_1 \
  -c "/defs${SRC_MINUS_GO}/proto/protoc.sh SRC_DIR=\"${SRC_MINUS_GO}\" DOTNET_DIR=\"${DOTNET_DIR}\" CPP_DIR=\"${CPP_DIR}\" PYTHON_DIR=\"${PYTHON_DIR}\" BUILD_PROTOS=\"${BUILD_PROTOS}\" GOPATH=\"$GOPATH\""
### protoc.sh

# copy geometry over to java
cp -r "${SCRIPT_DIR}"/epl/protobuf/v1/geometry*.proto "${SCRIPT_DIR}"/../java/geometry-chain/epl-geometry-service/src/main/proto/epl/protobuf/v1
# copy geometry over to java

docker run --rm \
  -v "${SCRIPT_DIR}"/../docs:/out \
  -v "${SCRIPT_DIR}":/protos \
  pseudomuto/protoc-gen-doc:1.3.1 --proto_path=/protos/ "${BUILD_PROTOS}"/*.proto