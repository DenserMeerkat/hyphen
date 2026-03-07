package com.denser.hyphen.state

import androidx.compose.ui.text.TextRange
import com.denser.hyphen.model.MarkupStyleRange
import kotlin.time.TimeMark
import kotlin.time.TimeSource

internal data class EditorSnapshot(
    val text: String,
    val selection: TextRange,
    val spans: List<MarkupStyleRange>
)

internal class HistoryManager(
    private val maxHistorySize: Int = 50,
    private val debounceMillis: Long = 500L
) {
    private val undoStack = mutableListOf<EditorSnapshot>()
    private val redoStack = mutableListOf<EditorSnapshot>()
    private var lastSaveTime: TimeMark? = null

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    fun saveSnapshot(currentSnapshot: EditorSnapshot, force: Boolean = false) {
        val lastSnapshot = undoStack.lastOrNull()
        if (lastSnapshot == currentSnapshot) return

        val now = TimeSource.Monotonic.markNow()

        val isDebouncing = !force && lastSaveTime?.let {
            it.elapsedNow().inWholeMilliseconds < debounceMillis
        } ?: false

        if (isDebouncing) {
            lastSaveTime = now
            return
        }

        undoStack.add(currentSnapshot)
        redoStack.clear()
        lastSaveTime = now

        if (undoStack.size > maxHistorySize) {
            undoStack.removeAt(0)
        }
    }

    fun undo(currentState: EditorSnapshot): EditorSnapshot? {
        if (undoStack.isEmpty()) return null

        redoStack.add(currentState)
        val snapshot = undoStack.removeAt(undoStack.lastIndex)

        lastSaveTime = null
        return snapshot
    }

    fun redo(currentState: EditorSnapshot): EditorSnapshot? {
        if (redoStack.isEmpty()) return null

        if (undoStack.lastOrNull() != currentState) {
            undoStack.add(currentState)
        }
        val snapshot = redoStack.removeAt(redoStack.lastIndex)

        lastSaveTime = null
        return snapshot
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
        lastSaveTime = null
    }
}