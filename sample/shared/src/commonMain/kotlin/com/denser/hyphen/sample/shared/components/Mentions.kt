package com.denser.hyphen.sample.shared.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.ui.style.HyphenStyleConfig
import hyphen.sample.shared.generated.resources.Res
import hyphen.sample.shared.generated.resources.bolt_24dp
import hyphen.sample.shared.generated.resources.content_copy_24dp
import hyphen.sample.shared.generated.resources.label_24dp
import hyphen.sample.shared.generated.resources.mail_24dp
import hyphen.sample.shared.generated.resources.notifications_24dp
import hyphen.sample.shared.generated.resources.person_24dp
import hyphen.sample.shared.generated.resources.refresh_24dp
import hyphen.sample.shared.generated.resources.restore_page_24dp
import hyphen.sample.shared.generated.resources.search_24dp
import org.jetbrains.compose.resources.painterResource

fun getSampleStyleConfig(): HyphenStyleConfig {
    return HyphenStyleConfig(
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
    )
}

@Composable
fun SampleMentionDropdown(
    mention: MarkupStyle.Mention,
    offset: Offset,
    onDismiss: () -> Unit
) {
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
}

@Composable
fun SampleMentionHoverCard(mention: MarkupStyle.Mention) {
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
