project(lz4)

add_library(${PROJECT_NAME}
    lz4.c
    lz4frame.c
    lz4hc.c
    xxhash.c
)

target_include_directories(${PROJECT_NAME} PUBLIC "${PROJECT_SOURCE_DIR}")

target_compile_options(${PROJECT_NAME} PUBLIC -w -O3)
set_target_properties(${PROJECT_NAME} PROPERTIES ANDROID_ARM_MODE arm)