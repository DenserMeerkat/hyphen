package com.denser.hyphen.ui.mention

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.model.MarkupStyleRange

/**
 * Configuration for mention interactions and rendering.
 *
 * @property onMentionClick Callback invoked when a mention is clicked or tapped.
 * @property hoverCardContent Composable content shown in a hover card when the pointer
 *   is over the mention.
 * @property dropdownContent Optional composable for a context menu (right-click/long-press).
 */
@Immutable
data class HyphenMentionConfig(
    val onMentionClick: (MarkupStyle.Mention) -> Unit = {},
    val hoverCardContent: @Composable (MarkupStyle.Mention) -> Unit = {},
    val dropdownContent: (@Composable (
        span: MarkupStyleRange,
        menuOffset: Offset,
        onDismiss: () -> Unit,
    ) -> Unit)? = null
)
