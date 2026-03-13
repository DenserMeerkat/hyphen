package com.denser.hyphen.model

/**
 * Discriminated union of all formatting styles supported by the Hyphen editor.
 *
 * Styles are divided into two categories:
 *
 * **Inline & Heading styles** apply to text and are represented as [MarkupStyleRange] spans.
 * They can be toggled via [com.denser.hyphen.state.HyphenTextState.toggleStyle]. Headings
 * automatically expand their spans to cover the entire line they sit on, keeping their Markdown
 * syntax hidden from the raw text state.
 *
 * | Style | Markdown syntax | Visual effect |
 * |---|---|---|
 * | [Bold] | `**text**` | Heavy font weight |
 * | [Italic] | `*text*` or `_text_` | Oblique font style |
 * | [Underline] | `__text__` | Underline decoration |
 * | [Strikethrough] | `~~text~~` | Line-through decoration |
 * | [InlineCode] | `` `text` `` | Monospace, highlighted background |
 * | [Highlight] | `==text==` | Coloured background highlight |
 * | [H1]..[H6] | `# text` to `###### text` | Scaled, bold headings (line-spanning) |
 *
 * **Block styles** apply to entire lines and are represented as line-prefix characters
 * inserted directly into [com.denser.hyphen.state.HyphenTextState.text] by [com.denser.hyphen.state.BlockStyleManager]. Smart Enter
 * continues or exits the block automatically.
 *
 * | Style | Markdown prefix | Behaviour |
 * |---|---|---|
 * | [BulletList] | `- `, `* `, or `• ` | Unordered list item |
 * | [OrderedList] | `1. `, `2. `, etc. | Auto-numbered ordered list |
 * | [Blockquote] | `> ` or `┃ ` | Indented quotation block |
 */
sealed interface MarkupStyle {

    /** Bold text. Serialized as `**text**`. */
    data object Bold : MarkupStyle

    /** Italic text. Serialized as `*text*` or `_text_`. */
    data object Italic : MarkupStyle

    /** Underlined text. Serialized as `__text__`. */
    data object Underline : MarkupStyle

    /** Struck-through text. Serialized as `~~text~~`. */
    data object Strikethrough : MarkupStyle

    /** Inline code fragment. Serialized as `` `text` ``. */
    data object InlineCode : MarkupStyle

    /** Highlighted text. Serialized as `==text==`. */
    data object Highlight : MarkupStyle

    /**
     * Unordered (bullet) list item. Represented by a `- `, `* `, or `• ` prefix on the line.
     *
     * Pressing Enter inside a bullet item inserts a new `- ` prefix on the next line.
     * Pressing Enter on an empty bullet item removes the prefix and exits the list.
     */
    data object BulletList : MarkupStyle

    /**
     * Ordered (numbered) list item. Represented by an auto-incrementing `N. ` prefix.
     *
     * Numbers are recalculated across the entire document whenever an ordered list is
     * modified. Pressing Enter inside an item inserts the next number; pressing Enter on
     * an empty item exits the list.
     */
    data object OrderedList : MarkupStyle

    /**
     * Block quotation. Represented by a `> ` or `┃ ` prefix on the line.
     *
     * Pressing Enter inside a blockquote continues it with another `> ` or `┃ ` prefix.
     * Pressing Enter on an empty prefix line exits the blockquote.
     */
    data object Blockquote : MarkupStyle

    /** Heading level 1. Spans the entire line. Serialized as `# text`. */
    data object H1 : MarkupStyle

    /** Heading level 2. Spans the entire line. Serialized as `## text`. */
    data object H2 : MarkupStyle

    /** Heading level 3. Spans the entire line. Serialized as `### text`. */
    data object H3 : MarkupStyle

    /** Heading level 4. Spans the entire line. Serialized as `#### text`. */
    data object H4 : MarkupStyle

    /** Heading level 5. Spans the entire line. Serialized as `##### text`. */
    data object H5 : MarkupStyle

    /** Heading level 6. Spans the entire line. Serialized as `###### text`. */
    data object H6 : MarkupStyle
}