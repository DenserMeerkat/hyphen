package com.denser.hyphen.ui

import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.insert
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
    val isSoftEnter = cursorBefore < newText.length &&
            newText[cursorBefore] == '\n' &&
            newText.length == previousText.length + 1 &&
            newText.removeRange(cursorBefore, cursorBefore + 1) == previousText

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