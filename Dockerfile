FROM python:3.7.7-buster

RUN DEBIAN_FRONTEND=noninteractive apt-get update

WORKDIR /opt/src/geometry-client-python
COPY ./ /opt/src/geometry-client-python

RUN pip install -r requirements.txt && \
    python setup.py install
