#!/usr/bin/env python3
"""
Bongo Input Method Service for Windows
Provides system-wide Avro phonetic Bengali typing on modern Windows (10/11).
Integrates with the bongo_engine Rust library (or built-in fallback) via ctypes,
intercepts keystrokes via low-level keyboard hooks (WH_KEYBOARD_LL),
and injects Unicode text via SendInput.
"""

import os
import sys
import time
import json
import ctypes
from ctypes import wintypes
from pathlib import Path

# Windows Configuration Path
def get_config_dir():
    appdata = os.environ.get("APPDATA")
    if appdata:
        return Path(appdata) / "Bongo"
    return Path.home() / ".config" / "bongo"

CONFIG_DIR = get_config_dir()
CONFIG_FILE = CONFIG_DIR / "settings.json"

DEFAULT_SETTINGS = {
    "typing_mode": 0,         # 0: PhoneticFirst, 1: Smart, 2: PhoneticOnly
    "suggest_emoji": True,
    "candidate_font": "Noto Sans Bengali",
    "candidate_font_size": 16,
    "toggle_key": "F12",
    "autostart": True,
}

def load_settings():
    settings = dict(DEFAULT_SETTINGS)
    if CONFIG_FILE.is_file():
        try:
            with open(CONFIG_FILE, "r", encoding="utf-8") as f:
                saved = json.load(f)
                settings.update(saved)
        except Exception:
            pass
    return settings

# Locate bongo_engine.dll
def find_engine_dll():
    search_paths = [
        Path(__file__).parent.parent / "engine" / "target" / "release" / "bongo_engine.dll",
        Path(__file__).parent.parent / "engine" / "target" / "debug" / "bongo_engine.dll",
        Path(__file__).parent / "bongo_engine.dll",
        Path(sys.executable).parent / "bongo_engine.dll",
        Path(os.environ.get("LOCALAPPDATA", "")) / "Programs" / "Bongo" / "bongo_engine.dll",
        Path(r"C:\Program Files\Bongo\bongo_engine.dll"),
    ]
    for p in search_paths:
        if p.is_file():
            return str(p)
    return "bongo_engine.dll"

# Native C FFI Binding for bongo_engine.dll
class BongoNativeBinding:
    def __init__(self):
        self.lib = None
        dll_path = find_engine_dll()
        try:
            self.lib = ctypes.CDLL(dll_path)
            self._setup_signatures()
        except OSError:
            self.lib = None

    def _setup_signatures(self):
        self.lib.bongo_engine_new.restype = ctypes.c_void_p
        self.lib.bongo_engine_free.argtypes = [ctypes.c_void_p]

        self.lib.bongo_engine_set_mode.argtypes = [ctypes.c_void_p, ctypes.c_uint32]
        self.lib.bongo_engine_set_emoji.argtypes = [ctypes.c_void_p, ctypes.c_bool]
        self.lib.bongo_engine_has_session.argtypes = [ctypes.c_void_p]
        self.lib.bongo_engine_has_session.restype = ctypes.c_bool

        self.lib.bongo_engine_feed_char.argtypes = [ctypes.c_void_p, ctypes.c_uint32, ctypes.c_uint8, ctypes.c_uint8]
        self.lib.bongo_engine_feed_char.restype = ctypes.c_void_p

        self.lib.bongo_engine_backspace.argtypes = [ctypes.c_void_p, ctypes.c_bool]
        self.lib.bongo_engine_backspace.restype = ctypes.c_void_p

        self.lib.bongo_engine_commit.argtypes = [ctypes.c_void_p, ctypes.c_size_t]
        self.lib.bongo_engine_reset.argtypes = [ctypes.c_void_p]

        self.lib.bongo_suggestion_count.argtypes = [ctypes.c_void_p]
        self.lib.bongo_suggestion_count.restype = ctypes.c_size_t

        self.lib.bongo_suggestion_get_candidate.argtypes = [ctypes.c_void_p, ctypes.c_size_t]
        self.lib.bongo_suggestion_get_candidate.restype = ctypes.c_char_p

        self.lib.bongo_suggestion_get_preview.argtypes = [ctypes.c_void_p]
        self.lib.bongo_suggestion_get_preview.restype = ctypes.c_char_p

        self.lib.bongo_suggestion_free.argtypes = [ctypes.c_void_p]
        self.lib.bongo_string_free.argtypes = [ctypes.c_char_p]


