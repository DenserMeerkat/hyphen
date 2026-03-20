package com.denser.hyphen.core.state

import com.denser.hyphen.core.model.MarkupStyle
import com.denser.hyphen.core.model.StyleRange
import com.denser.hyphen.core.model.StyleSets

object SpanManager {

    fun resolveChangeOrigin(cursorPosition: Int, lengthDifference: Int, textLength: Int): Int {
        val origin = if (lengthDifference > 0) cursorPosition - lengthDifference else cursorPosition
        return origin.coerceIn(0, textLength)
    }

    fun shiftSpans(
        currentSpans: List<StyleRange>,
        changeStart: Int,
        lengthDifference: Int,
    ): List<StyleRange> {
        if (lengthDifference == 0) return currentSpans
        return currentSpans.mapNotNull { span ->
            when {
                changeStart >= span.end -> span
                changeStart < span.start -> {
                    val newStart = (span.start + lengthDifference).coerceAtLeast(0)
                    val newEnd = (span.end + lengthDifference).coerceAtLeast(newStart)
                    if (newStart == newEnd) null else span.copy(start = newStart, end = newEnd)
                }
                else -> {
                    val newEnd = (span.end + lengthDifference).coerceAtLeast(span.start)
                    if (span.start == newEnd) null else span.copy(end = newEnd)
                }
            }
        }
    }

    fun mergeSpans(
        existing: List<StyleRange>,
        incoming: List<StyleRange>,
    ): List<StyleRange> {
        val incomingKeys = incoming
            .map { Triple(it.style::class, it.start, it.end) }
            .toHashSet()
        val kept = existing.filter {
            Triple(it.style::class, it.start, it.end) !in incomingKeys
        }
        return kept + incoming
    }

    fun consolidateSpans(spans: List<StyleRange>): List<StyleRange> {
        val result = mutableListOf<StyleRange>()
        spans.groupBy { it.style }.forEach { (style, styleSpans) ->
            val sorted = styleSpans.filter { it.start < it.end }.sortedBy { it.start }
            if (sorted.isEmpty()) return@forEach
            var currentStart = sorted[0].start
            var currentEnd = sorted[0].end
            for (i in 1 until sorted.size) {
                val span = sorted[i]
                if (span.start <= currentEnd) {
                    currentEnd = maxOf(currentEnd, span.end)
                } else {
                    result.add(StyleRange(style, currentStart, currentEnd))
                    currentStart = span.start
                    currentEnd = span.end
                }
            }
            result.add(StyleRange(style, currentStart, currentEnd))
        }
        return result
    }

    fun toggleStyle(
        currentSpans: List<StyleRange>,
        style: MarkupStyle,
        start: Int,
        end: Int,
    ): List<StyleRange> {
        val fullyEncloses = currentSpans.any {
            it.style == style && it.start <= start && it.end >= end
        }
        val newSpans = currentSpans.toMutableList()
        if (fullyEncloses) {
            val overlaps = newSpans.filter {
                it.style == style && it.start < end && it.end > start
            }
            newSpans.removeAll(overlaps)
            overlaps.forEach { span ->
                if (span.start < start) newSpans.add(StyleRange(style, span.start, start))
                if (span.end > end) newSpans.add(StyleRange(style, end, span.end))
            }
        } else {
            newSpans.add(StyleRange(style, start, end))
        }
        return consolidateSpans(newSpans)
    }

    fun applyTypingOverrides(
        currentSpans: List<StyleRange>,
        activeStyles: List<MarkupStyle>,
        changeOrigin: Int,
        insertEnd: Int,
    ): List<StyleRange> {
        val newSpans = currentSpans.toMutableList()

        activeStyles.forEach { style ->
            newSpans.add(StyleRange(style, changeOrigin, insertEnd))
        }

        val inactiveStyles = StyleSets.allInline.filter { it !in activeStyles }
        inactiveStyles.forEach { style ->
            val overlaps = newSpans.filter {
                it.style == style && it.start < insertEnd && it.end > changeOrigin
            }
            newSpans.removeAll(overlaps)
            overlaps.forEach { span ->
                if (span.start < changeOrigin)
                    newSpans.add(StyleRange(style, span.start, changeOrigin))
                if (span.end > insertEnd)
                    newSpans.add(StyleRange(style, insertEnd, span.end))
            }
        }
        return newSpans
    }
}