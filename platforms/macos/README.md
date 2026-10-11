# Bongo for macOS

Native Avro phonetic Bengali input method built specifically for Apple Silicon macOS (M1, M2, M3, M4+).

Built with Apple's `InputMethodKit`, Swift, and a pure-Rust transliteration engine.

---

## Architecture Overview

```
platforms/macos/
├── Bongo/                               # Input Method bundle
│   ├── Resources/
│   │   ├── AppIcon.icns                 # High-resolution application icon
│   │   ├── Bongo.entitlements           # App entitlements
│   │   ├── Info.plist                   # Input method service registration
│   │   └── iconTemplate.pdf             # Menu bar icon template
│   └── Sources/
│       ├── AppDelegate.swift            # Lifecycle and menu handling
│       ├── BridgeHeader.h               # C bridge for Rust engine
│       ├── CandidatePanel.swift         # Floating candidate HUD
│       ├── Engine.swift                 # Rust engine bindings
│       ├── InputController.swift        # IMKInputController implementation
│       ├── WelcomeWindow.swift          # Welcome and settings UI
│       └── main.swift                   # Input method entry point
├── Launcher/                            # Applications launcher app
│   ├── Resources/
│   └── Sources/main.swift               # Launches input method process
├── engine/                              # Rust static engine
│   ├── Cargo.toml
│   └── src/                             # Transliteration logic (libavrobangla_engine.a)
├── release/                             # Packaged DMG artifacts
└── scripts/
    └── create-dmg.sh                    # Compiles and creates distributor DMG
```

---

## System Requirements

- macOS 13.0 (Ventura), macOS 14 (Sonoma), macOS 15 (Sequoia), or newer
- Apple Silicon Mac (M1, M2, M3, M4, or newer)
- Xcode Command Line Tools (`xcode-select --install`)
- Rust toolchain (`rustup target add aarch64-apple-darwin`)

---

## Building the Release DMG

Run the automated build script:

```bash
./platforms/macos/scripts/create-dmg.sh release
```

This compiles:
1. The static Rust engine library (`libavrobangla_engine.a`) for `aarch64-apple-darwin`.
2. The native Swift Input Method bundle (`Bongo Input Method.app`).
3. The Applications launcher (`Bongo.app`).
4. Signs both bundles with ad-hoc signatures.
5. Packages a drag-and-drop DMG into `platforms/macos/release/Bongo-1.0-macos-arm64.dmg`.

---

## Installation & Activation

1. Open `Bongo-1.0-macos-arm64.dmg`.
2. Drag `Bongo.app` into `/Applications`.
3. Launch `Bongo` once from Applications to register the input method with macOS.
4. Go to **System Settings** -> **Keyboard** -> **Input Sources** -> **Edit** -> **+**.
5. Add **Bongo** under Bengali.
6. Switch anytime with the **Globe / fn** key or <kbd>Control + Space</kbd>.
