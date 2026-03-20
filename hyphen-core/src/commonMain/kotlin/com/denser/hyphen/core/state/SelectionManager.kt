package com.denser.hyphen.core.state

import androidx.compose.ui.text.TextRange

/**
 * Remembers the last valid (non-collapsed) selection so that toolbar operations
 * fired after the field loses focus still act on the correct range.
 *
 * When a toolbar button is tapped on Desktop or Web, the text field loses focus
 * before the click is processed — which would collapse the selection to zero.
 * [SelectionManager] holds onto the last non-collapsed selection so style toggles
 * always act on the intended range.
 */
class SelectionManager {
    private var lastValidSelection: TextRange = TextRange.Zero
    var isFocused: Boolean = false

    fun onSelectionChanged(current: TextRange) {
        if (isFocused) {
            if (current.start != current.end) {
                lastValidSelection = current
            } else {
                clear()
            }
        }
    }

    fun resolve(current: TextRange): Pair<Int, Int> {
        val effective = effectiveSelection(current)
        return minOf(effective.start, effective.end) to maxOf(effective.start, effective.end)
    }

    fun effectiveSelection(current: TextRange): TextRange =
        if (!isFocused && current.start == current.end &&
            lastValidSelection.start != lastValidSelection.end
        ) {
            lastValidSelection
        } else current

    fun clear() {
        lastValidSelection = TextRange.Zero
    }
}