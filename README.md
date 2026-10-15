# FF DATA INJECTOR

A high-performance, native Android application built with **Kotlin** and **Jetpack Compose** for managing and injecting user-selected data files for the official Free Fire Android packages (`com.dts.freefireth` and `com.dts.freefiremax`).

---

## Features

- **Official Free Fire Package Management**:
  - Automatically queries and detects official Free Fire (`com.dts.freefireth`) and Free Fire MAX (`com.dts.freefiremax`).
  - Displays game name, package ID, version name, and build number.
  - Launches the game with one tap or directs to the official Google Play Store page if not installed.
- **Storage Access Framework (SAF) File Picker**:
  - Select up to 10 managed files from device storage.
  - Strictly enforces the 250 MB per-file size boundary (rejects oversized files with clear diagnostics).
  - Validates non-empty files (rejects 0-byte files) and detects duplicate selections.
- **Destination Folder & Android 11+ Scoped Storage Compliance**:
  - Target destination: `Android/data/com.dts.freefireth/files/`.
  - Storage explainer modal clarifying Android 11+ sandboxing rules.
  - Active write verification test (`.perm_check_*`).
  - Transparent error reporting if the operating system restricts access to `Android/data`.
- **Streaming Byte Copy (INJECT Action)**:
  - Background coroutine streaming I/O with 64 KB buffers to prevent Out-Of-Memory (OOM) issues on low-end devices.
  - Real-time progress percentage and byte transfer tracking.
  - Verifies written byte size against source file upon completion.
- **Safe File Removal (REMOVE Action)**:
  - Removes only files placed and tracked by this application.
  - Confirmation dialog prevents accidental file loss.
  - "REMOVE ALL" action to safely clean up all injected files.
- **Persistence Across App Restarts**:
  - Managed file statuses, verified byte sizes, selected game target, and destination tree permissions are preserved across device reboots and app restarts.
- **Real-Time Operation Console**:
  - Terminal-style logger displaying live timestamped events, SAF checks, and copy progress.

---

## Building the Project

### Prerequisites
- Android Studio Ladybug / Meerkat (or newer)
- JDK 17
- Android SDK with API 36 / 34 build-tools

### Build Debug APK locally
```bash
./gradlew assembleDebug
```
The output APK is generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Running Unit & Robolectric Tests
```bash
./gradlew testDebugUnitTest
```

---

## Automated GitHub Actions CI
The repository includes a ready-to-use GitHub Actions workflow (`.github/workflows/build-apk.yml`) that automatically compiles `app-debug.apk` and uploads it as a downloadable artifact on every push.
