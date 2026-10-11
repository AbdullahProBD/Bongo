# Building Bongo from Source

This guide covers building release packages for macOS, Linux, and Windows.

Clone the repository:
```sh
git clone https://github.com/mehedishakeel/Bongo.git
cd Bongo
```

---

## 1. macOS (Apple Silicon & Intel)

### Requirements
- macOS 13 (Ventura) or newer
- Xcode Command Line Tools: `xcode-select --install`
- Rust stable: `rustup default stable`
- Rust targets:
  ```sh
  rustup target add aarch64-apple-darwin
  rustup target add x86_64-apple-darwin
  ```

### Build Commands
```sh
# Build Universal 2 DMG (Apple Silicon + Intel):
./platforms/macos/scripts/create-dmg.sh universal

# Or build Apple Silicon only:
./platforms/macos/scripts/create-dmg.sh arm64

# Or build Intel Mac only:
./platforms/macos/scripts/create-dmg.sh x86_64
```
Artifacts are saved into `platforms/macos/release/`.

---

## 2. Linux (Ubuntu, Debian, Fedora, Arch)

### Requirements
- Linux development headers:
  ```bash
  sudo apt install -y build-essential curl python3 libibus-1.0-dev libgtk-4-dev libadwaita-1-dev dpkg-dev
  ```
- Rust stable: `curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh`

### Build Commands
```bash
# Compile core Rust engine:
(cd platforms/linux/engine && cargo build --release)

# Build Debian / Ubuntu .deb package:
./platforms/linux/scripts/build-deb.sh

# Build Fedora / RHEL .rpm package:
./platforms/linux/scripts/build-rpm.sh
```
Artifacts are saved into `platforms/linux/release/`.

---

## 3. Windows (Windows 10 & 11)

### Requirements
- Windows 10 (1903+) or Windows 11 (64-bit)
- Python 3.10+ with `pyinstaller` and `requests`: `pip install pyinstaller requests`
- Inno Setup 6 (installed to `C:\Program Files (x86)\Inno Setup 6\ISCC.exe`)
- WiX Toolset v4/v5/v7 (available in PATH via `wix`)
- Rust toolchain (optional, for compiling `bongo_engine.dll` from source)

### Build Commands
```cmd
:: Build both Setup (.exe) and MSI packages:
platforms\windows\scripts\build-windows.bat

:: Or build via PowerShell:
powershell -ExecutionPolicy Bypass -File platforms\windows\scripts\build-windows.ps1
```
Artifacts are saved into `platforms/windows/release/`:
- `Bongo-Setup-1.0.0.exe`
- `Bongo-1.0.0-x64.msi`
- Accompanying `.sha256` checksums

---

## 4. Automated Verification Suite

Run the distribution integrity test suite from the repository root:
```sh
python3 -m unittest discover -s tests -v
```
All 6 platform verification tests must pass.
