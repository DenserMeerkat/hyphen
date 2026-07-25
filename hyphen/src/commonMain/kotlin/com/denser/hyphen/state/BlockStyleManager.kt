package com.denser.hyphen.state

import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.insert
import androidx.compose.ui.text.TextRange
import com.denser.hyphen.markdown.MarkdownConstants
import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.model.MarkupStyleRange
import com.denser.hyphen.model.StyleSets

internal object BlockStyleManager {

    fun isBlockStyle(style: MarkupStyle): Boolean {
        return style in StyleSets.allBlock
    }

    fun hasBlockStyle(text: String, selection: TextRange, style: MarkupStyle): Boolean {
        val selStart = minOf(selection.start, selection.end)
        val selEnd = maxOf(selection.start, selection.end)

        val lineStarts = mutableListOf<Int>()
        var currentStart = text.lastIndexOf('\n', (selStart - 1).coerceAtLeast(0)) + 1
        if (currentStart == -1) currentStart = 0
        
        while (currentStart <= selEnd) {
            lineStarts.add(currentStart)
            val nextNewline = text.indexOf('\n', currentStart)
            if (nextNewline == -1 || nextNewline >= selEnd) break
            currentStart = nextNewline + 1
        }

        return lineStarts.any { start ->
            val end = text.indexOf('\n', start).let { if (it == -1) text.length else it }
            val lineText = text.substring(start, end)
            
            when (style) {
                is MarkupStyle.CheckboxUnchecked -> MarkdownConstants.CHECKBOX_UNCHECKED_REGEX.containsMatchIn(lineText)
                is MarkupStyle.CheckboxChecked -> MarkdownConstants.CHECKBOX_CHECKED_REGEX.containsMatchIn(lineText)
                is MarkupStyle.BulletList -> MarkdownConstants.BULLET_LIST_REGEX.containsMatchIn(lineText) &&
                        !MarkdownConstants.CHECKBOX_UNCHECKED_REGEX.containsMatchIn(lineText) &&
                        !MarkdownConstants.CHECKBOX_CHECKED_REGEX.containsMatchIn(lineText)
                is MarkupStyle.OrderedList -> MarkdownConstants.ORDERED_LIST_REGEX.containsMatchIn(lineText)
                is MarkupStyle.Blockquote -> MarkdownConstants.BLOCKQUOTE_REGEX.containsMatchIn(lineText)
                is MarkupStyle.H1 -> text.startsWith("# ", start)
                is MarkupStyle.H2 -> text.startsWith("## ", start)
                is MarkupStyle.H3 -> text.startsWith("### ", start)
                is MarkupStyle.H4 -> text.startsWith("#### ", start)
                is MarkupStyle.H5 -> text.startsWith("##### ", start)
                is MarkupStyle.H6 -> text.startsWith("###### ", start)
                else -> false
            }
        }
    }

