# Configuration & Settings

Bongo provides a unified, graphical Settings and Customization suite on macOS, Linux, and Windows.

<div align="center">
  <img src="https://raw.githubusercontent.com/mehedishakeel/Bongo/main/docs/assets/bongo-settings.png" alt="Bongo Settings" width="600">
</div>

---

## How to Open Settings

### On macOS
- Click the **Bongo** icon in your top menu bar and choose **Settings...**.
- Alternatively, open **Bongo** directly from your **Applications** folder.

### On Linux
- Open your desktop application menu and search for **Bongo** or **Bongo Settings**.
- Or run `bongo-settings` in any terminal window.

### On Windows
- Right-click the **Bongo** tray icon in the taskbar notification area and select **Open Settings...**.
- Or launch **Bongo Settings** directly from your Start Menu.

<div align="center">
  <img src="https://raw.githubusercontent.com/mehedishakeel/Bongo/main/docs/assets/bongo-windows-settings.png" alt="Bongo Windows Settings Window" width="600">
</div>

---

## Settings Tabs Overview

The application features 4 dedicated tabs:

### 1. Getting Started
- Quick onboarding walkthrough.
- One-click shortcut to system Keyboard settings.
- Interactive live test box to verify your typing setup without opening another app.

### 2. Bongo Layout
- Interactive visual reference of Avro phonetic keystrokes.
- Allows quick lookup of vowels, consonants, and complex conjuncts while you write.

### 3. Settings (Typing Behavior)

<div align="center">
  <img src="https://raw.githubusercontent.com/mehedishakeel/Bongo/main/docs/assets/bongo-settings.png" alt="Settings Tab" width="550">
</div>

#### Typing Modes
Bongo offers three tailored typing modes:

1. **Phonetic First (Recommended)**:
   - Primary focus is intuitive phonetic translation matching what you type.
   - If an exact dictionary match exists, it presents it as the primary choice while displaying alternative phonetic candidates in the HUD window.
   - Ideal for all everyday writing, chatting, and professional typing.

2. **Smart Suggestions**:
   - Uses frequency-weighted lexicon statistics to propose the most contextually relevant words.
   - Offers autocorrect and predictive completion for common phrases.

3. **Phonetic Only**:
   - Strictly converts keystrokes into characters without consulting the dictionary for vocabulary substitutions.
   - Useful for specialized terms, transliterations of foreign names, or scientific notation.

#### Additional Options
- **Show emoji in suggestions**: When enabled, common keywords (like `love`, `smile`, `heart`) display matching emojis alongside Bengali suggestions in the candidate list.
- **Check for updates**: Periodically checks the official GitHub repository for new releases and updates.

---

### 4. Fonts & Typography

<div align="center">
  <img src="https://raw.githubusercontent.com/mehedishakeel/Bongo/main/docs/assets/bongo-fonts.png" alt="Fonts Tab" width="550">
</div>

Bongo gives you complete control over Bengali typography across both the settings interface and the floating candidate window:

- **Font Family Selection**: Choose any Bengali font installed on your system:
  - *macOS Default*: Kohinoor Bangla, Bengali Sangam MN.
  - *Open Fonts*: Noto Sans Bengali, Noto Serif Bengali, Kalpurush, SolaimanLipi, Ekush, July, Shonar Bangla.
- **Font Sizing Slider**: Adjust the candidate text size smoothly (from 14pt to 32pt).
- **Real-Time Live Preview**: The sample text box updates immediately as you adjust font choices, allowing you to fine-tune readability for high-DPI (Retina) screens.

---

## The Floating Candidate HUD

While typing in any application (such as Safari, Chrome, Notes, Slack, Pages, or Terminal), Bongo displays a floating, translucent candidate window:

<div align="center">
  <img src="https://raw.githubusercontent.com/mehedishakeel/Bongo/main/docs/assets/bongo-hud-candidate.png" alt="Floating Candidate HUD" width="340">
</div>

### Keyboard Controls:
| Key | Action |
|:---|:---|
| **Space** | Commit the highlighted candidate and insert a space. |
| **Enter / Return** | Commit the highlighted candidate without inserting a space. |
| **Number keys `1` - `9`** | Directly select and commit candidate 1, 2, 3, etc. |
| **Arrow Down / Up** | Move selection highlight between suggestions. |
| **Escape** | Dismiss candidate menu and preserve literal typed characters. |
| **Backspace** | Delete previous phonetic character and recompute suggestions. |
