package com.denser.hyphen.state

import androidx.compose.ui.text.TextRange
import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.model.TriggerConfig
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
        state.toggleCheckbox()
        assertEquals("- [x] Write tests", state.text)
        assertTrue(state.hasStyle(MarkupStyle.CheckboxChecked))
        assertFalse(state.hasStyle(MarkupStyle.CheckboxUnchecked))

        // 2. Toggle back to Unchecked
        state.toggleCheckbox()
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

        state.toggleCheckbox()

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

    @Test
    fun `link style should not expand after space at the end`() {
        val state = HyphenTextState("google")
        state.isFocused = true
        state.select(0, 6)
        state.toggleStyle(MarkupStyle.Link("https://google.com"))

        // Ensure link is created
        assertEquals(1, state.spans.size)
        assertTrue(state.spans[0].style is MarkupStyle.Link)
        assertEquals(0, state.spans[0].start)
        assertEquals(6, state.spans[0].end)

        // Move cursor to end
        state.select(6)

        // Type a space
        state.textFieldState.edit {
            replace(6, 6, " ")
            selection = TextRange(7)
            state.processInput(this)
        }

        // Verify link has NOT expanded
        val linkSpan = state.spans.find { it.style is MarkupStyle.Link }
        assertEquals(0, linkSpan?.start)
        assertEquals(6, linkSpan?.end) // Should still be 6
    }

    @Test
    fun `updating a link display name and url updates correctly even when display text length increases`() {
        val state = HyphenTextState("Click here for info")
        state.isFocused = true
        state.select(6, 10) // "here"
        val linkStyle = MarkupStyle.Link("https://google.com")
        state.toggleStyle(linkStyle)

        val initialLink = state.spans.find { it.style is MarkupStyle.Link }!!
        assertEquals(6, initialLink.start)
        assertEquals(10, initialLink.end)

        // Update display text to "here now" (length increases from 4 to 8)
        state.updateLink(initialLink, "here now", "https://github.com")

        assertEquals("Click here now for info", state.text)
        val updatedLink = state.spans.find { it.style is MarkupStyle.Link }!!
        assertEquals(6, updatedLink.start)
        assertEquals(14, updatedLink.end)
        assertEquals("https://github.com", (updatedLink.style as MarkupStyle.Link).url)
    }

    @Test
    fun `link style should not expand when replacing suffix with space`() {
        // Test case for the fix: rawLengthDifference <= 0 but whitespace inserted
        val state = HyphenTextState("googleX")
        state.isFocused = true
        state.select(0, 6) // Link is "google"
        state.toggleStyle(MarkupStyle.Link("https://google.com"))

        // Select the "X" (index 6 to 7)
        state.select(6, 7)

        // Replace "X" with a space " "
        // previousText: "googleX" (len 7)
        // newText:      "google " (len 7)
        // rawLengthDifference = 0
        state.textFieldState.edit {
            replace(6, 7, " ")
            selection = TextRange(7)
            state.processInput(this)
        }

        // Verify link has NOT expanded to include the space
        val linkSpan = state.spans.find { it.style is MarkupStyle.Link }
        assertEquals(0, linkSpan?.start)
        assertEquals(6, linkSpan?.end) // Should still be 6
    }

    @Test
    fun `cursor snaps outside mention span when placed inside`() {
        val triggerConfigs = listOf(TriggerConfig(trigger = "@", scheme = "mention"))
        val state = HyphenTextState("Hello [@JohnDoe](mention:123)!", triggerConfigs)
        state.isFocused = true

        // The mention span is at 6..14 (length 8, "@JohnDoe")
        // Snapping from the left -> should snap to end of mention (14)
        state.select(10)
        assertEquals(14, state.selection.start)

        // Snapping from the right -> should snap to start of mention (6)
        state.textFieldState.edit { selection = TextRange(15) }
        state.updateSelection(TextRange(15)) // set lastCursorPosition to 15
        
        state.select(10)
        assertEquals(6, state.selection.start)
    }

    @Test
    fun `deleting a character inside mention deletes the entire mention`() {
        val triggerConfigs = listOf(TriggerConfig(trigger = "@", scheme = "mention"))
        val state = HyphenTextState("Hello [@JohnDoe](mention:123)!", triggerConfigs)
        state.isFocused = true

        // Mention is at 6..14. Backspace at the end deletes 'e' at index 13
        state.textFieldState.edit {
            replace(13, 14, "")
            selection = TextRange(13)
            state.processInput(this)
        }

        // Verify the entire mention is deleted
        assertEquals("Hello !", state.text)
        assertTrue(state.spans.none { it.style is MarkupStyle.Mention })
    }

    @Test
    fun `inserting text before mention shifts it without clearing it`() {
        val triggerConfigs = listOf(TriggerConfig(trigger = "@", scheme = "mention"))
        val state = HyphenTextState("[@JohnDoe](mention:123)", triggerConfigs)
        state.isFocused = true

        // Insert "@" at the start (index 0)
        state.textFieldState.edit {
            replace(0, 0, "@")
            selection = TextRange(1)
            state.processInput(this)
        }

        // Verify that the mention span shifted right to 1..9 and text is "@@JohnDoe"
        assertEquals("@@JohnDoe", state.text)
        val mentionSpan = state.spans.find { it.style is MarkupStyle.Mention }
        assertEquals(1, mentionSpan?.start)
        assertEquals(9, mentionSpan?.end)
    }

    @Test
    fun `inserting text after mention preserves it without clearing it`() {
        val triggerConfigs = listOf(TriggerConfig(trigger = "@", scheme = "mention"))
        val state = HyphenTextState("[@JohnDoe](mention:123)", triggerConfigs)
        state.isFocused = true

        // Insert "!" at the end (index 8)
        state.textFieldState.edit {
            replace(8, 8, "!")
            selection = TextRange(9)
            state.processInput(this)
        }

        // Verify that the mention span is preserved at 0..8 and text is "@JohnDoe!"
        assertEquals("@JohnDoe!", state.text)
        val mentionSpan = state.spans.find { it.style is MarkupStyle.Mention }
        assertEquals(0, mentionSpan?.start)
        assertEquals(8, mentionSpan?.end)
    }

    @Test
    fun `typing another trigger after a completed mention does not overwrite or destroy it`() {
        val triggerConfigs = listOf(TriggerConfig(trigger = "@", scheme = "mention"))
        val state = HyphenTextState("[@Alice](mention:123)", triggerConfigs)
        state.isFocused = true

        // Ensure the completed mention is correct initially
        assertEquals(1, state.spans.size)
        val mention = state.spans[0]
        assertEquals("123", (mention.style as MarkupStyle.Mention).id)
        assertEquals("@Alice", mention.style.display)
        assertEquals(0, mention.start)
        assertEquals(6, mention.end)

        // Type a space (so text is "@Alice ")
        state.textFieldState.edit {
            replace(6, 6, " ")
            selection = TextRange(7)
            state.processInput(this)
        }

        // Type "@" (so text is "@Alice @", creating activeTrigger)
        state.textFieldState.edit {
            replace(7, 7, "@")
            selection = TextRange(8)
            state.processInput(this)
        }

        // Verify the completed mention is preserved perfectly with its original ID "123"
        val mentionSpan = state.spans.find { it.style is MarkupStyle.Mention && it.style.id == "123" }
        assertEquals(0, mentionSpan?.start)
        assertEquals(6, mentionSpan?.end)

        // Verify that the new trigger span also exists with empty ID
        val triggerSpan = state.spans.find { it.style is MarkupStyle.Mention && it.style.id.isEmpty() }
        assertEquals(7, triggerSpan?.start)
        assertEquals(8, triggerSpan?.end)
    }

    @Test
    fun `typing characters inside a link does not create duplicate nested link spans`() {
        val state = HyphenTextState("[Links](https://github.com/densermeerkat/hyphen)")
        state.isFocused = true

        // Initial link check: span is at 0..5 ("Links")
        assertEquals(1, state.spans.size)
        assertTrue(state.spans[0].style is MarkupStyle.Link)
        assertEquals(0, state.spans[0].start)
        assertEquals(5, state.spans[0].end)

        // Place cursor at index 2 ("Li|nks") and type "a"
        state.textFieldState.edit {
            replace(2, 2, "a")
            selection = TextRange(3)
            state.processInput(this)
        }

        // Verify the text is "Lianks" and we still have exactly ONE link span spanning 0..6
        assertEquals("Lianks", state.text)
        val linkSpans = state.spans.filter { it.style is MarkupStyle.Link }
        assertEquals(1, linkSpans.size)
        assertEquals(0, linkSpans[0].start)
        assertEquals(6, linkSpans[0].end)
        assertEquals("https://github.com/densermeerkat/hyphen", (linkSpans[0].style as MarkupStyle.Link).url)
    }

    @Test
    fun `dragging selection over a completed mention does not delete it when non-breaking spaces are present`() {
        val triggerConfigs = listOf(TriggerConfig(trigger = "@", scheme = "mention"))
        val state = HyphenTextState("", triggerConfigs)
        state.isFocused = true

        // Complete a mention with a trailing non-breaking space (so text is "@Alice\u00A0")
        state.textFieldState.edit {
            replace(0, 0, "@Al")
            selection = TextRange(3)
            state.processInput(this)
        }
        state.completeMention("123", "@Alice") // Replaces "@Al" with "@Alice\u00A0"

        assertEquals("@Alice\u00A0", state.textFieldState.text.toString())
        assertEquals(1, state.spans.size)

        // Simulate dragging selection: change selection to select the mention (0..5) without text changes
        state.textFieldState.edit {
            selection = TextRange(0, 5)
            state.processInput(this)
        }

        // Verify the mention was NOT deleted
        assertEquals("@Alice\u00A0", state.textFieldState.text.toString())
        assertEquals(1, state.spans.size)
    }

    @Test
    fun `deleting a selection enclosing multiple mentions deletes them naturally without reverting`() {
        val triggerConfigs = listOf(TriggerConfig(trigger = "@", scheme = "mention"))
        val state = HyphenTextState("Hello [@Alice](mention:123) and [@Bob](mention:456)!", triggerConfigs)
        state.isFocused = true

        // Initial check: clean text is "Hello @Alice and @Bob!" (length 22)
        assertEquals("Hello @Alice and @Bob!", state.text)
        assertEquals(2, state.spans.filter { it.style is MarkupStyle.Mention }.size)

        // Select the entire text and delete it
        state.textFieldState.edit {
            replace(0, length, "")
            selection = TextRange(0)
            state.processInput(this)
        }

        // Verify the entire text is deleted successfully and no mentions remain (no revert occurred)
        assertEquals("", state.text)
        assertTrue(state.spans.none { it.style is MarkupStyle.Mention })
    }
}