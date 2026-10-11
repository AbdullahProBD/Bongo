# Welcome to the Bongo Wiki

<div align="center">
  <img src="https://raw.githubusercontent.com/mehedishakeel/Bongo/main/docs/assets/bongo-website-hero.png" alt="Bongo - A New Era of Bangla Avro Typing" width="100%">
  <br><br>
  <h1><strong>বঙ্গ · Bongo</strong></h1>
  <p><strong>A New Era of Bangla Avro Typing for macOS, Linux and Windows</strong></p>
  <p>
    <a href="https://github.com/mehedishakeel/Bongo/releases/latest"><img src="https://img.shields.io/badge/Release-v1.0.0-E05344?style=flat-square&logo=apple&logoColor=white" alt="Release v1.0.0"></a>
    <a href="https://github.com/mehedishakeel/Bongo/blob/main/LICENSE"><img src="https://img.shields.io/badge/License-MPL--2.0-blue?style=flat-square" alt="License MPL-2.0"></a>
    <a href="https://getbongo.app"><img src="https://img.shields.io/badge/Website-getbongo.app-2ea44f?style=flat-square" alt="Official Website"></a>
    <img src="https://img.shields.io/badge/Privacy-100%25%20On--Device-success?style=flat-square" alt="Privacy">
  </p>
</div>

---

## Overview

**Bongo (বঙ্গ)** is a native, privacy-first Bengali (Bangla) phonetic input method designed from the ground up for modern desktop operating systems. Built around the intuitive and beloved **Avro phonetic standard**, Bongo allows you to type natural Bangla phonetically using ordinary English characters (for example, typing `ami` yields `আমি`, and `dhonnobad` yields `ধন্যবাদ`).

Unlike legacy tools ported from obsolete codebases, Bongo is powered by a high-performance **Rust phonetic core** and integrates directly into the official operating system input frameworks:
- **macOS**: Native `InputMethodKit` (`IMKServerInput`) and native Swift interface.
- **Linux**: Standard `IBus` input engine with a modern GTK4 / Libadwaita settings application.
- **Windows**: Low-level keyboard hook background service with native Settings application and Setup / MSI packages.

---

## Visual Showcase

<div align="center">
  <table>
    <tr>
      <td width="50%" align="center">
        <img src="https://raw.githubusercontent.com/mehedishakeel/Bongo/main/docs/assets/bongo-getting-started.png" alt="Getting Started Window" width="100%">
        <br><strong>Frictionless Onboarding (macOS)</strong>
      </td>
      <td width="50%" align="center">
        <img src="https://raw.githubusercontent.com/mehedishakeel/Bongo/main/docs/assets/bongo-settings.png" alt="Settings Window" width="100%">
        <br><strong>3 Typing Modes & Auto-Correct</strong>
      </td>
    </tr>
    <tr>
      <td width="50%" align="center">
        <img src="https://raw.githubusercontent.com/mehedishakeel/Bongo/main/docs/assets/bongo-fonts.png" alt="Font Preview Window" width="100%">
        <br><strong>Custom Typography & Live Preview</strong>
      </td>
      <td width="50%" align="center">
        <img src="https://raw.githubusercontent.com/mehedishakeel/Bongo/main/docs/assets/bongo-linux-settings.png" alt="Linux GTK4 Settings" width="100%">
        <br><strong>Native GTK4 Settings (Linux)</strong>
      </td>
    </tr>
    <tr>
      <td colspan="2" align="center">
        <img src="https://raw.githubusercontent.com/mehedishakeel/Bongo/main/docs/assets/bongo-windows-settings.png" alt="Windows Settings Window" width="90%">
        <br><strong>Native Windows Settings, Layout Cheatsheet & Typing Playground</strong>
      </td>
    </tr>
  </table>
  <br>
  <img src="https://raw.githubusercontent.com/mehedishakeel/Bongo/main/docs/assets/bongo-hud-candidate.png" alt="Candidate HUD" width="380">
  <br>
  <strong>Tactile Translucent Candidate HUD Window</strong>
</div>

---

## Core Pillars & Key Highlights

