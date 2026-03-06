package com.denser.hyphen.state

import androidx.compose.ui.text.TextRange
import com.denser.hyphen.model.MarkupStyle
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.Test

class HyphenTextStateTest {

    
    // init
    @Test
    fun `init with plain text sets text and leaves spans empty`() {
        val state = HyphenTextState("Hello World")

        assertEquals("Hello World", state.text)
        assertTrue(state.spans.isEmpty())
        assertFalse(state.canUndo)
    }

    @Test
    fun `init with markdown text processes formatting correctly`() {
        val state = HyphenTextState("**Hello**")

        assertEquals("Hello", state.text)
        assertEquals(1, state.spans.size)
        val span = state.spans.first()
        assertEquals(MarkupStyle.Bold, span.style)
        assertEquals(0, span.start)
        assertEquals(5, span.end)
    }

    @Test
    fun `init with empty string produces empty state`() {
        val state = HyphenTextState("")

        assertEquals("", state.text)
        assertTrue(state.spans.isEmpty())
        assertFalse(state.canUndo)
    }

    @Test
    fun `init with multiple inline styles parses all spans`() {
        val state = HyphenTextState("**bold** and *italic*")

        assertEquals("bold and italic", state.text)
        assertTrue(state.spans.any { it.style == MarkupStyle.Bold })
        assertTrue(state.spans.any { it.style == MarkupStyle.Italic })
    }

    
    // isFocused / updateSelection
    @Test
    fun `isFocused defaults to false`() {
        val state = HyphenTextState("Hello")
        assertFalse(state.isFocused)
    }

    @Test
    fun `isFocused can be set`() {
        val state = HyphenTextState("Hello")
        state.isFocused = true
        assertTrue(state.isFocused)

        state.isFocused = false
        assertFalse(state.isFocused)
    }

    @Test
    fun `updateSelection preserves last valid selection for toolbar use`() {
        val state = HyphenTextState("Hello World")

        state.isFocused = true
        state.updateSelection(TextRange(0, 5))
        state.isFocused = false

        state.toggleStyle(MarkupStyle.Bold)

        assertEquals(1, state.spans.size)
        assertEquals(0, state.spans.first().start)
        assertEquals(5, state.spans.first().end)
    }

    @Test
    fun `updateSelection does not save collapsed cursor`() {
        val state = HyphenTextState("Hello World")

        state.isFocused = true
        state.updateSelection(TextRange(0, 5))  // valid — should be saved
        state.updateSelection(TextRange(3))      // collapsed — should not overwrite

        state.isFocused = false
        state.toggleStyle(MarkupStyle.Bold)

        assertEquals(1, state.spans.size)
        assertEquals(0, state.spans.first().start)
        assertEquals(5, state.spans.first().end)
    }

    
    // resolvedSelection (tested indirectly via toggleStyle / hasStyle)
    @Test
    fun `resolvedSelection handles reversed selection correctly in toggleStyle`() {
        val state = HyphenTextState("Hello")
        state.textFieldState.edit { this.selection = TextRange(5, 0) }
        state.toggleStyle(MarkupStyle.Bold)

        assertEquals(1, state.spans.size)
        assertEquals(0, state.spans.first().start)
        assertEquals(5, state.spans.first().end)
    }

    @Test
    fun `resolvedSelection handles reversed selection correctly in hasStyle`() {
        val state = HyphenTextState("Hello")
        state.textFieldState.edit { this.selection = TextRange(0, 5) }
        state.toggleStyle(MarkupStyle.Bold)

        state.textFieldState.edit { this.selection = TextRange(5, 0) }
        assertTrue(state.hasStyle(MarkupStyle.Bold))
    }

    
    // toggleStyle — inline styles
    @Test
    fun `toggleStyle with inline style on empty selection sets pending override`() {
        val state = HyphenTextState("Hello")

        state.toggleStyle(MarkupStyle.Bold)

        assertTrue(state.spans.isEmpty())
        assertEquals(state.pendingOverrides[MarkupStyle.Bold], true)
        assertTrue(state.hasStyle(MarkupStyle.Bold))
    }

