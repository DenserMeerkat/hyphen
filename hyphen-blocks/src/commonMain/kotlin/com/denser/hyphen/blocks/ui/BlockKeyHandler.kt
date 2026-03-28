package com.denser.hyphen.blocks.ui

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.insert
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import com.denser.hyphen.blocks.model.CheckboxBlock
import com.denser.hyphen.blocks.state.BlockFocusHandler
import com.denser.hyphen.blocks.state.HyphenBlockState
import com.denser.hyphen.blocks.state.NavDirection

internal fun handleBlockKeyEvent(
    event: KeyEvent,
    blockId: String,
    fieldState: TextFieldState,
    blockState: HyphenBlockState,
    focusHandler: BlockFocusHandler,
    layout: (() -> TextLayoutResult?)? = null,
): Boolean {
    if (event.type != KeyEventType.KeyDown) return false

    val cursorStart = fieldState.selection.start
    val layoutResult = layout?.invoke()
    val isOnFirstLine = layoutResult?.let { it.getLineForOffset(cursorStart) == 0 } ?: false
    val isOnLastLine = layoutResult?.let { it.getLineForOffset(cursorStart) == it.lineCount - 1 } ?: false
    
    val isCollapsed = fieldState.selection.start == fieldState.selection.end
    val isPrimary = event.isCtrlPressed || event.isMetaPressed
    val isShift = event.isShiftPressed

    return when (event.key) {

        Key.Backspace -> {
            if (isCollapsed && cursorStart <= 1) {
                blockState.mergeWithPrevious(blockId)
                true
            } else false
        }

        Key.Enter -> when {
            isPrimary -> {
                val block = blockState.blocks.firstOrNull { it.id == blockId }
                if (block is CheckboxBlock) {
                    blockState.toggleCheckbox(blockId)
                    true
                } else false
            }
            isShift -> {
                fieldState.edit {
                    insert(cursorStart, "\n")
                    selection = TextRange(cursorStart + 1)
                }
                true
            }
            else -> {
                blockState.splitBlock(blockId, cursorStart)
                true
            }
        }

        Key.DirectionLeft -> {
            if (!isPrimary && !isShift && isCollapsed && cursorStart <= 1) {
                blockState.focusPreviousBlock(blockId, direction = NavDirection.Left)
                true
            } else false
        }

        Key.DirectionRight -> {
            if (!isPrimary && !isShift && isCollapsed && cursorStart >= fieldState.text.length) {
                blockState.focusNextBlock(blockId, direction = NavDirection.Right)
                true
            } else false
        }

        Key.DirectionUp -> {
            if (!isPrimary && !isShift && isCollapsed) {
                if (cursorStart <= 1 || isOnFirstLine) {
                    val xOffset = layoutResult?.getCursorRect(cursorStart)?.left
                    val navigated = blockState.focusPreviousBlock(blockId, xOffset, NavDirection.Up)
                    if (!navigated) fieldState.edit { selection = TextRange(1) }
                    true
                } else {
                    focusHandler.cursorBeforeNav = cursorStart
                    focusHandler.pendingNavCheck = BlockFocusHandler.PendingNavDirection.Up
                    false
                }
            } else false
        }

        Key.DirectionDown -> {
            if (!isPrimary && !isShift && isCollapsed) {
                if (cursorStart >= fieldState.text.length || isOnLastLine) {
                    val xOffset = layoutResult?.getCursorRect(cursorStart)?.left
                    val navigated = blockState.focusNextBlock(blockId, xOffset, NavDirection.Down)
                    if (!navigated) fieldState.edit { selection = TextRange(fieldState.text.length) }
                    true
                } else {
                    focusHandler.cursorBeforeNav = cursorStart
                    focusHandler.pendingNavCheck = BlockFocusHandler.PendingNavDirection.Down
                    false
                }
            } else false
        }

        else -> false
    }
}