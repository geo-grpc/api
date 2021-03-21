# based off of requirements for gdal build at 3.0.4:
# https://raw.githubusercontent.com/OSGeo/gdal/v3.0.4/gdal/docker/ubuntu-small/Dockerfile
ARG JAVA_VERSION=11.0.10
FROM openjdk:${JAVA_VERSION}-jdk-buster

# Derived from osgeo/gdal Even Rouault <even.rouault@spatialys.com>
# which is in turn derived from osgeo/proj by Howard Butler <howard@hobu.co>
# Also derived from Echo Park Labs fuse builder
MAINTAINER David Raleigh <davidraleigh@gmail.com>

RUN apt-get update -y \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --fix-missing --no-install-recommends \
    ant \
    autoconf \
    automake \
    autotools-dev \
    binutils \
    build-essential \
    ca-certificates \
    cmake \
    g++ \
    git \
    libc-ares-dev \
    libcunit1-dev \
    libev-dev \
    libevent-dev \
    libgtest-dev \
    libjansson-dev \
    libjemalloc-dev \
    libnghttp2-dev \
    libssl-dev \
    libtool \
    libxml2-dev \
    make \
    pkg-config \
    protobuf-compiler \
    python3.7-dev \
    python3-distutils \
    software-properties-common \
    wget \
    unzip \
    zip \
    zlib1g-dev

RUN cd /usr/src/googletest/googletest && \
    cmake CMakeLists.txt && \
    make && \
    cp *.a /usr/lib

WORKDIR /opt/src
RUN wget https://curl.haxx.se/download/curl-7.53.0.tar.gz && \
    tar -xvf curl-7.53.0.tar.gz && \
    cd curl-7.53.0/ && \
    LIBS="-lpthread" ./configure --with-nghttp2 --with-ssl --prefix=/usr/local/ && \
    make && \
    make install && \
    # make test && \
    ldconfig

WORKDIR /opt/src
RUN git clone https://github.com/benmcollins/libjwt --branch v1.12.0 --depth 1 && \
    cd libjwt && \
    autoreconf -i && \
    ./configure && \
    make && \
    make install && \
    ldconfig

WORKDIR /opt/src
RUN git clone https://github.com/aws/aws-sdk-cpp.git --branch 1.6.42 --depth 1 && \
    cd /opt/src/aws-sdk-cpp && \
    cmake -DBUILD_ONLY="s3" && \
    cd /opt/src/aws-sdk-cpp/aws-cpp-sdk-s3 && \
    make -j 8 && make install && \
    cd /opt/src/aws-sdk-cpp/aws-cpp-sdk-core && \
    make install && \
    cd /opt/src && \
    rm -rf /opt/src/aws-sdk-cpp
