package com.denser.hyphen.state

import androidx.compose.ui.text.TextRange

internal class SelectionManager {
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

    fun resolve(current: TextRange, maxLength: Int): Pair<Int, Int> {
        val effective = effectiveSelection(current, maxLength)
        val start = effective.start.coerceIn(0, maxLength)
        val end = effective.end.coerceIn(0, maxLength)
        return minOf(start, end) to maxOf(start, end)
    }

    fun effectiveSelection(current: TextRange, maxLength: Int): TextRange {
        val base = if (!isFocused && current.start == current.end && lastValidSelection.start != lastValidSelection.end) {
            lastValidSelection
        } else current
        
        return TextRange(
            base.start.coerceIn(0, maxLength),
            base.end.coerceIn(0, maxLength)
        )
    }

    fun clear() {
        lastValidSelection = TextRange.Zero
    }
}