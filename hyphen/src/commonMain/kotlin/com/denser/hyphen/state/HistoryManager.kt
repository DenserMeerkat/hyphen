package com.denser.hyphen.state

import androidx.compose.ui.text.TextRange
import com.denser.hyphen.model.MarkupStyleRange

internal data class EditorSnapshot(
    val text: String,
    val selection: TextRange,
    val spans: List<MarkupStyleRange>
)

internal class HistoryManager(private val maxHistorySize: Int = 50) {
    private val undoStack = mutableListOf<EditorSnapshot>()
    private val redoStack = mutableListOf<EditorSnapshot>()

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    fun saveSnapshot(currentSnapshot: EditorSnapshot) {
        if (undoStack.lastOrNull() == currentSnapshot) return

        undoStack.add(currentSnapshot)
        redoStack.clear()

        if (undoStack.size > maxHistorySize) {
            undoStack.removeAt(0)
        }
    }

    fun undo(currentState: EditorSnapshot): EditorSnapshot? {
        if (undoStack.isEmpty()) return null

        redoStack.add(currentState)
        return undoStack.removeAt(undoStack.lastIndex)
    }

    fun redo(currentState: EditorSnapshot): EditorSnapshot? {
        if (redoStack.isEmpty()) return null
        if (undoStack.lastOrNull() != currentState) {
            undoStack.add(currentState)
        }
        return redoStack.removeAt(redoStack.lastIndex)
    }
}