    @Test
    fun `toggleStyle on empty selection turns off active style via pending override`() {
        val state = HyphenTextState("Hello")
        state.textFieldState.edit { this.selection = TextRange(0, 5) }
        state.toggleStyle(MarkupStyle.Bold)

        state.textFieldState.edit { this.selection = TextRange(2) }
        state.toggleStyle(MarkupStyle.Bold)

        assertEquals(state.pendingOverrides[MarkupStyle.Bold], false)
        assertFalse(state.hasStyle(MarkupStyle.Bold))
    }

    @Test
    fun `toggleStyle with inline style on text range applies span directly`() {
        val state = HyphenTextState("Hello")

        state.textFieldState.edit { this.selection = TextRange(0, 5) }
        state.toggleStyle(MarkupStyle.Bold)

        assertTrue(state.pendingOverrides.isEmpty())
        assertEquals(1, state.spans.size)
        assertEquals(MarkupStyle.Bold, state.spans.first().style)
        assertTrue(state.hasStyle(MarkupStyle.Bold))
    }

    @Test
    fun `toggleStyle twice on same range removes the span`() {
        val state = HyphenTextState("Hello")

        state.textFieldState.edit { this.selection = TextRange(0, 5) }
        state.toggleStyle(MarkupStyle.Bold)
        state.toggleStyle(MarkupStyle.Bold)

        assertTrue(state.spans.isEmpty())
        assertFalse(state.hasStyle(MarkupStyle.Bold))
    }

    @Test
    fun `toggleStyle applies multiple independent styles to overlapping ranges`() {
        val state = HyphenTextState("Hello World")

        state.textFieldState.edit { this.selection = TextRange(0, 11) }
        state.toggleStyle(MarkupStyle.Bold)

        state.textFieldState.edit { this.selection = TextRange(6, 11) }
        state.toggleStyle(MarkupStyle.Italic)

        assertTrue(state.spans.any { it.style == MarkupStyle.Bold })
        assertTrue(state.spans.any { it.style == MarkupStyle.Italic })

        state.textFieldState.edit { this.selection = TextRange(0, 5) }
        assertTrue(state.hasStyle(MarkupStyle.Bold))
        assertFalse(state.hasStyle(MarkupStyle.Italic))
    }

    @Test
    fun `toggleStyle with partial overlap splits existing span correctly`() {
        val state = HyphenTextState("Hello World")

        state.textFieldState.edit { this.selection = TextRange(0, 11) }
        state.toggleStyle(MarkupStyle.Bold)

        state.textFieldState.edit { this.selection = TextRange(6, 11) }
        state.toggleStyle(MarkupStyle.Bold)

        state.textFieldState.edit { this.selection = TextRange(0, 5) }
        assertTrue(state.hasStyle(MarkupStyle.Bold))

        state.textFieldState.edit { this.selection = TextRange(6, 11) }
        assertFalse(state.hasStyle(MarkupStyle.Bold))
    }

    @Test
    fun `toggleStyle clears remembered selection after applying`() {
        val state = HyphenTextState("Hello World")

        state.isFocused = true
        state.updateSelection(TextRange(0, 5))
        state.isFocused = false

        state.toggleStyle(MarkupStyle.Bold) // consumes remembered selection

        // Second toggle — remembered selection is cleared, should produce pending override
        state.toggleStyle(MarkupStyle.Italic)

        assertTrue(state.pendingOverrides.containsKey(MarkupStyle.Italic))
        assertFalse(state.spans.any { it.style == MarkupStyle.Italic })
    }

    
    // toggleStyle — block styles
    @Test
    fun `toggleStyle with block style delegates to BlockStyleManager and alters text`() {
        val state = HyphenTextState("Hello")

        state.textFieldState.edit { this.selection = TextRange(0, 5) }
        state.toggleStyle(MarkupStyle.BulletList)

        assertEquals("- Hello", state.text)
        assertTrue(state.hasStyle(MarkupStyle.BulletList))
    }

