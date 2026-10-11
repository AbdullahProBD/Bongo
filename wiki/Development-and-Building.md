# Development & Building from Source

This guide covers everything required to set up a development environment, compile the core Rust engine, build macOS, Linux, and Windows bundles, and run the test suite.

---

## Repository Structure

```
Bongo/
├── platforms/
│   ├── macos/
│   │   ├── engine/           # Rust phonetic core and C headers for macOS
│   │   ├── Bongo/            # Swift InputMethodKit IME server & Settings
│   │   ├── Launcher/         # Swift launcher and registrar application
│   │   ├── scripts/          # Packaging scripts (create-dmg.sh)
│   │   └── tests/            # Native integration tests
│   ├── linux/
│   │   ├── engine/           # Rust phonetic core and C headers for Linux
│   │   ├── ibus/             # IBus engine daemon & component XML
│   │   ├── ui/               # GTK4 / Libadwaita settings application
│   │   ├── data/             # Desktop entry, Metainfo XML, and icons
│   │   └── scripts/          # Packaging scripts (build-deb.sh, build-rpm.sh)
│   └── windows/
│       ├── engine/           # Rust phonetic core DLL (bongo_engine.dll)
│       ├── service/          # Win32 background service & hook (bongo_service.py)
│       ├── ui/               # Tkinter dark settings app (bongo_settings.py)
│       ├── installer/        # Inno Setup (.iss) and WiX (.wxs) scripts
│       └── scripts/          # Packaging scripts (build-windows.bat, build-windows.ps1)
├── docs/                     # Bilingual landing page & web app
├── tests/                    # Cross-platform distribution verification suite
└── wiki/                     # Complete documentation wiki
```

---

## Prerequisites

### Common Tools
- **Git**: `git --version` (2.30+)
- **Rust & Cargo**: Latest stable Rust toolchain (`rustup default stable`)
- **Python 3**: For test execution and packaging

### macOS Build Tools
- **macOS 13+** (Ventura, Sonoma, Sequoia)
- **Xcode Command Line Tools**: `xcode-select --install`
- **Rust Targets**:
  ```bash
  rustup target add aarch64-apple-darwin
  rustup target add x86_64-apple-darwin
  ```

### Linux Build Tools (Ubuntu / Debian)
- Essential build packages:
  ```bash
  sudo apt update
  sudo apt install -y build-essential curl python3 libibus-1.0-dev libgtk-4-dev libadwaita-1-dev dpkg-dev
  ```

### Windows Build Tools (Windows 10 / 11)
- **Python 3.10+**: with `pyinstaller` and `requests`
- **Inno Setup 6**: Command-line compiler `ISCC.exe`
- **WiX Toolset**: WiX v4/v5/v7 CLI `wix.exe`

---

## Building on macOS

### 1. Build the Rust Engine Manually
```bash
cd platforms/macos/engine
cargo build --release --target aarch64-apple-darwin
```
This produces the static library `target/aarch64-apple-darwin/release/libavrobangla_engine.a`.

### 2. Build Release DMG Packages
To build and assemble full `.dmg` release bundles, use the automated script:

```bash
# Build Universal 2 DMG (Apple Silicon + Intel):
./platforms/macos/scripts/create-dmg.sh universal

# Or build Apple Silicon only:
./platforms/macos/scripts/create-dmg.sh arm64

# Or build Intel Mac only:
./platforms/macos/scripts/create-dmg.sh x86_64

# Or build all three variants in one pass:
./platforms/macos/scripts/create-dmg.sh all
```

Output installers are saved into the `platforms/macos/release/` directory:
- `Bongo-1.0.0-macos-universal.dmg`
- `Bongo-1.0.0-macos-arm64.dmg`
- `Bongo-1.0.0-macos-intel-x86_64.dmg`
- Plus accompanying `.sha256` checksum files.

---

## Building on Linux

### 1. Build the Linux Engine
```bash
cd platforms/linux/engine
cargo build --release
```

### 2. Build `.deb` Package (Debian / Ubuntu / Mint)
```bash
./platforms/linux/scripts/build-deb.sh
```
Produces:
`platforms/linux/release/bongo_1.0.0_amd64.deb` (or `arm64.deb`).

### 3. Build `.rpm` Package (Fedora / RHEL)
```bash
./platforms/linux/scripts/build-rpm.sh
```
Produces:
`platforms/linux/release/bongo-1.0.0-1.x86_64.rpm` (or `aarch64.rpm`).

---

## Building on Windows

### 1. Automated Build Script
From the repository root on Windows:
```cmd
platforms\windows\scripts\build-windows.bat
```
Or in PowerShell:
```powershell
powershell -ExecutionPolicy Bypass -File platforms\windows\scripts\build-windows.ps1
```

The script performs:
1. Compiles `bongo.exe` (daemon) and `bongo-settings.exe` (GUI) via PyInstaller.
2. Compiles `Bongo-Setup-1.0.0.exe` via Inno Setup.
3. Compiles `Bongo-1.0.0-x64.msi` via WiX Toolset.
4. Generates SHA256 checksums into `platforms/windows/release/`.

---

## Running the Automated Test Suite

### Distribution & Integrity Tests
Run the cross-platform platform integrity and registration test suite:
```bash
python3 -m unittest discover -s tests -v
```

All 6 tests must pass:
- `test_macos_registration`: Validates InputMethodKit bundle IDs, Info.plist entries, and classes.
- `test_linux_registration`: Validates IBus component XML, desktop files, and AppStream metadata.
- `test_windows_registration`: Validates Windows installer manifests, registry entries, and icon bindings.
- `test_platform_architecture_integrity`: Ensures proper multi-arch targets exist and legacy files are absent.
- `test_release_artifact_layout`: Validates DMG packaging structure and link targets.
- `test_updates_use_bongo_release_configuration`: Validates update check endpoints.

### Script Syntax Validation
```bash
bash -n platforms/macos/scripts/create-dmg.sh
bash -n platforms/linux/scripts/install.sh
bash -n platforms/linux/scripts/build-deb.sh
bash -n platforms/linux/scripts/build-rpm.sh
python3 -m py_compile platforms/linux/ui/bongo_settings.py platforms/linux/ibus/bongo_ibus_engine.py
python3 -m py_compile platforms/windows/ui/bongo_settings.py platforms/windows/service/bongo_service.py
```

---

## Contributing & License

We welcome pull requests for bug fixes, new phonetic vocabulary corrections, and enhancements!
- Ensure all automated unit tests pass before submitting a pull request.
- Keep the codebase 100% offline with zero external network telemetry.
- Bongo is licensed under the **Mozilla Public License 2.0 (MPL-2.0)**.
