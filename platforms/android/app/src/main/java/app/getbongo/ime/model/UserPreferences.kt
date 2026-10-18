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
        get() = prefs.getString(KEY_THEME, "dark") ?: "dark"
        set(value) = prefs.edit().putString(KEY_THEME, value).apply()

    companion object {
        private const val PREFS_NAME = "bongo_preferences"
        private const val KEY_TYPING_MODE = "typing_mode"
        private const val KEY_SUGGEST_EMOJI = "suggest_emoji"
        private const val KEY_HAPTIC = "haptic_feedback"
        private const val KEY_SOUND = "sound_feedback"
        private const val KEY_THEME = "theme"
    }
}
