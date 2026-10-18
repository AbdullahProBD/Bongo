package app.getbongo.ime.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import app.getbongo.ime.model.ThemePalette
import app.getbongo.ime.model.UserPreferences

/**
 * On-screen soft keyboard view for Bongo Android IME.
 * Features:
 * - Key Popup Magnifier on touch
 * - Swipe-left from backspace to delete whole words
 * - Permanent top number row (1–0 / ১–০)
 * - Continuous Backspace auto-repeat on hold
 * - Spacebar cursor scrubbing (touch trackpad)
 * - Long-press key hints for numbers and symbols
 * - Haptic and audio click feedback
 * - Dynamic ThemePalette support (Midnight, OLED, Light)
 * - IME picker switcher on long-press
 */
class BongoKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val userPrefs = UserPreferences(context)
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val repeatHandler = Handler(Looper.getMainLooper())

    var isShifted = false
        private set
    var isBangla = true
        private set
    var isSymbols = false
        private set

    private var palette: ThemePalette = ThemePalette.getPalette(userPrefs.theme)

    var onKeyClickListener: ((KeyAction) -> Unit)? = null

    // Popup Magnifier
    private val popupTextView = TextView(context).apply {
        gravity = Gravity.CENTER
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 26f)
        typeface = Typeface.DEFAULT_BOLD
    }
    private val popupWindow = PopupWindow(popupTextView, dp(52), dp(62)).apply {
        isClippingEnabled = false
        isTouchable = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            elevation = dp(8).toFloat()
        }
    }

    sealed class KeyAction {
        data class CharacterKey(val ch: Char) : KeyAction()
        object Shift : KeyAction()
        object Backspace : KeyAction()
        object DeleteWord : KeyAction()
        object Space : KeyAction()
        object Return : KeyAction()
        object ToggleLanguage : KeyAction()
        object ToggleSymbols : KeyAction()
        object ToggleEmoji : KeyAction()
        object SwitchImePicker : KeyAction()
        object CursorLeft : KeyAction()
        object CursorRight : KeyAction()
        object Esc : KeyAction()
        object Tab : KeyAction()
        object Ctrl : KeyAction()
    }

    private companion object {
        val ROW1_HINTS_EN = mapOf('q' to '1', 'w' to '2', 'e' to '3', 'r' to '4', 't' to '5', 'y' to '6', 'u' to '7', 'i' to '8', 'o' to '9', 'p' to '0')
        val ROW1_HINTS_BN = mapOf('q' to '১', 'w' to '২', 'e' to '৩', 'r' to '৪', 't' to '৫', 'y' to '৬', 'u' to '৭', 'i' to '৮', 'o' to '৯', 'p' to '০')
        val ROW2_HINTS = mapOf('a' to '@', 's' to '#', 'd' to '৳', 'f' to '_', 'g' to '&', 'h' to '-', 'j' to '+', 'k' to '(', 'l' to ')')
        val ROW3_HINTS = mapOf('z' to '*', 'x' to '"', 'c' to '\'', 'v' to ':', 'b' to ';', 'n' to '!', 'm' to '?')
    }

    init {
        orientation = VERTICAL
        setBackgroundColor(palette.surfaceColor)
        setPadding(dp(3), dp(4), dp(3), dp(6))
        buildKeyboard()
    }

    fun applyTheme(newPalette: ThemePalette) {
        palette = newPalette
        setBackgroundColor(palette.surfaceColor)
        popupTextView.setTextColor(palette.keyTextColor)
        popupTextView.background = GradientDrawable().apply {
            cornerRadius = dp(10).toFloat()
            setColor(palette.keyNormalColor)
            setStroke(dp(1), palette.stripPrimaryBorder)
        }
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

    private fun playKeySound() {
        if (userPrefs.soundFeedback) {
            try {
                audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
            } catch (_: Exception) {}
        }
    }

    private fun showKeyPopup(anchor: View, text: String) {
        if (!userPrefs.keyPopupPreview || !anchor.isAttachedToWindow || anchor.windowToken == null) return
        try {
            popupTextView.text = text
            popupTextView.setTextColor(palette.keyTextColor)
            popupTextView.background = GradientDrawable().apply {
                cornerRadius = dp(10).toFloat()
                setColor(palette.keyNormalColor)
                setStroke(dp(1), palette.stripPrimaryBorder)
            }

            val loc = IntArray(2)
            anchor.getLocationInWindow(loc)
            val popupX = loc[0] + (anchor.width - dp(52)) / 2
            val popupY = loc[1] - dp(66)

            if (popupWindow.isShowing) {
                popupWindow.update(popupX, popupY, dp(52), dp(62))
            } else {
                popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, popupX, popupY)
            }
        } catch (_: Exception) {}
    }

    private fun hideKeyPopup() {
        try {
            if (popupWindow.isShowing) {
                popupWindow.dismiss()
            }
        } catch (_: Exception) {}
    }

    fun buildKeyboard() {
        hideKeyPopup()
        removeAllViews()

        if (isSymbols) {
            buildSymbolsKeyboard()
        } else {
            buildLetterKeyboard()
        }
    }

    private fun buildLetterKeyboard() {
        // Optional 5th row: Permanent Number Row on top
        if (userPrefs.showNumberRow) {
            val numRow = if (userPrefs.bengaliDigits && isBangla) {
                listOf("১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯", "০")
            } else {
                listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
            }
            addRow(numRow, hasSideSpacers = false, isNumberRow = true)
        }

        // Row 1: 10 keys
        val r1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
        addRow(r1, hasSideSpacers = false, isNumberRow = false)

        // Row 2: 9 keys (centered with 0.5f spacers on each side)
        val r2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l")
        addRow(r2, hasSideSpacers = true, isNumberRow = false)

        // Row 3: Shift + 7 keys + Backspace
        val r3 = listOf("SHIFT", "z", "x", "c", "v", "b", "n", "m", "BACKSPACE")
        addRow(r3, hasSideSpacers = false, isNumberRow = false)

        // Row 4: Bottom navigation bar
        val r4 = listOf("?123", "EMOJI", "LANG", "SPACE", if (isBangla) "।" else ".", "RETURN")
        addRow(r4, hasSideSpacers = false, isNumberRow = false)
    }

    private fun buildSymbolsKeyboard() {
        val r1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
        addRow(r1, hasSideSpacers = false, isNumberRow = false)

        val r2 = listOf("@", "#", "৳", "$", "%", "&", "-", "+", "(", ")")
        addRow(r2, hasSideSpacers = false, isNumberRow = false)

        val r3 = listOf("ESC", "*", "\"", "'", ":", ";", "!", "?", "BACKSPACE")
        addRow(r3, hasSideSpacers = false, isNumberRow = false)

        val r4 = listOf("ABC", "TAB", "EMOJI", "LANG", "SPACE", "।", "RETURN")
        addRow(r4, hasSideSpacers = false, isNumberRow = false)
    }

    private fun addRow(keys: List<String>, hasSideSpacers: Boolean, isNumberRow: Boolean) {
        val rowWeight = if (isNumberRow) 0.82f else 1.0f
        val rowLayout = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0, rowWeight)
            gravity = Gravity.CENTER_VERTICAL
        }

        if (hasSideSpacers) {
            val leftSpacer = View(context).apply {
                layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 0.5f)
            }
            rowLayout.addView(leftSpacer)
        }

        for (key in keys) {
            val keyView = createKeyView(key, isNumberRow)
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
        "ESC", "TAB", "CTRL" -> 1.1f
        else -> 1.0f
    }

    private fun getHintForChar(ch: Char): Char? {
        if (!userPrefs.longPressSymbols || isSymbols) return null
        val lower = ch.lowercaseChar()
        val numHints = if (userPrefs.bengaliDigits && isBangla) ROW1_HINTS_BN else ROW1_HINTS_EN
        // If number row is permanently visible, don't duplicate row 1 number hints
        val row1 = if (userPrefs.showNumberRow) null else numHints[lower]
        return row1 ?: ROW2_HINTS[lower] ?: ROW3_HINTS[lower]
    }

    private fun createKeyView(key: String, isNumberRow: Boolean): View {
        val weight = getKeyWeight(key)
        val lp = LayoutParams(0, LayoutParams.MATCH_PARENT, weight).apply {
            setMargins(dp(2), dp(2), dp(2), dp(2))
        }

        val container = FrameLayout(context).apply {
            layoutParams = lp
            background = createKeyBackground(key)
            isClickable = true
            isFocusable = true
        }

        val displayLabel = when (key) {
            "SHIFT" -> if (isShifted) "⇪" else "⇧"
            "BACKSPACE" -> "⌫"
            "RETURN" -> "⏎"
            "SPACE" -> if (isBangla) "Bongo" else "English"
            "LANG" -> "🌐"
            "EMOJI" -> "😊"
            "?123" -> "?123"
            "ABC" -> "ABC"
            "ESC" -> "Esc"
            "TAB" -> "Tab"
            "CTRL" -> "Ctrl"
            else -> if (isShifted) key.uppercase() else key
        }

        val tv = TextView(context).apply {
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, getKeyTextSize(key, isNumberRow))
            typeface = if (isSpecialKey(key)) Typeface.DEFAULT else Typeface.DEFAULT_BOLD
            setTextColor(getKeyTextColor(key))
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            text = displayLabel
        }
        container.addView(tv)

        // Show superscript symbol hint on letter keys
        if (!isSpecialKey(key) && key.length == 1 && !isNumberRow) {
            val hintChar = getHintForChar(key.first())
            if (hintChar != null) {
                val hintTv = TextView(context).apply {
                    text = hintChar.toString()
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                    setTextColor(palette.keyHintColor)
                    gravity = Gravity.TOP or Gravity.END
                    setPadding(0, dp(2), dp(4), 0)
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.TOP or Gravity.END
                    )
                }
                container.addView(hintTv)
            }
        }

        // Setup interaction based on key type
        when (key) {
            "BACKSPACE" -> setupBackspaceGestures(container)
            "SPACE" -> setupSpacebarScrubbing(container)
            "LANG" -> {
                container.setOnClickListener {
                    performHaptic()
                    playKeySound()
                    handleKeyClick(key)
                }
                container.setOnLongClickListener {
                    performHaptic()
                    onKeyClickListener?.invoke(KeyAction.SwitchImePicker)
                    true
                }
            }
            else -> {
                setupNormalKeyTouch(container, key, displayLabel, isNumberRow)
            }
        }

        return container
    }

    private fun setupNormalKeyTouch(container: View, key: String, displayLabel: String, isNumberRow: Boolean) {
        val isLetterOrNum = !isSpecialKey(key) && key.length == 1
        val hintChar = if (isLetterOrNum && !isNumberRow) getHintForChar(key.first()) else null

        container.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (isLetterOrNum) {
                        showKeyPopup(v, displayLabel)
                    }
                    false
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    hideKeyPopup()
                    false
                }
                else -> false
            }
        }

        container.setOnClickListener {
            performHaptic()
            playKeySound()
            handleKeyClick(key)
        }

        if (hintChar != null) {
            container.setOnLongClickListener {
                hideKeyPopup()
                performHaptic()
                playKeySound()
                onKeyClickListener?.invoke(KeyAction.CharacterKey(hintChar))
                true
            }
        }
    }

    private fun setupBackspaceGestures(view: View) {
        var startX = 0f
        var isSwipingWord = false
        var wordsDeletedCount = 0
        var isRepeating = false
        var repeatCount = 0

        val repeatRunnable = object : Runnable {
            override fun run() {
                if (isRepeating && !isSwipingWord) {
                    performHaptic()
                    playKeySound()
                    onKeyClickListener?.invoke(KeyAction.Backspace)
                    repeatCount++
                    val nextDelay = if (repeatCount > 15) 35L else 55L
                    repeatHandler.postDelayed(this, nextDelay)
                }
            }
        }

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    isSwipingWord = false
                    wordsDeletedCount = 0
                    isRepeating = true
                    repeatCount = 0
                    performHaptic()
                    playKeySound()
                    onKeyClickListener?.invoke(KeyAction.Backspace)
                    repeatHandler.postDelayed(repeatRunnable, 350L)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.x - startX
                    // Swipe left to delete whole words
                    if (userPrefs.swipeDeleteWords && dx < -dp(28)) {
                        if (!isSwipingWord) {
                            isSwipingWord = true
                            isRepeating = false
                            repeatHandler.removeCallbacks(repeatRunnable)
                        }
                        val wordsToTrigger = ((-dx - dp(28)) / dp(32)).toInt() + 1
                        if (wordsToTrigger > wordsDeletedCount) {
                            val diff = wordsToTrigger - wordsDeletedCount
                            for (i in 0 until diff) {
                                performHaptic()
                                playKeySound()
                                onKeyClickListener?.invoke(KeyAction.DeleteWord)
                            }
                            wordsDeletedCount = wordsToTrigger
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isRepeating = false
                    repeatHandler.removeCallbacks(repeatRunnable)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupSpacebarScrubbing(view: View) {
        var startX = 0f
        var lastX = 0f
        var isDragging = false
        val dragThreshold = dp(12)

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    lastX = event.x
                    isDragging = false
                    false
                }
                MotionEvent.ACTION_MOVE -> {
                    val totalDx = Math.abs(event.x - startX)
                    if (userPrefs.cursorScrubbing && totalDx > dragThreshold) {
                        isDragging = true
                        val dx = event.x - lastX
                        if (Math.abs(dx) >= dragThreshold) {
                            performHaptic()
                            if (dx > 0) {
                                onKeyClickListener?.invoke(KeyAction.CursorRight)
                            } else {
                                onKeyClickListener?.invoke(KeyAction.CursorLeft)
                            }
                            lastX = event.x
                        }
                        true
                    } else {
                        false
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (isDragging) {
                        isDragging = false
                        true
                    } else {
                        performHaptic()
                        playKeySound()
                        onKeyClickListener?.invoke(KeyAction.Space)
                        true
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    isDragging = false
                    false
                }
                else -> false
            }
        }

        view.setOnLongClickListener {
            if (!isDragging) {
                performHaptic()
                onKeyClickListener?.invoke(KeyAction.SwitchImePicker)
                true
            } else {
                false
            }
        }
    }

    private fun getKeyTextSize(key: String, isNumberRow: Boolean): Float = when {
        isNumberRow -> 17f
        key == "SPACE" -> 14f
        key in listOf("SHIFT", "BACKSPACE", "RETURN") -> 18f
        key in listOf("?123", "ABC", "ESC", "TAB", "CTRL") -> 13f
        key in listOf("LANG", "EMOJI") -> 18f
        else -> 21f
    }

    private fun getKeyTextColor(key: String): Int = when (key) {
        "RETURN" -> Color.WHITE
        "SHIFT" -> if (isShifted) palette.stripPrimaryBorder else palette.keyTextColor
        "SPACE" -> if (isBangla) palette.keyTextColor else palette.keySpecialTextColor
        "?123", "ABC", "ESC", "TAB", "CTRL" -> palette.keySpecialTextColor
        else -> palette.keyTextColor
    }

    private fun isSpecialKey(key: String): Boolean =
        key in listOf("SHIFT", "BACKSPACE", "RETURN", "?123", "ABC", "LANG", "EMOJI", "ESC", "TAB", "CTRL")

    private fun createKeyBackground(key: String): StateListDrawable {
        val normalColor = when (key) {
            "RETURN" -> palette.keyReturnColor
            "SPACE" -> palette.keyNormalColor
            "SHIFT" -> if (isShifted) palette.stripPrimaryBg else palette.keySpecialColor
            else -> if (isSpecialKey(key)) palette.keySpecialColor else palette.keyNormalColor
        }

        val normal = GradientDrawable().apply {
            cornerRadius = dp(7).toFloat()
            setColor(normalColor)
            if (key != "RETURN" && !isSpecialKey(key)) {
                setStroke(dp(1), palette.keyBorderColor)
            }
        }

        val pressed = GradientDrawable().apply {
            cornerRadius = dp(7).toFloat()
            setColor(palette.keyPressedColor)
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
            "ESC" -> onKeyClickListener?.invoke(KeyAction.Esc)
            "TAB" -> onKeyClickListener?.invoke(KeyAction.Tab)
            "CTRL" -> onKeyClickListener?.invoke(KeyAction.Ctrl)
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
            else -> {
                val ch = if (isShifted) key.uppercase().first() else key.first()
                onKeyClickListener?.invoke(KeyAction.CharacterKey(ch))
                if (isShifted) {
                    setShift(false)
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        hideKeyPopup()
        super.onDetachedFromWindow()
    }

    private fun dp(value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()
}
