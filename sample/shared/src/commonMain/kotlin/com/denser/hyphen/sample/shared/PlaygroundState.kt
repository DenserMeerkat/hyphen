package com.denser.hyphen.sample.shared

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denser.hyphen.ui.style.BlockquoteStyle
import com.denser.hyphen.ui.style.HyphenStyleConfig

enum class EditorMode {
    BasicEditor,
    TextField,
}

enum class TextFieldVariant {
    Filled,
    Outlined,
}

enum class LeftToolWindow {
    StyleConfig,
    EditorConfig,
    StateInspector,
}

enum class BottomToolWindow {
    MarkdownOutput,
}

class PlaygroundState {

    // ── Editor mode ──────────────────────────────────────────────────────────
    var editorMode by mutableStateOf(EditorMode.BasicEditor)
    var textFieldVariant by mutableStateOf(TextFieldVariant.Filled)

    // ── Tool Windows (Independent, IntelliJ style) ───────────────────────────
    var activeLeftTool by mutableStateOf<LeftToolWindow?>(LeftToolWindow.StyleConfig)
    var activeBottomTool by mutableStateOf<BottomToolWindow?>(BottomToolWindow.MarkdownOutput)

    // Compact layout active tab (for mobile viewports)
    var activeMobileTab by mutableStateOf<MobileTab>(MobileTab.Editor)

    // ── Text & Layout Options ────────────────────────────────────────────────
    var isRtl by mutableStateOf(false)
    var isReadOnly by mutableStateOf(false)
    var isEnabled by mutableStateOf(true)
    var showSuggestionsPopup by mutableStateOf(true)

    var fontSize by mutableStateOf(15f)
    var horizontalPadding by mutableStateOf(16f)
    var indentSpaces by mutableStateOf(4)

    // ── Cursor Config ────────────────────────────────────────────────────────
    var useCursorColorOverride by mutableStateOf(false)
    var cursorColor by mutableStateOf(Color(0xFF3B82F6))

    // ── TextField specific options ───────────────────────────────────────────
    var showLabel by mutableStateOf(true)
    var labelText by mutableStateOf("Document Content")
    var useFloatingLabel by mutableStateOf(true)

    var showPlaceholder by mutableStateOf(true)
    var placeholderText by mutableStateOf("Start typing markdown...")

    var showSupportingText by mutableStateOf(false)
    var supportingText by mutableStateOf("Markdown formatting enabled")

    var isError by mutableStateOf(false)

    // ── Style Config options ─────────────────────────────────────────────────

    // Inline Styles
    var useBoldColorOverride by mutableStateOf(false)
    var boldColor by mutableStateOf(Color(0xFF1E293B))

    var highlightColor by mutableStateOf(Color(0xFFFDE047))
    var highlightAlpha by mutableStateOf(0.45f)

    var inlineCodeBgAlpha by mutableStateOf(0.12f)

    var linkColor by mutableStateOf(Color(0xFF2563EB))
    var linkUnderline by mutableStateOf(true)

    // Headings
    var headingBold by mutableStateOf(true)
    var h1Size by mutableStateOf(26f)
    var h2Size by mutableStateOf(22f)
    var h3Size by mutableStateOf(18f)
    var h4Size by mutableStateOf(16f)
    var h5Size by mutableStateOf(14f)
    var h6Size by mutableStateOf(13f)

    // Blockquote
    var useBlockquoteTextColorOverride by mutableStateOf(false)
    var blockquoteTextColor by mutableStateOf(Color(0xFF64748B))
    var blockquoteBgColor by mutableStateOf(Color(0xFF94A3B8))
    var blockquoteBgAlpha by mutableStateOf(0.12f)
    var blockquoteBorderColor by mutableStateOf(Color(0xFF64748B))
    var blockquoteBorderAlpha by mutableStateOf(0.6f)
    var blockquoteBorderWidth by mutableStateOf(4f)
    var blockquoteCornerRadius by mutableStateOf(4f)

    // Mentions
    var userMentionColor by mutableStateOf(Color(0xFF2563EB))
    var tagMentionColor by mutableStateOf(Color(0xFF16A34A))
    var varMentionColor by mutableStateOf(Color(0xFF9333EA))

    // Checkboxes
    var checkedStrikethrough by mutableStateOf(true)
    var checkedGrayOut by mutableStateOf(true)

