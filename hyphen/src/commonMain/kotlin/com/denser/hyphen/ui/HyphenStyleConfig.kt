package com.denser.hyphen.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

/**
 * Visual configuration for the Hyphen editor's inline and block formatting styles.
 *
 * Each property maps a [com.denser.hyphen.model.MarkupStyle] variant to a Compose [SpanStyle] that is applied by
 * the editor's `outputTransformation` whenever the corresponding span is active. Customize
 * any field to match your design system — for example, to use a brand accent color for
 * highlights or a custom monospace font for inline code.
 *
 * ```kotlin
 * HyphenBasicTextEditor(
 *     state = state,
 *     styleConfig = HyphenStyleConfig(
 *         boldStyle = SpanStyle(
 *             fontWeight = FontWeight.ExtraBold,
 *             color = Color(0xFF1A73E8),
 *         ),
 *         highlightStyle = SpanStyle(
 *             background = Color(0xFFFFF176),
 *         ),
 *     ),
 * )
 * ```
 *
 * All properties have sensible defaults so only the fields you want to override need to be
 * specified.
 *
 * @property boldStyle [SpanStyle] applied to [com.denser.hyphen.model.MarkupStyle.Bold] spans.
 *   Defaults to [FontWeight.Bold].
 * @property italicStyle [SpanStyle] applied to [com.denser.hyphen.model.MarkupStyle.Italic] spans.
 *   Defaults to [FontStyle.Italic].
 * @property underlineStyle [SpanStyle] applied to [com.denser.hyphen.model.MarkupStyle.Underline] spans.
 *   Defaults to [TextDecoration.Underline].
 * @property strikethroughStyle [SpanStyle] applied to [com.denser.hyphen.model.MarkupStyle.Strikethrough] spans.
 *   Defaults to [TextDecoration.LineThrough].
 * @property highlightStyle [SpanStyle] applied to [com.denser.hyphen.model.MarkupStyle.Highlight] spans.
 *   Defaults to a semi-transparent yellow background.
 * @property inlineCodeStyle [SpanStyle] applied to [com.denser.hyphen.model.MarkupStyle.InlineCode] spans.
 *   Defaults to [FontFamily.Monospace] with a light grey background.
 * @property blockquoteSpanStyle [SpanStyle] applied to [com.denser.hyphen.model.MarkupStyle.Blockquote] spans.
 *   Defaults to italic text in [Color.DarkGray] with a faint grey background. Note that
 *   block-level decoration (e.g. a vertical bar) must be added separately via a custom
 *   layout or draw modifier, as [SpanStyle] only controls character-level appearance.
 */
data class HyphenStyleConfig(
    val boldStyle: SpanStyle = SpanStyle(fontWeight = FontWeight.Bold),
    val italicStyle: SpanStyle = SpanStyle(fontStyle = FontStyle.Italic),
    val underlineStyle: SpanStyle = SpanStyle(textDecoration = TextDecoration.Underline),
    val strikethroughStyle: SpanStyle = SpanStyle(textDecoration = TextDecoration.LineThrough),
    val highlightStyle: SpanStyle = SpanStyle(background = Color(0xFFFFEB3B).copy(alpha = 0.4f)),
    val inlineCodeStyle: SpanStyle = SpanStyle(
        background = Color.Gray.copy(alpha = 0.15f),
        fontFamily = FontFamily.Monospace,
    ),
    val blockquoteSpanStyle: SpanStyle = SpanStyle(
        fontStyle = FontStyle.Italic,
        color = Color.DarkGray,
        background = Color.Gray.copy(alpha = 0.05f),
    ),
)