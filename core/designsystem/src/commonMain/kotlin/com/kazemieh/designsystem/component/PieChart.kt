// commonMain
package com.kazemieh.designsystem.component

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kazemieh.common.format
import com.kazemieh.common.toDegrees
import com.kazemieh.common.toRadians
import com.kazemieh.designsystem.LocalSpacing

// ---------- Data class ----------
data class PieChartItem(
    val id: Long? = null,
    val label: String,
    val value: Long,
    val color: Color? = null,
    val icon: ImageVector? = null
)

// ---------- Helper: draw painter at offset ----------
private fun DrawScope.drawPainterAt(painter: Painter, topLeft: Offset, sizePx: Float, tint: Color) {
    translate(topLeft.x, topLeft.y) {
        with(painter) {
            draw(size = Size(sizePx, sizePx), colorFilter = ColorFilter.tint(tint))
        }
    }
}

// ---------- Helper: calculate percentages ----------
private fun calculatePercentages(data: List<PieChartItem>): List<Float> {
    val total = data.sumOf { it.value.toDouble() }
    return if (total == 0.0) data.map { 0f }
    else data.map { ((it.value.toDouble() / total) * 100).toFloat() }
}

// ---------- Helper: generate dynamic colors ----------
private fun generateDynamicColors(count: Int): List<Color> {
    val colors = mutableListOf<Color>()
    val step = 360f / count
    repeat(count) { index ->
        val hue = step * index
        colors.add(Color.hsv(hue, 0.7f, 0.9f))
    }
    return colors
}

// ---------- Helper: determine readable text color based on background ----------
private fun textColorForBackground(bg: Color): Color {
    val luminance = bg.luminance()
    return if (luminance < 0.5) Color.White else Color.Black
}

// ---------- توابع کمکی برای فرمت اعداد ----------
fun formatCurrency(amount: Long): String {
    return buildString {
        var num = amount
        val units = listOf("", "هزار", "میلیون", "میلیارد", "تریلیون")
        var unitIndex = 0

        while (num >= 1000 && unitIndex < units.lastIndex) {
            if (num % 1000 != 0L) {
                val part = num % 1000
                if (isNotEmpty()) insert(0, " و ")
                insert(0, "${part} ${units[unitIndex]}")
            }
            num /= 1000
            unitIndex++
        }

        if (num > 0 || isEmpty()) {
            if (isNotEmpty()) insert(0, " و ")
            insert(0, "$num ${units[unitIndex]}")
        }
    }
}

// نسخه ساده‌تر برای فرمت اعداد
fun Long.formatNumber(): String {
    return toString().reversed().chunked(3).joinToString(",").reversed()
}

