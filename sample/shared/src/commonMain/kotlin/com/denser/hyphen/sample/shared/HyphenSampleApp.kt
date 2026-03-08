package com.denser.hyphen.sample.shared

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denser.hyphen.state.HyphenTextState
import com.denser.hyphen.state.rememberHyphenTextState
import com.denser.hyphen.ui.HyphenBasicTextEditor
import hyphen.sample.shared.generated.resources.Res
import hyphen.sample.shared.generated.resources.bug_report_24dp
import hyphen.sample.shared.generated.resources.dark_mode_24dp
import hyphen.sample.shared.generated.resources.light_mode_24dp
import hyphen.sample.shared.generated.resources.markdown_24dp
import org.jetbrains.compose.resources.painterResource


typealias VerticalScrollbarSlot = @Composable (scrollState: ScrollState, modifier: Modifier) -> Unit

// ─────────────────────────────────────────────────────────────────────────────
// Entry point
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HyphenSampleApp(
    verticalScrollbar: VerticalScrollbarSlot? = null,
) {
    val editorState = rememberHyphenTextState(initialText = DEMO_TEXT)
    var isDarkTheme by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 800.dp

        var showPanel by remember(isWide) { mutableStateOf(isWide) }
        var showMarkdown by remember(isWide) { mutableStateOf(isWide) }

        MaterialTheme(
            colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()
        ) {
            Scaffold(
                topBar = {
                    SampleTopBar(
                        state = editorState,
                        showPanel = showPanel,
                        showMarkdown = showMarkdown,
                        isDarkTheme = isDarkTheme,
                        onTogglePanel = { showPanel = !showPanel },
                        onToggleMarkdown = { showMarkdown = !showMarkdown },
                        onToggleTheme = { isDarkTheme = !isDarkTheme },
                    )
                }
            ) { innerPadding ->
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    val isWide = maxWidth >= 800.dp

                    if (isWide) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            EditorPane(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                state = editorState,
                                showMarkdown = showMarkdown,
                                verticalScrollbar = verticalScrollbar,
                            )
                            AnimatedVisibility(
                                visible = showPanel,
                                enter = expandHorizontally(tween(220)) + fadeIn(tween(220)),
                                exit = shrinkHorizontally(tween(220)) + fadeOut(tween(180)),
                            ) {
                                Row(modifier = Modifier.fillMaxHeight()) {
                                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                    StateInspectorPanel(
                                        state = editorState,
                                        modifier = Modifier.width(300.dp).fillMaxHeight(),
                                        verticalScrollbar = verticalScrollbar,
                                    )
                                }
                            }
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            EditorPane(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                state = editorState,
                                showMarkdown = showMarkdown,
                                verticalScrollbar = verticalScrollbar,
                            )
                            AnimatedVisibility(
                                visible = showPanel,
                                enter = expandVertically(tween(220)) + fadeIn(tween(220)),
                                exit = shrinkVertically(tween(220)) + fadeOut(tween(180)),
                            ) {
                                Column {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                    StateInspectorPanel(
                                        state = editorState,
                                        modifier = Modifier.fillMaxWidth().height(240.dp),
                                        horizontal = true,
                                        verticalScrollbar = verticalScrollbar,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SampleTopBar(
    state: HyphenTextState,
    showPanel: Boolean,
    showMarkdown: Boolean,
    isDarkTheme: Boolean,
    onTogglePanel: () -> Unit,
    onToggleMarkdown: () -> Unit,
    onToggleTheme: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Hyphen",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "demo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    IconButton(
                        onClick = onToggleTheme,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .size(40.dp)
                            .focusProperties { canFocus = false },
                    ) {
                        Icon(
                            painterResource(if (isDarkTheme) Res.drawable.light_mode_24dp else Res.drawable.dark_mode_24dp),
                            contentDescription = "Toggle Theme",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    IconToggleButton(
                        checked = showMarkdown,
                        onCheckedChange = { onToggleMarkdown() },
                        modifier = Modifier
                            .size(40.dp)
                            .focusProperties { canFocus = false },
                        shape = RoundedCornerShape(12.dp),
                        colors = IconButtonDefaults.iconToggleButtonColors(
                            checkedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    ) {
                        Icon(
                            painterResource(Res.drawable.markdown_24dp),
                            contentDescription = "Toggle markdown preview",
                        )
                    }
                    IconToggleButton(
                        checked = showPanel,
                        onCheckedChange = { onTogglePanel() },
                        modifier = Modifier
                            .size(40.dp)
                            .focusProperties { canFocus = false },
                        shape = RoundedCornerShape(12.dp),
                        colors = IconButtonDefaults.iconToggleButtonColors(
                            checkedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    ) {
                        Icon(
                            painterResource(Res.drawable.bug_report_24dp),
                            contentDescription = "Toggle Debugger",
                        )
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                HyphenToolbar(state = state)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Editor pane
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EditorPane(
    modifier: Modifier,
    state: HyphenTextState,
    showMarkdown: Boolean,
    verticalScrollbar: VerticalScrollbarSlot?,
) {
    val editorScrollState = rememberScrollState()

    Column(modifier = modifier) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            HyphenBasicTextEditor(
                state = state,
                scrollState = editorScrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
            )
            verticalScrollbar?.invoke(
                editorScrollState,
                Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp, horizontal = 2.dp),
            )
        }

        AnimatedVisibility(
            visible = showMarkdown,
            enter = expandVertically(tween(200)) + fadeIn(tween(200)),
            exit = shrinkVertically(tween(180)) + fadeOut(tween(180)),
        ) {
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                MarkdownPreviewPanel(
                    markdown = state.toMarkdown(),
                    modifier = Modifier.fillMaxWidth().height(240.dp),
                    verticalScrollbar = verticalScrollbar,
                )
            }
        }
    }
}

@Composable
private fun MarkdownPreviewPanel(
    markdown: String,
    modifier: Modifier = Modifier,
    verticalScrollbar: VerticalScrollbarSlot?,
) {
    val scrollState = rememberScrollState()

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Markdown output",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
            )
        }
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

// ─────────────────────────────────────────────────────────────────────────────
// State inspector panel
// ─────────────────────────────────────────────────────────────────────────────

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

    Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            InspectorHeader()
            if (horizontal) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) { selectionGroup() }
                    Box(Modifier.weight(1f)) { historyGroup() }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

@Composable
private fun ActiveSpansList(state: HyphenTextState) {
    InspectorGroup(title = "Live Spans (${state.spans.size})") {
        if (state.spans.isEmpty()) InspectorEmptyHint("no formatting")
        else {
            state.spans.take(40).forEach { span ->
                InspectorRow(span.style.toString().split(".").last(), "${span.start}..${span.end}")
            }
            if (state.spans.size > 40) InspectorEmptyHint("+ ${state.spans.size - 40} more")
        }
    }
}

@Composable
private fun InspectorHeader() {
    Row(
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
        )
        Text(
            text = "State Inspector",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Inspector primitives
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InspectorGroup(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .widthIn(min = 200.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.4.sp,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .padding(horizontal = 10.dp, vertical = 5.dp),
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun InspectorRow(
    key: String,
    value: String,
    accent: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = key,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (accent)
                    MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (accent) FontWeight.SemiBold else FontWeight.Normal,
            ),
        )
    }
}

@Composable
private fun InspectorEmptyHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.outlineVariant,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.padding(vertical = 2.dp),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo text
// ─────────────────────────────────────────────────────────────────────────────

private val DEMO_TEXT = """
    This is a paragraph demonstrating formatting:
    **Bold**, *Italic*, __Underline__, ~~Strikethrough~~, ==Highlight==, and `Inline Code`.

    > This is a blockquote. Nested text can also be **bold** inside quotes.

    - Bullet point 1
    - Bullet point 2

    1. Ordered list item 1
    2. Ordered list item 2
""".trimIndent()