    @Test
    fun `toggleStyle with block style twice removes the prefix`() {
        val state = HyphenTextState("Hello")

        state.textFieldState.edit { this.selection = TextRange(0, 5) }
        state.toggleStyle(MarkupStyle.BulletList)
        state.toggleStyle(MarkupStyle.BulletList)

        assertEquals("Hello", state.text)
        assertFalse(state.hasStyle(MarkupStyle.BulletList))
    }

    @Test
    fun `toggleStyle block style uses remembered selection when unfocused`() {
        val state = HyphenTextState("Hello")

        state.isFocused = true
        state.updateSelection(TextRange(0, 5))
        state.isFocused = false

        state.toggleStyle(MarkupStyle.BulletList)

        assertEquals("- Hello", state.text)
    }

    
    // hasStyle
    @Test
    fun `hasStyle returns false when no spans exist`() {
        val state = HyphenTextState("Hello")

        assertFalse(state.hasStyle(MarkupStyle.Bold))
        assertFalse(state.hasStyle(MarkupStyle.Italic))
    }

    @Test
    fun `hasStyle returns false for cursor outside span boundaries`() {
        val state = HyphenTextState("Hello World")

        state.textFieldState.edit { this.selection = TextRange(6, 11) }
        state.toggleStyle(MarkupStyle.Bold)

        state.textFieldState.edit { this.selection = TextRange(0) }
        assertFalse(state.hasStyle(MarkupStyle.Bold))
    }

    @Test
    fun `hasStyle returns true for cursor at span boundary`() {
        val state = HyphenTextState("Hello")

        state.textFieldState.edit { this.selection = TextRange(0, 5) }
        state.toggleStyle(MarkupStyle.Bold)

        state.textFieldState.edit { this.selection = TextRange(0) }
        assertTrue(state.hasStyle(MarkupStyle.Bold))
    }

    
    // isStyleAt
    @Test
    fun `isStyleAt returns true inside span range`() {
        val state = HyphenTextState("Hello")

        state.textFieldState.edit { this.selection = TextRange(0, 5) }
        state.toggleStyle(MarkupStyle.Bold)

        assertTrue(state.isStyleAt(0, MarkupStyle.Bold))
        assertTrue(state.isStyleAt(4, MarkupStyle.Bold))
    }

    @Test
    fun `isStyleAt returns false outside span range`() {
        val state = HyphenTextState("Hello World")

        state.textFieldState.edit { this.selection = TextRange(0, 5) }
        state.toggleStyle(MarkupStyle.Bold)

        assertFalse(state.isStyleAt(6, MarkupStyle.Bold))
        assertFalse(state.isStyleAt(5, MarkupStyle.Bold))
    }

    @Test
    fun `isStyleAt returns false for wrong style at valid position`() {
        val state = HyphenTextState("Hello")

        state.textFieldState.edit { this.selection = TextRange(0, 5) }
        state.toggleStyle(MarkupStyle.Bold)

        assertFalse(state.isStyleAt(2, MarkupStyle.Italic))
    }

    
    // clearPendingOverrides
    @Test
    fun `clearPendingOverrides clears active typing styles`() {
        val state = HyphenTextState("Hello")
        state.toggleStyle(MarkupStyle.Bold)

        state.clearPendingOverrides()

        assertTrue(state.pendingOverrides.isEmpty())
        assertFalse(state.hasStyle(MarkupStyle.Bold))
    }

    @Test
    fun `clearPendingOverrides is a no-op when nothing is pending`() {
        val state = HyphenTextState("Hello")

        state.clearPendingOverrides()

        assertTrue(state.pendingOverrides.isEmpty())
    }

