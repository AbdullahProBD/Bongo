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
import android.widget.GridLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * Modern, smooth, categorized Emoji picker panel for Bongo IME.
 */
class EmojiKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var onEmojiSelected: ((String) -> Unit)? = null
    var onBackToKeyboard: (() -> Unit)? = null
    var onBackspace: (() -> Unit)? = null

    private var selectedCategoryIndex = 0
    private val categoryBar = LinearLayout(context)
    private val emojiContainer = LinearLayout(context)
    private val scrollView = ScrollView(context)

    companion object {
        private val CATEGORIES = listOf(
            Category("Smileys", "😀", listOf(
                "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "🥲", "☺️",
                "😊", "😇", "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗",
                "😙", "😚", "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓",
                "😎", "🥸", "🤩", "🥳", "😏", "😒", "😞", "😔", "😟", "😕",
                "🙁", "😣", "😖", "😫", "😩", "🥺", "😢", "😭", "😤", "😠",
                "😡", "🤬", "🤯", "😳", "🥵", "🥶", "😱", "😨", "😰", "😥",
                "😓", "🤗", "🤔", "🤭", "🤫", "🤥", "😶", "😐", "😑", "😬",
                "🙄", "😯", "😦", "😧", "😮", "😲", "🥱", "😴", "🤤", "😪",
                "😵", "🤐", "🥴", "🤢", "🤮", "🤧", "😷", "🤒", "🤕", "🤑", "🤠"
            )),
            Category("Gestures", "👍", listOf(
                "👍", "👎", "👏", "🙌", "👐", "🤲", "🤝", "🙏", "✍️", "💅",
                "👋", "🤚", "🖐️", "✋", "🖖", "👌", "🤌", "🤏", "✌️", "🤞",
                "🤟", "🤘", "🤙", "👈", "👉", "👆", "🖕", "👇", "☝️", "✊",
                "👊", "🤛", "🤜", "💪", "🦾", "🦿", "🦵", "🦶", "👂", "👃",
                "👀", "👁️", "👅", "👄", "🧠", "🫀", "🫁", "🦴", "🦷", "🫂"
            )),
            Category("Hearts", "❤️", listOf(
                "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔",
                "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝", "💟", "✨",
                "🌟", "💫", "⭐", "🔥", "💥", "💯", "💢", "🎉", "🎊", "🎈"
            )),
            Category("Nature", "🌸", listOf(
                "🌺", "🌸", "🌹", "🌷", "🌼", "🌻", "🌾", "🌿", "🍀", "🍁",
                "🍂", "🍃", "☀️", "🌙", "☁️", "🌧️", "❄️", "⚡", "🌈", "⭐",
                "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯",
                "🦁", "🐮", "🐷", "🐸", "🐵", "🐔", "🐧", "🐦", "🦅", "🦉",
                "🦇", "🐺", "🐗", "🐴", "🦄", "🐝", "🐛", "🦋", "🐌", "🐞"
            )),
            Category("Culture", "🇧🇩", listOf(
                "🇧🇩", "☕", "🍵", "🧃", "🥤", "🧋", "🍕", "🍔", "🍟", "🍎",
                "🥭", "🍌", "🍉", "🍇", "🍓", "🏏", "⚽", "🏀", "🏆", "🥇",
                "🎯", "🎲", "🎮", "🚗", "🚌", "🚆", "✈️", "🚀", "💡", "📱",
                "💻", "📚", "✏️", "🔔", "🎵", "🎶", "🎤", "🎧", "🎬", "🕌"
            ))
        )
    }

    data class Category(val name: String, val icon: String, val emojis: List<String>)

    init {
        orientation = VERTICAL
        setBackgroundColor(Color.parseColor("#121214"))

        setupCategoryBar()
        setupScrollView()
        setupBottomBar()

        loadCategory(0)
    }

    private fun setupCategoryBar() {
        val hScroll = HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp(40))
        }

        categoryBar.apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), 0, dp(8), 0)
        }

        CATEGORIES.forEachIndexed { index, cat ->
            val tab = TextView(context).apply {
                text = "${cat.icon} ${cat.name}"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(if (index == selectedCategoryIndex) Color.parseColor("#818cf8") else Color.parseColor("#71717a"))
                setPadding(dp(12), dp(8), dp(12), dp(8))
                gravity = Gravity.CENTER

                background = createTabBackground(index == selectedCategoryIndex)

                setOnClickListener {
                    loadCategory(index)
                }
            }
            categoryBar.addView(tab)
        }

        hScroll.addView(categoryBar)
        addView(hScroll)

        // Divider
        addView(View(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp(1))
            setBackgroundColor(Color.parseColor("#27272a"))
        })
    }

    private fun setupScrollView() {
        scrollView.apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f)
            isFillViewport = true
        }

        emojiContainer.apply {
            orientation = VERTICAL
            setPadding(dp(6), dp(8), dp(6), dp(8))
        }

        scrollView.addView(emojiContainer)
        addView(scrollView)
    }

    private fun setupBottomBar() {
        val bottomBar = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp(48))
            setBackgroundColor(Color.parseColor("#18181b"))
            setPadding(dp(8), dp(4), dp(8), dp(4))
        }

        // Return to keyboard button
        val abcBtn = TextView(context).apply {
            text = "ABC"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            background = createKeyBackground(false)
            layoutParams = LayoutParams(dp(64), LayoutParams.MATCH_PARENT).apply {
                setMargins(dp(4), dp(2), dp(4), dp(2))
            }
            setOnClickListener {
                onBackToKeyboard?.invoke()
            }
        }

        // Center space bar
        val spaceBtn = TextView(context).apply {
            text = "Space"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setTextColor(Color.parseColor("#a1a1aa"))
            gravity = Gravity.CENTER
            background = createKeyBackground(false)
            layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 1f).apply {
                setMargins(dp(4), dp(2), dp(4), dp(2))
            }
            setOnClickListener {
                onEmojiSelected?.invoke(" ")
            }
        }

        // Backspace button
        val backspaceBtn = TextView(context).apply {
            text = "⌫"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            background = createKeyBackground(false)
            layoutParams = LayoutParams(dp(64), LayoutParams.MATCH_PARENT).apply {
                setMargins(dp(4), dp(2), dp(4), dp(2))
            }
            setOnClickListener {
                onBackspace?.invoke()
            }
        }

        bottomBar.addView(abcBtn)
        bottomBar.addView(spaceBtn)
        bottomBar.addView(backspaceBtn)

        addView(bottomBar)
    }

    private fun loadCategory(categoryIndex: Int) {
        selectedCategoryIndex = categoryIndex

        // Update category tabs style
        for (i in 0 until categoryBar.childCount) {
            val child = categoryBar.getChildAt(i) as? TextView ?: continue
            val isSelected = (i == categoryIndex)
            child.setTextColor(if (isSelected) Color.parseColor("#818cf8") else Color.parseColor("#71717a"))
            child.background = createTabBackground(isSelected)
        }

        emojiContainer.removeAllViews()
        val emojis = CATEGORIES[categoryIndex].emojis

        // 7 columns of emojis
        val numColumns = 7
        var rowLayout: LinearLayout? = null

        emojis.forEachIndexed { idx, emoji ->
            if (idx % numColumns == 0) {
                rowLayout = LinearLayout(context).apply {
                    orientation = HORIZONTAL
                    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp(44))
                    weightSum = numColumns.toFloat()
                }
                emojiContainer.addView(rowLayout)
            }

            val tv = TextView(context).apply {
                text = emoji
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
                gravity = Gravity.CENTER
                layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)
                background = createRippleBackground()
                setOnClickListener {
                    onEmojiSelected?.invoke(emoji)
                }
            }
            rowLayout?.addView(tv)
        }

        scrollView.smoothScrollTo(0, 0)
    }

    private fun createTabBackground(isSelected: Boolean): GradientDrawable = GradientDrawable().apply {
        cornerRadius = dp(16).toFloat()
        if (isSelected) {
            setColor(Color.parseColor("#1f1f2e"))
            setStroke(dp(1), Color.parseColor("#4f46e5"))
        } else {
            setColor(Color.TRANSPARENT)
        }
    }

    private fun createKeyBackground(isSpecial: Boolean): StateListDrawable {
        val normal = GradientDrawable().apply {
            cornerRadius = dp(6).toFloat()
            setColor(if (isSpecial) Color.parseColor("#1f1f24") else Color.parseColor("#27272a"))
        }
        val pressed = GradientDrawable().apply {
            cornerRadius = dp(6).toFloat()
            setColor(Color.parseColor("#3f3f46"))
        }
        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), pressed)
            addState(intArrayOf(), normal)
        }
    }

    private fun createRippleBackground(): StateListDrawable {
        val normal = GradientDrawable().apply {
            setColor(Color.TRANSPARENT)
        }
        val pressed = GradientDrawable().apply {
            cornerRadius = dp(8).toFloat()
            setColor(Color.parseColor("#27272a"))
        }
        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), pressed)
            addState(intArrayOf(), normal)
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
