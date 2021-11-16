FROM python:3.7.7-buster

RUN DEBIAN_FRONTEND=noninteractive apt-get update

RUN mkdir -p /opt/src/epl_protobuf
COPY ./epl_protobuf/ /opt/src/epl_protobuf

WORKDIR /opt/src/epl_geometry
COPY ./epl_geometry /opt/src/epl_geometry

RUN pip install -r requirements.txt && \
    python setup.py install
