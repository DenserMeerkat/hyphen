package com.denser.hyphen.blocks.state

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.text.TextRange
import com.denser.hyphen.blocks.model.BlockquoteBlock
import com.denser.hyphen.blocks.model.BulletListBlock
import com.denser.hyphen.blocks.model.CheckboxBlock
import com.denser.hyphen.blocks.model.DividerBlock
import com.denser.hyphen.blocks.model.HyphenBlock
import com.denser.hyphen.blocks.model.OrderedListBlock
import com.denser.hyphen.blocks.model.TextBlock
import com.denser.hyphen.core.model.StyleRange

internal sealed interface BlockSnapshot {
    val id: String
    val spans: List<StyleRange>
}

internal data class TextBlockSnapshot(
    override val id: String,
    val text: String,
    val type: TextBlock.TextType,
    override val spans: List<StyleRange> = emptyList(),
) : BlockSnapshot

internal data class CheckboxBlockSnapshot(
    override val id: String,
    val text: String,
    val isChecked: Boolean,
    override val spans: List<StyleRange> = emptyList(),
) : BlockSnapshot

internal data class BulletListBlockSnapshot(
    override val id: String,
    val text: String,
    override val spans: List<StyleRange> = emptyList(),
) : BlockSnapshot

internal data class OrderedListBlockSnapshot(
    override val id: String,
    val text: String,
    override val spans: List<StyleRange> = emptyList(),
) : BlockSnapshot

internal data class BlockquoteBlockSnapshot(
    override val id: String,
    val text: String,
    override val spans: List<StyleRange> = emptyList(),
) : BlockSnapshot

internal data class DividerBlockSnapshot(
    override val id: String,
) : BlockSnapshot {
    override val spans: List<StyleRange> = emptyList()
}

internal data class BlockDocumentSnapshot(
    val blocks: List<BlockSnapshot>,
    val focusedBlockId: String? = null,
    val selection: TextRange? = null,
)

internal fun HyphenBlock.toSnapshot(zwsp: String): BlockSnapshot = when (this) {
    is TextBlock -> TextBlockSnapshot(id, state.text.toString().removePrefix(zwsp), type, spans)
    is CheckboxBlock -> CheckboxBlockSnapshot(id, state.text.toString().removePrefix(zwsp), isChecked, spans)
    is BulletListBlock -> BulletListBlockSnapshot(id, state.text.toString().removePrefix(zwsp), spans)
    is OrderedListBlock -> OrderedListBlockSnapshot(id, state.text.toString().removePrefix(zwsp), spans)
    is BlockquoteBlock -> BlockquoteBlockSnapshot(id, state.text.toString().removePrefix(zwsp), spans)
    is DividerBlock -> DividerBlockSnapshot(id)
}

internal fun BlockSnapshot.toBlock(zwsp: String): HyphenBlock = when (this) {
    is TextBlockSnapshot -> TextBlock(id = id, state = TextFieldState(zwsp + text), type = type, spans = spans)
    is CheckboxBlockSnapshot -> CheckboxBlock(id = id, state = TextFieldState(zwsp + text), isChecked = isChecked, spans = spans)
    is BulletListBlockSnapshot -> BulletListBlock(id = id, state = TextFieldState(zwsp + text), spans = spans)
    is OrderedListBlockSnapshot -> OrderedListBlock(id = id, state = TextFieldState(zwsp + text), spans = spans)
    is BlockquoteBlockSnapshot -> BlockquoteBlock(id = id, state = TextFieldState(zwsp + text), spans = spans)
    is DividerBlockSnapshot -> DividerBlock(id = id)
}

internal fun BlockSnapshot.textContent(): String = when (this) {
    is TextBlockSnapshot -> text
    is CheckboxBlockSnapshot -> text
    is BulletListBlockSnapshot -> text
    is OrderedListBlockSnapshot -> text
    is BlockquoteBlockSnapshot -> text
    is DividerBlockSnapshot -> ""
}

internal fun HyphenBlock.snapshotClass(): kotlin.reflect.KClass<out BlockSnapshot> = when (this) {
    is TextBlock -> TextBlockSnapshot::class
    is CheckboxBlock -> CheckboxBlockSnapshot::class
    is BulletListBlock -> BulletListBlockSnapshot::class
    is OrderedListBlock -> OrderedListBlockSnapshot::class
    is BlockquoteBlock -> BlockquoteBlockSnapshot::class
    is DividerBlock -> DividerBlockSnapshot::class
}