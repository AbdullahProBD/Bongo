package app.getbongo.ime

import android.content.Context
import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.os.SystemClock
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import app.getbongo.ime.engine.BongoEngine
import app.getbongo.ime.ui.BongoKeyboardView
import app.getbongo.ime.ui.CandidateStripView
import app.getbongo.ime.ui.EmojiKeyboardView

/**
 * Main Android InputMethodService for Bongo.
 * Handles input routing between soft/hardware keyboard, the Bongo phonetic engine,
 * candidate selection strip, shortcuts toolbar (Ctrl, Tab, Esc, Cursor),
 * emoji panel, and the target application's InputConnection.
 */
class BongoInputMethodService : InputMethodService() {

    private lateinit var bongoEngine: BongoEngine
    private var keyboardView: BongoKeyboardView? = null
    private var emojiKeyboardView: EmojiKeyboardView? = null
    private var candidateStripView: CandidateStripView? = null
    private var isBanglaMode = true
    private var isCtrlActive = false

    override fun onCreate() {
        super.onCreate()
        bongoEngine = BongoEngine()
    }

    override fun onCreateInputView(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#121214"))
        }

        candidateStripView = CandidateStripView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(44)
            )
            onCandidateSelected = { index, _ ->
                handleCandidateSelection(index)
            }
            onShortcutAction = { action ->
                handleShortcutAction(action)
            }
        }

        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(1)
            )
            setBackgroundColor(Color.parseColor("#27272a"))
        }

        val keyboardFrame = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(270) // Sized comfortably matching Ridmik & Gboard
            )
        }

        keyboardView = BongoKeyboardView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBanglaMode(isBanglaMode)
            onKeyClickListener = { action ->
                handleKeyAction(action)
            }
        }

        emojiKeyboardView = EmojiKeyboardView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            visibility = View.GONE
            onEmojiSelected = { emoji ->
                currentInputConnection?.commitText(emoji, 1)
            }
            onBackToKeyboard = {
                showKeyboard()
            }
            onBackspace = {
                sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
            }
        }

        keyboardFrame.addView(keyboardView)
        keyboardFrame.addView(emojiKeyboardView)

        root.addView(candidateStripView)
        root.addView(divider)
        root.addView(keyboardFrame)

        return root
    }

    private fun showKeyboard() {
        emojiKeyboardView?.visibility = View.GONE
        keyboardView?.visibility = View.VISIBLE
        candidateStripView?.visibility = View.VISIBLE
    }

    private fun showEmojiKeyboard() {
        commitOngoingComposition()
        keyboardView?.visibility = View.GONE
        candidateStripView?.visibility = View.GONE
        emojiKeyboardView?.visibility = View.VISIBLE
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        showKeyboard()
        resetComposition()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        commitOngoingComposition()
        resetComposition()
    }

    private fun handleShortcutAction(action: CandidateStripView.ShortcutAction) {
        val ic = currentInputConnection ?: return

        when (action) {
            is CandidateStripView.ShortcutAction.Esc -> {
                sendDownUpKeyEvents(KeyEvent.KEYCODE_ESCAPE)
            }
            is CandidateStripView.ShortcutAction.Tab -> {
                sendDownUpKeyEvents(KeyEvent.KEYCODE_TAB)
            }
            is CandidateStripView.ShortcutAction.ToggleCtrl -> {
                isCtrlActive = !isCtrlActive
                candidateStripView?.setCtrlActive(isCtrlActive)
            }
            is CandidateStripView.ShortcutAction.SelectAll -> {
                ic.performContextMenuAction(android.R.id.selectAll)
            }
            is CandidateStripView.ShortcutAction.Copy -> {
                ic.performContextMenuAction(android.R.id.copy)
            }
            is CandidateStripView.ShortcutAction.Paste -> {
                ic.performContextMenuAction(android.R.id.paste)
            }
            is CandidateStripView.ShortcutAction.CursorLeft -> {
                sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT)
            }
            is CandidateStripView.ShortcutAction.CursorRight -> {
                sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT)
            }
        }
    }

    private fun handleKeyAction(action: BongoKeyboardView.KeyAction) {
        val ic = currentInputConnection ?: return

        when (action) {
            is BongoKeyboardView.KeyAction.CharacterKey -> {
                val ch = action.ch

                // If Ctrl is active, dispatch Ctrl shortcut
                if (isCtrlActive) {
                    val lower = ch.lowercaseChar()
                    when (lower) {
                        'a' -> ic.performContextMenuAction(android.R.id.selectAll)
                        'c' -> ic.performContextMenuAction(android.R.id.copy)
                        'v' -> ic.performContextMenuAction(android.R.id.paste)
                        'x' -> ic.performContextMenuAction(android.R.id.cut)
                        'z' -> ic.performContextMenuAction(android.R.id.undo)
                        else -> {
                            val keyCode = when (lower) {
                                'd' -> KeyEvent.KEYCODE_D
                                'l' -> KeyEvent.KEYCODE_L
                                'w' -> KeyEvent.KEYCODE_W
                                'k' -> KeyEvent.KEYCODE_K
                                'u' -> KeyEvent.KEYCODE_U
                                'e' -> KeyEvent.KEYCODE_E
                                else -> KeyEvent.KEYCODE_UNKNOWN
                            }
                            if (keyCode != KeyEvent.KEYCODE_UNKNOWN) {
                                val now = SystemClock.uptimeMillis()
                                ic.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, KeyEvent.META_CTRL_ON))
                                ic.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0, KeyEvent.META_CTRL_ON))
                            }
                        }
                    }
                    isCtrlActive = false
                    candidateStripView?.setCtrlActive(false)
                    return
                }

                if (isBanglaMode && (ch in 'a'..'z' || ch in 'A'..'Z' || ch in "0123456789`~@#\$%^&*()-_=+[{]}\\|;:'\",<.>/?")) {
                    bongoEngine.feedChar(ch, keyboardView?.isShifted == true)
                    val composing = bongoEngine.preedit
                    ic.setComposingText(composing, 1)
                    candidateStripView?.setCandidates(bongoEngine.candidates)
                } else {
                    commitOngoingComposition()
                    ic.commitText(ch.toString(), 1)
                }
            }
            is BongoKeyboardView.KeyAction.Backspace -> {
                if (isBanglaMode && bongoEngine.hasSession()) {
                    val hasRemaining = bongoEngine.handleBackspace()
                    if (hasRemaining && bongoEngine.preedit.isNotEmpty()) {
                        ic.setComposingText(bongoEngine.preedit, 1)
                        candidateStripView?.setCandidates(bongoEngine.candidates)
                    } else {
                        ic.setComposingText("", 0)
                        candidateStripView?.clear()
                    }
                } else {
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
                }
            }
            is BongoKeyboardView.KeyAction.Space -> {
                if (isBanglaMode && bongoEngine.hasSession()) {
                    val committed = bongoEngine.commitCandidate(0)
                    ic.commitText("$committed ", 1)
                    candidateStripView?.clear()
                } else {
                    ic.commitText(" ", 1)
                }
            }
            is BongoKeyboardView.KeyAction.Return -> {
                if (isBanglaMode && bongoEngine.hasSession()) {
                    val committed = bongoEngine.commitCandidate(0)
                    ic.commitText(committed, 1)
                    candidateStripView?.clear()
                }
                val editorInfo = currentInputEditorInfo
                val imeOptions = editorInfo?.imeOptions ?: 0
                val actionId = imeOptions and EditorInfo.IME_MASK_ACTION
                if (actionId != EditorInfo.IME_ACTION_NONE && actionId != EditorInfo.IME_ACTION_UNSPECIFIED) {
                    ic.performEditorAction(actionId)
                } else {
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
                }
            }
            is BongoKeyboardView.KeyAction.ToggleLanguage -> {
                commitOngoingComposition()
                isBanglaMode = !isBanglaMode
                keyboardView?.setBanglaMode(isBanglaMode)
            }
            is BongoKeyboardView.KeyAction.SwitchImePicker -> {
                commitOngoingComposition()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.showInputMethodPicker()
            }
            is BongoKeyboardView.KeyAction.ToggleEmoji -> {
                showEmojiKeyboard()
            }
            is BongoKeyboardView.KeyAction.Esc -> {
                sendDownUpKeyEvents(KeyEvent.KEYCODE_ESCAPE)
            }
            is BongoKeyboardView.KeyAction.Tab -> {
                sendDownUpKeyEvents(KeyEvent.KEYCODE_TAB)
            }
            is BongoKeyboardView.KeyAction.Ctrl -> {
                isCtrlActive = !isCtrlActive
                candidateStripView?.setCtrlActive(isCtrlActive)
            }
            is BongoKeyboardView.KeyAction.Shift,
            is BongoKeyboardView.KeyAction.ToggleSymbols -> {
                // UI state handled internally in BongoKeyboardView
            }
        }
    }

    private fun handleCandidateSelection(index: Int) {
        val ic = currentInputConnection ?: return
        val committed = bongoEngine.commitCandidate(index)
        ic.commitText("$committed ", 1)
        candidateStripView?.clear()
    }

    private fun commitOngoingComposition() {
        val ic = currentInputConnection ?: return
        if (bongoEngine.hasSession()) {
            val committed = bongoEngine.commitCandidate(0)
            ic.commitText(committed, 1)
            candidateStripView?.clear()
        }
    }

    private fun resetComposition() {
        bongoEngine.resetSession()
        isCtrlActive = false
        candidateStripView?.clear()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event == null) return super.onKeyDown(keyCode, event)

        // Shortcut to switch mode: Control + Space
        if (keyCode == KeyEvent.KEYCODE_SPACE && (event.isCtrlPressed || event.isMetaPressed)) {
            commitOngoingComposition()
            isBanglaMode = !isBanglaMode
            keyboardView?.setBanglaMode(isBanglaMode)
            return true
        }

        // Hardware keyboard support for active phonetic typing
        if (isBanglaMode) {
            when (keyCode) {
                KeyEvent.KEYCODE_DEL -> {
                    if (bongoEngine.hasSession()) {
                        handleKeyAction(BongoKeyboardView.KeyAction.Backspace)
                        return true
                    }
                }
                KeyEvent.KEYCODE_SPACE -> {
                    if (bongoEngine.hasSession()) {
                        handleKeyAction(BongoKeyboardView.KeyAction.Space)
                        return true
                    }
                }
                KeyEvent.KEYCODE_ENTER -> {
                    if (bongoEngine.hasSession()) {
                        handleKeyAction(BongoKeyboardView.KeyAction.Return)
                        return true
                    }
                }
                else -> {
                    val unicode = event.keyCharacterMap.get(keyCode, event.metaState)
                    if (unicode > 0 && unicode.toChar().isLetterOrDigit()) {
                        handleKeyAction(BongoKeyboardView.KeyAction.CharacterKey(unicode.toChar()))
                        return true
                    }
                }
            }
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        bongoEngine.destroy()
        super.onDestroy()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
