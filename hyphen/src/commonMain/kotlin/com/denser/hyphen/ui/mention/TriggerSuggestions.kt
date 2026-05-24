package com.denser.hyphen.ui.mention

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.denser.hyphen.model.TriggerState
import com.denser.hyphen.state.HyphenTextState

/**
 * A standard Material 3 autocomplete suggestions popup list.
 *
 * This composable connects with the editor's trigger state to handle mouse and keyboard navigation
 * (such as arrow keys to move highlights and Enter to select). It manages active selection state
 * on the hoisted [HyphenTextState] automatically.
 *
 * @param state The active [HyphenTextState] of the editor. Used to coordinate indices, count,
 *   and handle Enter-key autocomplete requests.
 * @param trigger The active [TriggerState] that represents the matched trigger (e.g. '@' or '#')
 *   and contains the user's typed search query.
 * @param items The list of filtering items ([SuggestionItem]) to display in the dropdown.
 * @param onSelect Callback invoked when a suggestion item is selected (either by mouse click or Enter key).
 * @param modifier Optional [Modifier] applied to the suggestion popup container.
 */
@Composable
fun TriggerSuggestions(
    state: HyphenTextState,
    trigger: TriggerState,
    items: List<SuggestionItem>,
    onSelect: (SuggestionItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    SideEffect {
        state.suggestionCount = items.size
    }

    LaunchedEffect(state.suggestionSelectionRequested) {
        if (state.suggestionSelectionRequested) {
            val selected = items.getOrNull(state.suggestionSelectedIndex)
            if (selected != null) {
                onSelect(selected)
            }
            state.suggestionSelectionRequested = false
        }
    }

    val lazyListState = rememberLazyListState()

    LaunchedEffect(state.suggestionSelectedIndex) {
        if (items.isNotEmpty()) {
            val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
            val isVisible = visibleItems.any { it.index == state.suggestionSelectedIndex }
            
            if (!isVisible || visibleItems.first().index == state.suggestionSelectedIndex || visibleItems.last().index == state.suggestionSelectedIndex) {
                lazyListState.animateScrollToItem(state.suggestionSelectedIndex)
            }
        }
    }

    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 3.dp,
        shadowElevation = 3.dp,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.widthIn(min = 160.dp, max = 280.dp)
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.heightIn(max = 300.dp)
        ) {
            itemsIndexed(items) { index, item ->
                val isSelected = index == state.suggestionSelectedIndex
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(item) }
                        .background(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    if (item.icon != null) {
                        item.icon.invoke()
                        Spacer(Modifier.width(8.dp))
                    }
                    Column {
                        Text(
                            text = item.display,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                        if (item.subtitle != null) {
                            Text(
                                text = item.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Represents a single choice inside the autocomplete suggestion list.
 *
 * @property id The unique identifier of the entity (e.g. "user-123").
 * @property display The primary text displayed in the option list and inserted into the editor.
 * @property subtitle Optional description or detail shown below the main display text.
 * @property icon Optional composable icon displayed to the left of the text content.
 */
data class SuggestionItem(
    val id: String,
    val display: String,
    val subtitle: String? = null,
    val icon: (@Composable () -> Unit)? = null
)
