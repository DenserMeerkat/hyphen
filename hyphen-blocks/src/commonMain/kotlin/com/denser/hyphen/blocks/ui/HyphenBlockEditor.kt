package com.denser.hyphen.blocks.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.denser.hyphen.blocks.model.BlockquoteBlock
import com.denser.hyphen.blocks.model.BulletListBlock
import com.denser.hyphen.blocks.model.CheckboxBlock
import com.denser.hyphen.blocks.model.DividerBlock
import com.denser.hyphen.blocks.model.OrderedListBlock
import com.denser.hyphen.blocks.model.TextBlock
import com.denser.hyphen.blocks.state.HyphenBlockState
import com.denser.hyphen.blocks.ui.blocks.BlockquoteBlock
import com.denser.hyphen.blocks.ui.blocks.BulletListBlock
import com.denser.hyphen.blocks.ui.blocks.CheckboxBlock
import com.denser.hyphen.blocks.ui.blocks.OrderedListBlock
import com.denser.hyphen.blocks.ui.blocks.TextBlock
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull

@Composable
fun HyphenBlockEditor(
    state: HyphenBlockState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(vertical = 8.dp),
    debugBorder: Boolean = false,
) {
    val listState = rememberLazyListState()
    val debugModifier = if (debugBorder) Modifier.border(1.dp, Color.Red) else Modifier

    LaunchedEffect(listState) {
        snapshotFlow { state.pendingFocus?.id }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { pendingId ->
                val index = state.blocks.indexOfFirst { it.id == pendingId }
                if (index == -1) return@collect
                val layoutInfo = listState.layoutInfo
                val visibleItems = layoutInfo.visibleItemsInfo
                val isVisible = visibleItems.any { it.index == index }
                val isPartiallyHidden = visibleItems.any { item ->
                    item.index == index &&
                            (item.offset < 0 || item.offset + item.size > layoutInfo.viewportEndOffset)
                }
                if (!isVisible || isPartiallyHidden) {
                    listState.scrollToItem(index)
                }
            }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize().then(debugModifier),
        contentPadding = contentPadding,
    ) {
        itemsIndexed(
            items = state.blocks,
            key = { _, block -> block.id },
        ) { index, block ->
            when (block) {
                is TextBlock -> TextBlock(
                    block = block,
                    blockState = state,
                )
                is CheckboxBlock -> CheckboxBlock(
                    block = block,
                    blockState = state,
                )
                is BulletListBlock -> BulletListBlock(
                    block = block,
                    blockState = state,
                )
                is OrderedListBlock -> OrderedListBlock(
                    block = block,
                    index = index,
                    blockState = state,
                )
                is BlockquoteBlock -> BlockquoteBlock(
                    block = block,
                    blockState = state,
                )
                is DividerBlock -> HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }
}