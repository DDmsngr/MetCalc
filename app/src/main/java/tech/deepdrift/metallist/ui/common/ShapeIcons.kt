package tech.deepdrift.metallist.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import tech.deepdrift.metallist.domain.model.ProfileShape
import kotlin.math.cos
import kotlin.math.sin

/**
 * Изометрические контурные иконки 11 форм проката. Рисуются программно,
 * чтобы не зависеть от drawable-ресурсов.
 */
@Composable
fun ShapeIcon(
    shape: ProfileShape,
    color: Color,
    modifier: Modifier = Modifier,
    sizeDp: Int = 88,
) {
    Box(modifier = modifier.size(sizeDp.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(sizeDp.dp)) {
            val stroke = Stroke(width = 3f)
            val w = size.width
            val h = size.height
            when (shape) {
                ProfileShape.Round -> drawRound(w, h, color, stroke)
                ProfileShape.PipeRound -> drawPipeRound(w, h, color, stroke)
                ProfileShape.Hex -> drawHex(w, h, color, stroke)
                ProfileShape.Plate -> drawSheet(w, h, color, stroke)
                ProfileShape.BentChannel -> drawBent(w, h, color, stroke)
                ProfileShape.PipeRect -> drawPipeRect(w, h, color, stroke)
                ProfileShape.Angle -> drawAngle(w, h, color, stroke)
                ProfileShape.IBeam -> drawIBeam(w, h, color, stroke)
                ProfileShape.Channel -> drawChannel(w, h, color, stroke)
                ProfileShape.Rebar -> drawRebar(w, h, color, stroke)
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRound(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val cx = w / 2f
    val cy = h / 2f
    val r = kotlin.math.min(w, h) * 0.35f
    drawCircle(color = c, radius = r, center = Offset(cx, cy), style = s)
    // штриховка внутри
    val step = r / 4f
    for (i in -3..3) {
        val y = cy + i * step
        drawLine(c, Offset(cx - r * 0.9f, y), Offset(cx + r * 0.9f, y), strokeWidth = 1.5f)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPipeRound(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val cx = w / 2f; val cy = h / 2f
    val r = kotlin.math.min(w, h) * 0.36f
    drawCircle(c, r, Offset(cx, cy), style = s)
    drawCircle(c, r * 0.55f, Offset(cx, cy), style = s)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHex(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val cx = w / 2f; val cy = h / 2f
    val r = kotlin.math.min(w, h) * 0.36f
    val path = Path()
    for (i in 0..5) {
        val a = Math.PI / 3 * i
        val x = cx + r * cos(a).toFloat()
        val y = cy + r * sin(a).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color = c, style = s)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSheet(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val plateW = w * 0.72f; val plateH = h * 0.18f
    val left = (w - plateW) / 2f; val top = h / 2f - plateH / 2f
    drawRect(c, topLeft = Offset(left, top), size = Size(plateW, plateH), style = s)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBent(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val a = kotlin.math.min(w, h) * 0.7f
    val left = (w - a) / 2f; val top = (h - a) / 2f
    val t = a * 0.14f
    val path = Path().apply {
        moveTo(left, top); lineTo(left + a, top)
        lineTo(left + a, top + t); lineTo(left + t, top + t)
        lineTo(left + t, top + a); lineTo(left, top + a); close()
    }
    drawPath(path, color = c, style = s)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPipeRect(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val a = kotlin.math.min(w, h) * 0.8f
    val b = a * 0.6f
    val left = (w - a) / 2f; val top = (h - b) / 2f
    drawRect(c, topLeft = Offset(left, top), size = Size(a, b), style = s)
    val t = a * 0.09f
    drawRect(c, topLeft = Offset(left + t, top + t), size = Size(a - 2 * t, b - 2 * t), style = s)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAngle(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val a = kotlin.math.min(w, h) * 0.72f
    val left = (w - a) / 2f; val top = (h - a) / 2f
    val t = a * 0.16f
    val path = Path().apply {
        moveTo(left, top); lineTo(left + t, top)
        lineTo(left + t, top + a - t); lineTo(left + a, top + a - t)
        lineTo(left + a, top + a); lineTo(left, top + a); close()
    }
    drawPath(path, color = c, style = s)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawIBeam(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val a = kotlin.math.min(w, h) * 0.7f
    val left = (w - a) / 2f; val top = (h - a) / 2f
    val fT = a * 0.18f; val wT = a * 0.24f
    val cx = left + a / 2f
    val path = Path().apply {
        moveTo(left, top); lineTo(left + a, top)
        lineTo(left + a, top + fT); lineTo(cx + wT / 2, top + fT)
        lineTo(cx + wT / 2, top + a - fT); lineTo(left + a, top + a - fT)
        lineTo(left + a, top + a); lineTo(left, top + a)
        lineTo(left, top + a - fT); lineTo(cx - wT / 2, top + a - fT)
        lineTo(cx - wT / 2, top + fT); lineTo(left, top + fT); close()
    }
    drawPath(path, color = c, style = s)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawChannel(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val a = kotlin.math.min(w, h) * 0.7f
    val left = (w - a) / 2f; val top = (h - a) / 2f
    val fT = a * 0.18f
    val path = Path().apply {
        moveTo(left, top); lineTo(left + a, top)
        lineTo(left + a, top + fT); lineTo(left + fT, top + fT)
        lineTo(left + fT, top + a - fT); lineTo(left + a, top + a - fT)
        lineTo(left + a, top + a); lineTo(left, top + a); close()
    }
    drawPath(path, color = c, style = s)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRebar(
    w: Float, h: Float, c: Color, s: Stroke,
) {
    val bandH = h * 0.32f
    val left = w * 0.1f; val right = w * 0.9f
    val top = (h - bandH) / 2f
    drawRect(c, topLeft = Offset(left, top), size = Size(right - left, bandH), style = s)
    val step = (right - left) / 12f
    val dashed = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
    for (i in 0..12) {
        val x = left + i * step
        drawLine(c, Offset(x, top - 6f), Offset(x, top + bandH + 6f), strokeWidth = 2f, pathEffect = dashed)
    }
}
