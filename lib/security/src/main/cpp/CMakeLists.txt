cmake_minimum_required(VERSION 3.10.2)

project(tonsecurity)
message(STATUS "Project: ${PROJECT_NAME} ${ANDROID_ABI}")

set(CMAKE_C_STANDARD 11)

add_library(${PROJECT_NAME} SHARED security_jni.c)

add_subdirectory(argon2)

target_link_libraries(${PROJECT_NAME} android)
target_compile_options(${PROJECT_NAME} PUBLIC -finline-functions -ffast-math)