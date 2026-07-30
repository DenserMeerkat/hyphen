package com.denser.hyphen.sample.shared

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldLabelPosition
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.sample.shared.components.EditorConfigPanel
import com.denser.hyphen.sample.shared.components.MarkdownPreviewPanel
import com.denser.hyphen.sample.shared.components.PlaygroundTopBar
import com.denser.hyphen.sample.shared.components.SampleMentionDropdown
import com.denser.hyphen.sample.shared.components.SampleMentionHoverCard
import com.denser.hyphen.sample.shared.components.SampleTriggerPopup
import com.denser.hyphen.sample.shared.components.StateInspectorPanel
import com.denser.hyphen.sample.shared.components.StyleConfigPanel
import com.denser.hyphen.sample.shared.components.SuggestionsBottomBar
import com.denser.hyphen.state.HyphenTextState
import com.denser.hyphen.ui.HyphenBasicTextEditor
import com.denser.hyphen.ui.material3.HyphenTextField
import com.denser.hyphen.ui.mention.HyphenMentionConfig
import hyphen.sample.shared.generated.resources.Res
import hyphen.sample.shared.generated.resources.bug_report_24dp
import hyphen.sample.shared.generated.resources.markdown_24dp
import hyphen.sample.shared.generated.resources.palette_24dp
import hyphen.sample.shared.generated.resources.terminal_24dp
import hyphen.sample.shared.generated.resources.tune_24dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

