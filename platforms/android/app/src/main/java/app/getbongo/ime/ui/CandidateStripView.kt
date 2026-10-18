package app.getbongo.ime.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Modern suggestion strip displaying transliterated candidate words
 * directly above the keyboard with highlighted primary choice.
 */
class CandidateStripView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr) {

    private val container = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        setPadding(dp(8), 0, dp(8), 0)
    }

    var onCandidateSelected: ((Int, String) -> Unit)? = null

    init {
        isHorizontalScrollBarEnabled = false
        setBackgroundColor(Color.parseColor("#121214")) // Matches keyboard surface
        addView(container)
    }

    fun setCandidates(candidates: List<String>) {
        container.removeAllViews()
        if (candidates.isEmpty()) {
            return
        }

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
        container.removeAllViews()
    }

    private fun createCandidateBackground(isPrimary: Boolean): StateListDrawable {
        val normal = GradientDrawable().apply {
            cornerRadius = dp(16).toFloat()
            if (isPrimary) {
                setColor(Color.parseColor("#312e81")) // Indigo 900
                setStroke(dp(1), Color.parseColor("#6366f1")) // Indigo 500 border
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

    private fun dp(value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()
}
