# Troubleshooting & FAQ

Frequently asked questions and troubleshooting solutions for **Bongo (বঙ্গ)** on macOS and Linux.

---

## macOS Questions & Troubleshooting

### 1. macOS says: *"Apple cannot verify that 'Bongo' is free of malware"*
**Why this happens:**
Bongo is a community-driven, 100% free and open-source project. Apple requires open-source developers to pay a recurring annual enterprise developer subscription to notarize binaries. Without a paid notarization profile, macOS Gatekeeper shows this caution banner for any downloaded application.

**How to open Bongo safely:**
1. Open **System Settings → Privacy & Security**.
2. Scroll down to the **Security** header.
3. You will see a notice saying: *"Bongo was blocked from use because it is not from an identified developer."*
4. Click **Open Anyway** and enter your administrator password or Touch ID.
5. In the confirmation dialog, click **Open**.

Alternatively, you can remove the quarantine flag directly in Terminal:
```bash
xattr -cr /Applications/Bongo.app
```

---

### 2. Why did macOS ask for Desktop / Documents / Downloads folder access?
**Why this happens:**
On modern macOS versions (macOS 13+), if an application is launched directly from the `~/Downloads` folder (or if a newly installed app is launched while still tagged with the Safari quarantine attribute), macOS Privacy & Security (TCC) proactively displays a prompt asking whether to grant folder access.

**The Fix:**
- Ensure `Bongo.app` is placed in `/Applications`, not run from within Downloads or inside the mounted DMG disk image.
- Bongo does **not** need access to your personal documents, photos, or downloads. You can safely click **Don't Allow** on that dialog; Bongo functions completely normally because input method services operate entirely in memory.
- To strip quarantine flags from the installed app, run:
  ```bash
  xattr -cr /Applications/Bongo.app
  ```

---

### 3. Bongo does not show up in *System Settings → Keyboard → Input Sources*
If you search for `Bengali` and Bongo does not appear in the list:
1. Make sure you opened **Bongo.app** from `/Applications` at least once. The launcher registers the input method service with macOS on its first run.
2. If it still does not appear, manually register the input method with LaunchServices:
   ```bash
   /System/Library/Frameworks/CoreServices.framework/Frameworks/LaunchServices.framework/Support/lsregister -f "/Applications/Bongo.app/Contents/Library/Input Methods/Bongo Input Method.app"
   ```
3. Log out of your macOS user account and log back in (or restart your Mac).
4. Re-open **System Settings → Keyboard → Input Sources → Edit... → +** and search for **Bengali**. Bongo will appear under the Bengali section.

---

### 4. How do I switch between English and Bongo without opening settings?
You can toggle between English and Bongo using standard macOS shortcuts:
- **Globe Key (`fn`)**: On modern Mac keyboards, tap the **Globe / fn** key once to cycle input sources. (Configure this in **System Settings → Keyboard → Press fn key to: Change Input Source**).
- **Control + Space**: The universal macOS shortcut to switch to the previous input source.
- **Top Menu Bar**: Click the language switcher icon in the top-right menu bar.

*Note: If `Command + Space` opens Spotlight Search instead, use `Control + Space` or reassign shortcuts in **System Settings → Keyboard → Keyboard Shortcuts → Input Sources**.*

---

## Linux Questions & Troubleshooting

### 1. Bongo does not appear in *Settings → Keyboard → Input Sources* on Ubuntu / Debian
If `Bengali (Bongo)` does not appear in the input source selection menu:
1. Restart the IBus daemon in your terminal:
   ```bash
   ibus restart
   ```
2. Verify that the component definition exists:
   ```bash
   ls -l /usr/share/ibus/component/bongo.xml
   ```
3. If using GNOME, log out of your desktop session and log back in.
4. Open **Settings → Keyboard → Input Sources → +**, search for `Bengali`, and select **Bengali (Bongo)**.

---

### 2. Bengali characters appear as blank squares (tofu) or broken shapes
This happens when your Linux installation lacks proper Bengali Unicode fonts:
1. Install standard Bengali fonts on Ubuntu / Debian:
   ```bash
   sudo apt install fonts-noto-core fonts-beng fonts-lohit-beng-bengali
   ```
2. Or on Fedora:
   ```bash
   sudo dnf install google-noto-sans-bengali-fonts lohit-bengali-fonts
   ```
3. Open `bongo-settings`, go to the **Fonts** tab, and select **Noto Sans Bengali** or **Kalpurush**.

---

### 3. Does Bongo work on Wayland as well as X11?
Yes. Bongo interfaces with the standard desktop input framework via **IBus**. It is fully compatible with both **Wayland** (the default on modern Ubuntu, Fedora, and Debian) and classic **X11** sessions.

---

### 4. How do I quickly switch between English and Bongo on Linux?
Press **Super + Space** (Windows Key + Space) or **Ctrl + Space** to alternate between your configured input sources.

---

## Windows Questions & Troubleshooting

### 1. Windows SmartScreen says: *"Windows protected your PC"*
**Why this happens:**
Windows SmartScreen displays this banner for open-source applications that have not established a paid Microsoft EV code-signing reputation.

**How to run Bongo safely:**
1. On the SmartScreen prompt, click **More info**.
2. Click the **Run anyway** button that appears.
3. The installer runs normally.

---

### 2. How do I switch between English and Bangla on Windows?
Press **F12** on your keyboard anywhere across Windows (Word, Notepad, Chrome, VS Code, Slack). Bongo immediately toggles between English and Bangla phonetic mode.

---

### 3. Where is Bongo running?
Bongo runs in the background and places an icon in your taskbar notification area (system tray):
- Right-click the **Bongo** tray icon to open **Settings**, view the **Layout Cheatsheet**, or choose **Exit**.
- You can also launch **Bongo Settings** from your Start Menu at any time.

---

### 4. How do I install better Bengali fonts on Windows?
Windows includes *Nirmala UI* and *Vrinda* by default. For the finest rendering of complex Bengali conjuncts:
1. Download **Noto Sans Bengali** from Google Fonts or **Kalpurush** from OmicronLab.
2. Right-click the downloaded TTF font and select **Install for all users**.
3. Open **Bongo Settings**, navigate to the **Fonts** tab, and choose your preferred typeface.
