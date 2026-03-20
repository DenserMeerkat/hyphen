package com.denser.hyphen.core.markdown

import com.denser.hyphen.core.model.MarkupStyle
import com.denser.hyphen.core.model.StyleRange
import kotlin.test.Test
import kotlin.test.assertEquals

class MarkdownSerializerTest {

    // ─────────────────────────────────────────────────────────────────────────────
    // Full String Serialization Tests
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    fun `serialize handles overlapping styles perfectly without tangling tags`() {
        val text = "Overlap"
        val spans = listOf(
            StyleRange(MarkupStyle.Bold, 0, 7),
            StyleRange(MarkupStyle.Italic, 0, 7)
        )

        val result = MarkdownSerializer.serialize(text, spans)

        // Verifies the `.thenBy { it.isClosing }` sorting logic works correctly
        // by ensuring tags close in the reverse order they were opened.
        assertEquals("***Overlap***", result)
    }

    @Test
    fun `serialize adds heading prefixes correctly without closing tags`() {
        val text = "Heading 1\nSubheading"
        val spans = listOf(
            StyleRange(MarkupStyle.H1, 0, 9),
            StyleRange(MarkupStyle.H3, 10, 20)
        )

        val result = MarkdownSerializer.serialize(text, spans)

        // Headings only have start symbols ("# "), no end symbols
        assertEquals("# Heading 1\n### Subheading", result)
    }

    @Test
    fun `serialize ignores list block styles because they remain in text buffer`() {
        val text = "- [x] Finished Task\n- Bullet"
        val spans = listOf(
            StyleRange(MarkupStyle.CheckboxChecked, 0, 19),
            StyleRange(MarkupStyle.BulletList, 20, 28)
        )

        val result = MarkdownSerializer.serialize(text, spans)

        // The serializer should NOT add extra prefixes for these block styles
        // because the Hyphen state engine preserves them in the raw text string.
        assertEquals("- [x] Finished Task\n- Bullet", result)
    }

    @Test
    fun `serialize combines block prefix preservation and inline wrapping flawlessly`() {
        // The buffer contains the literal checkbox prefix AND the text
        val text = "- [ ] Buy Milk"
        val spans = listOf(
            StyleRange(MarkupStyle.CheckboxUnchecked, 0, 14),
            StyleRange(MarkupStyle.Bold, 10, 14) // "Milk" is bold
        )

        val result = MarkdownSerializer.serialize(text, spans)

        assertEquals("- [ ] Buy **Milk**", result)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Substring (Selection/Clipboard) Serialization Tests
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    fun `substring serialize safely truncates a span cut in half`() {
        // "This is a sentence"
        // Let's say "is a sentence" (5 to 18) is Bold.
        val text = "This is a sentence"
        val spans = listOf(StyleRange(MarkupStyle.Bold, 5, 18))

        // If the user only selects/copies "is a" (indices 5 to 9)
        val result = MarkdownSerializer.serialize(text, spans, start = 5, end = 9)

        // The span should shrink to fit the substring boundary, closing safely.
        assertEquals("**is a**", result)
    }

    @Test
    fun `substring serialize drops spans completely outside the selection`() {
        val text = "Bold and Italic"
        val spans = listOf(
            StyleRange(MarkupStyle.Bold, 0, 4),   // "Bold"
            StyleRange(MarkupStyle.Italic, 9, 15) // "Italic"
        )

        // Select only the word "Italic"
        val result = MarkdownSerializer.serialize(text, spans, start = 9, end = 15)

        // The bold span should be entirely stripped out
        assertEquals("*Italic*", result)
    }

    @Test
    fun `substring serialize handles negative and out of bounds indices safely`() {
        val text = "Text"
        val spans = listOf(StyleRange(MarkupStyle.Bold, 0, 4))

        // Attempt to copy from -10 to +100
        val result = MarkdownSerializer.serialize(text, spans, start = -10, end = 100)

        // coerceIn should catch this and process the full valid string
        assertEquals("**Text**", result)
    }

    @Test
    fun `substring serialize returns empty string if start is greater than or equal to end`() {
        val text = "Text"
        val spans = listOf(StyleRange(MarkupStyle.Bold, 0, 4))

        val reversedBoundsResult = MarkdownSerializer.serialize(text, spans, start = 4, end = 2)
        val identicalBoundsResult = MarkdownSerializer.serialize(text, spans, start = 2, end = 2)

        assertEquals("", reversedBoundsResult)
        assertEquals("", identicalBoundsResult)
    }
}