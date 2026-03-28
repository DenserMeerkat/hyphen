package com.denser.hyphen.blocks.ui

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.insert
import androidx.compose.ui.text.TextRange
import com.denser.hyphen.blocks.model.TextBlock
import com.denser.hyphen.blocks.state.HyphenBlockState
import com.denser.hyphen.core.constants.EditorConstants
import com.denser.hyphen.core.constants.MarkdownConstants


class BlockInputTransformation(
    private val blockId: String,
    private val blockState: HyphenBlockState,
) : InputTransformation {

    var pendingSnapshot: Boolean = false
        private set

    fun consumePendingSnapshot(): Boolean {
        val pending = pendingSnapshot
        pendingSnapshot = false
        return pending
    }

    override fun TextFieldBuffer.transformInput() {
        pendingSnapshot = false

        val originalTextStr = originalText.toString()
        val newText = asCharSequence().toString()
        val lengthDelta = newText.length - originalTextStr.length

        if (originalTextStr.startsWith(EditorConstants.ZWSP) && !newText.startsWith(EditorConstants.ZWSP)) {
            if (lengthDelta == -1 && originalSelection.collapsed && originalSelection.start == 1) {
                revertAllChanges()
                blockState.mergeWithPrevious(blockId)
                return
            }
        }

        if (!newText.startsWith(EditorConstants.ZWSP)) {
            insert(0, EditorConstants.ZWSP)
        }

        val safeStart = maxOf(1, selection.start)
        val safeEnd = maxOf(1, selection.end)
        if (safeStart != selection.start || safeEnd != selection.end) {
            selection = TextRange(safeStart, safeEnd)
        }

        val cursorBefore = originalSelection.start

        if (lengthDelta == 1 && cursorBefore > 0 && newText.getOrNull(cursorBefore) == '\n') {
            revertAllChanges()
            blockState.splitBlock(blockId, cursorBefore)
            return
        }

        if (lengthDelta == 1) {
            val insertedChar = newText.getOrNull(cursorBefore)
            if (insertedChar == ' ') {
                val contentAfterZwsp = newText.removePrefix(EditorConstants.ZWSP)
                val trigger = detectBlockTrigger(contentAfterZwsp)

                if (trigger != null) {
                    val isAlreadyHeading = blockState.blocks
                        .find { it.id == blockId }
                        .let { it is TextBlock && it.type != TextBlock.TextType.Paragraph }

                    if (trigger is BlockTrigger.Heading && isAlreadyHeading) {
                        // fall through
                    } else {
                        revertAllChanges()
                        trigger.apply(blockId, blockState)
                        return
                    }
                }
            }
        }

        val isBulkChange = lengthDelta > 1 || lengthDelta < -1
        if (isBulkChange) {
            pendingSnapshot = true
            return
        }

        if (lengthDelta == 1 && cursorBefore > 0) {
            val insertedChar = newText.getOrNull(cursorBefore)
            if (insertedChar != null && isWordBoundary(insertedChar)) {
                pendingSnapshot = true
            }
        } else if (lengthDelta == -1 && cursorBefore > 0) {
            val deletedChar = originalTextStr.getOrNull(cursorBefore - 1)
            if (deletedChar != null && isWordBoundary(deletedChar)) {
                pendingSnapshot = true
            }
        }
    }

    private fun isWordBoundary(char: Char): Boolean =
        char.isWhitespace() || char in ".,;:!?\"'()[]{}—–-"
}


private sealed interface BlockTrigger {
    fun apply(blockId: String, blockState: HyphenBlockState)

    data object BulletList : BlockTrigger {
        override fun apply(blockId: String, blockState: HyphenBlockState) =
            blockState.conversions.triggerBulletList(blockId)
    }

    data class OrderedList(val startNumber: Int) : BlockTrigger {
        override fun apply(blockId: String, blockState: HyphenBlockState) =
            blockState.conversions.triggerOrderedList(blockId, startNumber)
    }

    data object Blockquote : BlockTrigger {
        override fun apply(blockId: String, blockState: HyphenBlockState) =
            blockState.conversions.triggerBlockquote(blockId)
    }

    data object Checkbox : BlockTrigger {
        override fun apply(blockId: String, blockState: HyphenBlockState) =
            blockState.conversions.triggerCheckbox(blockId)
    }

    data class Heading(val type: TextBlock.TextType) : BlockTrigger {
        override fun apply(blockId: String, blockState: HyphenBlockState) =
            blockState.conversions.triggerHeading(blockId, type)
    }
}

private fun detectBlockTrigger(content: String): BlockTrigger? = with(MarkdownConstants.Triggers) {
    when (content) {
        BULLET_HYPHEN, BULLET_ASTERISK, BULLET_SYMBOL -> BlockTrigger.BulletList
        BLOCKQUOTE -> BlockTrigger.Blockquote
        CHECKBOX_UNCHECKED_HYPHEN,
        CHECKBOX_UNCHECKED_ASTERISK -> BlockTrigger.Checkbox
        CHECKBOX_CHECKED_HYPHEN_L,
        CHECKBOX_CHECKED_HYPHEN_U,
        CHECKBOX_CHECKED_ASTERISK_L,
        CHECKBOX_CHECKED_ASTERISK_U -> BlockTrigger.Checkbox
        HEADING_H1 -> BlockTrigger.Heading(TextBlock.TextType.H1)
        HEADING_H2 -> BlockTrigger.Heading(TextBlock.TextType.H2)
        HEADING_H3 -> BlockTrigger.Heading(TextBlock.TextType.H3)
        HEADING_H4 -> BlockTrigger.Heading(TextBlock.TextType.H4)
        HEADING_H5 -> BlockTrigger.Heading(TextBlock.TextType.H5)
        HEADING_H6 -> BlockTrigger.Heading(TextBlock.TextType.H6)
        else -> {
            val match = MarkdownConstants.Regex.Trigger.ORDERED_LIST.find(content)
            if (match != null) {
                val num = match.groupValues[1].toIntOrNull() ?: 1
                BlockTrigger.OrderedList(startNumber = num)
            } else null
        }
    }
}