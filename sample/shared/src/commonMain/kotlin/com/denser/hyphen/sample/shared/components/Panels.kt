package com.denser.hyphen.sample.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.denser.hyphen.sample.shared.VerticalScrollbarSlot
import com.denser.hyphen.state.HyphenTextState

@Composable
fun StateInspectorPanel(
    state: HyphenTextState,
    modifier: Modifier = Modifier,
    horizontal: Boolean = false,
    verticalScrollbar: VerticalScrollbarSlot? = null,
) {
    val selectionGroup = @Composable {
        InspectorGroup(title = "Selection") {
            val sel = state.selection
            InspectorRow(
                "index",
                if (sel.collapsed) "@ ${sel.start}" else "${sel.start}..${sel.end}",
            )
            InspectorRow("active", state.isFocused.toString(), accent = state.isFocused)
        }
    }

    val historyGroup = @Composable {
        InspectorGroup(title = "History Stack") {
            InspectorRow("undoable", state.canUndo.toString(), accent = state.canUndo)
            InspectorRow("redoable", state.canRedo.toString(), accent = state.canRedo)
        }
    }

    val statsGroup = @Composable {
        InspectorGroup(title = "Document Stats") {
            InspectorRow("chars", state.text.length.toString())
            InspectorRow("spans", state.spans.size.toString(), accent = state.spans.isNotEmpty())
        }
    }

    val scrollState = rememberScrollState()

    Column(modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)) {
        PanelHeader(
            dot = MaterialTheme.colorScheme.primary,
            label = "State Inspector",
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (horizontal) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(Modifier.weight(1f)) { selectionGroup() }
                        Box(Modifier.weight(1f)) { historyGroup() }
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(Modifier.weight(1f)) { statsGroup() }
                    }
                } else {
                    selectionGroup()
                    historyGroup()
                    statsGroup()
                }
                ActiveSpansList(state)
                Spacer(Modifier.height(4.dp))
            }
            verticalScrollbar?.invoke(
                scrollState,
                Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp, horizontal = 2.dp),
            )
        }
    }
}


@Composable
fun MarkdownPreviewPanel(
    markdown: String,
    modifier: Modifier = Modifier,
    verticalScrollbar: VerticalScrollbarSlot?,
) {
    val scrollState = rememberScrollState()

    Column(modifier = modifier) {
        PanelHeader(
            dot = MaterialTheme.colorScheme.tertiary,
            label = "Markdown output",
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLowest),
        ) {
            Text(
                text = markdown.ifEmpty { "// empty" },
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = if (markdown.isEmpty())
                        MaterialTheme.colorScheme.outlineVariant
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )
            verticalScrollbar?.invoke(
                scrollState,
                Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp, horizontal = 2.dp),
            )
        }
    }
}