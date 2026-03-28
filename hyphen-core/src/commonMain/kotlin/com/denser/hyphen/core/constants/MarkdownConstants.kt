package com.denser.hyphen.core.constants

/**
 * Regex patterns and string constants for all Markdown syntax recognized by the Hyphen editor family.
 */
object MarkdownConstants {

    object Regex {
        object Inline {
            // **text**
            val BOLD = Regex("\\*\\*(?!\\s)(.+?)\\*\\*")

            // __text__
            val UNDERLINE = Regex("__(?!\\s)(.+?)\\_\\_")

            // ~~text~~
            val STRIKETHROUGH = Regex("~~(?!\\s)(.+?)~~")

            // ==text==
            val HIGHLIGHT = Regex("==(?!\\s)(.+?)==")

            // `text`
            val INLINE_CODE = Regex("`(?!\\s)(.+?)`")

            // *text*
            val ITALIC_ASTERISK = Regex("(?<!\\*)\\*(?!\\*|\\s)(.+?)(?<!\\*)\\*(?!\\*)")

            // _text_
            val ITALIC_UNDERSCORE = Regex("(?<!_)_(?!_|\\s)(.+?)(?<!_)_(?!_)")
        }

        object Block {
            // -, *, or • at line start
            val BULLET_LIST = Regex(
                pattern = """^[\-*•] (.*?)$""",
                option = RegexOption.MULTILINE,
            )

            // 1. at line start
            val ORDERED_LIST = Regex(
                pattern = """^\d+\. (.*?)$""",
                option = RegexOption.MULTILINE,
            )

            // > or ┃ at line start
            val BLOCKQUOTE = Regex(
                pattern = """^[>┃] (.*?)$""",
                option = RegexOption.MULTILINE,
            )

            // - [ ] or * [ ] at line start
            val CHECKBOX_UNCHECKED = Regex(
                pattern = """^[\-*] \[\s\] (.*?)$""",
                option = RegexOption.MULTILINE,
            )

            // - [x] or * [X] at line start
            val CHECKBOX_CHECKED = Regex(
                pattern = """^[\-*] \[[xX]\] (.*?)$""",
                option = RegexOption.MULTILINE,
            )

            // # Headings
            val H1 = Regex("""^# (.+?)$""", RegexOption.MULTILINE)
            val H2 = Regex("""^## (.+?)$""", RegexOption.MULTILINE)
            val H3 = Regex("""^### (.+?)$""", RegexOption.MULTILINE)
            val H4 = Regex("""^#### (.+?)$""", RegexOption.MULTILINE)
            val H5 = Regex("""^##### (.+?)$""", RegexOption.MULTILINE)
            val H6 = Regex("""^###### (.+?)$""", RegexOption.MULTILINE)
        }

        object Trigger {
            val ORDERED_LIST = Regex("""^(\d+)\. $""")
        }
    }

    object Triggers {
        // Bullets
        const val BULLET_HYPHEN = "- "
        const val BULLET_ASTERISK = "* "
        const val BULLET_SYMBOL = "• "

        // Quote
        const val BLOCKQUOTE = "> "

        // Checkboxes
        const val CHECKBOX_UNCHECKED_HYPHEN = "- [ ] "
        const val CHECKBOX_UNCHECKED_ASTERISK = "* [ ] "
        const val CHECKBOX_CHECKED_HYPHEN_L = "- [x] "
        const val CHECKBOX_CHECKED_HYPHEN_U = "- [X] "
        const val CHECKBOX_CHECKED_ASTERISK_L = "* [x] "
        const val CHECKBOX_CHECKED_ASTERISK_U = "* [X] "

        // Headings
        const val HEADING_H1 = "# "
        const val HEADING_H2 = "## "
        const val HEADING_H3 = "### "
        const val HEADING_H4 = "#### "
        const val HEADING_H5 = "##### "
        const val HEADING_H6 = "###### "
    }
}
