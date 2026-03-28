package com.denser.hyphen.blocks.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import com.denser.hyphen.blocks.model.HyphenBlock
import com.denser.hyphen.blocks.model.textState

enum class NavDirection { Up, Down, Left, Right }

data class PendingFocusRequest(
    val id: String,
    val direction: NavDirection? = null,
    val xOffset: Float? = null,
    val cursorPosition: Int? = null
)

class BlockNavigationManager(
    private val blocks: () -> List<HyphenBlock>,
) {

    private val focusRequesters = mutableStateMapOf<String, FocusRequester>()

    fun getFocusRequester(id: String): FocusRequester =
        focusRequesters.getOrPut(id) { FocusRequester() }

    fun removeFocusRequester(id: String) {
        focusRequesters.remove(id)
    }

    fun removeStaleRequesters(activeIds: Set<String>) {
        focusRequesters.keys.filter { it !in activeIds }.forEach { focusRequesters.remove(it) }
    }


    var focusedBlockId: String? by mutableStateOf(null)
        internal set

    var pendingFocus: PendingFocusRequest? by mutableStateOf(null)
        private set

    fun requestFocusAfterLayout(id: String, cursorPosition: Int? = null) {
        pendingFocus = PendingFocusRequest(id = id, cursorPosition = cursorPosition)
    }

    fun requestFocus(request: PendingFocusRequest) {
        pendingFocus = request
    }

    fun clearPendingFocus() {
        pendingFocus = null
    }

    fun focusPreviousBlock(currentId: String, xOffset: Float? = null, direction: NavDirection? = null): Boolean {
        val list = blocks()
        val index = list.indexOfFirst { it.id == currentId }
        if (index <= 0) return false
        val target = (index - 1 downTo 0).firstOrNull { list[it].isFocusable } ?: return false
        val prevBlock = list[target]
        requestFocus(
            PendingFocusRequest(
                id = prevBlock.id,
                direction = direction,
                xOffset = xOffset,
                cursorPosition = if (direction == NavDirection.Left) prevBlock.textState?.text?.length else null
            )
        )
        return true
    }

    fun focusNextBlock(currentId: String, xOffset: Float? = null, direction: NavDirection? = null): Boolean {
        val list = blocks()
        val index = list.indexOfFirst { it.id == currentId }
        if (index == -1 || index >= list.size - 1) return false
        val target = (index + 1 until list.size).firstOrNull { list[it].isFocusable } ?: return false
        requestFocus(
            PendingFocusRequest(
                id = list[target].id,
                direction = direction,
                xOffset = xOffset,
                cursorPosition = if (direction == NavDirection.Right) 1 else null
            )
        )
        return true
    }
}