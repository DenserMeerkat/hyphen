package com.denser.hyphen.state

import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.insert
import androidx.compose.ui.text.TextRange
import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.model.MarkupStyleRange
import com.denser.hyphen.model.StyleSets

internal object BlockStyleManager {

    fun isBlockStyle(style: MarkupStyle): Boolean {
        return style in StyleSets.allBlock
    }

    fun hasBlockStyle(text: String, selection: TextRange, style: MarkupStyle): Boolean {
        val cursor = minOf(selection.start, selection.end)
        val lastNewline = text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0))
        val lineStart = if (lastNewline == -1) 0 else lastNewline + 1
        val lineText = text.substring(lineStart)

        return when (style) {
            is MarkupStyle.BulletList -> lineText.startsWith("- ")
            is MarkupStyle.OrderedList -> Regex("^\\d+\\.\\s").find(lineText) != null
            is MarkupStyle.Blockquote -> lineText.startsWith("> ")
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
            state.isStyleAt(lineStart, MarkupStyle.BulletList) -> {
                val prefixChar = lineText.firstOrNull() ?: '-'
                val emptyPrefix = "$prefixChar "

                if (lineText == emptyPrefix) buffer.replace(lineStart, cursor, "")
                else buffer.insert(cursor, "\n$emptyPrefix")
                true
            }

            state.isStyleAt(lineStart, MarkupStyle.OrderedList) -> {
                val currentNumber =
                    Regex("^(\\d+)\\. ").find(lineText)?.groupValues?.get(1)?.toIntOrNull() ?: 1
                if (lineText.matches(Regex("^\\d+\\. $"))) buffer.replace(lineStart, cursor, "")
                else buffer.insert(cursor, "\n${currentNumber + 1}. ")
                true
            }

            state.isStyleAt(lineStart, MarkupStyle.Blockquote) -> {
                val prefixChar = lineText.firstOrNull() ?: '>'
                val emptyPrefix = "$prefixChar "

                if (lineText == emptyPrefix) buffer.replace(lineStart, cursor, "")
                else buffer.insert(cursor, "\n$emptyPrefix")
                true
            }

            else -> false
        }
    }

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

        val isOrderedList = prefix.trim().lastOrNull() == '.'
        val firstLineText = bufferText.substring(lineStarts.first())
        val isRemoving = if (isOrderedList) {
            Regex("^\\d+\\.\\s").find(firstLineText) != null
        } else {
            firstLineText.startsWith(prefix)
        }

        var currentSpans = spans.toList()

        for (i in lineStarts.indices.reversed()) {
            val lineStart = lineStarts[i]
            val lineText = buffer.asCharSequence().substring(lineStart, buffer.length)

            if (isRemoving) {
                if (isOrderedList) {
                    val match = Regex("^(\\d+\\.\\s)").find(lineText)
                    if (match != null) {
                        val matchedPrefix = match.value
                        buffer.replace(lineStart, lineStart + matchedPrefix.length, "")
                        currentSpans = SpanManager.shiftSpans(currentSpans, lineStart, -matchedPrefix.length)
                    }
                } else if (lineText.startsWith(prefix)) {
                    buffer.replace(lineStart, lineStart + prefix.length, "")
                    currentSpans = SpanManager.shiftSpans(currentSpans, lineStart, -prefix.length)
                }
            } else {
                val actualPrefix = if (isOrderedList) "1. " else prefix

                if (isOrderedList) {
                    val match = Regex("^(\\d+\\.\\s)").find(lineText)
                    if (match != null) {
                        val matchedPrefix = match.value
                        buffer.replace(lineStart, lineStart + matchedPrefix.length, actualPrefix)
                        val diff = actualPrefix.length - matchedPrefix.length
                        currentSpans = SpanManager.shiftSpans(currentSpans, lineStart, diff)
                    } else {
                        val bulletMatch = Regex("^(- )").find(lineText)
                        if (bulletMatch != null) {
                            buffer.replace(lineStart, lineStart + bulletMatch.value.length, actualPrefix)
                            val diff = actualPrefix.length - bulletMatch.value.length
                            currentSpans = SpanManager.shiftSpans(currentSpans, lineStart, diff)
                        } else {
                            buffer.insert(lineStart, actualPrefix)
                            currentSpans = SpanManager.shiftSpans(currentSpans, lineStart, actualPrefix.length)
                        }
                    }
                } else {
                    if (!lineText.startsWith(prefix)) {
                        val orderedMatch = Regex("^(\\d+\\.\\s)").find(lineText)
                        if (orderedMatch != null) {
                            buffer.replace(lineStart, lineStart + orderedMatch.value.length, prefix)
                            val diff = prefix.length - orderedMatch.value.length
                            currentSpans = SpanManager.shiftSpans(currentSpans, lineStart, diff)
                        } else {
                            buffer.insert(lineStart, prefix)
                            currentSpans = SpanManager.shiftSpans(currentSpans, lineStart, prefix.length)
                        }
                    }
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

            val match = Regex("^(\\d+)\\.\\s").find(lineText)
            if (match != null) {
                val oldPrefix = match.value
                val newPrefix = "$listCounter. "
                if (oldPrefix != newPrefix) {
                    buffer.replace(currentLineStart, currentLineStart + oldPrefix.length, newPrefix)
                    val diff = newPrefix.length - oldPrefix.length
                    currentSpans = SpanManager.shiftSpans(currentSpans, currentLineStart + oldPrefix.length, diff)
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
}