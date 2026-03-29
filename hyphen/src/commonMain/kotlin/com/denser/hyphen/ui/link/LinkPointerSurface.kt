package com.denser.hyphen.ui.link

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import com.denser.hyphen.model.MarkupStyleRange

/**
 * Platform-specific pointer behaviour for link spans.
 *
 * Each platform implementation must:
 * - Allow clicks / presses to pass through so the underlying [androidx.compose.foundation.text.BasicTextField] can place
 *   the cursor inside the link text.
 * - Trigger [onOpenUrl] on the platform's primary "open" gesture (Ctrl+click on
 *   desktop/web, dedicated button on mobile).
 * - Trigger [onShowMenu] on the platform's secondary gesture (right-click on
 *   desktop/web, long-press on mobile).
 */
@Composable
internal expect fun LinkPointerSurface(
    span: MarkupStyleRange,
    onOpenUrl: () -> Unit,
    onShowMenu: (pressOffset: Offset) -> Unit,
    content: @Composable BoxScope.() -> Unit,
)
