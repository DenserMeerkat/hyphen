package com.denser.hyphen.ui

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
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@Composable
internal actual fun rememberMarkdownClipboard(
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

                var start = state.selection.start.coerceAtMost(state.selection.end)
                var end = state.selection.start.coerceAtLeast(state.selection.end)

                if (start == end) {
                    start = lastKnownSelection.start.coerceAtMost(lastKnownSelection.end)
                    end = lastKnownSelection.start.coerceAtLeast(lastKnownSelection.end)
                }

                if (start != -1 && start < end) {
                    val markdown = state.toMarkdown(start, end)
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