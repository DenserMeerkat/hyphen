package com.denser.hyphen.state

import androidx.compose.ui.text.TextRange
import com.denser.hyphen.core.model.MarkupStyle
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.Test

class HyphenTextStateTest {

    // --- Helper for concise testing ---
    private fun HyphenTextState.select(start: Int, end: Int = start) {
        textFieldState.edit { selection = TextRange(start, end) }
        updateSelection(TextRange(start, end)) // Simulates the LaunchedEffect
    }

    @Test
    fun `init and setMarkdown parse formatting and manage history correctly`() {
        val state = HyphenTextState("**Hello**")

        assertEquals("Hello", state.text)
        assertTrue(state.hasStyle(MarkupStyle.Bold))
        assertFalse(state.canUndo)

        // Test the new setMarkdown API
        state.setMarkdown("_World_")
        assertEquals("World", state.text)
        assertTrue(state.hasStyle(MarkupStyle.Italic))
        assertFalse(state.hasStyle(MarkupStyle.Bold))
        assertFalse(state.canUndo) // History should be wiped clean
    }

    @Test
    fun `unfocused state retains last valid selection for toolbar actions`() {
        val state = HyphenTextState("Hello World")

        state.isFocused = true
        state.select(0, 5) // User highlights "Hello"

        state.isFocused = false // User clicks a toolbar button (focus lost)
        state.select(5, 5) // Native text field collapses cursor on focus loss

        state.toggleStyle(MarkupStyle.Bold) // Should still apply to the remembered "Hello"

        assertEquals("**Hello** World", state.toMarkdown())
    }

    @Test
    fun `inline styles toggle correctly and support typing overrides`() {
        val state = HyphenTextState("Hello")
        state.isFocused = true

        // 1. Apply span to selection
        state.select(0, 5)
        state.toggleStyle(MarkupStyle.Bold)
        assertEquals("**Hello**", state.toMarkdown())

        // 2. Remove span from selection
        state.toggleStyle(MarkupStyle.Bold)
        assertEquals("Hello", state.toMarkdown())

        // 3. Pending override on collapsed cursor
        state.select(5)
        state.toggleStyle(MarkupStyle.Italic)
        assertEquals(state.pendingOverrides[MarkupStyle.Italic], true)
        assertTrue(state.hasStyle(MarkupStyle.Italic))
    }

    @Test
    fun `block styles apply and remove prefixes`() {
        val state = HyphenTextState("Hello")
        state.isFocused = true
        state.select(0, 5)

        state.toggleStyle(MarkupStyle.BulletList)
        assertEquals("- Hello", state.text)
        assertTrue(state.hasStyle(MarkupStyle.BulletList))

        state.toggleStyle(MarkupStyle.BulletList)
        assertEquals("Hello", state.text)
        assertFalse(state.hasStyle(MarkupStyle.BulletList))
    }

    @Test
    fun `checkbox formatting acts as a block style via toggleStyle`() {
        val state = HyphenTextState("Clean dishes")
        state.isFocused = true
        state.select(0)

        // Add checkbox formatting
        state.toggleStyle(MarkupStyle.CheckboxUnchecked)
        assertEquals("- [ ] Clean dishes", state.text)
        assertTrue(state.hasStyle(MarkupStyle.CheckboxUnchecked))

        // Remove checkbox formatting
        state.toggleStyle(MarkupStyle.CheckboxUnchecked)
        assertEquals("Clean dishes", state.text)
        assertFalse(state.hasStyle(MarkupStyle.CheckboxUnchecked))
    }

    @Test
    fun `toggleCheckboxAtCursor switches state between checked and unchecked`() {
        val state = HyphenTextState("- [ ] Write tests")
        state.isFocused = true

        // Cursor is anywhere on the line
        state.select(10)

        // 1. Toggle to Checked
        state.toggleCheckboxAtCursor()
        assertEquals("- [x] Write tests", state.text)
        assertTrue(state.hasStyle(MarkupStyle.CheckboxChecked))
        assertFalse(state.hasStyle(MarkupStyle.CheckboxUnchecked))

        // 2. Toggle back to Unchecked
        state.toggleCheckboxAtCursor()
        assertEquals("- [ ] Write tests", state.text)
        assertTrue(state.hasStyle(MarkupStyle.CheckboxUnchecked))
        assertFalse(state.hasStyle(MarkupStyle.CheckboxChecked))
    }

    @Test
    fun `toggleCheckboxAtCursor does nothing on lines without checkboxes`() {
        val text = "Just a normal line"
        val state = HyphenTextState(text)
        state.isFocused = true
        state.select(5)

        state.toggleCheckboxAtCursor()

        // Assert text hasn't changed
        assertEquals(text, state.text)
    }

    @Test
    fun `clearAllStyles punches holes in spans and handles pending overrides`() {
        val state = HyphenTextState("**Hello** *World*")
        state.isFocused = true

        // 1. Clear overlapping spans
        state.select(0, 11)
        state.clearAllStyles()
        assertEquals("Hello World", state.toMarkdown())

        // 2. Clear pending typing overrides
        state.select(5)
        state.toggleStyle(MarkupStyle.Bold) // Turns bold ON for next typed char
        state.clearAllStyles() // Should force bold OFF
        assertEquals(state.pendingOverrides[MarkupStyle.Bold], false)
    }

    @Test
    fun `undo and redo traverse history cleanly`() {
        val state = HyphenTextState("Hello")
        state.isFocused = true

        state.select(0, 5)
        state.toggleStyle(MarkupStyle.Bold)
        assertEquals("**Hello**", state.toMarkdown())

        state.undo()
        assertEquals("Hello", state.toMarkdown())
        assertTrue(state.canRedo)

        state.redo()
        assertEquals("**Hello**", state.toMarkdown())
    }

    @Test
    fun `toMarkdown clamps boundaries and serializes substrings accurately`() {
        val state = HyphenTextState("**Hello** World")

        // 1. Safely clamp out-of-bounds indices
        assertEquals("**Hello** World", state.toMarkdown(-5, 999))

        // 2. Serialize exact substring (preserves span formatting)
        assertEquals("**Hello**", state.toMarkdown(0, 5))
    }
}