    // ── Style Builder ────────────────────────────────────────────────────────
    fun buildStyleConfig(): HyphenStyleConfig {
        val baseConfig = HyphenStyleConfig()

        return HyphenStyleConfig(
            boldStyle = if (useBoldColorOverride) {
                SpanStyle(fontWeight = FontWeight.Bold, color = boldColor)
            } else {
                baseConfig.boldStyle
            },

            highlightStyle = SpanStyle(
                background = highlightColor.copy(alpha = highlightAlpha),
            ),

            inlineCodeStyle = baseConfig.inlineCodeStyle.copy(
                background = baseConfig.inlineCodeStyle.background.copy(alpha = inlineCodeBgAlpha),
            ),

            linkStyle = SpanStyle(
                color = linkColor,
                textDecoration = if (linkUnderline) {
                    androidx.compose.ui.text.style.TextDecoration.Underline
                } else {
                    androidx.compose.ui.text.style.TextDecoration.None
                },
            ),

            mentionStyles = mapOf(
                "user" to SpanStyle(color = userMentionColor, fontWeight = FontWeight.SemiBold),
                "tag" to SpanStyle(color = tagMentionColor, fontWeight = FontWeight.SemiBold),
                "var" to SpanStyle(
                    color = varMentionColor,
                    background = varMentionColor.copy(alpha = 0.12f),
                    fontWeight = FontWeight.Medium,
                ),
            ),

            h1Style = SpanStyle(
                fontSize = h1Size.sp,
                fontWeight = if (headingBold) FontWeight.Bold else FontWeight.Normal,
            ),
            h2Style = SpanStyle(
                fontSize = h2Size.sp,
                fontWeight = if (headingBold) FontWeight.Bold else FontWeight.Normal,
            ),
            h3Style = SpanStyle(
                fontSize = h3Size.sp,
                fontWeight = if (headingBold) FontWeight.Bold else FontWeight.Normal,
            ),
            h4Style = SpanStyle(
                fontSize = h4Size.sp,
                fontWeight = if (headingBold) FontWeight.SemiBold else FontWeight.Normal,
            ),
            h5Style = SpanStyle(
                fontSize = h5Size.sp,
                fontWeight = if (headingBold) FontWeight.SemiBold else FontWeight.Normal,
            ),
            h6Style = SpanStyle(
                fontSize = h6Size.sp,
                fontWeight = if (headingBold) FontWeight.SemiBold else FontWeight.Normal,
            ),

            blockquoteSpanStyle = if (useBlockquoteTextColorOverride) {
                SpanStyle(
                    color = blockquoteTextColor,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                )
            } else {
                SpanStyle(
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                )
            },

            blockquoteStyle = BlockquoteStyle(
                backgroundColor = blockquoteBgColor.copy(alpha = blockquoteBgAlpha),
                borderColor = blockquoteBorderColor.copy(alpha = blockquoteBorderAlpha),
                borderWidth = blockquoteBorderWidth.dp,
                cornerRadius = blockquoteCornerRadius.dp,
            ),

            checkboxCheckedStyle = SpanStyle(
                textDecoration = if (checkedStrikethrough) {
                    androidx.compose.ui.text.style.TextDecoration.LineThrough
                } else {
                    androidx.compose.ui.text.style.TextDecoration.None
                },
                color = if (checkedGrayOut) {
                    Color.Gray
                } else {
                    Color.Unspecified
                },
            ),

            indentSpaces = indentSpaces,
        )
    }

    // ── Resets ───────────────────────────────────────────────────────────────
    fun resetStyleConfig() {
        useBoldColorOverride = false
        boldColor = Color(0xFF1E293B)
        highlightColor = Color(0xFFFDE047)
        highlightAlpha = 0.45f
        inlineCodeBgAlpha = 0.12f
        linkColor = Color(0xFF2563EB)
        linkUnderline = true
        headingBold = true
        h1Size = 26f
        h2Size = 22f
        h3Size = 18f
        h4Size = 16f
        h5Size = 14f
        h6Size = 13f
        useBlockquoteTextColorOverride = false
        blockquoteTextColor = Color(0xFF64748B)
        blockquoteBgColor = Color(0xFF94A3B8)
        blockquoteBgAlpha = 0.12f
        blockquoteBorderColor = Color(0xFF64748B)
        blockquoteBorderAlpha = 0.6f
        blockquoteBorderWidth = 4f
        blockquoteCornerRadius = 4f
        userMentionColor = Color(0xFF2563EB)
        tagMentionColor = Color(0xFF16A34A)
        varMentionColor = Color(0xFF9333EA)
        checkedStrikethrough = true
        checkedGrayOut = true
    }

    fun resetEditorConfig() {
        editorMode = EditorMode.BasicEditor
        isRtl = false
        isReadOnly = false
        isEnabled = true
        showSuggestionsPopup = true
        fontSize = 15f
        horizontalPadding = 16f
        indentSpaces = 4
        useCursorColorOverride = false
        cursorColor = Color(0xFF3B82F6)
        showLabel = true
        labelText = "Document Content"
        useFloatingLabel = true
        showPlaceholder = true
        placeholderText = "Start typing markdown..."
        showSupportingText = false
        supportingText = "Markdown formatting enabled"
        isError = false
    }
}

enum class MobileTab {
    Editor,
    Styles,
    Config,
    Markdown,
    Inspector,
}
