cmake_minimum_required(VERSION 3.13.4)
project(cpp)

#   BUILD_ONLY - only build project identified by this variable, a semi-colon delimited list, if this is set we will build only the projects listed. Core will always be built as will its unit tests.
#                    Also if a high level client is specified then we will build its dependencies as well. If a project has tests, the tests will be built.


set(JWT_LIBS m pthread)
if (APPLE)
    link_directories(/usr/local/lib)
    set(JWT_LIBS
            ${JWT_LIBS}
            /usr/local/lib/libjansson.dylib)
else()
    link_directories(/usr/lib/x86_64-linux-gnu)
    link_directories(/usr/local/lib)
    set(JWT_LIBS
            ${JWT_LIBS}
            /usr/lib/x86_64-linux-gnu/libjansson.so)
endif()

add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/epl-protobuf-v1)

if (APPLE)
    set(PROJECT_INCLUDES ${CMAKE_SOURCE_DIR}/src /usr/local/include/google/protobuf)
else()
    set(PROJECT_INCLUDES ${CMAKE_SOURCE_DIR}/src ${Protobuf_INCLUDE_DIRS})
endif()

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++17")

target_link_libraries(epl_protobuf_v1)

enable_testing()

add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/test)


