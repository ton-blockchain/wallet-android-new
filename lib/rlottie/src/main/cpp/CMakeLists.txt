cmake_minimum_required(VERSION 3.10.2)

project(tonrlottie)

set(CMAKE_CXX_STANDARD 14)

if (${ANDROID_ABI} STREQUAL "armeabi-v7a" OR ${ANDROID_ABI} STREQUAL "arm64-v8a")
    enable_language(ASM)
else()
    enable_language(ASM_NASM)
endif()

add_subdirectory(lz4)
add_subdirectory(rlottie)

add_library(${PROJECT_NAME} SHARED rlottie_jni.cpp)
target_link_libraries(${PROJECT_NAME} android jnigraphics lz4 rlottie)