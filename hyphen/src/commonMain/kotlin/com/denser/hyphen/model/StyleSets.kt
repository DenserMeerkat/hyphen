package com.denser.hyphen.model

object StyleSets {
    val allInline = listOf(
        MarkupStyle.Bold,
        MarkupStyle.Italic,
        MarkupStyle.Underline,
        MarkupStyle.Strikethrough,
        MarkupStyle.Highlight,
        MarkupStyle.InlineCode,
        MarkupStyle.H1,
        MarkupStyle.H2,
        MarkupStyle.H3,
        MarkupStyle.H4,
        MarkupStyle.H5,
        MarkupStyle.H6
    )
    val allBlock = listOf(
        MarkupStyle.Blockquote,
        MarkupStyle.BulletList,
        MarkupStyle.OrderedList,
        MarkupStyle.CheckboxUnchecked,
        MarkupStyle.CheckboxChecked
    )
    val allHeadings = listOf(
        MarkupStyle.H1,
        MarkupStyle.H2,
        MarkupStyle.H3,
        MarkupStyle.H4,
        MarkupStyle.H5,
        MarkupStyle.H6
    )
}