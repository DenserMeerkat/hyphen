package com.denser.hyphen.markdown

import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.model.MarkupStyleRange
import kotlin.test.assertEquals
import kotlin.test.Test

class MarkdownSerializerTest {

    @Test
    fun `serialize handles overlapping styles perfectly without tangling tags`() {
        val text = "Overlap"
        val spans = listOf(
            MarkupStyleRange(MarkupStyle.Bold, 0, 7),
            MarkupStyleRange(MarkupStyle.Italic, 0, 7)
        )

        val result = MarkdownSerializer.serialize(text, spans)

        // Verifies the `.thenBy { it.isClosing }` sorting logic works correctly
        assertEquals("***Overlap***", result)
    }

    @Test
    fun `substring serialize safely truncates a span cut in half`() {
        // "This is a sentence"
        // Let's say "is a sentence" (5 to 18) is Bold.
        val text = "This is a sentence"
        val spans = listOf(MarkupStyleRange(MarkupStyle.Bold, 5, 18))

        // If the user only selects/copies "is a" (indices 5 to 9)
        val result = MarkdownSerializer.serialize(text, spans, start = 5, end = 9)

        // The span should shrink to fit the substring boundary, closing safely.
        assertEquals("**is a**", result)
    }

    @Test
    fun `substring serialize drops spans completely outside the selection`() {
        val text = "Bold and Italic"
        val spans = listOf(
            MarkupStyleRange(MarkupStyle.Bold, 0, 4),   // "Bold"
            MarkupStyleRange(MarkupStyle.Italic, 9, 15) // "Italic"
        )

        // Select only the word "Italic"
        val result = MarkdownSerializer.serialize(text, spans, start = 9, end = 15)

        // The bold span should be entirely stripped out
        assertEquals("*Italic*", result)
    }

    @Test
    fun `substring serialize handles negative and out of bounds indices safely`() {
        val text = "Text"
        val spans = listOf(MarkupStyleRange(MarkupStyle.Bold, 0, 4))

        // Attempt to copy from -10 to +100
        val result = MarkdownSerializer.serialize(text, spans, start = -10, end = 100)

        // coerceIn should catch this and process the full valid string
        assertEquals("**Text**", result)
    }

    @Test
    fun `substring serialize returns empty string if start is greater than or equal to end`() {
        val text = "Text"
        val spans = listOf(MarkupStyleRange(MarkupStyle.Bold, 0, 4))

        val reversedBoundsResult = MarkdownSerializer.serialize(text, spans, start = 4, end = 2)
        val identicalBoundsResult = MarkdownSerializer.serialize(text, spans, start = 2, end = 2)

        assertEquals("", reversedBoundsResult)
        assertEquals("", identicalBoundsResult)
    }
}