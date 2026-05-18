// commonMain
package com.kazemieh.designsystem.component

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kazemieh.common.format
import com.kazemieh.common.toDegrees
import com.kazemieh.common.toRadians
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.picker.FinTrackIcons
import com.kazemieh.designsystem.picker.FinTrackPickerColors
import org.jetbrains.compose.resources.painterResource

// ---------- Data class ----------
data class PieChartItem(
    val id: Long? = null,
    val label: String,
    val value: Long,
    val color: Color? = null,
    val icon: ImageVector? = null,
    val colorId: Int? = null,
    val iconId: Int? = null
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
    modifier: Modifier = Modifier,
    radiusOuter: Dp = 80.dp,
    chartBarWidth: Dp = 15.dp,
    textDistanceExtra: Dp = 60.dp,
    animDuration: Int = 500,
    labelTextStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    legendTextStyle: TextStyle = MaterialTheme.typography.bodySmall,
    showLegend: Boolean = true,
    enableAnimation: Boolean = true,
    onSliceClick: ((PieChartItem) -> Unit)? = null
) {
    if (data.isEmpty()) return

    val space = LocalSpacing.current
    val isDark = isSystemInDarkTheme()

    val painters = data.map { item ->
        val res = item.iconId?.let { FinTrackIcons.findIcon(it).resource }
        res?.let { painterResource(it) } ?: item.icon?.let { rememberVectorPainter(it) }
    }

    val percentages = remember(data) { calculatePercentages(data) }
    val total = data.sumOf { it.value.toDouble() }.takeIf { it != 0.0 } ?: 1.0
    val sliceAngles = remember(data) { data.map { 360f * (it.value.toFloat() / total.toFloat()) } }

    val colors = remember(data, isDark) {
        val dynamicColors = generateDynamicColors(data.size)
        data.mapIndexed { index, item ->
            item.color ?: item.colorId?.let { FinTrackPickerColors.getColorById(it, isDark) }
            ?: dynamicColors[index]
        }
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
        modifier = modifier.fillMaxWidth(),
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
                painters = painters
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
    // چون ممکن است متن‌ها دو خطی شوند، فاصله عمودی مینیمم را کمی بیشتر می‌کنیم تا روی هم نیفتند
    val minGap = 38.dp

    Canvas(modifier = Modifier.size(radiusOuter * 2 + textDistanceExtra * 2)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = radiusOuter.toPx()

        val radialOffset = 12.dp.toPx()
        val horizontalLineLen = 20.dp.toPx() // خط افقی را کمی کوتاه‌تر کردم تا جای متن بیشتر شود

        class LabelInfo(
            val index: Int,
            val edge: Offset,
            var elbowX: Float,
            var lineEndX: Float,
            var yPos: Float,
            val isRightSide: Boolean,
            val layout: androidx.compose.ui.text.TextLayoutResult
        )

        val labels = mutableListOf<LabelInfo>()
        var startAngle = 0f

        // ---------- فاز ۱: محاسبه نقاط و محدود کردن عرض متن (دو خطی شدن) ----------
        sliceAngles.forEachIndexed { index, sweep ->
            val midAngle = (startAngle + sweep / 2 + rotation) % 360
            val rad = midAngle.toDouble().toRadians()

            val cos = kotlin.math.cos(rad).toFloat()
            val sin = kotlin.math.sin(rad).toFloat()

            val edge = Offset(center.x + radius * cos, center.y + radius * sin)
            val elbowX = center.x + (radius + radialOffset) * cos
            val initialYPos = center.y + (radius + radialOffset) * sin

            val isRightSide = cos >= 0

            // محاسبه محل پایان خط افقی همینجا انجام می‌شود تا بدانیم چقدر فضا برای متن داریم
            val lineEndX =
                if (isRightSide) elbowX + horizontalLineLen else elbowX - horizontalLineLen

            val iconSize = if (painters[index] != null) 18.dp.toPx() else 0f
            val iconMargin = if (iconSize > 0f) 8f else 0f
            val spaceFromLine = 8f
            val canvasPadding = 4.dp.toPx() // مقداری حاشیه امن برای نچسبیدن به لبه صفحه

            // محاسبه دقیق فضای خالی باقیمانده تا لبه صفحه
            val maxAvailableTextWidth = if (isRightSide) {
                size.width - lineEndX - iconSize - iconMargin - spaceFromLine - canvasPadding
            } else {
                lineEndX - iconSize - iconMargin - spaceFromLine - canvasPadding
            }

            // جلوگیری از کرش کردن در صورتی که فضای محاسبه شده منفی یا خیلی کم شود
            val safeMaxWidth = maxOf(200f, maxAvailableTextWidth).toInt()

            val labelText = "${data[index].label} ${percentages[index].format(0)}%"

            // تنظیمات جدید متن: تراز کردن + محدودیت سایز + دو خطی شدن
            val layout = textMeasurer.measure(
                text = androidx.compose.ui.text.AnnotatedString(labelText),
                style = textStyle.copy(
                    color = colors[index],
                    textAlign = if (isRightSide) TextAlign.Start else TextAlign.End // تراز هوشمند
                ),
                softWrap = true, // اجازه شکستن خط
                maxLines = 2, // نهایتاً دو خط
                overflow = TextOverflow.Ellipsis, // سه نقطه شدن در صورت کمبود جا
                constraints = Constraints(maxWidth = safeMaxWidth) // اعمال محدودیت عرض
            )

            labels.add(LabelInfo(index, edge, elbowX, lineEndX, initialYPos, isRightSide, layout))
            startAngle += sweep
        }

        // ---------- فاز ۲: حل مشکل روی هم افتادگی عمودی ----------
        val minGapPx = minGap.toPx()
        fun fixOverlaps(list: List<LabelInfo>) {
            if (list.size <= 1) return
            val sorted = list.sortedBy { it.yPos }
            val originalMidY = (sorted.first().yPos + sorted.last().yPos) / 2

            for (i in 1 until sorted.size) {
                val prev = sorted[i - 1]
                val current = sorted[i]
                if (current.yPos - prev.yPos < minGapPx) {
                    current.yPos = prev.yPos + minGapPx
                }
            }

            val newMidY = (sorted.first().yPos + sorted.last().yPos) / 2
            val shiftOffset = originalMidY - newMidY
            sorted.forEach { it.yPos += shiftOffset }
        }

        fixOverlaps(labels.filter { it.isRightSide })
        fixOverlaps(labels.filter { !it.isRightSide })

        // ---------- فاز ۳: رسم نهایی ----------
        labels.forEach { info ->
            val itemColor = colors[info.index]

            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(info.edge.x, info.edge.y)
                lineTo(info.elbowX, info.yPos)
                lineTo(info.lineEndX, info.yPos)
            }
            drawPath(path, color = itemColor, style = Stroke(width = 2f))

            val iconSize = if (painters[info.index] != null) 18.dp.toPx() else 0f
            val iconMargin = if (iconSize > 0f) 8f else 0f
            val totalContentWidth = iconSize + iconMargin + info.layout.size.width

            val startX = if (info.isRightSide) {
                info.lineEndX + 8f
            } else {
                info.lineEndX - 8f - totalContentWidth
            }

            // رسم آیکون
            painters[info.index]?.let { painter ->
                val iconOffset = Offset(
                    x = startX,
                    y = info.yPos - iconSize / 2f
                )
                drawPainterAt(painter, iconOffset, iconSize, itemColor)
            }

            // رسم متن
            val textOffset = Offset(
                x = startX + iconSize + iconMargin,
                y = info.yPos - info.layout.size.height / 2f
            )
            drawText(info.layout, topLeft = textOffset)
        }
    }
}

// ---------- Legend ----------
@Composable
private fun PieChartLegend(
    data: List<PieChartItem>,
    colors: List<Color>,
    painters: List<Painter?>
) {
    val space = LocalSpacing.current
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = space.large),
        horizontalArrangement = Arrangement.spacedBy(
            space = space.small,
            alignment = Alignment.Start
        ),
    ) {
        data.forEachIndexed { index, item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = space.mediumSmall)
            ) {
                painters[index]?.let {
                    Icon(
                        painter = it,
                        contentDescription = item.label,
                        tint = colors[index],
                        modifier = Modifier.size(space.large)
                    )
                } ?: run {
                    Box(
                        modifier = Modifier
                            .size(space.large)
                            .background(colors[index], shape = CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(space.small))
                FintrackLabelSmallText(
                    text = "${item.label}: ${item.value.formatNumber()} تومان"
                )
            }
        }
    }
}

