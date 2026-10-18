package app.getbongo.ime.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import app.getbongo.ime.model.ThemePalette

/**
 * Dual-mode candidate & utility strip above the keyboard:
 * - When typing: displays live phonetic candidate choices (with primary highlighted)
 * - When idle: displays fast shortcuts (Esc, Tab, Ctrl, Clipboard, Voice, Select All, Copy, Paste, Cursor ◀ ▶)
 */
class CandidateStripView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr) {

    sealed class ShortcutAction {
        object Esc : ShortcutAction()
        object Tab : ShortcutAction()
        object ToggleCtrl : ShortcutAction()
        object Clipboard : ShortcutAction()
        object VoiceInput : ShortcutAction()
        object SelectAll : ShortcutAction()
        object Copy : ShortcutAction()
        object Paste : ShortcutAction()
        object CursorLeft : ShortcutAction()
        object CursorRight : ShortcutAction()
    }

    private val container = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        setPadding(dp(6), 0, dp(6), 0)
    }

    var onCandidateSelected: ((Int, String) -> Unit)? = null
    var onShortcutAction: ((ShortcutAction) -> Unit)? = null

    private var palette: ThemePalette = ThemePalette.getPalette("midnight")
    private var isCtrlActive = false
    private var ctrlTextView: TextView? = null

    init {
        isHorizontalScrollBarEnabled = false
        setBackgroundColor(palette.surfaceColor)
        addView(container)
        showShortcuts()
    }

    fun applyTheme(newPalette: ThemePalette) {
        palette = newPalette
        setBackgroundColor(palette.surfaceColor)
        showShortcuts()
    }

    fun setCtrlActive(active: Boolean) {
        isCtrlActive = active
        ctrlTextView?.let { tv ->
            tv.setTextColor(if (active) palette.stripPrimaryBorder else palette.stripSecondaryText)
            tv.background = createShortcutBackground(isPrimary = active)
        }
    }

    fun setCandidates(candidates: List<String>) {
        if (candidates.isEmpty()) {
            showShortcuts()
            return
        }

        container.removeAllViews()
        ctrlTextView = null

        candidates.forEachIndexed { index, candidate ->
            val tv = TextView(context).apply {
                text = candidate
                setTextSize(TypedValue.COMPLEX_UNIT_SP, if (index == 0) 17f else 15f)
                typeface = if (index == 0) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                setTextColor(if (index == 0) palette.stripPrimaryText else palette.stripSecondaryText)
                setPadding(dp(14), dp(6), dp(14), dp(6))
                gravity = Gravity.CENTER
                isClickable = true
                isFocusable = true
                background = createCandidateBackground(index == 0)

                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(dp(3), 0, dp(3), 0)
                }
                layoutParams = lp

                setOnClickListener {
                    onCandidateSelected?.invoke(index, candidate)
                }
            }
            container.addView(tv)
        }
        smoothScrollTo(0, 0)
    }

    fun showClipboardPreview(clipText: String, onPaste: (String) -> Unit) {
        container.removeAllViews()
        ctrlTextView = null

        val closeBtn = TextView(context).apply {
            text = "✕"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(palette.stripSecondaryText)
            setPadding(dp(12), dp(6), dp(12), dp(6))
            gravity = Gravity.CENTER
            background = createShortcutBackground(false)
            setOnClickListener { showShortcuts() }
        }
        container.addView(closeBtn)

        val snippet = if (clipText.length > 35) clipText.take(35) + "…" else clipText
        val clipPill = TextView(context).apply {
            text = "📋 $snippet"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(palette.stripPrimaryText)
            setPadding(dp(14), dp(6), dp(14), dp(6))
            gravity = Gravity.CENTER
            background = createCandidateBackground(true)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(dp(4), 0, dp(4), 0)
            }
            layoutParams = lp
            setOnClickListener {
                onPaste(clipText)
                showShortcuts()
            }
        }
        container.addView(clipPill)
    }

    fun clear() {
        showShortcuts()
    }

    fun showShortcuts() {
        container.removeAllViews()
        ctrlTextView = null

        val shortcuts = listOf(
            Triple("Esc", ShortcutAction.Esc, false),
            Triple("Tab", ShortcutAction.Tab, false),
            Triple("Ctrl", ShortcutAction.ToggleCtrl, isCtrlActive),
            Triple("📋", ShortcutAction.Clipboard, false),
            Triple("🎙️", ShortcutAction.VoiceInput, false),
            Triple("Select All", ShortcutAction.SelectAll, false),
            Triple("Copy", ShortcutAction.Copy, false),
            Triple("Paste", ShortcutAction.Paste, false),
            Triple("◀", ShortcutAction.CursorLeft, false),
            Triple("▶", ShortcutAction.CursorRight, false)
        )

        for ((label, action, isHighlighted) in shortcuts) {
            val tv = TextView(context).apply {
                text = label
                setTextSize(TypedValue.COMPLEX_UNIT_SP, if (label == "📋" || label == "🎙️") 15f else 13f)
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(if (isHighlighted) palette.stripPrimaryBorder else palette.stripSecondaryText)
                setPadding(dp(12), dp(5), dp(12), dp(5))
                gravity = Gravity.CENTER
                isClickable = true
                isFocusable = true
                background = createShortcutBackground(isHighlighted)

                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(dp(2), 0, dp(2), 0)
                }
                layoutParams = lp

                setOnClickListener {
                    onShortcutAction?.invoke(action)
                }
            }

            if (action is ShortcutAction.ToggleCtrl) {
                ctrlTextView = tv
            }

            container.addView(tv)
        }
    }

    private fun createCandidateBackground(isPrimary: Boolean): StateListDrawable {
        val normal = GradientDrawable().apply {
            cornerRadius = dp(16).toFloat()
            if (isPrimary) {
                setColor(palette.stripPrimaryBg)
                setStroke(dp(1), palette.stripPrimaryBorder)
            } else {
                setColor(palette.stripSecondaryBg)
                setStroke(dp(1), palette.keyBorderColor)
            }
        }

        val pressed = GradientDrawable().apply {
            cornerRadius = dp(16).toFloat()
            setColor(palette.keyPressedColor)
        }

        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), pressed)
            addState(intArrayOf(), normal)
        }
    }

    private fun createShortcutBackground(isPrimary: Boolean): StateListDrawable {
        val normal = GradientDrawable().apply {
            cornerRadius = dp(8).toFloat()
            if (isPrimary) {
                setColor(palette.stripPrimaryBg)
                setStroke(dp(1), palette.stripPrimaryBorder)
            } else {
                setColor(Color.TRANSPARENT)
            }
        }

        val pressed = GradientDrawable().apply {
            cornerRadius = dp(8).toFloat()
            setColor(palette.keyPressedColor)
        }

        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), pressed)
            addState(intArrayOf(), normal)
        }
    }

    private fun dp(value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()
}
