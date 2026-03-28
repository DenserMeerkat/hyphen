package com.denser.hyphen.blocks.model

import androidx.compose.foundation.text.input.TextFieldState
import com.denser.hyphen.core.model.StyleRange
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface HyphenBlock {
    val id: String
    val isFocusable: Boolean get() = true
}

data class TextBlock @OptIn(ExperimentalUuidApi::class) constructor(
    override val id: String = Uuid.random().toString(),
    val state: TextFieldState = TextFieldState(),
    val type: TextType = TextType.Paragraph,
    val spans: List<StyleRange> = emptyList(),
) : HyphenBlock {
    enum class TextType { Paragraph, H1, H2, H3, H4, H5, H6 }
}

data class CheckboxBlock @OptIn(ExperimentalUuidApi::class) constructor(
    override val id: String = Uuid.random().toString(),
    val state: TextFieldState = TextFieldState(),
    val isChecked: Boolean = false,
    val spans: List<StyleRange> = emptyList(),
) : HyphenBlock

data class BulletListBlock @OptIn(ExperimentalUuidApi::class) constructor(
    override val id: String = Uuid.random().toString(),
    val state: TextFieldState = TextFieldState(),
    val spans: List<StyleRange> = emptyList(),
) : HyphenBlock


data class OrderedListBlock @OptIn(ExperimentalUuidApi::class) constructor(
    override val id: String = Uuid.random().toString(),
    val state: TextFieldState = TextFieldState(),
    val startNumber: Int = 1,
    val spans: List<StyleRange> = emptyList(),
) : HyphenBlock

data class BlockquoteBlock @OptIn(ExperimentalUuidApi::class) constructor(
    override val id: String = Uuid.random().toString(),
    val state: TextFieldState = TextFieldState(),
    val spans: List<StyleRange> = emptyList(),
) : HyphenBlock

data class DividerBlock @OptIn(ExperimentalUuidApi::class) constructor(
    override val id: String = Uuid.random().toString(),
) : HyphenBlock {
    override val isFocusable: Boolean get() = false
}


val HyphenBlock.textState: TextFieldState?
    get() = when (this) {
        is TextBlock -> state
        is CheckboxBlock -> state
        is BulletListBlock -> state
        is OrderedListBlock -> state
        is BlockquoteBlock -> state
        is DividerBlock -> null
    }

val HyphenBlock.spans: List<StyleRange>
    get() = when (this) {
        is TextBlock -> spans
        is CheckboxBlock -> spans
        is BulletListBlock -> spans
        is OrderedListBlock -> spans
        is BlockquoteBlock -> spans
        is DividerBlock -> emptyList()
    }

val HyphenBlock.continuesOnEnter: Boolean
    get() = when (this) {
        is CheckboxBlock, is BulletListBlock, is OrderedListBlock, is BlockquoteBlock -> true
        is TextBlock, is DividerBlock -> false
    }

fun HyphenBlock.withSpans(newSpans: List<StyleRange>): HyphenBlock = when (this) {
    is TextBlock -> copy(spans = newSpans)
    is CheckboxBlock -> copy(spans = newSpans)
    is BulletListBlock -> copy(spans = newSpans)
    is OrderedListBlock -> copy(spans = newSpans)
    is BlockquoteBlock -> copy(spans = newSpans)
    is DividerBlock -> this
}