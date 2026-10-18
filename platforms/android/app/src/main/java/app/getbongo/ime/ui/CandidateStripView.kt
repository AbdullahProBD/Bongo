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

/**
 * Dual-mode candidate & utility strip above the keyboard:
 * - When typing: displays live phonetic candidate choices (with primary highlighted)
 * - When idle: displays fast shortcuts (Esc, Tab, Ctrl, Select All, Copy, Paste, Cursor ◀ ▶)
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

    private var isCtrlActive = false
    private var ctrlTextView: TextView? = null

    init {
        isHorizontalScrollBarEnabled = false
        setBackgroundColor(Color.parseColor("#121214"))
        addView(container)
        showShortcuts()
    }

    fun setCtrlActive(active: Boolean) {
        isCtrlActive = active
        ctrlTextView?.let { tv ->
            tv.setTextColor(if (active) Color.parseColor("#818cf8") else Color.parseColor("#d4d4d8"))
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
                setTextColor(if (index == 0) Color.WHITE else Color.parseColor("#d4d4d8"))
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
            Triple("Select All", ShortcutAction.SelectAll, false),
            Triple("Copy", ShortcutAction.Copy, false),
            Triple("Paste", ShortcutAction.Paste, false),
            Triple("◀", ShortcutAction.CursorLeft, false),
            Triple("▶", ShortcutAction.CursorRight, false)
        )

        for ((label, action, isHighlighted) in shortcuts) {
            val tv = TextView(context).apply {
                text = label
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(if (isHighlighted) Color.parseColor("#818cf8") else Color.parseColor("#d4d4d8"))
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
                setColor(Color.parseColor("#312e81"))
                setStroke(dp(1), Color.parseColor("#6366f1"))
            } else {
                setColor(Color.parseColor("#202127"))
                setStroke(dp(1), Color.parseColor("#2a2b34"))
            }
        }

        val pressed = GradientDrawable().apply {
            cornerRadius = dp(16).toFloat()
            setColor(Color.parseColor("#4338ca"))
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
                setColor(Color.parseColor("#2e2f42"))
                setStroke(dp(1), Color.parseColor("#6366f1"))
            } else {
                setColor(Color.parseColor("#1c1d22"))
                setStroke(dp(1), Color.parseColor("#26272e"))
            }
        }

        val pressed = GradientDrawable().apply {
            cornerRadius = dp(8).toFloat()
            setColor(Color.parseColor("#3b3d49"))
        }

        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), pressed)
            addState(intArrayOf(), normal)
        }
    }

    private fun dp(value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()
}
