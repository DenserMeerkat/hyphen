package com.denser.hyphen.state

import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextRange
import com.denser.hyphen.markdown.MarkdownProcessor
import com.denser.hyphen.markdown.MarkdownSerializer
import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.model.MarkupStyleRange
import com.denser.hyphen.model.StyleSets

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

    private val historyManager = HistoryManager()
    private val selectionManager = SelectionManager()
    private var isUndoingOrRedoing = false

    var isFocused: Boolean
        get() = selectionManager.isFocused
        set(value) {
            selectionManager.isFocused = value
        }

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

    fun updateSelection(newSelection: TextRange) {
        selectionManager.onSelectionChanged(newSelection)
    }

    private fun resolvedSelection(): Pair<Int, Int> =
        selectionManager.resolve(selection)

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
        val isWordBoundary =
            newText.lastOrNull()?.isWhitespace() == true || newText.lastOrNull() == '\n'

        if (!canUndo || isPasting || isWordBoundary) saveSnapshot()

        val cursorPosition = buffer.selection.start
        val changeOrigin = SpanManager.resolveChangeOrigin(
            cursorPosition,
            rawLengthDifference,
            previousText.length
        )
        val activeInlineStyles = StyleSets.allInline.filter { hasStyle(it) }
        val markdownResult = MarkdownProcessor.process(newText, cursorPosition)
        var updatedSpans: List<MarkupStyleRange>

        if (markdownResult != null) {
            val cleanLengthDifference = markdownResult.cleanText.length - previousText.length
            var baseSpans = SpanManager.shiftSpans(_spans, changeOrigin, cleanLengthDifference)

            if (cleanLengthDifference > 0) {
                val insertEnd = changeOrigin + cleanLengthDifference
                baseSpans = SpanManager.applyTypingOverrides(
                    baseSpans,
                    activeInlineStyles,
                    changeOrigin,
                    insertEnd
                )
                clearPendingOverrides()
            }

            updatedSpans = SpanManager.mergeSpans(baseSpans, markdownResult.newSpans)

            buffer.replace(0, buffer.length, markdownResult.cleanText)
            buffer.selection =
                TextRange(markdownResult.newCursorPosition.coerceIn(0, buffer.length))

            val stylesJustClosed = markdownResult.newSpans
                .filter { it.end == markdownResult.newCursorPosition }
                .map { it.style }

            if (stylesJustClosed.isNotEmpty()) {
                pendingOverrides = pendingOverrides.toMutableMap().apply {
                    stylesJustClosed.forEach { put(it, false) }
                }
            }

            saveSnapshot()
        } else {
            updatedSpans = SpanManager.shiftSpans(_spans, changeOrigin, rawLengthDifference)

            if (rawLengthDifference > 0) {
                val insertEnd = changeOrigin + rawLengthDifference
                updatedSpans = SpanManager.applyTypingOverrides(
                    updatedSpans,
                    activeInlineStyles,
                    changeOrigin,
                    insertEnd
                )
                clearPendingOverrides()
            }
        }

        _spans.clear()
        _spans.addAll(SpanManager.consolidateSpans(updatedSpans))
    }

    fun toggleStyle(style: MarkupStyle) {
        saveSnapshot()

        if (BlockStyleManager.isBlockStyle(style)) {
            applyBlockStyleInternal(style)
        } else {
            applyInlineStyleInternal(style)
        }

        selectionManager.clear()
    }

    fun clearAllStyles() {
        val (selStart, selEnd) = resolvedSelection()

        if (selStart == selEnd) {
            pendingOverrides = pendingOverrides.toMutableMap().apply {
                StyleSets.allInline.filter { hasStyle(it) }.forEach { put(it, false) }
            }
            return
        }

        saveSnapshot()

        val updatedSpans = _spans.flatMap { span ->
            when {
                span.end <= selStart || span.start >= selEnd -> listOf(span)
                span.start >= selStart && span.end <= selEnd -> emptyList()
                span.start < selStart && span.end > selEnd -> listOf(
                    span.copy(end = selStart),
                    span.copy(start = selEnd),
                )

                span.start < selStart -> listOf(span.copy(end = selStart))
                else -> listOf(span.copy(start = selEnd))
            }
        }

        _spans.clear()
        _spans.addAll(SpanManager.consolidateSpans(updatedSpans))
        clearPendingOverrides()
        selectionManager.clear()
    }

    fun hasStyle(style: MarkupStyle): Boolean {
        if (BlockStyleManager.isBlockStyle(style)) {
            return BlockStyleManager.hasBlockStyle(text, selection, style)
        }
        if (pendingOverrides.containsKey(style)) return pendingOverrides[style] == true

        val (selStart, selEnd) = resolvedSelection()
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
        if (previousState != null) {
            restoreSnapshot(previousState)
            selectionManager.clear()
        }
    }

    fun redo() {
        val nextState = historyManager.redo(getCurrentSnapshot())
        if (nextState != null) {
            restoreSnapshot(nextState)
            selectionManager.clear()
        }
    }

    fun toMarkdown(start: Int = 0, end: Int = text.length): String {
        val safeStart = start.coerceIn(0, text.length)
        val safeEnd = end.coerceIn(safeStart, text.length)
        return MarkdownSerializer.serialize(text, spans, safeStart, safeEnd)
    }

    private fun applyBlockStyleInternal(style: MarkupStyle) {
        val effectiveSel = selectionManager.effectiveSelection(selection)
        var shiftedSpans = _spans.toList()
        textFieldState.edit {
            shiftedSpans =
                BlockStyleManager.applyBlockStyle(this, shiftedSpans, effectiveSel, style)
        }
        val result = MarkdownProcessor.process(text, selection.start)
        _spans.clear()
        _spans.addAll(
            if (result != null) {
                SpanManager.consolidateSpans(SpanManager.mergeSpans(shiftedSpans, result.newSpans))
            } else {
                SpanManager.consolidateSpans(shiftedSpans)
            }
        )
    }

    private fun applyInlineStyleInternal(style: MarkupStyle) {
        val (selStart, selEnd) = resolvedSelection()
        if (selStart == selEnd) {
            pendingOverrides = pendingOverrides + (style to !hasStyle(style))
            return
        }
        val newSpans = SpanManager.toggleStyle(_spans, style, selStart, selEnd)
        _spans.clear()
        _spans.addAll(newSpans)
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