    fun handleSmartEnter(state: HyphenTextState, buffer: TextFieldBuffer): Boolean {
        val bufferText = buffer.asCharSequence()
        val cursor = buffer.selection.start
        if (cursor <= 0) return false

        val lastNewline = bufferText.lastIndexOf('\n', cursor - 1)
        val lineStart = if (lastNewline == -1) 0 else lastNewline + 1
        val lineEnd = bufferText.indexOf('\n', lineStart).let { if (it == -1) bufferText.length else it }
        val fullLineText = bufferText.substring(lineStart, lineEnd)

        val indent = fullLineText.takeWhile { it == ' ' || it == '\t' }
        val unindentedLine = fullLineText.substring(indent.length)

        val styleCheckIndex = lineStart + indent.length

        return when {
            (state.isStyleAt(styleCheckIndex, MarkupStyle.CheckboxUnchecked) || state.isStyleAt(styleCheckIndex, MarkupStyle.CheckboxChecked)) -> {
                val isPrefixOnly = Regex("""^[\-*][ \u00A0]\[[\s\S]\][ \u00A0]?$""").matches(unindentedLine)
                if (isPrefixOnly) {
                    buffer.replace(lineStart, lineEnd, "")
                } else {
                    val prefix = if (unindentedLine.startsWith("*")) "* [ ] " else "- [ ] "
                    buffer.insert(cursor, "\n$indent$prefix")
                }
                true
            }

            state.isStyleAt(styleCheckIndex, MarkupStyle.BulletList) -> {
                val isPrefixOnly = Regex("""^[\-*•][ \u00A0]?$""").matches(unindentedLine)
                if (isPrefixOnly) {
                    buffer.replace(lineStart, lineEnd, "")
                } else {
                    val prefix = if (unindentedLine.isNotEmpty()) {
                        val firstChar = unindentedLine[0]
                        if (firstChar == '-' || firstChar == '*' || firstChar == '•') {
                            if (unindentedLine.length >= 2 && (unindentedLine[1] == ' ' || unindentedLine[1] == '\u00A0')) {
                                unindentedLine.take(2)
                            } else {
                                "$firstChar "
                            }
                        } else "- "
                    } else "- "
                    buffer.insert(cursor, "\n$indent$prefix")
                }
                true
            }

            state.isStyleAt(styleCheckIndex, MarkupStyle.OrderedList) -> {
                val dotIndex = unindentedLine.indexOf('.')
                val isPrefixOnly = Regex("""^\d+\.[ \u00A0]?$""").matches(unindentedLine)
                if (isPrefixOnly) {
                    buffer.replace(lineStart, lineEnd, "")
                } else {
                    val currentNumber = if (dotIndex != -1) unindentedLine.substring(0, dotIndex).toIntOrNull() ?: 1 else 1
                    buffer.insert(cursor, "\n$indent${currentNumber + 1}. ")
                }
                true
            }

            state.isStyleAt(styleCheckIndex, MarkupStyle.Blockquote) -> {
                val isPrefixOnly = Regex("""^>[ \u00A0]?$""").matches(fullLineText)
                if (isPrefixOnly) {
                    buffer.replace(lineStart, lineEnd, "")
                } else {
                    buffer.insert(cursor, "\n> ")
                }
                true
            }

            else -> false
        }
    }

    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    fun applyBlockStyle(
        buffer: TextFieldBuffer,
        spans: List<MarkupStyleRange>,
        selection: TextRange,
        style: MarkupStyle
    ): List<MarkupStyleRange> {
        val prefix = when (style) {
            is MarkupStyle.BulletList -> "- "
            is MarkupStyle.OrderedList -> "1. "
            is MarkupStyle.Blockquote -> "> "
            is MarkupStyle.CheckboxUnchecked -> "- [ ] "
            is MarkupStyle.CheckboxChecked -> "- [x] "
            else -> return spans
        }

        val selStart = minOf(selection.start, selection.end)
        val selEnd = maxOf(selection.start, selection.end)
        val bufferText = buffer.asCharSequence().toString()

        val lineStarts = mutableListOf<Int>()
        var currentStart = bufferText.lastIndexOf('\n', (selStart - 1).coerceAtLeast(0)) + 1
        if (currentStart == -1) currentStart = 0
        lineStarts.add(currentStart)

        var searchIndex = currentStart
        while (searchIndex < selEnd) {
            val nextNewline = bufferText.indexOf('\n', searchIndex)
            if (nextNewline != -1 && nextNewline < selEnd) {
                if (nextNewline == selEnd - 1 && selStart != selEnd) break
                lineStarts.add(nextNewline + 1)
                searchIndex = nextNewline + 1
            } else {
                break
            }
        }

        val firstLineEnd = bufferText.indexOf('\n', lineStarts.first()).let { if (it == -1) buffer.length else it }
        val firstLineText = bufferText.substring(lineStarts.first(), firstLineEnd)

        val isRemoving = when (style) {
            is MarkupStyle.CheckboxUnchecked, is MarkupStyle.CheckboxChecked ->
                MarkdownConstants.CHECKBOX_UNCHECKED_REGEX.containsMatchIn(firstLineText) ||
                        MarkdownConstants.CHECKBOX_CHECKED_REGEX.containsMatchIn(firstLineText)
            is MarkupStyle.OrderedList -> MarkdownConstants.ORDERED_LIST_REGEX.containsMatchIn(firstLineText)
            is MarkupStyle.BulletList -> MarkdownConstants.BULLET_LIST_REGEX.containsMatchIn(firstLineText)
            is MarkupStyle.Blockquote -> MarkdownConstants.BLOCKQUOTE_REGEX.containsMatchIn(firstLineText)
            else -> false
        }

        var currentSpans = spans.toList()

        for (i in lineStarts.indices.reversed()) {
            val lineStart = lineStarts[i]
            val lineEnd = bufferText.indexOf('\n', lineStart).let { if (it == -1) buffer.length else it }
            val lineText = bufferText.substring(lineStart, lineEnd)

            val leadingIndentLen = lineText.takeWhile { it == ' ' || it == '\t' }.length
            val unindentedText = lineText.substring(leadingIndentLen)

            var existingPrefixLen = 0
            if (MarkdownConstants.ORDERED_LIST_REGEX.containsMatchIn(lineText)) {
                existingPrefixLen = unindentedText.indexOf('.') + 2
            } else if (MarkdownConstants.CHECKBOX_UNCHECKED_REGEX.containsMatchIn(lineText) || MarkdownConstants.CHECKBOX_CHECKED_REGEX.containsMatchIn(lineText)) {
                existingPrefixLen = 6
            } else if (MarkdownConstants.BULLET_LIST_REGEX.containsMatchIn(lineText)) {
                existingPrefixLen = 2
            } else if (MarkdownConstants.BLOCKQUOTE_REGEX.containsMatchIn(lineText)) {
                existingPrefixLen = 2
            }

            val prefixStart = lineStart + leadingIndentLen

            if (isRemoving) {
                val matchTarget = when (style) {
                    is MarkupStyle.CheckboxUnchecked, is MarkupStyle.CheckboxChecked ->
                        MarkdownConstants.CHECKBOX_UNCHECKED_REGEX.containsMatchIn(lineText) ||
                                MarkdownConstants.CHECKBOX_CHECKED_REGEX.containsMatchIn(lineText)
                    is MarkupStyle.OrderedList -> MarkdownConstants.ORDERED_LIST_REGEX.containsMatchIn(lineText)
                    is MarkupStyle.BulletList -> MarkdownConstants.BULLET_LIST_REGEX.containsMatchIn(lineText)
                    is MarkupStyle.Blockquote -> MarkdownConstants.BLOCKQUOTE_REGEX.containsMatchIn(lineText)
                    else -> false
                }

                if (matchTarget && existingPrefixLen > 0) {
                    buffer.replace(prefixStart, prefixStart + existingPrefixLen, "")
                    currentSpans = SpanManager.shiftSpans(currentSpans, prefixStart, -existingPrefixLen)
                }
            } else {
                val actualPrefix = if (style is MarkupStyle.OrderedList) "1. " else prefix

                if (existingPrefixLen > 0) {
                    val existingPrefix = unindentedText.take(existingPrefixLen)
                    if (existingPrefix != actualPrefix) {
                        buffer.replace(prefixStart, prefixStart + existingPrefixLen, actualPrefix)
                        val diff = actualPrefix.length - existingPrefixLen
                        currentSpans = SpanManager.shiftSpans(currentSpans, prefixStart, diff, push = true)
                    }
                } else {
                    buffer.insert(prefixStart, actualPrefix)
                    currentSpans = SpanManager.shiftSpans(currentSpans, prefixStart, actualPrefix.length, push = true)
                }
            }
        }

        val listCounters = mutableMapOf<String, Int>()
        var currentLineStart = 0
        while (currentLineStart < buffer.length) {
            val bufferStr = buffer.asCharSequence()
            val nextNewline = bufferStr.indexOf('\n', currentLineStart)
            val lineEnd = if (nextNewline == -1) buffer.length else nextNewline
            val lineText = bufferStr.substring(currentLineStart, lineEnd)

            if (MarkdownConstants.ORDERED_LIST_REGEX.containsMatchIn(lineText)) {
                val indent = lineText.takeWhile { it == ' ' || it == '\t' }
                val unindented = lineText.substring(indent.length)
                val oldPrefixLen = unindented.indexOf('.') + 2
                val oldPrefix = unindented.take(oldPrefixLen)

                val listCounter = (listCounters[indent] ?: 0) + 1
                listCounters[indent] = listCounter

                val newPrefix = "$listCounter. "
                val prefixStart = currentLineStart + indent.length
                if (oldPrefix != newPrefix) {
                    buffer.replace(prefixStart, prefixStart + oldPrefixLen, newPrefix)
                    val diff = newPrefix.length - oldPrefixLen
                    currentSpans = SpanManager.shiftSpans(currentSpans, prefixStart + oldPrefixLen, diff)
                    currentLineStart = lineEnd + diff + 1
                } else {
                    currentLineStart = lineEnd + 1
                }
            } else {
                listCounters.clear()
                currentLineStart = lineEnd + 1
            }
        }
        return currentSpans
    }

