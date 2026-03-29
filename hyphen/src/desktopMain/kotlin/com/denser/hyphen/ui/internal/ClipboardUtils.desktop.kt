package com.denser.hyphen.ui.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.NativeClipboard
import com.denser.hyphen.state.HyphenTextState
import com.denser.hyphen.markdown.MarkdownSerializer
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@Composable
internal actual fun rememberMarkdownClipboard(
    state: HyphenTextState,
    clipboardLabel: String,
): Clipboard {
    var lastKnownSelection by remember { mutableStateOf(state.selection) }
    var lastKnownText by remember { mutableStateOf(state.text) }
    var lastKnownSpans by remember { mutableStateOf(state.spans.toList()) }

    if (state.selection.start != state.selection.end) {
        lastKnownSelection = state.selection
        lastKnownText = state.text
        lastKnownSpans = state.spans.toList()
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

                val start = lastKnownSelection.start.coerceAtMost(lastKnownSelection.end)
                val end = lastKnownSelection.start.coerceAtLeast(lastKnownSelection.end)

                if (start < end && end <= lastKnownText.length) {
                    val markdown = MarkdownSerializer.serialize(lastKnownText, lastKnownSpans, start, end)
                    val stringSelection = StringSelection(markdown)
                    Toolkit.getDefaultToolkit().systemClipboard.setContents(stringSelection, null)
                } else {
                    originalClipboard.setClipEntry(clipEntry)
                }
            }

            override val nativeClipboard: NativeClipboard
                get() = originalClipboard.nativeClipboard
        }
    }
}