package app.getbongo.ime.engine

/**
 * Android Kotlin implementation of Bidirectional Unicode <-> Bijoy 2000 (ANSI) conversion.
 * Conforms to OmicronLab poriborton specifications.
 */
object BijoyConverter {

    private val U2B_MAP: Map<String, String> = mapOf(
        "০" to "0", "১" to "1", "২" to "2", "৩" to "3", "৪" to "4",
        "৫" to "5", "৬" to "6", "৭" to "7", "৮" to "8", "৯" to "9",
        "ৎ" to "r", "ঁ" to "u", "ং" to "s", "ঃ" to "t", "৳" to "$",
        "“" to "Ò", "”" to "Ó", "‘" to "Ô", "’" to "Õ",
        "অ" to "A", "আ" to "Av", "ই" to "B", "ঈ" to "C", "উ" to "D",
        "ঊ" to "E", "ঋ" to "F", "এ" to "G", "ঐ" to "H", "ও" to "I", "ঔ" to "J",
        "ক" to "K", "খ" to "L", "গ" to "M", "ঘ" to "N", "ঙ" to "O",
        "চ" to "P", "ছ" to "Q", "জ" to "R", "ঝ" to "S", "ঞ" to "T",
        "ট" to "U", "ঠ" to "V", "ড" to "W", "ঢ" to "X", "ণ" to "Y",
        "ত" to "Z", "থ" to "_", "দ" to "`", "ধ" to "a", "ন" to "b",
        "প" to "c", "ফ" to "d", "ব" to "e", "ভ" to "f", "ম" to "g",
        "য" to "h", "র" to "i", "ল" to "j", "শ" to "k", "ষ" to "l",
        "স" to "m", "হ" to "n", "ঢ়" to "p", "ড়" to "o", "য়" to "q", "য়" to "q",
        "স্প্র" to "¯cÖ", "স্ত্র" to "¯¿", "স্ত্ব" to "¯—¡", "স্ট্র" to "÷ª", "স্ক্র" to "¯Œ",
        "ষ্প্র" to "®cÖ", "ষ্ট্র" to "óª", "ষ্ক্র" to "®Œ", "ষ্ক্ব" to "®‹¡", "শ্ল" to "k−",
        "ম্ভ্র" to "¤£", "ম্ব্র" to "¤^ª", "ম্প্র" to "¤cÖ", "ন্ধ্র" to "Üª", "ন্দ্র" to "›`ª",
        "ন্দ্ব" to "›Ø", "ন্থ্র" to "š’ª", "ন্ত্র" to "š¿", "ন্ত্ব" to "š—¡", "ন্ড্র" to "Ûª",
        "ন্ট্র" to "›Uª", "ন্স" to "Ý", "দ্ভ্র" to "™£", "দ্দ্ব" to "Ï¡", "ত্ত্র" to "Ë«",
        "ত্ত্ব" to "Ë¡", "ণ্ড্র" to "Êª", "জ্জ্ব" to "¾¡", "চ্ছ্র" to "”Qª", "চ্ছ্ব" to "”Q¡",
        "ঙ্ঘ্র" to "•Nª", "ঙ্ক্ষ" to "•¶", "গ্ধ্র" to "»ª", "ক্ষ্ম" to "²", "ক্ষ্ব" to "¶¡",
        "ক্ষ্ণ" to "¶è", "ক্ত্র" to "³«", "ক্ট্র" to "±ª", "হ্র" to "nª", "হ্ম" to "þ",
        "হ্ব" to "nŸ", "হ্ন" to "ý", "হ্ণ" to "nœ", "হ্ল" to "n¬", "স্ল" to "¯¬",
        "স্র" to "mª", "স্ম" to "¯§", "স্ব" to "¯^", "স্ফ" to "ù", "স্প" to "¯c",
        "স্প্ল" to "¯c−", "স্ন" to "ø", "স্থ" to "¯’", "স্ত" to "¯—", "স্ট" to "÷",
        "স্খ" to "ö", "স্ক" to "¯‹", "ষ্ম" to "®§", "ষ্ব" to "®^", "ষ্ফ" to "õ",
        "ষ্প" to "®c", "ষ্ণ" to "ò", "ষ্ণ্ব" to "ò¡", "ষ্ঠ" to "ô", "ষ্ট" to "ó",
        "ষ্ক" to "®‹", "শ্র" to "kª", "শ্ম" to "k¥", "শ্ব" to "k¦", "শ্ন" to "kœ",
        "শ্ছ" to "ñ", "শ্চ" to "ð", "ল্ল" to "j−", "ল্ম" to "j¥", "ল্ব" to "j¦",
        "ল্ভ" to "j¢", "লপ" to "jè", "লফ" to "jô", "লঘ" to "jN", "লখ" to "jL",
        "লগ" to "jM", "ল্ক" to "jK", "ম্ম" to "¤§", "ম্ব" to "¤^", "ম্ন" to "¤ø",
        "ম্ভ" to "¤¢", "ম্ফ" to "¤d", "ম্প" to "¤c", "ব্ল" to "eø", "ব্র" to "eª",
        "ব্ব" to "eŸ", "ব্দ" to "e`", "ব্জ" to "eR", "ফ্ল" to "d¬", "প্ল" to "c−",
        "প্র" to "cÖ", "প্স" to "cô", "প্ন" to "cø", "প্ত" to "cZ", "প্ট" to "cU",
        "পক্ব" to "cK¡", "ন্ম" to "b¥", "ন্ন" to "bø", "ন্ব" to "b¢", "ন্ধ" to "Ü",
        "ন্থ" to "š’", "ন্ত" to "š—", "ন্দ" to "›`", "ন্ঠ" to "Ú", "ন্ট" to "›U",
        "ঞ্র" to "Tª", "ঞ্ব" to "T¡", "ঞ্ম" to "TÍ", "ঞ্ছ" to "Tç", "ঞ্চ" to "TÁ",
        "ঞ্জ" to "T¾", "ঞ্ঝ" to "T‘", "ণ্ণ" to "Yè", "ণ্ম" to "Y¥", "ণ্ব" to "Y¦",
        "ণ্ঢ" to "ê", "ণ্ঠ" to "È", "ণ্ড" to "Ê", "ঢ্র" to "Xª", "ড্র" to "Wª",
        "দ্ভ" to "™¢", "দ্দ" to "Ï", "দ্ধ" to "×", "দ্ব" to "Ø", "দ্ঘ" to "™a",
        "দ্গ" to "™M", "দ্র" to "`ª", "দ্ম" to "`¥", "ধ্ন" to "aœ", "ধ্ম" to "a¥",
        "ধ্র" to "aª", "ধ্ব" to "aŸ", "ত্ম" to "Z¥", "ত্ন" to "Zœ", "ত্ব" to "Z¡",
        "ত্র" to "Î", "ত্ত" to "Ë", "ত্থ" to "Ì", "ঠ্র" to "Vª", "ট্র" to "Uª",
        "ট্ব" to "U¡", "ট্ম" to "U¥", "ট্ট" to "Æ", "ঝ্র" to "Sª", "জ্ঞ" to "Á",
        "জ্জ" to "¾", "জ্জ্র" to "¾ª", "জ্ব" to "À", "জ্র" to "Rª", "জম" to "R¥",
        "চ্ছ" to "”Q", "চ্চ" to "”P", "ছ্র" to "Qª", "ঙ্ঘ" to "•N", "ঙ্খ" to "•L",
        "ঙ্গ" to "•M", "ঙ্ম" to "•g", "ঙ্ক" to "¼", "ঘ্র" to "Nª", "ঘ্ন" to "Nœ",
        "গ্র" to "MÖ", "গ্ম" to "M¥", "গ্ব" to "M¦", "গ্ন" to "Mœ", "গ্ল" to "M−",
        "গ্ধ" to "»", "খ্র" to "Lª", "ক্স" to "·", "ক্ষ" to "¶", "ক্ল" to "K¬",
        "ক্ন" to "Kè", "ক্র" to "µ", "ক্ম" to "´", "ক্ব" to "K¡", "ক্ত" to "³",
        "ক্ট" to "±", "ক্ক" to "°", "ড়্গ" to "ÿ"
    )

