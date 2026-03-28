package com.denser.hyphen.blocks.state

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.insert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.TextRange
import com.denser.hyphen.blocks.model.BlockquoteBlock
import com.denser.hyphen.blocks.model.BulletListBlock
import com.denser.hyphen.blocks.model.CheckboxBlock
import com.denser.hyphen.blocks.model.DividerBlock
import com.denser.hyphen.blocks.model.HyphenBlock
import com.denser.hyphen.blocks.model.OrderedListBlock
import com.denser.hyphen.blocks.model.TextBlock
import com.denser.hyphen.blocks.model.spans
import com.denser.hyphen.blocks.model.textState
import com.denser.hyphen.blocks.model.withSpans
import com.denser.hyphen.core.model.StyleRange
import com.denser.hyphen.core.constants.EditorConstants
import com.denser.hyphen.inline.state.HyphenInlineState
import com.denser.hyphen.blocks.state.NavDirection
import com.denser.hyphen.blocks.state.PendingFocusRequest

class HyphenBlockState(
    initialBlocks: List<HyphenBlock> = emptyList(),
) {
    
    internal val _blocks = mutableStateListOf<HyphenBlock>().apply {
        if (initialBlocks.isEmpty()) add(TextBlock(state = TextFieldState(EditorConstants.ZWSP)))
        else addAll(initialBlocks)
    }

    val blocks: List<HyphenBlock> get() = _blocks

    private val historyManager = BlockHistoryManager()

    val conversions = BlockConversionManager(this)

    val navigation = BlockNavigationManager(blocks = { _blocks })

    val canUndo: Boolean get() = historyManager.canUndo
    val canRedo: Boolean get() = historyManager.canRedo

    private var isRestoringSnapshot = false

    var focusedBlockId: String?
        get() = navigation.focusedBlockId
        internal set(value) { navigation.focusedBlockId = value }

    val pendingFocus: PendingFocusRequest? get() = navigation.pendingFocus

    fun getFocusRequester(id: String): FocusRequester = navigation.getFocusRequester(id)
    fun clearPendingFocus() = navigation.clearPendingFocus()
    fun requestFocusAfterLayout(id: String, cursorPosition: Int? = null) =
        navigation.requestFocusAfterLayout(id, cursorPosition)
    fun focusPreviousBlock(currentId: String, xOffset: Float? = null, direction: NavDirection? = null): Boolean = 
        navigation.focusPreviousBlock(currentId, xOffset, direction)
    fun focusNextBlock(currentId: String, xOffset: Float? = null, direction: NavDirection? = null): Boolean = 
        navigation.focusNextBlock(currentId, xOffset, direction)


    private val inlineStateRegistry = mutableStateMapOf<String, HyphenInlineState>()

    internal fun registerInlineState(blockId: String, state: HyphenInlineState) {
        inlineStateRegistry[blockId] = state
    }

    internal fun unregisterInlineState(blockId: String) {
        inlineStateRegistry.remove(blockId)
    }

    fun getInlineState(blockId: String): HyphenInlineState? = inlineStateRegistry[blockId]


    fun updateBlockSpans(blockId: String, spans: List<StyleRange>) {
        if (isRestoringSnapshot) return
        val index = _blocks.indexOfFirst { it.id == blockId }
        if (index == -1) return
        _blocks[index] = _blocks[index].withSpans(spans)
    }


    private fun currentSnapshot(): BlockDocumentSnapshot =
        BlockDocumentSnapshot(_blocks.map { it.toSnapshot(EditorConstants.ZWSP) })

    fun saveSnapshot() {
        if (isRestoringSnapshot) return
        historyManager.saveSnapshot(currentSnapshot())
    }

    private fun restoreSnapshot(snapshot: BlockDocumentSnapshot) {
        isRestoringSnapshot = true

        val currentMap = _blocks.associateBy { it.id }
        val restoredBlocks = snapshot.blocks.map { snap ->
            val existing = currentMap[snap.id]
            if (existing != null && snap::class == existing.snapshotClass()) {
                existing.textState?.edit { replace(0, length, EditorConstants.ZWSP + snap.textContent()) }
                when {
                    existing is CheckboxBlock && snap is CheckboxBlockSnapshot ->
                        existing.copy(isChecked = snap.isChecked, spans = snap.spans)
                    existing is TextBlock && snap is TextBlockSnapshot ->
                        existing.copy(type = snap.type, spans = snap.spans)
                    else -> existing.withSpans(snap.spans)
                }
            } else {
                snap.toBlock(EditorConstants.ZWSP)
            }
        }

        _blocks.clear()
        _blocks.addAll(restoredBlocks)
        navigation.removeStaleRequesters(restoredBlocks.map { it.id }.toSet())

        isRestoringSnapshot = false
    }

    fun undo() {
        val previous = historyManager.undo(currentSnapshot()) ?: return
        restoreSnapshot(previous)
    }

    fun redo() {
        val next = historyManager.redo(currentSnapshot()) ?: return
        restoreSnapshot(next)
    }

    fun splitBlock(blockId: String, cursorPosition: Int) {
        val index = _blocks.indexOfFirst { it.id == blockId }
        if (index == -1) return
        saveSnapshot()

        val currentBlock = _blocks[index]
        val state = currentBlock.textState ?: return
        val adjustedCursor = (cursorPosition - 1).coerceAtLeast(0)
        val cleanText = state.text.toString().removePrefix(EditorConstants.ZWSP)

        if (cleanText.isEmpty() && currentBlock !is TextBlock) {
            val newBlock = TextBlock(state = TextFieldState(EditorConstants.ZWSP))
            _blocks[index] = newBlock
            requestFocusAfterLayout(newBlock.id, cursorPosition = 1)
            return
        }

        val textToKeep = cleanText.take(adjustedCursor)
        val textToMove = cleanText.substring(adjustedCursor)
        state.edit { replace(0, length, EditorConstants.ZWSP + textToKeep) }

        val existingSpans = currentBlock.spans
        val spansToKeep = existingSpans.filter { it.end <= adjustedCursor }
        val spansToMove = existingSpans
            .filter { it.start >= adjustedCursor }
            .map { it.copy(start = it.start - adjustedCursor, end = it.end - adjustedCursor) }

        _blocks[index] = currentBlock.withSpans(spansToKeep)

        val newBlock: HyphenBlock = when (currentBlock) {
            is TextBlock -> TextBlock(state = TextFieldState(EditorConstants.ZWSP + textToMove), spans = spansToMove)
            is CheckboxBlock -> CheckboxBlock(state = TextFieldState(EditorConstants.ZWSP + textToMove), spans = spansToMove)
            is BulletListBlock -> BulletListBlock(state = TextFieldState(EditorConstants.ZWSP + textToMove), spans = spansToMove)
            is OrderedListBlock -> OrderedListBlock(state = TextFieldState(EditorConstants.ZWSP + textToMove), startNumber = currentBlock.startNumber, spans = spansToMove)
            is BlockquoteBlock -> BlockquoteBlock(state = TextFieldState(EditorConstants.ZWSP + textToMove), spans = spansToMove)
            is DividerBlock -> TextBlock(state = TextFieldState(EditorConstants.ZWSP + textToMove))
        }

        _blocks.add(index + 1, newBlock)
        requestFocusAfterLayout(newBlock.id, cursorPosition = 1)
    }

    fun mergeWithPrevious(blockId: String) {
        val index = _blocks.indexOfFirst { it.id == blockId }
        if (index <= 0) return
        saveSnapshot()

        val currentBlock = _blocks[index]
        val prevBlock = _blocks[index - 1]
        val prevState = prevBlock.textState ?: return
        val textToAppend = currentBlock.textState?.text?.toString()?.removePrefix(EditorConstants.ZWSP) ?: return

        val oldLength = prevState.text.length
        val mergeIndex = oldLength - 1
        prevState.edit {
            insert(oldLength, textToAppend)
            selection = TextRange(oldLength)
        }

        val shiftedSpans = currentBlock.spans.map {
            it.copy(start = it.start + mergeIndex, end = it.end + mergeIndex)
        }
        _blocks[index - 1] = prevBlock.withSpans(prevBlock.spans + shiftedSpans)
        _blocks.removeAt(index)
        navigation.removeFocusRequester(blockId)
        requestFocusAfterLayout(prevBlock.id, cursorPosition = mergeIndex + 1)
    }

    fun toggleCheckbox(blockId: String) {
        val index = _blocks.indexOfFirst { it.id == blockId }
        if (index == -1) return
        val block = _blocks[index]
        if (block is CheckboxBlock) {
            saveSnapshot()
            _blocks[index] = block.copy(isChecked = !block.isChecked)
        }
    }

    fun moveBlock(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        if (fromIndex !in _blocks.indices || toIndex !in _blocks.indices) return
        saveSnapshot()
        val block = _blocks.removeAt(fromIndex)
        _blocks.add(toIndex, block)
    }

    fun orderedListNumber(index: Int): Int {
        val block = _blocks.getOrNull(index) as? OrderedListBlock ?: return 1
        var runStart = index
        for (i in index - 1 downTo 0) {
            if (_blocks[i] is OrderedListBlock) runStart = i else break
        }
        val firstBlock = _blocks[runStart] as? OrderedListBlock
        val base = firstBlock?.startNumber ?: 1
        return base + (index - runStart)
    }
}

@Composable
fun rememberHyphenBlockState(
    initialBlocks: List<HyphenBlock> = emptyList(),
): HyphenBlockState = remember { HyphenBlockState(initialBlocks) }