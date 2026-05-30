package com.denser.hyphen.markdown

import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.model.TriggerConfig
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.Test

class MarkdownProcessorTest {

    @Test
    fun `process returns null when no markdown syntax is present`() {
        val text = "Just a normal string without formatting."
        val result = MarkdownProcessor.process(text, cursorPosition = 5)

        assertNull(result,"Processor should return null to avoid unnecessary recompositions")
    }

    @Test
    fun `process correctly strips inline markdown and generates accurate spans`() {
        // Raw text: "Hello **World**" (15 chars)
        // Clean text: "Hello World" (11 chars)
        val text = "Hello **World**"
        val result = MarkdownProcessor.process(text, cursorPosition = 0)

        assertNotNull(result)
        assertEquals("Hello World", result?.cleanText)

        assertEquals(1, result?.newSpans?.size)
        val span = result?.newSpans?.first()

        assertEquals(MarkupStyle.Bold, span?.style)
        assertEquals(6, span?.start) // 'W' is at index 6
        assertEquals(11, span?.end)  // 'd' ends at index 11
    }

    @Test
    fun `process handles multiple inline styles sequentially`() {
        // Raw text: "**Bold** and *Italic*"
        val text = "**Bold** and *Italic*"
        val result = MarkdownProcessor.process(text, cursorPosition = 0)

        assertNotNull(result)
        assertEquals("Bold and Italic", result?.cleanText)
        assertEquals(2, result?.newSpans?.size)

        val boldSpan = result?.newSpans?.find { it.style == MarkupStyle.Bold }
        val italicSpan = result?.newSpans?.find { it.style == MarkupStyle.Italic }

        assertNotNull(boldSpan)
        assertEquals(0, boldSpan?.start)
        assertEquals(4, boldSpan?.end) // "Bold"

        assertNotNull(italicSpan)
        assertEquals(9, italicSpan?.start)
        assertEquals(15, italicSpan?.end) // "Italic"
    }

    @Test
    fun `process preserves block prefixes in text but still applies spans`() {
        val textsAndStyles = listOf(
            "- Bullet" to MarkupStyle.BulletList,
            "> Quote" to MarkupStyle.Blockquote,
            "1. Ordered" to MarkupStyle.OrderedList
        )

        for ((text, expectedStyle) in textsAndStyles) {
            val result = MarkdownProcessor.process(text, cursorPosition = 0)

            assertNotNull(result,"Failed on: $text")
            // Block styles should NOT strip the prefix from the cleanText
            assertEquals(text, result?.cleanText)

            assertEquals(1, result?.newSpans?.size)
            val span = result?.newSpans?.first()
            assertEquals(expectedStyle, span?.style)
            assertEquals(0, span?.start)
            assertEquals(text.length, span?.end)
        }
    }

    @Test
    fun `process identifies checkbox block styles`() {
        val textsAndStyles = listOf(
            "- [ ] Unchecked task" to MarkupStyle.CheckboxUnchecked,
            "- [x] Checked task" to MarkupStyle.CheckboxChecked,
            "* [X] Another checked" to MarkupStyle.CheckboxChecked
        )

        for ((text, expectedStyle) in textsAndStyles) {
            val result = MarkdownProcessor.process(text, cursorPosition = 0)

            assertNotNull(result, "Failed on: $text")

            // Checkbox block styles should preserve the prefix
            assertEquals(text, result?.cleanText)

            // Checkboxes might also generate a BulletList span since they start with "- "
            // We just need to assert that the specific Checkbox span was generated and covers the line.
            val span = result?.newSpans?.find { it.style == expectedStyle }
            assertNotNull(span, "Missing expected $expectedStyle for text: $text")
            assertEquals(0, span?.start)
            assertEquals(text.length, span?.end)
        }
    }

