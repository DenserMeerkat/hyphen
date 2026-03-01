package com.denser.hyphen.ui

import android.content.ClipData
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