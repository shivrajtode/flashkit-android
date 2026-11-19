# FlashKit Android (Native APK)

A native Android application built with **Jetpack Compose** & **Material 3 Expressive**, transforming an Android phone into a portable flashing and maintenance station over USB OTG.

Replaces the Termux:GUI shell implementation with a modern, standalone native APK.

---

## 🚀 Features

- **Multi-Tier Execution Engine**:
  - **🔓 Root (libsu)**: Unrestricted fastboot/ADB execution, direct block-level access.
  - **🟡 Shizuku**: ADB-level shell operations without needing root on the host.
  - **🟢 Rootless (USB Host API)**: Raw USB communication directly to target devices.
- **Partition Flashing**: Flash `boot`, `recovery`, `vbmeta`, `super`, `system`, or custom partitions.
- **ADB Sideload**: One-click ROM and OTA zip sideloading.
- **Reboot Control**: Reboot target to system, recovery, bootloader, or userspace fastbootd.
- **Raw Command Runner**: Run preset diagnostics or execute custom commands directly.
- **Material 3 Expressive UI**: Dynamic theming, real-time command console, responsive card-based layout.

---

## 🛠️ Project Structure

```
FlashKit/
├── app/
│   ├── src/main/
│   │   ├── java/com/flashkit/app/
│   │   │   ├── FlashKitApp.kt            # Application entry & libsu config
│   │   │   ├── MainActivity.kt           # Activity & USB Intent routing
│   │   │   ├── data/
│   │   │   │   ├── model/Models.kt       # DeviceState, JobStatus, Tiers
│   │   │   │   └── repository/DeviceRepository.kt # USB & Shell engine
│   │   │   └── ui/
│   │   │       ├── Navigation.kt         # Jetpack Compose Navigation
│   │   │       ├── theme/Theme.kt        # Material 3 Expressive theme
│   │   │       ├── components/           # Device status card, buttons, console
│   │   │       └── screens/              # Home, Flash, Sideload, Reboot, Commands
│   │   ├── res/                          # Themes, strings, USB device filter
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 📦 How to Build

1. Open this directory in **Android Studio (Ladybug or newer)**.
2. Let Gradle sync project dependencies.
3. Connect an Android device or emulator (Android 8.0+ / API 26+).
4. Run `Build -> Assemble APK` or hit **Run (Shift + F10)**.
