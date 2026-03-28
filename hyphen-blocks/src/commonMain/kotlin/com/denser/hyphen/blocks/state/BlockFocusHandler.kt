package com.denser.hyphen.blocks.state

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.text.TextRange
import kotlinx.coroutines.flow.drop
import androidx.compose.ui.text.TextLayoutResult
import kotlinx.coroutines.flow.firstOrNull

class BlockFocusHandler internal constructor(
    private val blockId: String,
    private val fieldState: TextFieldState,
    private val blockState: HyphenBlockState,
) {
    fun onFocusChanged(focusState: FocusState) {
        if (focusState.isFocused) {
            blockState.focusedBlockId = blockId
        } else if (blockState.focusedBlockId == blockId) {
            blockState.focusedBlockId = null
        }
    }

    enum class PendingNavDirection { Up, Down, None }

    var pendingNavCheck: PendingNavDirection by mutableStateOf(PendingNavDirection.None)
    var cursorBeforeNav: Int by mutableStateOf(0)
}

@Composable
fun rememberBlockFocusHandler(
    blockId: String,
    fieldState: TextFieldState,
    blockState: HyphenBlockState,
    layoutProvider: (() -> TextLayoutResult?)? = null,
): BlockFocusHandler {
    val handler = remember(blockId, blockState) {
        BlockFocusHandler(blockId, fieldState, blockState)
    }

    LaunchedEffect(blockState.pendingFocus) {
        val req = blockState.pendingFocus ?: return@LaunchedEffect
        if (req.id == blockId) {
            runCatching {
                blockState.getFocusRequester(blockId).requestFocus()
            }
            if (req.cursorPosition != null) {
                val safePos = req.cursorPosition.coerceIn(1, fieldState.text.length)
                fieldState.edit { selection = TextRange(safePos) }
            } else if (req.direction != null && layoutProvider != null) {
                val result = snapshotFlow { layoutProvider.invoke() }
                    .firstOrNull { it != null }

                if (result != null) {
                    var targetCursor = fieldState.selection.start
                    if (req.direction == NavDirection.Up) {
                        val lineIndex = (result.lineCount - 1).coerceAtLeast(0)
                        val y = (result.getLineTop(lineIndex) + result.getLineBottom(lineIndex)) / 2f
                        targetCursor = result.getOffsetForPosition(androidx.compose.ui.geometry.Offset(req.xOffset ?: 0f, y))
                    } else if (req.direction == NavDirection.Down) {
                        val lineIndex = 0
                        val y = (result.getLineTop(lineIndex) + result.getLineBottom(lineIndex)) / 2f
                        targetCursor = result.getOffsetForPosition(androidx.compose.ui.geometry.Offset(req.xOffset ?: 0f, y))
                    }
                    val safePos = maxOf(1, targetCursor.coerceIn(1, fieldState.text.length))
                    fieldState.edit { selection = TextRange(safePos) }
                }
            }
            blockState.clearPendingFocus()
        }
    }

    LaunchedEffect(handler) {
        snapshotFlow { handler.pendingNavCheck to fieldState.selection.start }
            .drop(1)
            .collect { (direction, currentCursor) ->
                if (direction == BlockFocusHandler.PendingNavDirection.None) return@collect
                val cursorMoved = currentCursor != handler.cursorBeforeNav
                handler.pendingNavCheck = BlockFocusHandler.PendingNavDirection.None
                if (!cursorMoved) {
                    var xOffset: Float? = null
                    val result = layoutProvider?.invoke()
                    if (result != null) {
                        xOffset = result.getCursorRect(currentCursor).left
                    }
                    when (direction) {
                        BlockFocusHandler.PendingNavDirection.Up -> {
                            val navigated = blockState.focusPreviousBlock(blockId, xOffset, NavDirection.Up)
                            if (!navigated) fieldState.edit { selection = TextRange(1) }
                        }
                        BlockFocusHandler.PendingNavDirection.Down -> {
                            val navigated = blockState.focusNextBlock(blockId, xOffset, NavDirection.Down)
                            if (!navigated) fieldState.edit { selection = TextRange(fieldState.text.length) }
                        }
                        BlockFocusHandler.PendingNavDirection.None -> Unit
                    }
                }
            }
    }

    return handler
}