package com.denser.hyphen.state

import androidx.compose.ui.text.TextRange

internal class SelectionManager {
    private var lastValidSelection: TextRange = TextRange.Zero
    var isFocused: Boolean = false

    fun onSelectionChanged(current: TextRange) {
        if (isFocused && current.start != current.end) {
            lastValidSelection = current
        } else if (!isFocused) {
            clear()
        }
    }

    fun resolve(current: TextRange): Pair<Int, Int> {
        val effective = effectiveSelection(current)
        return minOf(effective.start, effective.end) to maxOf(effective.start, effective.end)
    }

    fun effectiveSelection(current: TextRange): TextRange =
        if (!isFocused && current.start == current.end && lastValidSelection.start != lastValidSelection.end) {
            lastValidSelection
        } else current

    fun clear() {
        lastValidSelection = TextRange.Zero
    }
}