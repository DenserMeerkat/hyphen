package com.denser.hyphen.markdown

import com.denser.hyphen.state.SpanManager
import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.model.MarkupStyleRange
import com.denser.hyphen.model.StyleSets
import com.denser.hyphen.model.TriggerConfig

internal object MarkdownProcessor {

    data class ProcessResult(
        val cleanText: String,
        val newSpans: List<MarkupStyleRange>,
        val newCursorPosition: Int,
        val explicitlyClosedStyles: Set<MarkupStyle> = emptySet(),
    )

    fun process(
        rawText: String, 
        cursorPosition: Int,
        triggerConfigs: List<TriggerConfig> = emptyList()
    ): ProcessResult? {
        var processedText = rawText
        var extractedSpans = listOf<MarkupStyleRange>()
        var currentCursor = cursorPosition
        var hasChanges = false
        val closedStyles = mutableSetOf<MarkupStyle>()

        fun applyRule(
            regex: Regex,
            styleFactory: (MatchResult) -> MarkupStyle,
            getPrefixRemoved: (MatchResult) -> Int,
            getSuffixRemoved: (MatchResult) -> Int = { 0 },
            getPrefixAdded: (MatchResult) -> String = { "" },
            getSuffixAdded: (MatchResult) -> String = { "" }
        ) {
            var match = regex.find(processedText)

            while (match != null) {
                val matchRange = match.range.first..(match.range.last + 1)

                val overlaps = extractedSpans.any { existing ->
                    if (existing.style in StyleSets.allHeadings || existing.style in StyleSets.allBlock) return@any false
                    val existingRange = existing.start until existing.end
                    matchRange.first < existingRange.last && existingRange.first < matchRange.last
                }

                if (overlaps) {
                    match = regex.find(processedText, match.range.last + 1)
                    continue
                }

                val style = styleFactory(match)
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
                    extractedSpans = extractedSpans + MarkupStyleRange(style, startIndex, spanEnd)
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
                extractedSpans = extractedSpans + MarkupStyleRange(style, startIndex, spanEnd)

                match = regex.find(processedText)
            }
        }

        applyRule(MarkdownConstants.H1_REGEX, { MarkupStyle.H1 }, getPrefixRemoved = { 2 })
        applyRule(MarkdownConstants.H2_REGEX, { MarkupStyle.H2 }, getPrefixRemoved = { 3 })
        applyRule(MarkdownConstants.H3_REGEX, { MarkupStyle.H3 }, getPrefixRemoved = { 4 })
        applyRule(MarkdownConstants.H4_REGEX, { MarkupStyle.H4 }, getPrefixRemoved = { 5 })
        applyRule(MarkdownConstants.H5_REGEX, { MarkupStyle.H5 }, getPrefixRemoved = { 6 })
        applyRule(MarkdownConstants.H6_REGEX, { MarkupStyle.H6 }, getPrefixRemoved = { 7 })

        applyRule(
            MarkdownConstants.CHECKBOX_UNCHECKED_REGEX,
            { MarkupStyle.CheckboxUnchecked },
            getPrefixRemoved = { 6 },
            getPrefixAdded = { match -> match.value.substring(0, 6) }
        )
        applyRule(
            MarkdownConstants.CHECKBOX_CHECKED_REGEX,
            { MarkupStyle.CheckboxChecked },
            getPrefixRemoved = { 6 },
            getPrefixAdded = { match -> match.value.substring(0, 6) }
        )

        applyRule(
            MarkdownConstants.BULLET_LIST_REGEX,
            { MarkupStyle.BulletList },
            getPrefixRemoved = { 2 },
            getPrefixAdded = { match -> match.value.substring(0, 2) }
        )
        applyRule(
            MarkdownConstants.BLOCKQUOTE_REGEX,
            { MarkupStyle.Blockquote },
            getPrefixRemoved = { 2 },
            getPrefixAdded = { match -> match.value.substring(0, 2) }
        )
        applyRule(
            MarkdownConstants.ORDERED_LIST_REGEX,
            { MarkupStyle.OrderedList },
            getPrefixRemoved = { match -> match.value.indexOf('.') + 2 },
            getPrefixAdded = { match -> match.value.substring(0, match.value.indexOf('.') + 2) }
        )

        triggerConfigs.forEach { config ->
            val escapedTrigger = Regex.escape(config.trigger)
            val longerTriggers = triggerConfigs.filter { it.trigger.length > config.trigger.length && it.trigger.contains(config.trigger) }
            
            val lookaheads = mutableSetOf<String>()
            val lookbehinds = mutableSetOf<String>()

            longerTriggers.forEach { longer ->
                var idx = longer.trigger.indexOf(config.trigger)
                while (idx != -1) {
                    val prefix = longer.trigger.substring(0, idx)
                    val suffix = longer.trigger.substring(idx + config.trigger.length)
                    
                    if (prefix.isNotEmpty()) lookbehinds.add(Regex.escape(prefix))
                    if (suffix.isNotEmpty()) lookaheads.add(Regex.escape(suffix))
                    idx = longer.trigger.indexOf(config.trigger, idx + 1)
                }
            }

            val lookahead = if (lookaheads.isNotEmpty()) "(?!${lookaheads.joinToString("|")})" else ""
            val lookbehind = if (lookbehinds.isNotEmpty()) "(?<!${lookbehinds.joinToString("|")})" else ""
            
            val pattern = if (config.endTrigger.isNullOrEmpty()) {
                """(?<![\w\[])$lookbehind$escapedTrigger$lookahead([^\s]+)"""
            } else {
                val escapedEnd = Regex.escape(config.endTrigger)
                """(?<!\[)$lookbehind$escapedTrigger$lookahead((?:(?!$escapedEnd).)+?)$escapedEnd"""
            }
            
            val autoMentionRegex = Regex(pattern)
            var match = autoMentionRegex.find(processedText)
            while (match != null) {
                val isTyping = cursorPosition >= match.range.first && cursorPosition <= match.range.last + 1
                val isFormallyClosed = !config.endTrigger.isNullOrEmpty()
                
                if (isTyping && !isFormallyClosed) {
                    match = autoMentionRegex.find(processedText, match.range.last + 1)
                    continue
                }

                val overlaps = extractedSpans.any { span ->
                    if (span.style in StyleSets.allHeadings || span.style in StyleSets.allBlock) return@any false
                    val matchRange = match.range.first..(match.range.last + 1)
                    val spanRange = span.start until span.end
                    matchRange.first < spanRange.last && spanRange.first < matchRange.last
                }
                
                if (overlaps) {
                    match = autoMentionRegex.find(processedText, match.range.last + 1)
                    continue
                }

                val style = MarkupStyle.Mention(
                    display = match.value,
                    scheme = config.scheme,
                    id = match.groupValues[1]
                )
                
                hasChanges = true
                extractedSpans = extractedSpans + MarkupStyleRange(style, match.range.first, match.range.last + 1)
                match = autoMentionRegex.find(processedText, match.range.last + 1)
            }
        }

        applyRule(MarkdownConstants.BOLD_REGEX, { MarkupStyle.Bold }, { 2 }, { 2 })
        applyRule(MarkdownConstants.UNDERLINE_REGEX, { MarkupStyle.Underline }, { 2 }, { 2 })
        applyRule(MarkdownConstants.STRIKETHROUGH_REGEX, { MarkupStyle.Strikethrough }, { 2 }, { 2 })
        applyRule(MarkdownConstants.HIGHLIGHT_REGEX, { MarkupStyle.Highlight }, { 2 }, { 2 })
        applyRule(MarkdownConstants.INLINE_CODE_REGEX, { MarkupStyle.InlineCode }, { 1 }, { 1 })
        applyRule(MarkdownConstants.ITALIC_ASTERISK_REGEX, { MarkupStyle.Italic }, { 1 }, { 1 })
        applyRule(MarkdownConstants.ITALIC_UNDERSCORE_REGEX, { MarkupStyle.Italic }, { 1 }, { 1 })

        val mentionSchemes = triggerConfigs.map { it.scheme }.distinct()
        if (mentionSchemes.isNotEmpty()) {
            val mentionPattern = mentionSchemes.joinToString("|") { Regex.escape(it) }
            val dynamicMentionRegex = Regex(MarkdownConstants.MENTION_REGEX_TEMPLATE.replace("%s", mentionPattern))
            
            applyRule(
                regex = dynamicMentionRegex,
                styleFactory = { match ->
                    MarkupStyle.Mention(
                        display = match.groupValues[1],
                        scheme = match.groupValues[2],
                        id = match.groupValues[3]
                    )
                },
                getPrefixRemoved = { 1 },
                getSuffixRemoved = { match -> match.groupValues[2].length + match.groupValues[3].length + 4 }
            )
        }

        applyRule(
            regex = MarkdownConstants.LINK_REGEX,
            styleFactory = { match -> MarkupStyle.Link(match.groupValues[2]) },
            getPrefixRemoved = { 1 },
            getSuffixRemoved = { match -> match.groupValues[2].length + 3 }
        )

        if (!hasChanges) return null

        return ProcessResult(
            cleanText = processedText,
            newSpans = extractedSpans,
            newCursorPosition = currentCursor,
            explicitlyClosedStyles = closedStyles
        )
    }
}