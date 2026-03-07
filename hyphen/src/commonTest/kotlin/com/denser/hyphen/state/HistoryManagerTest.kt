package com.denser.hyphen.state

import androidx.compose.ui.text.TextRange
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.Test

class HistoryManagerTest {

    private fun createSnapshot(text: String) = EditorSnapshot(
        text = text,
        selection = TextRange(text.length),
        spans = emptyList()
    )

    @Test
    fun `initial state cannot undo or redo`() {
        val manager = HistoryManager(debounceMillis = 0L)

        assertFalse(manager.canUndo)
        assertFalse(manager.canRedo)
        assertNull(manager.undo(createSnapshot("current")))
        assertNull(manager.redo(createSnapshot("current")))
    }

    @Test
    fun `saveSnapshot adds to history and enables undo`() {
        val manager = HistoryManager(debounceMillis = 0L)
        manager.saveSnapshot(createSnapshot("State 1"))

        assertTrue(manager.canUndo)
        assertFalse(manager.canRedo)
    }

    @Test
    fun `saveSnapshot ignores consecutive identical snapshots`() {
        val manager = HistoryManager(debounceMillis = 0L)
        val snapshot = createSnapshot("Same State")

        manager.saveSnapshot(snapshot)
        manager.saveSnapshot(snapshot) // Should be ignored
        manager.saveSnapshot(snapshot) // Should be ignored

        val currentState = createSnapshot("Current State")
        val previousState = manager.undo(currentState)

        assertEquals("Same State", previousState?.text)
        assertFalse(manager.canUndo)
    }

    @Test
    fun `saveSnapshot clears the redo stack`() {
        val manager = HistoryManager(debounceMillis = 0L)

        manager.saveSnapshot(createSnapshot("State 1"))
        val currentState = createSnapshot("State 2")
        manager.undo(currentState)

        assertTrue(manager.canRedo)

        manager.saveSnapshot(createSnapshot("State 3"))

        assertFalse(manager.canRedo)
    }

    @Test
    fun `history respects maxHistorySize and drops oldest snapshots`() {
        val maxSize = 3
        val manager = HistoryManager(maxHistorySize = maxSize, debounceMillis = 0L)

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

        assertEquals(maxSize, undoCount)
        assertEquals("State 3", current.text)
    }

    @Test
    fun `undo pushes current state to redo stack`() {
        val manager = HistoryManager(debounceMillis = 0L)
        manager.saveSnapshot(createSnapshot("State 1"))

        val currentState = createSnapshot("State 2")
        val undoneState = manager.undo(currentState)

        assertEquals("State 1", undoneState?.text)
        assertTrue(manager.canRedo)
    }

    @Test
    fun `redo pushes current state back to undo stack`() {
        val manager = HistoryManager(debounceMillis = 0L)
        manager.saveSnapshot(createSnapshot("State 1"))

        val state2 = createSnapshot("State 2")
        manager.undo(state2)

        val currentState = createSnapshot("State 1 (restored)")
        val redoneState = manager.redo(currentState)

        assertEquals("State 2", redoneState?.text)
        assertTrue(manager.canUndo)
    }

    @Test
    fun `saveSnapshot debounces rapid changes`() {
        // Set an artificially long debounce so rapid saves are guaranteed to be ignored
        val manager = HistoryManager(debounceMillis = 5000L)

        manager.saveSnapshot(createSnapshot("State 1"))
        manager.saveSnapshot(createSnapshot("State 2")) // Should be ignored by debounce

        val currentState = createSnapshot("Current State")
        val undoneState = manager.undo(currentState)

        assertEquals("State 1", undoneState?.text)
        assertFalse(manager.canUndo) // Only State 1 should exist
    }

    @Test
    fun `saveSnapshot with force bypasses debounce`() {
        val manager = HistoryManager(debounceMillis = 5000L)

        manager.saveSnapshot(createSnapshot("State 1"))
        // Force is true, so it must save despite the debounce timer
        manager.saveSnapshot(createSnapshot("State 2"), force = true)

        val currentState = createSnapshot("Current State")
        val undoneState = manager.undo(currentState)

        assertEquals("State 2", undoneState?.text)
    }

    @Test
    fun `clear wipes undo and redo stacks completely`() {
        val manager = HistoryManager(debounceMillis = 0L)

        manager.saveSnapshot(createSnapshot("State 1"))
        manager.undo(createSnapshot("State 2"))

        assertTrue(manager.canUndo || manager.canRedo)

        manager.clear()

        assertFalse(manager.canUndo)
        assertFalse(manager.canRedo)
    }
}