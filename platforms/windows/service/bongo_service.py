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
import re
import ctypes
from ctypes import wintypes
from pathlib import Path

class SafeStream:
    def write(self, s):
        pass
    def flush(self):
        pass

if sys.stdout is None:
    sys.stdout = SafeStream()
if sys.stderr is None:
    sys.stderr = SafeStream()

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
        Path(sys.executable).parent / "bongo_engine.dll",
        Path(__file__).resolve().parent / "bongo_engine.dll",
        Path(__file__).resolve().parent.parent / "build" / "bongo_engine.dll",
        Path(__file__).resolve().parent.parent / "engine" / "target" / "release" / "bongo_engine.dll",
        Path(__file__).resolve().parent.parent / "engine" / "target" / "debug" / "bongo_engine.dll",
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
        except (OSError, AttributeError, Exception):
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
        self.lib.bongo_suggestion_get_candidate.restype = ctypes.c_void_p

        self.lib.bongo_suggestion_get_preview.argtypes = [ctypes.c_void_p]
        self.lib.bongo_suggestion_get_preview.restype = ctypes.c_void_p

        self.lib.bongo_suggestion_free.argtypes = [ctypes.c_void_p]
        self.lib.bongo_string_free.argtypes = [ctypes.c_void_p]



# Pure Python Avro Phonetic Engine
AVRO_PATTERNS_DATA = json.loads(r'[{"find":"NgkSh","default":"ঙ্ক্ষ","rules":[]},{"find":"Ngkkh","default":"ঙ্ক্ষ","rules":[]},{"find":"kkhN","default":"ক্ষ্ণ","rules":[]},{"find":"kShN","default":"ক্ষ্ণ","rules":[]},{"find":"kkhm","default":"ক্ষ্ম","rules":[]},{"find":"kShm","default":"ক্ষ্ম","rules":[]},{"find":"NGch","default":"ঞ্ছ","rules":[]},{"find":"Nggh","default":"ঙ্ঘ","rules":[]},{"find":"Ngkh","default":"ঙ্খ","rules":[]},{"find":"NGjh","default":"ঞ্ঝ","rules":[]},{"find":"ngOU","default":"ঙ্গৌ","rules":[]},{"find":"ngOI","default":"ঙ্গৈ","rules":[]},{"find":"Ngkx","default":"ঙ্ক্ষ","rules":[]},{"find":"rri`","default":"ৃ","rules":[]},{"find":"shch","default":"শ্ছ","rules":[]},{"find":"ShTh","default":"ষ্ঠ","rules":[]},{"find":"Shph","default":"ষ্ফ","rules":[]},{"find":"psh","default":"পশ","rules":[]},{"find":"bhl","default":"ভ্ল","rules":[]},{"find":"bdh","default":"ব্ধ","rules":[]},{"find":"cNG","default":"চ্ঞ","rules":[]},{"find":"cch","default":"চ্ছ","rules":[]},{"find":"dhn","default":"ধ্ন","rules":[]},{"find":"dhm","default":"ধ্ম","rules":[]},{"find":"dgh","default":"দ্ঘ","rules":[]},{"find":"ddh","default":"দ্ধ","rules":[]},{"find":"dbh","default":"দ্ভ","rules":[]},{"find":"...","default":"...","rules":[]},{"find":"ghn","default":"ঘ্ন","rules":[]},{"find":"Ghn","default":"ঘ্ন","rules":[]},{"find":"gdh","default":"গ্ধ","rules":[]},{"find":"Gdh","default":"গ্ধ","rules":[]},{"find":"jjh","default":"জ্ঝ","rules":[]},{"find":"jNG","default":"জ্ঞ","rules":[]},{"find":"kxN","default":"ক্ষ্ণ","rules":[]},{"find":"kxm","default":"ক্ষ্ম","rules":[]},{"find":"kkh","default":"ক্ষ","rules":[]},{"find":"kSh","default":"ক্ষ","rules":[]},{"find":"ksh","default":"কশ","rules":[]},{"find":"lbh","default":"ল্ভ","rules":[]},{"find":"ldh","default":"ল্ধ","rules":[]},{"find":"lkh","default":"লখ","rules":[]},{"find":"lgh","default":"লঘ","rules":[]},{"find":"lph","default":"লফ","rules":[]},{"find":"mth","default":"ম্থ","rules":[]},{"find":"mph","default":"ম্ফ","rules":[]},{"find":"mbh","default":"ম্ভ","rules":[]},{"find":"mpl","default":"মপ্ল","rules":[]},{"find":"NGc","default":"ঞ্চ","rules":[]},{"find":"nch","default":"ঞ্ছ","rules":[]},{"find":"njh","default":"ঞ্ঝ","rules":[]},{"find":"ngh","default":"ঙ্ঘ","rules":[]},{"find":"Ngk","default":"ঙ্ক","rules":[]},{"find":"Ngx","default":"ঙ্ষ","rules":[]},{"find":"Ngg","default":"ঙ্গ","rules":[]},{"find":"Ngm","default":"ঙ্ম","rules":[]},{"find":"NGj","default":"ঞ্জ","rules":[]},{"find":"ndh","default":"ন্ধ","rules":[]},{"find":"nTh","default":"ন্ঠ","rules":[]},{"find":"NTh","default":"ণ্ঠ","rules":[]},{"find":"nth","default":"ন্থ","rules":[]},{"find":"nkh","default":"ঙ্খ","rules":[]},{"find":"ngo","default":"ঙ্গ","rules":[]},{"find":"nga","default":"ঙ্গা","rules":[]},{"find":"ngi","default":"ঙ্গি","rules":[]},{"find":"ngI","default":"ঙ্গী","rules":[]},{"find":"ngu","default":"ঙ্গু","rules":[]},{"find":"ngU","default":"ঙ্গূ","rules":[]},{"find":"nge","default":"ঙ্গে","rules":[]},{"find":"ngO","default":"ঙ্গো","rules":[]},{"find":"NDh","default":"ণ্ঢ","rules":[]},{"find":"nsh","default":"নশ","rules":[]},{"find":"Ngr","default":"ঙর","rules":[]},{"find":"NGr","default":"ঞর","rules":[]},{"find":"ngr","default":"ংর","rules":[]},{"find":"OI`","default":"ৈ","rules":[]},{"find":"OU`","default":"ৌ","rules":[]},{"find":"phl","default":"ফ্ল","rules":[]},{"find":"rri","default":"ৃ","rules":[{"matches":[{"type":"PrefixIsNot","scope":"Consonant"}],"replace":"ঋ"},{"matches":[{"type":"PrefixIs","scope":"Punctuation"}],"replace":"ঋ"}]},{"find":"rrZ","default":"রর\u200d্য","rules":[]},{"find":"rry","default":"রর\u200d্য","rules":[]},{"find":"Sch","default":"শ্ছ","rules":[]},{"find":"skl","default":"স্ক্ল","rules":[]},{"find":"skh","default":"স্খ","rules":[]},{"find":"sth","default":"স্থ","rules":[]},{"find":"sph","default":"স্ফ","rules":[]},{"find":"shc","default":"শ্চ","rules":[]},{"find":"sht","default":"শ্ত","rules":[]},{"find":"shn","default":"শ্ন","rules":[]},{"find":"shm","default":"শ্ম","rules":[]},{"find":"shl","default":"শ্ল","rules":[]},{"find":"Shk","default":"ষ্ক","rules":[]},{"find":"ShT","default":"ষ্ট","rules":[]},{"find":"ShN","default":"ষ্ণ","rules":[]},{"find":"Shp","default":"ষ্প","rules":[]},{"find":"Shf","default":"ষ্ফ","rules":[]},{"find":"Shm","default":"ষ্ম","rules":[]},{"find":"spl","default":"স্প্ল","rules":[]},{"find":"oo`","default":"ু","rules":[]},{"find":"tth","default":"ত্থ","rules":[]},{"find":"t``","default":"ৎ","rules":[]},{"find":"ee`","default":"ী","rules":[]},{"find":"bj","default":"ব্জ","rules":[]},{"find":"bd","default":"ব্দ","rules":[]},{"find":"bb","default":"ব্ব","rules":[]},{"find":"bl","default":"ব্ল","rules":[]},{"find":"bh","default":"ভ","rules":[]},{"find":"vl","default":"ভ্ল","rules":[]},{"find":"cc","default":"চ্চ","rules":[]},{"find":"ch","default":"ছ","rules":[]},{"find":"dv","default":"দ্ভ","rules":[]},{"find":"dm","default":"দ্ম","rules":[]},{"find":"DD","default":"ড্ড","rules":[]},{"find":"Dh","default":"ঢ","rules":[]},{"find":"dh","default":"ধ","rules":[]},{"find":"dg","default":"দ্গ","rules":[]},{"find":"dd","default":"দ্দ","rules":[]},{"find":".`","default":".","rules":[]},{"find":"..","default":"।।","rules":[]},{"find":"gN","default":"গ্ণ","rules":[]},{"find":"GN","default":"গ্ণ","rules":[]},{"find":"gn","default":"গ্ন","rules":[]},{"find":"Gn","default":"গ্ন","rules":[]},{"find":"gm","default":"গ্ম","rules":[]},{"find":"Gm","default":"গ্ম","rules":[]},{"find":"gl","default":"গ্ল","rules":[]},{"find":"Gl","default":"গ্ল","rules":[]},{"find":"gg","default":"জ্ঞ","rules":[]},{"find":"GG","default":"জ্ঞ","rules":[]},{"find":"Gg","default":"জ্ঞ","rules":[]},{"find":"gG","default":"জ্ঞ","rules":[]},{"find":"gh","default":"ঘ","rules":[]},{"find":"Gh","default":"ঘ","rules":[]},{"find":"hN","default":"হ্ণ","rules":[]},{"find":"hn","default":"হ্ন","rules":[]},{"find":"hm","default":"হ্ম","rules":[]},{"find":"hl","default":"হ্ল","rules":[]},{"find":"jh","default":"ঝ","rules":[]},{"find":"jj","default":"জ্জ","rules":[]},{"find":"kx","default":"ক্ষ","rules":[]},{"find":"kk","default":"ক্ক","rules":[]},{"find":"kT","default":"ক্ট","rules":[]},{"find":"kt","default":"ক্ত","rules":[]},{"find":"kl","default":"ক্ল","rules":[]},{"find":"ks","default":"ক্স","rules":[]},{"find":"kh","default":"খ","rules":[]},{"find":"lk","default":"ল্ক","rules":[]},{"find":"lg","default":"ল্গ","rules":[]},{"find":"lT","default":"ল্ট","rules":[]},{"find":"lD","default":"ল্ড","rules":[]},{"find":"lp","default":"ল্প","rules":[]},{"find":"lv","default":"ল্ভ","rules":[]},{"find":"lm","default":"ল্ম","rules":[]},{"find":"ll","default":"ল্ল","rules":[]},{"find":"lb","default":"ল্ব","rules":[]},{"find":"mn","default":"ম্ন","rules":[]},{"find":"mp","default":"ম্প","rules":[]},{"find":"mv","default":"ম্ভ","rules":[]},{"find":"mm","default":"ম্ম","rules":[]},{"find":"ml","default":"ম্ল","rules":[]},{"find":"mb","default":"ম্ব","rules":[]},{"find":"mf","default":"ম্ফ","rules":[]},{"find":"nj","default":"ঞ্জ","rules":[]},{"find":"Ng","default":"ঙ","rules":[]},{"find":"NG","default":"ঞ","rules":[]},{"find":"nk","default":"ঙ্ক","rules":[]},{"find":"ng","default":"ং","rules":[]},{"find":"nn","default":"ন্ন","rules":[]},{"find":"NN","default":"ণ্ণ","rules":[]},{"find":"Nn","default":"ণ্ন","rules":[]},{"find":"nm","default":"ন্ম","rules":[]},{"find":"Nm","default":"ণ্ম","rules":[]},{"find":"nd","default":"ন্দ","rules":[]},{"find":"nT","default":"ন্ট","rules":[]},{"find":"NT","default":"ণ্ট","rules":[]},{"find":"nD","default":"ন্ড","rules":[]},{"find":"ND","default":"ণ্ড","rules":[]},{"find":"nt","default":"ন্ত","rules":[]},{"find":"ns","default":"ন্স","rules":[]},{"find":"nc","default":"ঞ্চ","rules":[]},{"find":"O`","default":"ো","rules":[]},{"find":"OI","default":"ৈ","rules":[{"matches":[{"type":"PrefixIsNot","scope":"Consonant"}],"replace":"ঐ"},{"matches":[{"type":"PrefixIs","scope":"Punctuation"}],"replace":"ঐ"}]},{"find":"OU","default":"ৌ","rules":[{"matches":[{"type":"PrefixIsNot","scope":"Consonant"}],"replace":"ঔ"},{"matches":[{"type":"PrefixIs","scope":"Punctuation"}],"replace":"ঔ"}]},{"find":"pT","default":"প্ট","rules":[]},{"find":"pt","default":"প্ত","rules":[]},{"find":"pn","default":"প্ন","rules":[]},{"find":"pp","default":"প্প","rules":[]},{"find":"pl","default":"প্ল","rules":[]},{"find":"ps","default":"প্স","rules":[]},{"find":"ph","default":"ফ","rules":[]},{"find":"fl","default":"ফ্ল","rules":[]},{"find":"rZ","default":"র\u200d্য","rules":[{"matches":[{"type":"PrefixIs","scope":"Consonant"},{"type":"PrefixIsNot","scope":"Char","val":"r"},{"type":"PrefixIsNot","scope":"Char","val":"y"},{"type":"PrefixIsNot","scope":"Char","val":"w"},{"type":"PrefixIsNot","scope":"Char","val":"x"}],"replace":"্র্য"}]},{"find":"ry","default":"র\u200d্য","rules":[{"matches":[{"type":"PrefixIs","scope":"Consonant"},{"type":"PrefixIsNot","scope":"Char","val":"r"},{"type":"PrefixIsNot","scope":"Char","val":"y"},{"type":"PrefixIsNot","scope":"Char","val":"w"},{"type":"PrefixIsNot","scope":"Char","val":"x"}],"replace":"্র্য"}]},{"find":"rr","default":"রর","rules":[{"matches":[{"type":"PrefixIsNot","scope":"Consonant"},{"type":"SuffixIsNot","scope":"Vowel"},{"type":"SuffixIsNot","scope":"Char","val":"r"},{"type":"SuffixIsNot","scope":"Punctuation"}],"replace":"র্"},{"matches":[{"type":"PrefixIs","scope":"Consonant"},{"type":"PrefixIsNot","scope":"Char","val":"r"}],"replace":"্রর"}]},{"find":"Rg","default":"ড়্গ","rules":[]},{"find":"Rh","default":"ঢ়","rules":[]},{"find":"sk","default":"স্ক","rules":[]},{"find":"Sc","default":"শ্চ","rules":[]},{"find":"sT","default":"স্ট","rules":[]},{"find":"st","default":"স্ত","rules":[]},{"find":"sn","default":"স্ন","rules":[]},{"find":"sp","default":"স্প","rules":[]},{"find":"sf","default":"স্ফ","rules":[]},{"find":"sm","default":"স্ম","rules":[]},{"find":"sl","default":"স্ল","rules":[]},{"find":"sh","default":"শ","rules":[]},{"find":"Sc","default":"শ্চ","rules":[]},{"find":"St","default":"শ্ত","rules":[]},{"find":"Sn","default":"শ্ন","rules":[]},{"find":"Sm","default":"শ্ম","rules":[]},{"find":"Sl","default":"শ্ল","rules":[]},{"find":"Sh","default":"ষ","rules":[]},{"find":"oo","default":"ু","rules":[{"matches":[{"type":"PrefixIsNot","scope":"Consonant"},{"type":"SuffixIsNot","scope":"Char","val":"`"}],"replace":"উ"},{"matches":[{"type":"PrefixIs","scope":"Punctuation"},{"type":"SuffixIsNot","scope":"Char","val":"`"}],"replace":"উ"}]},{"find":"o`","default":"","rules":[]},{"find":"oZ","default":"অ্য","rules":[]},{"find":"TT","default":"ট্ট","rules":[]},{"find":"Tm","default":"ট্ম","rules":[]},{"find":"Th","default":"ঠ","rules":[]},{"find":"tn","default":"ত্ন","rules":[]},{"find":"tm","default":"ত্ম","rules":[]},{"find":"th","default":"থ","rules":[]},{"find":"tt","default":"ত্ত","rules":[]},{"find":"aZ","default":"অ্যা","rules":[]},{"find":"AZ","default":"অ্যা","rules":[]},{"find":"a`","default":"া","rules":[]},{"find":"A`","default":"া","rules":[]},{"find":"i`","default":"ি","rules":[]},{"find":"I`","default":"ী","rules":[]},{"find":"u`","default":"ু","rules":[]},{"find":"U`","default":"ূ","rules":[]},{"find":"ee","default":"ী","rules":[{"matches":[{"type":"PrefixIsNot","scope":"Consonant"},{"type":"SuffixIsNot","scope":"Char","val":"`"}],"replace":"ঈ"},{"matches":[{"type":"PrefixIs","scope":"Punctuation"},{"type":"SuffixIsNot","scope":"Char","val":"`"}],"replace":"ঈ"}]},{"find":"e`","default":"ে","rules":[]},{"find":":`","default":":","rules":[]},{"find":"^`","default":"^","rules":[]},{"find":",,","default":"্\u200c","rules":[]},{"find":"b","default":"ব","rules":[]},{"find":"v","default":"ভ","rules":[]},{"find":"c","default":"চ","rules":[]},{"find":"D","default":"ড","rules":[]},{"find":"d","default":"দ","rules":[]},{"find":".","default":"।","rules":[{"matches":[{"type":"SuffixIs","scope":"Number"}],"replace":"."}]},{"find":"g","default":"গ","rules":[]},{"find":"G","default":"গ","rules":[]},{"find":"h","default":"হ","rules":[]},{"find":"j","default":"জ","rules":[]},{"find":"J","default":"জ","rules":[]},{"find":"k","default":"ক","rules":[]},{"find":"l","default":"ল","rules":[]},{"find":"m","default":"ম","rules":[]},{"find":"0","default":"০","rules":[]},{"find":"1","default":"১","rules":[]},{"find":"2","default":"২","rules":[]},{"find":"3","default":"৩","rules":[]},{"find":"4","default":"৪","rules":[]},{"find":"5","default":"৫","rules":[]},{"find":"6","default":"৬","rules":[]},{"find":"7","default":"৭","rules":[]},{"find":"8","default":"৮","rules":[]},{"find":"9","default":"৯","rules":[]},{"find":"n","default":"ন","rules":[]},{"find":"N","default":"ণ","rules":[]},{"find":"O","default":"ো","rules":[{"matches":[{"type":"PrefixIsNot","scope":"Consonant"}],"replace":"ও"},{"matches":[{"type":"PrefixIs","scope":"Punctuation"}],"replace":"ও"}]},{"find":"f","default":"ফ","rules":[]},{"find":"p","default":"প","rules":[]},{"find":"R","default":"ড়","rules":[]},{"find":"r","default":"র","rules":[{"matches":[{"type":"PrefixIs","scope":"Consonant"},{"type":"PrefixIsNot","scope":"Char","val":"r"},{"type":"PrefixIsNot","scope":"Char","val":"y"},{"type":"PrefixIsNot","scope":"Char","val":"w"},{"type":"PrefixIsNot","scope":"Char","val":"x"},{"type":"PrefixIsNot","scope":"Char","val":"Z"}],"replace":"্র"}]},{"find":"s","default":"স","rules":[]},{"find":"S","default":"শ","rules":[]},{"find":"o","default":"","rules":[{"matches":[{"type":"PrefixIs","scope":"Vowel"},{"type":"PrefixIsNot","scope":"Char","val":"o"}],"replace":"ও"},{"matches":[{"type":"PrefixIs","scope":"Vowel"},{"type":"PrefixIs","scope":"Char","val":"o"}],"replace":"অ"},{"matches":[{"type":"PrefixIs","scope":"Punctuation"}],"replace":"অ"}]},{"find":"T","default":"ট","rules":[]},{"find":"t","default":"ত","rules":[]},{"find":"a","default":"া","rules":[{"matches":[{"type":"PrefixIs","scope":"Punctuation"},{"type":"SuffixIsNot","scope":"Char","val":"`"}],"replace":"আ"},{"matches":[{"type":"PrefixIsNot","scope":"Consonant"},{"type":"PrefixIsNot","scope":"Char","val":"a"},{"type":"SuffixIsNot","scope":"Char","val":"`"}],"replace":"য়া"},{"matches":[{"type":"PrefixIs","scope":"Char","val":"a"},{"type":"SuffixIsNot","scope":"Char","val":"`"}],"replace":"আ"}]},{"find":"i","default":"ি","rules":[{"matches":[{"type":"PrefixIsNot","scope":"Consonant"},{"type":"SuffixIsNot","scope":"Char","val":"`"}],"replace":"ই"},{"matches":[{"type":"PrefixIs","scope":"Punctuation"},{"type":"SuffixIsNot","scope":"Char","val":"`"}],"replace":"ই"}]},{"find":"I","default":"ী","rules":[{"matches":[{"type":"PrefixIsNot","scope":"Consonant"},{"type":"SuffixIsNot","scope":"Char","val":"`"}],"replace":"ঈ"},{"matches":[{"type":"PrefixIs","scope":"Punctuation"},{"type":"SuffixIsNot","scope":"Char","val":"`"}],"replace":"ঈ"}]},{"find":"u","default":"ু","rules":[{"matches":[{"type":"PrefixIsNot","scope":"Consonant"},{"type":"SuffixIsNot","scope":"Char","val":"`"}],"replace":"উ"},{"matches":[{"type":"PrefixIs","scope":"Punctuation"},{"type":"SuffixIsNot","scope":"Char","val":"`"}],"replace":"উ"}]},{"find":"U","default":"ূ","rules":[{"matches":[{"type":"PrefixIsNot","scope":"Consonant"},{"type":"SuffixIsNot","scope":"Char","val":"`"}],"replace":"ঊ"},{"matches":[{"type":"PrefixIs","scope":"Punctuation"},{"type":"SuffixIsNot","scope":"Char","val":"`"}],"replace":"ঊ"}]},{"find":"e","default":"ে","rules":[{"matches":[{"type":"PrefixIsNot","scope":"Consonant"},{"type":"SuffixIsNot","scope":"Char","val":"`"}],"replace":"এ"},{"matches":[{"type":"PrefixIs","scope":"Punctuation"},{"type":"SuffixIsNot","scope":"Char","val":"`"}],"replace":"এ"}]},{"find":"z","default":"য","rules":[]},{"find":"Z","default":"্য","rules":[]},{"find":"y","default":"্য","rules":[{"matches":[{"type":"PrefixIsNot","scope":"Consonant"},{"type":"PrefixIsNot","scope":"Punctuation"}],"replace":"য়"},{"matches":[{"type":"PrefixIs","scope":"Punctuation"}],"replace":"ইয়"}]},{"find":"Y","default":"য়","rules":[]},{"find":"q","default":"ক","rules":[]},{"find":"w","default":"ও","rules":[{"matches":[{"type":"PrefixIs","scope":"Punctuation"},{"type":"SuffixIs","scope":"Vowel"}],"replace":"ওয়"},{"matches":[{"type":"PrefixIs","scope":"Consonant"}],"replace":"্ব"}]},{"find":"x","default":"ক্স","rules":[{"matches":[{"type":"PrefixIs","scope":"Punctuation"}],"replace":"এক্স"}]},{"find":":","default":"ঃ","rules":[]},{"find":"^","default":"ঁ","rules":[]},{"find":",","default":",","rules":[]},{"find":"$","default":"৳","rules":[]},{"find":"`","default":"","rules":[]}]')
AVRO_DICTIONARY_DATA = json.loads(r'{"ami":["আমি","আম","আমায়","আমেরিকায়","আমার"],"amader":["আমাদের","আমোদ","আমড়া"],"amar":["আমার","আমরা","অমর"],"amra":["আমরা"],"amake":["আমাকে"],"tumi":["তুমি","তোমরা","তোমার"],"tomader":["তোমাদের"],"tomar":["তোমার"],"tomake":["তোমাকে"],"tomay":["তোমায়","তোমাকে"],"apni":["আপনি","আপনে","আপ্নি"],"apnader":["আপনাদের"],"apnar":["আপনার"],"apnake":["আপনাকে"],"she":["সে","শে"],"tini":["তিনি"],"tara":["তারা"],"tader":["তাদের"],"taderke":["তাদেরকে"],"ora":["ওরা"],"oder":["ওদের"],"era":["এরা"],"eder":["এদের"],"kemon":["কেমন","কেমনে"],"achen":["আছেন","আছি","আছে"],"achi":["আছি"],"ache":["আছে"],"achilo":["ছিল","আছিল"],"chilo":["ছিল"],"chilam":["ছিলাম"],"chile":["ছিলে"],"chilen":["ছিলেন"],"bhalo":["ভালো","ভাল","ভালা"],"valo":["ভালো","ভাল"],"bhalobashi":["ভালোবাসি","ভালোবাসা"],"valobashi":["ভালোবাসি"],"bhalobasha":["ভালোবাসা","ভালোবাসায়"],"valobasha":["ভালোবাসা"],"dhonnobad":["ধন্যবাদ","ধন্য","ধন"],"bangla":["বাংলা","বাঙলা","বাঙালি","বাংলাদেশ"],"bangladesh":["বাংলাদেশ","বাংলাদেশী"],"banglay":["বাংলায়","বাংলায়","বাঙলায়"],"shonar":["সোনার","শোনার","সুন্দর"],"sonar":["সোনার"],"gan":["গান","গণ","জ্ঞান"],"gai":["গাই","গায়ে"],"bongo":["বঙ্গ","বঙ্গবন্ধু","বঙ্গোপসাগর"],"lekho":["লেখো","লেখা","লেখক"],"likhi":["লিখি"],"likhte":["লিখতে"],"lekha":["লেখা"],"onek":["অনেক","অনেকে","অনুপ"],"shob":["সব","শব","সবাই"],"sob":["সব","সবাই"],"shobai":["সবাই"],"sobai":["সবাই"],"shobar":["সবার","শোবার"],"sobar":["সবার"],"shobaike":["সবাইকে"],"shanti":["শান্তি","শান্ত"],"kolkata":["কলকাতা","কলিকাতা"],"dhaka":["ঢাকা","ঢাকাই","ঢাক"],"robindronath":["রবীন্দ্রনাথ","রবি","রব"],"kazi":["কাজী","কাজি"],"nazrul":["নজরুল"],"koto":["কত","কতো"],"kichu":["কিছু","কিছুই"],"desh":["দেশ","দেশে","দেশের"],"deshe":["দেশে"],"desher":["দেশের"],"manush":["মানুষ","মানুষের","মানুষকে"],"manushe":["মানুষে"],"manusher":["মানুষের"],"bondhu":["বন্ধু","বন্ধুরা","বন্ধুর"],"bondhura":["বন্ধুরা"],"khabar":["খাবার","খবর"],"khobor":["খবর","খবরে"],"shundor":["সুন্দর","সুন্দরী"],"sundor":["সুন্দর"],"ranna":["রান্না","রান্নাঘর"],"kothay":["কোথায়","কথায়"],"shomoy":["সময়","সাময়িক"],"somoy":["সময়"],"protidin":["প্রতিদিন"],"notun":["নতুন","নূতন"],"purono":["পুরোনো","পুরনো"],"shobdo":["শব্দ","শব্দকোষ"],"sobdo":["শব্দ"],"ki":["কি","কী"],"keno":["কেন","কেনো"],"kobe":["কবে"],"kothao":["কোথাও"],"kokhon":["কখন","কখনো"],"ekhon":["এখন","এখনো"],"tokhon":["তখন","তখনো"],"jokhon":["যখন","যখনই"],"ei":["এই"],"oi":["ওই","ঐ"],"shei":["সেই"],"je":["যে","যেসব"],"o":["ও"],"ebong":["এবং"],"ar":["আর","আরও"],"kintu":["কিন্তু"],"ba":["বা"],"othoba":["অথবা"],"na":["না","নাই"],"hacche":["হচ্ছে"],"hocche":["হচ্ছে"],"hobe":["হবে","হবেন"],"hobar":["হবার"],"hoyeche":["হয়েছে"],"kaj":["কাজ","কাজে"],"kaje":["কাজে"],"basha":["বাসা","ভাষা"],"bhasha":["ভাষা","ভাষায়"],"bari":["বাড়ি","বারি"],"ghor":["ঘর","ঘরে"],"boi":["বই","বইয়ের"],"kotha":["কথা","কথায়"],"shune":["শুনে","শুনেছি"],"shunbo":["শুনব","শুনবো"],"shunte":["শুনতে"],"shuni":["শুনি"],"dekhe":["দেখে","দেখেছি"],"dekha":["দেখা","দেখার"],"dekho":["দেখো","দেখ"],"dekhi":["দেখি"],"dekhte":["দেখতে"],"ashun":["আসুন"],"ashbo":["আসব","আসবো"],"ashchi":["আসছি"],"aschi":["আসছি"],"ashlam":["আসলাম"],"jao":["যাও"],"jai":["যাই"],"jacchi":["যাচ্ছি"],"jabo":["যাব","যাবো"],"gelam":["গেলাম"],"geche":["গেছে","গিয়েছে"],"korbo":["করব","করবো"],"korchi":["করছি"],"kori":["করি"],"koro":["করো","কর"],"korben":["করবেন"],"kore":["করে","করেছি"],"korechi":["করেছি"],"shokal":["সকাল","সকালে"],"sokal":["সকাল"],"dupur":["দুপুর","দুপুরে"],"shondha":["সন্ধ্যা","সন্ধ্যায়"],"sondha":["সন্ধ্যা"],"rat":["রাত","রাতে"],"din":["দিন","দিনে"],"mash":["মাস","মাসে"],"bochor":["বছর","বছরে"],"mon":["মন","মনে"],"mone":["মনে","মনেও"],"janina":["জানিনা","জানি না"],"parina":["পারিনা","পারি না"],"pari":["পারি"],"parbo":["পারব","পারবো"],"parbe":["পারবে"],"bhabchi":["ভাবছি"],"bhabi":["ভাবি"],"bhabo":["ভাবো","ভাব"],"bhabte":["ভাবতে"],"khub":["খুব","খুবি"],"shotti":["সত্যি","সত্য"],"sotti":["সত্যি"],"shothik":["সঠিক"],"sothik":["সঠিক"],"bhul":["ভুল","ভুলে"],"thik":["ঠিক","ঠিকই"],"shohoj":["সহজ","সহজে"],"sohoj":["সহজ"],"kothin":["কঠিন"],"shobcheye":["সবচেয়ে","সবচাইতে"],"sobcheye":["সবচেয়ে"],"prothom":["প্রথম","প্রথমে"],"shesh":["শেষ","শেষে"],"shuru":["শুরু","শুরুতে"],"suru":["শুরু"],"subheccha":["শুভেচ্ছা"],"shubheccha":["শুভেচ্ছা"],"shuvo":["শুভ"],"shubh":["শুভ"],"computer":["কম্পিউটার"],"keyboard":["কীবোর্ড","কিবোর্ড"],"software":["সফটওয়্যার","সফটওয়্যার"],"native":["নেটিভ"],"apple":["অ্যাপল","আপেল"],"silicon":["সিলিকন"],"mac":["ম্যাক"],"macos":["ম্যাকওএস"],"macbook":["ম্যাকবুক"],"font":["ফন্ট"],"download":["ডাউনলোড"],"install":["ইনস্টল"],"free":["ফ্রি"],"typing":["টাইপিং"],"bengali":["বাঙালি","বেঙ্গলি"],"mehedi":["মেহেদি","মেহেদী"],"shakeel":["শাকিল","শাকীল"]}')

