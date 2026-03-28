package com.denser.hyphen.blocks.state

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.denser.hyphen.core.model.StyleRange
import com.denser.hyphen.inline.state.HyphenInlineState

@Composable
fun rememberBlockInlineState(
    blockId: String,
    fieldState: TextFieldState,
    blockState: HyphenBlockState,
    initialSpans: List<StyleRange>,
): HyphenInlineState {
    val state = remember(fieldState) {
        HyphenInlineState(
            fieldState = fieldState,
            initialSpans = initialSpans,
            onSnapshotRequested = blockState::saveSnapshot,
            onSpansChanged = { spans -> blockState.updateBlockSpans(blockId, spans) },
        )
    }

    if (state.spans != initialSpans) {
        state.syncSpans(initialSpans)
    }

    DisposableEffect(blockId) {
        blockState.registerInlineState(blockId, state)
        onDispose { blockState.unregisterInlineState(blockId) }
    }

    return state
}