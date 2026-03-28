package com.denser.hyphen.sample.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.denser.hyphen.blocks.model.BlockquoteBlock
import com.denser.hyphen.blocks.model.BulletListBlock
import com.denser.hyphen.blocks.model.CheckboxBlock
import com.denser.hyphen.blocks.model.DividerBlock
import com.denser.hyphen.blocks.model.HyphenBlock
import com.denser.hyphen.blocks.model.OrderedListBlock
import com.denser.hyphen.blocks.model.TextBlock
import com.denser.hyphen.blocks.state.HyphenBlockState
import com.denser.hyphen.blocks.state.rememberHyphenBlockState
import com.denser.hyphen.blocks.ui.HyphenBlockEditor
import com.denser.hyphen.core.model.MarkupStyle
import com.denser.hyphen.core.constants.EditorConstants
import com.denser.hyphen.inline.state.HyphenInlineState
import com.denser.hyphen.sample.shared.components.FormatToggleButton
import hyphen.sample.shared.generated.resources.Res
import hyphen.sample.shared.generated.resources.bug_report_24dp
import hyphen.sample.shared.generated.resources.check_box_24dp
import hyphen.sample.shared.generated.resources.checklist_24dp
import hyphen.sample.shared.generated.resources.code_24dp
import hyphen.sample.shared.generated.resources.dark_mode_24dp
import hyphen.sample.shared.generated.resources.format_bold_24dp
import hyphen.sample.shared.generated.resources.format_h1_24dp
import hyphen.sample.shared.generated.resources.format_h2_24dp
import hyphen.sample.shared.generated.resources.format_h3_24dp
import hyphen.sample.shared.generated.resources.format_h4_24dp
import hyphen.sample.shared.generated.resources.format_h5_24dp
import hyphen.sample.shared.generated.resources.format_h6_24dp
import hyphen.sample.shared.generated.resources.format_ink_highlighter_24dp
import hyphen.sample.shared.generated.resources.format_italic_24dp
import hyphen.sample.shared.generated.resources.format_list_bulleted_24dp
import hyphen.sample.shared.generated.resources.format_list_numbered_24dp
import hyphen.sample.shared.generated.resources.format_quote_24dp
import hyphen.sample.shared.generated.resources.format_strikethrough_24dp
import hyphen.sample.shared.generated.resources.format_underlined_24dp
import hyphen.sample.shared.generated.resources.light_mode_24dp
import hyphen.sample.shared.generated.resources.redo_24dp
import hyphen.sample.shared.generated.resources.undo_24dp
import org.jetbrains.compose.resources.painterResource

@Composable
fun HyphenBlocksSampleApp() {
    val blockState = rememberHyphenBlockState(initialBlocks = DEMO_BLOCKS)
    var isDarkTheme by remember { mutableStateOf(false) }
    var debugBorder by remember { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()
    ) {
        Scaffold(
            topBar = {
                BlocksSampleTopBar(
                    blockState = blockState,
                    isDarkTheme = isDarkTheme,
                    debugBorder = debugBorder,
                    onToggleTheme = { isDarkTheme = !isDarkTheme },
                    onToggleDebugBorder = { debugBorder = !debugBorder },
                )
            }
        ) { innerPadding ->
            HyphenBlockEditor(
                state = blockState,
                debugBorder = debugBorder,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding(),
                contentPadding = PaddingValues(vertical = 12.dp),
            )
        }
    }
}

