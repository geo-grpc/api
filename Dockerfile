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

# TODO build proto each time.
# ARG PROTO_PATH=./proto
# RUN python -mgrpc_tools.protoc -I=../../src/main/proto/ --python_out=./ --grpc_python_out=./ ../../src/main/proto/geometry_operators.proto

ENV GEOMETRY_SERVICE_HOST="localhost:8980"
EXPOSE 80

CMD python /opt/src/test/sample.py "$GEOMETRY_SERVICE_HOST"
