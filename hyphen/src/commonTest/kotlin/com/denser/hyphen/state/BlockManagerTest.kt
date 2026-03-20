package com.denser.hyphen.state

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.text.TextRange
import com.denser.hyphen.core.model.MarkupStyle
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

    @Test
    fun `hasBlockStyle distinguishes checkboxes from bullet lists`() {
        val text = "- [ ] Task 1\n- Regular bullet\n- [x] Task 2"

        // Line 1: Unchecked task
        assertTrue(BlockStyleManager.hasBlockStyle(text, TextRange(2), MarkupStyle.CheckboxUnchecked))
        assertFalse(BlockStyleManager.hasBlockStyle(text, TextRange(2), MarkupStyle.BulletList))

        // Line 2: Regular bullet
        assertTrue(BlockStyleManager.hasBlockStyle(text, TextRange(15), MarkupStyle.BulletList))
        assertFalse(BlockStyleManager.hasBlockStyle(text, TextRange(15), MarkupStyle.CheckboxUnchecked))

        // Line 3: Checked task
        assertTrue(BlockStyleManager.hasBlockStyle(text, TextRange(35), MarkupStyle.CheckboxChecked))
        assertFalse(BlockStyleManager.hasBlockStyle(text, TextRange(35), MarkupStyle.BulletList))
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
    fun `handleSmartEnter continues checkboxes as unchecked`() {
        val state = HyphenTextState("- [x] Finished Task")

        state.textFieldState.edit {
            this.selection = TextRange(19)
            val handled = BlockStyleManager.handleSmartEnter(state, this)
            assertTrue(handled)
        }

        assertEquals("- [x] Finished Task\n- [ ] ", state.textFieldState.text.toString())
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

    @Test
    fun `handleSmartEnter clears empty checkbox`() {
        val state = HyphenTextState("- [ ] ")

        state.textFieldState.edit {
            this.selection = TextRange(6)
            val handled = BlockStyleManager.handleSmartEnter(state, this)
            assertTrue(handled)
        }

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
    fun `applyBlockStyle applies checkbox prefix`() {
        val state = TextFieldState("Buy groceries")

        state.edit {
            BlockStyleManager.applyBlockStyle(this, emptyList(), TextRange(5), MarkupStyle.CheckboxUnchecked)
        }

        assertEquals("- [ ] Buy groceries", state.text.toString())
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
    fun `applyBlockStyle converts between list types seamlessly including checkboxes`() {
        val state = TextFieldState("- Bullet point")

        state.edit {
            // Apply OrderedList over a BulletList
            BlockStyleManager.applyBlockStyle(this, emptyList(), TextRange(5), MarkupStyle.OrderedList)
        }
        assertEquals("1. Bullet point", state.text.toString())

        state.edit {
            // Apply Checkbox over OrderedList
            BlockStyleManager.applyBlockStyle(this, emptyList(), TextRange(5), MarkupStyle.CheckboxUnchecked)
        }
        assertEquals("- [ ] Bullet point", state.text.toString())
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
        val text = "1. First\nSecond\n1. Third"
        val state = TextFieldState(text)

        state.edit {
            BlockStyleManager.applyBlockStyle(this, emptyList(), TextRange(10), MarkupStyle.OrderedList)
        }

        assertEquals("1. First\n2. Second\n3. Third", state.text.toString())
    }

    @Test
    fun `applyBlockStyle resets auto-renumbering when list breaks`() {
        val text = "1. Item\nPlain Text\nItem"
        val state = TextFieldState(text)

        state.edit {
            BlockStyleManager.applyBlockStyle(this, emptyList(), TextRange(text.length - 1), MarkupStyle.OrderedList)
        }

        assertEquals("1. Item\nPlain Text\n1. Item", state.text.toString())
    }

    // --- toggleCheckbox Tests ---
    @Test
    fun `toggleCheckbox flips between checked and unchecked states`() {
        val state = TextFieldState("- [ ] Task")

        state.edit {
            val toggled = BlockStyleManager.toggleCheckbox(this, 0, strictPrefixCheck = false)
            assertTrue(toggled)
        }
        assertEquals("- [x] Task", state.text.toString())

        state.edit {
            val toggled = BlockStyleManager.toggleCheckbox(this, 0, strictPrefixCheck = false)
            assertTrue(toggled)
        }
        assertEquals("- [ ] Task", state.text.toString())
    }

    @Test
    fun `toggleCheckbox respects strictPrefixCheck bounds`() {
        val state = TextFieldState("- [ ] Task")

        state.edit {
            // Offset 8 is inside the word "Task". strictPrefixCheck = true should ignore it.
            val toggled = BlockStyleManager.toggleCheckbox(this, 8, strictPrefixCheck = true)
            assertFalse(toggled)
        }
        assertEquals("- [ ] Task", state.text.toString())

        state.edit {
            // strictPrefixCheck = false allows toggling from anywhere on the line
            val toggled = BlockStyleManager.toggleCheckbox(this, 8, strictPrefixCheck = false)
            assertTrue(toggled)
        }
        assertEquals("- [x] Task", state.text.toString())
    }

    @Test
    fun `toggleCheckbox allows clicking inside prefix with strictPrefixCheck true`() {
        val state = TextFieldState("- [ ] Task")

        state.edit {
            // Offset 3 is inside the "- [ ] " prefix
            val toggled = BlockStyleManager.toggleCheckbox(this, 3, strictPrefixCheck = true)
            assertTrue(toggled)
        }
        assertEquals("- [x] Task", state.text.toString())
    }
}