### 1. Pure Native Speed & Responsiveness
Bongo compiles directly into native CPU machine code for both ARM64 and x86_64 architectures:
- **Apple Silicon Native**: Built specifically for M1, M2, M3, M4, M5 Macs.
- **Intel Mac Native**: Core i5, i7, i9, and Xeon Macs.
- **Linux PC Native**: 64-bit AMD/Intel (`amd64`) and ARM64 single-board / laptop computers.
- **Microsecond Latency**: Pure Rust dictionary lookups and phonetic rule evaluation with zero perceptible typing lag.

### 2. 100% On-Device Privacy
- **Zero Network Telemetry**: Transmits 0 KB of network traffic.
- **No Accessibility Keylogging**: Built on official input method frameworks (`InputMethodKit` on macOS, `IBus` on Linux). It does not require intrusive Accessibility permissions.
- **Offline Lexicon**: Bundles an offline 150,000+ word Bengali dictionary and trie index directly in the app bundle.

### 3. Apple & Linux Native Desktop Aesthetics
- **Seamless System Integration**: Respects macOS dark/light appearance and Linux desktop themes automatically.
- **Translucent Floating Candidate Bar**: Displays smart numbered candidates `[1]`, `[2]`, `[3]` next to your cursor with smooth keyboard selection.
- **Custom Fonts**: Choose any installed Bengali font (Noto Sans Bengali, Kalpurush, SolaimanLipi, Ekush, Kohinoor Bangla, etc.) with real-time size adjustment.

---

## Quick Navigation

Explore the comprehensive wiki pages to learn everything about Bongo:

| Wiki Page | Description |
|:---|:---|
| **[[Installation Guide\|Installation-Guide]]** | Complete setup instructions for macOS (DMG / Homebrew), Linux (.deb / .rpm / source), and Windows (Setup EXE / MSI). |
| **[[Phonetic Key Map\|Phonetic-Key-Map]]** | Full phonetic reference for vowels, consonants, conjuncts (যুক্তবর্ণ), numbers, and symbols. |
| **[[Configuration & Settings\|Configuration-and-Settings]]** | Explanation of typing modes, candidate settings, typography, and keyboard hotkeys. |
| **[[Architecture & Privacy\|Architecture-and-Security]]** | Technical deep dive into the Rust engine, InputMethodKit, IBus, and privacy architecture. |
| **[[Troubleshooting & FAQ\|Troubleshooting-and-FAQ]]** | Solutions for macOS Gatekeeper, shortcut switching, Linux IBus restarts, and Windows configuration. |
| **[[Development & Building\|Development-and-Building]]** | Step-by-step instructions to compile Bongo from source code on macOS, Linux, and Windows. |

---

## Quick Install Summary

### macOS (Apple Silicon & Intel)
1. Download the latest `Bongo-1.0.0-macos-universal.dmg` (or dedicated `arm64` / `intel-x86_64` DMGs) from [Releases](https://github.com/mehedishakeel/Bongo/releases/latest).
2. Open the DMG and drag `Bongo.app` into **Applications**.
3. Launch `Bongo` once from Applications to register the input method with macOS.
4. Go to **System Settings → Keyboard → Input Sources**, click **+**, select **Bengali**, and add **Bongo**.
5. Switch anytime using the **Globe / fn** key or **Control + Space**!

### Linux (Ubuntu / Debian / Linux Mint)
Download the `.deb` package and install via terminal:
```bash
sudo apt update
sudo apt install ./bongo_1.0.0_amd64.deb
```
Then add **Bengali (Bongo)** under **Settings → Keyboard → Input Sources**, and switch with **Super + Space**!

### Windows (Windows 10 & 11)
1. Download `Bongo-Setup-1.0.0.exe` (or `Bongo-1.0.0-x64.msi` for enterprise) from [Releases](https://github.com/mehedishakeel/Bongo/releases/latest).
2. Run the installer and launch Bongo.
3. Press **F12** anywhere across Windows to toggle instantly between Bangla and English mode.

For detailed instructions, see the **[[Installation Guide\|Installation-Guide]]**.
