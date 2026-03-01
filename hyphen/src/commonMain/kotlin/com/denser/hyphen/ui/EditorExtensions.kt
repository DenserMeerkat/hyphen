package com.denser.hyphen.ui

import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.insert
import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.Clipboard
import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.state.BlockStyleManager
import com.denser.hyphen.state.HyphenTextState

internal fun handleHardwareKeyEvent(
    event: KeyEvent,
    state: HyphenTextState
): Boolean {
    return when {
        event.key == Key.Enter && event.type == KeyEventType.KeyDown -> {
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
        else -> false
    }
}

internal fun applyMarkdownStyles(
    state: HyphenTextState,
    styleConfig: HyphenStyleConfig,
    buffer: TextFieldBuffer
) {
    with(buffer) {
        state.spans.forEach { span ->
            val safeStart = span.start.coerceIn(0, length)
            val safeEnd = span.end.coerceIn(0, length)
            if (safeStart >= safeEnd) return@forEach

            when (span.style) {
                is MarkupStyle.Bold -> addStyle(styleConfig.boldStyle, safeStart, safeEnd)
                is MarkupStyle.Italic -> addStyle(styleConfig.italicStyle, safeStart, safeEnd)
                is MarkupStyle.Underline -> addStyle(styleConfig.underlineStyle, safeStart, safeEnd)
                is MarkupStyle.Strikethrough -> addStyle(styleConfig.strikethroughStyle, safeStart, safeEnd)
                is MarkupStyle.Highlight -> addStyle(styleConfig.highlightStyle, safeStart, safeEnd)
                is MarkupStyle.InlineCode -> addStyle(styleConfig.inlineCodeStyle, safeStart, safeEnd)
                is MarkupStyle.BulletList -> {}
                is MarkupStyle.OrderedList -> {}
                is MarkupStyle.Blockquote -> addStyle(styleConfig.blockquoteSpanStyle, safeStart, safeEnd)
            }
        }
    }
}

internal fun processMarkdownInput(
    state: HyphenTextState,
    onValueChange: ((String) -> Unit)?,
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
    onValueChange?.invoke(buffer.asCharSequence().toString())
}

@Composable
internal expect fun rememberMarkdownClipboard(
    state: HyphenTextState,
    clipboardLabel: String,
): Clipboard