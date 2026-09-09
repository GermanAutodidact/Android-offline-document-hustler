cmake_minimum_required(VERSION 3.22.1)
project("lokit_native_render")

# Aggressive compiler flags for maximum binary size reduction and speed
# -Os: Optimize for binary size
# -ffunction-sections -fdata-sections: Place functions and data in separate sections
# -flto: Link-Time Optimization across translation units
set(CMAKE_C_FLAGS_RELEASE "-Os -ffunction-sections -fdata-sections -flto")
set(CMAKE_CXX_FLAGS_RELEASE "-Os -ffunction-sections -fdata-sections -flto -fno-exceptions -fno-rtti")

# Linker flags:
# -Wl,--gc-sections: Dead code elimination at link time
# -s: Strip all symbols
# -flto: Whole-program optimization
set(CMAKE_SHARED_LINKER_FLAGS_RELEASE "-Wl,--gc-sections -s -flto")

add_library(
    lokit_native_render
    SHARED
    lokit_native_window.cpp
)

# Android native libraries required for direct ANativeWindow rendering
find_library(log-lib log)
find_library(android-lib android)

target_link_libraries(
    lokit_native_render
    ${android-lib}
    ${log-lib}
)
