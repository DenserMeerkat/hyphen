package com.denser.hyphen.markdown

internal object MarkdownConstants {
    // **text**
    val BOLD_REGEX = Regex("""\*\*(.+?)\*\*""")

    // *text*
    val ITALIC_ASTERISK_REGEX = Regex("""(?<!\*)\*(?!\*)(.+?)(?<!\*)\*(?!\*)""")

    // _text_
    val ITALIC_UNDERSCORE_REGEX = Regex("""(?<![\w_])_(?!_)(.+?)(?<!_)_(?![\w_])""")

    // ~~text~~
    val STRIKETHROUGH_REGEX = Regex("""~~(.+?)~~""")

    // __text__
    val UNDERLINE_REGEX = Regex("""(?<![\w_])__(?!_)(.+?)(?<!_)__(?![\w_])""")

    // `text`
    val INLINE_CODE_REGEX = Regex("""`(.+?)`""")

    // ==text==
    val HIGHLIGHT_REGEX = Regex("""==(.+?)==""")

    // [text](url)
    val LINK_REGEX = Regex("\\[(.+?)\\]\\((.+?)\\)")

    // [display](scheme:id)
    const val MENTION_REGEX_TEMPLATE = """\[(.+?)\]\((%s):(.+?)\)"""

    // -, *, or • at line start (with optional leading indentation)
    val BULLET_LIST_REGEX = Regex(
        pattern = """^[ \t]*[\-*•][ \u00A0](.*?)$""",
        option = RegexOption.MULTILINE
    )

    // 1. at line start (with optional leading indentation)
    val ORDERED_LIST_REGEX = Regex(
        pattern = """^[ \t]*\d+\.[ \u00A0](.*?)$""",
        option = RegexOption.MULTILINE
    )

    // > at line start
    val BLOCKQUOTE_REGEX = Regex(
        pattern = """^>[ \u00A0](.*?)$""",
        option = RegexOption.MULTILINE
    )

    // - [ ] or * [ ] at line start (with optional leading indentation)
    val CHECKBOX_UNCHECKED_REGEX = Regex(
        pattern = """^[ \t]*[\-*][ \u00A0]\[\s\][ \u00A0](.*?)$""",
        option = RegexOption.MULTILINE
    )

    // - [x] or * [X] at line start (with optional leading indentation)
    val CHECKBOX_CHECKED_REGEX = Regex(
        pattern = """^[ \t]*[\-*][ \u00A0]\[[xX]\][ \u00A0](.*?)$""",
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