    fun toggleCheckbox(
        buffer: TextFieldBuffer,
        spans: List<MarkupStyleRange>,
        offset: Int,
        strictPrefixCheck: Boolean
    ): Pair<Boolean, List<MarkupStyleRange>> {
        val bufferText = buffer.asCharSequence()
        if (offset < 0 || offset > bufferText.length) return false to spans

        val lastNewline = bufferText.lastIndexOf('\n', (offset - 1).coerceAtLeast(0))
        val lineStart = if (lastNewline == -1) 0 else lastNewline + 1
        val lineEnd = bufferText.indexOf('\n', lineStart).let { if (it == -1) buffer.length else it }
        val lineText = bufferText.substring(lineStart, lineEnd)

        val indentLen = lineText.takeWhile { it == ' ' || it == '\t' }.length
        val prefixStart = lineStart + indentLen
        val isInPrefix = offset >= lineStart && offset <= prefixStart + 6

        if (!strictPrefixCheck || isInPrefix) {
            if (MarkdownConstants.CHECKBOX_UNCHECKED_REGEX.containsMatchIn(lineText)) {
                buffer.replace(prefixStart, prefixStart + 6, "- [x] ")
                return true to spans
            }
            if (MarkdownConstants.CHECKBOX_CHECKED_REGEX.containsMatchIn(lineText)) {
                buffer.replace(prefixStart, prefixStart + 6, "- [ ] ")
                return true to spans
            }
        }
        return false to spans
    }

