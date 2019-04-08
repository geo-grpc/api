# protobuf

```bash
protoc -I proto/ proto/epl/protobuf/*.proto --go_out=$GOPATH/src
protoc -I proto/ proto/epl/grpc/*.proto --go_out=plugins=grpc:$GOPATH/src
```
