# Native Memory Extractor

Rust-based native library for advanced memory extraction on Android devices. This library provides low-level memory access capabilities for security research and debugging purposes.

## Overview

The native-extractor library is written in Rust and compiled to native Android libraries (.so files) for multiple architectures. It provides JNI interfaces that can be called from Kotlin/Java code.

## Features

- 🔍 Direct process memory reading via `/proc/[pid]/mem`
- 🧵 Memory region analysis via `/proc/[pid]/maps`
- 📝 String extraction from process memory
- 🔒 Root access verification
- 🌐 Multi-architecture support (ARM64, ARM32, x86_64, x86)

## Prerequisites

### Required Tools

1. **Rust Toolchain**
   ```bash
   # Install Rust
   curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh
   source $HOME/.cargo/env
   ```

2. **Android NDK**
   - Install via Android Studio SDK Manager
   - Or download from: https://developer.android.com/ndk/downloads
   - Minimum NDK version: r25 or newer

3. **Android Targets for Rust**
   ```bash
   rustup target add aarch64-linux-android
   rustup target add armv7-linux-androideabi
   rustup target add x86_64-linux-android
   rustup target add i686-linux-android
   ```

### Environment Setup

Set the ANDROID_NDK_HOME environment variable:

```bash
# Linux/macOS
export ANDROID_NDK_HOME=/path/to/android-ndk
export ANDROID_NDK_ROOT=$ANDROID_NDK_HOME

# Add to ~/.bashrc or ~/.zshrc for persistence
echo 'export ANDROID_NDK_HOME=/path/to/android-ndk' >> ~/.bashrc
```

### Cargo Configuration

Create `~/.cargo/config.toml` with NDK linker paths:

```toml
[target.aarch64-linux-android]
linker = "/path/to/android-ndk/toolchains/llvm/prebuilt/linux-x86_64/bin/aarch64-linux-android30-clang"

[target.armv7-linux-androideabi]
linker = "/path/to/android-ndk/toolchains/llvm/prebuilt/linux-x86_64/bin/armv7a-linux-androideabi30-clang"

[target.x86_64-linux-android]
linker = "/path/to/android-ndk/toolchains/llvm/prebuilt/linux-x86_64/bin/x86_64-linux-android30-clang"

[target.i686-linux-android]
linker = "/path/to/android-ndk/toolchains/llvm/prebuilt/linux-x86_64/bin/i686-linux-android30-clang"
```

**Note**: Replace `/path/to/android-ndk` with your actual NDK path and adjust `linux-x86_64` if you're on macOS (use `darwin-x86_64`) or Windows.

## Building

### Quick Build

Use the provided build script:

```bash
cd native-extractor
./build.sh
```

This script will:
1. Check for required tools
2. Install Android targets if needed
3. Build for all supported architectures
4. Copy libraries to `app/src/main/jniLibs/`

### Manual Build

Build for specific architectures:

```bash
# ARM64 (most common on modern Android devices)
cargo build --release --target aarch64-linux-android

# ARM32
cargo build --release --target armv7-linux-androideabi

# x86_64 (for emulators)
cargo build --release --target x86_64-linux-android

# x86
cargo build --release --target i686-linux-android
```

Built libraries will be in `target/<arch>/release/libnative_extractor.so`

### Copy to Android Project

```bash
# Create jniLibs directory structure
mkdir -p ../app/src/main/jniLibs/{arm64-v8a,armeabi-v7a,x86_64,x86}

# Copy libraries
cp target/aarch64-linux-android/release/libnative_extractor.so \
   ../app/src/main/jniLibs/arm64-v8a/

cp target/armv7-linux-androideabi/release/libnative_extractor.so \
   ../app/src/main/jniLibs/armeabi-v7a/

cp target/x86_64-linux-android/release/libnative_extractor.so \
   ../app/src/main/jniLibs/x86_64/

cp target/i686-linux-android/release/libnative_extractor.so \
   ../app/src/main/jniLibs/x86/
```

## Architecture Details

### Supported Android ABIs

| Rust Target | Android ABI | Description |
|------------|-------------|-------------|
| `aarch64-linux-android` | `arm64-v8a` | 64-bit ARM (most modern devices) |
| `armv7-linux-androideabi` | `armeabi-v7a` | 32-bit ARM (older devices) |
| `x86_64-linux-android` | `x86_64` | 64-bit x86 (emulators) |
| `i686-linux-android` | `x86` | 32-bit x86 (older emulators) |

### Library Size

The Rust library is optimized for size:
- Release builds use `opt-level = "z"` (optimize for size)
- LTO (Link Time Optimization) enabled
- Symbols stripped
- Typical size: 200-500 KB per architecture