    fun handleIndent(state: HyphenTextState, buffer: TextFieldBuffer, isShift: Boolean, indentSpaces: Int = 2): Boolean {
        val bufferText = buffer.asCharSequence()
        val selStart = minOf(buffer.selection.start, buffer.selection.end)
        val selEnd = maxOf(buffer.selection.start, buffer.selection.end)

        val lineStarts = mutableListOf<Int>()
        var currentStart = bufferText.lastIndexOf('\n', (selStart - 1).coerceAtLeast(0)) + 1
        if (currentStart == -1) currentStart = 0
        lineStarts.add(currentStart)

        var searchIndex = currentStart
        while (searchIndex < selEnd) {
            val nextNewline = bufferText.indexOf('\n', searchIndex)
            if (nextNewline != -1 && nextNewline < selEnd) {
                if (nextNewline == selEnd - 1 && selStart != selEnd) break
                lineStarts.add(nextNewline + 1)
                searchIndex = nextNewline + 1
            } else break
        }

        val indentString = " ".repeat(indentSpaces.coerceAtLeast(1))

        var modified = false
        var currentSpans = state.spans
        for (start in lineStarts.reversed()) {
            val lineEnd = bufferText.indexOf('\n', start).let { if (it == -1) buffer.length else it }
            val lineText = bufferText.substring(start, lineEnd)
            val indentLen = lineText.takeWhile { it == ' ' || it == '\t' }.length

            if (!isShift) {
                buffer.insert(start, indentString)
                currentSpans = SpanManager.shiftSpans(currentSpans, start, indentString.length, push = true)
                modified = true
            } else {
                if (indentLen > 0) {
                    val removeCount = minOf(indentSpaces.coerceAtLeast(1), indentLen)
                    buffer.replace(start, start + removeCount, "")
                    currentSpans = SpanManager.shiftSpans(currentSpans, start, -removeCount)
                    modified = true
                }
            }
        }
        if (modified) {
            state._spans.clear()
            state._spans.addAll(currentSpans)
        }
        return modified
    }
}