    private const val B_AA_KAR = '\u09be'
    private const val B_OU_KAR = '\u09cc'
    private const val B_I_KAR = '\u09bf'
    private const val B_E_KAR = '\u09c7'
    private const val B_OI_KAR = '\u09c8'
    private const val B_U_KAR = '\u09c1'
    private const val B_UU_KAR = '\u09c2'
    private const val B_RRI_KAR = '\u09c3'
    private const val B_O_KAR = '\u09cb'
    private const val B_HASANTA = '\u09cd'
    private const val B_DARI = '\u0964'
    private const val B_DDARI = '\u0965'
    private const val ZWJ = '\u200d'
    private const val ZWNJ = '\u200c'

    private fun isConsonant(c: Char): Boolean = c in '\u0995'..'\u09b9'
    private fun isVowel(c: Char): Boolean = c in '\u0985'..'\u0994'
    private fun isKar(c: Char): Boolean = c in B_AA_KAR..B_OU_KAR
    private fun isFrontKar(c: Char): Boolean = c == B_I_KAR || c == B_E_KAR || c == B_OI_KAR

    private fun isBaseLineRightChar(c: String): Boolean =
        c in listOf("খ", "গ", "ঘ", "ন", "ণ", "থ", "দ", "ধ", "প", "ব", "ম", "য", "র", "ল", "শ", "ষ", "স", "হ", "য়", "য়")

