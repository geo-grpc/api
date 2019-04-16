# protobuf

Proto files here are used in [STAC](https://github.com/geo-grpc/stac) service and in [geometry service](https://github.com/geo-grpc/geometry-service-java)

```bash
protoc -I proto/ proto/epl/protobuf/*.proto --go_out=$GOPATH/src
protoc -I proto/ proto/epl/grpc/*.proto --go_out=plugins=grpc:$GOPATH/src
```
