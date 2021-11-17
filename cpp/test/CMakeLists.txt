# https://github.com/dmonopoly/gtest-cmake-example/blob/master/CMakeLists.txt
# http://www.kaizou.org/2014/11/gtest-cmake/
# https://stackoverflow.com/questions/8507723/how-to-start-working-with-gtest-and-cmake
cmake_minimum_required(VERSION 3.7)

# Cmake find modules
list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_LIST_DIR}/../cmake")

find_package(Protobuf REQUIRED)
find_package(GRPC REQUIRED)
FIND_PACKAGE(CURL)

project(unitTest C CXX)
#find_package(GTest REQUIRED)

#target_link_libraries(GoogleTest /usr/local/lib/libgtest.a /usr/local/lib/libgtest_main.a)
include(FetchContent)
FetchContent_Declare(
        googletest
        URL https://github.com/google/googletest/archive/609281088cfefc76f9d0ce82e1ff6c30cc3591e5.zip
)
# For Windows: Prevent overriding the parent project's compiler/linker settings
set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)
FetchContent_MakeAvailable(googletest)

include_directories(${GTEST_INCLUDE_DIRS}
        ${protobuf_lib_INCLUDE}
        ${CMAKE_SOURCE_DIR}/protobuf-lib
        ./helper.h)

set(SOURCE_FILES ./helper.cpp ./main.cpp)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++17")

add_executable(unitTest ${SOURCE_FILES})


target_link_libraries(unitTest
        ${GTEST_BOTH_LIBRARIES}
        ${CMAKE_THREAD_LIBS_INIT}
        ${JWT_LIBS}
        epl_protobuf_v1
        curl)