    @Test
    fun `clearPendingOverrides clears multiple pending styles at once`() {
        val state = HyphenTextState("Hello")
        state.toggleStyle(MarkupStyle.Bold)
        state.toggleStyle(MarkupStyle.Italic)

        state.clearPendingOverrides()

        assertTrue(state.pendingOverrides.isEmpty())
    }

    
    // clearAllStyles
    @Test
    fun `clearAllStyles removes all inline styles from selection`() {
        val state = HyphenTextState("Hello World")

        state.textFieldState.edit { this.selection = TextRange(0, 11) }
        state.toggleStyle(MarkupStyle.Bold)
        state.toggleStyle(MarkupStyle.Italic)

        state.clearAllStyles()

        assertTrue(state.spans.isEmpty())
        assertFalse(state.hasStyle(MarkupStyle.Bold))
        assertFalse(state.hasStyle(MarkupStyle.Italic))
    }

    @Test
    fun `clearAllStyles only removes styles within selection bounds`() {
        val state = HyphenTextState("Hello World")

        state.textFieldState.edit { this.selection = TextRange(0, 5) }
        state.toggleStyle(MarkupStyle.Bold)

        state.textFieldState.edit { this.selection = TextRange(6, 11) }
        state.toggleStyle(MarkupStyle.Italic)

        state.textFieldState.edit { this.selection = TextRange(0, 5) }
        state.clearAllStyles()

        assertFalse(state.hasStyle(MarkupStyle.Bold))

        state.textFieldState.edit { this.selection = TextRange(6, 11) }
        assertTrue(state.hasStyle(MarkupStyle.Italic))
    }

    @Test
    fun `clearAllStyles on collapsed cursor clears pending overrides instead`() {
        val state = HyphenTextState("Hello")
        state.toggleStyle(MarkupStyle.Bold)
        state.toggleStyle(MarkupStyle.Italic)

        state.clearAllStyles()

        assertTrue(state.pendingOverrides[MarkupStyle.Bold] == false)
        assertTrue(state.pendingOverrides[MarkupStyle.Italic] == false)
        assertFalse(state.hasStyle(MarkupStyle.Bold))
        assertFalse(state.hasStyle(MarkupStyle.Italic))
    }

    @Test
    fun `clearAllStyles on collapsed cursor does not affect existing spans`() {
        val state = HyphenTextState("Hello")

        state.textFieldState.edit { this.selection = TextRange(0, 5) }
        state.toggleStyle(MarkupStyle.Bold)

        state.textFieldState.edit { this.selection = TextRange(2) }
        state.clearAllStyles()

        assertEquals(1, state.spans.size)
        assertEquals(MarkupStyle.Bold, state.spans.first().style)
    }

    @Test
    fun `clearAllStyles is a no-op when selection has no styles`() {
        val state = HyphenTextState("Hello World")

        state.textFieldState.edit { this.selection = TextRange(0, 11) }
        state.clearAllStyles()

        assertTrue(state.spans.isEmpty())
    }

    @Test
    fun `clearAllStyles handles reversed selection correctly`() {
        val state = HyphenTextState("Hello")

        state.textFieldState.edit { this.selection = TextRange(0, 5) }
        state.toggleStyle(MarkupStyle.Bold)

        state.textFieldState.edit { this.selection = TextRange(5, 0) }
        state.clearAllStyles()

        assertTrue(state.spans.isEmpty())
    }

    @Test
    fun `clearAllStyles saves undo snapshot`() {
        val state = HyphenTextState("Hello")

        state.textFieldState.edit { this.selection = TextRange(0, 5) }
        state.toggleStyle(MarkupStyle.Bold)
        state.clearAllStyles()

        assertTrue(state.canUndo)
        state.undo()

        assertEquals(1, state.spans.size)
        assertEquals(MarkupStyle.Bold, state.spans.first().style)
    }

    @Test
    fun `clearAllStyles uses remembered selection when unfocused`() {
        val state = HyphenTextState("Hello World")

        state.textFieldState.edit { this.selection = TextRange(0, 11) }
        state.toggleStyle(MarkupStyle.Bold)

        state.isFocused = true
        state.updateSelection(TextRange(0, 11))
        state.isFocused = false

        state.clearAllStyles()

        assertTrue(state.spans.isEmpty())
    }

    
    // undo / redo
    @Test
    fun `undo and redo successfully restore text and spans`() {
        val state = HyphenTextState("Hello")

        state.textFieldState.edit { this.selection = TextRange(0, 5) }
        state.toggleStyle(MarkupStyle.Bold)
        assertEquals(1, state.spans.size)

        state.undo()

        assertTrue(state.spans.isEmpty())
        assertTrue(state.canRedo)

        state.redo()

        assertEquals(1, state.spans.size)
        assertEquals(MarkupStyle.Bold, state.spans.first().style)
    }

