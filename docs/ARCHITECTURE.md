# Bongo Architecture

Bongo is a native, offline Bangla phonetic input method built for macOS, Linux, and Windows.

## 1. Core Rust Engine (`avrobangla_engine`)

The phonetic core is written in pure Rust with zero platform-specific dependencies:
- **Parser & Automaton**: Implements the official Avro phonetic scheme using a deterministic finite automaton.
- **Offline Lexicon**: Bundles a compressed trie with over 150,000 verified Bengali words for sub-microsecond dictionary lookups.
- **C-FFI Interface**: Exposes a unified C-compatible ABI in `engine.h` across macOS, Linux, and Windows.

## 2. macOS Platform Architecture

On macOS, Bongo follows Apple's official `InputMethodKit` architecture:
- **`Bongo Input Method.app`**: InputMethodKit service receiving WindowServer key events via `IMKServerInput`, rendering the candidate HUD (`NSPanel`), and committing text via client proxies.
- **`Bongo.app`**: Launcher and configuration app built with Swift and AppKit. It registers the input method with LaunchServices without root permissions.

## 3. Linux Platform Architecture

On Linux, Bongo integrates directly into the desktop input framework via **IBus**:
- **IBus Component (`bongo.xml`)**: Registers Bongo with the desktop session as an official Bengali input engine.
- **IBus Engine Daemon (`bongo_ibus_engine.py`)**: Interacts with the IBus D-Bus service, processes key events through the Rust core, and updates candidate views.
- **Settings Application (`bongo-settings`)**: Modern GTK4 / Libadwaita application for typography, mode configuration, and layout cheatsheet.

## 4. Windows Platform Architecture

On Windows, Bongo operates through a background service and low-level keyboard hook:
- **Keyboard Hook Daemon (`bongo_service.py`)**: Uses Win32 `SetWindowsHookExW` (`WH_KEYBOARD_LL`) to capture keystrokes, feeds them into the Rust phonetic engine, and injects converted Bengali Unicode text via Win32 `SendInput`. Supports global toggle hotkey (`F12`) and taskbar tray icon.
- **Settings Suite (`bongo_settings.py`)**: Native Tkinter-based settings and layout reference app with immersive dark mode.
- **Packaging**: Distributed as an Inno Setup standalone executable (`Bongo-Setup-1.0.0.exe`) and WiX Toolset MSI installer (`Bongo-1.0.0-x64.msi`).

## 5. Releases and Updates

Release artifacts are built and published for:
- macOS: Apple Silicon (`arm64`), Intel (`x86_64`), and Universal 2 DMGs.
- Linux: Debian / Ubuntu (`.deb`) and Fedora / RHEL (`.rpm`) packages.
- Windows: Setup (`.exe`) and Enterprise (`.msi`) installers.

Bongo checks the GitHub Releases API periodically and notifies users when updates are available.
