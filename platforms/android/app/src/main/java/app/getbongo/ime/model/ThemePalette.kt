package app.getbongo.ime.model

import android.graphics.Color

/**
 * Color palette configurations for Bongo IME themes.
 */
data class ThemePalette(
    val name: String,
    val surfaceColor: Int,
    val keyNormalColor: Int,
    val keySpecialColor: Int,
    val keyReturnColor: Int,
    val keyPressedColor: Int,
    val keyTextColor: Int,
    val keySpecialTextColor: Int,
    val keyHintColor: Int,
    val keyBorderColor: Int,
    val stripPrimaryBg: Int,
    val stripPrimaryText: Int,
    val stripPrimaryBorder: Int,
    val stripSecondaryBg: Int,
    val stripSecondaryText: Int,
    val dividerColor: Int
) {
    companion object {
        fun getPalette(themeName: String): ThemePalette = when (themeName.lowercase()) {
            "oled" -> ThemePalette(
                name = "oled",
                surfaceColor = Color.parseColor("#000000"),
                keyNormalColor = Color.parseColor("#121214"),
                keySpecialColor = Color.parseColor("#08080a"),
                keyReturnColor = Color.parseColor("#6366f1"),
                keyPressedColor = Color.parseColor("#27272a"),
                keyTextColor = Color.WHITE,
                keySpecialTextColor = Color.parseColor("#e4e4e7"),
                keyHintColor = Color.parseColor("#52525b"),
                keyBorderColor = Color.parseColor("#1e1e24"),
                stripPrimaryBg = Color.parseColor("#1e1b4b"),
                stripPrimaryText = Color.WHITE,
                stripPrimaryBorder = Color.parseColor("#6366f1"),
                stripSecondaryBg = Color.parseColor("#121214"),
                stripSecondaryText = Color.parseColor("#d4d4d8"),
                dividerColor = Color.parseColor("#1c1c20")
            )
            "light" -> ThemePalette(
                name = "light",
                surfaceColor = Color.parseColor("#f4f4f5"),
                keyNormalColor = Color.parseColor("#ffffff"),
                keySpecialColor = Color.parseColor("#e4e4e7"),
                keyReturnColor = Color.parseColor("#4f46e5"),
                keyPressedColor = Color.parseColor("#d4d4d8"),
                keyTextColor = Color.parseColor("#18181b"),
                keySpecialTextColor = Color.parseColor("#27272a"),
                keyHintColor = Color.parseColor("#a1a1aa"),
                keyBorderColor = Color.parseColor("#d4d4d8"),
                stripPrimaryBg = Color.parseColor("#e0e7ff"),
                stripPrimaryText = Color.parseColor("#312e81"),
                stripPrimaryBorder = Color.parseColor("#818cf8"),
                stripSecondaryBg = Color.parseColor("#ffffff"),
                stripSecondaryText = Color.parseColor("#3f3f46"),
                dividerColor = Color.parseColor("#e4e4e7")
            )
            else -> ThemePalette( // "midnight" (default, replicates classic Ridmik dark theme)
                name = "midnight",
                surfaceColor = Color.parseColor("#18191b"),
                keyNormalColor = Color.parseColor("#3c3d40"),
                keySpecialColor = Color.parseColor("#222325"),
                keyReturnColor = Color.parseColor("#222325"),
                keyPressedColor = Color.parseColor("#595a5f"),
                keyTextColor = Color.WHITE,
                keySpecialTextColor = Color.WHITE,
                keyHintColor = Color.parseColor("#8c8d93"),
                keyBorderColor = Color.parseColor("#252629"),
                stripPrimaryBg = Color.parseColor("#292a2d"),
                stripPrimaryText = Color.WHITE,
                stripPrimaryBorder = Color.parseColor("#6366f1"),
                stripSecondaryBg = Color.parseColor("#1c1d20"),
                stripSecondaryText = Color.parseColor("#d4d4d8"),
                dividerColor = Color.parseColor("#202123")
            )
        }
    }
}
