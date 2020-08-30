#!/usr/bin/env bash

### protoc.sh
docker run --rm -it -v "${GOPATH}":/defs --entrypoint /bin/sh namely/protoc:1.28_2 -c "/defs/src/github.com/geo-grpc/api/proto/protoc.sh"
### protoc.sh

docker run --rm   -v "$(pwd)/.."/docs:/out -v "$(pwd)":/protos pseudomuto/protoc-gen-doc:1.3.1 \
  --proto_path=/protos/ \
  epl/protobuf/v1/geometry.proto \
  epl/protobuf/v1/geometry_service.proto \
  epl/protobuf/v1/query.proto \
  epl/protobuf/v1/stac.proto \
  epl/protobuf/v1/stac_service.proto