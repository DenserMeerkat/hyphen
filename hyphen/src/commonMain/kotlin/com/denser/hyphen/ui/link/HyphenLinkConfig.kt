package com.denser.hyphen.ui.link

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import com.denser.hyphen.model.MarkupStyleRange

/**
 * Callbacks and composable slots that control how link interactions are handled in the
 * Hyphen editor.
 *
 * Pass a customized instance to [com.denser.hyphen.ui.HyphenBasicTextEditor] via the `linkConfig` parameter to
 * override the built-in dropdown menu, edit dialog, or both. Any slot left as `null` falls
 * back to the default built-in implementation.
 *
 * **Default interaction model (built-in)**
 *
 * | Platform | Interaction | Result |
 * |---|---|---|
 * | Desktop / Web | Click | Place cursor inside link text |
 * | Desktop / Web | Ctrl / Cmd + Click | Open URL in browser |
 * | Desktop / Web | Right-click | Show context dropdown |
 * | Mobile | Press | Place cursor inside link text |
 * | Mobile | Long-press | Show context dropdown |
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
 *             // your own DropdownMenu / BottomSheet / etc.
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
 * @property onFollowLink Called when the user triggers "open URL" (Ctrl+click on
 *   desktop/web, or the "Follow Link" menu item on all platforms). The [String] argument
 *   is the raw URL stored in [com.denser.hyphen.model.MarkupStyle.Link.url]. Defaults to
 *   `null`, which uses [androidx.compose.ui.platform.LocalUriHandler] to open the URL.
 * @property dropdownContent Composable slot that replaces the built-in link context menu.
 *   When non-null it is invoked instead of the default [androidx.compose.material3.DropdownMenu].
 *   Parameters:
 *   - `span` — the [com.denser.hyphen.model.MarkupStyleRange] of the link that was interacted with.
 *   - `menuOffset` — the [androidx.compose.ui.geometry.Offset] where the interaction occurred, used to position the dropdown.
 *   - `onDismiss` — call this to close the menu without any further action.
 *   - `onEditRequest` — call this to open the edit dialog (built-in or custom) for `span`.
 * @property dialogContent Composable slot that replaces the built-in link edit dialog.
 *   When non-null it is invoked instead of the default [androidx.compose.material3.AlertDialog].
 *   Parameters:
 *   - `span` — the [com.denser.hyphen.model.MarkupStyleRange] being edited (contains current text bounds and URL).
 *   - `onDismiss` — call this to close the dialog without saving.
 *   - `onConfirm` — call this with `(newText, newUrl)` to commit the edit via
 *     [com.denser.hyphen.state.HyphenTextState.updateLink].
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