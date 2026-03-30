package com.denser.hyphen.ui.checkbox

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import com.denser.hyphen.model.MarkupStyle
import com.denser.hyphen.state.HyphenTextState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InlineCheckbox(
    style: MarkupStyle,
    startIndex: Int,
    state: HyphenTextState,
    textStyle: TextStyle,
) {
    val fontSize = textStyle.fontSize.takeIf { it.isSp } ?: 16.sp
    val ratio = fontSize.value / 16f

    CompositionLocalProvider(
        LocalRippleConfiguration provides null
    ) {
        Box(modifier = Modifier.scale(0.85f * ratio)) {
            Checkbox(
                checked = style is MarkupStyle.CheckboxChecked,
                onCheckedChange = null,
                enabled = true
            )

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { state.toggleCheckbox(startIndex) }
                    )
            )
        }
    }
}