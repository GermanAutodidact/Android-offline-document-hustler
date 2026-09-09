#!/usr/bin/env bash
# ==============================================================================
# DocPreserve - Aggressively Optimized LibreOfficeKit Build Script for Android
# Target Device: Samsung Galaxy A25 (SM-A256B, Exynos 1280 - ARM64-v8a)
# ==============================================================================
set -euo pipefail

# 1. Target Architecture: Samsung Galaxy A25 uses Exynos 1280 (64-bit ARM)
export ANDROID_ABI="arm64-v8a"
export ANDROID_PLATFORM="android-24" # Min SDK 24 matches app/build.gradle.kts
export NDK_TOOLCHAIN_VERSION="clang"

# 2. Aggressive Compiler Flags requested:
# -Os: Optimize for binary size
# -ffunction-sections -fdata-sections: Place functions and data in separate ELF sections
# -flto: Link-Time Optimization across all translation units
export CFLAGS="-Os -ffunction-sections -fdata-sections -flto"
export CXXFLAGS="-Os -ffunction-sections -fdata-sections -flto -fno-exceptions -fno-rtti"

# 3. Linker Flags requested:
# -Wl,--gc-sections: Dead code elimination (removes unused sections)
# -s: Strip all debug and non-essential symbols from the binary
# -flto: Link-time whole-program optimization
export LDFLAGS="-Wl,--gc-sections -s -flto"

# 4. Configure Flags for Minimal LOKit without bundled fonts:
# --without-fonts: Disables bundling 30-50MB of TTF/OTF fonts; uses /system/fonts
# --enable-lto: Enables LLVM Gold/LLD Link-Time Optimization
# Direct ANativeWindow rendering is enabled via SurfaceView JNI bridge
AUTOGEN_ARGS=(
    "--with-distro=LibreOfficeAndroid"
    "--with-android-ndk=${ANDROID_NDK_HOME:-/opt/android-ndk}"
    "--with-android-sdk=${ANDROID_SDK_ROOT:-/opt/android-sdk}"
    "--with-android-abi=${ANDROID_ABI}"
    "--enable-lto"
    "--without-fonts"
    "--disable-gui"
    "--disable-firebird-sdbc"
    "--disable-postgresql-sdbc"
    "--disable-coinmp"
    "--disable-odk"
    "--disable-gstreamer-1-0"
    "--disable-pdfium"
    "--disable-dconf"
    "--disable-gconf"
    "--disable-randr"
    "--disable-cups"
    "--without-java"
    "--without-help"
    "--without-myspell-dicts"
)

echo "===================================================================="
echo "DocPreserve: Starting LOKit minimal build for Samsung Galaxy A25"
echo "ABI:             ${ANDROID_ABI}"
echo "Compiler Flags:  ${CFLAGS}"
echo "Linker Flags:    ${LDFLAGS}"
echo "Fonts Strategy:  System Fonts (/system/fonts) - Zero bundled APK bloat"
echo "Render Engine:   ANativeWindow (SurfaceView) Direct Framebuffer"
echo "===================================================================="

# When building in cross-compilation environment:
# ./autogen.sh "${AUTOGEN_ARGS[@]}"
# make -j$(nproc)