## API Reference

### JNI Functions

#### checkRootAccess()

```kotlin
NativeMemoryExtractor.checkRootAccess(): String
```

Checks if the device has root access by attempting to access protected system files.

**Returns**: Status message indicating root availability

#### readProcessMemory(pid)

```kotlin
NativeMemoryExtractor.readProcessMemory(pid: Int): String
```

Reads memory maps and information for a given process.

**Parameters**:
- `pid`: Process ID to read

**Returns**: Memory map information or error message

**Requires**: Root access

#### extractStrings(pid, minLength)

```kotlin
NativeMemoryExtractor.extractStrings(pid: Int, minLength: Int): String
```

Extracts printable strings from process memory.

**Parameters**:
- `pid`: Process ID to extract from
- `minLength`: Minimum string length (default: 4)

**Returns**: Extracted strings and process information

**Requires**: Root access

## Implementation Details

### Memory Access Methods

The library uses several Linux `/proc` filesystem interfaces:

1. **`/proc/[pid]/maps`**: Memory region information
2. **`/proc/[pid]/mem`**: Direct memory access (requires root)
3. **`/proc/[pid]/cmdline`**: Process command line
4. **`/proc/[pid]/environ`**: Environment variables

### Security

⚠️ **Important Security Notes**:

- Reading other processes' memory requires **root access**
- SELinux policies may block access even with root
- Modern Android versions have additional protections
- This tool should only be used on devices you own
- Always obtain proper authorization for security testing

### Error Handling

The library provides graceful error handling:
- Missing root access → Informative error messages
- SELinux blocks → Clear indication of restrictions
- Invalid PIDs → Safe error reporting
- Library not loaded → Kotlin fallback to accessibility service

## Testing

### Run Unit Tests

```bash
cargo test
```

### Test on Device

1. Build and install the full Android app
2. Grant root access when prompted
3. Use the app to test extraction on a target app
4. Check logcat for detailed output:
   ```bash
   adb logcat -s TextExtractor
   ```

## Troubleshooting

### Build Errors

**Error**: `linker not found`
- **Solution**: Configure `~/.cargo/config.toml` with correct NDK linker paths

**Error**: `target not found`
- **Solution**: Install Android target: `rustup target add <target-name>`

**Error**: `ANDROID_NDK_HOME not set`
- **Solution**: Export NDK path in your environment

### Runtime Errors

**Error**: `UnsatisfiedLinkError`
- **Solution**: Ensure library is in correct `jniLibs/<abi>` directory
- Check device architecture matches built library

**Error**: `Permission denied` on `/proc/[pid]/mem`
- **Solution**: Requires root access
- Grant root permission to the app
- Check SELinux status: `adb shell getenforce`

**Error**: Native functions return errors
- **Solution**: Check logcat for detailed Rust error messages
- Verify target app is running: `adb shell ps | grep <package>`

## Development

### Adding New Native Functions

1. **Define JNI function in Rust** (`lib.rs`):
   ```rust
   #[no_mangle]
   pub extern "C" fn Java_com_textextractor_NativeMemoryExtractor_myFunction(
       env: JNIEnv,
       _class: JClass,
       param: i32,
   ) -> jstring {
       // Implementation
   }
   ```

2. **Declare in Kotlin** (`NativeMemoryExtractor.kt`):
   ```kotlin
   private external fun nativeMyFunction(param: Int): String
   ```

3. **Rebuild** native library:
   ```bash
   ./build.sh
   ```

### Dependencies

Current dependencies in `Cargo.toml`:
- `jni`: JNI bindings for Rust
- `libc`: C library bindings
- `nix`: Unix system APIs

Add new dependencies as needed:
```toml
[dependencies]
your-crate = "1.0"
```

## Performance Considerations

- Memory operations are expensive - use sparingly
- Scanning large memory regions can take time
- Consider using filters to reduce extracted data
- Multi-threaded scanning may improve performance

## Alternatives

If native library is not available or root access is not possible:
- Use the **Accessibility Service** (no root required)
- Android Debug Bridge (ADB) for development
- Memory dumps via Android Studio profiler

## References

- [Android NDK Documentation](https://developer.android.com/ndk)
- [Rust JNI Bindings](https://docs.rs/jni/)
- [Linux proc filesystem](https://man7.org/linux/man-pages/man5/proc.5.html)
- [Android Security Overview](https://source.android.com/docs/security)

## License

This native library is part of the Android Text Extractor project and follows the same license and usage guidelines.

## Contributing

When contributing to the native library:
1. Test on multiple architectures
2. Ensure no memory leaks
3. Handle errors gracefully
4. Document security implications
5. Follow Rust best practices
