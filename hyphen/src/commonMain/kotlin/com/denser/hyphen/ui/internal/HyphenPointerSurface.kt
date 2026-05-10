package com.denser.hyphen.ui.internal

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerIcon
import com.denser.hyphen.model.MarkupStyleRange

/**
 * A unified interaction surface for all inline elements (links, mentions, etc.).
 * Handles hover, click, right-click, and modifier keys (Ctrl/Meta).
 */
@Composable
expect fun HyphenPointerSurface(
    span: MarkupStyleRange,
    onHoverChanged: (Boolean) -> Unit,
    onClick: (isCtrlPressed: Boolean, isRightClick: Boolean, offset: Offset) -> Boolean,
    pointerIcon: PointerIcon? = null,
    content: @Composable BoxScope.() -> Unit,
)
