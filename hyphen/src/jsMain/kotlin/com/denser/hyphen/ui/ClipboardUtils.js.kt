package com.denser.hyphen.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.NativeClipboard
import com.denser.hyphen.state.HyphenTextState
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.events.Event
import org.w3c.dom.clipboard.ClipboardEvent

@OptIn(ExperimentalComposeUiApi::class, ExperimentalWasmJsInterop::class)
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

    DisposableEffect(state, lastKnownSelection) {
        val copyListener: (Event) -> Unit = listener@{ event ->
            val clipboardEvent = event as? ClipboardEvent ?: return@listener

            val currentSelection = state.selection
            val currentText = state.text

            var start = currentSelection.start.coerceAtMost(currentSelection.end)
            var end = currentSelection.start.coerceAtLeast(currentSelection.end)

            if (start == end) {
                start = lastKnownSelection.start.coerceAtMost(lastKnownSelection.end)
                end = lastKnownSelection.start.coerceAtLeast(lastKnownSelection.end)
            }

            if (start != -1 && start < end && end <= currentText.length) {
                val markdown = state.toMarkdown(start, end)
                clipboardEvent.preventDefault()
                clipboardEvent.clipboardData?.setData("text/plain", markdown)
            }
        }

        document.addEventListener("copy", copyListener)

        onDispose {
            document.removeEventListener("copy", copyListener)
        }
    }

    return remember(originalClipboard, state, clipboardLabel) {
        object : Clipboard {
            override suspend fun getClipEntry(): ClipEntry? = originalClipboard.getClipEntry()

            override suspend fun setClipEntry(clipEntry: ClipEntry?) {
                if (clipEntry == null) {
                    originalClipboard.setClipEntry(null)
                    return
                }

                val currentSelection = state.selection
                val currentText = state.text

                var start = currentSelection.start.coerceAtMost(currentSelection.end)
                var end = currentSelection.start.coerceAtLeast(currentSelection.end)

                if (start == end) {
                    start = lastKnownSelection.start.coerceAtMost(lastKnownSelection.end)
                    end = lastKnownSelection.start.coerceAtLeast(lastKnownSelection.end)
                }

                if (start != -1 && start < end && end <= currentText.length) {
                    val markdown = state.toMarkdown(start, end)
                    try {
                        window.navigator.clipboard.writeText(markdown)
                    } catch (e: Exception) {
                        originalClipboard.setClipEntry(clipEntry)
                    }
                } else {
                    originalClipboard.setClipEntry(clipEntry)
                }
            }

            override val nativeClipboard: NativeClipboard
                get() = originalClipboard.nativeClipboard
        }
    }
}