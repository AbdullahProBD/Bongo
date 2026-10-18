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
 * Guides user through activating the keyboard, switching input methods,
 * configuring preferences, and testing Bangla typing.
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
            text = "Modern Bengali Phonetic Input Method"
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

        container.addView(createSpacer(dp(20)))

        // Preferences Card
        container.addView(createSectionHeader("PREFERENCES"))
        val prefsCard = createCardLayout()

        val hapticSwitch = Switch(this).apply {
            text = "Haptic feedback on keypress"
            setTextColor(Color.WHITE)
            isChecked = userPrefs.hapticFeedback
            setOnCheckedChangeListener { _, isChecked ->
                userPrefs.hapticFeedback = isChecked
            }
        }
        val soundSwitch = Switch(this).apply {
            text = "Sound feedback on keypress"
            setTextColor(Color.WHITE)
            isChecked = userPrefs.soundFeedback
            setOnCheckedChangeListener { _, isChecked ->
                userPrefs.soundFeedback = isChecked
            }
        }
        val emojiSwitch = Switch(this).apply {
            text = "Suggest emojis & symbols"
            setTextColor(Color.WHITE)
            isChecked = userPrefs.suggestEmoji
            setOnCheckedChangeListener { _, isChecked ->
                userPrefs.suggestEmoji = isChecked
            }
        }
        prefsCard.addView(hapticSwitch)
        prefsCard.addView(createSpacer(dp(8)))
        prefsCard.addView(soundSwitch)
        prefsCard.addView(createSpacer(dp(8)))
        prefsCard.addView(emojiSwitch)
        container.addView(prefsCard)

        // Footer version
        val footer = TextView(this).apply {
            text = "Bongo for Android • v0.1.0\nLicensed under MIT"
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

    private fun createButton(text: String, onClick: () -> Unit): Button = Button(this).apply {
        this.text = text
        setTextColor(Color.WHITE)
        textSize = 14f
        typeface = Typeface.DEFAULT_BOLD
        background = GradientDrawable().apply {
            cornerRadius = dp(8).toFloat()
            setColor(Color.parseColor("#4f46e5")) // Indigo 600
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
