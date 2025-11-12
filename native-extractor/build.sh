#!/bin/bash

# Build script for Rust native library for Android
# This script compiles the Rust code for multiple Android architectures

set -e

echo "Building native-extractor for Android..."

# Android NDK targets
TARGETS=(
    "aarch64-linux-android"    # ARM64
    "armv7-linux-androideabi"  # ARM32
    "x86_64-linux-android"     # x86_64
    "i686-linux-android"       # x86
)

# Check if cargo is installed
if ! command -v cargo &> /dev/null; then
    echo "Error: Rust/Cargo not found. Please install Rust from https://rustup.rs/"
    exit 1
fi

# Check if Android NDK targets are installed
echo "Checking Rust Android targets..."
for target in "${TARGETS[@]}"; do
    if ! rustup target list | grep -q "$target (installed)"; then
        echo "Installing target: $target"
        rustup target add "$target"
    fi
done

# Build for each target
for target in "${TARGETS[@]}"; do
    echo "Building for $target..."
    cargo build --release --target "$target"
done

# Create jniLibs directory structure
echo "Creating jniLibs directory structure..."
JNILIBS_DIR="../app/src/main/jniLibs"
mkdir -p "$JNILIBS_DIR/arm64-v8a"
mkdir -p "$JNILIBS_DIR/armeabi-v7a"
mkdir -p "$JNILIBS_DIR/x86_64"
mkdir -p "$JNILIBS_DIR/x86"

# Copy built libraries to jniLibs
echo "Copying libraries..."
cp "target/aarch64-linux-android/release/libnative_extractor.so" "$JNILIBS_DIR/arm64-v8a/"
cp "target/armv7-linux-androideabi/release/libnative_extractor.so" "$JNILIBS_DIR/armeabi-v7a/"
cp "target/x86_64-linux-android/release/libnative_extractor.so" "$JNILIBS_DIR/x86_64/"
cp "target/i686-linux-android/release/libnative_extractor.so" "$JNILIBS_DIR/x86/"

echo "Build complete! Libraries copied to $JNILIBS_DIR"
echo ""
echo "Note: To use these libraries, you need to:"
echo "1. Set up Android NDK"
echo "2. Set ANDROID_NDK_HOME environment variable"
echo "3. Configure cargo with NDK linker paths"
echo ""
echo "See native-extractor/README.md for detailed instructions"
