package com.denser.hyphen.ui.internal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.*
import com.denser.hyphen.model.MarkupStyleRange

@Composable
actual fun HyphenPointerSurface(
    span: MarkupStyleRange,
    onHoverChanged: (Boolean) -> Unit,
    onClick: (isCtrlPressed: Boolean, isRightClick: Boolean, offset: Offset) -> Boolean,
    pointerIcon: PointerIcon?,
    content: @Composable BoxScope.() -> Unit,
) {
    var isModifierDown by remember { mutableStateOf(false) }
    var isHandlingGesture by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .pointerHoverIcon(
                pointerIcon ?: if (isModifierDown) PointerIcon.Hand else PointerIcon.Text
            )
            .sharedPointerInput(span, pass = PointerEventPass.Initial) { event ->
                isModifierDown = event.keyboardModifiers.isCtrlPressed ||
                        event.keyboardModifiers.isMetaPressed

                when (event.type) {
                    PointerEventType.Enter -> onHoverChanged(true)
                    PointerEventType.Exit -> onHoverChanged(false)
                    PointerEventType.Press -> {
                        val position = event.changes.firstOrNull()?.position ?: Offset.Zero
                        
                        val handled = onClick(
                            isModifierDown,
                            event.buttons.isSecondaryPressed,
                            position
                        )
                        
                        if (handled) {
                            event.changes.forEach { it.consume() }
                            isHandlingGesture = true
                        } else {
                            isHandlingGesture = false
                        }
                    }
                    PointerEventType.Release -> {
                        if (isHandlingGesture) {
                            event.changes.forEach { it.consume() }
                        }
                        isHandlingGesture = false
                    }
                }
            },
        content = content,
    )
}
