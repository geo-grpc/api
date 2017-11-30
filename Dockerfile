FROM python:2.7-slim as builder

RUN apt update

RUN pip install --upgrade pip && \
    pip install grpcio-tools

WORKDIR /opt/src/geometry-client-python
COPY ./ ./

RUN python -mgrpc_tools.protoc -I=./proto/ --python_out=./epl/geometry --grpc_python_out=./epl/geometry ./proto/geometry_operators.proto


FROM python:2.7-slim

RUN apt update

RUN pip install --upgrade pip && \
    pip install grpc && \
    pip install grpcio

# TODO remove this and place it as an install for the testing
RUN pip install shapely

WORKDIR /opt/src/geometry-client-python

COPY --from=builder /opt/src/geometry-client-python /opt/src/geometry-client-python

RUN python setup.py install

ENV GEOMETRY_SERVICE_HOST="localhost:8980"
