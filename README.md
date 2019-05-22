# Python gRPC Geometry Client
This requires the gRPC geometry library and docker. You need to build the service as the docker hosted `echoparklabs/geometry-service-java` is out of date. 
```bash
git clone https://github.com/geo-grpc/geometry-service-java.git
cd geometry-service-java
docker build -t echoparklabs/geometry-service-java .
docker run -p 8980:8980 -d echoparklabs/geometry-service-java
```

## Install Python gRPC Geometry Client

```bash
pip install -r requirements.txt
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

