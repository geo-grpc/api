#!/usr/bin/env bash

protoc -I proto/ proto/epl/protobuf/geometry.proto --go_out=$GOPATH/src
protoc -I proto/ proto/epl/protobuf/query.proto --go_out=$GOPATH/src
protoc -I proto/ proto/epl/protobuf/stac.proto --go_out=$GOPATH/src
protoc -I proto/ proto/epl/protobuf/naip.proto --go_out=$GOPATH/src
protoc -I proto/ proto/epl/protobuf/stac_service.proto --go_out=plugins=grpc:$GOPATH/src
protoc -I proto/ proto/epl/protobuf/geometry_service.proto --go_out=plugins=grpc:$GOPATH/src

python -mgrpc_tools.protoc -I=./proto --python_out=./python \
    ./proto/epl/protobuf/geometry.proto \
    ./proto/epl/protobuf/query.proto \
    ./proto/epl/protobuf/stac.proto \
    ./proto/epl/protobuf/naip.proto

python -mgrpc_tools.protoc -I=./proto --grpc_python_out=./python \
    ./proto/epl/protobuf/geometry_service.proto \
    ./proto/epl/protobuf/stac_service.proto

#docker run --rm   -v $(pwd)/doc:/out   -v $(pwd)/proto/epl/protobuf:/protos   pseudomuto/protoc-gen-doc