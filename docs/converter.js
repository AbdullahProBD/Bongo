/**
 * Bongo · Unicode ↔ Bijoy (ANSI) Converter & Linguistic Utilities for Web
 * Based on OmicronLab poriborton specifications by Dr. Mehdi Hasan Khan.
 * Zero external dependencies, client-side, real-time conversion.
 */
(function (global) {
    'use strict';

    var U2B_MAP = {
        "০": "0", "১": "1", "২": "2", "৩": "3", "৪": "4",
        "৫": "5", "৬": "6", "৭": "7", "৮": "8", "৯": "9",
        "ৎ": "r", "ঁ": "u", "ং": "s", "ঃ": "t", "৳": "$",
        "“": "Ò", "”": "Ó", "‘": "Ô", "’": "Õ",
        "অ": "A", "আ": "Av", "ই": "B", "ঈ": "C", "উ": "D",
        "ঊ": "E", "ঋ": "F", "এ": "G", "ঐ": "H", "ও": "I", "ঔ": "J",
        "ক": "K", "খ": "L", "গ": "M", "ঘ": "N", "ঙ": "O",
        "চ": "P", "ছ": "Q", "জ": "R", "ঝ": "S", "ঞ": "T",
        "ট": "U", "ঠ": "V", "ড": "W", "ঢ": "X", "ণ": "Y",
        "ত": "Z", "থ": "_", "দ": "`", "ধ": "a", "ন": "b",
        "প": "c", "ফ": "d", "ব": "e", "ভ": "f", "ম": "g",
        "য": "h", "র": "i", "ল": "j", "শ": "k", "ষ": "l",
        "স": "m", "হ": "n", "ঢ়": "p", "ড়": "o", "য়": "q", "য়": "q",
        "স্প্র": "¯cÖ", "স্ত্র": "¯¿", "স্ত্ব": "¯—¡", "স্ট্র": "÷ª", "স্ক্র": "¯Œ",
        "ষ্প্র": "®cÖ", "ষ্ট্র": "óª", "ষ্ক্র": "®Œ", "ষ্ক্ব": "®‹¡", "শ্ল": "k−",
        "ম্ভ্র": "¤£", "ম্ব্র": "¤^ª", "ম্প্র": "¤cÖ", "ন্ধ্র": "Üª", "ন্দ্র": "›`ª",
        "ন্দ্ব": "›Ø", "ন্থ্র": "š’ª", "ন্ত্র": "š¿", "ন্ত্ব": "š—¡", "ন্ড্র": "Ûª",
        "ন্ট্র": "›Uª", "ন্স": "Ý", "দ্ভ্র": "™£", "দ্দ্ব": "Ï¡", "ত্ত্র": "Ë«",
        "ত্ত্ব": "Ë¡", "ণ্ড্র": "Êª", "জ্জ্ব": "¾¡", "চ্ছ্র": "”Qª", "চ্ছ্ব": "”Q¡",
        "ঙ্ঘ্র": "•Nª", "ঙ্ক্ষ": "•¶", "গ্ধ্র": "»ª", "ক্ষ্ম": "²", "ক্ষ্ব": "¶¡",
        "ক্ষ্ণ": "¶è", "ক্ত্র": "³«", "ক্ট্র": "±ª", "হ্র": "nª", "হ্ম": "þ",
        "হ্ব": "nŸ", "হ্ন": "ý", "হ্ণ": "nœ", "হ্ল": "n¬", "স্ল": "¯¬",
        "স্র": "mª", "স্ম": "¯§", "স্ব": "¯^", "স্ফ": "ù", "স্প": "¯c",
        "স্প্ল": "¯c−", "স্ন": "ø", "স্থ": "¯’", "স্ত": "¯—", "স্ট": "÷",
        "স্খ": "ö", "স্ক": "¯‹", "ষ্ম": "®§", "ষ্ব": "®^", "ষ্ফ": "õ",
        "ষ্প": "®c", "ষ্ণ": "ò", "ষ্ণ্ব": "ò¡", "ষ্ঠ": "ô", "ষ্ট": "ó",
        "ষ্ক": "®‹", "শ্র": "kª", "শ্ম": "k¥", "শ্ব": "k¦", "শ্ন": "kœ",
        "শ্ছ": "ñ", "শ্চ": "ð", "ল্ল": "j−", "ল্ম": "j¥", "ল্ব": "j¦",
        "ল্ভ": "j¢", "লপ": "jè", "লফ": "jô", "লঘ": "jN", "লখ": "jL",
        "লগ": "jM", "ল্ক": "jK", "ম্ম": "¤§", "ম্ব": "¤^", "ম্ন": "¤ø",
        "ম্ভ": "¤¢", "ম্ফ": "¤d", "ম্প": "¤c", "ব্ল": "eø", "ব্র": "eª",
        "ব্ব": "eŸ", "ব্দ": "e`", "ব্জ": "eR", "ফ্ল": "d¬", "প্ল": "c−",
        "প্র": "cÖ", "প্স": "cô", "প্ন": "cø", "প্ত": "cZ", "প্ট": "cU",
        "পক্ব": "cK¡", "ন্ম": "b¥", "ন্ন": "bø", "ন্ব": "b¢", "ন্ধ": "Ü",
        "ন্থ": "š’", "ন্ত": "š—", "ন্দ": "›`", "ন্ঠ": "Ú", "ন্ট": "›U",
        "ঞ্র": "Tª", "ঞ্ব": "T¡", "ঞ্ম": "TÍ", "ঞ্ছ": "Tç", "ঞ্চ": "TÁ",
        "ঞ্জ": "T¾", "ঞ্ঝ": "T‘", "ণ্ণ": "Yè", "ণ্ম": "Y¥", "ণ্ব": "Y¦",
        "ণ্ঢ": "ê", "ণ্ঠ": "È", "ণ্ড": "Ê", "ঢ্র": "Xª", "ড্র": "Wª",
        "দ্ভ": "™¢", "দ্দ": "Ï", "দ্ধ": "×", "দ্ব": "Ø", "দ্ঘ": "™a",
        "দ্গ": "™M", "দ্র": "`ª", "দ্ম": "`¥", "ধ্ন": "aœ", "ধ্ম": "a¥",
        "ধ্র": "aª", "ধ্ব": "aŸ", "ত্ম": "Z¥", "ত্ন": "Zœ", "ত্ব": "Z¡",
        "ত্র": "Î", "ত্ত": "Ë", "ত্থ": "Ì", "ঠ্র": "Vª", "ট্র": "Uª",
        "ট্ব": "U¡", "ট্ম": "U¥", "ট্ট": "Æ", "ঝ্র": "Sª", "জ্ঞ": "Á",
        "জ্জ": "¾", "জ্জ্র": "¾ª", "জ্ব": "À", "জ্র": "Rª", "জম": "R¥",
        "চ্ছ": "”Q", "চ্চ": "”P", "ছ্র": "Qª", "ঙ্ঘ": "•N", "ঙ্খ": "•L",
        "ঙ্গ": "•M", "ঙ্ম": "•g", "ঙ্ক": "¼", "ঘ্র": "Nª", "ঘ্ন": "Nœ",
        "গ্র": "MÖ", "গ্ম": "M¥", "গ্ব": "M¦", "গ্ন": "Mœ", "গ্ল": "M−",
        "গ্ধ": "»", "খ্র": "Lª", "ক্স": "·", "ক্ষ": "¶", "ক্ল": "K¬",
        "ক্ন": "Kè", "ক্র": "µ", "ক্ম": "´", "ক্ব": "K¡", "ক্ত": "³",
        "ক্ট": "±", "ক্ক": "°", "ড়্গ": "ÿ"
    };

    var BIJOY_CLUSTERS = [
        "¯cÖ", "¯¿", "¯—¡", "÷ª", "¯Œ", "®cÖ", "óª", "®Œ", "®‹¡", "k−", "¤£", "¤^ª", "¤cÖ", "Üª", "›`ª",
        "›Ø", "š’ª", "š¿", "š—¡", "Ûª", "›Uª", "Ý", "™£", "Ï¡", "Ë«", "Ë¡", "Êª", "¾¡", "”Qª", "”Q¡",
        "•Nª", "•¶", "»ª", "²", "¶¡", "¶è", "³«", "±ª", "nª", "þ", "nŸ", "ý", "nœ", "n¬", "¯¬", "mª",
        "¯§", "¯^", "ù", "¯c", "¯c−", "ø", "¯’", "¯—", "÷", "ö", "¯‹", "®§", "®^", "õ", "®c", "ò", "ò¡",
        "ô", "ó", "®‹", "kª", "k¥", "k¦", "kœ", "ñ", "ð", "j−", "j¥", "j¦", "j¢", "jè", "jô", "jN", "jL",
        "jM", "jK", "¤§", "¤^", "¤ø", "¤¢", "¤d", "¤c", "eø", "eª", "eŸ", "e`", "eR", "d¬", "c−", "cÖ",
        "cô", "cø", "cZ", "cU", "cK¡", "b¥", "bø", "b¢", "Ü", "ÜŸ", "š’", "š—", "›`", "›a", "Ú", "›U",
        "Tª", "T¡", "TÍ", "Tç", "TÁ", "T¾", "T‘", "Yè", "Y¥", "Y¦", "ê", "È", "Ê", "Xª", "Wª", "™¢", "Ï",
        "×", "Ø", "™a", "™M", "`ª", "`¥", "aœ", "a¥", "aª", "aŸ", "Z¥", "Zœ", "Z¡", "Z_©", "Î", "Ë", "Ì",
        "Vª", "Uª", "U¡", "U¥", "Æ", "Sª", "Á", "¾", "¾ª", "À", "Rª", "R¥", "R¡", "”Q", "”P", "Qª", "•N",
        "•L", "•M", "•g", "¼", "Nª", "Nœ", "MÖ", "M¥", "M¦", "Mœ", "M−", "»", "Lª", "·", "¶", "K¬", "Kè",
        "µ", "´", "K¡", "³", "±", "°", "ÿ"
    ];

    var B2U_SINGLES = {};
    for (var k in U2B_MAP) {
        if (U2B_MAP.hasOwnProperty(k) && U2B_MAP[k].length === 1) {
            B2U_SINGLES[U2B_MAP[k]] = k;
        }
    }
    B2U_SINGLES["Av"] = "আ";
    B2U_SINGLES["¸"] = "গু"; B2U_SINGLES["ï"] = "শু"; B2U_SINGLES["û"] = "হু"; B2U_SINGLES["ü"] = "হৃ";
    B2U_SINGLES["‘"] = "্তু"; B2U_SINGLES["“"] = "ু"; B2U_SINGLES["”"] = "ু";
    B2U_SINGLES["ƒ"] = "ূ"; B2U_SINGLES["‚"] = "ূ"; B2U_SINGLES["~"] = "ূ";
    B2U_SINGLES["z"] = "ু"; B2U_SINGLES["y"] = "ু"; B2U_SINGLES["–"] = "ু";
    B2U_SINGLES["…"] = "ৃ"; B2U_SINGLES["„"] = "ৃ";
    B2U_SINGLES["v"] = "া"; B2U_SINGLES["x"] = "ী"; B2U_SINGLES["w"] = "ি";
    B2U_SINGLES["†"] = "ে"; B2U_SINGLES["‡"] = "ে"; B2U_SINGLES["ˆ"] = "ৈ"; B2U_SINGLES["‰"] = "ৈ";
    B2U_SINGLES["|"] = "।"; B2U_SINGLES["\\"] = "॥";
    B2U_SINGLES["&"] = "্"; B2U_SINGLES["¨"] = "্য"; B2U_SINGLES["ª"] = "্র";
    B2U_SINGLES["Ö"] = "্র"; B2U_SINGLES["«"] = "্র";

    var B2U_LIGATURES = [];
    for (var uKey in U2B_MAP) {
        if (U2B_MAP.hasOwnProperty(uKey) && U2B_MAP[uKey].length > 1 && uKey !== "আ") {
            B2U_LIGATURES.push({ b: U2B_MAP[uKey], u: uKey });
        }
    }
    B2U_LIGATURES.sort(function (a, b) {
        return b.b.length - a.b.length;
    });

    var CLUSTER_REGEX_STR = "(?:" + BIJOY_CLUSTERS.map(function(s) {
        return s.replace(/[-/\\^$*+?.()|[\]{}]/g, '\\$&');
    }).join("|") + "|[K-Z_`a-no-q])";

    var B_AA_KAR = "\u09be", B_OU_KAR = "\u09cc";
    var B_I_KAR = "\u09bf", B_E_KAR = "\u09c7", B_OI_KAR = "\u09c8";
    var B_U_KAR = "\u09c1", B_UU_KAR = "\u09c2", B_RRI_KAR = "\u09c3";
    var B_O_KAR = "\u09cb", B_HASANTA = "\u09cd", B_DARI = "\u0964", B_DDARI = "\u0965";
    var ZWJ = "\u200d", ZWNJ = "\u200c";

    function isConsonant(c) {
        return c >= "\u0995" && c <= "\u09b9";
    }
    function isVowel(c) {
        return c >= "\u0985" && c <= "\u0994";
    }
    function isKar(c) {
        return c >= B_AA_KAR && c <= B_OU_KAR;
    }
    function isFrontKar(c) {
        return c === B_I_KAR || c === B_E_KAR || c === B_OI_KAR;
    }
    function isBaseLineRightChar(c) {
        return "খগঘনণথদধপবমযরলশষসহয়য়".indexOf(c) !== -1;
    }
    function isSpecialWithRFola(s) {
        return ["শ্র", "দ্র", "গ্র", "ত্র", "জ্র", "থ্র", "ধ্র", "প্র", "ব্র", "ভ্র", "ম্র", "স্র", "ন্দ্র", "ম্প্র", "ষ্প্র", "স্প্র"].indexOf(s) !== -1;
    }
    function isSpecialWithL(s) {
        return ["গ্ল", "প্ল", "ব্ল", "শ্ল", "স্ল"].indexOf(s) !== -1;
    }
    function isFrontFacing(s) {
        if (!s) return true;
        var encHasanta = false;
        var encConsonant = false;
        for (var i = s.length - 1; i >= 0; i--) {
            var c = s[i];
            if (c === B_HASANTA) {
                encHasanta = true;
                continue;
            }
            if (isConsonant(c) && encConsonant && !encHasanta) return false;
            if (isConsonant(c)) {
                encConsonant = true;
                encHasanta = false;
                continue;
            }
            if (/\s/.test(c)) break;
            if (isVowel(c) || isKar(c)) return false;
        }
        return true;
    }

    function replaceKar(kar, front, preceding) {
        if (kar === "া") return "v";
        if (kar === "ী") return "x";
        if (kar === "ু") {
            if (preceding === "র") return "“";
            var l1 = preceding.slice(-1);
            if (l1 === "র") {
                if (isSpecialWithRFola(preceding.slice(-3)) || isSpecialWithRFola(preceding.slice(-5))) return "“";
                return "y";
            }
            if (l1 === "ল") {
                if (isSpecialWithL(preceding.slice(-3)) || preceding.slice(-5) === "স্প্ল") return "“";
                return "y";
            }
            if (l1 === "ণ") return preceding.slice(-3) === "ষ্ণ" ? "z" : "y";
            if (l1 === "ষ") return preceding.slice(-3) === "ক্ষ" ? "z" : "y";
            if (isBaseLineRightChar(l1)) return "y";
            if (l1 === "ড়" || l1 === "ঢ়") return "–";
            return "z";
        }
        if (kar === "ূ") {
            if (preceding === "র") return "ƒ";
            var l1u = preceding.slice(-1);
            if (l1u === "র") {
                if (isSpecialWithRFola(preceding.slice(-3)) || isSpecialWithRFola(preceding.slice(-5))) return "ƒ";
                return "~";
            }
            if (l1u === "ল") {
                if (isSpecialWithL(preceding.slice(-3)) || preceding.slice(-5) === "স্প্ল") return "ƒ";
                return "~";
            }
            if (l1u === "ষ") return preceding.slice(-3) === "ক্ষ" ? "‚" : "~";
            if (isBaseLineRightChar(l1u)) return "~";
            return "‚";
        }
        if (kar === "ৃ") {
            return isBaseLineRightChar(preceding.slice(-1)) ? "„" : "…";
        }
        if (kar === "ি") return "w";
        if (kar === "ে") return front ? "†" : "‡";
        if (kar === "ৈ") return front ? "ˆ" : "‰";
        return "";
    }

    function convertBuffer(buf) {
        var s = buf.join("");
        var reph = false;
        var zFola = false;
        var hasanta = false;
        if (s.indexOf("র্") === 0) {
            s = s.slice(2);
            reph = true;
        }
        if (s.indexOf("র\u200d") === 0) {
            s = s[0] + s.slice(2);
        }
        if (s.slice(-2) === "্য") {
            s = s.slice(0, -2);
            zFola = true;
        }
        if (s.slice(-1) === "্") {
            s = s.slice(0, -1);
            hasanta = true;
        }
        var out = U2B_MAP[s] || s;
        if (zFola) out += "¨";
        if (reph) out += "©";
        if (hasanta) out += "&";
        return out;
    }

    /**
     * Convert Unicode Bengali text to Bijoy 2000 (ANSI)
     */
    function unicodeToBijoy(inputStr) {
        if (!inputStr) return "";
        var text = inputStr.replace(/\u09af\u09bc/g, "য়").replace(/\u09a1\u09bc/g, "ড়").replace(/\u09a2\u09bc/g, "ঢ়");
        var out = [];
        var buffer = [];
        var encounteredHasanta = false;

        for (var pos = 0; pos < text.length; pos++) {
            var c = text[pos];
            if (c === B_O_KAR) {
                out.push(replaceKar("ে", isFrontFacing(text.slice(0, pos)), ""));
                if (buffer.length > 0) {
                    out.push(convertBuffer(buffer));
                    buffer = [];
                }
                out.push("v");
            } else if (c === B_OU_KAR) {
                out.push(replaceKar("ে", isFrontFacing(text.slice(0, pos)), ""));
                if (buffer.length > 0) {
                    out.push(convertBuffer(buffer));
                    buffer = [];
                }
                out.push("Š");
            } else if (isFrontKar(c)) {
                out.push(replaceKar(c, isFrontFacing(text.slice(0, pos)), ""));
                if (buffer.length > 0) {
                    out.push(convertBuffer(buffer));
                    buffer = [];
                }
            } else if (c === B_U_KAR && buffer.join("") === "গ") {
                out.push("¸");
                buffer = [];
            } else if (c === B_U_KAR && buffer.join("") === "শ") {
                out.push("ï");
                buffer = [];
            } else if (c === B_U_KAR && buffer.join("") === "হ") {
                out.push("û");
                buffer = [];
            } else if (c === B_U_KAR && buffer.join("").slice(-2) === "্ত") {
                var conv = convertBuffer(buffer);
                if (conv.slice(-1) === "—") conv = conv.slice(0, -1) + "‘";
                out.push(conv);
                buffer = [];
            } else if (c === B_RRI_KAR && buffer.join("") === "হ") {
                out.push("ü");
                buffer = [];
            } else if (isKar(c)) {
                var bufStr = buffer.join("");
                var karChar = replaceKar(c, false, bufStr);
                if (buffer.length > 0) {
                    out.push(convertBuffer(buffer));
                    buffer = [];
                }
                out.push(karChar);
            } else if (c === B_HASANTA) {
                encounteredHasanta = true;
                buffer.push(B_HASANTA);
            } else if (c === B_DARI || c === "\u09e4") {
                if (buffer.length > 0) {
                    out.push(convertBuffer(buffer));
                    buffer = [];
                }
                out.push("|");
            } else if (c === B_DDARI || c === "\u09e5") {
                if (buffer.length > 0) {
                    out.push(convertBuffer(buffer));
                    buffer = [];
                }
                out.push("\\");
            } else if (c === ZWJ) {
                buffer.push(ZWJ);
            } else if (c === ZWNJ) {
                if (buffer.length > 0 && buffer[buffer.length - 1] === B_HASANTA) {
                    buffer.pop();
                    if (buffer.length > 0) {
                        out.push(convertBuffer(buffer));
                        buffer = [];
                    }
                    out.push("&");
                    encounteredHasanta = false;
                }
            } else if (encounteredHasanta) {
                buffer.push(c);
                encounteredHasanta = false;
            } else if ((c >= "\u0980" && c <= "\u09ff") || c === "‘" || c === "’" || c === "“" || c === "”") {
                if (buffer.length > 0) {
                    out.push(convertBuffer(buffer));
                    buffer = [];
                }
                buffer.push(c);
            } else {
                if (buffer.length > 0) {
                    out.push(convertBuffer(buffer));
                    buffer = [];
                }
                out.push(c);
            }
        }
        if (buffer.length > 0) {
            out.push(convertBuffer(buffer));
            buffer = [];
        }
        return out.join("");
    }

    /**
     * Convert Bijoy 2000 (ANSI) to Unicode Bengali
     */
    function bijoyToUnicode(inputStr) {
        if (!inputStr) return "";
        var s = inputStr;

        var compORe = new RegExp("[†‡](" + CLUSTER_REGEX_STR + ")v", "g");
        s = s.replace(compORe, "$1ো");

        var compOURe = new RegExp("[†‡](" + CLUSTER_REGEX_STR + ")Š", "g");
        s = s.replace(compOURe, "$1ৌ");

        var preIRe = new RegExp("w(" + CLUSTER_REGEX_STR + ")", "g");
        s = s.replace(preIRe, "$1ি");

        var preERe = new RegExp("[†‡](" + CLUSTER_REGEX_STR + ")", "g");
        s = s.replace(preERe, "$1ে");

        var preOIRe = new RegExp("[ˆ‰](" + CLUSTER_REGEX_STR + ")", "g");
        s = s.replace(preOIRe, "$1ৈ");

        var rephRe = new RegExp("(" + CLUSTER_REGEX_STR + ")©", "g");
        s = s.replace(rephRe, "র্$1");

        for (var i = 0; i < B2U_LIGATURES.length; i++) {
            var item = B2U_LIGATURES[i];
            if (s.indexOf(item.b) !== -1) {
                s = s.split(item.b).join(item.u);
            }
        }

        var out = [];
        var idx = 0;
        var n = s.length;
        while (idx < n) {
            if (idx + 1 < n && B2U_SINGLES[s.slice(idx, idx + 2)]) {
                out.push(B2U_SINGLES[s.slice(idx, idx + 2)]);
                idx += 2;
            } else if (B2U_SINGLES[s[idx]]) {
                out.push(B2U_SINGLES[s[idx]]);
                idx += 1;
            } else {
                out.push(s[idx]);
                idx += 1;
            }
        }

        var result = out.join("");
        result = result.replace(/্+/g, "্").replace(/্$/g, "");
        return result;
    }

    /**
     * Separate punctuation padding (iAvro & ibus-avro tokenization)
     */
    function separatePadding(word) {
        if (!word) return { prefix: "", stem: "", suffix: "" };
        var m1 = word.match(/^([\-\]~!@#%&*()_=+[{}'\";<>\/?|.,:]+)/);
        var prefix = m1 ? m1[1] : "";
        var rest = word.slice(prefix.length);
        var m2 = rest.match(/([\-\]~!@#%&*()_=+[{}'\";<>\/?|.,:]+)$/);
        var suffix = m2 ? m2[1] : "";
        var stem = suffix ? rest.slice(0, rest.length - suffix.length) : rest;
        return { prefix: prefix, stem: stem, suffix: suffix };
    }

    /**
     * Case sensitivity normalizer
     */
    function fixStringCase(word) {
        if (!word) return "";
        var caseSensitive = "OIUDGJNRSTYZ";
        var out = [];
        for (var i = 0; i < word.length; i++) {
            var c = word[i];
            if (caseSensitive.indexOf(c) !== -1 || !/[a-zA-Z]/.test(c)) {
                out.push(c);
            } else {
                out.push(c.toLowerCase());
            }
        }
        return out.join("");
    }

    // Export module globally
    global.BongoConverter = {
        unicodeToBijoy: unicodeToBijoy,
        bijoyToUnicode: bijoyToUnicode,
        separatePadding: separatePadding,
        fixStringCase: fixStringCase
    };

})(typeof window !== 'undefined' ? window : this);
