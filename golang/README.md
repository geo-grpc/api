# Microservice Computational Geometry

Using gRPC, a fork of ESRI's computational geometry library and the twpayne geometry library these packages expose additional computational geometry methods for golang that might be useful to golang GIS software engineers.

Here is a list of the currently exposed methods:

- Buffer
- ConvexHull
- DensifyByLength
- Generalize
- GeodesicBuffer
- GeodeticDensifyByLength
- Project
- Simplify
- ShiftXY
- Contains
- Crosses
- Disjoint
- Equals
- Intersects
- Overlaps
- Touches
- Within
- Difference
- Intersection
- SymmetricDifference
- Union
- GeodeticLength
- GeodeticArea

the `"github.com/geo-grpc/api/epl/geometry"` and the `"github.com/geo-grpc/api/epl/protobuf/v1"` can be used with the `geogrpc/geometry-chain` docker image to execute computational geometry calls in golang.

To test locally, execute the following docker command:
```shell script
docker run -it -p 8980:8980 geogrpc/geometry-chain:buster-0604876
```