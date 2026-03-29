package com.denser.hyphen.state

import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.insert
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.TextRange
import com.denser.hyphen.markdown.MarkdownProcessor
import com.denser.hyphen.markdown.MarkdownSerializer
import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.model.MarkupStyleRange
import com.denser.hyphen.model.StyleSets
import kotlinx.coroutines.flow.Flow

/**
 * Hoisted state for a Hyphen markdown text editor.
 *
 * [HyphenTextState] is the single source of truth for all editor content. It holds the raw
 * plain text (stripped of Markdown syntax), the list of active [MarkupStyleRange] spans that
 * describe formatting, pending typing overrides, and the full undo/redo history.
 *
 * Markdown syntax entered by the user is processed by [MarkdownProcessor] and immediately
 * converted into spans — the [text] property therefore always contains clean, undecorated
 * content. Call [toMarkdown] or collect [markdownFlow] to serialize the current state back
 * to a Markdown string.
 *
 * **Typical usage**
 * ```kotlin
 * val state = rememberHyphenTextState()
 *
 * HyphenBasicTextEditor(state = state)
 *
 * // Bold the selected range from a toolbar button
 * Button(onClick = { state.toggleStyle(MarkupStyle.Bold) }) {
 * Text("B")
 * }
 *
 * // Read the result
 * val markdown = state.toMarkdown()
 * ```
 *
 * **Initializing with Markdown**
 *
 * Pass a Markdown string to the constructor and it will be parsed on creation. You can also
 * update the content programmatically later using [setMarkdown]:
 * ```kotlin
 * val state = rememberHyphenTextState("**Hello**, _world_!")
 * // state.text == "Hello, world!"
 * // state.spans contains Bold(0..5) and Italic(7..12)
 * ```
 *
 * @param initialText Plain text or Markdown string used to seed the editor on creation.
 * Markdown syntax is parsed immediately — [text] will contain the clean result.
 */
