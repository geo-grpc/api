# gRPC Computational Geometry

Using gRPC, a fork of ESRI's computational geometry library and the twpayne geometry library these packages expose additional computational geometry methods for golang that might be useful to golang GIS software engineers.

Below is a list of all the methods exposed through gRPC. The links connect to the ESRI computational geometry library documentation:

* ##### **Topological operations**
    _Boolean operations on Polygons, Polylines, Points and MultiPoints._
    * [Cut](http://esri.github.io/geometry-api-java/doc/Cut.html){:target="_blank"}
    * [Difference](http://esri.github.io/geometry-api-java/javadoc/com/esri/core/geometry/OperatorDifference.html){:target="_blank"}
    * [Intersection](http://esri.github.io/geometry-api-java/doc/Intersection.html){:target="_blank"}
    * [Symmetric Difference](http://esri.github.io/geometry-api-java/javadoc/com/esri/core/geometry/OperatorSymmetricDifference.html){:target="_blank"}
    * [Union](http://esri.github.io/geometry-api-java/javadoc/com/esri/core/geometry/OperatorUnion.html){:target="_blank"}


* ##### **Validation**
    * Simplify - validates and fixes the geometry to be correct for storage in geodatabase
    * Simplify with OGC restrictions - validates and fixes the geometry to be correct according to OGC rules


* ##### **Relational operations**
    _Read [Performing relational operations](http://esri.github.io/geometry-api-java/doc/RelationalOperators.html)._
    * [Relate using DE-9IM matrix](http://esri.github.io/geometry-api-java/doc/Relate.html)
    * __[Contains](http://esri.github.io/geometry-api-java/doc/Contains.html)__
    * __[Crosses](http://esri.github.io/geometry-api-java/doc/Crosses.html)__
    * __[Disjoint](http://esri.github.io/geometry-api-java/doc/Disjoint.html)__
    * __[Equals](http://esri.github.io/geometry-api-java/doc/Equals.html)__
    * __[Intersects](http://esri.github.io/geometry-api-java/doc/Intersects.html)__
    * __[Overlaps](http://esri.github.io/geometry-api-java/doc/Overlaps.html)__
    * __[Touches](http://esri.github.io/geometry-api-java/doc/Touches.html)__
    * __[Within](http://esri.github.io/geometry-api-java/doc/Within.html)__

* ##### **Other operations**
    * Boundary - creates a geometry that is the boundary of a given geometry
    * Buffer - creates buffer polygon around the given geometry
    * Clip - clips geometries with a 2-dimensional envelope
    * Convex Hull - creates the convex hull of a given geometry
    * Densify - densifies geometries by plotting points between existing vertices
    * Distance - calculates the distance between two geometries
    * Generalize - simplifies geometries using the Douglas-Peucker algorithm
    * Offset - creates geometries that are offset from the input geometries by a given distance 
    * Geodesic Distance (see geodesicDistanceOnWGS84 in GeometryEngine) - calculates the shortest distance between two points on the WGS84 spheroid


the `"github.com/geo-grpc/api/epl/geometry"` and the `"github.com/geo-grpc/api/epl/protobuf/v1"` can be used with the `geogrpc/geometry-chain` docker image to execute computational geometry calls in golang.

To test locally, execute the following docker command:
```shell script
docker run -it -p 8980:8980 geogrpc/geometry-chain:buster-0604876
```