package com.denser.hyphen.core.state

import com.denser.hyphen.core.model.MarkupStyle
import com.denser.hyphen.core.model.StyleRange
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SpanManagerTest {

    // ─────────────────────────────────────────────────────────────────────────────
    // shiftSpans Tests
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    fun `shiftSpans shifts span to the right when text is inserted before it`() {
        val spans = listOf(StyleRange(MarkupStyle.Bold, start = 5, end = 10))
        val result = SpanManager.shiftSpans(spans, changeStart = 2, lengthDifference = 3)

        assertEquals(1, result.size)
        val shiftedSpan = result.first()
        assertEquals(8, shiftedSpan.start)
        assertEquals(13, shiftedSpan.end)
    }

    @Test
    fun `shiftSpans expands span when text is inserted inside it`() {
        val spans = listOf(StyleRange(MarkupStyle.Bold, start = 5, end = 10))
        val result = SpanManager.shiftSpans(spans, changeStart = 7, lengthDifference = 3)

        assertEquals(1, result.size)
        val expandedSpan = result.first()
        assertEquals(5, expandedSpan.start)
        assertEquals(13, expandedSpan.end)
    }

    @Test
    fun `shiftSpans deletes span if it is completely collapsed by deletion`() {
        val spans = listOf(StyleRange(MarkupStyle.Bold, start = 5, end = 10))
        val result = SpanManager.shiftSpans(spans, changeStart = 2, lengthDifference = -10)

        assertTrue(result.isEmpty(), "Span should be completely destroyed if its text is deleted")
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // mergeSpans Tests
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    fun `mergeSpans deduplicates exact matches and appends all incoming spans`() {
        // The implementation filters out existing spans that perfectly match
        // the class, start, and end of incoming spans.
        val existing = listOf(
            StyleRange(MarkupStyle.Bold, 0, 5),
            StyleRange(MarkupStyle.Italic, 6, 10)
        )
        val incoming = listOf(
            StyleRange(MarkupStyle.Bold, 0, 5), // Exact match, should deduplicate
            StyleRange(MarkupStyle.Underline, 0, 5) // New span
        )

        val result = SpanManager.mergeSpans(existing, incoming)

        assertEquals(3, result.size)
        assertTrue(result.any { it.style == MarkupStyle.Italic && it.start == 6 })
        assertTrue(result.any { it.style == MarkupStyle.Bold && it.start == 0 })
        assertTrue(result.any { it.style == MarkupStyle.Underline && it.start == 0 })
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // consolidateSpans Tests
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    fun `consolidateSpans merges overlapping spans of the same style`() {
        val spans = listOf(
            StyleRange(MarkupStyle.Bold, 0, 5),
            StyleRange(MarkupStyle.Bold, 3, 8)
        )

        val result = SpanManager.consolidateSpans(spans)

        assertEquals(1, result.size)
        assertEquals(0, result.first().start)
        assertEquals(8, result.first().end)
    }

    @Test
    fun `consolidateSpans does not merge different styles`() {
        val spans = listOf(
            StyleRange(MarkupStyle.Bold, 0, 5),
            StyleRange(MarkupStyle.Italic, 3, 8)
        )

        val result = SpanManager.consolidateSpans(spans)

        assertEquals(2, result.size)
    }

    @Test
    fun `consolidateSpans merges overlapping block styles identically to inline styles`() {
        val spans = listOf(
            StyleRange(MarkupStyle.BulletList, 0, 5),
            StyleRange(MarkupStyle.BulletList, 3, 8)
        )

        val result = SpanManager.consolidateSpans(spans)

        // The SpanManager groups by style class, so overlapping blocks will be mathematically merged.
        assertEquals(1, result.size)
        assertEquals(0, result.first().start)
        assertEquals(8, result.first().end)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // toggleStyle Tests
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    fun `toggleStyle splits an existing span if toggled inside it`() {
        val spans = listOf(StyleRange(MarkupStyle.Bold, 0, 10))
        val result = SpanManager.toggleStyle(spans, MarkupStyle.Bold, start = 4, end = 6)

        // Toggling Bold off in the middle of a Bold span should rip it in half
        assertEquals(2, result.size)

        val firstHalf = assertNotNull(result.find { it.start == 0 })
        assertEquals(4, firstHalf.end)

        val secondHalf = assertNotNull(result.find { it.start == 6 })
        assertEquals(10, secondHalf.end)
    }

    @Test
    fun `toggleStyle adds a new span if the style does not fully enclose the range`() {
        val spans = listOf(StyleRange(MarkupStyle.Bold, 0, 4))
        val result = SpanManager.toggleStyle(spans, MarkupStyle.Bold, start = 8, end = 12)

        assertEquals(2, result.size)
        assertTrue(result.any { it.start == 8 && it.end == 12 })
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // applyTypingOverrides Tests
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    fun `applyTypingOverrides removes inactive styles and adds active styles in typed range`() {
        val spans = listOf(StyleRange(MarkupStyle.Bold, 0, 10))

        // User typed from 4 to 6. Bold is inactive in the toolbar. Italic is active.
        val result = SpanManager.applyTypingOverrides(
            currentSpans = spans,
            activeStyles = listOf(MarkupStyle.Italic),
            changeOrigin = 4,
            insertEnd = 6
        )

        // Bold should be split (0..4, 6..10), Italic should be added inside the typing gap (4..6)
        assertEquals(3, result.size)

        assertTrue(result.any { it.style == MarkupStyle.Bold && it.start == 0 && it.end == 4 })
        assertTrue(result.any { it.style == MarkupStyle.Bold && it.start == 6 && it.end == 10 })
        assertTrue(result.any { it.style == MarkupStyle.Italic && it.start == 4 && it.end == 6 })
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // resolveChangeOrigin Tests
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    fun `resolveChangeOrigin calculates correct origin for insertions and deletions`() {
        val textLength = 20

        val insertOrigin = SpanManager.resolveChangeOrigin(cursorPosition = 10, lengthDifference = 2, textLength)
        assertEquals(8, insertOrigin, "Insert origin should backtrack by the length difference")

        val deleteOrigin = SpanManager.resolveChangeOrigin(cursorPosition = 5, lengthDifference = -3, textLength)
        assertEquals(5, deleteOrigin, "Delete origin should be exactly at the cursor position")
    }
}