def _conditional_lowercase(c: str) -> str:
    lc = c.lower()
    return c if lc in ('o', 'i', 'u', 'd', 'g', 'j', 'n', 'r', 's', 't', 'y', 'z') else lc

def _is_vowel(c: str) -> bool:
    return c in 'aeiouAEIOU'

def _is_consonant(c: str) -> bool:
    return not _is_vowel(c) and (('a' <= c <= 'z') or ('A' <= c <= 'Z'))

def _is_punctuation(c: str) -> bool:
    return not (('a' <= c <= 'z') or ('A' <= c <= 'Z'))

def _is_digit(c: str) -> bool:
    return '0' <= c <= '9'

def _match_condition(cond: dict, prefix: str, suffix: str) -> bool:
    is_prefix = cond['type'] in ('PrefixIs', 'PrefixIsNot')
    is_pos = cond['type'] in ('PrefixIs', 'SuffixIs')
    ch = prefix if is_prefix else suffix
    scope = cond['scope']
    if scope == 'Vowel': ok = _is_vowel(ch)
    elif scope == 'Consonant': ok = _is_consonant(ch)
    elif scope == 'Punctuation': ok = _is_punctuation(ch)
    elif scope == 'Number': ok = _is_digit(ch)
    elif scope == 'Char': ok = (ch == cond.get('val'))
    else: ok = False
    return ok if is_pos else not ok

