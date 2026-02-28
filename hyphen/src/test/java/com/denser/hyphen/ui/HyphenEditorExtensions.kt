package com.denser.hyphen.ui

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.insert
import androidx.compose.ui.text.TextRange
import com.denser.hyphen.state.HyphenTextState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HyphenEditorExtensionsTest {

    @Test
    fun `processMarkdownInput detects soft enter and delegates to Smart Enter`() {
        // 1. Setup a state with an active bullet list
        val state = HyphenTextState("- Item 1")
        val bufferState = TextFieldState("- Item 1")

        // Ensure the state's selection matches the end of the text
        state.textFieldState.edit { this.selection = TextRange(8) }

        var callbackTriggered = false

        bufferState.edit {
            this.selection = TextRange(8)
            // 2. Simulate the user typing a newline character at the end
            this.insert(8, "\n")

            // 3. Process the input
            processMarkdownInput(
                state = state,
                onValueChange = { callbackTriggered = true },
                buffer = this
            )

            // 4. Verify Smart Enter hijacked the newline and added the prefix
            assertEquals("- Item 1\n- ", this.asCharSequence().toString())
        }

        assertTrue("onValueChange callback should be invoked", callbackTriggered)
    }

    @Test
    fun `processMarkdownInput allows standard newline if Smart Enter ignores it`() {
        // 1. Setup a state with plain text (no lists)
        val state = HyphenTextState("Plain text")
        val bufferState = TextFieldState("Plain text")

        state.textFieldState.edit { this.selection = TextRange(10) }

        bufferState.edit {
            this.selection = TextRange(10)
            // 2. Simulate typing a newline
            this.insert(10, "\n")

            processMarkdownInput(
                state = state,
                onValueChange = null,
                buffer = this
            )

            // 4. Verify the newline was inserted normally without prefixes
            assertEquals("Plain text\n", this.asCharSequence().toString())
        }
    }

    @Test
    fun `processMarkdownInput processes normal typing without intercepting`() {
        val state = HyphenTextState("Hello")
        val bufferState = TextFieldState("Hello")

        state.textFieldState.edit { this.selection = TextRange(5) }

        bufferState.edit {
            this.selection = TextRange(5)
            // Simulate typing a word, NOT a newline
            this.insert(5, " World")

            processMarkdownInput(
                state = state,
                onValueChange = null,
                buffer = this
            )

            assertEquals("Hello World", this.asCharSequence().toString())
        }
    }
}