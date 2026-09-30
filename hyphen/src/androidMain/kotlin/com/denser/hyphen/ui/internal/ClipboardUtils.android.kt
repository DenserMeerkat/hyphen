package com.denser.hyphen.ui.internal

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import com.denser.hyphen.markdown.MarkdownSerializer
import com.denser.hyphen.model.MarkupStyleRange
import com.denser.hyphen.state.HyphenTextState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal actual fun rememberMarkdownClipboard(
    state: HyphenTextState,
    clipboardLabel: String,
): Clipboard {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var lastKnownSelection by remember { mutableStateOf(state.selection) }
    var lastKnownText by remember { mutableStateOf(state.text) }
    var lastKnownSpans by remember { mutableStateOf<List<MarkupStyleRange>>(emptyList()) }

    val currentSel = state.selection
    if (currentSel.start != currentSel.end) {
        lastKnownSelection = currentSel
        lastKnownText = state.text
        lastKnownSpans = state.spans.toList()
    }

    DisposableEffect(Unit) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        var isMarkdownWrite = false

        val listener = ClipboardManager.OnPrimaryClipChangedListener {
            if (isMarkdownWrite || !state.isFocused) return@OnPrimaryClipChangedListener

            val selStart = minOf(lastKnownSelection.start, lastKnownSelection.end)
            val selEnd = maxOf(lastKnownSelection.start, lastKnownSelection.end)
            if (selStart < selEnd && selEnd <= lastKnownText.length) {
                scope.launch {
                    delay(50)
                    val markdown = MarkdownSerializer.serialize(
                        lastKnownText,
                        lastKnownSpans,
                        selStart,
                        selEnd,
                    )
                    isMarkdownWrite = true
                    try {
                        clipboardManager.setPrimaryClip(
                            ClipData.newPlainText(clipboardLabel, markdown)
                        )
                    } finally {
                        isMarkdownWrite = false
                    }
                }
            }
        }

        clipboardManager.addPrimaryClipChangedListener(listener)
        onDispose { clipboardManager.removePrimaryClipChangedListener(listener) }
    }

    return LocalClipboard.current
}
