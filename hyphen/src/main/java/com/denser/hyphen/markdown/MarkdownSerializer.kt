package com.denser.hyphen.markdown

import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.model.MarkupStyleRange

object MarkdownSerializer {
    private data class Insertion(val index: Int, val symbol: String, val isClosing: Boolean)

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
            val symbol = when (span.style) {
                is MarkupStyle.Bold -> "**"
                is MarkupStyle.Italic -> "*"
                is MarkupStyle.Underline -> "__"
                is MarkupStyle.Strikethrough -> "~~"
                is MarkupStyle.Highlight -> "=="
                is MarkupStyle.InlineCode -> "`"
                else -> null
            }

            if (symbol != null) {
                insertions.add(Insertion(span.start, symbol, isClosing = false))
                insertions.add(Insertion(span.end, symbol, isClosing = true))
            }
        }

        insertions.sortWith(compareByDescending<Insertion> { it.index }.thenBy { it.isClosing })

        for (insertion in insertions) {
            val safeIndex = insertion.index.coerceIn(0, builder.length)
            builder.insert(safeIndex, insertion.symbol)
        }

        return builder.toString()
    }
}