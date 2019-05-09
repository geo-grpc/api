FROM python:3.5-slim

RUN apt update

WORKDIR /opt/src/geometry-client-python
COPY ./ /opt/src/geometry-client-python

RUN pip3 install .

ENV GEOMETRY_SERVICE_HOST="localhost:8980"
