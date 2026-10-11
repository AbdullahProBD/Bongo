# Installation Guide

This guide provides step-by-step instructions for installing and enabling **Bongo (বঙ্গ)** on macOS and Linux.

---

## Supported Operating Systems & Hardware

| Platform | Minimum Version | Supported Architectures | Installer Formats |
|:---|:---|:---|:---|
| **macOS** | macOS 13 (Ventura), 14 (Sonoma), 15 (Sequoia)+ | Apple Silicon (`arm64`: M1, M2, M3, M4, M5)<br>Intel Core (`x86_64`: Core i5, i7, i9, Xeon) | `.dmg` (Universal 2, arm64, x86_64) |
| **Linux** | Ubuntu 22.04 / 24.04+, Debian 12+, Linux Mint, Fedora 39+, Arch | 64-bit PC (`x86_64` / `amd64`)<br>ARM64 (`aarch64` / `arm64`) | `.deb`, `.rpm`, Terminal Installer |
| **Windows** | Windows 10 (1903+) / Windows 11 | 64-bit PC (`x86_64` / `amd64`)<br>ARM64 (`aarch64`) | `.exe` (Setup), `.msi` (Enterprise) |

---

## macOS Installation

### Method 1: Graphical DMG Installer (Recommended)

1. **Download the DMG**:
   Go to the [Bongo Releases Page](https://github.com/mehedishakeel/Bongo/releases/latest) and download the file for your Mac:
   - `Bongo-1.0.0-macos-universal.dmg` (Works on all Macs: Apple Silicon & Intel)
   - `Bongo-1.0.0-macos-arm64.dmg` (Optimized for Apple Silicon M-series)
   - `Bongo-1.0.0-macos-intel-x86_64.dmg` (Optimized for Intel Core Macs)

2. **Drag and Drop**:
   Double-click the downloaded `.dmg` file. A window will open. Drag **Bongo.app** into the **Applications** folder shortcut.

3. **First Launch & Service Registration**:
   - Open your **Applications** folder in Finder and double-click **Bongo**.
   - If prompted by macOS Gatekeeper (*"Apple cannot verify that 'Bongo' is free of malware"* because Bongo is open-source and not signed with an enterprise Apple Developer ID):
     1. Open **System Settings → Privacy & Security**.
     2. Scroll down to the **Security** section.
     3. Click **Open Anyway** next to Bongo, and enter your user password.
   - Bongo's launcher will start and automatically register the underlying input method service (`Bongo Input Method.app`) with macOS.

4. **Add Bongo to Keyboard Input Sources**:
   - Open **System Settings → Keyboard**.
   - Under the **Text Input** section, click **Edit...** next to **Input Sources**.
   - Click the **+** (plus) button at the bottom-left of the dialog.
   - In the search bar on the left, type `Bengali` (or scroll to it).
   - In the list of available Bengali layouts on the right, select **Bongo** and click **Add**.
   - Click **Done**.

5. **Start Typing in Bangla**:
   - Click the input source menu in your macOS menu bar (top-right of your screen) and select **Bongo**.
   - Alternatively, press the **Globe key** (`fn` / Globe) or press **Control + Space** to toggle between English and Bongo without lifting your hands.

---

### Method 2: One-Line Terminal Command (macOS)

If you prefer installing via the terminal, run the following automated command:

```bash
curl -fsSL https://getbongo.app/install.sh | bash
```

This script automatically detects your Mac processor architecture, downloads the matching release DMG, installs `Bongo.app` into `/Applications`, and registers the input method service.

---

## Linux Installation

### Method 1: Debian / Ubuntu / Linux Mint (.deb)

1. **Download the `.deb` package**:
   Download the package matching your processor architecture from [Releases](https://github.com/mehedishakeel/Bongo/releases/latest):
   - Standard PC (Intel / AMD 64-bit): `bongo_1.0.0_amd64.deb`
   - ARM64 devices: `bongo_1.0.0_arm64.deb`

2. **Install using APT**:
   Open a terminal in the folder where you downloaded the file and run:
   ```bash
   sudo apt update
   sudo apt install ./bongo_1.0.0_amd64.deb
   ```
   *(Replace `amd64` with `arm64` if using an ARM device).*

---

### Method 2: Fedora / RHEL (.rpm)

1. **Download the `.rpm` package**:
   Download `bongo-1.0.0-1.x86_64.rpm` (or `aarch64.rpm`) from [Releases](https://github.com/mehedishakeel/Bongo/releases/latest).

2. **Install using DNF**:
   ```bash
   sudo dnf install ./bongo-1.0.0-1.x86_64.rpm
   ```

---

### Method 3: One-Line Terminal Installer (Linux)

You can install Bongo on any supported Linux distribution with a single command:

```bash
curl -fsSL https://getbongo.app/install-linux.sh | bash
```

---

### Enabling Bongo on Linux (IBus Setup)

1. **Restart IBus**:
   In your terminal, run:
   ```bash
   ibus restart
   ```
   *(Or log out and log back in to your desktop session).*

2. **Add Bengali (Bongo) in Desktop Settings**:
   - **GNOME Desktop (Ubuntu / Fedora / Debian)**:
     - Open **Settings → Keyboard**.
     - Under **Input Sources**, click **+** (Add Input Source).
     - Click the three dots icon (`⋮`) or search for `Bengali`.
     - Select **Bengali (Bongo)** and click **Add**.
   - **KDE Plasma / XFCE**:
     - Open **IBus Preferences** (run `ibus-setup` or search "IBus" in your application menu).
     - Go to the **Input Method** tab.
     - Click **Add → Bengali → Bongo**.

3. **Switching & Typing**:
   - Press **Super + Space** (Windows key + Space) or **Ctrl + Space** to toggle between English and Bongo.
   - Launch `bongo-settings` from your application launcher or terminal anytime to configure fonts and typing options.

---

## Windows Installation

### Method 1: Graphical Setup Installer (Recommended)
1. **Download Setup Executable**:
   Download `Bongo-Setup-1.0.0.exe` from the [Bongo Releases Page](https://github.com/mehedishakeel/Bongo/releases/latest).
2. **Run Installer**:
   Double-click the installer and follow the setup wizard.
   Select optional desktop shortcuts and Windows startup preferences.
3. **Start Typing in Bangla**:
   Bongo launches in the taskbar notification area.
   Press **F12** anywhere across Windows (Word, Notepad, Chrome, VS Code) to toggle instantly between Bangla and English mode.

### Method 2: Enterprise MSI Deployment
1. Download `Bongo-1.0.0-x64.msi`.
2. Deploy via Active Directory / InTune or install silently via command line:
   ```cmd
   msiexec /i Bongo-1.0.0-x64.msi /qn /norestart
   ```

---

## How to Uninstall

### Uninstall on macOS
To cleanly remove Bongo from your Mac:
1. Open **System Settings → Keyboard → Input Sources → Edit...**.
2. Select **Bongo** and click the **-** (minus) button to remove it.
3. Open Finder, go to `/Applications`, and drag `Bongo.app` to Trash.
4. Optional: Remove preference files:
   ```bash
   rm -rf ~/Library/Preferences/org.bongo.*
   ```

### Uninstall on Linux
- **Debian / Ubuntu**:
  ```bash
  sudo apt remove bongo
  ibus restart
  ```
- **Fedora**:
  ```bash
  sudo dnf remove bongo
  ibus restart
  ```

### Uninstall on Windows
1. Open **Windows Settings → Apps → Installed apps**.
2. Locate **Bongo** in the list, click the three dots, and select **Uninstall**.
3. Follow the uninstallation wizard to remove all files and startup registry entries.