// ---------- Main PieChart ----------
@Composable
fun PieChart(
    data: List<PieChartItem>,
    radiusOuter: Dp = 80.dp,
    chartBarWidth: Dp = 15.dp,
    textDistanceExtra: Dp = 40.dp,
    animDuration: Int = 500,
    labelTextStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    legendTextStyle: TextStyle = MaterialTheme.typography.bodySmall,
    showLegend: Boolean = true,
    enableAnimation: Boolean = true,
    onSliceClick: ((PieChartItem) -> Unit)? = null
) {
    if (data.isEmpty()) return

    val space = LocalSpacing.current

    val painters = data.map { it.icon?.let { rememberVectorPainter(it) } }

    val percentages = remember(data) { calculatePercentages(data) }
    val total = data.sumOf { it.value.toDouble() }.takeIf { it != 0.0 } ?: 1.0
    val sliceAngles = remember(data) { data.map { 360f * (it.value.toFloat() / total.toFloat()) } }

    val colors = remember(data) {
        val provided = data.map { it.color }
        if (provided.any { it == null }) generateDynamicColors(data.size)
        else provided.filterNotNull()
    }

    val textMeasurer = rememberTextMeasurer()
    var animationPlayed by rememberSaveable() { mutableStateOf(false) }
    val chartKey = remember(data) {
        data.fold(0L) { acc, item ->
            acc + item.value + item.label.hashCode().toLong()
        }
    }
    val animatedRotation by animateFloatAsState(
        targetValue = if (enableAnimation) 90f * 11f else 0f,
        animationSpec = tween(animDuration, easing = LinearOutSlowInEasing),
        label = "pieRotation"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(radiusOuter * 2 + textDistanceExtra * 2)
                .pointerInput(onSliceClick) {
                    if (onSliceClick != null) {
                        detectTapGestures { offset ->
                            // چک کردن اینکه روی کدام Slice کلیک شده
                            val center = Offset(
                                (size.width / 2).toFloat(),
                                (size.height / 2).toFloat()
                            )
                            val touchAngle = kotlin.math.atan2(
                                (offset.y - center.y).toDouble(),
                                (offset.x - center.x).toDouble()
                            ).toFloat().let {
                                it.toDegrees().let { if (it < 0) it + 360f else it }
                            }
                            var start = 0f
                            sliceAngles.forEachIndexed { i, sweep ->
                                if (touchAngle in start..(start + sweep)) {
                                    onSliceClick(data[i])
                                    return@detectTapGestures
                                }
                                start += sweep
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Arc Chart
            Canvas(
                modifier = Modifier
                    .size(radiusOuter * 2)
                    .rotate(animatedRotation)
            ) {
                var start = 0f
                val radius = radiusOuter.toPx() - chartBarWidth.toPx() / 2
                sliceAngles.forEachIndexed { i, sweep ->
                    drawArc(
                        color = colors[i],
                        startAngle = start,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(chartBarWidth.toPx(), cap = StrokeCap.Butt)
                    )
                    start += sweep
                }
            }

            // Labels
            PieChartLabelsCanvas(
                radiusOuter = radiusOuter,
                textDistanceExtra = textDistanceExtra,
                sliceAngles = sliceAngles,
                data = data,
                colors = colors,
                percentages = percentages,
                textMeasurer = textMeasurer,
                textStyle = labelTextStyle,
                painters = painters,
                rotation = animatedRotation
            )
        }

        if (showLegend) {
            Spacer(modifier = Modifier.height(space.mediumLarge))
            PieChartLegend(
                data = data,
                colors = colors,
            )
        }
    }
}

// ---------- Labels ----------
@Composable
private fun PieChartLabelsCanvas(
    radiusOuter: Dp,
    textDistanceExtra: Dp,
    sliceAngles: List<Float>,
    data: List<PieChartItem>,
    colors: List<Color>,
    percentages: List<Float>,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    textStyle: TextStyle,
    rotation: Float,
    painters: List<Painter?>
) {
    Canvas(modifier = Modifier.size(radiusOuter * 2 + textDistanceExtra * 2)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = radiusOuter.toPx()
        var startAngle = 0f

        sliceAngles.forEachIndexed { index, sweep ->
            val middleAngle = (startAngle + sweep / 2 + rotation) % 360
            val rad = middleAngle.toDouble().toRadians()

            // استفاده از kotlin.math به جای Math جاوا
            val cosRad = kotlin.math.cos(rad)
            val sinRad = kotlin.math.sin(rad)

            val edge = Offset(
                x = center.x + (radius * cosRad).toFloat(),
                y = center.y + (radius * sinRad).toFloat()
            )
            val textDistance = radius + textDistanceExtra.toPx()
            val textPoint = Offset(
                x = center.x + (textDistance * cosRad).toFloat(),
                y = center.y + (textDistance * sinRad).toFloat()
            )

            drawLine(colors[index], edge, textPoint, strokeWidth = 2f)

            val labelText = "${data[index].label} ${percentages[index].format(0)}%"
            val txtColor = textColorForBackground(colors[index])
            val layout = textMeasurer.measure(
                AnnotatedString(labelText),
                style = textStyle.copy(color = txtColor)
            )

            val textOffset = when (middleAngle.toInt()) {
                in 45..135 -> Offset(textPoint.x - layout.size.width / 2, textPoint.y + 5f)
                in 135..225 -> Offset(
                    textPoint.x - layout.size.width - 5f,
                    textPoint.y - layout.size.height / 2
                )

                in 225..315 -> Offset(
                    textPoint.x - layout.size.width / 2,
                    textPoint.y - layout.size.height - 5f
                )

                else -> Offset(textPoint.x + 5f, textPoint.y - layout.size.height / 2)
            }

            painters[index]?.let { painter ->
                val iconSize = 18.dp.toPx()
                val iconOffset = Offset(
                    x = textOffset.x - iconSize - 6f,
                    y = textOffset.y + (layout.size.height - iconSize) / 2f
                )
                drawPainterAt(painter, iconOffset, iconSize, txtColor)
            }

            drawText(layout, topLeft = textOffset)
            startAngle += sweep
        }
    }
}

// ---------- Legend ----------
@Composable
private fun PieChartLegend(
    data: List<PieChartItem>,
    colors: List<Color>
) {
    val space = LocalSpacing.current
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = space.large),
        horizontalArrangement = Arrangement.spacedBy(
            space = space.small,
            alignment = Alignment.CenterHorizontally
        ),
    ) {
        data.forEachIndexed { index, item ->
            val txtColor = textColorForBackground(colors[index])
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = space.mediumSmall)
            ) {
                Box(
                    modifier = Modifier
                        .size(space.large)
                        .background(colors[index], shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(space.small))
                item.icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = item.label,
                        tint = txtColor,
                        modifier = Modifier.size(space.large)
                    )
                    Spacer(modifier = Modifier.width(space.small))
                }
                FintrackLabelSmallText(
                    text = "${item.label}: ${item.value.formatNumber()} تومان"
                )
            }
        }
    }
}

