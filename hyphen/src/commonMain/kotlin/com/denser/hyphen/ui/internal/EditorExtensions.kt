package com.denser.hyphen.ui.internal

import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.insert
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.ScrollState
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.em
import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.model.StyleSets
import com.denser.hyphen.model.TextRangeWith
import com.denser.hyphen.state.BlockStyleManager
import com.denser.hyphen.state.HyphenTextState
import com.denser.hyphen.ui.style.HyphenStyleConfig

internal fun handleHardwareKeyEvent(
    event: KeyEvent,
    state: HyphenTextState,
    styleConfig: HyphenStyleConfig = HyphenStyleConfig(),
): Boolean {
    val isKeyDown = event.type == KeyEventType.KeyDown
    if (!isKeyDown) return false

    val isPrimaryModifier = event.isCtrlPressed || event.isMetaPressed
    val isShift = event.isShiftPressed
    val isAlt = event.isAltPressed

    return when {
        isPrimaryModifier && !isShift && !isAlt && event.key == Key.Enter -> {
            state.toggleCheckbox(state.selection.start)
            true
        }
        state.activeTrigger != null && !isPrimaryModifier && !isShift && !isAlt -> {
            when (event.key) {
                Key.DirectionDown -> {
                    if (state.suggestionCount > 0) {
                        state.suggestionSelectedIndex = (state.suggestionSelectedIndex + 1) % state.suggestionCount
                        true
                    } else false
                }
                Key.DirectionUp -> {
                    if (state.suggestionCount > 0) {
                        state.suggestionSelectedIndex = (state.suggestionSelectedIndex - 1 + state.suggestionCount) % state.suggestionCount
                        true
                    } else false
                }
                Key.Enter -> {
                    state.suggestionSelectionRequested = true
                    true
                }
                else -> false
            }
        }
        event.key == Key.Enter && !isPrimaryModifier && !isAlt -> {
            var consumed = false
            val hasSelection = !state.selection.collapsed
            if (!hasSelection && !isShift) {
                state.saveSnapshot(force = true)
                state.textFieldState.edit {
                    consumed = BlockStyleManager.handleSmartEnter(state, this)
                    if (consumed) {
                        state.processInput(this)
                    }
                }
            }
            if (!consumed) {
                state.saveSnapshot(force = true)
                state.textFieldState.edit {
                    val selStart = minOf(selection.start, selection.end)
                    val selEnd = maxOf(selection.start, selection.end)
                    replace(selStart, selEnd, "\n")
                    state.processInput(this)
                }
                consumed = true
            }
            consumed
        }

        event.key == Key.Tab && !isPrimaryModifier && !isAlt -> {
            state.saveSnapshot(force = true)
            state.textFieldState.edit {
                BlockStyleManager.handleIndent(state, this, isShift, styleConfig.indentSpaces)
            }
            true
        }

        isPrimaryModifier && !isShift && !isAlt -> {
            when (event.key) {
                Key.B -> { state.toggleStyle(MarkupStyle.Bold); true }
                Key.I -> { state.toggleStyle(MarkupStyle.Italic); true }
                Key.U -> { state.toggleStyle(MarkupStyle.Underline); true }
                Key.Z -> { state.undo(); true }
                Key.Y -> { state.redo(); true }
                Key.Spacebar -> { state.clearAllStyles(); true }
                Key.One -> { state.toggleStyle(MarkupStyle.H1); true }
                Key.Two -> { state.toggleStyle(MarkupStyle.H2); true }
                Key.Three -> { state.toggleStyle(MarkupStyle.H3); true }
                Key.Four -> { state.toggleStyle(MarkupStyle.H4); true }
                Key.Five -> { state.toggleStyle(MarkupStyle.H5); true }
                Key.Six -> { state.toggleStyle(MarkupStyle.H6); true }
                Key.K -> { state.toggleLink(); true }
                else -> false
            }
        }

        isPrimaryModifier && isShift -> {
            when (event.key) {
                Key.S -> { state.toggleStyle(MarkupStyle.Strikethrough); true }
                Key.H -> { state.toggleStyle(MarkupStyle.Highlight); true }
                Key.X -> { state.toggleStyle(MarkupStyle.Strikethrough); true }
                Key.Z -> { state.redo(); true }
                else -> false
            }
        }

        isPrimaryModifier && isAlt && event.key == Key.X -> {
            state.toggleStyle(MarkupStyle.Strikethrough)
            true
        }

        else -> false
    }
}

