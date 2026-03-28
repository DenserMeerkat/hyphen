package com.denser.hyphen.blocks.state

import androidx.compose.foundation.text.input.TextFieldState
import com.denser.hyphen.blocks.model.BlockquoteBlock
import com.denser.hyphen.blocks.model.BulletListBlock
import com.denser.hyphen.blocks.model.CheckboxBlock
import com.denser.hyphen.blocks.model.HyphenBlock
import com.denser.hyphen.blocks.model.OrderedListBlock
import com.denser.hyphen.blocks.model.TextBlock
import com.denser.hyphen.blocks.model.spans
import com.denser.hyphen.blocks.model.textState
import com.denser.hyphen.core.model.StyleRange
import com.denser.hyphen.core.constants.EditorConstants

class BlockConversionManager(
    private val state: HyphenBlockState
) {

    fun toggleHeading(blockId: String, type: TextBlock.TextType) {
        val block = state._blocks.find { it.id == blockId } ?: return
        if (block is TextBlock && block.type == type) convertToParagraph(blockId)
        else convertBlock(blockId) { id, txtState, spans ->
            TextBlock(id = id, state = txtState, type = type, spans = spans)
        }
    }

    fun toggleBulletList(blockId: String) {
        val block = state._blocks.find { it.id == blockId } ?: return
        if (block is BulletListBlock) convertToParagraph(blockId)
        else convertBlock(blockId) { id, txtState, spans ->
            BulletListBlock(id = id, state = txtState, spans = spans)
        }
    }

    fun toggleOrderedList(blockId: String) {
        val block = state._blocks.find { it.id == blockId } ?: return
        if (block is OrderedListBlock) convertToParagraph(blockId)
        else convertBlock(blockId) { id, txtState, spans ->
            OrderedListBlock(id = id, state = txtState, spans = spans)
        }
    }

    fun toggleBlockquote(blockId: String) {
        val block = state._blocks.find { it.id == blockId } ?: return
        if (block is BlockquoteBlock) convertToParagraph(blockId)
        else convertBlock(blockId) { id, txtState, spans ->
            BlockquoteBlock(id = id, state = txtState, spans = spans)
        }
    }

    fun toggleCheckbox(blockId: String) {
        val block = state._blocks.find { it.id == blockId } ?: return
        if (block is CheckboxBlock) convertToParagraph(blockId)
        else convertBlock(blockId) { id, txtState, spans ->
            CheckboxBlock(id = id, state = txtState, spans = spans)
        }
    }

    fun convertToParagraph(blockId: String) {
        convertBlock(blockId) { id, txtState, spans ->
            TextBlock(id = id, state = txtState, type = TextBlock.TextType.Paragraph, spans = spans)
        }
    }

    fun triggerBulletList(blockId: String) =
        convertBlock(blockId, clearText = true) { id, txtState, spans ->
            BulletListBlock(id = id, state = txtState, spans = spans)
        }

    fun triggerOrderedList(blockId: String, startNumber: Int = 1) =
        convertBlock(blockId, clearText = true) { id, txtState, spans ->
            OrderedListBlock(id = id, state = txtState, startNumber = startNumber, spans = spans)
        }

    fun triggerBlockquote(blockId: String) =
        convertBlock(blockId, clearText = true) { id, txtState, spans ->
            BlockquoteBlock(id = id, state = txtState, spans = spans)
        }

    fun triggerCheckbox(blockId: String) =
        convertBlock(blockId, clearText = true) { id, txtState, spans ->
            CheckboxBlock(id = id, state = txtState, spans = spans)
        }

    fun triggerHeading(blockId: String, type: TextBlock.TextType) =
        convertBlock(blockId, clearText = true) { id, txtState, spans ->
            TextBlock(id = id, state = txtState, type = type, spans = spans)
        }


    private inline fun convertBlock(
        blockId: String,
        clearText: Boolean = false,
        factory: (String, TextFieldState, List<StyleRange>) -> HyphenBlock,
    ) {
        val index = state._blocks.indexOfFirst { it.id == blockId }
        if (index == -1) return

        state.saveSnapshot()

        val existing = state._blocks[index]
        val stateToUse = if (clearText) {
            TextFieldState(EditorConstants.ZWSP)
        } else {
            existing.textState ?: TextFieldState(EditorConstants.ZWSP)
        }

        val newBlock = factory(existing.id, stateToUse, existing.spans)
        state._blocks[index] = newBlock
        state.requestFocusAfterLayout(
            id = existing.id,
            cursorPosition = if (clearText) 1 else null,
        )
    }
}