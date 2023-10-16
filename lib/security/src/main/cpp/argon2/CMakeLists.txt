target_include_directories(${PROJECT_NAME}
        PUBLIC
        "${CMAKE_CURRENT_LIST_DIR}/include"
        "${CMAKE_CURRENT_LIST_DIR}/src/"
        "${CMAKE_CURRENT_LIST_DIR}/src/blake2"
)

target_sources(${PROJECT_NAME}
        PRIVATE
        "${CMAKE_CURRENT_LIST_DIR}/src/blake2/blake2b.c"
        "${CMAKE_CURRENT_LIST_DIR}/src/argon2.c"
        "${CMAKE_CURRENT_LIST_DIR}/src/core.c"
        "${CMAKE_CURRENT_LIST_DIR}/src/encoding.c"
        "${CMAKE_CURRENT_LIST_DIR}/src/ref.c"
        "${CMAKE_CURRENT_LIST_DIR}/src/thread.c"
)