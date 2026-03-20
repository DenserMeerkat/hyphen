package com.denser.hyphen.core.model

/**
 * Convenience groupings of [MarkupStyle] variants used across the Hyphen editor family.
 */
object StyleSets {

    /**
     * All [MarkupStyle.Inline] styles — the complete set of character-level formats.
     * Used by `HyphenInlineState`, `SpanManager.applyTypingOverrides`, and anywhere
     * only inline styles should be considered.
     */
    val allInline: List<MarkupStyle.Inline> = listOf(
        MarkupStyle.Bold,
        MarkupStyle.Italic,
        MarkupStyle.Underline,
        MarkupStyle.Strikethrough,
        MarkupStyle.InlineCode,
        MarkupStyle.Highlight,
    )

    /**
     * All heading styles. A subset of [MarkupStyle] used only in the flat-text
     * `hyphen` editor — not applicable inside `hyphen-blocks` block fields.
     */
    val allHeadings: List<MarkupStyle> = listOf(
        MarkupStyle.H1,
        MarkupStyle.H2,
        MarkupStyle.H3,
        MarkupStyle.H4,
        MarkupStyle.H5,
        MarkupStyle.H6,
    )

    /**
     * All block-level styles used in the flat-text `hyphen` editor.
     */
    val allBlock: List<MarkupStyle> = listOf(
        MarkupStyle.BulletList,
        MarkupStyle.OrderedList,
        MarkupStyle.Blockquote,
        MarkupStyle.CheckboxUnchecked,
        MarkupStyle.CheckboxChecked,
    )
}