    private fun isSpecialCombinationWithRFola(s: String): Boolean =
        s in listOf("শ্র", "দ্র", "গ্র", "ত্র", "জ্র", "থ্র", "ধ্র", "প্র", "ব্র", "ভ্র", "ম্র", "স্র", "ন্দ্র", "ম্প্র", "ষ্প্র", "স্প্র")

    private fun isSpecialCombinationWithL(s: String): Boolean =
        s in listOf("গ্ল", "প্ল", "ব্ল", "শ্ল", "স্ল")

    private fun isFrontFacing(s: String): Boolean {
        if (s.isEmpty()) return true
        var encounteredHasanta = false
        var encounteredConsonant = false
        for (i in s.indices.reversed()) {
            val c = s[i]
            if (c == B_HASANTA) {
                encounteredHasanta = true
                continue
            }
            if (isConsonant(c) && encounteredConsonant && !encounteredHasanta) return false
            if (isConsonant(c)) {
                encounteredConsonant = true
                encounteredHasanta = false
                continue
            }
            if (c.isWhitespace()) break
            if (isVowel(c) || isKar(c)) return false
        }
        return true
    }

    private fun replaceKar(kar: Char, front: Boolean, preceding: String): String {
        return when (kar) {
            'া' -> "v"
            'ী' -> "x"
            'ু' -> {
                if (preceding == "র") "“"
                else {
                    val l1 = preceding.takeLast(1)
                    if (l1 == "র") {
                        if (isSpecialCombinationWithRFola(preceding.takeLast(3)) || isSpecialCombinationWithRFola(preceding.takeLast(5))) "“" else "y"
                    } else if (l1 == "ল") {
                        if (isSpecialCombinationWithL(preceding.takeLast(3)) || preceding.takeLast(5) == "স্প্ল") "“" else "y"
                    } else if (l1 == "ণ") {
                        if (preceding.takeLast(3) == "ষ্ণ") "z" else "y"
                    } else if (l1 == "ষ") {
                        if (preceding.takeLast(3) == "ক্ষ") "z" else "y"
                    } else if (isBaseLineRightChar(l1)) "y"
                    else if (l1 == "ড়" || l1 == "ঢ়") "–"
                    else "z"
                }
            }
            'ূ' -> {
                if (preceding == "র") "ƒ"
                else {
                    val l1 = preceding.takeLast(1)
                    if (l1 == "র") {
                        if (isSpecialCombinationWithRFola(preceding.takeLast(3)) || isSpecialCombinationWithRFola(preceding.takeLast(5))) "ƒ" else "~"
                    } else if (l1 == "ল") {
                        if (isSpecialCombinationWithL(preceding.takeLast(3)) || preceding.takeLast(5) == "স্প্ল") "ƒ" else "~"
                    } else if (l1 == "ষ") {
                        if (preceding.takeLast(3) == "ক্ষ") "‚" else "~"
                    } else if (isBaseLineRightChar(l1)) "~"
                    else "‚"
                }
            }
            'ৃ' -> if (isBaseLineRightChar(preceding.takeLast(1))) "„" else "…"
            'ি' -> "w"
            'ে' -> if (front) "†" else "‡"
            'ৈ' -> if (front) "ˆ" else "‰"
            else -> ""
        }
    }

