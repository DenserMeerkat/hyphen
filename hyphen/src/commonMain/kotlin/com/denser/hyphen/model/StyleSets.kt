package com.denser.hyphen.model

object StyleSets {
    val allInline = listOf(
        MarkupStyle.Bold,
        MarkupStyle.Italic,
        MarkupStyle.Underline,
        MarkupStyle.Strikethrough,
        MarkupStyle.Highlight,
        MarkupStyle.InlineCode,
    )
    val allBlock = listOf(
        MarkupStyle.Blockquote,
        MarkupStyle.BulletList,
        MarkupStyle.OrderedList
    )
}