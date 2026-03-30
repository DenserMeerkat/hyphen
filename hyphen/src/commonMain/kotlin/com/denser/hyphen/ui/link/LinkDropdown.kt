package com.denser.hyphen.ui.link

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.denser.hyphen.Res
import com.denser.hyphen.edit_square_24dp
import com.denser.hyphen.open_in_new_24dp
import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.model.MarkupStyleRange
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun LinkDropdown(
    span: MarkupStyleRange,
    menuOffset: Offset,
    onDismiss: () -> Unit,
    onFollowLink: () -> Unit,
    onEditLink: (MarkupStyleRange) -> Unit,
) {
    val density = LocalDensity.current
    val dpOffset = with(density) { DpOffset(menuOffset.x.toDp(), menuOffset.y.toDp()) }
    val linkStyle = span.style as? MarkupStyle.Link

    DropdownMenu(
        expanded = true,
        offset = dpOffset,
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.medium,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 3.dp,
    ) {
        if (!linkStyle?.url.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .widthIn(max = 240.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    Text(
                        text = linkStyle.url,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }

        DropdownMenuItem(
            text = {
                Text(
                    "Follow Link",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.open_in_new_24dp),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            },
            onClick = onFollowLink,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        )
        DropdownMenuItem(
            text = {
                Text(
                    "Edit Link",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.edit_square_24dp),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            },
            onClick = { onEditLink(span) },
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        )
    }
}
