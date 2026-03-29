package com.denser.hyphen.ui.link

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.*
import com.denser.hyphen.model.MarkupStyleRange

@Composable
internal actual fun LinkPointerSurface(
    span: MarkupStyleRange,
    onOpenUrl: () -> Unit,
    onShowMenu: (pressOffset: Offset) -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    var isModifierDown by remember { mutableStateOf(false) }
    var isHandlingGesture by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .pointerHoverIcon(
                if (isModifierDown) PointerIcon.Hand else PointerIcon.Text,
                overrideDescendants = false,
            )
            .sharedPointerInput(span, pass = PointerEventPass.Initial) { event ->
                isModifierDown = event.keyboardModifiers.isCtrlPressed ||
                        event.keyboardModifiers.isMetaPressed

                if (event.type == PointerEventType.Press) {
                    val position = event.changes.firstOrNull()?.position ?: return@sharedPointerInput

                    when {
                        event.buttons.isSecondaryPressed -> {
                            event.changes.forEach { it.consume() }
                            isHandlingGesture = true
                            onShowMenu(position)
                        }
                        event.buttons.isPrimaryPressed && isModifierDown -> {
                            event.changes.forEach { it.consume() }
                            isHandlingGesture = true
                            onOpenUrl()
                        }
                        else -> {
                            isHandlingGesture = false
                        }
                    }
                } else if (event.type == PointerEventType.Release) {
                    if (isHandlingGesture) {
                        event.changes.forEach { it.consume() }
                    }
                    isHandlingGesture = false
                }
            },
        content = content,
    )
}