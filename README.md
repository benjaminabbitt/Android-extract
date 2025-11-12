# Android Text Extractor

A powerful Android application for extracting text from other applications' memory and UI components. Built with Kotlin and Android best practices, with optional Rust-based native library for advanced memory extraction on rooted devices.

## ⚠️ Important Notice

This tool is designed for:
- **Security research and penetration testing** (with proper authorization)
- **Educational purposes** and learning about Android internals
- **Accessibility testing** and UI analysis
- **Debugging and development** of your own applications

**Do not use this tool for:**
- Unauthorized access to other applications
- Stealing sensitive information
- Violating privacy or terms of service
- Any malicious purposes

Always ensure you have proper authorization before using this tool on any device or application.

## Features

### Accessibility Service-Based Extraction (No Root Required)
- ✅ Extract text from any Android application UI
- ✅ Monitor text fields and input areas
- ✅ Capture content descriptions and labels
- ✅ Real-time text extraction as users interact with apps
- ✅ Export extracted text to files
- ✅ Filter by application package name
- ✅ Works on non-rooted devices

### Native Memory Extraction (Root Required - Optional)
- 🔒 Direct memory access via `/proc/[pid]/mem`
- 🔒 String extraction from process heap
- 🔒 Memory region scanning
- 🔒 Environment variable extraction
- 🔒 Advanced process analysis

## Architecture

### Components

1. **TextExtractionAccessibilityService**: Core service that monitors accessibility events
2. **MainActivity**: UI for viewing and managing extracted text
3. **TextDataRepository**: In-memory storage for extracted data
4. **NativeMemoryExtractor** (Optional): Rust-based JNI wrapper for advanced extraction

### Technology Stack

- **Kotlin**: Primary language for Android app
- **Rust**: Optional native library for low-level memory access
- **Android Accessibility Services**: Official API for UI monitoring
- **JNI**: Bridge between Kotlin and Rust code

## Building the Project

### Prerequisites

- Android Studio Arctic Fox or newer
- Android SDK with API level 24+ (Android 7.0+)
- JDK 17
- Gradle 8.2+
- (Optional) Rust toolchain for native library

### Standard Build (Accessibility Service Only)

```bash
# Clone the repository
git clone https://github.com/yourusername/Android-extract.git
cd Android-extract

# Build with Gradle
./gradlew assembleDebug

# Or open in Android Studio and build
```

### Build with Native Library (Optional)

For advanced memory extraction features:

```bash
# Install Rust
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh

# Install Android NDK (via Android Studio SDK Manager)
# Set environment variable
export ANDROID_NDK_HOME=/path/to/ndk

# Build native library
cd native-extractor
./build.sh

# Return to root and build APK
cd ..
./gradlew assembleDebug
```

See [native-extractor/README.md](native-extractor/README.md) for detailed native build instructions.

## Installation

### Install on Device

```bash
# Install via ADB
adb install app/build/outputs/apk/debug/app-debug.apk

# Or transfer APK to device and install manually
```

### Grant Accessibility Permission

1. Open the app
2. Tap "Enable Service"
3. Navigate to Settings → Accessibility
4. Find "Text Extractor" service
5. Toggle it ON
6. Grant permission when prompted

## Usage

### Basic Text Extraction

1. **Enable the Service**: Launch the app and enable the accessibility service
2. **Use Other Apps**: Open any application and interact with it
3. **View Extracted Text**: Return to Text Extractor to see captured text
4. **Filter Results**: Tap on entries to see details by app
5. **Export Data**: Use the "Export Log" button to save results

### Advanced Memory Extraction (Root Only)

If you have root access and built with native library:

```kotlin
// Check root access
val status = NativeMemoryExtractor.checkRootAccess()

// Get process ID
val pid = NativeMemoryExtractor.getProcessIdByPackage("com.example.app")

// Read memory
if (pid != null) {
    val memoryInfo = NativeMemoryExtractor.readProcessMemory(pid)
    val strings = NativeMemoryExtractor.extractStrings(pid, minLength = 4)
}
```

## API Reference

### TextExtractionAccessibilityService

Main accessibility service that captures text from other apps.

**Events Monitored:**
- `TYPE_WINDOW_STATE_CHANGED`: Window/activity changes
- `TYPE_WINDOW_CONTENT_CHANGED`: Content updates
- `TYPE_VIEW_TEXT_CHANGED`: Text field changes
- `TYPE_VIEW_FOCUSED`: Focus events

