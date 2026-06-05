# Fidiro VPN

A perfect VPN implementation for Android devices.

## Features

- 🔒 Secure VPN connection
- 📱 Easy-to-use UI
- 🚀 Fast and reliable
- 🔐 Packet encryption and obfuscation
- 📡 DNS protection

## Building the APK

### Prerequisites

- Java 11 or higher
- Android SDK (API 34)
- Gradle 8.1.0 or higher

### Build Instructions

#### Using Gradle Wrapper (Recommended)

**Debug APK:**
```bash
./gradlew assembleDebug
```

**Release APK:**
```bash
./gradlew assembleRelease
```

### Output Location

- **Debug APK:** `app/build/outputs/apk/debug/app-debug.apk`
- **Release APK:** `app/build/outputs/apk/release/app-release.apk`

### Installation

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Project Structure

```
.
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── AndroidManifest.xml
│   │       ├── java/com/fidiro/vpn/
│   │       │   ├── MainActivity.java
│   │       │   └── FidiroVPNService.java
│   │       └── res/
│   │           ├── layout/
│   │           ├── values/
│   │           └── mipmap/
│   ├── build.gradle
│   └── proguard-rules.pro
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## Components

### MainActivity
Main activity handling the UI and VPN connection initiation.

### FidiroVPNService
Service handling the VPN tunnel, packet processing, and encryption.

## Configuration

Edit these files to customize:

- `gradle.properties` - Project-wide settings
- `app/build.gradle` - App-specific build configuration
- `.github/workflows/build-apk.yml` - CI/CD pipeline

## Permissions Required

- `INTERNET` - Network access
- `ACCESS_NETWORK_STATE` - Network status monitoring
- `FOREGROUND_SERVICE` - Background service
- `BIND_VPN_SERVICE` - VPN service binding

## API Requirements

- Minimum SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)

## License

This project is open source and available under the MIT License.

## Support

For issues and questions, please create an issue on GitHub.
