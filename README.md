# DataRescue Pro - Professional File Recovery App

DataRescue Pro is a comprehensive Android file recovery application that helps users recover deleted files including photos, videos, documents, audio files, and more from both rooted and non-rooted Android devices.

## Features

### Core Functionality
- **Deep File Scanning**: Advanced algorithms to detect recoverable files
- **Multiple File Types**: Support for photos, videos, documents, audio, and more
- **Root & Non-Root Support**: Works on both rooted and standard Android devices
- **Real-time Progress**: Live scanning progress with detailed information
- **Selective Recovery**: Choose which files to recover
- **Native Performance**: C++ native libraries for optimal performance

### Supported File Types
- **Images**: JPG, PNG, GIF, BMP, TIFF, WEBP, HEIC
- **Videos**: MP4, AVI, MOV, MKV, 3GP, WEBM
- **Audio**: MP3, WAV, AAC, FLAC, OGG, M4A
- **Documents**: PDF, DOC, DOCX, XLS, XLSX, PPT, PPTX
- **Archives**: ZIP, RAR, 7Z
- **Applications**: APK files

### Technical Features
- **Jetpack Compose UI**: Modern, responsive user interface
- **Material Design 3**: Clean and intuitive design
- **Root Detection**: Automatic detection with enhanced features for rooted devices
- **File System Analysis**: Support for ext4, F2FS, and other file systems
- **Memory Optimization**: Efficient memory usage during scanning
- **Background Processing**: Non-blocking UI during recovery operations

## Architecture

### Frontend
- **Kotlin**: 100% Kotlin codebase
- **Jetpack Compose**: Modern UI toolkit
- **Material Design 3**: Clean, optimized UI
- **Coroutines**: Asynchronous operations
- **StateFlow**: Reactive state management
- **Navigation Compose**: Type-safe navigation

### Backend & Native
- **C++ Native Libraries**: JNI integration for performance-critical operations
- **File System Libraries**: libext4, libf2fs support
- **Custom NDK Modules**: Low-level disk scanning algorithms
- **File Signature Detection**: Magic number-based file type identification
- **Sector Scanning**: Deep disk analysis capabilities

### Monetization
- **Google AdMob Integration**: Multiple ad formats
- **Banner Ads**: Non-intrusive bottom placement
- **Interstitial Ads**: Between major actions
- **Reward Ads**: Before key features
- **Native Ads**: Integrated content ads

## Project Structure

```
app/
├── src/main/
│   ├── java/com/coderx/datarescuepro/
│   │   ├── ui/
│   │   │   ├── screens/          # All app screens
│   │   │   ├── components/       # Reusable UI components
│   │   │   └── theme/           # App theming
│   │   ├── core/
│   │   │   ├── recovery/        # File recovery engine
│   │   │   └── system/          # System utilities
│   │   ├── navigation/          # Navigation setup
│   │   └── service/             # Background services
│   ├── cpp/                     # Native C++ code
│   │   ├── datarescue.cpp       # JNI bridge
│   │   ├── file_scanner.cpp     # File scanning logic
│   │   └── disk_analyzer.cpp    # Disk analysis
│   └── res/                     # Android resources
```

## Building the Project

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK API 21-35
- NDK version 25.0 or later
- CMake 3.22.1 or later

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd DataRescuePro
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the project directory

3. **Configure NDK**
   - Ensure NDK is installed via SDK Manager
   - Verify CMake is available

4. **Build the project**
   ```bash
   ./gradlew assembleDebug
   ```

### AdMob Configuration

1. **Get AdMob App ID**
   - Create account at [AdMob Console](https://admob.google.com/)
   - Create new app and get App ID

2. **Update AndroidManifest.xml**
   ```xml
   <meta-data
       android:name="com.google.android.gms.ads.APPLICATION_ID"
       android:value="YOUR_ADMOB_APP_ID" />
   ```

3. **Configure Ad Unit IDs**
   - Update ad unit IDs in the AdBanner component
   - Replace test IDs with production IDs

## Permissions

The app requires the following permissions:

### Essential Permissions
- `READ_EXTERNAL_STORAGE`: Read files from storage
- `WRITE_EXTERNAL_STORAGE`: Save recovered files
- `MANAGE_EXTERNAL_STORAGE`: Full storage access (Android 11+)

### Optional Permissions
- `INTERNET`: AdMob ads
- `ACCESS_NETWORK_STATE`: Network status for ads
- `WAKE_LOCK`: Prevent sleep during long operations
- `VIBRATE`: User feedback

## Testing

### Device Testing
- Test on both rooted and non-rooted devices
- Verify across different Android versions (API 21-35)
- Test on various OEM customizations (Samsung, Xiaomi, etc.)

### Performance Testing
- Memory usage monitoring
- CPU usage optimization
- Battery impact analysis
- Large file handling

### Recovery Testing
- Test with various file types
- Verify recovery success rates
- Test edge cases and corrupted files

## Deployment

### Play Store Requirements
- Target latest Android API level
- Comply with storage permissions policy
- Include comprehensive privacy policy
- Use Android App Bundle format

### Release Checklist
- [ ] Update version code and name
- [ ] Enable ProGuard/R8 obfuscation
- [ ] Replace test AdMob IDs with production IDs
- [ ] Test on multiple devices
- [ ] Verify all permissions are necessary
- [ ] Update privacy policy
- [ ] Generate signed APK/AAB

## Performance Metrics

### Target Metrics
- **Recovery Success Rate**: >85% for accessible files
- **Scan Speed**: <2 minutes for 32GB storage
- **Memory Usage**: <200MB during operation
- **Battery Usage**: <5% per hour during scan
- **App Rating**: Target 4.5+ stars
- **Crash Rate**: <0.5% crash-free sessions

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions:
- Create an issue on GitHub
- Email: support@datarescuepro.com

## Changelog

### Version 1.0.0
- Initial release
- Basic file recovery functionality
- Root and non-root support
- AdMob integration
- Material Design 3 UI