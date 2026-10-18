package app.getbongo.ime.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import app.getbongo.ime.engine.BongoNative
import app.getbongo.ime.model.UserPreferences

/**
 * Modern Bongo Keyboard settings & setup activity.
 * Guides user through activating the keyboard, switching themes,
 * adjusting height, and configuring advanced typing gestures.
 */
class SettingsActivity : Activity() {

    private lateinit var userPrefs: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userPrefs = UserPreferences(this)

        val root = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#09090b")) // Dark background
            isFillViewport = true
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(36), dp(20), dp(36))
        }

        // Header
        val title = TextView(this).apply {
            text = "Bongo"
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#818cf8"))
        }
        val subtitle = TextView(this).apply {
            text = "Modern Bengali Input Method for Android"
            textSize = 14f
            setTextColor(Color.parseColor("#a1a1aa"))
            setPadding(0, dp(4), 0, dp(24))
        }
        container.addView(title)
        container.addView(subtitle)

        // Setup Card
        container.addView(createSectionHeader("SETUP & ACTIVATION"))
        val setupCard = createCardLayout()

        val enableBtn = createButton("1. Enable Bongo in Settings") {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }
        val selectBtn = createButton("2. Select Bongo Keyboard") {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showInputMethodPicker()
        }
        setupCard.addView(enableBtn)
        setupCard.addView(createSpacer(dp(10)))
        setupCard.addView(selectBtn)
        container.addView(setupCard)

        container.addView(createSpacer(dp(20)))

        // Quick Test Field Card
        container.addView(createSectionHeader("TEST TYPING (BONGO PHONETIC)"))
        val testCard = createCardLayout()
        val testInput = EditText(this).apply {
            hint = "Type 'ami banglay gan gai' here..."
            setHintTextColor(Color.parseColor("#71717a"))
            setTextColor(Color.WHITE)
            textSize = 16f
            setBackgroundColor(Color.parseColor("#18181b"))
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }
        testCard.addView(testInput)
        container.addView(testCard)

        container.addView(createSpacer(dp(20)))

        // Themes Card
        container.addView(createSectionHeader("THEME SELECTION"))
        val themeCard = createCardLayout()
        val themeRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 3f
        }
        val themeOptions = listOf("midnight" to "Midnight", "oled" to "OLED Black", "light" to "Light")
        val themeButtons = mutableListOf<Button>()

        themeOptions.forEach { (key, label) ->
            val isSelected = userPrefs.theme == key
            val btn = Button(this).apply {
                text = label
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(if (isSelected) Color.WHITE else Color.parseColor("#a1a1aa"))
                background = createOptionBackground(isSelected)
                val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(dp(3), 0, dp(3), 0)
                }
                layoutParams = lp
                setOnClickListener {
                    userPrefs.theme = key
                    themeButtons.forEachIndexed { i, b ->
                        val sel = themeOptions[i].first == key
                        b.setTextColor(if (sel) Color.WHITE else Color.parseColor("#a1a1aa"))
                        b.background = createOptionBackground(sel)
                    }
                }
            }
            themeButtons.add(btn)
            themeRow.addView(btn)
        }
        themeCard.addView(themeRow)
        container.addView(themeCard)

        container.addView(createSpacer(dp(20)))

        // Keyboard Height Card
        container.addView(createSectionHeader("KEYBOARD HEIGHT"))
        val heightCard = createCardLayout()
        val heightRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 3f
        }
        val heightOptions = listOf(250 to "Compact", 270 to "Standard", 295 to "Tall")
        val heightButtons = mutableListOf<Button>()

        heightOptions.forEach { (h, label) ->
            val isSelected = userPrefs.keyboardHeightDp == h
            val btn = Button(this).apply {
                text = label
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(if (isSelected) Color.WHITE else Color.parseColor("#a1a1aa"))
                background = createOptionBackground(isSelected)
                val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(dp(3), 0, dp(3), 0)
                }
                layoutParams = lp
                setOnClickListener {
                    userPrefs.keyboardHeightDp = h
                    heightButtons.forEachIndexed { i, b ->
                        val sel = heightOptions[i].first == h
                        b.setTextColor(if (sel) Color.WHITE else Color.parseColor("#a1a1aa"))
                        b.background = createOptionBackground(sel)
                    }
                }
            }
            heightButtons.add(btn)
            heightRow.addView(btn)
        }
        heightCard.addView(heightRow)
        container.addView(heightCard)

        container.addView(createSpacer(dp(20)))

        // Typing Gestures & Features Card
        container.addView(createSectionHeader("TYPING & GESTURES"))
        val prefsCard = createCardLayout()

        val hapticSwitch = Switch(this).apply {
            text = "Haptic feedback on keypress"
            setTextColor(Color.WHITE)
            isChecked = userPrefs.hapticFeedback
            setOnCheckedChangeListener { _, isChecked -> userPrefs.hapticFeedback = isChecked }
        }
        val soundSwitch = Switch(this).apply {
            text = "Sound effects on keypress"
            setTextColor(Color.WHITE)
            isChecked = userPrefs.soundFeedback
            setOnCheckedChangeListener { _, isChecked -> userPrefs.soundFeedback = isChecked }
        }
        val scrubbingSwitch = Switch(this).apply {
            text = "Spacebar cursor trackpad (swipe to scrub)"
            setTextColor(Color.WHITE)
            isChecked = userPrefs.cursorScrubbing
            setOnCheckedChangeListener { _, isChecked -> userPrefs.cursorScrubbing = isChecked }
        }
        val doubleSpaceSwitch = Switch(this).apply {
            text = "Double-tap Space for Dari (। / .)"
            setTextColor(Color.WHITE)
            isChecked = userPrefs.doubleSpacePeriod
            setOnCheckedChangeListener { _, isChecked -> userPrefs.doubleSpacePeriod = isChecked }
        }
        val hintsSwitch = Switch(this).apply {
            text = "Long-press letter for numbers & symbols"
            setTextColor(Color.WHITE)
            isChecked = userPrefs.longPressSymbols
            setOnCheckedChangeListener { _, isChecked -> userPrefs.longPressSymbols = isChecked }
        }
        val bnDigitsSwitch = Switch(this).apply {
            text = "Use Bengali numerals on hints (১, ২, ৩...)"
            setTextColor(Color.WHITE)
            isChecked = userPrefs.bengaliDigits
            setOnCheckedChangeListener { _, isChecked -> userPrefs.bengaliDigits = isChecked }
        }

        val numRowSwitch = Switch(this).apply {
            text = "Permanent top number row (1-0 / ১-০)"
            setTextColor(Color.WHITE)
            isChecked = userPrefs.showNumberRow
            setOnCheckedChangeListener { _, isChecked -> userPrefs.showNumberRow = isChecked }
        }
        val popupSwitch = Switch(this).apply {
            text = "Key popup preview bubble on touch"
            setTextColor(Color.WHITE)
            isChecked = userPrefs.keyPopupPreview
            setOnCheckedChangeListener { _, isChecked -> userPrefs.keyPopupPreview = isChecked }
        }
        val swipeDeleteSwitch = Switch(this).apply {
            text = "Swipe left from backspace to delete words"
            setTextColor(Color.WHITE)
            isChecked = userPrefs.swipeDeleteWords
            setOnCheckedChangeListener { _, isChecked -> userPrefs.swipeDeleteWords = isChecked }
        }

        prefsCard.addView(numRowSwitch)
        prefsCard.addView(createSpacer(dp(8)))
        prefsCard.addView(popupSwitch)
        prefsCard.addView(createSpacer(dp(8)))
        prefsCard.addView(swipeDeleteSwitch)
        prefsCard.addView(createSpacer(dp(8)))
        prefsCard.addView(hapticSwitch)
        prefsCard.addView(createSpacer(dp(8)))
        prefsCard.addView(soundSwitch)
        prefsCard.addView(createSpacer(dp(8)))
        prefsCard.addView(scrubbingSwitch)
        prefsCard.addView(createSpacer(dp(8)))
        prefsCard.addView(doubleSpaceSwitch)
        prefsCard.addView(createSpacer(dp(8)))
        prefsCard.addView(hintsSwitch)
        prefsCard.addView(createSpacer(dp(8)))
        prefsCard.addView(bnDigitsSwitch)
        container.addView(prefsCard)

        container.addView(createSpacer(dp(20)))

        // Engine Status Card
        container.addView(createSectionHeader("ENGINE STATUS"))
        val statusCard = createCardLayout()
        val engineStatus = TextView(this).apply {
            val isNative = BongoNative.isLoaded
            text = if (isNative) {
                "⚡ Core: Rust Native Engine (JNI 64-bit)"
            } else {
                "✓ Core: Pure-Kotlin Bongo Engine (Resilient Fallback)"
            }
            textSize = 14f
            setTextColor(if (isNative) Color.parseColor("#34d399") else Color.parseColor("#60a5fa"))
        }
        statusCard.addView(engineStatus)
        container.addView(statusCard)

        // Footer version
        val footer = TextView(this).apply {
            text = "Bongo for Android • v0.2.0\nLicensed under MIT"
            textSize = 12f
            setTextColor(Color.parseColor("#52525b"))
            gravity = Gravity.CENTER
            setPadding(0, dp(32), 0, dp(16))
        }
        container.addView(footer)

        root.addView(container)
        setContentView(root)
    }

    private fun createSectionHeader(title: String): TextView = TextView(this).apply {
        text = title
        textSize = 12f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(Color.parseColor("#71717a"))
        setPadding(dp(4), 0, 0, dp(8))
    }

    private fun createCardLayout(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        background = GradientDrawable().apply {
            cornerRadius = dp(12).toFloat()
            setColor(Color.parseColor("#18181b"))
            setStroke(dp(1), Color.parseColor("#27272a"))
        }
        setPadding(dp(16), dp(16), dp(16), dp(16))
    }

    private fun createOptionBackground(isSelected: Boolean): GradientDrawable = GradientDrawable().apply {
        cornerRadius = dp(8).toFloat()
        if (isSelected) {
            setColor(Color.parseColor("#4f46e5"))
            setStroke(dp(1), Color.parseColor("#6366f1"))
        } else {
            setColor(Color.parseColor("#27272a"))
            setStroke(dp(1), Color.parseColor("#3f3f46"))
        }
    }

    private fun createButton(text: String, onClick: () -> Unit): Button = Button(this).apply {
        this.text = text
        setTextColor(Color.WHITE)
        textSize = 14f
        typeface = Typeface.DEFAULT_BOLD
        background = GradientDrawable().apply {
            cornerRadius = dp(8).toFloat()
            setColor(Color.parseColor("#4f46e5"))
        }
        setPadding(dp(16), dp(12), dp(16), dp(12))
        setOnClickListener { onClick() }
    }

    private fun createSpacer(heightPx: Int): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            heightPx
        )
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