@Composable
private fun BlocksSampleTopBar(
    blockState: HyphenBlockState,
    isDarkTheme: Boolean,
    debugBorder: Boolean,
    onToggleTheme: () -> Unit,
    onToggleDebugBorder: () -> Unit,
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
                        text = "Hyphen Blocks",
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
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FormatToggleButton(
                        icon = Res.drawable.bug_report_24dp,
                        contentDescription = "Toggle debug border",
                        isActive = debugBorder,
                        enabled = true,
                        onClick = onToggleDebugBorder,
                    )
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.size(40.dp).focusProperties { canFocus = false },
                    ) {
                        Icon(
                            painterResource(if (isDarkTheme) Res.drawable.light_mode_24dp else Res.drawable.dark_mode_24dp),
                            contentDescription = "Toggle theme",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            BlocksToolbar(blockState = blockState)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun BlocksToolbar(blockState: HyphenBlockState) {
    val scrollState = rememberScrollState()
    val focusedId = blockState.focusedBlockId
    val focusedBlock: HyphenBlock? = focusedId?.let { id -> blockState.blocks.firstOrNull { it.id == id } }
    val inlineState: HyphenInlineState? = focusedId?.let { blockState.getInlineState(it) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // ── Inline styles ─────────────────────────────────────────────
                FormatToggleButton(
                    icon = Res.drawable.format_bold_24dp,
                    contentDescription = "Bold",
                    isActive = inlineState?.hasStyle(MarkupStyle.Bold) == true,
                    enabled = inlineState != null,
                    onClick = { inlineState?.toggleStyle(MarkupStyle.Bold) },
                )
                FormatToggleButton(
                    icon = Res.drawable.format_italic_24dp,
                    contentDescription = "Italic",
                    isActive = inlineState?.hasStyle(MarkupStyle.Italic) == true,
                    enabled = inlineState != null,
                    onClick = { inlineState?.toggleStyle(MarkupStyle.Italic) },
                )
                FormatToggleButton(
                    icon = Res.drawable.format_underlined_24dp,
                    contentDescription = "Underline",
                    isActive = inlineState?.hasStyle(MarkupStyle.Underline) == true,
                    enabled = inlineState != null,
                    onClick = { inlineState?.toggleStyle(MarkupStyle.Underline) },
                )
                FormatToggleButton(
                    icon = Res.drawable.format_strikethrough_24dp,
                    contentDescription = "Strikethrough",
                    isActive = inlineState?.hasStyle(MarkupStyle.Strikethrough) == true,
                    enabled = inlineState != null,
                    onClick = { inlineState?.toggleStyle(MarkupStyle.Strikethrough) },
                )
                FormatToggleButton(
                    icon = Res.drawable.format_ink_highlighter_24dp,
                    contentDescription = "Highlight",
                    isActive = inlineState?.hasStyle(MarkupStyle.Highlight) == true,
                    enabled = inlineState != null,
                    onClick = { inlineState?.toggleStyle(MarkupStyle.Highlight) },
                )
                FormatToggleButton(
                    icon = Res.drawable.code_24dp,
                    contentDescription = "Inline code",
                    isActive = inlineState?.hasStyle(MarkupStyle.InlineCode) == true,
                    enabled = inlineState != null,
                    onClick = { inlineState?.toggleStyle(MarkupStyle.InlineCode) },
                )

                VerticalDivider(modifier = Modifier.height(16.dp).padding(horizontal = 2.dp), color = MaterialTheme.colorScheme.outlineVariant)

                FormatToggleButton(
                    icon = Res.drawable.format_h1_24dp,
                    contentDescription = "Heading 1",
                    isActive = focusedBlock is TextBlock && focusedBlock.type == TextBlock.TextType.H1,
                    enabled = focusedId != null,
                    onClick = { focusedId?.let { blockState.conversions.toggleHeading(it, TextBlock.TextType.H1) } },
                )
                FormatToggleButton(
                    icon = Res.drawable.format_h2_24dp,
                    contentDescription = "Heading 2",
                    isActive = focusedBlock is TextBlock && focusedBlock.type == TextBlock.TextType.H2,
                    enabled = focusedId != null,
                    onClick = { focusedId?.let { blockState.conversions.toggleHeading(it, TextBlock.TextType.H2) } },
                )
                FormatToggleButton(
                    icon = Res.drawable.format_h3_24dp,
                    contentDescription = "Heading 3",
                    isActive = focusedBlock is TextBlock && focusedBlock.type == TextBlock.TextType.H3,
                    enabled = focusedId != null,
                    onClick = { focusedId?.let { blockState.conversions.toggleHeading(it, TextBlock.TextType.H3) } },
                )
                FormatToggleButton(
                    icon = Res.drawable.format_h4_24dp,
                    contentDescription = "Heading 4",
                    isActive = focusedBlock is TextBlock && focusedBlock.type == TextBlock.TextType.H4,
                    enabled = focusedId != null,
                    onClick = { focusedId?.let { blockState.conversions.toggleHeading(it, TextBlock.TextType.H4) } },
                )
                FormatToggleButton(
                    icon = Res.drawable.format_h5_24dp,
                    contentDescription = "Heading 5",
                    isActive = focusedBlock is TextBlock && focusedBlock.type == TextBlock.TextType.H5,
                    enabled = focusedId != null,
                    onClick = { focusedId?.let { blockState.conversions.toggleHeading(it, TextBlock.TextType.H5) } },
                )
                FormatToggleButton(
                    icon = Res.drawable.format_h6_24dp,
                    contentDescription = "Heading 6",
                    isActive = focusedBlock is TextBlock && focusedBlock.type == TextBlock.TextType.H6,
                    enabled = focusedId != null,
                    onClick = { focusedId?.let { blockState.conversions.toggleHeading(it, TextBlock.TextType.H6) } },
                )

                VerticalDivider(modifier = Modifier.height(16.dp).padding(horizontal = 2.dp), color = MaterialTheme.colorScheme.outlineVariant)

                FormatToggleButton(
                    icon = Res.drawable.format_list_bulleted_24dp,
                    contentDescription = "Bullet list",
                    isActive = focusedBlock is BulletListBlock,
                    enabled = focusedId != null,
                    onClick = { focusedId?.let { blockState.conversions.toggleBulletList(it) } },
                )
                FormatToggleButton(
                    icon = Res.drawable.format_list_numbered_24dp,
                    contentDescription = "Ordered list",
                    isActive = focusedBlock is OrderedListBlock,
                    enabled = focusedId != null,
                    onClick = { focusedId?.let { blockState.conversions.toggleOrderedList(it) } },
                )
                FormatToggleButton(
                    icon = Res.drawable.checklist_24dp,
                    contentDescription = "Checkbox",
                    isActive = focusedBlock is CheckboxBlock,
                    enabled = focusedId != null,
                    onClick = { focusedId?.let { blockState.toggleCheckbox(it) } },
                )
                FormatToggleButton(
                    icon = Res.drawable.format_quote_24dp,
                    contentDescription = "Blockquote",
                    isActive = focusedBlock is BlockquoteBlock,
                    enabled = focusedId != null,
                    onClick = { focusedId?.let { blockState.conversions.toggleBlockquote(it) } },
                )

                if (focusedBlock is CheckboxBlock) {
                    VerticalDivider(modifier = Modifier.height(16.dp).padding(horizontal = 2.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    FormatToggleButton(
                        icon = Res.drawable.check_box_24dp,
                        contentDescription = if (focusedBlock.isChecked) "Mark undone" else "Mark done",
                        isActive = focusedBlock.isChecked,
                        enabled = true,
                        onClick = { focusedId?.let { blockState.toggleCheckbox(it) } },
                    )
                }
            }
        }

        VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)

        IconButton(
            onClick = { blockState.undo() },
            enabled = blockState.canUndo,
            modifier = Modifier.size(36.dp).focusProperties { canFocus = false },
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            ),
        ) {
            Icon(painterResource(Res.drawable.undo_24dp), contentDescription = "Undo", modifier = Modifier.size(20.dp))
        }
        IconButton(
            onClick = { blockState.redo() },
            enabled = blockState.canRedo,
            modifier = Modifier.size(36.dp).focusProperties { canFocus = false },
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            ),
        ) {
            Icon(painterResource(Res.drawable.redo_24dp), contentDescription = "Redo", modifier = Modifier.size(20.dp))
        }
    }
}

