package com.denser.hyphen.markdown

import com.denser.hyphen.state.SpanManager
import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.model.MarkupStyleRange
import kotlin.collections.plus

internal object MarkdownProcessor {

    data class ProcessResult(
        val cleanText: String,
        val newSpans: List<MarkupStyleRange>,
        val newCursorPosition: Int,
    )

    fun process(rawText: String, cursorPosition: Int): ProcessResult? {
        var processedText = rawText
        var extractedSpans = listOf<MarkupStyleRange>()
        var currentCursor = cursorPosition
        var hasChanges = false

        fun applyRule(
            regex: Regex,
            style: MarkupStyle,
            getPrefixRemoved: (MatchResult) -> Int,
            getSuffixRemoved: (MatchResult) -> Int = { 0 },
            getPrefixAdded: (MatchResult) -> String = { "" },
            getSuffixAdded: (MatchResult) -> String = { "" }
        ) {
            var match = regex.find(processedText)

            while (match != null) {
                val innerText = match.groupValues[1]
                val startIndex = match.range.first

                val prefixRemoved = getPrefixRemoved(match)
                val suffixRemoved = getSuffixRemoved(match)
                val prefixAdded = getPrefixAdded(match)
                val suffixAdded = getSuffixAdded(match)
                val transformedContent = prefixAdded + innerText + suffixAdded

                if (transformedContent == match.value) {
                    hasChanges = true
                    val spanEnd = startIndex + transformedContent.length
                    extractedSpans = extractedSpans + MarkupStyleRange(style, startIndex, spanEnd)
                    match = regex.find(processedText, match.range.last + 1)
                    continue
                }

                hasChanges = true

                val innerShift = prefixAdded.length - prefixRemoved
                val totalShift = innerShift + (suffixAdded.length - suffixRemoved)

                extractedSpans =
                    SpanManager.shiftSpans(extractedSpans, startIndex + prefixRemoved, innerShift)
                extractedSpans = SpanManager.shiftSpans(
                    extractedSpans,
                    startIndex + match.value.length + innerShift,
                    suffixAdded.length - suffixRemoved,
                )

                when {
                    currentCursor > startIndex && currentCursor <= startIndex + prefixRemoved ->
                        currentCursor = startIndex + prefixAdded.length

                    currentCursor > startIndex + prefixRemoved &&
                            currentCursor <= startIndex + match.value.length - suffixRemoved ->
                        currentCursor += innerShift

                    currentCursor > startIndex + match.value.length - suffixRemoved &&
                            currentCursor <= startIndex + match.value.length ->
                        currentCursor =
                            startIndex + prefixAdded.length + innerText.length + suffixAdded.length

                    currentCursor > startIndex + match.value.length ->
                        currentCursor += totalShift
                }

                processedText = processedText.replaceRange(match.range, transformedContent)

                val spanEnd = startIndex + transformedContent.length
                extractedSpans = extractedSpans + MarkupStyleRange(style, startIndex, spanEnd)

                match = regex.find(processedText)
            }
        }

        applyRule(MarkdownConstants.BOLD_REGEX, MarkupStyle.Bold, { 2 }, { 2 })
        applyRule(MarkdownConstants.UNDERLINE_REGEX, MarkupStyle.Underline, { 2 }, { 2 })
        applyRule(MarkdownConstants.STRIKETHROUGH_REGEX, MarkupStyle.Strikethrough, { 2 }, { 2 })
        applyRule(MarkdownConstants.HIGHLIGHT_REGEX, MarkupStyle.Highlight, { 2 }, { 2 })
        applyRule(MarkdownConstants.INLINE_CODE_REGEX, MarkupStyle.InlineCode, { 1 }, { 1 })
        applyRule(MarkdownConstants.ITALIC_ASTERISK_REGEX, MarkupStyle.Italic, { 1 }, { 1 })
        applyRule(MarkdownConstants.ITALIC_UNDERSCORE_REGEX, MarkupStyle.Italic, { 1 }, { 1 })

        applyRule(
            MarkdownConstants.BULLET_LIST_REGEX,
            MarkupStyle.BulletList,
            getPrefixRemoved = { 2 },
            getPrefixAdded = { match -> match.value.substring(0, 2) }
        )
        applyRule(
            MarkdownConstants.BLOCKQUOTE_REGEX,
            MarkupStyle.Blockquote,
            getPrefixRemoved = { 2 },
            getPrefixAdded = { match -> match.value.substring(0, 2) }
        )
        applyRule(
            MarkdownConstants.ORDERED_LIST_REGEX,
            MarkupStyle.OrderedList,
            getPrefixRemoved = { match -> match.value.indexOf('.') + 2 },
            getPrefixAdded = { match -> match.value.substring(0, match.value.indexOf('.') + 2) }
        )

        if (!hasChanges) return null

        return ProcessResult(
            cleanText = processedText,
            newSpans = extractedSpans,
            newCursorPosition = currentCursor,
        )
    }
}