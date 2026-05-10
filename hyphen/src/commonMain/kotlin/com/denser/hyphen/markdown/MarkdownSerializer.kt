package com.denser.hyphen.markdown

import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.model.MarkupStyleRange
import com.denser.hyphen.model.StyleSets

object MarkdownSerializer {
    private data class Insertion(
        val index: Int,
        val symbol: String,
        val isClosing: Boolean,
        val priority: Int
    )

    private fun getStylePriority(style: MarkupStyle): Int = when (style) {
        is MarkupStyle.H1, is MarkupStyle.H2, is MarkupStyle.H3,
        is MarkupStyle.H4, is MarkupStyle.H5, is MarkupStyle.H6 -> 10
        is MarkupStyle.Bold -> 20
        is MarkupStyle.Italic -> 30
        is MarkupStyle.Strikethrough, is MarkupStyle.Underline, is MarkupStyle.Highlight -> 40
        is MarkupStyle.Link, is MarkupStyle.Mention -> 50
        is MarkupStyle.InlineCode -> 100
        else -> 200
    }

    fun serialize(text: String, spans: List<MarkupStyleRange>, start: Int, end: Int): String {
        if (start >= end) return ""

        val safeStart = start.coerceIn(0, text.length)
        val safeEnd = end.coerceIn(safeStart, text.length)
        val selectedText = text.substring(safeStart, safeEnd)

        val selectedSpans = spans.mapNotNull { span ->
            val intersectStart = maxOf(safeStart, span.start)
            val intersectEnd = minOf(safeEnd, span.end)

            if (intersectStart < intersectEnd) {
                MarkupStyleRange(
                    style = span.style,
                    start = intersectStart - safeStart,
                    end = intersectEnd - safeStart
                )
            } else null
        }

        return serialize(selectedText, selectedSpans)
    }

    fun serialize(text: String, spans: List<MarkupStyleRange>): String {
        val builder = StringBuilder(text)
        val insertions = mutableListOf<Insertion>()

        for (span in spans) {
            val startSymbol = when (span.style) {
                is MarkupStyle.Bold -> "**"
                is MarkupStyle.Italic -> "*"
                is MarkupStyle.Underline -> "__"
                is MarkupStyle.Strikethrough -> "~~"
                is MarkupStyle.Highlight -> "=="
                is MarkupStyle.InlineCode -> "`"
                is MarkupStyle.H1 -> "# "
                is MarkupStyle.H2 -> "## "
                is MarkupStyle.H3 -> "### "
                is MarkupStyle.H4 -> "#### "
                is MarkupStyle.H5 -> "##### "
                is MarkupStyle.H6 -> "###### "
                is MarkupStyle.Link -> "["
                is MarkupStyle.Mention -> "["
                else -> null
            }

            val endSymbol = when (val style = span.style) {
                in StyleSets.allHeadings -> ""
                is MarkupStyle.Link -> "](${style.url})"
                is MarkupStyle.Mention -> "](${style.scheme}:${style.id})"
                else -> startSymbol
            }

            if (startSymbol != null) {
                val priority = getStylePriority(span.style)
                insertions.add(Insertion(span.start, startSymbol, isClosing = false, priority))
                if (endSymbol != null && endSymbol.isNotEmpty()) {
                    insertions.add(Insertion(span.end, endSymbol, isClosing = true, priority))
                }
            }
        }

        insertions.sortWith(Comparator { a, b ->
            if (a.index != b.index) {
                // Primary sort: index DESCENDING (insert from end to start)
                b.index.compareTo(a.index)
            } else if (a.isClosing != b.isClosing) {
                // Secondary sort: Openings before Closings in the list
                // (Closing inserted last -> ends up on the left: "] [")
                a.isClosing.compareTo(b.isClosing)
            } else {
                // Tertiary sort: Priority
                if (a.isClosing) {
                    // For Closings: Outer (lower priority) before Inner (higher priority) in list
                    // (Inner inserted last -> ends up on the left: "Inner Outer")
                    a.priority.compareTo(b.priority)
                } else {
                    // For Openings: Inner (higher priority) before Outer (lower priority) in list
                    // (Outer inserted last -> ends up on the left: "Outer Inner")
                    b.priority.compareTo(a.priority)
                }
            }
        })

        for (insertion in insertions) {
            val safeIndex = insertion.index.coerceIn(0, builder.length)
            builder.insert(safeIndex, insertion.symbol)
        }

        return builder.toString()
    }
}