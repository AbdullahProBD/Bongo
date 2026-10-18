package app.getbongo.ime.model

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages user preferences for Bongo Android IME.
 */
class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var typingMode: Int
        get() = prefs.getInt(KEY_TYPING_MODE, 0)
        set(value) = prefs.edit().putInt(KEY_TYPING_MODE, value).apply()

    var suggestEmoji: Boolean
        get() = prefs.getBoolean(KEY_SUGGEST_EMOJI, true)
        set(value) = prefs.edit().putBoolean(KEY_SUGGEST_EMOJI, value).apply()

    var hapticFeedback: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC, value).apply()

    var soundFeedback: Boolean
        get() = prefs.getBoolean(KEY_SOUND, false)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    var theme: String
        get() = prefs.getString(KEY_THEME, "midnight") ?: "midnight"
        set(value) = prefs.edit().putString(KEY_THEME, value).apply()

    var keyboardHeightDp: Int
        get() = prefs.getInt(KEY_KEYBOARD_HEIGHT, 270)
        set(value) = prefs.edit().putInt(KEY_KEYBOARD_HEIGHT, value).apply()

    var doubleSpacePeriod: Boolean
        get() = prefs.getBoolean(KEY_DOUBLE_SPACE_PERIOD, true)
        set(value) = prefs.edit().putBoolean(KEY_DOUBLE_SPACE_PERIOD, value).apply()

    var longPressSymbols: Boolean
        get() = prefs.getBoolean(KEY_LONG_PRESS_SYMBOLS, true)
        set(value) = prefs.edit().putBoolean(KEY_LONG_PRESS_SYMBOLS, value).apply()

    var cursorScrubbing: Boolean
        get() = prefs.getBoolean(KEY_CURSOR_SCRUBBING, true)
        set(value) = prefs.edit().putBoolean(KEY_CURSOR_SCRUBBING, value).apply()

    var bengaliDigits: Boolean
        get() = prefs.getBoolean(KEY_BENGALI_DIGITS, false)
        set(value) = prefs.edit().putBoolean(KEY_BENGALI_DIGITS, value).apply()

    var showNumberRow: Boolean
        get() = prefs.getBoolean(KEY_SHOW_NUMBER_ROW, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_NUMBER_ROW, value).apply()

    var keyPopupPreview: Boolean
        get() = prefs.getBoolean(KEY_POPUP_PREVIEW, true)
        set(value) = prefs.edit().putBoolean(KEY_POPUP_PREVIEW, value).apply()

    var swipeDeleteWords: Boolean
        get() = prefs.getBoolean(KEY_SWIPE_DELETE, true)
        set(value) = prefs.edit().putBoolean(KEY_SWIPE_DELETE, value).apply()

    companion object {
        private const val PREFS_NAME = "bongo_preferences"
        private const val KEY_TYPING_MODE = "typing_mode"
        private const val KEY_SUGGEST_EMOJI = "suggest_emoji"
        private const val KEY_HAPTIC = "haptic_feedback"
        private const val KEY_SOUND = "sound_feedback"
        private const val KEY_THEME = "theme"
        private const val KEY_KEYBOARD_HEIGHT = "keyboard_height"
        private const val KEY_DOUBLE_SPACE_PERIOD = "double_space_period"
        private const val KEY_LONG_PRESS_SYMBOLS = "long_press_symbols"
        private const val KEY_CURSOR_SCRUBBING = "cursor_scrubbing"
        private const val KEY_BENGALI_DIGITS = "bengali_digits"
        private const val KEY_SHOW_NUMBER_ROW = "show_number_row"
        private const val KEY_POPUP_PREVIEW = "key_popup_preview"
        private const val KEY_SWIPE_DELETE = "swipe_delete_words"
    }
}