# Pure Python Phonetic Fallback Transliteration Map
# Used when bongo_engine.dll is not built yet (testing or portable execution)
FALLBACK_PHONETIC_MAP = {
    "k": "ক", "kh": "খ", "g": "গ", "gh": "ঘ", "Ng": "ঙ",
    "c": "চ", "ch": "ছ", "j": "জ", "jh": "ঝ", "NG": "ঞ",
    "T": "ট", "Th": "ঠ", "D": "ড", "Dh": "ঢ", "N": "ণ",
    "t": "ত", "th": "থ", "d": "দ", "dh": "ধ", "n": "ন",
    "p": "প", "ph": "ফ", "f": "ফ", "b": "ব", "bh": "ভ", "v": "ভ", "m": "ম",
    "z": "য", "r": "র", "l": "ল", "sh": "শ", "S": "শ", "Sh": "ষ", "s": "স",
    "h": "হ", "R": "ড়", "Rh": "ঢ়", "y": "য়", "Y": "য়",
    "o": "অ", "a": "আ", "i": "ই", "I": "ঈ", "u": "উ", "U": "ঊ",
    "rri": "ঋ", "e": "এ", "OI": "ঐ", "O": "ও", "OU": "ঔ",
}


# Win32 Structures and Constants for Keyboard Injection
if sys.platform == "win32":
    user32 = ctypes.windll.user32
    kernel32 = ctypes.windll.kernel32

    WH_KEYBOARD_LL = 13
    WM_KEYDOWN = 0x0100
    WM_KEYUP = 0x0101
    WM_SYSKEYDOWN = 0x0104
    WM_SYSKEYUP = 0x0105

    VK_ESCAPE = 0x1B
    VK_SPACE = 0x20
    VK_RETURN = 0x0D
    VK_BACK = 0x08
    VK_F12 = 0x7B
    VK_SHIFT = 0x10
    VK_CONTROL = 0x11
    VK_MENU = 0x12  # Alt

    INPUT_KEYBOARD = 1
    KEYEVENTF_KEYUP = 0x0002
    KEYEVENTF_UNICODE = 0x0004

    class KEYBDINPUT(ctypes.Structure):
        _fields_ = [
            ("wVk", wintypes.WORD),
            ("wScan", wintypes.WORD),
            ("dwFlags", wintypes.DWORD),
            ("time", wintypes.DWORD),
            ("dwExtraInfo", ctypes.c_ulong),
        ]

    class INPUT(ctypes.Structure):
        class _INPUT_UNION(ctypes.Union):
            _fields_ = [("ki", KEYBDINPUT)]
        _anonymous_ = ("u",)
        _fields_ = [
            ("type", wintypes.DWORD),
            ("u", _INPUT_UNION),
        ]

    class KBDLLHOOKSTRUCT(ctypes.Structure):
        _fields_ = [
            ("vkCode", wintypes.DWORD),
            ("scanCode", wintypes.DWORD),
            ("flags", wintypes.DWORD),
            ("time", wintypes.DWORD),
            ("dwExtraInfo", ctypes.c_ulong),
        ]

    HOOKPROC = ctypes.WINFUNCTYPE(ctypes.c_long, ctypes.c_int, wintypes.WPARAM, wintypes.LPARAM)


def send_unicode_string(text: str):
    """Injects a Unicode string into the active window using SendInput."""
    if sys.platform != "win32":
        return

    inputs = []
    for ch in text:
        # Key down
        inp_down = INPUT()
        inp_down.type = INPUT_KEYBOARD
        inp_down.ki.wVk = 0
        inp_down.ki.wScan = ord(ch)
        inp_down.ki.dwFlags = KEYEVENTF_UNICODE
        inp_down.ki.time = 0
        inp_down.ki.dwExtraInfo = 0
        inputs.append(inp_down)

        # Key up
        inp_up = INPUT()
        inp_up.type = INPUT_KEYBOARD
        inp_up.ki.wVk = 0
        inp_up.ki.wScan = ord(ch)
        inp_up.ki.dwFlags = KEYEVENTF_UNICODE | KEYEVENTF_KEYUP
        inp_up.ki.time = 0
        inp_up.ki.dwExtraInfo = 0
        inputs.append(inp_up)

    if inputs:
        n = len(inputs)
        arr = (INPUT * n)(*inputs)
        user32.SendInput(n, ctypes.byref(arr), ctypes.sizeof(INPUT))