    @Test
    fun `cursor shifts correctly when typing AFTER an inline markdown block`() {
        // Raw text: "**abc**d"
        // Let's say the cursor is at the very end (index 8)
        val text = "**abc**d"
        val result = MarkdownProcessor.process(text, cursorPosition = 8)

        assertNotNull(result)
        assertEquals("abcd", result?.cleanText)

        // The ** (2) and ** (2) were removed, so the cursor should shift left by 4
        assertEquals(4, result?.newCursorPosition)
    }

    @Test
    fun `cursor shifts correctly when typing INSIDE an inline markdown block`() {
        // Raw text: "**ab**"
        // Cursor is between 'a' and 'b' (index 3)
        val text = "**ab**"
        val result = MarkdownProcessor.process(text, cursorPosition = 3)

        assertNotNull(result)
        assertEquals("ab", result?.cleanText)

        // The prefix ** (2 chars) was removed to the left of the cursor.
        // The cursor should shift left by 2.
        assertEquals(1, result?.newCursorPosition)
    }

    @Test
    fun `cursor snaps safely if caught INSIDE a deleted markdown tag`() {
        // Raw text: "**abc**"
        // Cursor is right in the middle of the ending tag (index 6, between the * and *)
        val text = "**abc**"
        val result = MarkdownProcessor.process(text, cursorPosition = 6)

        assertNotNull(result)
        // Cursor should snap safely to the end of the inner text
        assertEquals(3, result?.newCursorPosition)
    }

    @Test
    fun `process parses dynamic mention and resolves correct span and clean text`() {
        val triggerConfigs = listOf(TriggerConfig(trigger = "@", scheme = "mention"))
        val text = "[John Doe](mention:123)"
        val result = MarkdownProcessor.process(text, cursorPosition = 0, triggerConfigs = triggerConfigs)

        assertNotNull(result)
        assertEquals("John Doe", result?.cleanText)
        assertEquals(1, result?.newSpans?.size)

        val span = result?.newSpans?.first()
        assertEquals(MarkupStyle.Mention(display = "John Doe", scheme = "mention", id = "123"), span?.style)
        assertEquals(0, span?.start)
        assertEquals(8, span?.end)
    }

    @Test
    fun `process parses dynamic mention and shifts downstream spans correctly`() {
        val triggerConfigs = listOf(TriggerConfig(trigger = "@", scheme = "mention"))
        val text = "[John Doe](mention:123) and **Bold**"
        val result = MarkdownProcessor.process(text, cursorPosition = 0, triggerConfigs = triggerConfigs)

        assertNotNull(result)
        assertEquals("John Doe and Bold", result?.cleanText)
        assertEquals(2, result?.newSpans?.size)

        val mentionSpan = result?.newSpans?.find { it.style is MarkupStyle.Mention }
        val boldSpan = result?.newSpans?.find { it.style == MarkupStyle.Bold }

        assertNotNull(mentionSpan)
        assertEquals(0, mentionSpan?.start)
        assertEquals(8, mentionSpan?.end)

        assertNotNull(boldSpan)
        assertEquals(13, boldSpan?.start)
        assertEquals(17, boldSpan?.end)
    }

    @Test
    fun `process parses multiple dynamic mentions in a single line`() {
        val triggerConfigs = listOf(TriggerConfig(trigger = "@", scheme = "mention"))
        val text = "[Alice](mention:1) and [Bob](mention:2)"
        val result = MarkdownProcessor.process(text, cursorPosition = 0, triggerConfigs = triggerConfigs)

        assertNotNull(result)
        assertEquals("Alice and Bob", result?.cleanText)
        assertEquals(2, result?.newSpans?.size)

        val aliceSpan = result?.newSpans?.find { (it.style as? MarkupStyle.Mention)?.id == "1" }
        val bobSpan = result?.newSpans?.find { (it.style as? MarkupStyle.Mention)?.id == "2" }

        assertNotNull(aliceSpan)
        assertEquals(0, aliceSpan?.start)
        assertEquals(5, aliceSpan?.end)

        assertNotNull(bobSpan)
        assertEquals(10, bobSpan?.start)
        assertEquals(13, bobSpan?.end)
    }
}