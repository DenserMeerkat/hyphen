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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denser.hyphen.state.HyphenTextState
import com.denser.hyphen.state.rememberHyphenTextState
import com.denser.hyphen.ui.HyphenBasicTextEditor
import com.denser.hyphen.sample.shared.components.MarkdownPreviewPanel
import com.denser.hyphen.sample.shared.components.StateInspectorPanel
import com.denser.hyphen.sample.shared.components.SampleTriggerPopup
import com.denser.hyphen.sample.shared.components.SuggestionsBottomBar
import com.denser.hyphen.sample.shared.components.SampleTopBar
import com.denser.hyphen.sample.shared.components.getSampleStyleConfig
import com.denser.hyphen.sample.shared.components.SampleMentionDropdown
import com.denser.hyphen.sample.shared.components.SampleMentionHoverCard
import com.denser.hyphen.sample.shared.data.HyphenDatabase
import com.denser.hyphen.sample.shared.data.HyphenDraft
import com.denser.hyphen.sample.shared.data.getDatabaseBuilder
import com.denser.hyphen.sample.shared.data.getRoomDatabase
import com.denser.hyphen.sample.shared.data.initDatabase
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.denser.hyphen.model.TriggerConfig
import com.denser.hyphen.ui.mention.HyphenMentionConfig
import com.denser.hyphen.model.MarkupStyle


typealias VerticalScrollbarSlot = @Composable (scrollState: ScrollState, modifier: Modifier) -> Unit

// ─────────────────────────────────────────────────────────────────────────────
// Entry point
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HyphenSampleApp(
    verticalScrollbar: VerticalScrollbarSlot? = null,
    context: Any? = null,
) {
    val isAndroid = context != null
    val editorState = rememberHyphenTextState(initialText = "")
    
    LaunchedEffect(Unit) {
        editorState.triggerConfigs = listOf(
            TriggerConfig(trigger = "@", scheme = "user"),
            TriggerConfig(trigger = "#", scheme = "tag"),
            TriggerConfig(trigger = "{", scheme = "var", endTrigger = "}", addSpaceOnCompletion = false),
            TriggerConfig(trigger = "{{", scheme = "var", endTrigger = "}}", addSpaceOnCompletion = false)
        )
    }

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
                bottomBar = {
                    val showBottomBar = isAndroid && editorState.activeTrigger != null
                    if (showBottomBar) {
                        SuggestionsBottomBar(
                            state = editorState,
                            modifier = Modifier
                                .navigationBarsPadding()
                                .imePadding()
                        )
                    }
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { innerPadding ->
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding)
                ) {
                    val isWide = maxWidth >= 800.dp

                    if (isWide) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            EditorPane(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                state = editorState,
                                showMarkdown = showMarkdown,
                                verticalScrollbar = verticalScrollbar,
                                snackbarHostState = snackbarHostState,
                                isAndroid = isAndroid,
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
                                snackbarHostState = snackbarHostState,
                                isAndroid = isAndroid,
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
// Editor pane
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EditorPane(
    modifier: Modifier,
    state: HyphenTextState,
    showMarkdown: Boolean,
    verticalScrollbar: VerticalScrollbarSlot?,
    snackbarHostState: SnackbarHostState,
    isAndroid: Boolean,
) {
    val editorScrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.imePadding()) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            HyphenBasicTextEditor(
                state = state,
                scrollState = editorScrollState,
                showDefaultSuggestionsPopup = !isAndroid,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                styleConfig = getSampleStyleConfig(),
                triggerPopup = { trigger ->
                    SampleTriggerPopup(trigger, state)
                },
                mentionConfig = remember {
                    HyphenMentionConfig(
                        onMentionClick = { mention ->
                            scope.launch {
                                snackbarHostState.showSnackbar("Clicked mention: ${mention.display}")
                            }
                        },
                        dropdownContent = { span, offset, onDismiss ->
                            val mention = span.style as? MarkupStyle.Mention
                            if (mention != null) {
                                SampleMentionDropdown(mention, offset, onDismiss)
                            }
                        },
                        hoverCardContent = { mention ->
                            SampleMentionHoverCard(mention)
                        }
                    )
                }
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

    @Alice @Bob
    #bug #feature
    {project_name} {{user_count}}
""".trimIndent()