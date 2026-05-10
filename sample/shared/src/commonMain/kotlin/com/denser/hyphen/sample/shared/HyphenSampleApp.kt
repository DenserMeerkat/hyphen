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
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
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
import androidx.compose.ui.text.SpanStyle
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
import hyphen.sample.shared.generated.resources.person_24dp
import hyphen.sample.shared.generated.resources.mail_24dp
import hyphen.sample.shared.generated.resources.search_24dp
import hyphen.sample.shared.generated.resources.notifications_24dp
import hyphen.sample.shared.generated.resources.content_copy_24dp
import hyphen.sample.shared.generated.resources.label_24dp
import hyphen.sample.shared.generated.resources.bolt_24dp
import hyphen.sample.shared.generated.resources.refresh_24dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import com.denser.hyphen.model.TriggerConfig
import com.denser.hyphen.model.TriggerState
import com.denser.hyphen.ui.mention.HyphenMentionConfig
import com.denser.hyphen.ui.mention.TriggerSuggestions
import com.denser.hyphen.ui.mention.SuggestionItem
import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.ui.style.HyphenStyleConfig


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
                                snackbarHostState = snackbarHostState,
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
    snackbarHostState: SnackbarHostState,
) {
    val editorScrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

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
                styleConfig = HyphenStyleConfig(
                    mentionStyles = mapOf(
                        "user" to SpanStyle(
                            color = Color(0xFF1976D2),
                            fontWeight = FontWeight.Bold
                        ),
                        "tag" to SpanStyle(
                            color = Color(0xFF388E3C),
                            fontWeight = FontWeight.Bold
                        ),
                        "var" to SpanStyle(
                            color = Color(0xFF7B1FA2),
                            background = Color(0xFF7B1FA2).copy(alpha = 0.1f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                ),
                triggerPopup = { trigger ->
                    SampleTriggerPopup(trigger, state)
                },
                mentionConfig = HyphenMentionConfig(
                    onMentionClick = { mention ->
                        scope.launch {
                            snackbarHostState.showSnackbar("Clicked mention: ${mention.display}")
                        }
                    },
                    dropdownContent = dropdownContent@ { span, offset, onDismiss ->
                        val mention = span.style as? MarkupStyle.Mention ?: return@dropdownContent
                        val density = LocalDensity.current
                        val dpOffset = with(density) { DpOffset(offset.x.toDp(), offset.y.toDp() + 8.dp) }

                        DropdownMenu(
                            expanded = true,
                            onDismissRequest = onDismiss,
                            offset = dpOffset,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = MaterialTheme.shapes.medium,
                            shadowElevation = 4.dp,
                        ) {
                            Box(modifier = Modifier.padding(12.dp).widthIn(max = 240.dp)) {
                                Text(
                                    text = "${mention.scheme.replaceFirstChar { it.uppercase() }}: ${mention.display}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            when (mention.scheme) {
                                "user" -> {
                                    DropdownMenuItem(
                                        text = { Text("View Profile") },
                                        onClick = { onDismiss() },
                                        leadingIcon = {
                                            Icon(
                                                painterResource(Res.drawable.person_24dp),
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Send Message") },
                                        onClick = { onDismiss() },
                                        leadingIcon = {
                                            Icon(
                                                painterResource(Res.drawable.mail_24dp),
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    )
                                }
                                "tag" -> {
                                    DropdownMenuItem(
                                        text = { Text("Search ${mention.display}") },
                                        onClick = { onDismiss() },
                                        leadingIcon = {
                                            Icon(
                                                painterResource(Res.drawable.search_24dp),
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Follow Tag") },
                                        onClick = { onDismiss() },
                                        leadingIcon = {
                                            Icon(
                                                painterResource(Res.drawable.notifications_24dp),
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    )
                                }
                                "var" -> {
                                    DropdownMenuItem(
                                        text = { Text("Refresh Value") },
                                        onClick = { onDismiss() },
                                        leadingIcon = {
                                            Icon(
                                                painterResource(Res.drawable.refresh_24dp),
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    )
                                }
                            }

                            DropdownMenuItem(
                                text = { Text("Copy ID") },
                                onClick = { onDismiss() },
                                leadingIcon = {
                                    Icon(
                                        painterResource(Res.drawable.content_copy_24dp),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            )
                        }
                    },
                    hoverCardContent = { mention ->
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            tonalElevation = 8.dp,
                            shadowElevation = 6.dp,
                            modifier = Modifier.widthIn(min = 180.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painterResource(
                                            when (mention.scheme) {
                                                "user" -> Res.drawable.person_24dp
                                                "tag" -> Res.drawable.label_24dp
                                                "var" -> Res.drawable.bolt_24dp
                                                else -> Res.drawable.restore_page_24dp
                                            }
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = mention.display,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Scheme: ${mention.scheme}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "ID: ${mention.id}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                )
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

@Composable
private fun SampleTriggerPopup(
    trigger: TriggerState,
    state: HyphenTextState
) {
    val options = remember(trigger.config.trigger, trigger.query) {
        val list = when (trigger.config.trigger) {
            "@" -> listOf("Alice", "Bob", "Charlie", "David", "Eve", "Frank", "Grace")
            "#" -> listOf("bug", "feature", "enhancement", "question", "wontfix", "duplicate")
            "{" -> listOf("project_name", "user_count", "today_date", "current_time", "version")
            "{{" -> listOf("project_name", "user_count", "today_date", "current_time", "version")
            else -> emptyList()
        }
        list.filter { it.contains(trigger.query, ignoreCase = true) }
    }

    val suggestions = options.map { option ->
        SuggestionItem(
            id = option,
            display = option,
            subtitle = when(trigger.config.trigger) {
                "@" -> "Team Member"
                "#" -> "Label"
                "{" -> "Variable"
                "{{" -> "Dynamic Var"
                else -> "Suggestion"
            },
            icon = {
                Icon(
                    painterResource(
                        when(trigger.config.trigger) {
                            "@" -> Res.drawable.person_24dp
                            "#" -> Res.drawable.label_24dp
                            "{" -> Res.drawable.bolt_24dp
                            "{{" -> Res.drawable.bolt_24dp
                            else -> Res.drawable.restore_page_24dp
                        }
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
    }

    TriggerSuggestions(
        state = state,
        trigger = trigger,
        items = suggestions,
        onSelect = { item ->
            val triggerPrefix = trigger.config.trigger
            val triggerEnd = trigger.config.endTrigger ?: ""
            state.completeMention(
                id = item.id,
                display = "$triggerPrefix${item.display}$triggerEnd"
            )
        }
    )
}