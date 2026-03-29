package com.denser.hyphen.ui.style

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

/**
 * Visual style applied to the prefix marker and content text of a list item.
 *
 * Used for [HyphenStyleConfig.bulletListStyle] and [HyphenStyleConfig.orderedListStyle].
 * Checkboxes do not use this type — see [HyphenStyleConfig.checkboxCheckedStyle].
 *
 * @property prefixStyle [SpanStyle] applied to the marker (e.g. `- `, `1.`).
 *   Defaults to `null` (inherits base [androidx.compose.ui.text.TextStyle]).
 * @property contentStyle [SpanStyle] applied to the text after the marker.
 *   Defaults to `null` (inherits base [androidx.compose.ui.text.TextStyle]).
 */
data class ListItemStyle(
    val prefixStyle: SpanStyle? = null,
    val contentStyle: SpanStyle? = null,
)

/**
 * Visual configuration for the Hyphen editor's inline and block formatting styles.
 *
 * **Checkbox rendering**
 *
 * Checkbox items are always rendered with a Material3 [androidx.compose.material3.Checkbox]
 * widget overlaid on the text field. The raw `- [ ] ` / `- [x] ` prefix is collapsed to
 * zero visual width via `fontSize = 0.sp` so it never appears. The widget appearance is
 * driven by your Material3 theme — the only text-level customization point is the label
 * of checked items via [checkboxCheckedStyle]:
 *
 * ```kotlin
 * HyphenBasicTextEditor(
 *     state = state,
 *     styleConfig = HyphenStyleConfig(
 *         checkboxCheckedStyle = SpanStyle(
 *             textDecoration = TextDecoration.LineThrough,
 *             color = Color.Gray,
 *         ),
 *     ),
 * )
 * ```
 *
 * @property boldStyle Applied to [com.denser.hyphen.model.MarkupStyle.Bold] spans.
 * @property italicStyle Applied to [com.denser.hyphen.model.MarkupStyle.Italic] spans.
 * @property underlineStyle Applied to [com.denser.hyphen.model.MarkupStyle.Underline] spans.
 * @property strikethroughStyle Applied to [com.denser.hyphen.model.MarkupStyle.Strikethrough] spans.
 * @property highlightStyle Applied to [com.denser.hyphen.model.MarkupStyle.Highlight] spans.
 * @property inlineCodeStyle Applied to [com.denser.hyphen.model.MarkupStyle.InlineCode] spans.
 * @property blockquoteSpanStyle Applied to [com.denser.hyphen.model.MarkupStyle.Blockquote] spans.
 * @property bulletListStyle Controls bullet list item appearance (prefix + content).
 * @property orderedListStyle Controls ordered list item appearance (prefix + content).
 * @property checkboxCheckedStyle [SpanStyle] applied to the label text of checked
 *   items only. Defaults to [TextDecoration.LineThrough]. Set to `null` to use the base style.
 * @property checkboxUncheckedStyle [SpanStyle] applied to the label text of unchecked
 *   items only. Defaults to `null`.
 * @property h1Style Applied to [com.denser.hyphen.model.MarkupStyle.H1] spans.
 * @property h2Style Applied to [com.denser.hyphen.model.MarkupStyle.H2] spans.
 * @property h3Style Applied to [com.denser.hyphen.model.MarkupStyle.H3] spans.
 * @property h4Style Applied to [com.denser.hyphen.model.MarkupStyle.H4] spans.
 * @property h5Style Applied to [com.denser.hyphen.model.MarkupStyle.H5] spans.
 * @property h6Style Applied to [com.denser.hyphen.model.MarkupStyle.H6] spans.
 * @property linkStyle Applied to [com.denser.hyphen.model.MarkupStyle.Link] spans.
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
        color = Color.Gray,
        background = Color.Gray.copy(alpha = 0.05f),
    ),
    val bulletListStyle: ListItemStyle = ListItemStyle(),
    val orderedListStyle: ListItemStyle = ListItemStyle(),
    val checkboxCheckedStyle: SpanStyle? = SpanStyle(
        textDecoration = TextDecoration.LineThrough,
    ),
    val checkboxUncheckedStyle: SpanStyle? = null,
    val h1Style: SpanStyle = SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
    val h2Style: SpanStyle = SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    val h3Style: SpanStyle = SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
    val h4Style: SpanStyle = SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
    val h5Style: SpanStyle = SpanStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold),
    val h6Style: SpanStyle = SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
    val linkStyle: SpanStyle = SpanStyle(
        color = Color.Blue,
        textDecoration = TextDecoration.Underline,
    ),
)