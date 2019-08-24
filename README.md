# protobuf

Proto files here are used in [STAC](https://github.com/geo-grpc/stac) service and in [geometry service](https://github.com/geo-grpc/geometry-service-java)

Build new versions using:
```bash
./generate.sh
```

Update documnetation:
```bash
docker run --rm   -v $(pwd)/docs:/out   -v $(pwd)/proto:/protos   pseudomuto/protoc-gen-doc --proto_path=/protos/ epl/protobuf/geometry.proto epl/protobuf/geometry_service.proto epl/protobuf/query.proto epl/protobuf/stac.proto epl/protobuf/stac_service.proto
```