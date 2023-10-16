cmake_minimum_required(VERSION 3.10.2)

project(tonsecurity)
message(STATUS "Project: ${PROJECT_NAME} ${ANDROID_ABI}")

set(CMAKE_C_STANDARD 11)

# sodium
set(SODIUM_PATH ${CMAKE_SOURCE_DIR}/sodium)
add_library(libsodium SHARED IMPORTED)
set_target_properties(libsodium PROPERTIES IMPORTED_LOCATION ${SODIUM_PATH}/libs/${ANDROID_ABI}/libsodium.so)
include_directories(${SODIUM_PATH}/include)

add_library(${PROJECT_NAME} SHARED security_jni.c)

add_subdirectory(argon2)

target_link_libraries(${PROJECT_NAME} android libsodium)
target_compile_options(${PROJECT_NAME} PUBLIC -finline-functions -ffast-math)