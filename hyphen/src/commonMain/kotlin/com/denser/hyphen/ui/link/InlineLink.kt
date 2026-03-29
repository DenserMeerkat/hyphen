package com.denser.hyphen.ui.link

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.denser.hyphen.Res
import com.denser.hyphen.edit_square_24dp
import com.denser.hyphen.open_in_new_24dp
import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.model.MarkupStyleRange
import com.denser.hyphen.state.HyphenTextState


@Composable
internal fun InlineLink(
    span: MarkupStyleRange,
    state: HyphenTextState,
    linkConfig: HyphenLinkConfig,
) {
    val linkStyle = span.style as? MarkupStyle.Link ?: return
    val uriHandler = LocalUriHandler.current

    var showMenu by remember { mutableStateOf(false) }
    var menuOffset by remember { mutableStateOf(Offset.Zero) }

    var spanForDialog by remember { mutableStateOf<MarkupStyleRange?>(null) }

    val openUrl: () -> Unit = {
        val url = linkStyle.url
        if (url.isNotBlank()) {
            linkConfig.onFollowLink?.invoke(url) ?: uriHandler.openUri(url)
        }
    }

    val requestEdit: (MarkupStyleRange) -> Unit = { target ->
        if (linkConfig.dialogContent != null) {
            spanForDialog = target
        } else {
            state.activeLinkForEditing = target
        }
    }

    LinkPointerSurface(
        span = span,
        onOpenUrl = openUrl,
        onShowMenu = { pressOffset ->
            val currentStart = state.selection.start
            state.textFieldState.edit {
                selection = TextRange(currentStart)
            }

            menuOffset = pressOffset
            showMenu = true
        },
    ) {
        if (showMenu) {
            if (linkConfig.dropdownContent != null) {
                linkConfig.dropdownContent.invoke(
                    span,
                    { showMenu = false },
                    { target ->
                        showMenu = false
                        requestEdit(target)
                    },
                )
            } else {
                DefaultLinkDropdown(
                    span = span,
                    menuOffset = menuOffset,
                    onDismiss = { showMenu = false },
                    onFollowLink = {
                        showMenu = false
                        openUrl()
                    },
                    onEditLink = { target ->
                        showMenu = false
                        requestEdit(target)
                    },
                )
            }
        }

        val dialogSpan = spanForDialog
        if (dialogSpan != null && linkConfig.dialogContent != null) {
            linkConfig.dialogContent.invoke(
                dialogSpan,
                { spanForDialog = null },
                { newText, newUrl ->
                    state.updateLink(dialogSpan, newText, newUrl)
                    spanForDialog = null
                },
            )
        }
    }
}

@Composable
private fun DefaultLinkDropdown(
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