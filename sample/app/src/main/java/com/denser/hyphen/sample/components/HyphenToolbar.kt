package com.denser.hyphen.sample.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.sample.R
import com.denser.hyphen.state.HyphenTextState

@Composable
fun HyphenToolbar(
    state: HyphenTextState,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val containerColor = MaterialTheme.colorScheme.surfaceContainer

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = containerColor)
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FormatToggleButton(
                    icon = R.drawable.format_bold_24px,
                    contentDescription = "Bold",
                    isActive = state.hasStyle(MarkupStyle.Bold),
                    onClick = { state.toggleStyle(MarkupStyle.Bold) }
                )

                FormatToggleButton(
                    icon = R.drawable.format_italic_24px,
                    contentDescription = "Italic",
                    isActive = state.hasStyle(MarkupStyle.Italic),
                    onClick = { state.toggleStyle(MarkupStyle.Italic) }
                )

                FormatToggleButton(
                    icon = R.drawable.format_underlined_24px,
                    contentDescription = "Underline",
                    isActive = state.hasStyle(MarkupStyle.Underline),
                    onClick = { state.toggleStyle(MarkupStyle.Underline) }
                )

                FormatToggleButton(
                    icon = R.drawable.format_strikethrough_24px,
                    contentDescription = "Strikethrough",
                    isActive = state.hasStyle(MarkupStyle.Strikethrough),
                    onClick = { state.toggleStyle(MarkupStyle.Strikethrough) }
                )

                FormatToggleButton(
                    icon = R.drawable.format_ink_highlighter_24px,
                    isActive = state.hasStyle(MarkupStyle.Highlight),
                    onClick = { state.toggleStyle(MarkupStyle.Highlight) }
                )

                FormatToggleButton(
                    icon = R.drawable.format_quote_24px,
                    contentDescription = "Blockquote",
                    isActive = state.hasStyle(MarkupStyle.Blockquote),
                    onClick = { state.toggleStyle(MarkupStyle.Blockquote) }
                )

                FormatToggleButton(
                    icon = R.drawable.code_24px,
                    contentDescription = "Inline Code",
                    isActive = state.hasStyle(MarkupStyle.InlineCode),
                    onClick = { state.toggleStyle(MarkupStyle.InlineCode) }
                )

                FormatToggleButton(
                    icon = R.drawable.format_list_bulleted_24px,
                    contentDescription = "Bullet List",
                    isActive = state.hasStyle(MarkupStyle.BulletList),
                    onClick = { state.toggleStyle(MarkupStyle.BulletList) }
                )

                FormatToggleButton(
                    icon = R.drawable.format_list_numbered_24px,
                    contentDescription = "Ordered List",
                    isActive = state.hasStyle(MarkupStyle.OrderedList),
                    onClick = { state.toggleStyle(MarkupStyle.OrderedList) }
                )
                Spacer(Modifier.width(4.dp))
            }
        }

        VerticalDivider(
            modifier = Modifier
                .height(24.dp)
                .padding(horizontal = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        IconButton(
            onClick = { state.undo() },
            enabled = state.canUndo,
            modifier = Modifier.size(36.dp),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        ) {
            Icon(
                painterResource(R.drawable.undo_24px),
                contentDescription = "Undo",
                modifier = Modifier.size(20.dp)
            )
        }

        IconButton(
            onClick = { state.redo() },
            enabled = state.canRedo,
            modifier = Modifier.size(36.dp),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        ) {
            Icon(
                painterResource(R.drawable.redo_24px),
                contentDescription = "Redo",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun FormatToggleButton(
    isActive: Boolean,
    onClick: () -> Unit,
    icon: Int,
    contentDescription: String? = null
) {
    IconToggleButton(
        checked = isActive,
        onCheckedChange = { onClick() },
        modifier = Modifier.size(40.dp),
        shape = RoundedCornerShape(12.dp),
        colors = IconButtonDefaults.iconToggleButtonColors(
            checkedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            checkedContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Icon(
            painterResource(icon),
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp)
        )
    }
}