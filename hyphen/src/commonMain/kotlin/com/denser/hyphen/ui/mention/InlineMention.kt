package com.denser.hyphen.ui.mention

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.model.MarkupStyleRange
import com.denser.hyphen.state.HyphenTextState
import com.denser.hyphen.ui.internal.HyphenInlinePopup
import com.denser.hyphen.ui.internal.HyphenPointerSurface
import kotlinx.coroutines.delay

@Composable
internal fun InlineMention(
    span: MarkupStyleRange,
    state: HyphenTextState,
    mentionConfig: HyphenMentionConfig,
) {
    val mention = span.style as? MarkupStyle.Mention ?: return
    var isHovered by remember { mutableStateOf(false) }
    var showPopup by remember { mutableStateOf(false) }

    LaunchedEffect(isHovered) {
        if (isHovered) {
            delay(400)
            showPopup = true
        } else {
            showPopup = false
        }
    }

    var menuOffset by remember { mutableStateOf(Offset.Zero) }
    var showMenu by remember { mutableStateOf(false) }

    HyphenPointerSurface(
        span = span,
        onHoverChanged = { isHovered = it },
        onClick = { isCtrl, isRight, offset ->
            if (isRight && state.selection.collapsed) {
                menuOffset = offset
                showMenu = true
                true
            } else if (!isRight && state.selection.collapsed) {
                mentionConfig.onMentionClick(mention)
                false
            } else {
                false
            }
        },
        pointerIcon = androidx.compose.ui.input.pointer.PointerIcon.Hand
    ) {
        if (showPopup) {
            HyphenInlinePopup {
                mentionConfig.hoverCardContent(mention)
            }
        }

        if (showMenu && mentionConfig.dropdownContent != null) {
            mentionConfig.dropdownContent.invoke(
                span,
                menuOffset,
                { showMenu = false }
            )
        }
    }
}
