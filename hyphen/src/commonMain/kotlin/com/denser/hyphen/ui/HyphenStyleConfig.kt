package com.denser.hyphen.ui

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
 * Used independently per list type via [HyphenStyleConfig.bulletListStyle],
 * [HyphenStyleConfig.orderedListStyle], [HyphenStyleConfig.checkboxUncheckedStyle],
 * and [HyphenStyleConfig.checkboxCheckedStyle].
 *
 * ```kotlin
 * HyphenBasicTextEditor(
 *     state = state,
 *     styleConfig = HyphenStyleConfig(
 *         bulletListStyle = ListItemStyle(
 *             prefixStyle = SpanStyle(color = MaterialTheme.colorScheme.primary),
 *             contentStyle = SpanStyle(fontStyle = FontStyle.Italic),
 *         ),
 *         checkboxCheckedStyle = ListItemStyle(
 *             prefixStyle = SpanStyle(color = MaterialTheme.colorScheme.primary),
 *             contentStyle = SpanStyle(
 *                 textDecoration = TextDecoration.LineThrough,
 *                 color = Color.Gray,
 *             ),
 *         ),
 *     ),
 * )
 * ```
 *
 * @property prefixStyle [SpanStyle] applied to the marker portion of the list item —
 *   the bullet (`-`), number (`1.`), or raw checkbox syntax (`- [ ] ` / `- [x] `). Defaults to
 *   `null`, which inherits the base [androidx.compose.ui.text.TextStyle] set on the editor.
 * @property contentStyle [SpanStyle] applied to the text content after the marker.
 *   Defaults to `null`, which inherits the base [androidx.compose.ui.text.TextStyle]
 *   set on the editor with no modification.
 */
data class ListItemStyle(
    val prefixStyle: SpanStyle? = null,
    val contentStyle: SpanStyle? = null,
)

/**
 * Visual configuration for the Hyphen editor's inline and block formatting styles.
 *
 * Each property maps a [com.denser.hyphen.core.model.MarkupStyle] variant to a Compose [SpanStyle] that is applied by
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
 *         checkboxUncheckedStyle = ListItemStyle(
 *             prefixStyle = SpanStyle(color = Color.Gray),
 *         ),
 *         checkboxCheckedStyle = ListItemStyle(
 *             prefixStyle = SpanStyle(color = MaterialTheme.colorScheme.primary),
 *             contentStyle = SpanStyle(
 *                 textDecoration = TextDecoration.LineThrough,
 *                 color = Color.Gray,
 *             ),
 *         ),
 *     ),
 * )
 * ```
 *
 * All properties have sensible defaults so only the fields you want to override need to be
 * specified.
 *
 * @property boldStyle [SpanStyle] applied to [com.denser.hyphen.core.model.MarkupStyle.Bold] spans.
 *   Defaults to [FontWeight.Bold].
 * @property italicStyle [SpanStyle] applied to [com.denser.hyphen.core.model.MarkupStyle.Italic] spans.
 *   Defaults to [FontStyle.Italic].
 * @property underlineStyle [SpanStyle] applied to [com.denser.hyphen.core.model.MarkupStyle.Underline] spans.
 *   Defaults to [TextDecoration.Underline].
 * @property strikethroughStyle [SpanStyle] applied to [com.denser.hyphen.core.model.MarkupStyle.Strikethrough] spans.
 *   Defaults to [TextDecoration.LineThrough].
 * @property highlightStyle [SpanStyle] applied to [com.denser.hyphen.core.model.MarkupStyle.Highlight] spans.
 *   Defaults to a semi-transparent yellow background.
 * @property inlineCodeStyle [SpanStyle] applied to [com.denser.hyphen.core.model.MarkupStyle.InlineCode] spans.
 *   Defaults to [FontFamily.Monospace] with a light grey background.
 * @property blockquoteSpanStyle [SpanStyle] applied to [com.denser.hyphen.core.model.MarkupStyle.Blockquote] spans.
 *   Defaults to italic text in [Color.Gray] with a faint grey background. Note that
 *   block-level decoration (e.g. a vertical bar) must be added separately via a custom
 *   layout or draw modifier, as [SpanStyle] only controls character-level appearance.
 * @property bulletListStyle [ListItemStyle] controlling the visual appearance of
 *   [com.denser.hyphen.core.model.MarkupStyle.BulletList] items. [ListItemStyle.prefixStyle] is applied to
 *   the `- ` marker; [ListItemStyle.contentStyle] is applied to the text that follows.
 * @property orderedListStyle [ListItemStyle] controlling the visual appearance of
 *   [com.denser.hyphen.core.model.MarkupStyle.OrderedList] items. [ListItemStyle.prefixStyle] is applied to
 *   the number and period (e.g. `1.`); [ListItemStyle.contentStyle] is applied to the text
 *   that follows.
 * @property checkboxUncheckedStyle [ListItemStyle] controlling the visual appearance of
 *   unchecked checkbox items (`- [ ]`). [ListItemStyle.prefixStyle] is applied to the symbol;
 *   [ListItemStyle.contentStyle] is applied to the label text.
 * @property checkboxCheckedStyle [ListItemStyle] controlling the visual appearance of
 *   checked checkbox items (`- [x]`). [ListItemStyle.prefixStyle] is applied to the symbol;
 *   [ListItemStyle.contentStyle] is applied to the label text. A common pattern is to set
 *   `contentStyle = SpanStyle(textDecoration = TextDecoration.LineThrough, color = Color.Gray)`
 *   to visually strike through completed items.
 * @property h1Style [SpanStyle] applied to [com.denser.hyphen.core.model.MarkupStyle.H1] spans.
 *   Defaults to 24 sp bold. Triggered by `# ` at the start of a line.
 * @property h2Style [SpanStyle] applied to [com.denser.hyphen.core.model.MarkupStyle.H2] spans.
 *   Defaults to 22 sp bold. Triggered by `## ` at the start of a line.
 * @property h3Style [SpanStyle] applied to [com.denser.hyphen.core.model.MarkupStyle.H3] spans.
 *   Defaults to 20 sp bold. Triggered by `### ` at the start of a line.
 * @property h4Style [SpanStyle] applied to [com.denser.hyphen.core.model.MarkupStyle.H4] spans.
 *   Defaults to 18 sp bold. Triggered by `#### ` at the start of a line.
 * @property h5Style [SpanStyle] applied to [com.denser.hyphen.core.model.MarkupStyle.H5] spans.
 *   Defaults to 17 sp bold. Triggered by `##### ` at the start of a line.
 * @property h6Style [SpanStyle] applied to [com.denser.hyphen.core.model.MarkupStyle.H6] spans.
 *   Defaults to 16 sp bold. Triggered by `###### ` at the start of a line.
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
    val checkboxUncheckedStyle: ListItemStyle = ListItemStyle(),
    val checkboxCheckedStyle: ListItemStyle = ListItemStyle(
        contentStyle = SpanStyle(textDecoration = TextDecoration.LineThrough)
    ),
    val h1Style: SpanStyle = SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
    val h2Style: SpanStyle = SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    val h3Style: SpanStyle = SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
    val h4Style: SpanStyle = SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
    val h5Style: SpanStyle = SpanStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold),
    val h6Style: SpanStyle = SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
)