package com.denser.hyphen.core.model

/**
 * A span of [MarkupStyle] applied over a character range [start]..[end]
 * (start-inclusive, end-exclusive) within editor text.
 *
 * **In `hyphen`** — spans may carry any [MarkupStyle], including block-level
 * styles such as [MarkupStyle.BulletList] or [MarkupStyle.H1].
 *
 * **In `hyphen-blocks`** — spans inside a block's text field must only carry
 * [MarkupStyle.Inline] variants. This is enforced by contract in
 * `HyphenInlineState` rather than at the type level, keeping the data class simple.
 */
data class StyleRange(
    val style: MarkupStyle,
    val start: Int,
    val end: Int,
) {
    init {
        require(start <= end) { "Invalid range: start ($start) > end ($end)" }
        require(start >= 0) { "Invalid range: start cannot be negative" }
    }
}