package com.denser.hyphen.state

import androidx.compose.ui.text.TextRange
import com.denser.hyphen.model.MarkupStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HyphenTextStateTest {

    @Test
    fun `init with plain text sets text and leaves spans empty`() {
        val state = HyphenTextState("Hello World")

        assertEquals("Hello World", state.text)
        assertTrue(state.spans.isEmpty())
        assertFalse(state.canUndo) // The initial state is the baseline
    }

    @Test
    fun `init with markdown text processes formatting correctly`() {
        val state = HyphenTextState("**Hello**")

        // The text should be stripped of markdown
        assertEquals("Hello", state.text)

        // Spans should contain the Bold style
        assertEquals(1, state.spans.size)
        val span = state.spans.first()
        assertEquals(MarkupStyle.Bold, span.style)
        assertEquals(0, span.start)
        assertEquals(5, span.end)
    }

    @Test
    fun `toggleStyle with inline style on empty selection sets pending override`() {
        val state = HyphenTextState("Hello")

        // toggleStyle relies on the internal textFieldState selection.
        // By default, cursor is at the end (index 5)
        state.toggleStyle(MarkupStyle.Bold)

        // It shouldn't add a span yet...
        assertTrue(state.spans.isEmpty())
        // ...but it should be marked as pending for the next typed character
        assertTrue(state.pendingOverrides[MarkupStyle.Bold] == true)
        assertTrue(state.hasStyle(MarkupStyle.Bold))
    }

    @Test
    fun `clearPendingOverrides clears active typing styles`() {
        val state = HyphenTextState("Hello")
        state.toggleStyle(MarkupStyle.Bold)

        state.clearPendingOverrides()

        assertTrue(state.pendingOverrides.isEmpty())
        assertFalse(state.hasStyle(MarkupStyle.Bold))
    }

    @Test
    fun `toggleStyle with inline style on text range applies span directly`() {
        val state = HyphenTextState("Hello")

        // Select the entire word
        state.textFieldState.edit { this.selection = TextRange(0, 5) }
        state.toggleStyle(MarkupStyle.Bold)

        assertTrue(state.pendingOverrides.isEmpty())
        assertEquals(1, state.spans.size)
        assertEquals(MarkupStyle.Bold, state.spans.first().style)
        assertTrue(state.hasStyle(MarkupStyle.Bold))
    }

    @Test
    fun `toggleStyle with block style delegates to BlockStyleManager and alters text`() {
        val state = HyphenTextState("Hello")

        // Select the word
        state.textFieldState.edit { this.selection = TextRange(0, 5) }
        state.toggleStyle(MarkupStyle.BulletList)

        // It should have physically inserted the prefix
        assertEquals("- Hello", state.text)

        // The block style should be recognized
        assertTrue(state.hasStyle(MarkupStyle.BulletList))
    }

    @Test
    fun `undo and redo successfully restore text and spans`() {
        val state = HyphenTextState("Hello") // State 1: Plain

        state.textFieldState.edit { this.selection = TextRange(0, 5) }
        state.toggleStyle(MarkupStyle.Bold) // State 2: Bold

        assertEquals(1, state.spans.size)

        // Action: Undo
        state.undo()

        // Assert: Restored to State 1
        assertTrue(state.spans.isEmpty())
        assertTrue(state.canRedo)

        // Action: Redo
        state.redo()

        // Assert: Restored to State 2
        assertEquals(1, state.spans.size)
        assertEquals(MarkupStyle.Bold, state.spans.first().style)
    }

    @Test
    fun `toMarkdown accurately serializes current state`() {
        val state = HyphenTextState("Hello World")

        // Bold the "World"
        state.textFieldState.edit { this.selection = TextRange(6, 11) }
        state.toggleStyle(MarkupStyle.Bold)

        val markdown = state.toMarkdown()

        assertEquals("Hello **World**", markdown)
    }
}