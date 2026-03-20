package com.denser.hyphen.core.model

/**
 * All formatting styles understood by the Hyphen editor family.
 *
 * Styles are divided into two groups via the nested [Inline] marker interface:
 *
 * **[Inline] styles** — character-level formatting applicable inside any text field.
 * Used by both `hyphen` (flat text editor) and `hyphen-blocks` (block editor).
 * `HyphenInlineEditor` and `HyphenInlineState` only ever produce or consume
 * [Inline] spans — they have no knowledge of block-level styles.
 *
 * **Block-level styles** — structural prefixes meaningful only in `hyphen`'s flat-text
 * model (e.g. `BulletList`, `H1`). These have no representation in `hyphen-blocks`,
 * where structure is expressed through [com.denser.hyphen.blocks.model.HyphenBlock]
 * subclasses instead.
 *
 * ## Usage in hyphen-blocks
 *
 * When working with `HyphenInlineState` and `MarkupStyleRange` inside a block,
 * restrict spans to [Inline]:
 *
 * ```kotlin
 * // Checking inline style at cursor
 * val isBold = spans.any { it.style is MarkupStyle.Inline && it.style == MarkupStyle.Bold }
 *
 * // Toggling from a toolbar
 * inlineState.toggleStyle(MarkupStyle.Bold)   // Bold : Inline ✓
 * inlineState.toggleStyle(MarkupStyle.H1)     // H1 is not Inline — won't compile if
 *                                              // toggleStyle accepts MarkupStyle.Inline
 * ```
 *
 * ## Usage in hyphen (flat text editor)
 *
 * `HyphenTextState.toggleStyle` accepts the full [MarkupStyle] so both inline and
 * block-level styles can be applied through the same API.
 */
sealed interface MarkupStyle {

    /**
     * Marker interface for character-level inline styles.
     *
     * All [Inline] variants are also [MarkupStyle], so they are valid anywhere
     * a [MarkupStyle] is expected. `hyphen-blocks` constrains itself to [Inline]
     * to ensure block-level styles never appear inside a block's text field.
     */
    sealed interface Inline : MarkupStyle

    /** `**text**` — bold weight. */
    data object Bold : Inline

    /** `*text*` or `_text_` — italic. */
    data object Italic : Inline

    /** `__text__` — underline decoration. */
    data object Underline : Inline

    /** `~~text~~` — strikethrough decoration. */
    data object Strikethrough : Inline

    /** `` `text` `` — monospace inline code. */
    data object InlineCode : Inline

    /** `==text==` — highlight background. */
    data object Highlight : Inline

    /** `- ` at line start — unordered list item. */
    data object BulletList : MarkupStyle

    /** `1. ` at line start — ordered list item. */
    data object OrderedList : MarkupStyle

    /** `> ` at line start — blockquote. */
    data object Blockquote : MarkupStyle

    /** `- [ ] ` at line start — unchecked checkbox. */
    data object CheckboxUnchecked : MarkupStyle

    /** `- [x] ` at line start — checked checkbox. */
    data object CheckboxChecked : MarkupStyle

    /** `# ` at line start — heading level 1. */
    data object H1 : MarkupStyle

    /** `## ` at line start — heading level 2. */
    data object H2 : MarkupStyle

    /** `### ` at line start — heading level 3. */
    data object H3 : MarkupStyle

    /** `#### ` at line start — heading level 4. */
    data object H4 : MarkupStyle

    /** `##### ` at line start — heading level 5. */
    data object H5 : MarkupStyle

    /** `###### ` at line start — heading level 6. */
    data object H6 : MarkupStyle
}