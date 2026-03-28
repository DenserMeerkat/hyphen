package com.denser.hyphen.blocks.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

internal class BlockHistoryManager(private val maxDepth: Int = 100) {

    private val undoStack = ArrayDeque<BlockDocumentSnapshot>()
    private val redoStack = ArrayDeque<BlockDocumentSnapshot>()

    var canUndo by mutableStateOf(false)
        private set

    var canRedo by mutableStateOf(false)
        private set

    private fun updateState() {
        canUndo = undoStack.isNotEmpty()
        canRedo = redoStack.isNotEmpty()
    }

    fun saveSnapshot(snapshot: BlockDocumentSnapshot) {
        if (undoStack.lastOrNull() == snapshot) return
        undoStack.addLast(snapshot)
        if (undoStack.size > maxDepth) undoStack.removeFirst()
        redoStack.clear()
        updateState()
    }

    fun undo(current: BlockDocumentSnapshot): BlockDocumentSnapshot? {
        if (undoStack.isEmpty()) return null
        redoStack.addLast(current)
        if (redoStack.size > maxDepth) redoStack.removeFirst()
        val item = undoStack.removeLast()
        updateState()
        return item
    }

    fun redo(current: BlockDocumentSnapshot): BlockDocumentSnapshot? {
        if (redoStack.isEmpty()) return null
        undoStack.addLast(current)
        if (undoStack.size > maxDepth) undoStack.removeFirst()
        val item = redoStack.removeLast()
        updateState()
        return item
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
        updateState()
    }
}