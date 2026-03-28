package com.denser.hyphen.state

import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.insert
import androidx.compose.ui.text.TextRange
import com.denser.hyphen.core.constants.MarkdownConstants
import com.denser.hyphen.core.model.MarkupStyle
import com.denser.hyphen.core.model.StyleRange
import com.denser.hyphen.core.model.StyleSets
import com.denser.hyphen.core.state.SpanManager

internal object BlockStyleManager {

    fun isBlockStyle(style: MarkupStyle): Boolean {
        return style in StyleSets.allBlock
    }

    fun hasBlockStyle(text: String, selection: TextRange, style: MarkupStyle): Boolean {
        val cursor = minOf(selection.start, selection.end)
        val lastNewline = text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0))
        val lineStart = if (lastNewline == -1) 0 else lastNewline + 1

        val lineEnd = text.indexOf('\n', lineStart).let { if (it == -1) text.length else it }
        val lineText = text.substring(lineStart, lineEnd)

        return when (style) {
            is MarkupStyle.CheckboxUnchecked -> MarkdownConstants.Regex.Block.CHECKBOX_UNCHECKED.containsMatchIn(lineText)
            is MarkupStyle.CheckboxChecked -> MarkdownConstants.Regex.Block.CHECKBOX_CHECKED.containsMatchIn(lineText)
            is MarkupStyle.BulletList -> MarkdownConstants.Regex.Block.BULLET_LIST.containsMatchIn(lineText) &&
                    !MarkdownConstants.Regex.Block.CHECKBOX_UNCHECKED.containsMatchIn(lineText) &&
                    !MarkdownConstants.Regex.Block.CHECKBOX_CHECKED.containsMatchIn(lineText)
            is MarkupStyle.OrderedList -> MarkdownConstants.Regex.Block.ORDERED_LIST.containsMatchIn(lineText)
            is MarkupStyle.Blockquote -> MarkdownConstants.Regex.Block.BLOCKQUOTE.containsMatchIn(lineText)
            else -> false
        }
    }

    fun handleSmartEnter(state: HyphenTextState, buffer: TextFieldBuffer): Boolean {
        val bufferText = buffer.asCharSequence()
        val cursor = buffer.selection.start
        if (cursor <= 0) return false

        val lastNewline = bufferText.lastIndexOf('\n', cursor - 1)
        val lineStart = if (lastNewline == -1) 0 else lastNewline + 1
        val lineText = bufferText.substring(lineStart, cursor)

        return when {
            (state.isStyleAt(lineStart, MarkupStyle.CheckboxUnchecked) && MarkdownConstants.Regex.Block.CHECKBOX_UNCHECKED.containsMatchIn(lineText)) ||
                    (state.isStyleAt(lineStart, MarkupStyle.CheckboxChecked) && MarkdownConstants.Regex.Block.CHECKBOX_CHECKED.containsMatchIn(lineText)) -> {

                val prefix = MarkdownConstants.Triggers.CHECKBOX_UNCHECKED_HYPHEN
                val currentPrefixLen = prefix.length

                if (lineText.length == currentPrefixLen) {
                    buffer.replace(lineStart, cursor, "")
                } else {
                    buffer.insert(cursor, "\n$prefix")
                }
                true
            }

            state.isStyleAt(lineStart, MarkupStyle.BulletList) && MarkdownConstants.Regex.Block.BULLET_LIST.containsMatchIn(lineText) -> {
                val prefix = lineText.take(2)
                if (lineText == prefix) buffer.replace(lineStart, cursor, "")
                else buffer.insert(cursor, "\n$prefix")
                true
            }

            state.isStyleAt(lineStart, MarkupStyle.OrderedList) && MarkdownConstants.Regex.Block.ORDERED_LIST.containsMatchIn(lineText) -> {
                val prefixLen = lineText.indexOf('.') + 2
                val prefix = lineText.take(prefixLen)
                val currentNumber = prefix.dropLast(2).toIntOrNull() ?: 1
                if (lineText == prefix) buffer.replace(lineStart, cursor, "")
                else buffer.insert(cursor, "\n${currentNumber + 1}. ")
                true
            }

            state.isStyleAt(lineStart, MarkupStyle.Blockquote) && MarkdownConstants.Regex.Block.BLOCKQUOTE.containsMatchIn(lineText) -> {
                val prefix = lineText.take(2)
                if (lineText == prefix) buffer.replace(lineStart, cursor, "")
                else buffer.insert(cursor, "\n$prefix")
                true
            }

            else -> false
        }
    }

    fun applyBlockStyle(
        buffer: TextFieldBuffer,
        spans: List<StyleRange>,
        selection: TextRange,
        style: MarkupStyle
    ): List<StyleRange> {
        val prefix = when (style) {
            is MarkupStyle.BulletList -> MarkdownConstants.Triggers.BULLET_HYPHEN
            is MarkupStyle.OrderedList -> "1. "
            is MarkupStyle.Blockquote -> MarkdownConstants.Triggers.BLOCKQUOTE
            is MarkupStyle.CheckboxUnchecked -> MarkdownConstants.Triggers.CHECKBOX_UNCHECKED_HYPHEN
            is MarkupStyle.CheckboxChecked -> MarkdownConstants.Triggers.CHECKBOX_CHECKED_HYPHEN_L
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
                MarkdownConstants.Regex.Block.CHECKBOX_UNCHECKED.containsMatchIn(firstLineText) ||
                        MarkdownConstants.Regex.Block.CHECKBOX_CHECKED.containsMatchIn(firstLineText)
            is MarkupStyle.OrderedList -> MarkdownConstants.Regex.Block.ORDERED_LIST.containsMatchIn(firstLineText)
            is MarkupStyle.BulletList -> MarkdownConstants.Regex.Block.BULLET_LIST.containsMatchIn(firstLineText)
            is MarkupStyle.Blockquote -> MarkdownConstants.Regex.Block.BLOCKQUOTE.containsMatchIn(firstLineText)
            else -> false
        }

        var currentSpans = spans.toList()

        for (i in lineStarts.indices.reversed()) {
            val lineStart = lineStarts[i]
            val lineEnd = bufferText.indexOf('\n', lineStart).let { if (it == -1) buffer.length else it }
            val lineText = bufferText.substring(lineStart, lineEnd)

            var existingPrefixLen = 0
            if (MarkdownConstants.Regex.Block.ORDERED_LIST.containsMatchIn(lineText)) {
                existingPrefixLen = lineText.indexOf('.') + 2
            } else if (MarkdownConstants.Regex.Block.CHECKBOX_UNCHECKED.containsMatchIn(lineText) || MarkdownConstants.Regex.Block.CHECKBOX_CHECKED.containsMatchIn(lineText)) {
                existingPrefixLen = MarkdownConstants.Triggers.CHECKBOX_UNCHECKED_HYPHEN.length
            } else if (MarkdownConstants.Regex.Block.BULLET_LIST.containsMatchIn(lineText)) {
                existingPrefixLen = MarkdownConstants.Triggers.BULLET_HYPHEN.length
            } else if (MarkdownConstants.Regex.Block.BLOCKQUOTE.containsMatchIn(lineText)) {
                existingPrefixLen = MarkdownConstants.Triggers.BLOCKQUOTE.length
            }

            if (isRemoving) {
                val matchTarget = when (style) {
                    is MarkupStyle.CheckboxUnchecked, is MarkupStyle.CheckboxChecked ->
                        MarkdownConstants.Regex.Block.CHECKBOX_UNCHECKED.containsMatchIn(lineText) ||
                                MarkdownConstants.Regex.Block.CHECKBOX_CHECKED.containsMatchIn(lineText)
                    is MarkupStyle.OrderedList -> MarkdownConstants.Regex.Block.ORDERED_LIST.containsMatchIn(lineText)
                    is MarkupStyle.BulletList -> MarkdownConstants.Regex.Block.BULLET_LIST.containsMatchIn(lineText)
                    is MarkupStyle.Blockquote -> MarkdownConstants.Regex.Block.BLOCKQUOTE.containsMatchIn(lineText)
                    else -> false
                }

                if (matchTarget && existingPrefixLen > 0) {
                    buffer.replace(lineStart, lineStart + existingPrefixLen, "")
                    currentSpans = SpanManager.shiftSpans(currentSpans, lineStart, -existingPrefixLen)
                }
            } else {
                val actualPrefix = if (style is MarkupStyle.OrderedList) "1. " else prefix

                if (existingPrefixLen > 0) {
                    val existingPrefix = lineText.take(existingPrefixLen)
                    if (existingPrefix != actualPrefix) {
                        buffer.replace(lineStart, lineStart + existingPrefixLen, actualPrefix)
                        val diff = actualPrefix.length - existingPrefixLen
                        currentSpans = SpanManager.shiftSpans(currentSpans, lineStart, diff)
                    }
                } else {
                    buffer.insert(lineStart, actualPrefix)
                    currentSpans = SpanManager.shiftSpans(currentSpans, lineStart, actualPrefix.length)
                }
            }
        }

        var listCounter = 1
        var currentLineStart = 0
        while (currentLineStart < buffer.length) {
            val bufferStr = buffer.asCharSequence()
            val nextNewline = bufferStr.indexOf('\n', currentLineStart)
            val lineEnd = if (nextNewline == -1) buffer.length else nextNewline
            val lineText = bufferStr.substring(currentLineStart, lineEnd)

            if (MarkdownConstants.Regex.Block.ORDERED_LIST.containsMatchIn(lineText)) {
                val oldPrefixLen = lineText.indexOf('.') + 2
                val oldPrefix = lineText.take(oldPrefixLen)
                val newPrefix = "$listCounter. "
                if (oldPrefix != newPrefix) {
                    buffer.replace(currentLineStart, currentLineStart + oldPrefixLen, newPrefix)
                    val diff = newPrefix.length - oldPrefixLen
                    currentSpans = SpanManager.shiftSpans(currentSpans, currentLineStart + oldPrefixLen, diff)
                    currentLineStart = lineEnd + diff + 1
                } else {
                    currentLineStart = lineEnd + 1
                }
                listCounter++
            } else {
                listCounter = 1
                currentLineStart = lineEnd + 1
            }
        }
        return currentSpans
    }

    fun toggleCheckbox(buffer: TextFieldBuffer, offset: Int, strictPrefixCheck: Boolean): Boolean {
        val bufferText = buffer.asCharSequence()
        if (offset < 0 || offset > bufferText.length) return false

        val lastNewline = bufferText.lastIndexOf('\n', (offset - 1).coerceAtLeast(0))
        val lineStart = if (lastNewline == -1) 0 else lastNewline + 1
        val lineEnd = bufferText.indexOf('\n', lineStart).let { if (it == -1) buffer.length else it }
        val lineText = bufferText.substring(lineStart, lineEnd)

        val isInPrefix = offset >= lineStart && offset <= lineStart + 6

        if (!strictPrefixCheck || isInPrefix) {
            if (MarkdownConstants.Regex.Block.CHECKBOX_UNCHECKED.containsMatchIn(lineText)) {
                buffer.replace(lineStart, lineStart + MarkdownConstants.Triggers.CHECKBOX_CHECKED_HYPHEN_L.length, MarkdownConstants.Triggers.CHECKBOX_CHECKED_HYPHEN_L)
                return true
            }
            if (MarkdownConstants.Regex.Block.CHECKBOX_CHECKED.containsMatchIn(lineText)) {
                buffer.replace(lineStart, lineStart + MarkdownConstants.Triggers.CHECKBOX_UNCHECKED_HYPHEN.length, MarkdownConstants.Triggers.CHECKBOX_UNCHECKED_HYPHEN)
                return true
            }
        }
        return false
    }
}