def _get_replacement(pat: dict, prefix: str, suffix: str) -> str:
    for rule in pat.get('rules', []):
        if all(_match_condition(m, prefix, suffix) for m in rule.get('matches', [])):
            return rule['replace']
    return pat['default']

class AvroPhoneticEngine:
    def __init__(self):
        self.patterns = AVRO_PATTERNS_DATA
        self.dictionary = AVRO_DICTIONARY_DATA

    def convert_word(self, raw: str) -> str:
        if not raw:
            return ''
        inp = ''.join(_conditional_lowercase(c) for c in raw)
        prefix = ' '
        out = ''
        i = 0
        n = len(inp)
        while i < n:
            sub = inp[i:]
            matched = None
            for p in self.patterns:
                if sub.startswith(p['find']):
                    matched = p
                    break
            if matched:
                flen = len(matched['find'])
                suffix = inp[i + flen] if (i + flen < n) else ' '
                out += _get_replacement(matched, prefix, suffix)
                prefix = matched['find'][-1]
                i += flen
            else:
                prefix = inp[i]
                out += prefix
                i += 1
        return out

    def get_candidates(self, word: str) -> list:
        cleaned = (word or '').strip()
        if not cleaned:
            return []
        lower = cleaned.lower()
        cands = []
        seen = set()
        def add(c):
            if c and c not in seen:
                seen.add(c)
                cands.append(c)

        if lower in self.dictionary:
            for c in self.dictionary[lower]:
                add(c)
        rule_based = self.convert_word(cleaned)
        add(rule_based)
        if len(cands) < 5 and cleaned:
            add(self.convert_word(cleaned[0].upper() + cleaned[1:]))
        if len(cands) < 5:
            if 'sh' in lower:
                add(self.convert_word(re.sub(r'sh', 's', cleaned, flags=re.IGNORECASE)))
            elif 's' in lower:
                add(self.convert_word(re.sub(r's', 'sh', cleaned, flags=re.IGNORECASE)))
        add(cleaned)
        return cands[:5]

