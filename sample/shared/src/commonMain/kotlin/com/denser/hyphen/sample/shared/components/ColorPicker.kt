package com.denser.hyphen.sample.shared.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlin.math.abs

// ─────────────────────────────────────────────────────────────────────────────
// HSL ↔ Color conversions
// ─────────────────────────────────────────────────────────────────────────────

fun hslToColor(h: Float, s: Float, l: Float, a: Float = 1f): Color {
    val c = (1f - abs(2f * l - 1f)) * s
    val hPrime = h / 60f
    val x = c * (1f - abs(hPrime % 2f - 1f))
    val m = l - c / 2f
    return when {
        hPrime < 1f -> Color(c + m, x + m, m, a)
        hPrime < 2f -> Color(x + m, c + m, m, a)
        hPrime < 3f -> Color(m, c + m, x + m, a)
        hPrime < 4f -> Color(m, x + m, c + m, a)
        hPrime < 5f -> Color(x + m, m, c + m, a)
        else         -> Color(c + m, m, x + m, a)
    }
}

fun colorToHsl(color: Color): FloatArray {
    val r = color.red
    val g = color.green
    val b = color.blue
    val cMax = maxOf(r, g, b)
    val cMin = minOf(r, g, b)
    val delta = cMax - cMin
    val l = (cMax + cMin) / 2f
    val s = if (delta < 1e-6f) 0f else delta / (1f - abs(2f * l - 1f))
    val h = when {
        delta < 1e-6f -> 0f
        cMax == r     -> 60f * (((g - b) / delta).let { if (it < 0f) it + 6f else it })
        cMax == g     -> 60f * ((b - r) / delta + 2f)
        else          -> 60f * ((r - g) / delta + 4f)
    }
    return floatArrayOf(
        h.coerceIn(0f, 360f),
        s.coerceIn(0f, 1f),
        l.coerceIn(0f, 1f),
        color.alpha,
    )
}

fun Color.toHexString(): String {
    fun Int.hex2() = toString(16).padStart(2, '0').uppercase()
    return "#${(red * 255).toInt().hex2()}${(green * 255).toInt().hex2()}${(blue * 255).toInt().hex2()}"
}

// ─────────────────────────────────────────────────────────────────────────────
// Canvas-based gradient slider
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun GradientSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    brush: Brush,
    modifier: Modifier = Modifier,
) {
    val currentOnValueChange by rememberUpdatedState(onValueChange)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(26.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        currentOnValueChange((offset.x / size.width.toFloat()).coerceIn(0f, 1f))
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        currentOnValueChange((change.position.x / size.width.toFloat()).coerceIn(0f, 1f))
                    },
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    currentOnValueChange((offset.x / size.width.toFloat()).coerceIn(0f, 1f))
                }
            },
    ) {
        val trackH = 8.dp.toPx()
        val trackY = (size.height - trackH) / 2f
        val cr = trackH / 2f

        val trackPath = Path().apply {
            addRoundRect(RoundRect(0f, trackY, size.width, trackY + trackH, CornerRadius(cr)))
        }
        clipPath(trackPath) { drawRect(brush = brush) }

        // Thumb
        val tx = value.coerceIn(0f, 1f) * size.width
        val ty = size.height / 2f
        val tr = 9.dp.toPx()
        drawCircle(Color.Black.copy(alpha = 0.12f), tr + 1.5f, Offset(tx, ty + 1.5f))  // shadow
        drawCircle(Color.White, tr, Offset(tx, ty))
        drawCircle(Color.Black.copy(alpha = 0.22f), tr, Offset(tx, ty), style = Stroke(1.5.dp.toPx()))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HSL color picker (collapsible)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HslColorPicker(
    color: Color,
    onColorChange: (Color) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val currentOnColorChange by rememberUpdatedState(onColorChange)

    // Decompose color into HSL
    val initHsl = remember(color) { colorToHsl(color) }
    var h by remember { mutableStateOf(initHsl[0]) }
    var s by remember { mutableStateOf(initHsl[1]) }
    var l by remember { mutableStateOf(initHsl[2]) }
    var a by remember { mutableStateOf(initHsl[3]) }

    // Re-sync local HSL if caller changes color externally
    LaunchedEffect(color) {
        val newHsl = colorToHsl(color)
        h = newHsl[0]
        s = newHsl[1]
        l = newHsl[2]
        a = newHsl[3]
    }

    val currentColor = remember(h, s, l, a) { hslToColor(h, s, l, a) }

    // Debounce updates to global editor state for smooth 60fps local dragging
    LaunchedEffect(currentColor) {
        delay(120)
        currentOnColorChange(currentColor)
    }

    // Hue gradient – computed once per hue step
    val hueColors = remember { (0..12).map { i -> hslToColor(i * 30f, 1f, 0.5f) } }

    Column(modifier = modifier) {
        // ── Swatch row ────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    if (expanded) RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    else RoundedCornerShape(8.dp),
                )
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .clickable { expanded = !expanded }
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(currentColor)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = currentColor.toHexString(),
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // ── Slider panel ──────────────────────────────────────────────────
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HslSliderRow(label = "H  ${h.toInt()}°") {
                    GradientSlider(
                        value = h / 360f,
                        onValueChange = { h = it * 360f },
                        brush = Brush.horizontalGradient(hueColors),
                    )
                }
                HslSliderRow(label = "S  ${(s * 100).toInt()}%") {
                    GradientSlider(
                        value = s,
                        onValueChange = { s = it },
                        brush = Brush.horizontalGradient(
                            listOf(hslToColor(h, 0f, l), hslToColor(h, 1f, l)),
                        ),
                    )
                }
                HslSliderRow(label = "L  ${(l * 100).toInt()}%") {
                    GradientSlider(
                        value = l,
                        onValueChange = { l = it },
                        brush = Brush.horizontalGradient(
                            listOf(Color.Black, hslToColor(h, s, 0.5f), Color.White),
                        ),
                    )
                }
                HslSliderRow(label = "A  ${(a * 100).toInt()}%") {
                    GradientSlider(
                        value = a,
                        onValueChange = { a = it },
                        brush = Brush.horizontalGradient(
                            listOf(hslToColor(h, s, l, 0f), hslToColor(h, s, l, 1f)),
                        ),
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Private helpers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HslSliderRow(label: String, slider: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier.width(62.dp),
        )
        Box(modifier = Modifier.weight(1f)) { slider() }
    }
}
