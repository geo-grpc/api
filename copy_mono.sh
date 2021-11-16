#!/usr/bin/env bash

# Copy JAVA from mono
cp -r ../../nearspacelabs/mono/java/geometry-chain/epl-geometry-api/src ./java/geometry-chain/epl-geometry-api
cp -r ../../nearspacelabs/mono/java/geometry-chain/epl-geometry-api-ex/src ./java/geometry-chain/epl-geometry-api-ex
cp -r ../../nearspacelabs/mono/java/geometry-chain/epl-geometry-service/src ./java/geometry-chain/epl-geometry-service
cp ../../nearspacelabs/mono/java/geometry-chain/build.gradle ./java/geometry-chain/
cp ../../nearspacelabs/mono/java/geometry-chain/settings.gradle ./java/geometry-chain/
# Copy JAVA from mono

# Copy Python from mono
cp -r ../../nearspacelabs/mono/python/epl_geometry ./python
cp -r ../../nearspacelabs/mono/python/epl_protobuf ./python
# Copy Python from mono