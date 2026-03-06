package com.denser.hyphen.state

import androidx.compose.ui.text.TextRange
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SelectionManagerTest {


    // isFocused
    @Test
    fun `isFocused defaults to false`() {
        val manager = SelectionManager()
        assertFalse(manager.isFocused)
    }

    @Test
    fun `isFocused can be set and read`() {
        val manager = SelectionManager()
        manager.isFocused = true
        assertTrue(manager.isFocused)

        manager.isFocused = false
        assertFalse(manager.isFocused)
    }


    // onSelectionChanged
    @Test
    fun `onSelectionChanged saves non-collapsed selection when focused`() {
        val manager = SelectionManager()
        manager.isFocused = true

        manager.onSelectionChanged(TextRange(0, 5))

        val (start, end) = manager.resolve(TextRange(2)) // collapsed cursor
        // lastValidSelection should be used since current is collapsed and unfocused... 
        // but isFocused is still true here so current wins — test resolve separately
        // Here we just verify the save happened by going unfocused
        manager.isFocused = false
        val (s, e) = manager.resolve(TextRange(2))
        assertEquals(0, s)
        assertEquals(5, e)
    }

    @Test
    fun `onSelectionChanged does not save collapsed selection when focused`() {
        val manager = SelectionManager()
        manager.isFocused = true

        manager.onSelectionChanged(TextRange(0, 5)) // valid selection saved
        manager.onSelectionChanged(TextRange(3))    // collapsed — should not overwrite

        manager.isFocused = false
        val (s, e) = manager.resolve(TextRange(2))
        assertEquals(0, s)
        assertEquals(5, e)
    }

    @Test
    fun `onSelectionChanged clears lastValidSelection when not focused`() {
        val manager = SelectionManager()
        manager.isFocused = true
        manager.onSelectionChanged(TextRange(0, 5)) // save a valid selection

        manager.isFocused = false
        manager.onSelectionChanged(TextRange(0, 5)) // called while unfocused — should clear

        // Now resolve with a collapsed cursor — lastValid was cleared so no fallback
        val (s, e) = manager.resolve(TextRange(2))
        assertEquals(2, s)
        assertEquals(2, e)
    }


    // resolve
    @Test
    fun `resolve returns current selection when focused`() {
        val manager = SelectionManager()
        manager.isFocused = true
        manager.onSelectionChanged(TextRange(0, 10)) // save a big selection

        // Even with a saved selection, focused state should return current
        val (s, e) = manager.resolve(TextRange(2, 7))
        assertEquals(2, s)
        assertEquals(7, e)
    }

    @Test
    fun `resolve returns current collapsed selection when focused`() {
        val manager = SelectionManager()
        manager.isFocused = true
        manager.onSelectionChanged(TextRange(0, 5))

        // Collapsed cursor while focused — should still return current, not fallback
        val (s, e) = manager.resolve(TextRange(3))
        assertEquals(3, s)
        assertEquals(3, e)
    }

    @Test
    fun `resolve falls back to lastValidSelection when unfocused and current is collapsed`() {
        val manager = SelectionManager()
        manager.isFocused = true
        manager.onSelectionChanged(TextRange(2, 8))

        manager.isFocused = false
        val (s, e) = manager.resolve(TextRange(0)) // collapsed after losing focus
        assertEquals(2, s)
        assertEquals(8, e)
    }

    @Test
    fun `resolve does not fall back when unfocused but current is non-collapsed`() {
        val manager = SelectionManager()
        manager.isFocused = true
        manager.onSelectionChanged(TextRange(0, 10))

        manager.isFocused = false
        val (s, e) = manager.resolve(TextRange(1, 4))
        assertEquals(1, s)
        assertEquals(4, e)
    }

    @Test
    fun `resolve returns collapsed when unfocused and no lastValidSelection exists`() {
        val manager = SelectionManager()
        // Never saved anything, never focused

        val (s, e) = manager.resolve(TextRange(3))
        assertEquals(3, s)
        assertEquals(3, e)
    }

    @Test
    fun `resolve normalizes reversed selection`() {
        val manager = SelectionManager()
        manager.isFocused = true

        val (s, e) = manager.resolve(TextRange(8, 2))
        assertEquals(2, s)
        assertEquals(8, e)
    }

    @Test
    fun `resolve normalizes reversed lastValidSelection fallback`() {
        val manager = SelectionManager()
        manager.isFocused = true
        manager.onSelectionChanged(TextRange(8, 2)) // reversed drag

        manager.isFocused = false
        val (s, e) = manager.resolve(TextRange(0))
        assertEquals(2, s)
        assertEquals(8, e)
    }


    // effectiveSelection
    @Test
    fun `effectiveSelection returns current when focused`() {
        val manager = SelectionManager()
        manager.isFocused = true
        manager.onSelectionChanged(TextRange(0, 10))

        val effective = manager.effectiveSelection(TextRange(2, 5))
        assertEquals(TextRange(2, 5), effective)
    }

    @Test
    fun `effectiveSelection returns lastValid when unfocused and current is collapsed`() {
        val manager = SelectionManager()
        manager.isFocused = true
        manager.onSelectionChanged(TextRange(1, 6))

        manager.isFocused = false
        val effective = manager.effectiveSelection(TextRange(0))
        assertEquals(TextRange(1, 6), effective)
    }

    @Test
    fun `effectiveSelection returns current when unfocused but current is non-collapsed`() {
        val manager = SelectionManager()
        manager.isFocused = true
        manager.onSelectionChanged(TextRange(0, 10))

        manager.isFocused = false
        val effective = manager.effectiveSelection(TextRange(2, 7))
        assertEquals(TextRange(2, 7), effective)
    }

    @Test
    fun `effectiveSelection returns current when unfocused and no lastValid saved`() {
        val manager = SelectionManager()
        // No focus, no saved selection

        val effective = manager.effectiveSelection(TextRange(3))
        assertEquals(TextRange(3), effective)
    }


    // clear
    @Test
    fun `clear removes lastValidSelection so resolve no longer falls back`() {
        val manager = SelectionManager()
        manager.isFocused = true
        manager.onSelectionChanged(TextRange(0, 5))

        manager.isFocused = false
        manager.clear()

        val (s, e) = manager.resolve(TextRange(2))
        assertEquals(2, s)
        assertEquals(2, e)
    }

    @Test
    fun `clear is a no-op when nothing was saved`() {
        val manager = SelectionManager()

        // Should not throw
        manager.clear()

        val (s, e) = manager.resolve(TextRange(3))
        assertEquals(3, s)
        assertEquals(3, e)
    }

    @Test
    fun `clear allows new selection to be saved afterwards`() {
        val manager = SelectionManager()
        manager.isFocused = true
        manager.onSelectionChanged(TextRange(0, 5))

        manager.clear()

        manager.onSelectionChanged(TextRange(2, 9))
        manager.isFocused = false

        val (s, e) = manager.resolve(TextRange(0))
        assertEquals(2, s)
        assertEquals(9, e)
    }
}