package com.denser.hyphen.state

import com.denser.hyphen.model.MarkupStyleRange
import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.model.StyleSets

internal object SpanManager {

    fun shiftSpans(
        currentSpans: List<MarkupStyleRange>,
        changeStart: Int,
        lengthDifference: Int,
        push: Boolean = false
    ): List<MarkupStyleRange> {
        if (lengthDifference == 0) return currentSpans

        return currentSpans.mapNotNull { span ->
            when {
                changeStart > span.end || (!push && changeStart == span.end) -> span
                changeStart < span.start || (push && changeStart == span.start) -> {
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
        existing: List<MarkupStyleRange>,
        markdown: List<MarkupStyleRange>
    ): List<MarkupStyleRange> {
        val inlineMarkdownKeys = markdown
            .filter { !BlockStyleManager.isBlockStyle(it.style) }
            .map { Triple(it.style::class, it.start, it.end) }
            .toHashSet()

        val keptExisting = existing.filter { span ->
            if (BlockStyleManager.isBlockStyle(span.style)) return@filter false
            Triple(span.style::class, span.start, span.end) !in inlineMarkdownKeys
        }
        return keptExisting + markdown
    }

    fun consolidateSpans(spans: List<MarkupStyleRange>): List<MarkupStyleRange> {
        val consolidated = mutableListOf<MarkupStyleRange>()
        consolidated.addAll(spans.filter { BlockStyleManager.isBlockStyle(it.style) })

        val inlineSpans = spans.filter { !BlockStyleManager.isBlockStyle(it.style) }

        inlineSpans.groupBy { it.style }.forEach { (style, styleSpans) ->
            val sorted = styleSpans.filter { it.start < it.end }.sortedBy { it.start }
            if (sorted.isEmpty()) return@forEach

            var currentStart = sorted[0].start
            var currentEnd = sorted[0].end

            for (i in 1 until sorted.size) {
                val span = sorted[i]
                if (span.start <= currentEnd) {
                    currentEnd = maxOf(currentEnd, span.end)
                } else {
                    consolidated.add(MarkupStyleRange(style, currentStart, currentEnd))
                    currentStart = span.start
                    currentEnd = span.end
                }
            }
            consolidated.add(MarkupStyleRange(style, currentStart, currentEnd))
        }
        return consolidated
    }

    fun toggleStyle(
        currentSpans: List<MarkupStyleRange>,
        style: MarkupStyle,
        start: Int,
        end: Int
    ): List<MarkupStyleRange> {
        val fullyEncloses = currentSpans.any { it.style == style && it.start <= start && it.end >= end }
        val newSpans = currentSpans.toMutableList()

        if (fullyEncloses) {
            val overlaps = newSpans.filter { it.style == style && it.start < end && it.end > start }
            newSpans.removeAll(overlaps)

            overlaps.forEach { span ->
                if (span.start < start) newSpans.add(MarkupStyleRange(style, span.start, start))
                if (span.end > end) newSpans.add(MarkupStyleRange(style, end, span.end))
            }
        } else {
            newSpans.add(MarkupStyleRange(style, start, end))
        }
        return consolidateSpans(newSpans)
    }

    fun applyTypingOverrides(
        currentSpans: List<MarkupStyleRange>,
        activeStyles: List<MarkupStyle>,
        changeOrigin: Int,
        insertEnd: Int
    ): List<MarkupStyleRange> {
        val newSpans = currentSpans.toMutableList()

        activeStyles.forEach { style ->
            newSpans.add(MarkupStyleRange(style, changeOrigin, insertEnd))
        }

        val inactiveStyles = StyleSets.allInline.filter { it !in activeStyles }

        inactiveStyles.forEach { style ->
            val overlaps = newSpans.filter { it.style == style && it.start < insertEnd && it.end > changeOrigin }
            newSpans.removeAll(overlaps)
            overlaps.forEach { span ->
                if (span.start < changeOrigin) newSpans.add(MarkupStyleRange(style, span.start, changeOrigin))
                if (span.end > insertEnd) newSpans.add(MarkupStyleRange(style, insertEnd, span.end))
            }
        }
        return newSpans
    }

    fun resolveChangeOrigin(cursorPosition: Int, lengthDifference: Int, textLength: Int): Int {
        val origin = if (lengthDifference > 0) cursorPosition - lengthDifference else cursorPosition
        return origin.coerceIn(0, textLength)
    }
}