internal fun applyMarkdownStyles(
    state: HyphenTextState,
    styleConfig: HyphenStyleConfig,
    baseTextStyle: TextStyle,
    buffer: TextFieldBuffer
) {
    with(buffer) {
        val needsBaselineAnchor = state.spans.any { it.start == 0 && it.style in StyleSets.allHeadings }
        if (needsBaselineAnchor) {
            insert(0, "\u200B")
        }

        val checkboxes = state.spans
            .filter { it.style is MarkupStyle.CheckboxUnchecked || it.style is MarkupStyle.CheckboxChecked }

        val adjustment = if (needsBaselineAnchor) 1 else 0
        val sourceText = state.textFieldState.text.toString()

        val replacements = mutableListOf<TextRangeWith<String>>()

        checkboxes.forEach { cb ->
            val safeStart = (cb.start + adjustment).coerceIn(0, length)
            val safeEnd = (cb.start + adjustment + 6).coerceIn(0, length)
            if (safeStart < safeEnd) {
                replacements.add(TextRangeWith(safeStart, safeEnd, "  "))
            }
        }

        val blockquotes = state.spans
            .filter { it.style is MarkupStyle.Blockquote }

        blockquotes.forEach { bq ->
            val sourceText = state.textFieldState.text.toString()
            val startIdx = bq.start
            val prefixLen = if (startIdx + 1 < sourceText.length && 
                (sourceText[startIdx + 1] == ' ' || sourceText[startIdx + 1] == '\u00A0')) 2 else 1

            val safeStart = (bq.start + adjustment).coerceIn(0, length)
            val safeEnd = (bq.start + adjustment + prefixLen).coerceIn(0, length)
            if (safeStart < safeEnd) {
                replacements.add(TextRangeWith(safeStart, safeEnd, "\u200B"))
            }
        }

        val mentions = state.spans
            .filter { it.style is MarkupStyle.Mention }

        mentions.forEach { m ->
            val safeStart = (m.start + adjustment).coerceIn(0, length)
            if (safeStart < length) {
                val charAtStart = sourceText.getOrNull(m.start)
                if (charAtStart == '@' || charAtStart == '#' || charAtStart == '{') {
                    replacements.add(TextRangeWith(safeStart, safeStart, "\u200E"))
                }
            }
        }

        replacements.sortByDescending { it.start }
        replacements.forEach { rep ->
            if (rep.start < length && rep.end <= length) {
                replace(rep.start, rep.end, rep.value)
            }
        }




        val currentTextSeq = asCharSequence()

        state.spans.forEach { span ->
            val visualStart = HyphenOffsetMapper.toVisual(span.start, state).coerceIn(0, length)
            val visualEnd = HyphenOffsetMapper.toVisual(span.end, state).coerceIn(0, length)
            if (visualStart >= visualEnd) return@forEach

            when (span.style) {
                is MarkupStyle.Bold -> addStyle(styleConfig.boldStyle, visualStart, visualEnd)
                is MarkupStyle.Italic -> addStyle(styleConfig.italicStyle, visualStart, visualEnd)
                is MarkupStyle.Underline -> addStyle(styleConfig.underlineStyle, visualStart, visualEnd)
                is MarkupStyle.Strikethrough -> addStyle(styleConfig.strikethroughStyle, visualStart, visualEnd)
                is MarkupStyle.Highlight -> addStyle(styleConfig.highlightStyle, visualStart, visualEnd)
                is MarkupStyle.InlineCode -> addStyle(styleConfig.inlineCodeStyle, visualStart, visualEnd)
                is MarkupStyle.Link -> addStyle(styleConfig.linkStyle, visualStart, visualEnd)
                is MarkupStyle.Mention -> {
                    val customStyle = styleConfig.mentionStyles[span.style.scheme]
                    addStyle(customStyle ?: styleConfig.mentionStyle, visualStart, visualEnd)
                }
                is MarkupStyle.Blockquote -> addStyle(styleConfig.blockquoteSpanStyle, visualStart, visualEnd)

                is MarkupStyle.BulletList -> {
                    val lineText = currentTextSeq.substring(visualStart, visualEnd)
                    val leadingSpaces = lineText.takeWhile { it == ' ' || it == '\t' }.length
                    val prefixEnd = (visualStart + leadingSpaces + 2).coerceAtMost(visualEnd)
                    styleConfig.bulletListStyle.prefixStyle?.let { addStyle(it, visualStart, prefixEnd) }
                    styleConfig.bulletListStyle.contentStyle?.let { addStyle(it, prefixEnd, visualEnd) }
                }

                is MarkupStyle.OrderedList -> {
                    val lineText = currentTextSeq.substring(visualStart, visualEnd)
                    val leadingSpaces = lineText.takeWhile { it == ' ' || it == '\t' }.length
                    val dotIndex = lineText.indexOf('.', leadingSpaces)
                    val prefixLen = if (dotIndex != -1) (dotIndex + 2).coerceAtMost(lineText.length) else leadingSpaces + 3
                    val prefixEnd = (visualStart + prefixLen).coerceAtMost(visualEnd)
                    styleConfig.orderedListStyle.prefixStyle?.let { addStyle(it, visualStart, prefixEnd) }
                    styleConfig.orderedListStyle.contentStyle?.let { addStyle(it, prefixEnd, visualEnd) }
                }

                is MarkupStyle.CheckboxUnchecked -> {
                    val slotStart = visualStart
                    val slotEnd = (visualStart + 2).coerceAtMost(visualEnd)
                    addStyle(SpanStyle(letterSpacing = 0.8.em), slotStart, slotEnd)
                    styleConfig.checkboxUncheckedStyle?.let { addStyle(it, slotEnd, visualEnd) }
                }

                is MarkupStyle.CheckboxChecked -> {
                    val slotStart = visualStart
                    val slotEnd = (visualStart + 2).coerceAtMost(visualEnd)
                    addStyle(SpanStyle(letterSpacing = 0.8.em), slotStart, slotEnd)
                    styleConfig.checkboxCheckedStyle?.let { addStyle(it, slotEnd, visualEnd) }
                }

                is MarkupStyle.H1 -> addStyle(styleConfig.h1Style, visualStart, visualEnd)
                is MarkupStyle.H2 -> addStyle(styleConfig.h2Style, visualStart, visualEnd)
                is MarkupStyle.H3 -> addStyle(styleConfig.h3Style, visualStart, visualEnd)
                is MarkupStyle.H4 -> addStyle(styleConfig.h4Style, visualStart, visualEnd)
                is MarkupStyle.H5 -> addStyle(styleConfig.h5Style, visualStart, visualEnd)
                is MarkupStyle.H6 -> addStyle(styleConfig.h6Style, visualStart, visualEnd)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
internal fun processMarkdownInput(
    state: HyphenTextState,
    buffer: TextFieldBuffer
) {
    val previousText = state.text
    val newText = buffer.asCharSequence().toString()

    val cursorBefore = state.selection.start
    
    // Check if a single character was deleted (Backspace)
    if (newText.length == previousText.length - 1 && cursorBefore > 0) {
        val lastNewline = previousText.lastIndexOf('\n', (cursorBefore - 1).coerceAtLeast(0))
        val lineStart = if (lastNewline == -1) 0 else lastNewline + 1
        
        if (lineStart > 0 && previousText[lineStart - 1] == '\n') {
            val isBlockquote = state.isStyleAt(lineStart, MarkupStyle.Blockquote)
            if (isBlockquote) {
                val lineEnd = previousText.indexOf('\n', lineStart).let { if (it == -1) previousText.length else it }
                val lineText = previousText.substring(lineStart, lineEnd)
                val isPrefixOnly = lineText == "> " || lineText == ">\u00A0"
                if (isPrefixOnly && cursorBefore >= lineStart && cursorBefore <= lineStart + lineText.length) {
                    buffer.insert(lineStart - 1, "\n")
                    buffer.replace(lineStart, lineStart + lineText.length, "")
                    return
                }
            }
        }

        if (state.isStyleAt(lineStart, MarkupStyle.Blockquote) && cursorBefore == lineStart + 2) {
            if (lineStart < buffer.length && buffer.asCharSequence()[lineStart] == '>') {
                val hasSpace = lineStart + 1 < buffer.length && 
                    (buffer.asCharSequence()[lineStart + 1] == ' ' || buffer.asCharSequence()[lineStart + 1] == '\u00A0')
                if (!hasSpace) {
                    buffer.replace(lineStart, lineStart + 1, "")
                }
            }
        } else if ((state.isStyleAt(lineStart, MarkupStyle.CheckboxUnchecked) || state.isStyleAt(lineStart, MarkupStyle.CheckboxChecked)) && cursorBefore == lineStart + 6) {
            val prefixEnd = lineStart + 5
            if (prefixEnd <= buffer.length) {
                val prefix = buffer.asCharSequence().substring(lineStart, prefixEnd)
                if (prefix == "- [ ]" || prefix == "* [ ]" || prefix == "- [x]" || prefix == "* [x]" || prefix == "- [X]" || prefix == "* [X]") {
                    buffer.replace(lineStart, prefixEnd, "")
                }
            }
        }
    }

    val isSoftEnter = run {
        if (!state.selection.collapsed) return@run false
        var newlineInserted = false
        var newlineIndex = -1
        for (i in 0 until buffer.changes.changeCount) {
            val range = buffer.changes.getRange(i)
            val originalRange = buffer.changes.getOriginalRange(i)
            if (originalRange.collapsed) {
                val insertedText = buffer.asCharSequence().substring(range.min, range.max)
                val nlIdx = insertedText.indexOf('\n')
                if (nlIdx != -1) {
                    newlineInserted = true
                    newlineIndex = range.min + nlIdx
                    break
                }
            }
        }
        newlineInserted && newlineIndex == cursorBefore
    }

    if (isSoftEnter) {
        buffer.revertAllChanges()
        val handled = BlockStyleManager.handleSmartEnter(state, buffer)
        if (!handled) {
            buffer.insert(cursorBefore, "\n")
        }
    }

    state.processInput(buffer)
}

@Composable
internal expect fun rememberMarkdownClipboard(
    state: HyphenTextState,
    clipboardLabel: String,
): Clipboard

internal fun Modifier.drawBlockquotes(
    state: HyphenTextState,
    styleConfig: HyphenStyleConfig,
    textLayoutResult: () -> TextLayoutResult?,
    scrollState: ScrollState,
    horizontalPadding: Dp = 8.dp
): Modifier = this.drawBehind {
    val layout = textLayoutResult() ?: return@drawBehind
    val textLen = layout.layoutInput.text.length
    val scrollY = scrollState.value

    val intervals = state.spans
        .filter { it.style is MarkupStyle.Blockquote }
        .mapNotNull { span ->
            val visualStart = HyphenOffsetMapper.toVisual(span.start, state).coerceIn(0, textLen)
            val visualEnd = HyphenOffsetMapper.toVisual(span.end, state).coerceIn(0, textLen)
            if (visualStart > visualEnd) return@mapNotNull null

            val startLine = layout.getLineForOffset(visualStart)
            val endLine = layout.getLineForOffset((visualEnd - 1).coerceAtLeast(visualStart))

            val top = layout.getLineTop(startLine) - scrollY
            val bottom = layout.getLineBottom(endLine) - scrollY
            top to bottom
        }
        .sortedBy { it.first }

    if (intervals.isNotEmpty()) {
        val mergedIntervals = mutableListOf<Pair<Float, Float>>()
        var current = intervals[0]
        for (i in 1 until intervals.size) {
            val next = intervals[i]
            if (next.first <= current.second + 2f) {
                current = current.first to maxOf(current.second, next.second)
            } else {
                mergedIntervals.add(current)
                current = next
            }
        }
        mergedIntervals.add(current)

        val bqStyle = styleConfig.blockquoteStyle
        val isRtl = layout.layoutInput.layoutDirection == LayoutDirection.Rtl

        mergedIntervals.forEach { (top, bottom) ->
            // Draw background with slightly rounded corners
            val bgLeft = if (isRtl) horizontalPadding.toPx() else 0f
            drawRoundRect(
                color = bqStyle.backgroundColor,
                topLeft = Offset(bgLeft, top),
                size = Size(size.width - horizontalPadding.toPx(), bottom - top),
                cornerRadius = CornerRadius(bqStyle.cornerRadius.toPx())
            )

            // Draw thick border on the right for RTL, left for LTR
            val borderWidth = bqStyle.borderWidth.toPx()
            val borderX = if (isRtl) size.width - borderWidth else 0f
            drawRoundRect(
                color = bqStyle.borderColor,
                topLeft = Offset(borderX, top),
                size = Size(borderWidth, bottom - top),
                cornerRadius = CornerRadius(bqStyle.borderCornerRadius.toPx())
            )
        }
    }
}