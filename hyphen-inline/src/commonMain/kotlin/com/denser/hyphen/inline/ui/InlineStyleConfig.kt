package com.denser.hyphen.inline.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.denser.hyphen.core.model.MarkupStyle

/**
 * Visual configuration for styles rendered by [HyphenInlineEditor].
 *
 * @property boldStyle Applied to [MarkupStyle.Bold] spans. Defaults to bold weight.
 * @property italicStyle Applied to [MarkupStyle.Italic] spans. Defaults to italic.
 * @property underlineStyle Applied to [MarkupStyle.Underline] spans.
 * @property strikethroughStyle Applied to [MarkupStyle.Strikethrough] spans.
 * @property highlightStyle Applied to [MarkupStyle.Highlight] spans.
 *   Defaults to semi-transparent yellow background.
 * @property inlineCodeStyle Applied to [MarkupStyle.InlineCode] spans.
 *   Defaults to monospace with a light grey background.
 */
data class InlineStyleConfig(
    val boldStyle: SpanStyle = SpanStyle(fontWeight = FontWeight.Bold),
    val italicStyle: SpanStyle = SpanStyle(fontStyle = FontStyle.Italic),
    val underlineStyle: SpanStyle = SpanStyle(textDecoration = TextDecoration.Underline),
    val strikethroughStyle: SpanStyle = SpanStyle(textDecoration = TextDecoration.LineThrough),
    val highlightStyle: SpanStyle = SpanStyle(
        background = Color(0xFFFFEB3B).copy(alpha = 0.4f),
    ),
    val inlineCodeStyle: SpanStyle = SpanStyle(
        background = Color.Gray.copy(alpha = 0.15f),
        fontFamily = FontFamily.Monospace,
    ),
)