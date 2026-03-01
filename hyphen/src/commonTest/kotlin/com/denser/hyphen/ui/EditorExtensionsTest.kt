package com.denser.hyphen.ui

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import com.denser.hyphen.state.HyphenTextState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EditorExtensionsTest {

    @Test
    fun testProcessMarkdownInput_triggersOnValueChange() {
        val state = HyphenTextState(initialText = "Hello")
        val bufferState = TextFieldState("Hello")
        var emittedValue = ""

        bufferState.edit {
            replace(0, length, "Hello World")

            processMarkdownInput(
                state = state,
                onValueChange = { emittedValue = it },
                buffer = this
            )
        }

        assertEquals("Hello World", emittedValue)
    }

    @Test
    fun testProcessMarkdownInput_softEnterDetectsNewline() {
        val state = HyphenTextState(initialText = "- Item 1")
        state.textFieldState.setTextAndPlaceCursorAtEnd("- Item 1")

        val bufferState = TextFieldState("- Item 1")

        bufferState.edit {
            replace(length, length, "\n")

            processMarkdownInput(
                state = state,
                onValueChange = null,
                buffer = this
            )
        }

        val resultingText = bufferState.text.toString()
        assertTrue(
            resultingText.contains("- Item 1\n- "),
            "Expected SmartEnter to intercept the newline and insert a new bullet point, but got: $resultingText"
        )
    }
}