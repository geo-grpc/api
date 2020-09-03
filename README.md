# Python gRPC Geometry Client
Why wrap shapely? We wanted three different abilities. Protobuf support, a required spatial reference and spatial reference aware geometry operations.

### Install

#### Linux or Max
```bash
pip install epl.geometry
```

#### Windows
- Find out your whether you are using Windows 32-bit or 64-bit. Go to Settings => System => About => System Type .
- Find out your python version. Open Command prompt, enter python --version and remember the first two numbers. For example my version is Python 3.7.3 so I should remember the number 37:
```bash
pip install wheel
```
- Go here and download the wheel corresponding to items 1–2. For example, I have 64-bit OS and Python 3.7.3 so I need to download file Shapely-1.6.4.post2-cp37-cp37m-win_amd64.whl .
```bash
pip install <path-to-Downloads>\<filename_from_item_4> .
```
For example:
```bash
pip install C:\Users\Dalya\Downloads\Shapely-1.6.4.post2-cp37-cp37m-win_amd64.whl .
```

### Protobuf
We wanted to import, export and operate on protobuf geometries and envelopes. Our microservices communicate using protobufs, so it made sense for us to have a helper library that allowed us to work with the geometries on our client and quickly construct protobufs for messaging.

### Spatial Reference / ProjectionData
We never want a geometry separate from it's spatial reference details. We could have solved this by working exclusively with the geojson idea of always Wgs84, but we've found time and again that projected geometries exist and must be worked with. So instead of letting geometries float around without their coordinate system information we decided to tie it to the geometry, much like ESRI does with ArcObjects.

### Geometry Service and SpatialReference
We wanted easy projections and operators that returned results according to their spatial reference tolerance (geodetic buffer, area, and topo relationships). Most of the old shapely operators behave the same as they did before (with exception of project and generalize), but now they use a remote geometry service that requires spatial reference information (defined in the `ProjectionData` object). We've kept the native shapely topo operators and changed their names to use the s_ prefix.

### Changed Behavior
Many things default to a "geodetic" method if possible, you can override that with a boolean in the method calls. There are a few differences between how shapely behaves and how this wrapper functions.
- `project` now means to project a geometry from one spatial reference to another 
- `simplify` fixes a broken geometry. To remove vertices from a geometry use `s_simplify` or `generalize`
- `area` is no longer a property. it's a method and it defaults to geodetic (internally it uses `geodetic_area`). the default result unit is meters squared. to force non-geodetic use `geodetic=False`
- `carto_bounds` property is used for the bounds order cartopy prefers
- `carto_geom` returns a shapely geometry (as does `shapely_dump`). Cartopy doesn't want wrapped shapely geometries, it wants the real thing.
- `buffer` defaults to geodetic (like area). The distance you specify is in meters. You can override with `geodetic=False`
- `proj` field on geometry returns a ProjectionData protobuf object
- initializing geometry requires a spatial reference, `proj`, `epsg` or `proj4` definition

## Requirements
This requires the gRPC geometry service. You can run this example from docker. 
```bash
docker run -it -p 8980:8980 geogrpc/geometry-chain:latest
```

The `GEOMETRY_SERVICE_HOST` must also be set to a service. It defaults to localhost:8980.

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

run tests
```shell script
docker run -d -p 8980:8980 geogrpc/geometry-chain:latest
pytest ./test
```


