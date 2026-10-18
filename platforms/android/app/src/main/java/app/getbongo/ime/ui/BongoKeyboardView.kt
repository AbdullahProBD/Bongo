package app.getbongo.ime.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import app.getbongo.ime.model.UserPreferences

/**
 * On-screen soft keyboard view for Bongo Android IME.
 * Renders touch-optimized QWERTY layout with Avro phonetic switching,
 * Bengali numerals, and punctuation.
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
    }

    init {
        orientation = VERTICAL
        setBackgroundColor(Color.parseColor("#18181b"))
        setPadding(dp(4), dp(6), dp(4), dp(8))
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        }
    }

    fun buildKeyboard() {
        removeAllViews()

        val rows = if (isSymbols) {
            listOf(
                listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
                listOf("@", "#", "$", "%", "&", "-", "+", "(", ")"),
                listOf("SYM_TOGGLE", "*", "\"", "'", ":", ";", "!", "?", "BACKSPACE"),
                listOf("ABC", "LANG", "SPACE", "।", "RETURN")
            )
        } else {
            val r1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
            val r2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
            val r3 = listOf("SHIFT", "z", "x", "c", "v", "b", "n", "m", "BACKSPACE")
            val r4 = listOf("?123", "LANG", "SPACE", "।", "RETURN")
            listOf(r1, r2, r3, r4)
        }

        for (rowKeys in rows) {
            val rowLayout = LinearLayout(context).apply {
                orientation = HORIZONTAL
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f)
                weightSum = rowKeys.sumOf { getKeyWeight(it).toDouble() }.toFloat()
            }

            for (key in rowKeys) {
                val keyView = createKeyView(key)
                rowLayout.addView(keyView)
            }
            addView(rowLayout)
        }
    }

    private fun getKeyWeight(key: String): Float = when (key) {
        "SPACE" -> 4.0f
        "SHIFT", "BACKSPACE" -> 1.5f
        "RETURN" -> 1.5f
        "?123", "ABC" -> 1.3f
        "LANG" -> 1.2f
        else -> 1.0f
    }

    private fun createKeyView(key: String): View {
        val tv = TextView(context).apply {
            gravity = Gravity.CENTER
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)

            val normalBg = GradientDrawable().apply {
                cornerRadius = dp(6).toFloat()
                setColor(if (isSpecialKey(key)) Color.parseColor("#202024") else Color.parseColor("#27272a"))
            }
            background = normalBg

            val weight = getKeyWeight(key)
            val lp = LayoutParams(0, LayoutParams.MATCH_PARENT, weight).apply {
                setMargins(dp(3), dp(4), dp(3), dp(4))
            }
            layoutParams = lp

            text = when (key) {
                "SHIFT" -> if (isShifted) "⬆" else "⇧"
                "BACKSPACE" -> "⌫"
                "RETURN" -> "⏎"
                "SPACE" -> if (isBangla) "Bangla" else "English"
                "LANG" -> "🌐"
                "?123" -> "?123"
                "ABC" -> "ABC"
                "SYM_TOGGLE" -> "=/<"
                else -> if (isShifted) key.uppercase() else key
            }

            if (key == "SHIFT" && isShifted) {
                setTextColor(Color.parseColor("#818cf8"))
            }

            setOnClickListener {
                performHaptic()
                handleKeyClick(key)
            }
        }
        return tv
    }

    private fun isSpecialKey(key: String): Boolean =
        key in listOf("SHIFT", "BACKSPACE", "RETURN", "?123", "ABC", "LANG", "SYM_TOGGLE")

    private fun handleKeyClick(key: String) {
        when (key) {
            "SHIFT" -> toggleShift()
            "BACKSPACE" -> onKeyClickListener?.invoke(KeyAction.Backspace)
            "SPACE" -> onKeyClickListener?.invoke(KeyAction.Space)
            "RETURN" -> onKeyClickListener?.invoke(KeyAction.Return)
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
            "SYM_TOGGLE" -> {
                // Future symbol page toggle
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
