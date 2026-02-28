package com.denser.hyphen.model

sealed interface MarkupStyle {
    data object Bold : MarkupStyle
    data object Italic : MarkupStyle
    data object Underline : MarkupStyle
    data object Strikethrough : MarkupStyle
    data object InlineCode : MarkupStyle
    data object Highlight : MarkupStyle
    data object BulletList : MarkupStyle
    data object OrderedList : MarkupStyle
    data object Blockquote : MarkupStyle
}