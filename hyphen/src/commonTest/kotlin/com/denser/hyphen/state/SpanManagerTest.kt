package com.denser.hyphen.state

import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.model.MarkupStyleRange
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.Test

class SpanManagerTest {


    // --- shiftSpans Tests ---
    @Test
    fun `shiftSpans shifts span to the right when text is inserted before it`() {
        val spans = listOf(MarkupStyleRange(MarkupStyle.Bold, start = 5, end = 10))
        val result = SpanManager.shiftSpans(spans, changeStart = 2, lengthDifference = 3)

        assertEquals(1, result.size)
        assertEquals(8, result[0].start)
        assertEquals(13, result[0].end)
    }

    @Test
    fun `shiftSpans expands span when text is inserted inside it`() {
        val spans = listOf(MarkupStyleRange(MarkupStyle.Bold, start = 5, end = 10))
        val result = SpanManager.shiftSpans(spans, changeStart = 7, lengthDifference = 3)

        assertEquals(1, result.size)
        assertEquals(5, result[0].start)
        assertEquals(13, result[0].end)
    }

    @Test
    fun `shiftSpans deletes span if it is completely collapsed by deletion`() {
        val spans = listOf(MarkupStyleRange(MarkupStyle.Bold, start = 5, end = 10))
        val result = SpanManager.shiftSpans(spans, changeStart = 2, lengthDifference = -10)

        assertTrue(result.isEmpty())
    }

    // --- mergeSpans Tests ---
    @Test
    fun `mergeSpans clears old block styles and applies new markdown block styles`() {
        val existing = listOf(MarkupStyleRange(MarkupStyle.BulletList, 0, 10))
        val markdown = listOf(MarkupStyleRange(MarkupStyle.OrderedList, 0, 10))

        val result = SpanManager.mergeSpans(existing, markdown)

        assertEquals(1, result.size)
        assertEquals(MarkupStyle.OrderedList, result[0].style)
    }

    @Test
    fun `mergeSpans preserves existing inline styles if they do not perfectly match markdown`() {
        val existing = listOf(MarkupStyleRange(MarkupStyle.Bold, 0, 5))
        val markdown = listOf(MarkupStyleRange(MarkupStyle.Italic, 6, 10))

        val result = SpanManager.mergeSpans(existing, markdown)

        assertEquals(2, result.size)
        assertTrue(result.any { it.style == MarkupStyle.Bold })
        assertTrue(result.any { it.style == MarkupStyle.Italic })
    }


    // --- consolidateSpans Tests ---
    @Test
    fun `consolidateSpans merges overlapping inline spans of the same style`() {
        val spans = listOf(
            MarkupStyleRange(MarkupStyle.Bold, 0, 5),
            MarkupStyleRange(MarkupStyle.Bold, 3, 8)
        )
        val result = SpanManager.consolidateSpans(spans)

        assertEquals(1, result.size)
        assertEquals(0, result[0].start)
        assertEquals(8, result[0].end)
    }

    @Test
    fun `consolidateSpans does not merge different inline styles`() {
        val spans = listOf(
            MarkupStyleRange(MarkupStyle.Bold, 0, 5),
            MarkupStyleRange(MarkupStyle.Italic, 3, 8)
        )
        val result = SpanManager.consolidateSpans(spans)

        assertEquals(2, result.size)
    }

    @Test
    fun `consolidateSpans passes block styles through without modification`() {
        val spans = listOf(
            MarkupStyleRange(MarkupStyle.BulletList, 0, 5),
            MarkupStyleRange(MarkupStyle.BulletList, 3, 8)
        )
        val result = SpanManager.consolidateSpans(spans)

        // Block styles shouldn't be mathematically merged by the inline logic
        assertEquals(2, result.size)
    }


    // --- toggleStyle Tests ---
    @Test
    fun `toggleStyle splits an existing span if toggled inside it`() {
        val spans = listOf(MarkupStyleRange(MarkupStyle.Bold, 0, 10))
        val result = SpanManager.toggleStyle(spans, MarkupStyle.Bold, start = 4, end = 6)

        assertEquals(2, result.size)

        val first = result.find { it.start == 0 }
        val second = result.find { it.start == 6 }

        assertEquals(4, first?.end)
        assertEquals(10, second?.end)
    }

    @Test
    fun `toggleStyle adds a new span if the style does not fully enclose the range`() {
        val spans = listOf(MarkupStyleRange(MarkupStyle.Bold, 0, 4))
        val result = SpanManager.toggleStyle(spans, MarkupStyle.Bold, start = 8, end = 12)

        assertEquals(2, result.size)
        assertTrue(result.any { it.start == 8 && it.end == 12 })
    }


    // --- applyTypingOverrides Tests ---
    @Test
    fun `applyTypingOverrides removes inactive styles and adds active styles in typed range`() {
        val spans = listOf(MarkupStyleRange(MarkupStyle.Bold, 0, 10))

        // User typed from 4 to 6. Bold is inactive. Italic is active.
        val result = SpanManager.applyTypingOverrides(
            currentSpans = spans,
            activeStyles = listOf(MarkupStyle.Italic),
            changeOrigin = 4,
            insertEnd = 6
        )

        // Bold should be split (0..4, 6..10), Italic should be added (4..6)
        assertEquals(3, result.size)
        assertTrue(result.any { it.style == MarkupStyle.Bold && it.start == 0 && it.end == 4 })
        assertTrue(result.any { it.style == MarkupStyle.Bold && it.start == 6 && it.end == 10 })
        assertTrue(result.any { it.style == MarkupStyle.Italic && it.start == 4 && it.end == 6 })
    }


    // --- resolveChangeOrigin Tests ---
    @Test
    fun `resolveChangeOrigin calculates correct origin for insertions and deletions`() {
        val textLength = 20

        val insertOrigin = SpanManager.resolveChangeOrigin(cursorPosition = 10, lengthDifference = 2, textLength)
        assertEquals(8, insertOrigin)

        val deleteOrigin = SpanManager.resolveChangeOrigin(cursorPosition = 5, lengthDifference = -3, textLength)
        assertEquals(5, deleteOrigin)
    }
}