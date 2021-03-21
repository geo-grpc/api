ARG TAG=buster-4385c8a
ARG JAVA_VERSION=11.0.10
ARG JRE_TAG=${JAVA_VERSION}-jre-slim-buster
FROM geogrpc/proj:${TAG} as builder

MAINTAINER David Raleigh <davidraleigh@gmail.com>

# https://github.com/rocker-org/shiny/issues/19#issuecomment-308357402
RUN apt-get update || apt-get update

COPY ./ /opt/src/geometry-chain

WORKDIR /opt/src/geometry-chain


RUN ./gradlew build install
# Weirdest. works in docker on mac but not in linux
# RUN tar -xvf /opt/src/geometry-chain/epl-geometry-service/build/distributions/epl-geometry-service.tar
RUN tar -xf /opt/src/geometry-chain/epl-geometry-service/build/distributions/epl-geometry-service.tar -C /opt/src/geometry-chain/epl-geometry-service/build/distributions


# Production build
FROM openjdk:${JRE_TAG}

WORKDIR /usr/local
COPY --from=builder /usr/local .
RUN export JAVA_HOME=$(readlink -f /usr/bin/javac | sed "s:/bin/javac::")

ENV PROJ_LIB=/usr/local/share/proj

RUN /sbin/ldconfig

WORKDIR /opt/src/geometry-chain/build/install
COPY --from=builder /opt/src/geometry-chain/epl-geometry-service/build/distributions/epl-geometry-service ./

RUN chmod +x /opt/src/geometry-chain/build/install/bin/geometry-operators-server

EXPOSE 8980

WORKDIR /opt/src/geometry-chain/build/test-results
COPY --from=builder /opt/src/geometry-chain/epl-geometry-service/build/test-results .
COPY --from=builder /opt/src/geometry-chain/epl-geometry-api/build/test-results .
COPY --from=builder /opt/src/geometry-chain/epl-geometry-api-ex/build/test-results .

CMD /opt/src/geometry-chain/build/install/bin/geometry-operators-server
