#!/usr/bin/env bash

protoc -I proto/ proto/epl/protobuf/geometry.proto --go_out=$GOPATH/src
protoc -I proto/ proto/epl/protobuf/query.proto --go_out=$GOPATH/src
protoc -I proto/ proto/epl/protobuf/stac.proto --go_out=$GOPATH/src
protoc -I proto/ proto/epl/protobuf/stac_service.proto --go_out=plugins=grpc:$GOPATH/src
protoc -I proto/ proto/epl/protobuf/geometry_service.proto --go_out=plugins=grpc:$GOPATH/src

python -mgrpc_tools.protoc -I=./proto --python_out=./python \
    ./proto/epl/protobuf/geometry.proto \
    ./proto/epl/protobuf/query.proto \
    ./proto/epl/protobuf/stac.proto

python -mgrpc_tools.protoc -I=./proto --grpc_python_out=./python \
    ./proto/epl/protobuf/geometry_service.proto \
    ./proto/epl/protobuf/stac_service.proto

docker run --rm   -v $(pwd)/docs:/out   -v $(pwd)/proto:/protos   pseudomuto/protoc-gen-doc --proto_path=/protos/ epl/protobuf/geometry.proto epl/protobuf/geometry_service.proto epl/protobuf/query.proto epl/protobuf/stac.proto epl/protobuf/stac_service.proto