package com.denser.hyphen.state

import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import com.denser.hyphen.markdown.MarkdownProcessor
import com.denser.hyphen.markdown.MarkdownSerializer
import com.denser.hyphen.model.MarkupStyleRange
import com.denser.hyphen.model.MarkupStyle

class HyphenTextState(
    initialText: String = "",
) {
    val textFieldState = TextFieldState()

    val text: String get() = textFieldState.text.toString()
    val selection: TextRange get() = textFieldState.selection

    private val _spans = mutableStateListOf<MarkupStyleRange>()
    val spans: List<MarkupStyleRange> get() = _spans

    var pendingOverrides by mutableStateOf(mapOf<MarkupStyle, Boolean>())
        private set

    val canUndo: Boolean get() = historyManager.canUndo
    val canRedo: Boolean get() = historyManager.canRedo

    private val historyManager = EditorHistoryManager()
    private var isUndoingOrRedoing = false

    init {
        val markdownResult = MarkdownProcessor.process(initialText, 0)
        if (markdownResult != null) {
            textFieldState.edit {
                replace(0, length, markdownResult.cleanText)
            }
            _spans.addAll(SpanManager.consolidateSpans(markdownResult.newSpans))
        } else {
            textFieldState.setTextAndPlaceCursorAtEnd(initialText)
        }
    }

    fun processInput(buffer: TextFieldBuffer) {
        if (isUndoingOrRedoing) return

        val previousText = text
        val newText = buffer.asCharSequence().toString()

        if (previousText == newText) {
            if (selection != buffer.selection) clearPendingOverrides()
            return
        }

        val rawLengthDifference = newText.length - previousText.length
        val isPasting = rawLengthDifference > 1 || rawLengthDifference < -1
        val isWordBoundary = newText.lastOrNull()?.isWhitespace() == true || newText.lastOrNull() == '\n'

        if (!canUndo || isPasting || isWordBoundary) {
            saveSnapshot()
        }

        val cursorPosition = buffer.selection.start
        val changeOrigin = SpanManager.resolveChangeOrigin(cursorPosition, rawLengthDifference, previousText.length)

        val activeInlineStyles = listOf(
            MarkupStyle.Bold, MarkupStyle.Italic, MarkupStyle.Underline,
            MarkupStyle.Strikethrough, MarkupStyle.Highlight, MarkupStyle.InlineCode
        ).filter { hasStyle(it) }

        val markdownResult = MarkdownProcessor.process(newText, cursorPosition)
        var updatedSpans: List<MarkupStyleRange>

        if (markdownResult != null) {
            val cleanLengthDifference = markdownResult.cleanText.length - previousText.length
            var baseSpans = SpanManager.shiftSpans(_spans, changeOrigin, cleanLengthDifference)

            if (cleanLengthDifference > 0) {
                val insertEnd = changeOrigin + cleanLengthDifference
                baseSpans = SpanManager.applyTypingOverrides(baseSpans, activeInlineStyles, changeOrigin, insertEnd)
                clearPendingOverrides()
            }

            updatedSpans = SpanManager.mergeSpans(baseSpans, markdownResult.newSpans)

            buffer.replace(0, buffer.length, markdownResult.cleanText)
            buffer.selection = TextRange(markdownResult.newCursorPosition.coerceIn(0, buffer.length))

            val stylesJustClosed = markdownResult.newSpans
                .filter { it.end == markdownResult.newCursorPosition }
                .map { it.style }

            if (stylesJustClosed.isNotEmpty()) {
                val newOverrides = pendingOverrides.toMutableMap()
                stylesJustClosed.forEach { style ->
                    newOverrides[style] = false
                }
                pendingOverrides = newOverrides
            }

            saveSnapshot()
        } else {
            updatedSpans = SpanManager.shiftSpans(_spans, changeOrigin, rawLengthDifference)

            if (rawLengthDifference > 0) {
                val insertEnd = changeOrigin + rawLengthDifference
                updatedSpans = SpanManager.applyTypingOverrides(updatedSpans, activeInlineStyles, changeOrigin, insertEnd)
                clearPendingOverrides()
            }
        }

        _spans.clear()
        _spans.addAll(SpanManager.consolidateSpans(updatedSpans))
    }

    fun toggleStyle(style: MarkupStyle) {
        saveSnapshot()

        if (BlockStyleManager.isBlockStyle(style)) {
            var shiftedSpans = _spans.toList()
            textFieldState.edit {
                shiftedSpans = BlockStyleManager.applyBlockStyle(this, shiftedSpans, selection, style)
            }
            val result = MarkdownProcessor.process(text, selection.start)
            _spans.clear()
            if (result != null) {
                _spans.addAll(SpanManager.consolidateSpans(SpanManager.mergeSpans(shiftedSpans, result.newSpans)))
            } else {
                _spans.addAll(SpanManager.consolidateSpans(shiftedSpans))
            }
        } else {
            val selStart = minOf(selection.start, selection.end)
            val selEnd = maxOf(selection.start, selection.end)

            if (selStart == selEnd) {
                pendingOverrides = pendingOverrides + (style to !hasStyle(style))
                return
            }

            val newSpans = SpanManager.toggleStyle(_spans, style, selStart, selEnd)
            _spans.clear()
            _spans.addAll(newSpans)
        }
    }

    fun hasStyle(style: MarkupStyle): Boolean {
        if (BlockStyleManager.isBlockStyle(style)) {
            return BlockStyleManager.hasBlockStyle(text, selection, style)
        }

        val selStart = minOf(selection.start, selection.end)
        val selEnd = maxOf(selection.start, selection.end)

        if (pendingOverrides.containsKey(style)) return pendingOverrides[style] == true

        return if (selStart == selEnd) {
            _spans.any { span -> span.style == style && selStart >= span.start && selStart <= span.end }
        } else {
            val adjustedEnd = if (selEnd > selStart) selEnd - 1 else selEnd
            _spans.any { span -> span.style == style && span.start <= selStart && span.end > adjustedEnd }
        }
    }

    fun isStyleAt(index: Int, style: MarkupStyle): Boolean =
        _spans.any { it.style == style && index >= it.start && index < it.end }

    fun clearPendingOverrides() {
        if (pendingOverrides.isNotEmpty()) pendingOverrides = emptyMap()
    }

    fun undo() {
        val previousState = historyManager.undo(getCurrentSnapshot())
        if (previousState != null) restoreSnapshot(previousState)
    }

    fun redo() {
        val nextState = historyManager.redo(getCurrentSnapshot())
        if (nextState != null) restoreSnapshot(nextState)
    }

    fun toMarkdown(start: Int = 0, end: Int = text.length): String {
        val safeStart = start.coerceIn(0, text.length)
        val safeEnd = end.coerceIn(safeStart, text.length)
        return MarkdownSerializer.serialize(text, spans, safeStart, safeEnd)
    }

    private fun getCurrentSnapshot() = EditorSnapshot(text, selection, _spans.toList())

    private fun saveSnapshot() {
        historyManager.saveSnapshot(getCurrentSnapshot())
    }

    private fun restoreSnapshot(snapshot: EditorSnapshot) {
        isUndoingOrRedoing = true

        textFieldState.edit {
            replace(0, length, snapshot.text)
            this.selection = snapshot.selection
        }

        _spans.clear()
        _spans.addAll(snapshot.spans)
        clearPendingOverrides()

        isUndoingOrRedoing = false
    }
}

@Composable
fun rememberHyphenTextState(
    initialText: String = ""
): HyphenTextState = remember {
    HyphenTextState(initialText)
}