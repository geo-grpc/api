# Python gRPC Geometry Client
Why wrap shapely? We wanted three different abilities. Protobuf support, a required spatial reference and spatial reference aware geometry operations.

### Protobuf
We wanted to import, export and operate on protobuf geometries and envelopes. Our microservices communicate using protobufs, so it made sense for us to have a helper library that allowed us to work with the geometries on our client and quickly construct protobufs for messaging.

### Spatial Reference
We never want a geometry separate from it's spatial reference details. We could have solved this by working exclusively with the geojson idea of always Wgs84, but we've found time and again that projected geometries exist and must be worked with. So instead of letting geometries float around without their coordinate system information we decided to tie it to the geometry, much like ESRI does with ArcObjects.

### Geometry Service and SpatialReference
We wanted easy projections and operators that returned results according to their spatial reference tolerance (geodetic buffer, area, and topo relationships). Most of the old shapely operators behave the same as they did before (with exception of project and generalize), but now they use a remote geometry service that requires spatial reference information. We've kept the native shapely topo operators and changed their names to use the s_ prefix.

### Changed Behavior
There are a few differences between how shapely behaves and how this wrapper functions.
- `project` is now mean to project a geometry from one spatial reference to another 
- `simplify` fixes a broken geometry. To remove vertices from a geometry use `s_simplify` or `generalize`
- `area` is no longer a property. it's a method and it defaults to geodetic (internally it uses `geodetic_area`). the default result unit is meters squared. to force non-geodetic use `geodetic=False`
- `carto_bounds` property is used for the bounds order cartopy prefers
- `carto_geom` returns a shapely geometry (as does `shapely_dump`). Cartopy doesn't want wrapped shapely geometries, it wants the real thing.
- `buffer` defaults to geodetic (like area). The distance you specify is in meters. You can override with `geodetic=False`
- `sr` field on geometry returns a SpatialReferenceData protobuf object
- initializing geometry requires a spatial reference, sr, wkid or proj4 definition

## Requirements
This requires the gRPC geometry library and docker. You need to build the service as the docker hosted `echoparklabs/geometry-service-java` is out of date. 
```bash
git clone https://github.com/geo-grpc/geometry-service-java.git
cd geometry-service-java
docker build -t echoparklabs/geometry-service-java .
docker run -p 8980:8980 -d echoparklabs/geometry-service-java
```

## Install Python gRPC Geometry Client

install using pip (depends on shapely, epl.protobuf and grpc)
```bash
pip install epl.geometry
```

install for development
```bash
pip install -r requirements.txt
pip install -r requirements-test.txt
```

## Install and run demo
```bash
pip install -r requirements.txt
pip install -r requirements-demo.txt
jupyter notebook
```

if you have virtualenv installed
```bash
python -m ipykernel install --user --name=geometry-client-python
``` 

if cartopy crashes (issues [738](https://github.com/SciTools/cartopy/issues/738) and [879](https://github.com/SciTools/cartopy/issues/879)):
```bash
pip uninstall cartopy
pip uninstall shapely
pip install shapely --no-binary shapely
pip install cartopy --no-binary cartopy
jupyter notebook
```

