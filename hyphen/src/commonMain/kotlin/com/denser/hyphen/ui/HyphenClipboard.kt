package com.denser.hyphen.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.Clipboard

/**
 * A [androidx.compose.runtime.CompositionLocal] that provides the raw, underlying [Clipboard]
 * as it existed **before** [HyphenBasicTextEditor] wrapped it with its markdown-serialization
 * interceptor.
 *
 * Use this whenever you need to write a value to the system clipboard that should NOT be
 * treated as a selection copy from the editor.  A common example is a "Copy link URL" action
 * that appears in a context menu anchored to an editor link: the editor has an active selection
 * over the link at that point, but you want to put the raw URL string on the clipboard rather
 * than the markdown representation of the selected text.
 *
 * ### Usage
 *
 * Inside any composable that is in the composition subtree of [HyphenBasicTextEditor]:
 *
 * ```kotlin
 * val rawClipboard = LocalHyphenRawClipboard.current
 *
 * Button(onClick = {
 *     coroutineScope.launch {
 *         rawClipboard?.setClipEntry(
 *             ClipEntry(ClipData.newPlainText("URL", url))
 *         )
 *     }
 * }) {
 *     Text("Copy URL")
 * }
 * ```
 *
 * If the composable is rendered **outside** a [HyphenBasicTextEditor] the value is `null`;
 * fall back to [androidx.compose.ui.platform.LocalClipboard] in that case.
 */
val LocalHyphenRawClipboard = compositionLocalOf<Clipboard?> { null }
