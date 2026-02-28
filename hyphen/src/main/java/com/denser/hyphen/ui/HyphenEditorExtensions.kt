package com.denser.hyphen.ui

import android.content.ClipData
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.insert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.NativeClipboard
import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.state.BlockStyleManager
import com.denser.hyphen.state.HyphenTextState

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
internal fun rememberMarkdownClipboard(
    state: HyphenTextState,
    clipboardLabel: String,
): Clipboard {
    var lastKnownSelection by remember { mutableStateOf(state.selection) }
    if (state.selection.start != state.selection.end) {
        lastKnownSelection = state.selection
    }

    val originalClipboard = LocalClipboard.current

    return remember(originalClipboard, state, clipboardLabel) {
        object : Clipboard {
            override suspend fun getClipEntry(): ClipEntry? = originalClipboard.getClipEntry()

            override suspend fun setClipEntry(clipEntry: ClipEntry?) {
                if (clipEntry == null) {
                    originalClipboard.setClipEntry(null)
                    return
                }

                val item = clipEntry.clipData.getItemAt(0)
                val plainText = item.text?.toString() ?: ""
                if (plainText.isEmpty()) return

                var start = state.selection.start.coerceAtMost(state.selection.end)
                var end = state.selection.start.coerceAtLeast(state.selection.end)

                if (start == end) {
                    start = lastKnownSelection.start.coerceAtMost(lastKnownSelection.end)
                    end = lastKnownSelection.start.coerceAtLeast(lastKnownSelection.end)
                }

                val isValidBounds = start < end && end <= state.text.length && state.text.substring(
                    start,
                    end
                ) == plainText
                if (!isValidBounds) {
                    start = state.text.indexOf(plainText)
                    end = if (start != -1) start + plainText.length else -1
                }

                if (start != -1 && start < end) {
                    val markdown = state.toMarkdown(start, end)
                    val clipData = ClipData.newPlainText(clipboardLabel, markdown)
                    originalClipboard.setClipEntry(ClipEntry(clipData))
                } else {
                    originalClipboard.setClipEntry(clipEntry)
                }
            }

            override val nativeClipboard: NativeClipboard
                get() = originalClipboard.nativeClipboard
        }
    }
}