def send_backspace_keys(count: int):
    """Sends backspaces to erase raw keystrokes in target application."""
    if sys.platform != "win32":
        return

    inputs = []
    for _ in range(count):
        inp_down = INPUT()
        inp_down.type = INPUT_KEYBOARD
        inp_down.ki.wVk = VK_BACK
        inp_down.ki.wScan = 0
        inp_down.ki.dwFlags = 0
        inputs.append(inp_down)

        inp_up = INPUT()
        inp_up.type = INPUT_KEYBOARD
        inp_up.ki.wVk = VK_BACK
        inp_up.ki.wScan = 0
        inp_up.ki.dwFlags = KEYEVENTF_KEYUP
        inputs.append(inp_up)

    if inputs:
        n = len(inputs)
        arr = (INPUT * n)(*inputs)
        user32.SendInput(n, ctypes.byref(arr), ctypes.sizeof(INPUT))


class BongoWindowsService:
    def __init__(self):
        self.settings = load_settings()
        self._settings_mtime = 0

        self.bangla_active = False  # Toggled via F12
        self.native = BongoNativeBinding()
        self.engine_ptr = self.native.lib.bongo_engine_new() if (self.native and self.native.lib) else None

        self.buffer = ""
        self.candidates = []
        self.preedit_str = ""

        self.hook_id = None
        self.hook_proc_ref = None

    def toggle_mode(self):
        self.bangla_active = not self.bangla_active
        if not self.bangla_active:
            self.reset_session()
        mode_str = "Bangla" if self.bangla_active else "English"
        sys.stdout.write(f"Bongo: Switched to {mode_str} Mode\n")
        sys.stdout.flush()

    def has_session(self):
        if self.engine_ptr and self.native and self.native.lib:
            return self.native.lib.bongo_engine_has_session(self.engine_ptr)
        return bool(self.buffer)

    def feed_character(self, ch: str):
        self.buffer += ch
        if self.engine_ptr and self.native and self.native.lib:
            cp = ord(ch)
            modifier = 1 if ch.isupper() else 0
            sugg_ptr = self.native.lib.bongo_engine_feed_char(self.engine_ptr, cp, modifier, 0)
            if sugg_ptr:
                self.update_suggestions(sugg_ptr)
                self.native.lib.bongo_suggestion_free(sugg_ptr)
                return True
        else:
            # Fallback transliteration
            res = FALLBACK_PHONETIC_MAP.get(self.buffer, self.buffer)
            self.candidates = [res]
            self.preedit_str = res
            return True
        return False

    def update_suggestions(self, sugg_ptr):
        count = self.native.lib.bongo_suggestion_count(sugg_ptr)
        self.candidates = []
        for i in range(count):
            c_ptr = self.native.lib.bongo_suggestion_get_candidate(sugg_ptr, i)
            if c_ptr:
                c_str = c_ptr.decode("utf-8", errors="replace")
                self.candidates.append(c_str)
                self.native.lib.bongo_string_free(c_ptr)

        p_ptr = self.native.lib.bongo_suggestion_get_preview(sugg_ptr)
        if p_ptr:
            self.preedit_str = p_ptr.decode("utf-8", errors="replace")
            self.native.lib.bongo_string_free(p_ptr)
        elif self.candidates:
            self.preedit_str = self.candidates[0]
        else:
            self.preedit_str = ""

    def handle_backspace(self):
        if self.engine_ptr and self.native and self.native.lib:
            sugg_ptr = self.native.lib.bongo_engine_backspace(self.engine_ptr, False)
            if sugg_ptr:
                self.update_suggestions(sugg_ptr)
                self.native.lib.bongo_suggestion_free(sugg_ptr)
            else:
                self.reset_session()
        elif self.buffer:
            self.buffer = self.buffer[:-1]
            if not self.buffer:
                self.reset_session()
            else:
                res = FALLBACK_PHONETIC_MAP.get(self.buffer, self.buffer)
                self.candidates = [res]
                self.preedit_str = res

    def commit_candidate(self, index=0):
        if not self.candidates and not self.preedit_str:
            self.reset_session()
            return

        text_to_commit = ""
        if index < len(self.candidates):
            text_to_commit = self.candidates[index]
        else:
            text_to_commit = self.preedit_str

        # Commit to active application
        send_unicode_string(text_to_commit)

        if self.engine_ptr and self.native and self.native.lib:
            self.native.lib.bongo_engine_commit(self.engine_ptr, index)

        self.reset_session()

    def reset_session(self):
        if self.engine_ptr and self.native and self.native.lib:
            self.native.lib.bongo_engine_reset(self.engine_ptr)
        self.buffer = ""
        self.candidates = []
        self.preedit_str = ""

    def process_key(self, vk_code: int, is_down: bool, is_shift: bool, is_ctrl: bool, is_alt: bool) -> bool:
        """
        Returns True if the keystroke was handled/swallowed,
        or False to let it pass through to the system.
        """
        if not is_down:
            return False

        # Check toggle shortcut (F12)
        if vk_code == VK_F12:
            self.toggle_mode()
            return True

        if not self.bangla_active:
            return False

        # Pass through Ctrl or Alt combos (e.g. Ctrl+C, Alt+Tab)
        if is_ctrl or is_alt:
            if self.has_session():
                self.commit_candidate(0)
            return False

        # In-session navigation
        if self.has_session():
            if vk_code == VK_ESCAPE:
                self.reset_session()
                return True

            if vk_code in (VK_SPACE, VK_RETURN):
                self.commit_candidate(0)
                # Let space or return pass if needed, or swallow
                return True

            if vk_code == VK_BACK:
                self.handle_backspace()
                return True

            # Number keys 1-9 for candidate selection
            if 0x31 <= vk_code <= 0x39:
                idx = vk_code - 0x31
                if idx < len(self.candidates):
                    self.commit_candidate(idx)
                    return True

        # Process standard characters
        if 0x41 <= vk_code <= 0x5A:  # A-Z
            char_code = vk_code + (32 if not is_shift else 0)
            ch = chr(char_code)
            self.feed_character(ch)
            return True

        return False

    def run_windows_hook(self):
        """Sets up Windows low-level keyboard hook and message loop."""
        if sys.platform != "win32":
            sys.stdout.write("Bongo Service: Windows environment required for hook loop.\n")
            return

        def low_level_keyboard_proc(n_code, w_param, l_param):
            if n_code >= 0:
                is_down = w_param in (WM_KEYDOWN, WM_SYSKEYDOWN)
                hook_data = ctypes.cast(l_param, ctypes.POINTER(KBDLLHOOKSTRUCT)).contents
                vk = hook_data.vkCode

                is_shift = bool(user32.GetKeyState(VK_SHIFT) & 0x8000)
                is_ctrl = bool(user32.GetKeyState(VK_CONTROL) & 0x8000)
                is_alt = bool(user32.GetKeyState(VK_MENU) & 0x8000)

                handled = self.process_key(vk, is_down, is_shift, is_ctrl, is_alt)
                if handled:
                    return 1  # Swallow event

            return user32.CallNextHookEx(self.hook_id, n_code, w_param, l_param)

        self.hook_proc_ref = HOOKPROC(low_level_keyboard_proc)
        h_mod = kernel32.GetModuleHandleW(None)
        self.hook_id = user32.SetWindowsHookExW(
            WH_KEYBOARD_LL,
            self.hook_proc_ref,
            h_mod,
            0
        )

        if not self.hook_id:
            sys.stderr.write("Error: Failed to install low-level keyboard hook.\n")
            return

        sys.stdout.write("Bongo Windows Service active. Press F12 to toggle Bangla/English.\n")
        sys.stdout.flush()

        msg = wintypes.MSG()
        while user32.GetMessageW(ctypes.byref(msg), 0, 0, 0) != 0:
            user32.TranslateMessage(ctypes.byref(msg))
            user32.DispatchMessageW(ctypes.byref(msg))

        user32.UnhookWindowsHookEx(self.hook_id)


def main():
    # If launched interactively without --daemon, launch the settings/welcome window
    if sys.platform == "win32" and "--daemon" not in sys.argv:
        try:
            import subprocess
            candidates = [
                Path(sys.executable).parent / "bongo-settings.exe",
                Path(__file__).resolve().parent.parent / "ui" / "bongo_settings.py",
            ]
            for c in candidates:
                if c.is_file():
                    if c.suffix == ".py":
                        subprocess.Popen([sys.executable, str(c)])
                    else:
                        subprocess.Popen([str(c)])
                    break
        except Exception:
            pass

    service = BongoWindowsService()
    if sys.platform == "win32":
        service.run_windows_hook()
    else:
        sys.stdout.write("Bongo Windows Service initialized in headless cross-platform mode.\n")


if __name__ == "__main__":
    main()