### ExtractedTextData

Data class representing captured text:

```kotlin
data class ExtractedTextData(
    val packageName: String,      // App package name
    val appName: String,           // Human-readable app name
    val text: String,              // Extracted text
    val className: String?,        // UI component class
    val viewIdResourceName: String?, // View resource ID
    val timestamp: Long,           // Capture timestamp
    val eventType: String          // Event that triggered capture
)
```

### TextDataRepository

Singleton for storing and managing extracted data:

```kotlin
// Add data
TextDataRepository.addExtractedText(data)

// Get all data
val allData = TextDataRepository.getAllData()

// Filter by package
val appData = TextDataRepository.getDataByPackage("com.example.app")

// Clear data
TextDataRepository.clearData()

// Listen for new data
TextDataRepository.addListener { data ->
    // Handle new extracted text
}
```

## Project Structure

```
Android-extract/
├── app/
│   ├── src/main/
│   │   ├── java/com/textextractor/
│   │   │   ├── MainActivity.kt                    # Main UI activity
│   │   │   ├── TextExtractionAccessibilityService.kt  # Core service
│   │   │   ├── ExtractedTextData.kt              # Data models
│   │   │   ├── NativeMemoryExtractor.kt          # JNI wrapper
│   │   │   └── TextExtractorApp.kt               # Application class
│   │   ├── res/
│   │   │   ├── layout/                           # UI layouts
│   │   │   ├── values/                           # Strings, styles
│   │   │   └── xml/                              # Accessibility config
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── native-extractor/                             # Rust native library
│   ├── src/
│   │   └── lib.rs                                # Rust implementation
│   ├── Cargo.toml
│   ├── build.sh                                  # Build script
│   └── README.md
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Security Considerations

### Permissions

The app requests minimal permissions:
- `BIND_ACCESSIBILITY_SERVICE`: Required for text extraction
- `READ_LOGS`: Optional, for advanced debugging

### Data Privacy

- All extracted text is stored **only in memory**
- Data is cleared when app is closed
- No network access - data never leaves device
- Export functionality only writes to local storage

### Root Access

Native memory extraction requires root:
- Only works on explicitly rooted devices
- Requires user to grant root access
- Falls back gracefully on non-rooted devices

## Troubleshooting

### Service Not Working

1. Verify accessibility service is enabled in Settings
2. Check that app has not been force-stopped
3. Restart device if service becomes unresponsive
4. Check logcat for errors: `adb logcat -s TextExtractor`

### Native Library Issues

If native features don't work:
1. Ensure device architecture is supported (ARM64, ARM32, x86_64, x86)
2. Verify NDK was configured correctly during build
3. Check if device is rooted: `adb shell su -c "id"`
4. The app works fine without native library (accessibility service only)

### No Text Captured

- Some apps may use custom views that don't expose text via accessibility
- WebView content may require additional configuration
- Canvas-rendered text cannot be captured via accessibility APIs

## Development

### Adding New Features

1. Fork the repository
2. Create a feature branch: `git checkout -b feature-name`
3. Make your changes
4. Test thoroughly on multiple devices
5. Submit a pull request

### Running Tests

```bash
# Unit tests
./gradlew test

# Instrumentation tests (requires device/emulator)
./gradlew connectedAndroidTest
```

### Debugging

```bash
# View logs
adb logcat -s TextExtractor

# Debug accessibility service
adb shell settings get secure enabled_accessibility_services
```

## Contributing

Contributions are welcome! Please:
1. Follow Android and Kotlin best practices
2. Add tests for new features
3. Update documentation
4. Ensure code passes linting: `./gradlew ktlintCheck`

## License

This project is for educational and research purposes. Use responsibly and ethically.

## Acknowledgments

- Built with [Kotlin](https://kotlinlang.org/)
- Native library powered by [Rust](https://www.rust-lang.org/)
- Uses [Timber](https://github.com/JakeWharton/timber) for logging
- Inspired by Android accessibility services documentation

## Support

For issues, questions, or contributions:
- Open an issue on GitHub
- Check existing documentation
- Review Android accessibility services documentation

## Disclaimer

This tool is provided for educational and authorized security testing purposes only. The authors are not responsible for any misuse or damage caused by this tool. Always obtain proper authorization before testing on any system you do not own.
