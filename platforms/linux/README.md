# Bongo for Linux

Native Avro phonetic Bengali input method for modern Linux desktop environments (GNOME, KDE Plasma, XFCE).

Powered by a pure-Rust transliteration engine connected through an IBus engine daemon and an integrated GTK settings application.

---

## Architecture Overview

```
platforms/linux/
├── data/
│   ├── app.getbongo.Bongo.desktop       # Desktop launcher specification
│   ├── app.getbongo.Bongo.metainfo.xml  # AppStream metadata
│   ├── bongo.xml                        # IBus component definition
│   └── icons/                           # App icons (16x16 to 512x512)
├── engine/                              # Rust transliteration engine (C ABI)
│   ├── Cargo.toml
│   └── src/lib.rs                       # Shared library entry point (libbongo_engine.so)
├── ibus/                                # IBus input method engine daemon
│   └── bongo-ibus-engine.py             # Python IBus engine implementation
├── scripts/
│   ├── install.sh                       # Local build and install script
│   ├── build-deb.sh                     # Debian / Ubuntu package builder (.deb)
│   └── build-rpm.sh                     # Fedora / RHEL package builder (.rpm)
└── ui/
    └── bongo_settings.py                # GTK settings and welcome application
```

---

## Prerequisites

### Ubuntu / Debian:
```bash
sudo apt update
sudo apt install -y cargo rustc ibus libibus-1.0-dev python3 python3-gi gir1.2-ibus-1.0 gir1.2-gtk-4.0
```

### Fedora:
```bash
sudo dnf install -y cargo rust ibus ibus-devel python3 python3-gobject gtk4
```

### Arch Linux:
```bash
sudo pacman -S rust cargo ibus python-gobject gtk4
```

---

## Installation

### Option 1: Install from Debian / Ubuntu Package (.deb)

Download `bongo_1.0.1_amd64.deb` (or `bongo_1.0.1_arm64.deb`) from the [Releases](https://github.com/mehedishakeel/Bongo/releases/latest) page:

```bash
sudo apt install ./bongo_1.0.1_amd64.deb
```

### Option 2: Build & Install from Source

```bash
# From repository root
./platforms/linux/scripts/install.sh
```

---

## Enabling Bongo

1. Open **GNOME Settings** -> **Keyboard**.
2. Under **Input Sources**, click **Add** (`+`).
3. Click the three dots (`⋮`) to expand all languages.
4. Search for **Bengali (Bongo)** and add it.
5. Switch keyboards anytime with <kbd>Super + Space</kbd> or <kbd>Control + Space</kbd>.

> **Tip for Virtual Machine users (e.g. running Linux on macOS host):**
> macOS intercepts <kbd>Cmd + Space</kbd> for Spotlight. To set the input switch shortcut directly to <kbd>Control + Space</kbd> on Ubuntu:
> ```bash
> gsettings set org.gnome.desktop.wm.keybindings switch-input-source "['<Control>space']"
> ```
> Or to set it to <kbd>Alt + Shift</kbd>:
> ```bash
> gsettings set org.gnome.desktop.input-sources xkb-options "['grp:alt_shift_toggle']"
> ```

---

## Bongo Settings Application

Run the settings app from your application launcher or terminal:

```bash
bongo-settings
```

You can also open directly to specific tabs:
```bash
bongo-settings --tab 0  # Getting Started
bongo-settings --tab 1  # Phonetic Layout Cheatsheet
bongo-settings --tab 2  # Typing Settings (3 modes, emoji toggle)
bongo-settings --tab 3  # Fonts & Typography
```

---

## Building Packages

- **Debian / Ubuntu Package**:
  ```bash
  ./platforms/linux/scripts/build-deb.sh
  ```
- **Fedora / RPM Package**:
  ```bash
  ./platforms/linux/scripts/build-rpm.sh
  ```
