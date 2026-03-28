package com.denser.hyphen.inline.state

import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import com.denser.hyphen.core.markdown.MarkdownProcessor
import com.denser.hyphen.core.model.MarkupStyle
import com.denser.hyphen.core.model.StyleRange
import com.denser.hyphen.core.model.StyleSets
import com.denser.hyphen.core.state.SelectionManager
import com.denser.hyphen.core.state.SpanManager

class HyphenInlineState(
    val fieldState: TextFieldState,
    initialSpans: List<StyleRange> = emptyList(),
    private val onSnapshotRequested: () -> Unit,
    private val onSpansChanged: (List<StyleRange>) -> Unit = {},
) {
    private val _spans = mutableStateListOf<StyleRange>().apply { addAll(initialSpans) }

    val spans: List<StyleRange> get() = _spans

    var pendingOverrides by mutableStateOf(mapOf<MarkupStyle.Inline, Boolean>())
        private set

    private val selectionManager = SelectionManager()

    var isFocused: Boolean
        get() = selectionManager.isFocused
        set(value) { selectionManager.isFocused = value }

    fun updateSelection(newSelection: TextRange) {
        selectionManager.onSelectionChanged(newSelection)
    }

    private fun resolvedSelection(): Pair<Int, Int> =
        selectionManager.resolve(fieldState.selection)

    private fun commitSpans(updated: List<StyleRange>) {
        _spans.clear()
        _spans.addAll(updated)
        onSpansChanged(updated)
    }

    internal fun processInput(buffer: TextFieldBuffer) {
        val previousText = fieldState.text.toString()
        val newText = buffer.asCharSequence().toString()

        if (previousText == newText) {
            if (fieldState.selection != buffer.selection) clearPendingOverrides()
            return
        }

        val rawDelta = newText.length - previousText.length

        val cursorPosition = buffer.selection.start
        val changeOrigin = SpanManager.resolveChangeOrigin(cursorPosition, rawDelta, previousText.length)
        val activeStyles = StyleSets.allInline.filter { hasStyle(it) }
        val markdownResult = MarkdownProcessor.process(newText, cursorPosition, inlineOnly = true)
        val updatedSpans: List<StyleRange>

        if (markdownResult != null) {
            val cleanDelta = markdownResult.cleanText.length - previousText.length
            var baseSpans = SpanManager.shiftSpans(_spans, changeOrigin, cleanDelta)

            if (cleanDelta > 0) {
                baseSpans = SpanManager.applyTypingOverrides(
                    baseSpans, activeStyles, changeOrigin, changeOrigin + cleanDelta
                )
                clearPendingOverrides()
            }

            updatedSpans = SpanManager.mergeSpans(baseSpans, markdownResult.newSpans)

            buffer.replace(0, buffer.length, markdownResult.cleanText)
            buffer.selection = TextRange(markdownResult.newCursorPosition.coerceIn(0, buffer.length))

            if (markdownResult.explicitlyClosedStyles.isNotEmpty()) {
                pendingOverrides = pendingOverrides.toMutableMap().apply {
                    markdownResult.explicitlyClosedStyles
                        .filterIsInstance<MarkupStyle.Inline>()
                        .forEach { put(it, false) }
                }
            }

            onSnapshotRequested()
        } else {
            val shifted = SpanManager.shiftSpans(_spans, changeOrigin, rawDelta)
            updatedSpans = if (rawDelta > 0) {
                val result = SpanManager.applyTypingOverrides(
                    shifted, activeStyles, changeOrigin, changeOrigin + rawDelta
                )
                clearPendingOverrides()
                result
            } else {
                shifted
            }
        }

        commitSpans(SpanManager.consolidateSpans(updatedSpans))
    }

    fun toggleStyle(style: MarkupStyle.Inline) {
        onSnapshotRequested()
        val (selStart, selEnd) = resolvedSelection()

        if (selStart == selEnd) {
            pendingOverrides = pendingOverrides + (style to !hasStyle(style))
            selectionManager.clear()
            return
        }

        commitSpans(SpanManager.toggleStyle(_spans, style, selStart, selEnd))
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

        onSnapshotRequested()

        val updated = _spans.flatMap { span ->
            when {
                span.end <= selStart || span.start >= selEnd -> listOf(span)
                span.start >= selStart && span.end <= selEnd -> emptyList()
                span.start < selStart && span.end > selEnd ->
                    listOf(span.copy(end = selStart), span.copy(start = selEnd))
                span.start < selStart -> listOf(span.copy(end = selStart))
                else -> listOf(span.copy(start = selEnd))
            }
        }

        commitSpans(SpanManager.consolidateSpans(updated))
        clearPendingOverrides()
        selectionManager.clear()
    }

    fun hasStyle(style: MarkupStyle.Inline): Boolean {
        if (pendingOverrides.containsKey(style)) return pendingOverrides[style] == true
        val (selStart, selEnd) = resolvedSelection()
        return if (selStart == selEnd) {
            _spans.any { it.style == style && selStart > it.start && selStart <= it.end }
        } else {
            val adjustedEnd = if (selEnd > selStart) selEnd - 1 else selEnd
            _spans.any { it.style == style && it.start <= selStart && it.end > adjustedEnd }
        }
    }

    fun clearPendingOverrides() {
        if (pendingOverrides.isNotEmpty()) pendingOverrides = emptyMap()
    }

    fun syncSpans(newSpans: List<StyleRange>) {
        _spans.clear()
        _spans.addAll(newSpans)
    }
}