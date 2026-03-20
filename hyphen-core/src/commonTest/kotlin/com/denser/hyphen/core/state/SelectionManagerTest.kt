package com.denser.hyphen.core.state

import androidx.compose.ui.text.TextRange
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SelectionManagerTest {

    /**
     * Helper to instantiate a SelectionManager in an exact state.
     * It temporarily forces [isFocused] to true to ensure the [savedSelection]
     * is successfully registered into memory before applying the final focus state.
     */
    private fun createManager(
        isFocused: Boolean = true,
        savedSelection: TextRange? = null
    ): SelectionManager {
        return SelectionManager().apply {
            this.isFocused = true
            savedSelection?.let { onSelectionChanged(it) }
            this.isFocused = isFocused
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // State & Memory Lifecycle Tests
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    fun `isFocused updates correctly`() {
        val manager = SelectionManager()
        assertFalse(manager.isFocused)

        manager.isFocused = true
        assertTrue(manager.isFocused)
    }

    @Test
    fun `onSelectionChanged saves valid selection when focused`() {
        val manager = createManager(isFocused = true, savedSelection = TextRange(0, 5))

        // Blur the field and provide a collapsed cursor.
        // It should rescue the memory (0 to 5).
        manager.isFocused = false
        assertEquals(0 to 5, manager.resolve(TextRange(2)))
    }

    @Test
    fun `onSelectionChanged clears memory when cursor collapses while focused`() {
        val manager = createManager(isFocused = true, savedSelection = TextRange(0, 5))

        // User clicks somewhere else in the text, collapsing the cursor to index 3
        manager.onSelectionChanged(TextRange(3))

        // Blur the field. The memory should be gone, falling back to the current index.
        manager.isFocused = false
        assertEquals(3 to 3, manager.resolve(TextRange(3)))
    }

    @Test
    fun `onSelectionChanged protects existing memory when unfocused`() {
        val manager = createManager(isFocused = true, savedSelection = TextRange(0, 5))

        manager.isFocused = false

        // Some background process or external event changes the selection while unfocused.
        // The manager should ignore this and protect the actual user's last selection.
        manager.onSelectionChanged(TextRange(2))

        assertEquals(0 to 5, manager.resolve(TextRange(2)))
    }

    @Test
    fun `clear wipes memory but allows future saves`() {
        val manager = createManager(isFocused = true, savedSelection = TextRange(0, 5))

        manager.clear()
        manager.isFocused = false

        // Memory is wiped, falls back to current
        assertEquals(2 to 2, manager.resolve(TextRange(2)))

        // Prove it can save again after being cleared
        manager.isFocused = true
        manager.onSelectionChanged(TextRange(2, 9))
        manager.isFocused = false

        assertEquals(2 to 9, manager.resolve(TextRange(0)))
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Resolution & Normalization Tests
    // ─────────────────────────────────────────────────────────────────────────────

    @Test
    fun `resolve always trusts current selection when focused`() {
        val manager = createManager(isFocused = true, savedSelection = TextRange(2, 8))

        // Even though memory exists, we are focused, so current selection wins.
        assertEquals(1 to 4, manager.resolve(TextRange(1, 4)))
        assertEquals(3 to 3, manager.resolve(TextRange(3)))
    }

    @Test
    fun `resolve rescues memory only when unfocused AND current selection is collapsed`() {
        val manager = createManager(isFocused = false, savedSelection = TextRange(2, 8))

        // Current is collapsed (0), so it rescues the memory (2 to 8)
        assertEquals(2 to 8, manager.resolve(TextRange(0)))

        // Current is NOT collapsed (1 to 4), so it assumes the user explicitly
        // dragged a new selection while unfocused (e.g. mouse drag), and current wins.
        assertEquals(1 to 4, manager.resolve(TextRange(1, 4)))
    }

    @Test
    fun `resolve normalizes reversed selections safely`() {
        val manager = createManager(isFocused = true)

        // TextRange(8, 2) means the user dragged from right to left.
        // resolve() should flip it so start is always <= end.
        assertEquals(2 to 8, manager.resolve(TextRange(8, 2)))
    }

    @Test
    fun `effectiveSelection returns exact TextRange object`() {
        val manager = createManager(isFocused = true, savedSelection = TextRange(1, 6))

        // Focused: Returns current
        assertEquals(TextRange(2, 5), manager.effectiveSelection(TextRange(2, 5)))

        manager.isFocused = false

        // Unfocused & Collapsed: Rescues memory
        assertEquals(TextRange(1, 6), manager.effectiveSelection(TextRange(0)))

        // Unfocused & Not Collapsed: Returns current
        assertEquals(TextRange(2, 7), manager.effectiveSelection(TextRange(2, 7)))

        // No memory exists: Returns current
        assertEquals(TextRange(3), createManager(isFocused = false).effectiveSelection(TextRange(3)))
    }
}