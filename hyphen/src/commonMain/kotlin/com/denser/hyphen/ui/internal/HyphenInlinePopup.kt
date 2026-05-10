package com.denser.hyphen.ui.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

/**
 * A shared popup component for all inline elements.
 * Handles boundary detection, auto-flipping, and margin offsets.
 */
@Composable
fun HyphenInlinePopup(
    onDismiss: () -> Unit = {},
    focusable: Boolean = false,
    content: @Composable () -> Unit
) {
    val popupPositionProvider = remember {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset {
                val screenPadding = 8
                val verticalGap = 4
                var x = anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2
                x = x.coerceIn(screenPadding, windowSize.width - popupContentSize.width - screenPadding)

                var y = anchorBounds.bottom + verticalGap
                if (y + popupContentSize.height > windowSize.height - screenPadding) {
                    y = anchorBounds.top - popupContentSize.height - verticalGap
                }

                y = y.coerceIn(screenPadding, windowSize.height - popupContentSize.height - screenPadding)
                
                return IntOffset(x, y)
            }
        }
    }

    Popup(
        popupPositionProvider = popupPositionProvider,
        onDismissRequest = onDismiss,
        properties = PopupProperties(
            focusable = focusable,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        content()
    }
}
