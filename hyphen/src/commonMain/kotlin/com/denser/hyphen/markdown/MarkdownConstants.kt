package com.denser.hyphen.markdown

internal object MarkdownConstants {
    // **text**
    val BOLD_REGEX = Regex("\\*\\*(?!\\s)(.+?)\\*\\*")

    // *text*
    val ITALIC_ASTERISK_REGEX = Regex("(?<!\\*)\\*(?!\\*|\\s)(.+?)(?<!\\*)\\*(?!\\*)")

    // _text_
    val ITALIC_UNDERSCORE_REGEX = Regex("(?<!_)_(?!_|\\s)(.+?)(?<!_)_(?!_)")

    // ~~text~~
    val STRIKETHROUGH_REGEX = Regex("~~(?!\\s)(.+?)~~")

    // __text__
    val UNDERLINE_REGEX = Regex("__(?!\\s)(.+?)__")

    // `text`
    val INLINE_CODE_REGEX = Regex("`(?!\\s)(.+?)`")

    // ==text==
    val HIGHLIGHT_REGEX = Regex("==(?!\\s)(.+?)==")

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

    // - [ ] or * [ ] at line start
    val CHECKBOX_UNCHECKED_REGEX = Regex(
        pattern = """^[\-*] \[\s\] (.*?)$""",
        option = RegexOption.MULTILINE
    )

    // - [x] or * [X] at line start
    val CHECKBOX_CHECKED_REGEX = Regex(
        pattern = """^[\-*] \[[xX]\] (.*?)$""",
        option = RegexOption.MULTILINE
    )

    // # Heading 1
    val H1_REGEX = Regex(
        pattern = """^# (.+?)$""",
        option = RegexOption.MULTILINE
    )

    // ## Heading 2
    val H2_REGEX = Regex(
        pattern = """^## (.+?)$""",
        option = RegexOption.MULTILINE
    )

    // ### Heading 3
    val H3_REGEX = Regex(
        pattern = """^### (.+?)$""",
        option = RegexOption.MULTILINE
    )

    // #### Heading 4
    val H4_REGEX = Regex(
        pattern = """^#### (.+?)$""",
        option = RegexOption.MULTILINE
    )

    // ##### Heading 5
    val H5_REGEX = Regex(
        pattern = """^##### (.+?)$""",
        option = RegexOption.MULTILINE
    )

    // ###### Heading 6
    val H6_REGEX = Regex(
        pattern = """^###### (.+?)$""",
        option = RegexOption.MULTILINE
    )
}