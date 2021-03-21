#!/usr/bin/env bash

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

docker push "${C_BUILDER}"
docker push "${PROJ}"
docker push "${GEOMETRY}"
