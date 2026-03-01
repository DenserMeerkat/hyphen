package com.denser.hyphen.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

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
        background = Color.Gray.copy(alpha = 0.05f)
    )
)