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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denser.hyphen.state.HyphenTextState
import com.denser.hyphen.state.rememberHyphenTextState
import com.denser.hyphen.ui.HyphenBasicTextEditor
import androidx.compose.ui.platform.LocalUriHandler
import com.denser.hyphen.sample.shared.components.MarkdownPreviewPanel
import com.denser.hyphen.sample.shared.components.StateInspectorPanel
import hyphen.sample.shared.generated.resources.Res
import hyphen.sample.shared.generated.resources.bug_report_24dp
import hyphen.sample.shared.generated.resources.dark_mode_24dp
import hyphen.sample.shared.generated.resources.github
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
                    val uriHandler = LocalUriHandler.current
                    IconButton(
                        onClick = { uriHandler.openUri("https://github.com/DenserMeerkat/hyphen") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .size(40.dp)
                            .focusProperties { canFocus = false },
                    ) {
                        Icon(
                            painterResource(Res.drawable.github),
                            contentDescription = "GitHub",
                            modifier = Modifier.size(20.dp),
                        )
                    }
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
                    .padding(12.dp),
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

// ─────────────────────────────────────────────────────────────────────────────
// Demo text
// ─────────────────────────────────────────────────────────────────────────────

private val DEMO_TEXT = """
    # Heading 1
    ## Heading 2
    ### Heading 3
    #### Heading 4
    ##### Heading 5
    ###### Heading 6
    
    This is a paragraph demonstrating formatting:
    **Bold**, *Italic*, __Underline__, ~~Strikethrough~~, ==Highlight==, `Inline Code`, and [Links](https://github.com/densermeerkat/hyphen).

    > This is a blockquote. Nested text can also be **bold** inside quotes.

    - Bullet point 1
    - Bullet point 2

    1. Ordered list item 1
    2. Ordered list item 2
    
    - [ ] Checklist task 1
    - [x] Checklist task 2
""".trimIndent()