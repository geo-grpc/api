# gRPC GIS Computational Geometry fo Go

A microservice gRPC library for GIS computational geometry methods.

For example, here are some geometry operations chained together and not sent to gRPC service until `Execute` method is called.
```go
chain1 := geomOps.InitChain(geometry1)
chain1 = chain.Simplify(true).ProjectEPSG(32647).Buffer(.5).ConvexHull().ProjectEPSG(4326)
chain2 = geomOps.InitChain(geometry2)

result1, err := chain.Execute()
```

## Operators

Below is a list of all the methods exposed through gRPC. The links connect to the go docs and the ESRI computational geometry library documentation:

* #### Topological operations
    _Boolean operations on Polygons, Polylines, Points and MultiPoints._
    * [Cut](https://pkg.go.dev/github.com/geo-grpc/api/golang/epl/geometry#Cut) ([Esri Doc](http://esri.github.io/geometry-api-java/doc/Cut.html))
    * [Difference](https://pkg.go.dev/github.com/geo-grpc/api/golang/epl/geometry#Difference) ([Esri Doc](http://esri.github.io/geometry-api-java/javadoc/com/esri/core/geometry/OperatorDifference.html))
    * [Intersection](https://pkg.go.dev/github.com/geo-grpc/api/golang/epl/geometry#Intersection) ([Esri Doc](http://esri.github.io/geometry-api-java/doc/Intersection.html))
    * [Symmetric Difference](https://pkg.go.dev/github.com/geo-grpc/api/golang/epl/geometry#SymmetricDifference) ([GIS wiki](http://wiki.gis.com/wiki/index.php/Symmetrical_difference)) ([Esri Doc](https://pro.arcgis.com/en/pro-app/latest/tool-reference/analysis/symmetrical-difference.htm))
    * [Union](https://pkg.go.dev/github.com/geo-grpc/api/golang/epl/geometry#Union) ([Esri Doc](http://esri.github.io/geometry-api-java/javadoc/com/esri/core/geometry/OperatorUnion.html))


* #### Validation
    * [Simplify](https://pkg.go.dev/github.com/geo-grpc/api/golang/epl/geometry#Simplify) - validates and fixes the geometry to be correct for storage in an ESRI geodatabase
    * [Simplify with OGC restrictions](https://pkg.go.dev/github.com/geo-grpc/api/golang/epl/geometry#SimplifyOGC) - validates and fixes the geometry to be correct according to OGC rules


* #### Relational operations
    _Read [Esri Doc on Performing relational operations](http://esri.github.io/geometry-api-java/doc/RelationalOperators.html)._
    * [Relate using DE-9IM](https://pkg.go.dev/github.com/geo-grpc/api/golang/epl/geometry#Relate) [ESRI Doc](http://esri.github.io/geometry-api-java/doc/Relate.html)
    * [Contains](https://pkg.go.dev/github.com/geo-grpc/api/golang/epl/geometry#Contains) ([ESRI Doc](http://esri.github.io/geometry-api-java/doc/Contains.html))
    * [Crosses](https://pkg.go.dev/github.com/geo-grpc/api/golang/epl/geometry#Crosses) ([Esri Doc](http://esri.github.io/geometry-api-java/doc/Crosses.html))
    * [Disjoint](https://pkg.go.dev/github.com/geo-grpc/api/golang/epl/geometry#Disjoint) ([Esri Doc](http://esri.github.io/geometry-api-java/doc/Disjoint.html))
    * [Equals](https://pkg.go.dev/github.com/geo-grpc/api/golang/epl/geometry#Equals) ([Esri Doc](http://esri.github.io/geometry-api-java/doc/Equals.html))
    * [Intersects](https://pkg.go.dev/github.com/geo-grpc/api/golang/epl/geometry#Intersects) ([Esri Doc](http://esri.github.io/geometry-api-java/doc/Intersects.html))
    * [Overlaps](https://pkg.go.dev/github.com/geo-grpc/api/golang/epl/geometry#Overlaps) ([Esri Doc](http://esri.github.io/geometry-api-java/doc/Overlaps.html))
    * [Touches](https://pkg.go.dev/github.com/geo-grpc/api/golang/epl/geometry#Touches) ([Esri Doc](http://esri.github.io/geometry-api-java/doc/Touches.html))
    * [Within](https://pkg.go.dev/github.com/geo-grpc/api/golang/epl/geometry#Within) ([Esri Doc](http://esri.github.io/geometry-api-java/doc/Within.html))

* #### Other ESRI operations
    * [Boundary](https://pkg.go.dev/github.com/geo-grpc/api/golang/epl/geometry#Boundary) - creates a geometry that is the boundary of a given geometry
    * [Buffer](https://pkg.go.dev/github.com/geo-grpc/api/golang/epl/geometry#Buffer) - creates buffer polygon around the given geometry
    * [Clip](https://pkg.go.dev/github.com/geo-grpc/api/golang/epl/geometry#Clip) - clips geometries with a 2-dimensional envelope
    * [Convex Hull](https://pkg.go.dev/github.com/geo-grpc/api/golang/epl/geometry#ConvexHull) - creates the convex hull of a given geometry
    * [DensifyByLength](https://pkg.go.dev/github.com/geo-grpc/api/golang/epl/geometry#DensifyByLength) - densifies geometries by plotting points between existing vertices
    * [Generalize](https://pkg.go.dev/github.com/geo-grpc/api/golang/epl/geometry#Generalize) - simplifies geometries using the Douglas-Peucker algorithm
    * [Offset](https://pkg.go.dev/github.com/geo-grpc/api/golang/epl/geometry#Offset) - creates geometries that are offset from the input geometries by a given distance 

* #### Other Operations
    * [GeodesicBuffer](https://pkg.go.dev/github.com/geo-grpc/api/golang/epl/geometry#GeodesicBuffer) (Experimental) - Regardless of spatial reference, perform a geodesic buffer
    * [GeodesicArea](https://pkg.go.dev/github.com/geo-grpc/api/golang/epl/geometry#GeodesicArea) (Experimental) - Regardless of spatial reference, calculate area
    * [ProjectEPSG](https://pkg.go.dev/github.com/geo-grpc/api/golang/epl/geometry#ProjectEPSG) (Experimental) - Project a geometry to the given geographic or projected coordinate system defined by the epsg spatial reference code
    * [ShiftXY](https://pkg.go.dev/github.com/geo-grpc/api/golang/epl/geometry#ShiftXY) (Experimental) - Shift a geometry in the x and y direction using the coordinate system of it's spatial reference __or__ shift it in meters


### The Packages

The packages
```
"github.com/geo-grpc/api/epl/geometry"
"github.com/geo-grpc/api/epl/protobuf/v1"
``` 
can be used with the gRPC geometry service docker image, `geogrpc/geometry-chain`, to execute GIS computational geometry calls in golang.

To test locally, execute the following docker command:
```shell script
docker run -it -p 8980:8980 geogrpc/geometry-chain:buster-0604876
```