BENGALI_DIGITS = {
    "0": "০", "1": "১", "2": "২", "3": "৩", "4": "৪",
    "5": "৫", "6": "৬", "7": "৭", "8": "৮", "9": "৯",
}


# Virtual-Key and Hook Constants (defined globally for cross-platform headless testing)
WH_KEYBOARD_LL = 13
WM_KEYDOWN = 0x0100
WM_KEYUP = 0x0101
WM_SYSKEYDOWN = 0x0104
WM_SYSKEYUP = 0x0105

VK_BACK = 0x08
VK_TAB = 0x09
VK_RETURN = 0x0D
VK_SHIFT = 0x10
VK_CONTROL = 0x11
VK_MENU = 0x12  # Alt
VK_ESCAPE = 0x1B
VK_SPACE = 0x20
VK_F12 = 0x7B

INPUT_KEYBOARD = 1
KEYEVENTF_KEYUP = 0x0002
KEYEVENTF_UNICODE = 0x0004
LLKHF_INJECTED = 0x00000010

# Win32 Structures and API Functions for Keyboard Injection
if sys.platform == "win32":
    user32 = ctypes.windll.user32
    kernel32 = ctypes.windll.kernel32

    # Accurate 64-bit Win32 types
    wintypes.HMODULE = ctypes.c_void_p
    wintypes.HHOOK = ctypes.c_void_p

    class MOUSEINPUT(ctypes.Structure):
        _fields_ = [
            ("dx", wintypes.LONG),
            ("dy", wintypes.LONG),
            ("mouseData", wintypes.DWORD),
            ("dwFlags", wintypes.DWORD),
            ("time", wintypes.DWORD),
            ("dwExtraInfo", ctypes.c_size_t),
        ]

    class KEYBDINPUT(ctypes.Structure):
        _fields_ = [
            ("wVk", wintypes.WORD),
            ("wScan", wintypes.WORD),
            ("dwFlags", wintypes.DWORD),
            ("time", wintypes.DWORD),
            ("dwExtraInfo", ctypes.c_size_t),
        ]

    class HARDWAREINPUT(ctypes.Structure):
        _fields_ = [
            ("uMsg", wintypes.DWORD),
            ("wParamL", wintypes.WORD),
            ("wParamH", wintypes.WORD),
        ]

    class INPUT(ctypes.Structure):
        class _INPUT_UNION(ctypes.Union):
            _fields_ = [
                ("mi", MOUSEINPUT),
                ("ki", KEYBDINPUT),
                ("hi", HARDWAREINPUT),
            ]
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
            ("dwExtraInfo", ctypes.c_size_t),
        ]

    HOOKPROC = ctypes.WINFUNCTYPE(ctypes.c_ssize_t, ctypes.c_int, wintypes.WPARAM, wintypes.LPARAM)

    # Win32 API Explicit Signatures (prevents 64-bit handle truncation / error 126)
    kernel32.GetModuleHandleW.argtypes = [wintypes.LPCWSTR]
    kernel32.GetModuleHandleW.restype = wintypes.HMODULE

    kernel32.SetConsoleCtrlHandler.argtypes = [ctypes.c_void_p, wintypes.BOOL]
    kernel32.SetConsoleCtrlHandler.restype = wintypes.BOOL

    user32.SetWindowsHookExW.argtypes = [ctypes.c_int, HOOKPROC, wintypes.HMODULE, wintypes.DWORD]
    user32.SetWindowsHookExW.restype = wintypes.HHOOK

    user32.CallNextHookEx.argtypes = [wintypes.HHOOK, ctypes.c_int, wintypes.WPARAM, wintypes.LPARAM]
    user32.CallNextHookEx.restype = ctypes.c_ssize_t

    user32.UnhookWindowsHookEx.argtypes = [wintypes.HHOOK]
    user32.UnhookWindowsHookEx.restype = wintypes.BOOL

    user32.SendInput.argtypes = [wintypes.UINT, ctypes.c_void_p, ctypes.c_int]
    user32.SendInput.restype = wintypes.UINT

    user32.PostQuitMessage.argtypes = [ctypes.c_int]
    user32.PostQuitMessage.restype = None

    user32.GetKeyState.argtypes = [ctypes.c_int]
    user32.GetKeyState.restype = ctypes.c_short


