FROM python:2.7-slim

RUN apt update && \
    apt install -y python-pip

RUN python -m pip install --upgrade pip && \
    pip install grpc && \
    pip install shapely && \
    python -m pip install grpcio && \
    pip install grpcio-tools

WORKDIR /opt/src
COPY ./ ./

ENV HOSTNAME="localhost:8980"
EXPOSE 80

CMD python /opt/src/geometry_client/sample.py "$HOSTNAME"
