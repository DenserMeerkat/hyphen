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
import androidx.compose.ui.text.TextStyle
import com.denser.hyphen.core.model.MarkupStyle

import com.denser.hyphen.core.model.StyleSets
import com.denser.hyphen.core.constants.EditorConstants
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
            var consumed = false
            state.textFieldState.edit {
                val toggled = BlockStyleManager.toggleCheckbox(this, selection.start, strictPrefixCheck = false)
                if (toggled) {
                    state.processInput(this)
                    consumed = true
                }
            }
            consumed
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
            insert(0, EditorConstants.ZWSP)
        }
        val offset = if (needsBaselineAnchor) 1 else 0
        insert(length, EditorConstants.ZWSP)

        val baseSpanStyle = baseTextStyle.toSpanStyle()
        val textSeq = asCharSequence()
        for (i in textSeq.indices) {
            if (textSeq[i] == '\n') {
                addStyle(baseSpanStyle, i, i + 1)
            }
        }

        val sortedSpans = state.spans.sortedBy { span ->
            if (span.style in StyleSets.allHeadings) 0 else 1
        }

        sortedSpans.forEach { span ->
            val safeStart = (span.start + offset).coerceIn(0, length)
            val safeEnd = (span.end + offset).coerceIn(0, length)
            if (safeStart >= safeEnd) return@forEach

            when (span.style) {
                is MarkupStyle.Bold -> addStyle(styleConfig.boldStyle, safeStart, safeEnd)
                is MarkupStyle.Italic -> addStyle(styleConfig.italicStyle, safeStart, safeEnd)
                is MarkupStyle.Underline -> addStyle(styleConfig.underlineStyle, safeStart, safeEnd)
                is MarkupStyle.Strikethrough -> addStyle(styleConfig.strikethroughStyle, safeStart, safeEnd)
                is MarkupStyle.Highlight -> addStyle(styleConfig.highlightStyle, safeStart, safeEnd)
                is MarkupStyle.InlineCode -> addStyle(styleConfig.inlineCodeStyle, safeStart, safeEnd)
                is MarkupStyle.Blockquote -> addStyle(styleConfig.blockquoteSpanStyle, safeStart, safeEnd)

                is MarkupStyle.BulletList -> {
                    val prefixEnd = (safeStart + 2).coerceAtMost(safeEnd)
                    styleConfig.bulletListStyle.prefixStyle?.let { addStyle(it, safeStart, prefixEnd) }
                    styleConfig.bulletListStyle.contentStyle?.let { addStyle(it, prefixEnd, safeEnd) }
                }

                is MarkupStyle.OrderedList -> {
                    val lineText = asCharSequence().substring(safeStart, safeEnd)
                    val dotIndex = lineText.indexOf('.')
                    val prefixLen = if (dotIndex != -1) (dotIndex + 2).coerceAtMost(lineText.length) else 3
                    val prefixEnd = (safeStart + prefixLen).coerceAtMost(safeEnd)
                    styleConfig.orderedListStyle.prefixStyle?.let { addStyle(it, safeStart, prefixEnd) }
                    styleConfig.orderedListStyle.contentStyle?.let { addStyle(it, prefixEnd, safeEnd) }
                }

                is MarkupStyle.CheckboxUnchecked -> {
                    val prefixEnd = (safeStart + 6).coerceAtMost(safeEnd)
                    styleConfig.checkboxUncheckedStyle.prefixStyle?.let { addStyle(it, safeStart, prefixEnd) }
                    styleConfig.checkboxUncheckedStyle.contentStyle?.let { addStyle(it, prefixEnd, safeEnd) }
                }

                is MarkupStyle.CheckboxChecked -> {
                    val prefixEnd = (safeStart + 6).coerceAtMost(safeEnd)
                    styleConfig.checkboxCheckedStyle.prefixStyle?.let { addStyle(it, safeStart, prefixEnd) }
                    styleConfig.checkboxCheckedStyle.contentStyle?.let { addStyle(it, prefixEnd, safeEnd) }
                }

                is MarkupStyle.H1 -> addStyle(styleConfig.h1Style, safeStart, safeEnd)
                is MarkupStyle.H2 -> addStyle(styleConfig.h2Style, safeStart, safeEnd)
                is MarkupStyle.H3 -> addStyle(styleConfig.h3Style, safeStart, safeEnd)
                is MarkupStyle.H4 -> addStyle(styleConfig.h4Style, safeStart, safeEnd)
                is MarkupStyle.H5 -> addStyle(styleConfig.h5Style, safeStart, safeEnd)
                is MarkupStyle.H6 -> addStyle(styleConfig.h6Style, safeStart, safeEnd)
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