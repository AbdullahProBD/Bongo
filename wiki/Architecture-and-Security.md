# Architecture & Privacy

This document explains the technical architecture, internal subsystems, and privacy design of **Bongo (বঙ্গ)**.

```
+--------------------------------------------------------------------------+
|                                User Input                                |
|        (Physical Keyboard Keystroke in Safari, VS Code, Word, etc.)      |
+--------------------------------------------------------------------------+
                                     |
                                     v
       +-----------------------------+-----------------------------+
       |                             |                             |
       v (macOS)                     v (Linux)                     v (Windows)
+-----------------------------+ +---------------------------+ +---------------------------+
| Apple InputMethodKit        | | Linux IBus Framework      | | Win32 Low-Level Hook /    |
| (IMKServerInput Protocol)   | | (IBus Component Protocol) | | SendInput Unicode API     |
+-----------------------------+ +---------------------------+ +---------------------------+
       |                             |                             |
       | Swift Bridge                | Python / C Bridge           | Python / Ctypes FFI
       +-----------------------------+-----------------------------+
                                     |
                                     v
+--------------------------------------------------------------------------+
|                  Core Rust Phonetic Engine (libavrobangla)               |
|                                                                          |
|  - Avro Phonetic Rule Parser & Finite State Automaton                    |
|  - Compressed Trie Lexicon (150,000+ Bengali Words)                      |
|  - Candidate Ranking & Autocorrect Predictor                             |
+--------------------------------------------------------------------------+
                                     |
                                     v
+--------------------------------------------------------------------------+
|                   Output: Transformed Bengali Unicode                    |
|             (Committed into text field + Candidate HUD display)          |
+--------------------------------------------------------------------------+
```

---

## 1. Core Rust Phonetic Engine (`avrobangla_engine`)

At the center of Bongo sits a high-performance, memory-safe phonetic engine written in **Rust**:

- **Phonetic Rule Parser**: Implements the full Avro phonetic grammar using a deterministic finite automaton (DFA). It converts sequence buffers of English keystrokes into correct Bengali vowel, consonant, kar, and conjunct structures.
- **Trie Lexicon Index**: Encodes over 150,000 verified Bengali words into a compressed prefix trie structure. Lookups take sub-microsecond time and require minimal RAM (~12 MB).
- **C Foreign Function Interface (FFI)**: Exposes a stable, minimal C-compatible ABI in `engine.h`:
  - `bongo_engine_new()`: Initializes an isolated engine context.
  - `bongo_engine_process_key(engine, key_code, modifiers)`: Processes a keystroke and updates the internal syllable state.
  - `bongo_engine_get_composition(engine)`: Returns current in-progress composition string.
  - `bongo_engine_get_candidates(engine, max_count)`: Retrieves sorted list of word suggestions.
  - `bongo_engine_free(engine)`: Safely deallocates engine resources.

Because the core is written in pure Rust with zero OS-specific dependencies, the exact same deterministic phonetic algorithms run identically across macOS, Linux, and Windows.

---

## 2. macOS Platform Implementation

On macOS, Bongo is architected in accordance with Apple's official input method guidelines:

### Bundle Structure
```
Bongo.app/
├── Contents/
│   ├── MacOS/
│   │   └── Bongo (Launcher & Settings App)
│   ├── Resources/
│   │   ├── AppIcon.icns
│   │   └── iconTemplate.pdf
│   └── Library/
│       └── Input Methods/
│           └── Bongo Input Method.app/
│               └── Contents/
│                   ├── MacOS/
│                   │   └── Bongo (InputMethodKit Server)
│                   └── Info.plist
```

### Components
1. **`Bongo Input Method.app`**:
   - Implements Apple's official `InputMethodKit` framework (`IMKServerInput` protocol).
   - Handles text input events dispatched directly by macOS WindowServer.
   - Manages the floating, high-contrast candidate HUD window (`NSPanel`) that positions itself seamlessly beneath the user's cursor.
2. **`Bongo.app`**:
   - The user-facing launcher and configuration application.
   - When opened, it automatically registers `Bongo Input Method.app` with `LaunchServices` and `TIS` (Text Input Source) APIs without requiring root permissions or shell scripts.
   - Hosts the Swift 5 / AppKit Settings window with live typography preview.

---

## 3. Linux Platform Implementation

On Linux, Bongo integrates directly into the standard **IBus (Intelligent Input Bus)** desktop infrastructure:

### Components
1. **IBus Component XML (`/usr/share/ibus/component/bongo.xml`)**:
   - Declares `org.freedesktop.IBus.Bongo` as a native input method engine with language code `bn`.
   - Specifies the engine daemon binary path `/usr/libexec/bongo-ibus-engine`.
2. **IBus Engine Daemon (`bongo_ibus_engine`)**:
   - Communicates with IBus via D-Bus.
   - Captures client keystrokes, feeds them into the native Rust core library, and emits candidate lists back to the desktop environment (GNOME Shell, KDE Plasma, XFCE).
3. **Settings App (`bongo-settings`)**:
   - Modern GTK4 / Libadwaita user interface.
   - Shares identical configuration keys with macOS, providing a consistent experience across operating systems.

---

## 4. Windows Platform Implementation

On Windows, Bongo operates through a native background service and low-level keyboard hook:

### Components
1. **Windows Engine DLL (`bongo_engine.dll`)**:
   - Pure-Rust dynamic library exporting the unified C-FFI ABI for phonetics and dictionary lookup.
2. **Keyboard Hook Daemon (`bongo_service.py`)**:
   - Uses Win32 `SetWindowsHookExW` (`WH_KEYBOARD_LL`) to intercept keystrokes system-wide.
   - Injects converted Unicode text into active windows using Win32 `SendInput` (`KEYEVENTF_UNICODE`).
   - Manages floating candidate bar HUD and system tray icon.
   - Supports instant hotkey toggling (`F12`).
3. **Settings & Welcome GUI (`bongo_settings.py`)**:
   - Native dark-themed configuration suite with 4 identical tabs (Getting Started, Bongo Layout, Settings, Fonts).
   - Manages startup registry keys (`HKCU\Software\Microsoft\Windows\CurrentVersion\Run`) and configuration storage (`%APPDATA%\Bongo\settings.json`).

---

## 5. Privacy & Security Audit

Bongo was created specifically to eliminate the security risks common in unofficial keyboard tools.

### Comparison Table

| Security Attribute | Generic Keylogger Tools | Bongo (বঙ্গ) |
|:---|:---:|:---:|
| **Official OS Input API** | ❌ Injects via Accessibility / CGEvent | ✅ Native `InputMethodKit` / `IBus` |
| **Accessibility Permissions** | ❌ Required (Can read every screen & key) | ✅ **Not Required** |
| **Network Access / Telemetry** | ❌ Transmits usage metrics to analytics | ✅ **0 KB Network Traffic** |
| **Root / Admin Passwords** | ❌ Required for installation scripts | ✅ **Zero Root Required on macOS** |
| **Dictionary Storage** | ❌ Remote cloud APIs | ✅ **100% Local Offline Lexicon** |
| **Source Code Audit** | ❌ Closed source or untracked binary | ✅ **100% Open Source (MPL-2.0)** |

### Security Guarantees
1. **Zero Keylogging**: Bongo only receives text input when it is actively selected as the current input source. When switched to English, Bongo is completely dormant.
2. **Sandbox Isolation**: Bongo has no entitlement or permission to inspect files outside its local application container.
3. **Reproducible Builds**: All releases on GitHub are compiled directly from open-source code using public GitHub Actions workflows with published SHA-256 checksums.
