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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import hyphen.sample.shared.generated.resources.github
import hyphen.sample.shared.generated.resources.markdown_24dp
import com.denser.hyphen.sample.shared.data.HyphenDatabase
import com.denser.hyphen.sample.shared.data.HyphenDraft
import com.denser.hyphen.sample.shared.data.getDatabaseBuilder
import com.denser.hyphen.sample.shared.data.getRoomDatabase
import com.denser.hyphen.sample.shared.data.initDatabase
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import hyphen.sample.shared.generated.resources.more_vert_24dp
import hyphen.sample.shared.generated.resources.dark_mode_24dp
import hyphen.sample.shared.generated.resources.light_mode_24dp
import hyphen.sample.shared.generated.resources.save_24dp
import hyphen.sample.shared.generated.resources.restart_alt_24dp
import hyphen.sample.shared.generated.resources.restore_page_24dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource


typealias VerticalScrollbarSlot = @Composable (scrollState: ScrollState, modifier: Modifier) -> Unit

// ─────────────────────────────────────────────────────────────────────────────
// Entry point
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HyphenSampleApp(
    verticalScrollbar: VerticalScrollbarSlot? = null,
    context: Any? = null,
) {
    val editorState = rememberHyphenTextState(initialText = "")
    val snackbarHostState = remember { SnackbarHostState() }
    var isDarkTheme by remember { mutableStateOf(false) }
    var database by remember { mutableStateOf<HyphenDatabase?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (database == null) {
            try {
                context?.let { initDatabase(it) }
                val db = getRoomDatabase(getDatabaseBuilder())
                database = db

                val draft = db.hyphenDao().getDraft()
                if (draft != null) {
                    editorState.setMarkdown(draft.text)
                } else {
                    editorState.setMarkdown(DEMO_TEXT)
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("DB Error: ${e.message}")
            }
        }
    }

    val onSave: () -> Unit = remember(database) {
        {
            scope.launch {
                val db = database
                if (db != null) {
                    db.hyphenDao().saveDraft(HyphenDraft(text = editorState.toMarkdown()))
                    snackbarHostState.showSnackbar("Draft saved")
                } else {
                    snackbarHostState.showSnackbar("Database not ready")
                }
            }
            Unit
        }
    }

    val onReload: () -> Unit = remember(database) {
        {
            scope.launch {
                val db = database
                if (db != null) {
                    val draft = db.hyphenDao().getDraft()
                    if (draft != null) {
                        editorState.setMarkdown(draft.text)
                        snackbarHostState.showSnackbar("Draft reloaded")
                    } else {
                        snackbarHostState.showSnackbar("No draft found in Room")
                    }
                } else {
                    snackbarHostState.showSnackbar("Database not ready")
                }
            }
            Unit
        }
    }

    val onReset: () -> Unit = remember(database) {
        {
            scope.launch {
                database?.hyphenDao()?.clearDraft()
                editorState.setMarkdown(DEMO_TEXT)
                snackbarHostState.showSnackbar("Reset to demo text")
            }
            Unit
        }
    }

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
                        onSave = onSave,
                        onReload = onReload,
                        onReset = onReset,
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
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
    onSave: () -> Unit,
    onReload: () -> Unit,
    onReset: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

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

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .size(40.dp)
                                .focusProperties { canFocus = false },
                        ) {
                            Icon(
                                painterResource(Res.drawable.more_vert_24dp),
                                contentDescription = "More options",
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (isDarkTheme) "Light Mode" else "Dark Mode") },
                                onClick = {
                                    onToggleTheme()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        painterResource(if (isDarkTheme) Res.drawable.light_mode_24dp else Res.drawable.dark_mode_24dp),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Save Draft") },
                                onClick = {
                                    onSave()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        painterResource(Res.drawable.save_24dp),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Reload Editor") },
                                onClick = {
                                    onReload()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        painterResource(Res.drawable.restore_page_24dp),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Reset to Demo") },
                                onClick = {
                                    onReset()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        painterResource(Res.drawable.restart_alt_24dp),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            )
                        }
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