package com.denser.hyphen.state

import androidx.compose.ui.text.TextRange
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SelectionManagerTest {

    private fun manager(focused: Boolean = true, saved: TextRange? = null) =
        SelectionManager().apply {
            isFocused = focused
            saved?.let { onSelectionChanged(it) }
        }

    @Test
    fun `isFocused updates correctly`() {
        val manager = SelectionManager()
        assertFalse(manager.isFocused)

        manager.isFocused = true
        assertTrue(manager.isFocused)
    }

    @Test
    fun `onSelectionChanged memory lifecycle`() {
        // 1. Saves valid selection when focused
        val m1 = manager(focused = true, saved = TextRange(0, 5))
        m1.isFocused = false
        assertEquals(0 to 5, m1.resolve(TextRange(2)))

        // 2. Clears memory on collapsed cursor while focused
        val m2 = manager(focused = true, saved = TextRange(0, 5))
        m2.onSelectionChanged(TextRange(3))
        m2.isFocused = false
        assertEquals(3 to 3, m2.resolve(TextRange(3)))

        // 3. Ignores changes and protects memory when unfocused
        val m3 = manager(focused = true, saved = TextRange(0, 5))
        m3.isFocused = false
        m3.onSelectionChanged(TextRange(2))
        assertEquals(0 to 5, m3.resolve(TextRange(2)))
    }

    @Test
    fun `resolve returns correct range and normalizes reversed selections`() {
        val m = manager(focused = true, saved = TextRange(2, 8))

        // When Focused: Always trusts current selection
        assertEquals(1 to 4, m.resolve(TextRange(1, 4)))
        assertEquals(3 to 3, m.resolve(TextRange(3)))

        // When Unfocused: Rescues memory ONLY if current is collapsed
        m.isFocused = false
        assertEquals(2 to 8, m.resolve(TextRange(0))) // Memory rescued
        assertEquals(1 to 4, m.resolve(TextRange(1, 4))) // Current non-collapsed wins

        // No memory exists: Returns current
        assertEquals(3 to 3, manager(focused = false).resolve(TextRange(3)))

        // Normalizes reversed ranges seamlessly
        assertEquals(2 to 8, m.resolve(TextRange(8, 2)))
    }

    @Test
    fun `effectiveSelection returns correct TextRange object`() {
        val m = manager(focused = true, saved = TextRange(1, 6))

        assertEquals(TextRange(2, 5), m.effectiveSelection(TextRange(2, 5))) // Focused

        m.isFocused = false
        assertEquals(TextRange(1, 6), m.effectiveSelection(TextRange(0))) // Rescued
        assertEquals(TextRange(2, 7), m.effectiveSelection(TextRange(2, 7))) // Current wins
        assertEquals(TextRange(3), manager(focused = false).effectiveSelection(TextRange(3))) // No memory
    }

    @Test
    fun `clear wipes memory but allows future saves`() {
        val m = manager(focused = true, saved = TextRange(0, 5))

        m.clear() // Wipe it
        m.isFocused = false
        assertEquals(2 to 2, m.resolve(TextRange(2))) // Falls back to current

        // Prove it can save again
        m.isFocused = true
        m.onSelectionChanged(TextRange(2, 9))
        m.isFocused = false
        assertEquals(2 to 9, m.resolve(TextRange(0)))
    }
}