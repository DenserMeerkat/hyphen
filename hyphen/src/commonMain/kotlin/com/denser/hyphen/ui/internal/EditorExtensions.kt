package com.denser.hyphen.ui.internal

import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.insert
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
import com.denser.hyphen.state.BlockStyleManager
import com.denser.hyphen.state.HyphenTextState
import com.denser.hyphen.ui.style.HyphenStyleConfig

internal fun handleHardwareKeyEvent(
    event: KeyEvent,
    state: HyphenTextState
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
        event.key == Key.Enter && !isPrimaryModifier && !isShift && !isAlt -> {
            var consumed = false
            state.textFieldState.edit {
                val handled = BlockStyleManager.handleSmartEnter(state, this)
                if (handled) {
                    state.processInput(this)
                    consumed = true
                }
            }
            consumed
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
            .sortedByDescending { it.start }

        val adjustment = if (needsBaselineAnchor) 1 else 0
        checkboxes.forEach { cb ->
            val safeStart = (cb.start + adjustment).coerceIn(0, length)
            val safeEnd = (cb.start + adjustment + 6).coerceIn(0, length)
            if (safeStart < safeEnd) {
                replace(safeStart, safeEnd, "  ")
            }
        }

        val blockquotes = state.spans
            .filter { it.style is MarkupStyle.Blockquote }
            .sortedByDescending { it.start }

        blockquotes.forEach { bq ->
            val safeStart = (bq.start + adjustment).coerceIn(0, length)
            val safeEnd = (bq.start + adjustment + 1).coerceIn(0, length)
            if (safeStart < safeEnd) {
                replace(safeStart, safeEnd, " ")
            }
        }

        val baseSpanStyle = baseTextStyle.toSpanStyle()
        val currentTextSeq = asCharSequence()
        for (i in currentTextSeq.indices) {
            if (currentTextSeq[i] == '\n') {
                addStyle(baseSpanStyle, i, i + 1)
            }
        }

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
                    val prefixEnd = (visualStart + 2).coerceAtMost(visualEnd)
                    styleConfig.bulletListStyle.prefixStyle?.let { addStyle(it, visualStart, prefixEnd) }
                    styleConfig.bulletListStyle.contentStyle?.let { addStyle(it, prefixEnd, visualEnd) }
                }

                is MarkupStyle.OrderedList -> {
                    val lineText = currentTextSeq.substring(visualStart, visualEnd)
                    val dotIndex = lineText.indexOf('.')
                    val prefixLen = if (dotIndex != -1) (dotIndex + 2).coerceAtMost(lineText.length) else 3
                    val prefixEnd = (visualStart + prefixLen).coerceAtMost(visualEnd)
                    styleConfig.orderedListStyle.prefixStyle?.let { addStyle(it, visualStart, prefixEnd) }
                    styleConfig.orderedListStyle.contentStyle?.let { addStyle(it, prefixEnd, visualEnd) }
                }

                is MarkupStyle.CheckboxUnchecked -> {
                    val slotEnd = (visualStart + 2).coerceAtMost(visualEnd)
                    addStyle(SpanStyle(letterSpacing = 0.8.em), visualStart, slotEnd)
                    styleConfig.checkboxUncheckedStyle?.let { addStyle(it, slotEnd, visualEnd) }
                }

                is MarkupStyle.CheckboxChecked -> {
                    val slotEnd = (visualStart + 2).coerceAtMost(visualEnd)
                    addStyle(SpanStyle(letterSpacing = 0.8.em), visualStart, slotEnd)
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
        
        if (state.isStyleAt(lineStart, MarkupStyle.Blockquote) && cursorBefore == lineStart + 2) {
            if (lineStart < buffer.length && buffer.asCharSequence()[lineStart] == '>') {
                buffer.replace(lineStart, lineStart + 1, "")
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

    val isSoftEnter = cursorBefore < buffer.length &&
            buffer.asCharSequence()[cursorBefore] == '\n' &&
            buffer.length == previousText.length + 1 &&
            buffer.asCharSequence().toString().removeRange(cursorBefore, cursorBefore + 1) == previousText

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
    scrollState: ScrollState
): Modifier = this.drawBehind {
    val layout = textLayoutResult() ?: return@drawBehind
    val textLen = layout.layoutInput.text.length
    val scrollY = scrollState.value

    val intervals = state.spans
        .filter { it.style is MarkupStyle.Blockquote }
        .mapNotNull { span ->
            val visualStart = HyphenOffsetMapper.toVisual(span.start, state).coerceIn(0, textLen)
            val visualEnd = HyphenOffsetMapper.toVisual(span.end, state).coerceIn(0, textLen)
            if (visualStart >= visualEnd) return@mapNotNull null

            val startLine = layout.getLineForOffset(visualStart)
            val endLine = layout.getLineForOffset(visualEnd)

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

        mergedIntervals.forEach { (top, bottom) ->
            // Draw background with slightly rounded corners
            drawRoundRect(
                color = bqStyle.backgroundColor,
                topLeft = Offset(0f, top),
                size = Size(size.width - 8.dp.toPx(), bottom - top),
                cornerRadius = CornerRadius(bqStyle.cornerRadius.toPx())
            )

            // Draw thick border on the left with rounded corners
            val borderWidth = bqStyle.borderWidth.toPx()
            val borderLeft = 4.dp.toPx()
            drawRoundRect(
                color = bqStyle.borderColor,
                topLeft = Offset(borderLeft, top),
                size = Size(borderWidth, bottom - top),
                cornerRadius = CornerRadius(bqStyle.borderCornerRadius.toPx())
            )
        }
    }
}