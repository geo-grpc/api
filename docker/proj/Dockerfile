ARG TAG=buster-4385c8a
ARG JAVA_VERSION=11.0.10
FROM geogrpc/c-builder:${TAG} as builder

RUN DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends bsdtar

RUN export JAVA_HOME=$(readlink -f /usr/bin/javac | sed "s:/bin/javac::")

WORKDIR /opt/src
RUN wget -qO- https://github.com/geo-grpc/proj.4/archive/master.zip | bsdtar -xvf-

WORKDIR /opt/src/proj.4-master
# CC="ccache gcc" CFLAGS="-g -Wfloat-conversion -Wall -Wextra -Werror -Wunused-parameter -Wmissing-prototypes -Wmissing-declarations -Wformat -Werror=format-security -Wshadow -O2"
RUN chmod +x autogen.sh && \
    ./autogen.sh && \
    CFLAGS=-I$JAVA_HOME/include/linux ./configure --with-jni=$JAVA_HOME/include --prefix=/usr/local && \
    make -j 8 && \
    make install && \
    cd jniwrap && \
    ant -v && \
    mv /opt/src/proj.4-master/jniwrap/out/proj.jar /usr/local/lib/

# Horizontal datums to improve test results
WORKDIR /usr/local/share/proj
ARG FETCH_VERT=0
ENV VERT_DATUM=$FETCH_VERT
RUN chmod +x /opt/src/proj.4-master/datum-installs.sh && \
    /opt/src/proj.4-master/datum-installs.sh $FETCH_VERT

ENV PROJ_LIB=/usr/local/share/proj
WORKDIR /opt/src/proj.4-master
# TODO What's wrong here?
#RUN make check && \
#    cd src
#    make multistresstest && \
#    make test228