// ─────────────────────────────────────────────────────────────────────────────
// Root scaffold
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PlaygroundScaffold(
    playgroundState: PlaygroundState,
    hyphenState: HyphenTextState,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onSave: () -> Unit,
    onReload: () -> Unit,
    onReset: () -> Unit,
    snackbarHostState: SnackbarHostState,
    verticalScrollbar: VerticalScrollbarSlot? = null,
    isAndroid: Boolean = false,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 860.dp

        Scaffold(
            topBar = {
                PlaygroundTopBar(
                    state = hyphenState,
                    playgroundState = playgroundState,
                    isDarkTheme = isDarkTheme,
                    isWide = isWide,
                    onToggleTheme = onToggleTheme,
                    onSave = onSave,
                    onReload = onReload,
                    onReset = onReset,
                )
            },
            bottomBar = {
                Column(modifier = Modifier.imePadding()) {
                    if (hyphenState.activeTrigger != null) {
                        SuggestionsBottomBar(
                            state = hyphenState,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    if (!isWide) {
                        MobileBottomNav(playgroundState)
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
            ) {
                if (isWide) {
                    IdePlatformLayout(
                        playgroundState = playgroundState,
                        hyphenState = hyphenState,
                        snackbarHostState = snackbarHostState,
                        verticalScrollbar = verticalScrollbar,
                        isAndroid = isAndroid,
                    )
                } else {
                    MobileLayout(
                        playgroundState = playgroundState,
                        hyphenState = hyphenState,
                        snackbarHostState = snackbarHostState,
                        verticalScrollbar = verticalScrollbar,
                        isAndroid = isAndroid,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Wide Layout: Main Workspace | Right Tool Drawer | Right Activity Rail
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun IdePlatformLayout(
    playgroundState: PlaygroundState,
    hyphenState: HyphenTextState,
    snackbarHostState: SnackbarHostState,
    verticalScrollbar: VerticalScrollbarSlot?,
    isAndroid: Boolean,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // ── Central Workspace (Editor + Bottom Terminal Toolwindow) ───────
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            // Editor Workspace Window
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                PlaygroundEditorArea(
                    modifier = Modifier.fillMaxSize(),
                    playgroundState = playgroundState,
                    hyphenState = hyphenState,
                    snackbarHostState = snackbarHostState,
                    verticalScrollbar = verticalScrollbar,
                    isAndroid = isAndroid,
                )
            }

            // Collapsible Bottom Terminal Window (Markdown Output Terminal)
            AnimatedVisibility(
                visible = playgroundState.activeBottomTool != null,
                enter = expandVertically(tween(220)) + fadeIn(tween(220)),
                exit = shrinkVertically(tween(180)) + fadeOut(tween(180)),
            ) {
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest),
                    ) {
                        when (playgroundState.activeBottomTool) {
                            BottomToolWindow.MarkdownOutput -> MarkdownPreviewPanel(
                                markdown = hyphenState.toMarkdown(),
                                modifier = Modifier.fillMaxSize(),
                                verticalScrollbar = verticalScrollbar,
                            )

                            null -> {}
                        }
                    }
                }
            }
        }

        // ── Collapsible Right Tool Drawer (Styles & Configs) ───────────────
        AnimatedVisibility(
            visible = playgroundState.activeLeftTool != null,
            enter = expandHorizontally(tween(200)) + fadeIn(tween(200)),
            exit = shrinkHorizontally(tween(180)) + fadeOut(tween(180)),
        ) {
            Row(modifier = Modifier.fillMaxHeight()) {
                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Box(
                    modifier = Modifier
                        .width(320.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    when (playgroundState.activeLeftTool) {
                        LeftToolWindow.StyleConfig -> StyleConfigPanel(
                            state = playgroundState,
                            modifier = Modifier.fillMaxSize(),
                            verticalScrollbar = verticalScrollbar,
                        )

                        LeftToolWindow.EditorConfig -> EditorConfigPanel(
                            state = playgroundState,
                            modifier = Modifier.fillMaxSize(),
                            verticalScrollbar = verticalScrollbar,
                        )

                        LeftToolWindow.StateInspector -> StateInspectorPanel(
                            state = hyphenState,
                            modifier = Modifier.fillMaxSize(),
                            verticalScrollbar = verticalScrollbar,
                        )

                        null -> {}
                    }
                }
            }
        }

        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // ── Right Activity Rail ─────────────────────────────────────────
        RightActivityRail(playgroundState = playgroundState)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Right Activity Rail Component
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RightActivityRail(playgroundState: PlaygroundState) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxHeight().width(56.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Top Action Group: Styles, Editor Config, and State Inspector
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ActivityRailItem(
                    icon = painterResource(Res.drawable.palette_24dp),
                    label = "Styles",
                    selected = playgroundState.activeLeftTool == LeftToolWindow.StyleConfig,
                    onClick = {
                        playgroundState.activeLeftTool =
                            if (playgroundState.activeLeftTool == LeftToolWindow.StyleConfig) null
                            else LeftToolWindow.StyleConfig
                    },
                )

                ActivityRailItem(
                    icon = painterResource(Res.drawable.tune_24dp),
                    label = "Config",
                    selected = playgroundState.activeLeftTool == LeftToolWindow.EditorConfig,
                    onClick = {
                        playgroundState.activeLeftTool =
                            if (playgroundState.activeLeftTool == LeftToolWindow.EditorConfig) null
                            else LeftToolWindow.EditorConfig
                    },
                )

                ActivityRailItem(
                    icon = painterResource(Res.drawable.bug_report_24dp),
                    label = "Inspector",
                    selected = playgroundState.activeLeftTool == LeftToolWindow.StateInspector,
                    onClick = {
                        playgroundState.activeLeftTool =
                            if (playgroundState.activeLeftTool == LeftToolWindow.StateInspector) null
                            else LeftToolWindow.StateInspector
                    },
                )
            }

            // Bottom Action Group: Markdown Output Terminal
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ActivityRailItem(
                    icon = painterResource(Res.drawable.terminal_24dp),
                    label = "Terminal",
                    selected = playgroundState.activeBottomTool == BottomToolWindow.MarkdownOutput,
                    onClick = {
                        playgroundState.activeBottomTool =
                            if (playgroundState.activeBottomTool == BottomToolWindow.MarkdownOutput) null
                            else BottomToolWindow.MarkdownOutput
                    },
                )
            }
        }
    }
}

@Composable
private fun ActivityRailItem(
    icon: androidx.compose.ui.graphics.painter.Painter,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
        contentColor = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}



// ─────────────────────────────────────────────────────────────────────────────
// Editor Area (Basic / Filled TF / Outlined TF + RTL wrapping)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaygroundEditorArea(
    modifier: Modifier,
    playgroundState: PlaygroundState,
    hyphenState: HyphenTextState,
    snackbarHostState: SnackbarHostState,
    verticalScrollbar: VerticalScrollbarSlot?,
    isAndroid: Boolean,
) {
    val styleConfig by remember { derivedStateOf { playgroundState.buildStyleConfig() } }
    val scope = rememberCoroutineScope()
    val editorScrollState = rememberScrollState()

    val textStyle = TextStyle(
        fontSize = playgroundState.fontSize.sp,
        color = MaterialTheme.colorScheme.onSurface,
    )
    val cursorBrush = if (playgroundState.useCursorColorOverride) {
        SolidColor(playgroundState.cursorColor)
    } else {
        SolidColor(MaterialTheme.colorScheme.onSurface)
    }

    val mentionConfig = remember(snackbarHostState) {
        HyphenMentionConfig(
            onMentionClick = { mention ->
                scope.launch {
                    snackbarHostState.showSnackbar("Clicked: ${mention.display}")
                }
            },
            dropdownContent = { span, offset, onDismiss ->
                val mention = span.style as? MarkupStyle.Mention
                if (mention != null) SampleMentionDropdown(mention, offset, onDismiss)
            },
            hoverCardContent = { mention -> SampleMentionHoverCard(mention) },
        )
    }

    val triggerPopup: @Composable (com.denser.hyphen.model.TriggerState) -> Unit = { trigger ->
        SampleTriggerPopup(trigger, hyphenState)
    }

    Box(modifier = modifier.fillMaxSize().imePadding()) {
        val layoutDir = if (playgroundState.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.ui.platform.LocalLayoutDirection provides layoutDir,
        ) {
            when (playgroundState.editorMode) {
                EditorMode.BasicEditor -> {
                    HyphenBasicTextEditor(
                        state = hyphenState,
                        modifier = Modifier.fillMaxSize().padding(vertical = 12.dp),
                        enabled = playgroundState.isEnabled,
                        readOnly = playgroundState.isReadOnly,
                        textStyle = textStyle,
                        styleConfig = styleConfig,
                        mentionConfig = mentionConfig,
                        triggerPopup = triggerPopup,
                        showDefaultSuggestionsPopup = !isAndroid && playgroundState.showSuggestionsPopup,
                        scrollState = editorScrollState,
                        cursorBrush = cursorBrush,
                        horizontalPadding = playgroundState.horizontalPadding.dp,
                        layoutDirection = layoutDir,
                    )
                }

                EditorMode.TextField -> {
                    val interactionSource = remember { MutableInteractionSource() }
                    val isOutlined = playgroundState.textFieldVariant == TextFieldVariant.Outlined
                    val colors = if (isOutlined) OutlinedTextFieldDefaults.colors() else TextFieldDefaults.colors()

                    HyphenTextField(
                        state = hyphenState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        enabled = playgroundState.isEnabled,
                        readOnly = playgroundState.isReadOnly,
                        textStyle = textStyle,
                        styleConfig = styleConfig,
                        mentionConfig = mentionConfig,
                        triggerPopup = triggerPopup,
                        showDefaultSuggestionsPopup = !isAndroid && playgroundState.showSuggestionsPopup,
                        scrollState = editorScrollState,
                        interactionSource = interactionSource,
                        horizontalPadding = playgroundState.horizontalPadding.dp,
                        layoutDirection = layoutDir,
                        isError = playgroundState.isError,
                        shape = if (isOutlined) RoundedCornerShape(6.dp) else TextFieldDefaults.shape,
                        colors = colors,
                        labelPosition = if (playgroundState.useFloatingLabel)
                            TextFieldLabelPosition.Attached()
                        else
                            TextFieldLabelPosition.Above(),
                        label = if (playgroundState.showLabel) {
                            { Text(playgroundState.labelText) }
                        } else null,
                        placeholder = if (playgroundState.showPlaceholder) {
                            { Text(playgroundState.placeholderText) }
                        } else null,
                        supportingText = if (playgroundState.showSupportingText) {
                            {
                                Text(
                                    playgroundState.supportingText,
                                    color = if (playgroundState.isError)
                                        MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        } else null,
                    )
                }
            }
        }

        verticalScrollbar?.invoke(
            editorScrollState,
            Modifier
                .align(
                    if (playgroundState.isRtl) Alignment.CenterStart
                    else Alignment.CenterEnd,
                )
                .fillMaxHeight()
                .padding(vertical = 4.dp, horizontal = 2.dp),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Responsive Mobile Fallback (Width < 860dp)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MobileLayout(
    playgroundState: PlaygroundState,
    hyphenState: HyphenTextState,
    snackbarHostState: SnackbarHostState,
    verticalScrollbar: VerticalScrollbarSlot?,
    isAndroid: Boolean,
) {
    AnimatedContent(
        targetState = playgroundState.activeMobileTab,
        transitionSpec = {
            (fadeIn(tween(160)) togetherWith fadeOut(tween(120)))
        },
        modifier = Modifier.fillMaxSize(),
    ) { tab ->
        when (tab) {
            MobileTab.Editor -> PlaygroundEditorArea(
                modifier = Modifier.fillMaxSize(),
                playgroundState = playgroundState,
                hyphenState = hyphenState,
                snackbarHostState = snackbarHostState,
                verticalScrollbar = verticalScrollbar,
                isAndroid = isAndroid,
            )

            MobileTab.Styles -> StyleConfigPanel(
                state = playgroundState,
                modifier = Modifier.fillMaxSize(),
                verticalScrollbar = verticalScrollbar,
            )

            MobileTab.Config -> EditorConfigPanel(
                state = playgroundState,
                modifier = Modifier.fillMaxSize(),
                verticalScrollbar = verticalScrollbar,
            )

            MobileTab.Markdown -> MarkdownPreviewPanel(
                markdown = hyphenState.toMarkdown(),
                modifier = Modifier.fillMaxSize(),
                verticalScrollbar = verticalScrollbar,
            )

            MobileTab.Inspector -> StateInspectorPanel(
                state = hyphenState,
                modifier = Modifier.fillMaxSize(),
                verticalScrollbar = verticalScrollbar,
            )
        }
    }
}

@Composable
private fun MobileBottomNav(state: PlaygroundState) {
    NavigationBar {
        NavigationBarItem(
            selected = state.activeMobileTab == MobileTab.Editor,
            onClick = { state.activeMobileTab = MobileTab.Editor },
            icon = { Icon(painterResource(Res.drawable.markdown_24dp), contentDescription = "Editor") },
            label = { Text("Editor") },
        )
        NavigationBarItem(
            selected = state.activeMobileTab == MobileTab.Styles,
            onClick = { state.activeMobileTab = MobileTab.Styles },
            icon = { Icon(painterResource(Res.drawable.palette_24dp), contentDescription = "Styles") },
            label = { Text("Styles") },
        )
        NavigationBarItem(
            selected = state.activeMobileTab == MobileTab.Config,
            onClick = { state.activeMobileTab = MobileTab.Config },
            icon = { Icon(painterResource(Res.drawable.tune_24dp), contentDescription = "Config") },
            label = { Text("Config") },
        )
        NavigationBarItem(
            selected = state.activeMobileTab == MobileTab.Inspector,
            onClick = { state.activeMobileTab = MobileTab.Inspector },
            icon = { Icon(painterResource(Res.drawable.bug_report_24dp), contentDescription = "Inspector") },
            label = { Text("Inspector") },
        )
        NavigationBarItem(
            selected = state.activeMobileTab == MobileTab.Markdown,
            onClick = { state.activeMobileTab = MobileTab.Markdown },
            icon = { Icon(painterResource(Res.drawable.terminal_24dp), contentDescription = "Terminal") },
            label = { Text("Terminal") },
        )
    }
}