class HyphenTextState(
    initialText: String = "",
) {
    /**
     * The underlying [TextFieldState] driving the Compose text field.
     *
     * Exposed for integration with [androidx.compose.foundation.text.BasicTextField] and Material3 decorators. Prefer the
     * higher-level API ([text], [selection], [toggleStyle], etc.) for all formatting
     * operations — mutating [textFieldState] directly bypasses span tracking and undo history.
     */
    val textFieldState = TextFieldState()

    /** The current plain text content of the editor, with all Markdown syntax stripped. */
    val text: String get() = textFieldState.text.toString()

    /** The current cursor position or selected range within [text]. */
    val selection: TextRange get() = textFieldState.selection

    internal val _spans = mutableStateListOf<MarkupStyleRange>()

    /**
     * The active list of [MarkupStyleRange] entries describing all inline and block
     * formatting applied to [text]. Observed as Compose snapshot state — any composable
     * reading this list will recompose when spans change.
     */
    val spans: List<MarkupStyleRange> get() = _spans

    /**
     * Transient formatting intent for the next typed character(s).
     *
     * When the cursor is collapsed (no selection) and the user toggles a style, that intent
     * is stored here rather than creating a zero-length span. The override is applied to the
     * next inserted characters and then cleared. A `true` value means the style will be
     * applied; `false` means it will be suppressed even if the cursor sits inside an existing
     * span of that style.
     */
    var pendingOverrides by mutableStateOf(mapOf<MarkupStyle, Boolean>())
        private set

    /** `true` if there is at least one state in the undo stack. */
    val canUndo: Boolean get() = historyManager.canUndo

    /** `true` if there is at least one state in the redo stack. */
    val canRedo: Boolean get() = historyManager.canRedo

    private val historyManager = HistoryManager()
    private val selectionManager = SelectionManager()
    private var isUndoingOrRedoing = false

    /**
     * The link span currently being edited in a dialog.
     */
    var activeLinkForEditing by mutableStateOf<MarkupStyleRange?>(null)

    /**
     * Whether the text field currently has input focus.
     *
     * Kept in sync by [com.denser.hyphen.ui.HyphenBasicTextEditor] via `onFocusChanged`. Used by [SelectionManager]
     * to decide whether to fall back to the last valid selection when a toolbar button is
     * tapped — tapping a button causes the field to lose focus before the click is processed,
     * which would otherwise collapse the selection to zero.
     */
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

    // -------------------------------------------------------------------------
    // Selection
    // -------------------------------------------------------------------------

    /**
     * Notifies the state that the text field's selection has changed.
     *
     * [com.denser.hyphen.ui.HyphenBasicTextEditor] calls this automatically via a `LaunchedEffect`. Non-collapsed
     * selections are remembered so that toolbar operations (which cause the field to lose
     * focus) still act on the correct range. Collapsed cursor movements clear the remembered
     * selection so stale ranges do not persist across unrelated actions.
     *
     * @param newSelection The latest selection reported by the text field.
     */
    fun updateSelection(newSelection: TextRange) {
        selectionManager.onSelectionChanged(newSelection)
    }

    private fun resolvedSelection(): Pair<Int, Int> =
        selectionManager.resolve(selection, text.length)

    // -------------------------------------------------------------------------
    // Input processing
    // -------------------------------------------------------------------------

    /**
     * Processes a raw text change from the input pipeline and keeps spans, cursor position,
     * and undo history consistent.
     *
     * Called from the `inputTransformation` of [com.denser.hyphen.ui.HyphenBasicTextEditor] on every keystroke,
     * paste, or programmatic edit. Responsibilities:
     * - Detects completed Markdown syntax and converts it to spans via [MarkdownProcessor].
     * - Shifts existing spans to account for insertions and deletions.
     * - Applies or suppresses styles for newly inserted characters based on [pendingOverrides].
     * - Saves undo snapshots at word boundaries, after pastes, and after Markdown conversions.
     *
     * Direct callers outside of the editor input pipeline do not normally need to invoke this.
     *
     * @param buffer The mutable buffer provided by the input transformation, representing
     *   the text state after the user's latest edit.
     */
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

        saveSnapshot(force = !canUndo || isPasting || isWordBoundary)

        val deletedNewlines = previousText.count { it == '\n' } > newText.count { it == '\n' }

        val cursorPosition = buffer.selection.start
        val changeOrigin = SpanManager.resolveChangeOrigin(
            cursorPosition,
            rawLengthDifference,
            previousText.length
        )

        var safeSpans = _spans.toList()
        if (rawLengthDifference < 0) {
            val deleteEnd = changeOrigin - rawLengthDifference
            safeSpans = safeSpans.filterNot { span ->
                span.start >= changeOrigin && span.end <= deleteEnd
            }
        }

        val isNewlineInsertion = if (rawLengthDifference > 0) {
            val start = (buffer.selection.start - rawLengthDifference).coerceAtLeast(0)
            buffer.asCharSequence().substring(start, buffer.selection.start).contains('\n')
        } else false

        val availableInlineStyles = (StyleSets.allInline + _spans.map { it.style }.filterIsInstance<MarkupStyle.Link>()).distinct()
        val activeInlineStyles = availableInlineStyles.filter { style ->
            val hasStyleAtCursor = if (isNewlineInsertion) {
                val (selStart, _) = resolvedSelection()
                _spans.any { it.style == style && selStart > it.start && selStart < it.end }
            } else {
                hasStyle(style)
            }
            hasStyleAtCursor && (style !in StyleSets.allHeadings || pendingOverrides[style] == true)
        }

        val markdownResult = MarkdownProcessor.process(newText, cursorPosition)
        var updatedSpans: List<MarkupStyleRange>

        if (markdownResult != null) {
            val cleanLengthDifference = markdownResult.cleanText.length - previousText.length

            var baseSpans = SpanManager.shiftSpans(safeSpans, changeOrigin, cleanLengthDifference)

            if (cleanLengthDifference > 0) {
                val insertEnd = changeOrigin + cleanLengthDifference
                baseSpans = SpanManager.applyTypingOverrides(
                    baseSpans,
                    activeInlineStyles,
                    changeOrigin,
                    insertEnd
                )
            }

            val inlineBaseSpans = baseSpans.filterNot { BlockStyleManager.isBlockStyle(it.style) }
            updatedSpans = SpanManager.mergeSpans(inlineBaseSpans, markdownResult.newSpans)

            buffer.replace(0, buffer.length, markdownResult.cleanText)
            buffer.selection =
                TextRange(markdownResult.newCursorPosition.coerceIn(0, buffer.length))

            val stylesJustClosed = markdownResult.explicitlyClosedStyles

            if (stylesJustClosed.isNotEmpty()) {
                pendingOverrides = pendingOverrides.toMutableMap().apply {
                    stylesJustClosed.forEach { put(it, false) }
                }
            }

            saveSnapshot()
        } else {
            var shifted = SpanManager.shiftSpans(safeSpans, changeOrigin, rawLengthDifference)
            shifted = shifted.filterNot { BlockStyleManager.isBlockStyle(it.style) }

            if (rawLengthDifference > 0) {
                val insertEnd = changeOrigin + rawLengthDifference
                updatedSpans = SpanManager.applyTypingOverrides(
                    shifted,
                    activeInlineStyles,
                    changeOrigin,
                    insertEnd
                )
            } else {
                updatedSpans = shifted
            }
        }

        val finalSpans = updatedSpans.mapNotNull { span ->
            val isHeading = span.style in StyleSets.allHeadings

            if (isHeading) {
                if (span.start >= span.end) return@mapNotNull null

                val bufferStr = buffer.asCharSequence()
                val safeStart = span.start.coerceIn(0, bufferStr.length)

                val lastNewline = bufferStr.lastIndexOf('\n', (safeStart - 1).coerceAtLeast(0))
                val lineStart = if (lastNewline == -1) 0 else lastNewline + 1

                if (deletedNewlines && safeStart > lineStart) {
                    return@mapNotNull null
                }

                val nextNewline = bufferStr.indexOf('\n', lineStart)
                val lineEnd = if (nextNewline == -1) buffer.length else nextNewline

                if (lineStart >= lineEnd) return@mapNotNull null

                span.copy(start = lineStart, end = lineEnd)
            } else {
                span
            }
        }

        _spans.clear()
        _spans.addAll(SpanManager.consolidateSpans(finalSpans))
    }

    // -------------------------------------------------------------------------
    // Style operations
    // -------------------------------------------------------------------------

    /**
     * Toggles a [MarkupStyle] on the current selection, or sets a pending typing override
     * when the cursor is collapsed.
     *
     * - **Inline styles** (Bold, Italic, Underline, etc.): if the entire selection is already
     *   covered by the style, it is removed; otherwise it is added. When there is no selection,
     *   a [pendingOverrides] entry is set so the style applies to the next typed characters.
     * - **Block styles** (BulletList, OrderedList, Blockquote): the appropriate line prefix is
     *   inserted or removed via [BlockStyleManager]. Multiple selected lines are each prefixed
     *   individually.
     *
     * An undo snapshot is saved before every toggle. The remembered selection is cleared
     * after the operation so subsequent actions do not reuse a stale range.
     *
     * @param style The [MarkupStyle] to toggle.
     */
    fun toggleStyle(style: MarkupStyle) {
        saveSnapshot(force = true)

        if (BlockStyleManager.isBlockStyle(style)) {
            applyBlockStyleInternal(style)
        } else {
            applyInlineStyleInternal(style)
        }

        selectionManager.clear()
    }

    /**
     * Toggles the checked/unchecked state of checkboxes.
     * 
     * @param index If provided, toggles the checkbox on the line containing this index. 
     * If null (default), toggles all checkboxes within the current selection.
     */
    fun toggleCheckbox(index: Int? = null) {
        saveSnapshot(force = true)

        val (selStart, selEnd) = if (index != null) index to index else resolvedSelection()
        val lineStarts = mutableListOf<Int>()
        
        var currentIdx = text.lastIndexOf('\n', (selStart - 1).coerceAtLeast(0)) + 1
        if (currentIdx == -1) currentIdx = 0
        
        while (currentIdx <= selEnd) {
            lineStarts.add(currentIdx)
            val nextNewline = text.indexOf('\n', currentIdx)
            if (nextNewline == -1 || nextNewline >= selEnd) break
            currentIdx = nextNewline + 1
        }

        var anyToggled = false
        textFieldState.edit {
            lineStarts.reversed().forEach { start ->
                val spansList = _spans.toList()
                val (toggled, newSpans) = BlockStyleManager.toggleCheckbox(this, spansList, start, strictPrefixCheck = false)
                if (toggled) {
                    anyToggled = true
                    _spans.clear()
                    _spans.addAll(newSpans)
                }
            }
        }

        if (anyToggled) {
            val result = MarkdownProcessor.process(text, selStart)
            val inlineSpans = _spans.filterNot { BlockStyleManager.isBlockStyle(it.style) }

            _spans.clear()
            _spans.addAll(
                if (result != null) {
                    SpanManager.consolidateSpans(SpanManager.mergeSpans(inlineSpans, result.newSpans))
                } else {
                    SpanManager.consolidateSpans(inlineSpans)
                }
            )
        }
        selectionManager.clear()
    }

    /**
     * Removes all inline formatting from the current selection.
     *
     * - **With a selection**: every [MarkupStyleRange] overlapping the selected range is
     *   trimmed or removed. Spans extending beyond the selection boundaries are preserved
     *   outside the cleared region. An undo snapshot is saved before the operation.
     * - **Without a selection (collapsed cursor)**: all active inline styles at the cursor
     *   are suppressed via [pendingOverrides] set to `false`. No spans are mutated and no
     *   snapshot is saved.
     *
     * Block styles (list prefixes, blockquotes) are not affected.
     */
    fun clearAllStyles() {
        val (selStart, selEnd) = resolvedSelection()

        if (selStart == selEnd) {
            pendingOverrides = pendingOverrides.toMutableMap().apply {
                StyleSets.allInline.filter { hasStyle(it) }.forEach { put(it, false) }
                _spans.filter { it.style is MarkupStyle.Link && selStart > it.start && selStart <= it.end }
                    .forEach { put(it.style, false) }
            }
            return
        }

        saveSnapshot(force = true)

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

    /**
     * Toggles a link on the current selection.
     * If the cursor is inside an existing link, it opens it for editing.
     * If there is a selection, it wraps it in a link and opens it for editing.
     */
    fun toggleLink() {
        val (selStart, selEnd) = resolvedSelection()
        
        val existingLink = _spans.find { it.style is MarkupStyle.Link && selStart >= it.start && selStart <= it.end }
        if (existingLink != null) {
            if (selStart == selEnd && selStart == existingLink.end) {
                val isOff = pendingOverrides[existingLink.style] == false
                pendingOverrides = if (isOff) {
                    pendingOverrides - existingLink.style
                } else {
                    pendingOverrides + (existingLink.style to false)
                }
                return
            }
            
            activeLinkForEditing = existingLink
            return
        }
        
        if (selStart != selEnd) {
            saveSnapshot(force = true)
            val newLinkStyle = MarkupStyle.Link("")
            val newSpan = MarkupStyleRange(newLinkStyle, selStart, selEnd)
            
            val updated = _spans.toMutableList()
            updated.add(newSpan)
            
            _spans.clear()
            _spans.addAll(SpanManager.consolidateSpans(updated))
            
            activeLinkForEditing = _spans.find { it.style == newLinkStyle && it.start == selStart } ?: newSpan
        } else {
            saveSnapshot(force = true)
            val placeholder = "link"
            
            textFieldState.edit { insert(selStart, placeholder) }
            
            val shiftedSpans = SpanManager.shiftSpans(_spans, selStart, placeholder.length)
            
            val newLinkStyle = MarkupStyle.Link("")
            val newSpan = MarkupStyleRange(newLinkStyle, selStart, selStart + placeholder.length)
            
            val updated = shiftedSpans.toMutableList()
            updated.add(newSpan)
            
            _spans.clear()
            _spans.addAll(SpanManager.consolidateSpans(updated))
            
            activeLinkForEditing = _spans.find { it.style == newLinkStyle && it.start == selStart } ?: newSpan
        }
    }

    /**
     * Updates an existing link's display text and URL.
     *
     * @param span The current link span to update.
     * @param newText The new display text for the link.
     * @param newUrl The new URL to associate with the link.
     */
    fun updateLink(span: MarkupStyleRange, newText: String, newUrl: String) {
        val index = _spans.indexOf(span)
        if (index == -1) return

        saveSnapshot(force = true)
        
        val lengthDiff = newText.length - (span.end - span.start)

        textFieldState.edit {
            replace(span.start, span.end, newText)
        }

        val shiftedSpans = SpanManager.shiftSpans(_spans, span.start, lengthDiff)

        val finalSpans = shiftedSpans.map { s ->
            if (s.start == span.start && s.style is MarkupStyle.Link && s.style.url == (span.style as MarkupStyle.Link).url) {
                s.copy(style = MarkupStyle.Link(newUrl))
            } else s
        }
        
        _spans.clear()
        _spans.addAll(SpanManager.consolidateSpans(finalSpans))
        selectionManager.clear()
    }

    /**
     * Returns `true` if the given [style] is active at the current selection or cursor.
     *
     * - **Block styles**: delegates to [BlockStyleManager.hasBlockStyle], which inspects
     *   the line prefix at the cursor position.
     * - **Inline styles with a pending override**: the override value takes precedence over
     *   any underlying span, allowing the user to toggle a style on or off at a collapsed
     *   cursor before typing.
     * - **Inline styles with a selection**: returns `true` only if a span of the given style
     *   fully covers the entire selected range.
     * - **Inline styles with a collapsed cursor**: returns `true` if the cursor sits inside
     *   (or at the end boundary of) a span of the given style.
     *
     * @param style The [MarkupStyle] to query.
     */
    fun hasStyle(style: MarkupStyle): Boolean {
        if (BlockStyleManager.isBlockStyle(style)) {
            return BlockStyleManager.hasBlockStyle(text, selection, style)
        }
        
        if (style is MarkupStyle.Link && style.url.isEmpty()) {
            val (selStart, selEnd) = resolvedSelection()
            return if (selStart == selEnd) {
                _spans.any { it.style is MarkupStyle.Link && selStart > it.start && selStart <= it.end }
            } else {
                _spans.any { it.style is MarkupStyle.Link && it.start <= selStart && it.end >= selEnd }
            }
        }

        if (pendingOverrides.containsKey(style)) return pendingOverrides[style] == true

        val (selStart, selEnd) = resolvedSelection()
        return if (selStart == selEnd) {
            _spans.any { span -> span.style == style && selStart > span.start && selStart <= span.end }
        } else {
            val adjustedEnd = if (selEnd > selStart) selEnd - 1 else selEnd
            _spans.any { span -> span.style == style && span.start <= selStart && span.end > adjustedEnd }
        }
    }

    /**
     * Returns `true` if the given [style] is applied at a specific character [index].
     *
     * Unlike [hasStyle], this does not consider the current selection or pending overrides —
     * it is a direct point-in-time query against the span list. Used by
     * [BlockStyleManager.handleSmartEnter] to determine whether a line start carries a list
     * or blockquote style.
     *
     * @param index The character index to query (span range is start-inclusive, end-exclusive).
     * @param style The [MarkupStyle] to check for.
     */
    fun isStyleAt(index: Int, style: MarkupStyle): Boolean =
        _spans.any { it.style == style && index >= it.start && index < it.end }

    /**
     * Clears all entries in [pendingOverrides].
     *
     * Called automatically when the cursor moves or a character is inserted without an active
     * override. Can also be called explicitly to reset typing intent, e.g. when the editor
     * loses focus.
     */
    fun clearPendingOverrides() {
        if (pendingOverrides.isNotEmpty()) pendingOverrides = emptyMap()
    }

    // -------------------------------------------------------------------------
    // Undo / redo
    // -------------------------------------------------------------------------

    /**
     * Reverts the editor to the previous state in the undo history.
     *
     * The current state is pushed onto the redo stack so it can be restored with [redo].
     * Both [text] and [spans] are restored; [pendingOverrides] and the remembered selection
     * are cleared. Has no effect if [canUndo] is `false`.
     */
    fun undo() {
        val previousState = historyManager.undo(getCurrentSnapshot())
        if (previousState != null) {
            restoreSnapshot(previousState)
            selectionManager.clear()
        }
    }

    /**
     * Reapplies the most recently undone state from the redo stack.
     *
     * The current state is pushed back onto the undo stack. Both [text] and [spans] are
     * restored; [pendingOverrides] and the remembered selection are cleared. Has no effect
     * if [canRedo] is `false`.
     */
    fun redo() {
        val nextState = historyManager.redo(getCurrentSnapshot())
        if (nextState != null) {
            restoreSnapshot(nextState)
            selectionManager.clear()
        }
    }

    // -------------------------------------------------------------------------
    // Serialization
    // -------------------------------------------------------------------------

    /**
     * Serializes the current editor content to a Markdown string.
     *
     * Inline styles are wrapped with their corresponding delimiters (e.g. `**bold**`,
     * `*italic*`) and block prefixes (e.g. `- `, `> `) are preserved as-is in the output.
     * The result is suitable for storage, transmission, or pasting into any Markdown-aware
     * surface.
     *
     * An optional character range can be specified to serialize only a substring — used
     * internally by the clipboard integration when the user copies a selection. Out-of-bounds
     * values are clamped silently.
     *
     * @param start Start index of the range to serialize, inclusive. Defaults to `0`.
     * @param end End index of the range to serialize, exclusive. Defaults to [text] length.
     * @return A Markdown-formatted string representing the specified range.
     */
    fun toMarkdown(start: Int = 0, end: Int = text.length): String {
        val safeStart = start.coerceIn(0, text.length)
        val safeEnd = end.coerceIn(safeStart, text.length)
        return MarkdownSerializer.serialize(text, spans, safeStart, safeEnd)
    }

    /**
     * Programmatically replaces the entire contents of the editor with new Markdown.
     * This resets the undo/redo history and clears any pending overrides.
     * * @param markdown The Markdown string to parse and display.
     */
    fun setMarkdown(markdown: String) {
        val markdownResult = MarkdownProcessor.process(markdown, 0)

        textFieldState.edit {
            if (markdownResult != null) {
                replace(0, length, markdownResult.cleanText)
            } else {
                replace(0, length, markdown)
            }
        }

        _spans.clear()
        if (markdownResult != null) {
            _spans.addAll(SpanManager.consolidateSpans(markdownResult.newSpans))
        }

        clearPendingOverrides()
        selectionManager.clear()
        historyManager.clear()
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private fun applyBlockStyleInternal(style: MarkupStyle) {
        val effectiveSel = selectionManager.effectiveSelection(selection, text.length)
        var shiftedSpans = _spans.toList()
        textFieldState.edit {
            shiftedSpans =
                BlockStyleManager.applyBlockStyle(this, shiftedSpans, effectiveSel, style)
        }
        val result = MarkdownProcessor.process(text, selection.start)
        val inlineSpans = shiftedSpans.filterNot { BlockStyleManager.isBlockStyle(it.style) }
        _spans.clear()
        _spans.addAll(
            if (result != null) {
                SpanManager.consolidateSpans(SpanManager.mergeSpans(inlineSpans, result.newSpans))
            } else {
                SpanManager.consolidateSpans(inlineSpans)
            }
        )
    }

    private fun applyInlineStyleInternal(style: MarkupStyle) {
        val isHeading = style in StyleSets.allHeadings

        val (selStart, selEnd) = if (isHeading) {
            val startLine = text.lastIndexOf('\n', (selection.start - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
            val endLine = text.indexOf('\n', selection.end).let { if (it == -1) text.length else it }
            startLine to endLine
        } else {
            resolvedSelection()
        }

        if (selStart == selEnd) {
            pendingOverrides = pendingOverrides + (style to !hasStyle(style))

            if (isHeading && pendingOverrides[style] == true) {
                StyleSets.allHeadings.filter { it != style }.forEach { conflicting ->
                    pendingOverrides = pendingOverrides + (conflicting to false)
                }
            }
            return
        }

        var newSpans = SpanManager.toggleStyle(_spans, style, selStart, selEnd)

        if (isHeading && !hasStyle(style)) {
            val otherHeadings = StyleSets.allHeadings.filter { it != style }

            newSpans = newSpans.flatMap { span ->
                when (span.style) {
                    in otherHeadings -> {
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
                    style -> {
                        if (span.start >= selStart && span.end <= selEnd) {
                            val splitSpans = mutableListOf<MarkupStyleRange>()
                            var currentS = span.start
                            while (currentS < span.end) {
                                val nextNewline = text.indexOf('\n', currentS)
                                val eIdx = if (nextNewline == -1 || nextNewline >= span.end) span.end else nextNewline
                                if (currentS < eIdx) {
                                    splitSpans.add(span.copy(start = currentS, end = eIdx))
                                }
                                currentS = eIdx + 1
                            }
                            splitSpans
                        } else {
                            listOf(span)
                        }
                    }
                    else -> {
                        listOf(span)
                    }
                }
            }
        }

        _spans.clear()
        _spans.addAll(SpanManager.consolidateSpans(newSpans))
    }

    private fun getCurrentSnapshot() = EditorSnapshot(text, selection, _spans.toList())

    private fun saveSnapshot(force: Boolean = false) {
        historyManager.saveSnapshot(getCurrentSnapshot(), force)
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

/**
 * Creates and remembers a [HyphenTextState] across recompositions.
 *
 * @param initialText Plain text or Markdown string used to seed the editor on creation.
 *   See [HyphenTextState] for details on how Markdown is parsed at initialization.
 */
@Composable
fun rememberHyphenTextState(
    initialText: String = ""
): HyphenTextState = remember {
    HyphenTextState(initialText)
}

/**
 * Emits the serialized Markdown string whenever the text or formatting changes.
 * * Useful for observing state changes in a reactive pipeline, such as collecting
 * updates in a ViewModel or applying debouncing before saving to a database.
 */
val HyphenTextState.markdownFlow: Flow<String>
    get() = snapshotFlow {
        val currentText = this.text
        val currentSpans = this.spans.toList()
        MarkdownSerializer.serialize(currentText, currentSpans, 0, currentText.length)
    }