def replace_inline_text(erase_count: int, new_text: str):
    """Atomically erases erase_count characters and injects new_text in one SendInput batch."""
    if sys.platform != "win32":
        return

    inputs = []
    # 1. Backspaces to erase previously injected text
    for _ in range(erase_count):
        down = INPUT()
        down.type = INPUT_KEYBOARD
        down.ki.wVk = VK_BACK
        down.ki.wScan = 0
        down.ki.dwFlags = 0
        down.ki.time = 0
        down.ki.dwExtraInfo = 0
        inputs.append(down)

        up = INPUT()
        up.type = INPUT_KEYBOARD
        up.ki.wVk = VK_BACK
        up.ki.wScan = 0
        up.ki.dwFlags = KEYEVENTF_KEYUP
        up.ki.time = 0
        up.ki.dwExtraInfo = 0
        inputs.append(up)

    # 2. Unicode characters for new text
    for ch in new_text:
        cp = ord(ch)
        if cp <= 0xFFFF:
            code_units = [cp]
        else:
            cp -= 0x10000
            code_units = [0xD800 + (cp >> 10), 0xDC00 + (cp & 0x3FF)]
        for cu in code_units:
            down = INPUT()
            down.type = INPUT_KEYBOARD
            down.ki.wVk = 0
            down.ki.wScan = cu
            down.ki.dwFlags = KEYEVENTF_UNICODE
            down.ki.time = 0
            down.ki.dwExtraInfo = 0
            inputs.append(down)

            up = INPUT()
            up.type = INPUT_KEYBOARD
            up.ki.wVk = 0
            up.ki.wScan = cu
            up.ki.dwFlags = KEYEVENTF_UNICODE | KEYEVENTF_KEYUP
            up.ki.time = 0
            up.ki.dwExtraInfo = 0
            inputs.append(up)

    if inputs:
        n = len(inputs)
        arr = (INPUT * n)(*inputs)
        return user32.SendInput(n, ctypes.byref(arr), ctypes.sizeof(INPUT))


