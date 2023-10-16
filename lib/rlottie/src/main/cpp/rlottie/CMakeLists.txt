project(rlottie)

add_library(${PROJECT_NAME}
        src/lottie/lottieanimation.cpp
        src/lottie/lottieitem.cpp
        src/lottie/lottiekeypath.cpp
        src/lottie/lottieloader.cpp
        src/lottie/lottiemodel.cpp
        src/lottie/lottieparser.cpp
        src/lottie/lottieproxymodel.cpp
        src/vector/freetype/v_ft_math.cpp
        src/vector/freetype/v_ft_raster.cpp
        src/vector/freetype/v_ft_stroker.cpp
        src/vector/pixman/vregion.cpp
        src/vector/stb/stb_image.cpp
        src/vector/vbezier.cpp
        src/vector/vbitmap.cpp
        src/vector/vbrush.cpp
        src/vector/vcompositionfunctions.cpp
        src/vector/vdasher.cpp
        src/vector/vdebug.cpp
        src/vector/vdrawable.cpp
        src/vector/vdrawhelper.cpp
        src/vector/vdrawhelper_neon.cpp
        src/vector/velapsedtimer.cpp
        src/vector/vimageloader.cpp
        src/vector/vinterpolator.cpp
        src/vector/vmatrix.cpp
        src/vector/vpainter.cpp
        src/vector/vpath.cpp
        src/vector/vpathmesure.cpp
        src/vector/vraster.cpp
        src/vector/vrect.cpp
        src/vector/vrle.cpp)

target_compile_options(${PROJECT_NAME} PUBLIC
        -Wall
        -fno-rtti
        -finline-functions
        -ffast-math
        -Os
        -fno-unwind-tables
        -fno-asynchronous-unwind-tables
        -Wnon-virtual-dtor
        -Woverloaded-virtual
        -Wno-unused-parameter
        -fvisibility=hidden)

set_target_properties(${PROJECT_NAME} PROPERTIES ANDROID_ARM_MODE arm)
target_compile_definitions(${PROJECT_NAME} PUBLIC HAVE_PTHREAD NDEBUG)
target_include_directories(${PROJECT_NAME} PUBLIC
        inc
        src/vector/
        src/vector/pixman
        src/vector/freetype
        src/vector/stb)

if (${ANDROID_ABI} STREQUAL "armeabi-v7a")
    target_compile_options(${PROJECT_NAME} PUBLIC -fno-integrated-as)
    target_compile_definitions(${PROJECT_NAME} PUBLIC USE_ARM_NEON)
    target_sources(rlottie PRIVATE src/vector/pixman/pixman-arm-neon-asm.S)
elseif(${ANDROID_ABI} STREQUAL "arm64-v8a")
    target_compile_options(${PROJECT_NAME} PUBLIC -fno-integrated-as)
    target_compile_definitions(${PROJECT_NAME} PUBLIC USE_ARM_NEON __ARM64_NEON__)
    target_sources(${PROJECT_NAME} PRIVATE src/vector/pixman/pixman-arma64-neon-asm.S)
endif()