    @Test
    fun `undo is a no-op when history is empty`() {
        val state = HyphenTextState("Hello")

        state.undo()

        assertEquals("Hello", state.text)
        assertTrue(state.spans.isEmpty())
    }

    @Test
    fun `redo is a no-op when no undone actions exist`() {
        val state = HyphenTextState("Hello")

        state.textFieldState.edit { this.selection = TextRange(0, 5) }
        state.toggleStyle(MarkupStyle.Bold)

        state.redo()

        assertEquals(1, state.spans.size)
    }

    @Test
    fun `multiple undos walk back through full history`() {
        val state = HyphenTextState("Hello World")

        state.textFieldState.edit { this.selection = TextRange(0, 5) }
        state.toggleStyle(MarkupStyle.Bold)

        state.textFieldState.edit { this.selection = TextRange(6, 11) }
        state.toggleStyle(MarkupStyle.Italic)

        assertEquals(2, state.spans.size)

        state.undo()
        assertEquals(1, state.spans.size)
        assertEquals(MarkupStyle.Bold, state.spans.first().style)

        state.undo()
        assertTrue(state.spans.isEmpty())
    }

    @Test
    fun `undo clears remembered selection`() {
        val state = HyphenTextState("Hello")

        // Apply bold via direct selection (not remembered)
        state.textFieldState.edit { this.selection = TextRange(0, 5) }
        state.toggleStyle(MarkupStyle.Bold)

        // Now save a remembered selection before undoing
        state.isFocused = true
        state.updateSelection(TextRange(1, 4))
        state.isFocused = false

        // Undo should clear the remembered selection
        state.undo()

        // Remembered selection is gone — toggleStyle on collapsed cursor
        // should produce a pending override, not a span
        state.textFieldState.edit { this.selection = TextRange(2) }
        state.toggleStyle(MarkupStyle.Italic)

        assertTrue(state.pendingOverrides.containsKey(MarkupStyle.Italic))
        assertFalse(state.spans.any { it.style == MarkupStyle.Italic })
    }

    
    // toMarkdown
    @Test
    fun `toMarkdown accurately serializes current state`() {
        val state = HyphenTextState("Hello World")

        state.textFieldState.edit { this.selection = TextRange(6, 11) }
        state.toggleStyle(MarkupStyle.Bold)

        assertEquals("Hello **World**", state.toMarkdown())
    }

    @Test
    fun `toMarkdown with no styles returns plain text`() {
        val state = HyphenTextState("Hello World")

        assertEquals("Hello World", state.toMarkdown())
    }

    @Test
    fun `toMarkdown with start and end serializes only the substring`() {
        val state = HyphenTextState("Hello World")

        state.textFieldState.edit { this.selection = TextRange(0, 11) }
        state.toggleStyle(MarkupStyle.Bold)

        val markdown = state.toMarkdown(6, 11)
        assertEquals("**World**", markdown)
    }

    @Test
    fun `toMarkdown clamps out-of-bounds start and end safely`() {
        val state = HyphenTextState("Hello")

        val markdown = state.toMarkdown(-5, 999)
        assertEquals("Hello", markdown)
    }

    @Test
    fun `toMarkdown with multiple styles serializes all correctly`() {
        val state = HyphenTextState("Hello World")

        state.textFieldState.edit { this.selection = TextRange(0, 5) }
        state.toggleStyle(MarkupStyle.Bold)

        state.textFieldState.edit { this.selection = TextRange(6, 11) }
        state.toggleStyle(MarkupStyle.Italic)

        assertEquals("**Hello** *World*", state.toMarkdown())
    }
}