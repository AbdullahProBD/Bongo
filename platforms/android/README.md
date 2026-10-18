# Bongo for Android (IME)

High-performance Bengali (Avro Phonetic) Input Method for Android, powered by a native Rust engine with JNI bindings and a resilient pure-Kotlin fallback engine.

---

## 🌟 Key Features

- **Blazing Fast Avro Phonetic Transliteration**:
  - Type phonetically in English (e.g., `ami banglay gan gai`) to produce beautiful Bengali script (`আমি বাংলায় গান গাই`).
  - Uses the same core engine as macOS, Linux, and Windows (`riti`).
- **Dual-Engine Architecture**:
  - **Native Engine (JNI / Rust)**: Zero-overhead C ABI / JNI bridge to the high-performance Rust core.
  - **Kotlin Fallback Engine**: Pure-Kotlin Avro engine ensuring 100% functionality even on architectures where native libraries aren't pre-bundled.
- **Modern Soft Keyboard**:
  - Full QWERTY soft keyboard with fast toggle between Bangla and English.
  - Symbol & numeral layer with Bengali punctuation (।, etc.).
  - Real-time candidate strip for choosing phonetic suggestions.
  - Haptic feedback support.
- **Hardware Keyboard Support**:
  - Seamless physical keyboard support for tablets, Chromebooks, DeX, and Android VMs.
  - `Control + Space` shortcut to toggle between Bangla and English.

---

## 📁 Architecture & Directory Layout

```
platforms/android/
├── app/
│   ├── build.gradle.kts           # Android App Module configuration
│   ├── proguard-rules.pro         # Proguard/R8 rules preserving JNI symbols
│   └── src/main/
│       ├── AndroidManifest.xml    # Declares BongoInputMethodService & SettingsActivity
│       ├── java/app/getbongo/ime/
│       │   ├── BongoInputMethodService.kt # Main InputMethodService implementation
│       │   ├── engine/
│       │   │   ├── BongoEngine.kt         # Unified engine orchestrator
│       │   │   ├── BongoNative.kt         # JNI bridge (libbongo_engine.so)
│       │   │   └── AvroPhoneticEngine.kt  # Pure-Kotlin fallback transliterator
│       │   ├── model/
│       │   │   └── UserPreferences.kt     # Shared preferences management
│       │   └── ui/
│       │       ├── BongoKeyboardView.kt   # Soft keyboard view
│       │       ├── CandidateStripView.kt  # Horizontal candidate suggestions strip
│       │       └── SettingsActivity.kt    # Setup guide and test typing interface
│       └── res/
│           ├── mipmap-*/ic_launcher.png   # Bongo app launcher icons
│           ├── values/                    # strings.xml, colors.xml, themes.xml
│           └── xml/method.xml             # Android IME Subtype declaration (bn_BD, en_US)
├── engine/
│   ├── Cargo.toml                 # Cargo manifest for cdylib libbongo_engine.so
│   ├── .cargo/config.toml         # Offline vendored dependency configuration
│   └── src/lib.rs                 # JNI & C-ABI bindings
├── build.gradle.kts               # Root Gradle build script
├── settings.gradle.kts            # Project settings
└── gradlew                        # Gradle wrapper
```

---

## 🛠️ Building & Packaging

### Prerequisites

- **Java Development Kit (JDK)**: OpenJDK 17 or later.
- **Android SDK**: Build tools 34.0.0+, Android API 34.
- **Android NDK**: (Optional, for compiling native `.so` binaries).
- **Rust Toolchain**: `cargo` 1.75+ (for native library build).

### Step 1: (Optional) Compile Rust Native Engine for Android Targets

To build `libbongo_engine.so` for Android ABIs using `cargo-ndk`:

```bash
cd platforms/android/engine

# Example for ARM64 (Modern Android phones)
cargo ndk -t arm64-v8a -o ../app/src/main/jniLibs build --release

# Example for x86_64 (Android Studio Emulator)
cargo ndk -t x86_64 -o ../app/src/main/jniLibs build --release
```

*Note: If no `.so` is present in `jniLibs`, the app will automatically run on the built-in pure-Kotlin Avro engine.*

### Step 2: Build the Android APK

From `platforms/android/`:

```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease
```

The resulting APK will be generated at:
`platforms/android/app/build/outputs/apk/debug/app-debug.apk`

---

## 📲 Installation & Activation

1. **Install via ADB**:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
2. **Launch Bongo App**:
   Open the "Bongo" app from the home screen / app drawer.
3. **Follow the Setup Wizard**:
   - Tap **"1. Enable Bongo in Settings"** and toggle Bongo On.
   - Tap **"2. Select Bongo Keyboard"** and choose Bongo as the default input method.
   - Test typing directly in the test field!
