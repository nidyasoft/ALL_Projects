package com.example.ime

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.KeyEvent
import android.view.inputmethod.InputConnection
import com.example.SmartKeyboardApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.ArrayDeque

data class UndoSnapshot(
    val textBefore: String,
    val textAfter: String
)

class InputConnectionAdapter(
    private val context: Context,
    private val inputConnectionProvider: () -> InputConnection?,
    private val onWordChanged: (currentWord: String, contextText: String) -> Unit
) {
    private val undoStack = ArrayDeque<UndoSnapshot>(25)
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private fun ic(): InputConnection? = inputConnectionProvider()

    fun snapshotState() {
        val conn = ic() ?: return
        val before = conn.getTextBeforeCursor(500, 0)?.toString() ?: ""
        val after = conn.getTextAfterCursor(500, 0)?.toString() ?: ""
        if (undoStack.size >= 25) {
            undoStack.removeFirst()
        }
        undoStack.addLast(UndoSnapshot(before, after))
    }

    fun notifyContextChanged() {
        val conn = ic() ?: return
        val before = conn.getTextBeforeCursor(300, 0)?.toString() ?: ""
        val currentWord = extractCurrentWord(before)
        val contextBeforeWord = if (currentWord.isNotEmpty() && before.length >= currentWord.length) {
            before.substring(0, before.length - currentWord.length)
        } else {
            before
        }
        onWordChanged(currentWord, contextBeforeWord)
    }

    private fun extractCurrentWord(beforeCursor: String): String {
        if (beforeCursor.isEmpty()) return ""
        val separators = charArrayOf(' ', '\n', '\t', '،', '.', ',', '!', '؟', '?', ':', ';', '؛')
        val lastIdx = beforeCursor.lastIndexOfAny(separators)
        return if (lastIdx == -1) {
            beforeCursor
        } else {
            beforeCursor.substring(lastIdx + 1)
        }
    }

    fun commitText(text: String) {
        snapshotState()
        val conn = ic() ?: return

        // If committing a word separator (space/punctuation), learn the completed word + preceding N-gram
        if (text == " " || text == "\n" || text == "." || text == "،" || text == ",") {
            val before = conn.getTextBeforeCursor(200, 0)?.toString() ?: ""
            val completedWord = extractCurrentWord(before)
            if (completedWord.length >= 2) {
                val prevCtx = before.substring(0, before.length - completedWord.length)
                val activeLang = SmartKeyboardApp.instance.preferences.settingsState.value.currentLanguage
                SmartKeyboardApp.instance.predictionEngine.learnWord(
                    context = context,
                    word = completedWord,
                    language = activeLang,
                    previousContextText = prevCtx,
                    saveToPersonalIfNew = false
                )
            }
        }

        conn.commitText(text, 1)
        notifyContextChanged()
    }

    fun commitSuggestion(suggestion: String, saveToPersonal: Boolean = false) {
        val conn = ic() ?: return
        snapshotState()
        val before = conn.getTextBeforeCursor(250, 0)?.toString() ?: ""
        val currentWord = extractCurrentWord(before)
        val contextBeforeWord = if (currentWord.isNotEmpty() && before.length >= currentWord.length) {
            before.substring(0, before.length - currentWord.length)
        } else {
            before
        }
        if (currentWord.isNotEmpty()) {
            conn.deleteSurroundingText(currentWord.length, 0)
        }
        conn.commitText("$suggestion ", 1)
        val activeLang = SmartKeyboardApp.instance.preferences.settingsState.value.currentLanguage
        SmartKeyboardApp.instance.predictionEngine.learnWord(
            context = context,
            word = suggestion,
            language = activeLang,
            previousContextText = contextBeforeWord,
            saveToPersonalIfNew = saveToPersonal
        )
        notifyContextChanged()
    }

    fun deleteBackward() {
        snapshotState()
        val conn = ic() ?: return
        val selected = conn.getSelectedText(0)
        if (!selected.isNullOrEmpty()) {
            conn.commitText("", 1)
            notifyContextChanged()
            return
        }

        val before = conn.getTextBeforeCursor(2, 0)
        if (before.isNullOrEmpty()) return

        // Handle surrogate pairs or unicode combinations properly
        if (before.length >= 2 && Character.isSurrogatePair(before[0], before[1])) {
            conn.deleteSurroundingText(2, 0)
        } else {
            conn.deleteSurroundingText(1, 0)
        }
        notifyContextChanged()
    }

    fun deleteForward() {
        snapshotState()
        val conn = ic() ?: return
        val selected = conn.getSelectedText(0)
        if (!selected.isNullOrEmpty()) {
            conn.commitText("", 1)
            notifyContextChanged()
            return
        }
        conn.deleteSurroundingText(0, 1)
        notifyContextChanged()
    }

    fun sendEnter() {
        snapshotState()
        val conn = ic() ?: return
        val before = conn.getTextBeforeCursor(200, 0)?.toString() ?: ""
        val completedWord = extractCurrentWord(before)
        if (completedWord.length >= 2) {
            val prevCtx = before.substring(0, before.length - completedWord.length)
            val activeLang = SmartKeyboardApp.instance.preferences.settingsState.value.currentLanguage
            SmartKeyboardApp.instance.predictionEngine.learnWord(
                context = context,
                word = completedWord,
                language = activeLang,
                previousContextText = prevCtx,
                saveToPersonalIfNew = false
            )
        }
        conn.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        conn.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        notifyContextChanged()
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val previous = undoStack.removeLast()
        val conn = ic() ?: return

        // Clear local surrounding area and re-inject previous state
        val before = conn.getTextBeforeCursor(500, 0)?.toString() ?: ""
        val after = conn.getTextAfterCursor(500, 0)?.toString() ?: ""
        conn.deleteSurroundingText(before.length, after.length)
        conn.commitText(previous.textBefore, 1)
        val pos = previous.textBefore.length
        conn.commitText(previous.textAfter, 0)
        conn.setSelection(pos, pos)
        notifyContextChanged()
    }

    fun selectAll() {
        val conn = ic() ?: return
        // Try performContextMenuAction first
        val handled = conn.performContextMenuAction(android.R.id.selectAll)
        if (!handled) {
            val before = conn.getTextBeforeCursor(5000, 0)?.toString() ?: ""
            val after = conn.getTextAfterCursor(5000, 0)?.toString() ?: ""
            val totalLength = before.length + after.length
            conn.setSelection(0, totalLength)
        }
    }

    fun copy(pinImmediately: Boolean = false) {
        val conn = ic() ?: return
        val selected = conn.getSelectedText(0)?.toString()
        if (!selected.isNullOrEmpty()) {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            cm?.setPrimaryClip(ClipData.newPlainText("Copied Text", selected))
            coroutineScope.launch {
                SmartKeyboardApp.instance.clipboardRepository.addClip(selected, pinImmediately)
            }
        } else {
            conn.performContextMenuAction(android.R.id.copy)
            coroutineScope.launch {
                kotlinx.coroutines.delay(80L)
                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                val clip = cm?.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()
                if (!clip.isNullOrEmpty()) {
                    SmartKeyboardApp.instance.clipboardRepository.addClip(clip, pinImmediately)
                }
            }
        }
    }

    fun cut() {
        val conn = ic() ?: return
        snapshotState()
        val selected = conn.getSelectedText(0)?.toString()
        if (!selected.isNullOrEmpty()) {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            cm?.setPrimaryClip(ClipData.newPlainText("Cut Text", selected))
            coroutineScope.launch {
                SmartKeyboardApp.instance.clipboardRepository.addClip(selected, false)
            }
            conn.commitText("", 1)
        } else {
            conn.performContextMenuAction(android.R.id.cut)
            coroutineScope.launch {
                kotlinx.coroutines.delay(80L)
                SmartKeyboardApp.instance.syncSystemClipboard()
            }
        }
        notifyContextChanged()
    }

    fun paste() {
        snapshotState()
        val conn = ic() ?: return
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = cm?.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()
        if (!clip.isNullOrEmpty()) {
            conn.commitText(clip, 1)
            notifyContextChanged()
        } else {
            conn.performContextMenuAction(android.R.id.paste)
            notifyContextChanged()
        }
    }

    fun pasteExplicit(text: String) {
        snapshotState()
        val conn = ic() ?: return
        conn.commitText(text, 1)
        notifyContextChanged()
    }

    fun moveHome() {
        val conn = ic() ?: return
        val before = conn.getTextBeforeCursor(2000, 0)?.toString() ?: ""
        val lastNewline = before.lastIndexOf('\n')
        if (lastNewline != -1) {
            val moveCount = before.length - (lastNewline + 1)
            val currentPos = before.length
            val targetPos = currentPos - moveCount
            conn.setSelection(targetPos, targetPos)
        } else {
            conn.setSelection(0, 0)
        }
    }

    fun moveEnd() {
        val conn = ic() ?: return
        val before = conn.getTextBeforeCursor(2000, 0)?.toString() ?: ""
        val after = conn.getTextAfterCursor(2000, 0)?.toString() ?: ""
        val nextNewline = after.indexOf('\n')
        val targetPos = if (nextNewline != -1) {
            before.length + nextNewline
        } else {
            before.length + after.length
        }
        conn.setSelection(targetPos, targetPos)
    }

    fun moveHomeAll() {
        val conn = ic() ?: return
        conn.setSelection(0, 0)
    }

    fun moveEndAll() {
        val conn = ic() ?: return
        val before = conn.getTextBeforeCursor(10000, 0)?.toString() ?: ""
        val after = conn.getTextAfterCursor(10000, 0)?.toString() ?: ""
        val total = before.length + after.length
        conn.setSelection(total, total)
    }
}
