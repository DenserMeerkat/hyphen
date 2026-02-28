package com.denser.hyphen.state

import androidx.compose.ui.text.TextRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorHistoryManagerTest {

    private fun createSnapshot(text: String) = EditorSnapshot(
        text = text,
        selection = TextRange(text.length),
        spans = emptyList()
    )

    @Test
    fun `initial state cannot undo or redo`() {
        val manager = EditorHistoryManager()

        assertFalse(manager.canUndo)
        assertFalse(manager.canRedo)
        assertNull(manager.undo(createSnapshot("current")))
        assertNull(manager.redo(createSnapshot("current")))
    }

    @Test
    fun `saveSnapshot adds to history and enables undo`() {
        val manager = EditorHistoryManager()
        manager.saveSnapshot(createSnapshot("State 1"))

        assertTrue(manager.canUndo)
        assertFalse(manager.canRedo)
    }

    @Test
    fun `saveSnapshot ignores consecutive identical snapshots`() {
        val manager = EditorHistoryManager()
        val snapshot = createSnapshot("Same State")

        manager.saveSnapshot(snapshot)
        manager.saveSnapshot(snapshot) // Should be ignored
        manager.saveSnapshot(snapshot) // Should be ignored

        val currentState = createSnapshot("Current State")
        val previousState = manager.undo(currentState)

        assertEquals("Same State", previousState?.text)
        // If it saved duplicates, we could undo again, but it should be empty now
        assertFalse(manager.canUndo)
    }

    @Test
    fun `saveSnapshot clears the redo stack`() {
        val manager = EditorHistoryManager()

        // 1. Save state and then undo it to populate the redo stack
        manager.saveSnapshot(createSnapshot("State 1"))
        val currentState = createSnapshot("State 2")
        manager.undo(currentState)

        assertTrue(manager.canRedo)

        // 2. Typing new text (saving a new snapshot) should destroy the redo future
        manager.saveSnapshot(createSnapshot("State 3"))

        assertFalse(manager.canRedo)
    }

    @Test
    fun `history respects maxHistorySize and drops oldest snapshots`() {
        val maxSize = 3
        val manager = EditorHistoryManager(maxHistorySize = maxSize)

        // Save 5 states. It should only keep states 3, 4, and 5.
        manager.saveSnapshot(createSnapshot("State 1"))
        manager.saveSnapshot(createSnapshot("State 2"))
        manager.saveSnapshot(createSnapshot("State 3"))
        manager.saveSnapshot(createSnapshot("State 4"))
        manager.saveSnapshot(createSnapshot("State 5"))

        var undoCount = 0
        var current = createSnapshot("Current State")

        while (manager.canUndo) {
            val prev = manager.undo(current)
            if (prev != null) {
                current = prev
                undoCount++
            }
        }

        // It should only be able to undo exactly 3 times
        assertEquals(maxSize, undoCount)
        // The furthest back we can go should be State 3
        assertEquals("State 3", current.text)
    }

    @Test
    fun `undo pushes current state to redo stack`() {
        val manager = EditorHistoryManager()
        manager.saveSnapshot(createSnapshot("State 1"))

        val currentState = createSnapshot("State 2")
        val undoneState = manager.undo(currentState)

        assertEquals("State 1", undoneState?.text)
        assertTrue(manager.canRedo)
    }

    @Test
    fun `redo pushes current state back to undo stack`() {
        val manager = EditorHistoryManager()
        manager.saveSnapshot(createSnapshot("State 1"))

        val state2 = createSnapshot("State 2")
        manager.undo(state2) // Redo stack now contains State 2

        val currentState = createSnapshot("State 1 (restored)")
        val redoneState = manager.redo(currentState)

        assertEquals("State 2", redoneState?.text)
        assertTrue(manager.canUndo)
    }
}