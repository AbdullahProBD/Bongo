package app.getbongo.ime.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Suggestion strip displaying candidates directly above the keyboard.
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
    }

    var onCandidateSelected: ((Int, String) -> Unit)? = null

    init {
        isHorizontalScrollBarEnabled = false
        setBackgroundColor(Color.parseColor("#18181b")) // Dark Bongo surface
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
                textSize = if (index == 0) 18f else 16f
                typeface = if (index == 0) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                setTextColor(if (index == 0) Color.parseColor("#818cf8") else Color.parseColor("#ffffff"))
                setPadding(dp(16), dp(10), dp(16), dp(10))
                gravity = Gravity.CENTER
                isClickable = true
                isFocusable = true
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

    private fun dp(value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()
}
