package com.denser.hyphen.markdown

internal object MarkdownConstants {
    // **text**
    val BOLD_REGEX = Regex("\\*\\*(.*?)\\*\\*")

    // *text*
    val ITALIC_ASTERISK_REGEX = Regex("(?<!\\*)\\*(?!\\*)(.*?)(?<!\\*)\\*(?!\\*)")

    // _text_
    val ITALIC_UNDERSCORE_REGEX = Regex("(?<!_)_(?!_)(.*?)(?<!_)_(?!_)")

    // ~~text~~
    val STRIKETHROUGH_REGEX = Regex("~~(.*?)~~")

    // __text__
    val UNDERLINE_REGEX = Regex("__(.*?)__")

    // `text`
    val INLINE_CODE_REGEX = Regex("`(.*?)`")

    // ==text==
    val HIGHLIGHT_REGEX = Regex("==(.*?)==")

    // -, *, or • at line start
    val BULLET_LIST_REGEX = Regex(
        pattern = """^[\-*•] (.*?)$""",
        option = RegexOption.MULTILINE
    )

    // 1. at line start
    val ORDERED_LIST_REGEX = Regex(
        pattern = """^\d+\. (.*?)$""",
        option = RegexOption.MULTILINE
    )

    // > or ┃ at line start
    val BLOCKQUOTE_REGEX = Regex(
        pattern = """^[>┃] (.*?)$""",
        option = RegexOption.MULTILINE
    )
}