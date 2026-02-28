package com.denser.hyphen.markdown

internal object MarkdownConstants {
    // **text**
    val BOLD_REGEX = Regex("\\*\\*(.*?)\\*\\*")

    // *text* or _text_
    val ITALIC_REGEX = Regex("(?<!\\*)\\*(?!\\*)(.*?)(?<!\\*)\\*(?!\\*)|(?<!_)_(?!_)(.*?)(?<!_)_(?!_)")

    // ~~text~~
    val STRIKETHROUGH_REGEX = Regex("~~(.*?)~~")

    // __text__
    val UNDERLINE_REGEX = Regex("__(.*?)__")

    // `text`
    val INLINE_CODE_REGEX = Regex("`(.*?)`")

    // ==text==
    val HIGHLIGHT_REGEX = Regex("==(.*?)==")

    // -, *, or • at line start
    val BULLET_LIST_REGEX = Regex("(?m)^[\\-*•] (.*?)$")

    // 1. at line start
    val ORDERED_LIST_REGEX = Regex("(?m)^\\d+\\. (.*?)$")

    // > or ┃ at line start
    val BLOCKQUOTE_REGEX = Regex("(?m)^[>┃] (.*?)$")

    // Headers
    val H1_REGEX = Regex("(?m)^# (.*?)$")
    val H2_REGEX = Regex("(?m)^## (.*?)$")
    val H3_REGEX = Regex("(?m)^### (.*?)$")
    val H4_REGEX = Regex("(?m)^#### (.*?)$")
    val H5_REGEX = Regex("(?m)^##### (.*?)$")
    val H6_REGEX = Regex("(?m)^###### (.*?)$")
}