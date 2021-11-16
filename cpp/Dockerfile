ARG TAG=buster
FROM debian:${TAG} as builder

RUN apt-get update && apt-get clean

RUN apt-get update -y \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --fix-missing --no-install-recommends \
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
           software-properties-common \
           wget \
           unzip \
           zip \
           zlib1g-dev


# install protobuf first, then grpc
ENV GRPC_RELEASE_TAG v1.28.x
RUN git clone -b ${GRPC_RELEASE_TAG} https://github.com/grpc/grpc /var/local/git/grpc && \
    cd /var/local/git/grpc && \
    git submodule update --init && \
    echo "--- installing protobuf ---" && \
    cd third_party/protobuf && \
    git submodule update --init && \
    ./autogen.sh && ./configure --enable-shared && \
    make -j$(nproc) && make -j$(nproc) check && make install && make clean && ldconfig && \
    echo "--- installing grpc ---" && \
    cd /var/local/git/grpc && \
    make -j$(nproc) && make install && make clean && ldconfig && \
    rm -rf /var/local/git/grpc

WORKDIR /opt/src/cpp

COPY ./ ./

WORKDIR /opt/src/cpp/build
RUN cmake .. && \
    make -j 8
