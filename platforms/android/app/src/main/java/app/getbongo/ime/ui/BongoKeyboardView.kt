package app.getbongo.ime.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import app.getbongo.ime.model.UserPreferences

/**
 * On-screen soft keyboard view for Bongo Android IME.
 * Sized and styled with comfortable touch targets matching modern keyboards (like Ridmik / Gboard),
 * clear visual feedback, accent keys, Bengali punctuation, and emoji trigger.
 */
class BongoKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val userPrefs = UserPreferences(context)
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    var isShifted = false
        private set
    var isBangla = true
        private set
    var isSymbols = false
        private set

    var onKeyClickListener: ((KeyAction) -> Unit)? = null

    sealed class KeyAction {
        data class CharacterKey(val ch: Char) : KeyAction()
        object Shift : KeyAction()
        object Backspace : KeyAction()
        object Space : KeyAction()
        object Return : KeyAction()
        object ToggleLanguage : KeyAction()
        object ToggleSymbols : KeyAction()
        object ToggleEmoji : KeyAction()
    }

    init {
        orientation = VERTICAL
        setBackgroundColor(Color.parseColor("#121214")) // Sleek modern dark surface
        setPadding(dp(3), dp(6), dp(3), dp(8))
        buildKeyboard()
    }

    fun setBanglaMode(active: Boolean) {
        if (isBangla != active) {
            isBangla = active
            buildKeyboard()
        }
    }

    fun setShift(shifted: Boolean) {
        if (isShifted != shifted) {
            isShifted = shifted
            buildKeyboard()
        }
    }

    fun toggleShift() {
        setShift(!isShifted)
    }

    private fun performHaptic() {
        if (userPrefs.hapticFeedback) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(18)
                }
            } catch (_: Exception) {}
        }
    }

    fun buildKeyboard() {
        removeAllViews()

        if (isSymbols) {
            buildSymbolsKeyboard()
        } else {
            buildLetterKeyboard()
        }
    }

    private fun buildLetterKeyboard() {
        // Row 1: 10 keys
        val r1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
        addRow(r1, hasSideSpacers = false)

        // Row 2: 9 keys (centered with 0.5f spacers on each side so keys match row 1 width!)
        val r2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
        addRow(r2, hasSideSpacers = true)

        // Row 3: Shift + 7 keys + Backspace
        val r3 = listOf("SHIFT", "z", "x", "c", "v", "b", "n", "m", "BACKSPACE")
        addRow(r3, hasSideSpacers = false)

        // Row 4: Bottom navigation bar
        val r4 = listOf("?123", "EMOJI", "LANG", "SPACE", if (isBangla) "।" else ".", "RETURN")
        addRow(r4, hasSideSpacers = false)
    }

    private fun buildSymbolsKeyboard() {
        // Numbers
        val r1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
        addRow(r1, hasSideSpacers = false)

        // Symbols Row 1
        val r2 = listOf("@", "#", "৳", "$", "%", "&", "-", "+", "(", ")")
        addRow(r2, hasSideSpacers = false)

        // Symbols Row 2
        val r3 = listOf("SYM_MORE", "*", "\"", "'", ":", ";", "!", "?", "BACKSPACE")
        addRow(r3, hasSideSpacers = false)

        // Bottom row
        val r4 = listOf("ABC", "EMOJI", "LANG", "SPACE", "।", "RETURN")
        addRow(r4, hasSideSpacers = false)
    }

    private fun addRow(keys: List<String>, hasSideSpacers: Boolean) {
        val rowLayout = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f)
            gravity = Gravity.CENTER_VERTICAL
        }

        if (hasSideSpacers) {
            val leftSpacer = View(context).apply {
                layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 0.5f)
            }
            rowLayout.addView(leftSpacer)
        }

        for (key in keys) {
            val keyView = createKeyView(key)
            rowLayout.addView(keyView)
        }

        if (hasSideSpacers) {
            val rightSpacer = View(context).apply {
                layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 0.5f)
            }
            rowLayout.addView(rightSpacer)
        }

        addView(rowLayout)
    }

    private fun getKeyWeight(key: String): Float = when (key) {
        "SPACE" -> 4.0f
        "RETURN" -> 1.55f
        "SHIFT", "BACKSPACE" -> 1.45f
        "?123", "ABC" -> 1.25f
        "LANG", "EMOJI" -> 1.0f
        "SYM_MORE" -> 1.2f
        else -> 1.0f
    }

    private fun createKeyView(key: String): View {
        val tv = TextView(context).apply {
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, getKeyTextSize(key))
            typeface = if (isSpecialKey(key)) Typeface.DEFAULT else Typeface.DEFAULT_BOLD
            setTextColor(getKeyTextColor(key))
            background = createKeyBackground(key)

            val weight = getKeyWeight(key)
            val lp = LayoutParams(0, LayoutParams.MATCH_PARENT, weight).apply {
                setMargins(dp(2), dp(3), dp(2), dp(3))
            }
            layoutParams = lp

            text = when (key) {
                "SHIFT" -> if (isShifted) "⇪" else "⇧"
                "BACKSPACE" -> "⌫"
                "RETURN" -> "⏎"
                "SPACE" -> if (isBangla) "বাং • Avro" else "English"
                "LANG" -> "🌐"
                "EMOJI" -> "😊"
                "?123" -> "?123"
                "ABC" -> "ABC"
                "SYM_MORE" -> "=/<"
                else -> if (isShifted) key.uppercase() else key
            }

            setOnClickListener {
                performHaptic()
                handleKeyClick(key)
            }
        }
        return tv
    }

    private fun getKeyTextSize(key: String): Float = when (key) {
        "SPACE" -> 14f
        "SHIFT", "BACKSPACE", "RETURN" -> 18f
        "?123", "ABC", "SYM_MORE" -> 14f
        "LANG", "EMOJI" -> 18f
        else -> 21f
    }

    private fun getKeyTextColor(key: String): Int = when (key) {
        "RETURN" -> Color.WHITE
        "SHIFT" -> if (isShifted) Color.parseColor("#818cf8") else Color.WHITE
        "SPACE" -> if (isBangla) Color.parseColor("#e4e4e7") else Color.parseColor("#a1a1aa")
        "?123", "ABC", "SYM_MORE" -> Color.parseColor("#d4d4d8")
        else -> Color.WHITE
    }

    private fun isSpecialKey(key: String): Boolean =
        key in listOf("SHIFT", "BACKSPACE", "RETURN", "?123", "ABC", "LANG", "EMOJI", "SYM_MORE")

    private fun createKeyBackground(key: String): StateListDrawable {
        val normalColor = when (key) {
            "RETURN" -> Color.parseColor("#4f46e5") // Indigo accent
            "SPACE" -> Color.parseColor("#222329")
            "SHIFT" -> if (isShifted) Color.parseColor("#2e2f3d") else Color.parseColor("#1a1b20")
            else -> if (isSpecialKey(key)) Color.parseColor("#1a1b20") else Color.parseColor("#24252c")
        }

        val pressedColor = when (key) {
            "RETURN" -> Color.parseColor("#4338ca")
            else -> Color.parseColor("#3b3d49")
        }

        val normal = GradientDrawable().apply {
            cornerRadius = dp(7).toFloat()
            setColor(normalColor)
            if (key == "RETURN") {
                // Subtle glow on return key
            } else if (!isSpecialKey(key)) {
                setStroke(dp(1), Color.parseColor("#2d2e37"))
            }
        }

        val pressed = GradientDrawable().apply {
            cornerRadius = dp(7).toFloat()
            setColor(pressedColor)
        }

        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), pressed)
            addState(intArrayOf(), normal)
        }
    }

    private fun handleKeyClick(key: String) {
        when (key) {
            "SHIFT" -> toggleShift()
            "BACKSPACE" -> onKeyClickListener?.invoke(KeyAction.Backspace)
            "SPACE" -> onKeyClickListener?.invoke(KeyAction.Space)
            "RETURN" -> onKeyClickListener?.invoke(KeyAction.Return)
            "EMOJI" -> onKeyClickListener?.invoke(KeyAction.ToggleEmoji)
            "LANG" -> {
                isBangla = !isBangla
                buildKeyboard()
                onKeyClickListener?.invoke(KeyAction.ToggleLanguage)
            }
            "?123" -> {
                isSymbols = true
                buildKeyboard()
                onKeyClickListener?.invoke(KeyAction.ToggleSymbols)
            }
            "ABC" -> {
                isSymbols = false
                buildKeyboard()
                onKeyClickListener?.invoke(KeyAction.ToggleSymbols)
            }
            "SYM_MORE" -> {
                // Could expand to more secondary math symbols if needed
            }
            else -> {
                val ch = if (isShifted) key.uppercase().first() else key.first()
                onKeyClickListener?.invoke(KeyAction.CharacterKey(ch))
                if (isShifted) {
                    setShift(false)
                }
            }
        }
    }

    private fun dp(value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()
}
