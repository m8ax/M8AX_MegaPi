cmake_minimum_required(VERSION 3.22.1)
project("m8ax_megapi")
include_directories(${CMAKE_SOURCE_DIR})
add_library(gmp_lib SHARED IMPORTED)
set_target_properties(gmp_lib PROPERTIES IMPORTED_LOCATION
    ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libgmp.so)
add_library(${CMAKE_PROJECT_NAME} SHARED
    native-lib.cpp
    m8axpi.c)
target_link_libraries(${CMAKE_PROJECT_NAME}
    gmp_lib
    android
    log)