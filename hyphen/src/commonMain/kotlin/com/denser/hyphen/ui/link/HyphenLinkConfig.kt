package com.denser.hyphen.ui.link

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import com.denser.hyphen.model.MarkupStyleRange

/**
 * Interaction configuration for link spans in the Hyphen editor.
 *
 * Use this to customize how link taps, context menus, and the edit dialog behave.
 * Any slot left as `null` falls back to the default built-in implementation.
 *
 * **Interaction Model (Existing)**
 *
 * | Platform        | Primary Interaction (Open) | Secondary Interaction (Menu) |
 * |-----------------|---------------------------|------------------------------|
 * | Desktop / Web   | Ctrl / Cmd + Click        | Right-click                  |
 * | Mobile (Android)| Press (Cursor placement)  | Long-press                   |
 *
 * **Minimal usage — override only the URL-open behaviour:**
 * ```kotlin
 * HyphenBasicTextEditor(
 *     state = state,
 *     linkConfig = HyphenLinkConfig(
 *         onFollowLink = { url -> myAnalytics.track(url); openBrowser(url) }
 *     )
 * )
 * ```
 *
 * **Full usage — custom dropdown and dialog:**
 * ```kotlin
 * HyphenBasicTextEditor(
 *     state = state,
 *     linkConfig = HyphenLinkConfig(
 *         dropdownContent = { span, menuOffset, onDismiss, onEditRequest ->
 *             MyLinkMenu(
 *                 url = (span.style as MarkupStyle.Link).url,
 *                 onDismiss = onDismiss,
 *                 onEdit = onEditRequest,
 *             )
 *         },
 *         dialogContent = { span, onDismiss, onConfirm ->
 *             MyLinkDialog(
 *                 span = span,
 *                 onDismiss = onDismiss,
 *                 onConfirm = onConfirm,
 *             )
 *         },
 *     )
 * )
 * ```
 *
 * @param onFollowLink Called when a "follow link" action is triggered (e.g., Ctrl+Click on
 *   desktop, or via the default dropdown menu). If `null`, uses [androidx.compose.ui.platform.LocalUriHandler].
 * @param dropdownContent Composable slot that replaces the built-in context menu.
 *   Receives the [MarkupStyleRange], the [Offset] of the click, and callbacks to dismiss
 *   the menu or request opening the edit dialog.
 * @param dialogContent Composable slot that replaces the built-in link edit dialog.
 *   Receives the [MarkupStyleRange] being edited and callbacks for dismissal and confirmation.
 */
data class HyphenLinkConfig(
    val onFollowLink: ((url: String) -> Unit)? = null,
    val dropdownContent: (@Composable (
        span: MarkupStyleRange,
        menuOffset: Offset,
        onDismiss: () -> Unit,
        onEditRequest: (MarkupStyleRange) -> Unit,
    ) -> Unit)? = null,
    val dialogContent: (@Composable (
        span: MarkupStyleRange,
        onDismiss: () -> Unit,
        onConfirm: (newText: String, newUrl: String) -> Unit,
    ) -> Unit)? = null,
)