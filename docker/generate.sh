#!/usr/bin/env bash
set -e

JAVA_VERSION=11.0.10
COMMIT_SHA=$(git log -1 --format=%h)
PREFIX=geogrpc/
TAG=buster-${COMMIT_SHA}
if [[ $# -eq 0 ]] ; then
    echo tag "${TAG}"
else
  TAG=${TAG}-$1
  echo tag "${TAG}"
fi

C_BUILDER=${PREFIX}c-builder:${TAG}
PROJ=${PREFIX}proj:${TAG}
GEOMETRY=${PREFIX}geometry-chain:${TAG}

docker build --build-arg JAVA_VERSION="${JAVA_VERSION}" -t "${C_BUILDER}" -f ./c-builder/Dockerfile .
docker build --build-arg JAVA_VERSION="${JAVA_VERSION}" --build-arg TAG="${TAG}" -t "${PROJ}" -f ./proj/Dockerfile .
cd ../java/geometry-chain
docker build --build-arg JAVA_VERSION="${JAVA_VERSION}" --build-arg TAG="${TAG}" -t "${GEOMETRY}" .
cd ../../docker