package com.denser.hyphen.ui

import androidx.compose.ui.text.TextRange
import com.denser.hyphen.state.HyphenTextState
import com.denser.hyphen.ui.internal.copiedPlainText
import com.denser.hyphen.ui.internal.markdownForCopiedClip
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MarkdownCopyTest {

    private fun rewrite(
        state: HyphenTextState,
        clip: String?,
        selection: TextRange = TextRange(0, state.text.length),
        lastWrite: String? = null,
    ): String? = markdownForCopiedClip(clip, lastWrite, state.text, state.spans, selection)

    @Test
    fun `copied formatted selection is rewritten to markdown`() {
        val state = HyphenTextState("plain **bold** and *italic*")

        assertEquals("plain **bold** and *italic*", rewrite(state, "plain bold and italic"))
        assertEquals("**bold**", rewrite(state, "bold", TextRange(6, 10)))
        assertEquals("**bold**", rewrite(state, "bold", TextRange(10, 6)))
    }

    @Test
    fun `own markdown write is not rewritten again`() {
        val state = HyphenTextState("**bold** text")
        val markdown = rewrite(state, "bold text")

        assertEquals("**bold** text", markdown)
        assertNull(rewrite(state, markdown, lastWrite = markdown))
    }

    @Test
    fun `clip copied elsewhere is left alone`() {
        val state = HyphenTextState("**bold** text")

        assertNull(rewrite(state, "https://example.com"))
        assertNull(rewrite(state, "bold text and more"))
        assertNull(rewrite(state, null))
    }

    @Test
    fun `unformatted or invalid selection is left alone`() {
        val state = HyphenTextState("plain **bold**")

        assertNull(rewrite(state, "plain", TextRange(0, 5)))
        assertNull(rewrite(state, "", TextRange(2, 2)))
        assertNull(rewrite(state, "bold", TextRange(6, 99)))
    }

    @Test
    fun `checkbox prefixes match the collapsed copy`() {
        val state = HyphenTextState("- [ ] todo\n- [x] done")

        assertEquals("  todo\n  done", copiedPlainText(state.text, state.spans, 0, state.text.length))
        assertEquals("  todo", copiedPlainText(state.text, state.spans, 3, 10))
        assertEquals("- [ ] todo\n- [x] done", rewrite(state, "  todo\n  done"))
    }

    @Test
    fun `invisible characters in the copy are ignored`() {
        val heading = HyphenTextState("# Title\n> quote")
        assertEquals("# Title\n> quote", rewrite(heading, "​Title\n​quote"))

        val spaced = HyphenTextState("**a b**")
        assertEquals("**a b**", rewrite(spaced, "a b"))
    }
}