    private fun convertBuffer(buf: StringBuilder): String {
        var s = buf.toString()
        var reph = false
        var zFola = false
        var hasanta = false
        if (s.startsWith("র্")) {
            s = s.substring(2)
            reph = true
        }
        if (s.startsWith("র\u200d")) {
            s = s.take(1) + s.substring(2)
        }
        if (s.endsWith("্য")) {
            s = s.dropLast(2)
            zFola = true
        }
        if (s.endsWith('্')) {
            s = s.dropLast(1)
            hasanta = true
        }
        var out = U2B_MAP[s] ?: s
        if (zFola) out += "¨"
        if (reph) out += "©"
        if (hasanta) out += "&"
        return out
    }

    fun unicodeToBijoy(input: String): String {
        if (input.isEmpty()) return ""
        val text = input.replace("\u09af\u09bc", "য়").replace("\u09a1\u09bc", "ড়").replace("\u09a2\u09bc", "ঢ়")
        val out = StringBuilder()
        val buffer = StringBuilder()
        var encounteredHasanta = false

        for (pos in text.indices) {
            val c = text[pos]
            when {
                c == B_O_KAR -> {
                    out.append(replaceKar('ে', isFrontFacing(text.substring(0, pos)), ""))
                    if (buffer.isNotEmpty()) {
                        out.append(convertBuffer(buffer))
                        buffer.clear()
                    }
                    out.append("v")
                }
                c == B_OU_KAR -> {
                    out.append(replaceKar('ে', isFrontFacing(text.substring(0, pos)), ""))
                    if (buffer.isNotEmpty()) {
                        out.append(convertBuffer(buffer))
                        buffer.clear()
                    }
                    out.append("Š")
                }
                isFrontKar(c) -> {
                    out.append(replaceKar(c, isFrontFacing(text.substring(0, pos)), ""))
                    if (buffer.isNotEmpty()) {
                        out.append(convertBuffer(buffer))
                        buffer.clear()
                    }
                }
                c == B_U_KAR && buffer.toString() == "গ" -> {
                    out.append("¸")
                    buffer.clear()
                }
                c == B_U_KAR && buffer.toString() == "শ" -> {
                    out.append("ï")
                    buffer.clear()
                }
                c == B_U_KAR && buffer.toString() == "হ" -> {
                    out.append("û")
                    buffer.clear()
                }
                c == B_U_KAR && buffer.toString().endsWith("্ত") -> {
                    val conv = convertBuffer(buffer)
                    if (conv.endsWith("—")) {
                        out.append(conv.dropLast(1)).append("‘")
                    } else {
                        out.append(conv)
                    }
                    buffer.clear()
                }
                c == B_RRI_KAR && buffer.toString() == "হ" -> {
                    out.append("ü")
                    buffer.clear()
                }
                isKar(c) -> {
                    val kar = replaceKar(c, false, buffer.toString())
                    if (buffer.isNotEmpty()) {
                        out.append(convertBuffer(buffer))
                        buffer.clear()
                    }
                    out.append(kar)
                }
                c == B_HASANTA -> {
                    encounteredHasanta = true
                    buffer.append(B_HASANTA)
                }
                c == B_DARI || c == '\u09e4' -> {
                    if (buffer.isNotEmpty()) {
                        out.append(convertBuffer(buffer))
                        buffer.clear()
                    }
                    out.append("|")
                }
                c == B_DDARI || c == '\u09e5' -> {
                    if (buffer.isNotEmpty()) {
                        out.append(convertBuffer(buffer))
                        buffer.clear()
                    }
                    out.append("\\")
                }
                c == ZWJ -> buffer.append(ZWJ)
                c == ZWNJ -> {
                    if (buffer.isNotEmpty() && buffer.last() == B_HASANTA) {
                        buffer.deleteCharAt(buffer.length - 1)
                        if (buffer.isNotEmpty()) {
                            out.append(convertBuffer(buffer))
                            buffer.clear()
                        }
                        out.append("&")
                        encounteredHasanta = false
                    }
                }
                encounteredHasanta -> {
                    buffer.append(c)
                    encounteredHasanta = false
                }
                (c in '\u0980'..'\u09ff') || c in listOf('‘', '’', '“', '”') -> {
                    if (buffer.isNotEmpty()) {
                        out.append(convertBuffer(buffer))
                        buffer.clear()
                    }
                    buffer.append(c)
                }
                else -> {
                    if (buffer.isNotEmpty()) {
                        out.append(convertBuffer(buffer))
                        buffer.clear()
                    }
                    out.append(c)
                }
            }
        }
        if (buffer.isNotEmpty()) {
            out.append(convertBuffer(buffer))
            buffer.clear()
        }
        return out.toString()
    }
}
