package com.denser.hyphen.state

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.text.TextRange
import com.denser.hyphen.model.MarkupStyle
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.Test

class BlockStyleManagerTest {

    // --- isBlockStyle Tests ---
    @Test
    fun `isBlockStyle identifies correct block styles`() {
        assertTrue(BlockStyleManager.isBlockStyle(MarkupStyle.BulletList))
        assertTrue(BlockStyleManager.isBlockStyle(MarkupStyle.OrderedList))
        assertTrue(BlockStyleManager.isBlockStyle(MarkupStyle.Blockquote))

        assertFalse(BlockStyleManager.isBlockStyle(MarkupStyle.Bold))
        assertFalse(BlockStyleManager.isBlockStyle(MarkupStyle.Italic))
    }

    // --- hasBlockStyle Tests ---
    @Test
    fun `hasBlockStyle detects styles based on text prefixes`() {
        val text = "- Item 1\n1. Item 2\n> Quote\nPlain text"

        // Cursor on line 1 (- Item 1)
        assertTrue(BlockStyleManager.hasBlockStyle(text, TextRange(2), MarkupStyle.BulletList))
        assertFalse(BlockStyleManager.hasBlockStyle(text, TextRange(2), MarkupStyle.OrderedList))

        // Cursor on line 2 (1. Item 2)
        assertTrue(BlockStyleManager.hasBlockStyle(text, TextRange(12), MarkupStyle.OrderedList))

        // Cursor on line 3 (> Quote)
        assertTrue(BlockStyleManager.hasBlockStyle(text, TextRange(22), MarkupStyle.Blockquote))

        // Cursor on line 4 (Plain text)
        assertFalse(BlockStyleManager.hasBlockStyle(text, TextRange(30), MarkupStyle.BulletList))
    }

    // --- handleSmartEnter Tests ---
    @Test
    fun `handleSmartEnter continues bullet list`() {
        val state = HyphenTextState("- First Item")

        state.textFieldState.edit {
            this.selection = TextRange(12) // Cursor at the end
            val handled = BlockStyleManager.handleSmartEnter(state, this)
            assertTrue(handled)
        }

        // Assert it inserted a newline and the bullet prefix
        assertEquals("- First Item\n- ", state.textFieldState.text.toString())
    }

    @Test
    fun `handleSmartEnter continues alternate bullet styles`() {
        val state = HyphenTextState("• First Item")

        state.textFieldState.edit {
            this.selection = TextRange(12)
            val handled = BlockStyleManager.handleSmartEnter(state, this)
            assertTrue(handled)
        }

        // Assert it respected the exact alternate prefix character used
        assertEquals("• First Item\n• ", state.textFieldState.text.toString())
    }

    @Test
    fun `handleSmartEnter increments ordered list number`() {
        val state = HyphenTextState("5. Fifth Item")

        state.textFieldState.edit {
            this.selection = TextRange(13)
            val handled = BlockStyleManager.handleSmartEnter(state, this)
            assertTrue(handled)
        }

        assertEquals("5. Fifth Item\n6. ", state.textFieldState.text.toString())
    }

    @Test
    fun `handleSmartEnter clears empty list item`() {
        val state = HyphenTextState("- ") // User hit enter on an empty bullet

        state.textFieldState.edit {
            this.selection = TextRange(2)
            val handled = BlockStyleManager.handleSmartEnter(state, this)
            assertTrue(handled)
        }

        // Assert the empty prefix is removed completely
        assertEquals("", state.textFieldState.text.toString())
    }

    // --- applyBlockStyle Tests ---
    @Test
    fun `applyBlockStyle adds prefix to a plain line`() {
        val state = TextFieldState("Hello World")

        state.edit {
            BlockStyleManager.applyBlockStyle(this, emptyList(), TextRange(5), MarkupStyle.BulletList)
        }

        assertEquals("- Hello World", state.text.toString())
    }

    @Test
    fun `applyBlockStyle removes prefix if already applied`() {
        val state = TextFieldState("> Blockquote text")

        state.edit {
            BlockStyleManager.applyBlockStyle(this, emptyList(), TextRange(5), MarkupStyle.Blockquote)
        }

        assertEquals("Blockquote text", state.text.toString())
    }

    @Test
    fun `applyBlockStyle converts between list types seamlessly`() {
        val state = TextFieldState("- Bullet point")

        state.edit {
            // Apply OrderedList over a BulletList
            BlockStyleManager.applyBlockStyle(this, emptyList(), TextRange(5), MarkupStyle.OrderedList)
        }

        assertEquals("1. Bullet point", state.text.toString())
    }

    @Test
    fun `applyBlockStyle applies to multiple selected lines`() {
        val text = "Line 1\nLine 2\nLine 3"
        val state = TextFieldState(text)

        state.edit {
            // Select all text
            BlockStyleManager.applyBlockStyle(this, emptyList(), TextRange(0, text.length), MarkupStyle.BulletList)
        }

        assertEquals("- Line 1\n- Line 2\n- Line 3", state.text.toString())
    }

    @Test
    fun `applyBlockStyle heals and auto-renumbers ordered lists`() {
        // We have list item 1, a gap, and list item 1 again.
        val text = "1. First\nSecond\n1. Third"
        val state = TextFieldState(text)

        state.edit {
            // Cursor on "Second". We apply an OrderedList to it.
            BlockStyleManager.applyBlockStyle(this, emptyList(), TextRange(10), MarkupStyle.OrderedList)
        }

        // The manager should turn "Second" into "2.", and heal "1. Third" into "3."
        assertEquals("1. First\n2. Second\n3. Third", state.text.toString())
    }

    @Test
    fun `applyBlockStyle resets auto-renumbering when list breaks`() {
        val text = "1. Item\nPlain Text\nItem"
        val state = TextFieldState(text)

        state.edit {
            // Apply ordered list to the 3rd line ("Item")
            BlockStyleManager.applyBlockStyle(this, emptyList(), TextRange(text.length - 1), MarkupStyle.OrderedList)
        }

        // The Plain Text breaks the chain, so the 3rd line should start back at 1.
        assertEquals("1. Item\nPlain Text\n1. Item", state.text.toString())
    }
}