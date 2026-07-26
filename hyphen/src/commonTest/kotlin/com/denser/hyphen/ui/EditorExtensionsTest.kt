package com.denser.hyphen.ui.internal

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.insert
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.ui.text.TextRange
import com.denser.hyphen.state.HyphenTextState
import com.denser.hyphen.ui.internal.processMarkdownInput
import com.denser.hyphen.model.MarkupStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class EditorExtensionsTest {

    @Test
    fun testProcessMarkdownInput_softEnterDetectsNewline() {
        val state = HyphenTextState(initialText = "- Item 1")
        state.textFieldState.setTextAndPlaceCursorAtEnd("- Item 1")

        val bufferState = TextFieldState("- Item 1")

        bufferState.edit {
            insert(length, "\n")
            processMarkdownInput(state = state, buffer = this)
        }

        val resultingText = bufferState.text.toString()
        assertTrue(
            resultingText.contains("- Item 1\n-\u00A0"),
            "Expected SmartEnter to intercept the newline and insert a new bullet point, but got: $resultingText"
        )
    }

    @Test
    fun testProcessMarkdownInput_softEnterDetectsNewline_withMobileMultiChanges() {
        val state = HyphenTextState(initialText = "- Item 1")
        state.textFieldState.setTextAndPlaceCursorAtEnd("- Item 1")

        val bufferState = TextFieldState("- Item 1")

        bufferState.edit {
            replace(2, 6, "Item")
            insert(length, "\n")
            processMarkdownInput(state = state, buffer = this)
        }

        val resultingText = bufferState.text.toString()
        assertTrue(
            resultingText.contains("- Item 1\n-\u00A0"),
            "Expected SmartEnter to intercept the newline and insert a new bullet point, but got: $resultingText"
        )
    }

    @Test
    fun testProcessMarkdownInput_softEnterOnUncheckedCheckbox_insertsNewUncheckedCheckbox() {
        val text = "- [ ] Task 1"
        val state = HyphenTextState(initialText = text)
        state.textFieldState.setTextAndPlaceCursorAtEnd(text)

        val bufferState = TextFieldState(text)

        bufferState.edit {
            insert(length, "\n")
            processMarkdownInput(state = state, buffer = this)
        }

        val resultingText = bufferState.text.toString()
        assertEquals(
            "- [ ] Task 1\n- [ ]\u00A0",
            resultingText,
            "Expected SmartEnter to append a new unchecked checkbox."
        )
    }

    @Test
    fun testProcessMarkdownInput_softEnterOnCheckedCheckbox_insertsNewUncheckedCheckbox() {
        val text = "- [x] Done task"
        val state = HyphenTextState(initialText = text)
        state.textFieldState.setTextAndPlaceCursorAtEnd(text)

        val bufferState = TextFieldState(text)

        bufferState.edit {
            insert(length, "\n")
            processMarkdownInput(state = state, buffer = this)
        }

        val resultingText = bufferState.text.toString()
        assertEquals(
            "- [x] Done task\n- [ ]\u00A0",
            resultingText,
            "Expected SmartEnter on a checked task to append a new UNCHECKED checkbox."
        )
    }

    @Test
    fun testProcessMarkdownInput_softEnterOnEmptyCheckbox_removesCheckboxPrefix() {
        // Simulating the user hitting enter on a line that only contains a checkbox
        val text = "- [ ] "
        val state = HyphenTextState(initialText = text)
        state.textFieldState.setTextAndPlaceCursorAtEnd(text)

        val bufferState = TextFieldState(text)

        bufferState.edit {
            insert(length, "\n")
            processMarkdownInput(state = state, buffer = this)
        }

        val resultingText = bufferState.text.toString()
        assertEquals(
            "",
            resultingText,
            "Expected SmartEnter on an empty checkbox to remove the prefix and exit the list."
        )
    }

    @Test
    fun testProcessMarkdownInput_softEnterOnOrderedList_incrementsNumber() {
        val text = "1. First step"
        val state = HyphenTextState(initialText = text)
        state.textFieldState.setTextAndPlaceCursorAtEnd(text)

        val bufferState = TextFieldState(text)

        bufferState.edit {
            insert(length, "\n")
            processMarkdownInput(state = state, buffer = this)
        }

        val resultingText = bufferState.text.toString()
        assertEquals(
            "1. First step\n2.\u00A0",
            resultingText,
            "Expected SmartEnter to increment the ordered list number to 2."
        )
    }

    @Test
    fun testProcessMarkdownInput_softEnterOnEmptyBulletList_removesPrefix() {
        val text = "- "
        val state = HyphenTextState(initialText = text)
        state.textFieldState.setTextAndPlaceCursorAtEnd(text)

        val bufferState = TextFieldState(text)

        bufferState.edit {
            insert(length, "\n")
            processMarkdownInput(state = state, buffer = this)
        }

        val resultingText = bufferState.text.toString()
        assertEquals(
            "",
            resultingText,
            "Expected SmartEnter on an empty bullet item to exit the list."
        )
    }

    @Test
    fun testProcessMarkdownInput_softEnterOnBlockquote_appendsNewBlockquotePrefix() {
        val text = "> Quote"
        val state = HyphenTextState(initialText = text)
        state.textFieldState.setTextAndPlaceCursorAtEnd(text)

        val bufferState = TextFieldState(text)

        bufferState.edit {
            insert(length, "\n")
            processMarkdownInput(state = state, buffer = this)
        }

        val resultingText = bufferState.text.toString()
        assertEquals(
            "> Quote\n>\u00A0",
            resultingText,
            "Expected SmartEnter to append a new blockquote prefix."
        )
    }

    @Test
    fun testProcessMarkdownInput_softEnterOnEmptyBlockquote_removesBlockquotePrefix() {
        val text = "> "
        val state = HyphenTextState(initialText = text)
        state.textFieldState.setTextAndPlaceCursorAtEnd(text)

        val bufferState = TextFieldState(text)

        bufferState.edit {
            insert(length, "\n")
            processMarkdownInput(state = state, buffer = this)
        }

        val resultingText = bufferState.text.toString()
        assertEquals(
            "",
            resultingText,
            "Expected SmartEnter on an empty blockquote to remove prefix."
        )
    }

    @Test
    fun testProcessMarkdownInput_backspaceOnEmptyBlockquotePrefix_clearsEntirePrefix() {
        val text = "> "
        val state = HyphenTextState(initialText = text)
        state.textFieldState.setTextAndPlaceCursorAtEnd(text) // cursor at index 2

        val bufferState = TextFieldState(">") // simulate deleting the space

        bufferState.edit {
            processMarkdownInput(state = state, buffer = this)
        }

        val resultingText = bufferState.text.toString()
        assertEquals(
            "",
            resultingText,
            "Expected Backspace on empty blockquote prefix to clear the rest of the prefix."
        )
    }

    @Test
    fun testProcessMarkdownInput_backspaceOnEmptyCheckboxPrefix_clearsEntirePrefix() {
        val text = "- [ ] "
        val state = HyphenTextState(initialText = text)
        state.textFieldState.setTextAndPlaceCursorAtEnd(text) // cursor at index 6

        val bufferState = TextFieldState("- [ ]") // simulate deleting the trailing space

        bufferState.edit {
            processMarkdownInput(state = state, buffer = this)
        }

        val resultingText = bufferState.text.toString()
        assertEquals(
            "",
            resultingText,
            "Expected Backspace on empty checkbox prefix to clear the rest of the prefix."
        )
    }

    @Test
    fun testProcessMarkdownInput_pasteBlockquote() {
        val state = HyphenTextState(initialText = "")
        val bufferState = TextFieldState("> asdaasdda")

        bufferState.edit {
            processMarkdownInput(state = state, buffer = this)
        }

        // Verify that the span is added
        val blockquoteSpan = state.spans.find { it.style is MarkupStyle.Blockquote }
        assertNotNull(blockquoteSpan, "Expected Blockquote span to be created upon pasting.")
        assertEquals(0, blockquoteSpan.start)
        assertEquals(11, blockquoteSpan.end)
    }

    @Test
    fun testProcessMarkdownInput_backspaceOnEmptyBlockquoteLineStart_deletesPrefix() {
        val initialText = "Hello\n> "
        val state = HyphenTextState(initialText = initialText)
        state.setMarkdown(initialText)
        state.textFieldState.edit {
            this.selection = TextRange(6) // cursor is at start of line 2 prefix
        }

        // Simulate deleting the newline before '> ' (which happens when cursor is visually at start of line 2)
        val bufferState = TextFieldState("Hello> ") // deleted '\n' at index 5

        bufferState.edit {
            processMarkdownInput(state = state, buffer = this)
        }

        // Verify that prefix is deleted and newline is restored
        assertEquals("Hello\n", bufferState.text.toString(), "Expected blockquote prefix to be deleted and newline restored.")
    }

    @Test
    fun testProcessMarkdownInput_softEnterOnHeading_doesNotCarryOverHeadingStyle() {
        val state = HyphenTextState("# Header")
        state.textFieldState.setTextAndPlaceCursorAtEnd("# Header")

        val bufferState = TextFieldState("# Header")

        bufferState.edit {
            insert(length, "\n")
            processMarkdownInput(state = state, buffer = this)
        }

        // The heading style H1 should only cover "# Header" (the first line), not the second line.
        val headingSpans = state.spans.filter { it.style is MarkupStyle.H1 }
        assertEquals(1, headingSpans.size)
        assertEquals(0, headingSpans[0].start)
        assertEquals(6, headingSpans[0].end)
    }
}