private val DEMO_BLOCKS = listOf(
    TextBlock(state = TextFieldState(EditorConstants.ZWSP + "Hyphen Blocks"), type = TextBlock.TextType.H1),
    TextBlock(state = TextFieldState(EditorConstants.ZWSP + "A Notion-style block editor for Compose Multiplatform.")),
    TextBlock(state = TextFieldState(EditorConstants.ZWSP + "Block types"), type = TextBlock.TextType.H2),
    BulletListBlock(state = TextFieldState(EditorConstants.ZWSP + "Paragraph, H1 – H6")),
    BulletListBlock(state = TextFieldState(EditorConstants.ZWSP + "Bullet list")),
    BulletListBlock(state = TextFieldState(EditorConstants.ZWSP + "Ordered list")),
    BulletListBlock(state = TextFieldState(EditorConstants.ZWSP + "Checkbox")),
    BulletListBlock(state = TextFieldState(EditorConstants.ZWSP + "Blockquote")),
    BulletListBlock(state = TextFieldState(EditorConstants.ZWSP + "Divider")),
    TextBlock(state = TextFieldState(EditorConstants.ZWSP + "Ordered list"), type = TextBlock.TextType.H2),
    OrderedListBlock(state = TextFieldState(EditorConstants.ZWSP + "First item")),
    OrderedListBlock(state = TextFieldState(EditorConstants.ZWSP + "Second item")),
    OrderedListBlock(state = TextFieldState(EditorConstants.ZWSP + "Third item")),
    TextBlock(state = TextFieldState(EditorConstants.ZWSP + "Tasks"), type = TextBlock.TextType.H2),
    CheckboxBlock(state = TextFieldState(EditorConstants.ZWSP + "Completed task"), isChecked = true),
    CheckboxBlock(state = TextFieldState(EditorConstants.ZWSP + "Pending task"), isChecked = false),
    CheckboxBlock(state = TextFieldState(EditorConstants.ZWSP + "Another pending task"), isChecked = false),
    TextBlock(state = TextFieldState(EditorConstants.ZWSP + "Blockquote"), type = TextBlock.TextType.H2),
    BlockquoteBlock(state = TextFieldState(EditorConstants.ZWSP + "The best way to predict the future is to invent it.")),
    DividerBlock(),
    TextBlock(state = TextFieldState(EditorConstants.ZWSP + "Try typing - or # then space to trigger block conversion.")),
)