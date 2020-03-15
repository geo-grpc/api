# geometry-chain
Geometry library with Projection and Geodetic methods inheriting from an [altered version](https://github.com/davidraleigh/geometry-api-java/tree/epl) of the ESRI geometry-api-java.

## Dependencies and Requirements
- JDK of 11+
- 5.2 Proj lib with JNI

## Building
Your jni `proj.jar` file for Proj 5.2 must be located in `/usr/local/lib/proj.jar` or you'll need to update that in your build.gradle file.
```bash
./gradlew build install
```
