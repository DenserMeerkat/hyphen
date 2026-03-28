package com.denser.hyphen.core.markdown

import com.denser.hyphen.core.constants.MarkdownConstants
import com.denser.hyphen.core.model.MarkupStyle
import com.denser.hyphen.core.model.StyleRange
import com.denser.hyphen.core.state.SpanManager

/**
 * Detects and strips Markdown syntax from raw text, returning clean text and
 * a list of [StyleRange] spans.
 */
object MarkdownProcessor {

    data class ProcessResult(
        val cleanText: String,
        val newSpans: List<StyleRange>,
        val newCursorPosition: Int,
        val explicitlyClosedStyles: Set<MarkupStyle> = emptySet(),
    )

    /**
     * @param inlineOnly When `true`, only inline rules are applied.
     * @return A [ProcessResult] if any Markdown was detected and stripped,
     *   or `null` if the text required no changes.
     */
    fun process(
        rawText: String,
        cursorPosition: Int,
        inlineOnly: Boolean = false,
    ): ProcessResult? {
        var processedText = rawText
        var extractedSpans = listOf<StyleRange>()
        var currentCursor = cursorPosition
        var hasChanges = false
        val closedStyles = mutableSetOf<MarkupStyle>()

        fun applyRule(
            regex: Regex,
            style: MarkupStyle,
            getPrefixRemoved: (MatchResult) -> Int,
            getSuffixRemoved: (MatchResult) -> Int = { 0 },
            getPrefixAdded: (MatchResult) -> String = { "" },
            getSuffixAdded: (MatchResult) -> String = { "" },
        ) {
            var match = regex.find(processedText)

            while (match != null) {
                val innerText = match.groupValues[1]
                val startIndex = match.range.first

                if (currentCursor == startIndex + match.value.length) {
                    closedStyles.add(style)
                }

                val prefixRemoved = getPrefixRemoved(match)
                val suffixRemoved = getSuffixRemoved(match)
                val prefixAdded = getPrefixAdded(match)
                val suffixAdded = getSuffixAdded(match)
                val transformedContent = prefixAdded + innerText + suffixAdded

                if (transformedContent == match.value) {
                    hasChanges = true
                    val spanEnd = startIndex + transformedContent.length
                    extractedSpans = extractedSpans + StyleRange(style, startIndex, spanEnd)
                    match = regex.find(processedText, match.range.last + 1)
                    continue
                }

                hasChanges = true

                val innerShift = prefixAdded.length - prefixRemoved
                val totalShift = innerShift + (suffixAdded.length - suffixRemoved)

                extractedSpans = SpanManager.shiftSpans(extractedSpans, startIndex, innerShift)
                val suffixChangeStart = startIndex + match.value.length - suffixRemoved + innerShift
                extractedSpans = SpanManager.shiftSpans(
                    extractedSpans,
                    suffixChangeStart,
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
                extractedSpans = extractedSpans + StyleRange(style, startIndex, spanEnd)

                match = regex.find(processedText)
            }
        }

        applyRule(MarkdownConstants.Regex.Inline.BOLD, MarkupStyle.Bold, { 2 }, { 2 })
        applyRule(MarkdownConstants.Regex.Inline.UNDERLINE, MarkupStyle.Underline, { 2 }, { 2 })
        applyRule(MarkdownConstants.Regex.Inline.STRIKETHROUGH, MarkupStyle.Strikethrough, { 2 }, { 2 })
        applyRule(MarkdownConstants.Regex.Inline.HIGHLIGHT, MarkupStyle.Highlight, { 2 }, { 2 })
        applyRule(MarkdownConstants.Regex.Inline.INLINE_CODE, MarkupStyle.InlineCode, { 1 }, { 1 })
        applyRule(MarkdownConstants.Regex.Inline.ITALIC_ASTERISK, MarkupStyle.Italic, { 1 }, { 1 })
        applyRule(MarkdownConstants.Regex.Inline.ITALIC_UNDERSCORE, MarkupStyle.Italic, { 1 }, { 1 })

        if (!inlineOnly) {
            applyRule(
                MarkdownConstants.Regex.Block.CHECKBOX_UNCHECKED,
                MarkupStyle.CheckboxUnchecked,
                getPrefixRemoved = { 6 },
                getPrefixAdded = { match -> match.value.substring(0, 6) },
            )
            applyRule(
                MarkdownConstants.Regex.Block.CHECKBOX_CHECKED,
                MarkupStyle.CheckboxChecked,
                getPrefixRemoved = { 6 },
                getPrefixAdded = { match -> match.value.substring(0, 6) },
            )
            applyRule(
                MarkdownConstants.Regex.Block.BULLET_LIST,
                MarkupStyle.BulletList,
                getPrefixRemoved = { 2 },
                getPrefixAdded = { match -> match.value.substring(0, 2) },
            )
            applyRule(
                MarkdownConstants.Regex.Block.BLOCKQUOTE,
                MarkupStyle.Blockquote,
                getPrefixRemoved = { 2 },
                getPrefixAdded = { match -> match.value.substring(0, 2) },
            )
            applyRule(
                MarkdownConstants.Regex.Block.ORDERED_LIST,
                MarkupStyle.OrderedList,
                getPrefixRemoved = { match -> match.value.indexOf('.') + 2 },
                getPrefixAdded = { match -> match.value.substring(0, match.value.indexOf('.') + 2) },
            )
            applyRule(MarkdownConstants.Regex.Block.H1, MarkupStyle.H1, getPrefixRemoved = { 2 })
            applyRule(MarkdownConstants.Regex.Block.H2, MarkupStyle.H2, getPrefixRemoved = { 3 })
            applyRule(MarkdownConstants.Regex.Block.H3, MarkupStyle.H3, getPrefixRemoved = { 4 })
            applyRule(MarkdownConstants.Regex.Block.H4, MarkupStyle.H4, getPrefixRemoved = { 5 })
            applyRule(MarkdownConstants.Regex.Block.H5, MarkupStyle.H5, getPrefixRemoved = { 6 })
            applyRule(MarkdownConstants.Regex.Block.H6, MarkupStyle.H6, getPrefixRemoved = { 7 })
        }

        if (!hasChanges) return null

        return ProcessResult(
            cleanText = processedText,
            newSpans = extractedSpans,
            newCursorPosition = currentCursor,
            explicitlyClosedStyles = closedStyles,
        )
    }
}