def send_unicode_string(text: str):
    """Injects a Unicode string into the active window using SendInput."""
    replace_inline_text(0, text)


class BongoWindowsService:
    def __init__(self):
        self.settings = load_settings()
        self._settings_mtime = 0

        self.bangla_active = True  # Active by default on start; toggled via F12
        self.phonetic_engine = AvroPhoneticEngine()
        self.native = BongoNativeBinding()
        self.engine_ptr = self.native.lib.bongo_engine_new() if (self.native and self.native.lib) else None

        self.buffer = ""
        self.candidates = []
        self.preedit_str = ""
        self.last_injected_len = 0

        self.hook_id = None
        self.hook_proc_ref = None

    def toggle_mode(self):
        if self.has_session():
            self.commit_candidate(0)
        self.bangla_active = not self.bangla_active
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
            try:
                cp = ord(ch)
                modifier = 1 if ch.isupper() else 0
                sugg_ptr = self.native.lib.bongo_engine_feed_char(self.engine_ptr, cp, modifier, 0)
                if sugg_ptr:
                    self.update_suggestions(sugg_ptr)
                    self.native.lib.bongo_suggestion_free(sugg_ptr)
                    self._sync_inline_text()
                    return True
            except Exception:
                pass

        # Built-in Avro phonetic transliteration
        self.candidates = self.phonetic_engine.get_candidates(self.buffer)
        self.preedit_str = self.candidates[0] if self.candidates else self.buffer
        self._sync_inline_text()
        return True

    def _sync_inline_text(self):
        target = self.preedit_str or (self.candidates[0] if self.candidates else "")
        replace_inline_text(self.last_injected_len, target)
        self.last_injected_len = len(target)

    def update_suggestions(self, sugg_ptr):
        count = self.native.lib.bongo_suggestion_count(sugg_ptr)
        self.candidates = []
        for i in range(count):
            c_ptr = self.native.lib.bongo_suggestion_get_candidate(sugg_ptr, i)
            if c_ptr:
                c_str = ctypes.string_at(c_ptr).decode("utf-8", errors="replace")
                self.candidates.append(c_str)
                self.native.lib.bongo_string_free(c_ptr)

        p_ptr = self.native.lib.bongo_suggestion_get_preview(sugg_ptr)
        if p_ptr:
            self.preedit_str = ctypes.string_at(p_ptr).decode("utf-8", errors="replace")
            self.native.lib.bongo_string_free(p_ptr)
        elif self.candidates:
            self.preedit_str = self.candidates[0]
        else:
            self.preedit_str = ""

    def handle_backspace(self):
        if self.engine_ptr and self.native and self.native.lib:
            try:
                sugg_ptr = self.native.lib.bongo_engine_backspace(self.engine_ptr, False)
                if sugg_ptr:
                    self.update_suggestions(sugg_ptr)
                    self.native.lib.bongo_suggestion_free(sugg_ptr)
                    self._sync_inline_text()
                    return
                else:
                    self.clear_session()
                    return
            except Exception:
                pass

        if self.buffer:
            self.buffer = self.buffer[:-1]
            if not self.buffer:
                self.clear_session()
            else:
                self.candidates = self.phonetic_engine.get_candidates(self.buffer)
                self.preedit_str = self.candidates[0] if self.candidates else self.buffer
                self._sync_inline_text()

    def commit_candidate(self, index=0):
        if self.engine_ptr and self.native and self.native.lib:
            self.native.lib.bongo_engine_commit(self.engine_ptr, index)
        # Inline text is already on screen, so simply finalize session state
        self.buffer = ""
        self.candidates = []
        self.preedit_str = ""
        self.last_injected_len = 0

    def clear_session(self):
        """Erases any actively composed inline text and resets engine state."""
        if self.last_injected_len > 0:
            replace_inline_text(self.last_injected_len, "")
            self.last_injected_len = 0
        self.reset_session()

    def reset_session(self):
        if self.engine_ptr and self.native and self.native.lib:
            self.native.lib.bongo_engine_reset(self.engine_ptr)
        self.buffer = ""
        self.candidates = []
        self.preedit_str = ""
        self.last_injected_len = 0

    def process_key(self, vk_code: int, is_down: bool, is_shift: bool, is_ctrl: bool, is_alt: bool) -> bool:
        """
        Returns True if the keystroke was handled/swallowed,
        or False to let it pass through to the system.
        """
        if not is_down:
            return False

        # Toggle shortcut (F12)
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

        # In-session navigation and candidate selection
        if self.has_session():
            if vk_code == VK_ESCAPE:
                self.clear_session()
                return True

            if vk_code == VK_SPACE:
                self.commit_candidate(0)
                send_unicode_string(" ")
                return True

            if vk_code == VK_RETURN:
                # Commits active candidate cleanly without inserting an extra newline
                self.commit_candidate(0)
                return True

            if vk_code == VK_BACK:
                self.handle_backspace()
                return True

            # Number keys 1-9 for candidate selection if multiple candidates exist (and Shift is not held)
            if not is_shift and 0x31 <= vk_code <= 0x39:
                idx = vk_code - 0x31
                if idx < len(self.candidates) and len(self.candidates) > 1:
                    chosen = self.candidates[idx]
                    replace_inline_text(self.last_injected_len, chosen)
                    self.last_injected_len = len(chosen)
                    self.commit_candidate(idx)
                    return True
                else:
                    # Single candidate or index out of range: commit word and type Bengali numeral
                    self.commit_candidate(0)
                    digit_char = chr(vk_code)
                    bn_digit = BENGALI_DIGITS.get(digit_char, digit_char)
                    send_unicode_string(bn_digit)
                    return True

            # Digit 0 inside session: commit word and type Bengali numeral '০'
            if not is_shift and vk_code == 0x30:
                self.commit_candidate(0)
                send_unicode_string("০")
                return True

            # Period key inside session: commit word and type Bengali Dari (।)
            if not is_shift and vk_code == 0xBE:
                self.commit_candidate(0)
                send_unicode_string("।")
                return True

        # Digits 0-9 outside session: convert to Bengali numerals
        if not self.has_session() and not is_shift and 0x30 <= vk_code <= 0x39:
            digit_char = chr(vk_code)
            bn_digit = BENGALI_DIGITS.get(digit_char, digit_char)
            send_unicode_string(bn_digit)
            return True

        # Period outside session converts to Bengali Dari (।)
        if not self.has_session() and not is_shift and vk_code == 0xBE:
            send_unicode_string("।")
            return True

        # Backtick / tilde key (used in Avro phonetic rules, e.g. o`, a`, t``)
        if vk_code == 0xC0 and not is_shift:
            self.feed_character("`")
            return True

        # Process standard alphabet characters (A-Z)
        if 0x41 <= vk_code <= 0x5A:
            char_code = vk_code + (0 if is_shift else 32)
            ch = chr(char_code)
            self.feed_character(ch)
            return True

        # Any unhandled key while in session: commit candidate before letting key pass through
        # (prevents backspacing over punctuation or navigation events on subsequent keystrokes)
        if self.has_session():
            self.commit_candidate(0)
            return False

        return False

    def run_windows_hook(self):
        """Sets up Windows low-level keyboard hook and message loop."""
        if sys.platform != "win32":
            sys.stdout.write("Bongo Service: Windows environment required for hook loop.\n")
            return

        def low_level_keyboard_proc(n_code, w_param, l_param):
            try:
                if n_code >= 0:
                    hook_data = ctypes.cast(l_param, ctypes.POINTER(KBDLLHOOKSTRUCT)).contents

                    # Immediately pass through keystrokes injected by SendInput (prevents recursion)
                    if hook_data.flags & LLKHF_INJECTED:
                        return user32.CallNextHookEx(self.hook_id, n_code, w_param, l_param)

                    is_down = w_param in (WM_KEYDOWN, WM_SYSKEYDOWN)
                    vk = hook_data.vkCode

                    is_shift = bool(user32.GetKeyState(VK_SHIFT) & 0x8000)
                    is_ctrl = bool(user32.GetKeyState(VK_CONTROL) & 0x8000)
                    is_alt = bool(user32.GetKeyState(VK_MENU) & 0x8000)

                    handled = self.process_key(vk, is_down, is_shift, is_ctrl, is_alt)
                    if handled:
                        return 1  # Swallow event
            except (KeyboardInterrupt, SystemExit):
                user32.PostQuitMessage(0)
                return 0
            except Exception:
                pass

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
            err_code = kernel32.GetLastError()
            sys.stderr.write(f"Error: Failed to install low-level keyboard hook (code {err_code}).\n")
            return

        def console_ctrl_handler(ctrl_type):
            user32.PostQuitMessage(0)
            return 1

        PHANDLER_ROUTINE = ctypes.WINFUNCTYPE(wintypes.BOOL, wintypes.DWORD)
        self._ctrl_handler_ref = PHANDLER_ROUTINE(console_ctrl_handler)
        kernel32.SetConsoleCtrlHandler(self._ctrl_handler_ref, True)

        sys.stdout.write("Bongo Windows Service active. Press F12 to toggle Bangla/English.\n")
        sys.stdout.flush()

        try:
            msg = wintypes.MSG()
            while user32.GetMessageW(ctypes.byref(msg), 0, 0, 0) > 0:
                user32.TranslateMessage(ctypes.byref(msg))
                user32.DispatchMessageW(ctypes.byref(msg))
        finally:
            if self.hook_id:
                user32.UnhookWindowsHookEx(self.hook_id)
                self.hook_id = None


def check_single_instance():
    """Ensures only a single instance of the Bongo Windows service runs concurrently."""
    if sys.platform != "win32":
        return None
    mutex_name = "Global\\BongoWindowsServiceMutex"
    kernel32.CreateMutexW.argtypes = [ctypes.c_void_p, wintypes.BOOL, wintypes.LPCWSTR]
    kernel32.CreateMutexW.restype = ctypes.c_void_p
    h_mutex = kernel32.CreateMutexW(None, True, mutex_name)
    ERROR_ALREADY_EXISTS = 183
    if kernel32.GetLastError() == ERROR_ALREADY_EXISTS:
        return None
    return h_mutex


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

    if sys.platform == "win32":
        mutex = check_single_instance()
        if mutex is None:
            sys.stdout.write("Bongo Windows Service is already running.\n")
            sys.stdout.flush()
            return

    service = BongoWindowsService()
    if sys.platform == "win32":
        service.run_windows_hook()
    else:
        sys.stdout.write("Bongo Windows Service initialized in headless cross-platform mode.\n")


if __name__ == "__main__":
    main()

