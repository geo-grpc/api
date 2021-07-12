cmake_minimum_required(VERSION 3.13.4)
project(epl_protobuf_v1)
set(CMAKE_VERBOSE_MAKEFILE ON)

list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_LIST_DIR}/../cmake")

find_package(Protobuf  REQUIRED)
find_package(GRPC REQUIRED)

set(protobuf_MODULE_COMPATIBLE TRUE)

message(STATUS "Using protobuf ${protobuf_VERSION}")

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++17")

set(${PROJECT_NAME}_INCLUDE
        /usr/local/include
        ${PROTO_SRC_DIR}
        /usr/local/include/grpc++
        ${CMAKE_SOURCE_DIR}/epl-protobuf-v1
        CACHE INTERNAL "${PROJECT_NAME}: Include Directories" FORCE)

include_directories(${${PROJECT_NAME}_INCLUDE})

add_library(${PROJECT_NAME}
        ${CMAKE_SOURCE_DIR}/epl-protobuf-v1/epl/protobuf/v1/geometry.pb.cc
        ${CMAKE_SOURCE_DIR}/epl-protobuf-v1/epl/protobuf/v1/geometry.pb.h
        ${CMAKE_SOURCE_DIR}/epl-protobuf-v1/epl/protobuf/v1/geometry_service.grpc.pb.cc
        ${CMAKE_SOURCE_DIR}/epl-protobuf-v1/epl/protobuf/v1/geometry_service.grpc.pb.h
        ${CMAKE_SOURCE_DIR}/epl-protobuf-v1/epl/protobuf/v1/geometry_service.pb.cc
        ${CMAKE_SOURCE_DIR}/epl-protobuf-v1/epl/protobuf/v1/geometry_service.pb.h
        ${CMAKE_SOURCE_DIR}/epl-protobuf-v1/epl/protobuf/v1/query.pb.cc
        ${CMAKE_SOURCE_DIR}/epl-protobuf-v1/epl/protobuf/v1/query.pb.h
        ${CMAKE_SOURCE_DIR}/epl-protobuf-v1/epl/protobuf/v1/stac.pb.cc
        ${CMAKE_SOURCE_DIR}/epl-protobuf-v1/epl/protobuf/v1/stac.pb.h
        ${CMAKE_SOURCE_DIR}/epl-protobuf-v1/epl/protobuf/v1/stac_service.grpc.pb.cc
        ${CMAKE_SOURCE_DIR}/epl-protobuf-v1/epl/protobuf/v1/stac_service.grpc.pb.h
        ${CMAKE_SOURCE_DIR}/epl-protobuf-v1/epl/protobuf/v1/stac_service.pb.cc
        ${CMAKE_SOURCE_DIR}/epl-protobuf-v1/epl/protobuf/v1/stac_service.pb.h
        )

target_link_libraries(${PROJECT_NAME}
        gRPC::grpc++_reflection
        protobuf::libprotobuf)