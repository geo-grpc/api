FROM python:2.7-slim

RUN apt update && \
    apt install -y python-pip

RUN python -m pip install --upgrade pip && \
    pip install grpc && \
    pip install shapely && \
    python -m pip install grpcio && \
    pip install grpcio-tools

WORKDIR /opt/src/geometry-client-python
COPY ./ ./

RUN python -mgrpc_tools.protoc -I=./proto/ --python_out=./epl/geometry --grpc_python_out=./epl/geometry ./proto/geometry_operators.proto
RUN python setup.py install

ENV GEOMETRY_SERVICE_HOST="localhost:8980"
EXPOSE 80

#CMD python /opt/src/test/sample.py "$GEOMETRY_SERVICE_HOST"
