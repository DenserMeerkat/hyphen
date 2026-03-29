package com.denser.hyphen.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.denser.hyphen.Res
import com.denser.hyphen.link_24dp
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LinkEditDialog(
    initialText: String,
    initialUrl: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
) {
    var text by remember { mutableStateOf(initialText) }
    var url by remember { mutableStateOf(initialUrl) }
    val urlFocusRequester = remember { FocusRequester() }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 0.dp,
        ) {
            Column {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.link_24dp),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Edit link",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    LinkField(
                        label = "Display text",
                        value = text,
                        onValueChange = { text = it },
                        placeholder = "Link label",
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                        ),
                        onImeAction = { urlFocusRequester.requestFocus() },
                    )
                    LinkField(
                        label = "URL",
                        value = url,
                        onValueChange = { url = it },
                        placeholder = "https://",
                        monospace = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Done,
                        ),
                        onImeAction = { if (text.isNotBlank()) onConfirm(text, url) },
                        modifier = Modifier.focusRequester(urlFocusRequester),
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    ) {
                        Text("Cancel", style = MaterialTheme.typography.labelLarge)
                    }
                    Button(
                        onClick = { onConfirm(text, url) },
                        enabled = text.isNotBlank(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Text("Apply", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun LinkField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    monospace: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onImeAction: (() -> Unit)? = null,
) {
    var isFocused by remember { mutableStateOf(false) }

    val borderColor = if (isFocused)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.outlineVariant

    val fieldShape = RoundedCornerShape(8.dp)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 0.06.sp,
                fontSize = 10.sp,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .fillMaxWidth()
                .clip(fieldShape)
                .border(
                    width = if (isFocused) 1.dp else 0.5.dp,
                    color = borderColor,
                    shape = fieldShape,
                )
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .onFocusChanged { isFocused = it.isFocused }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = if (monospace) androidx.compose.ui.text.font.FontFamily.Monospace
                else androidx.compose.ui.text.font.FontFamily.Default,
                fontSize = if (monospace) 13.sp else 14.sp,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = keyboardOptions,
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            fontFamily = if (monospace) androidx.compose.ui.text.font.FontFamily.Monospace
                            else androidx.compose.ui.text.font.FontFamily.Default,
                            fontSize = if (monospace) 13.sp else 14.sp,
                        ),
                    )
                }
                inner()
            },
        )
    }
}