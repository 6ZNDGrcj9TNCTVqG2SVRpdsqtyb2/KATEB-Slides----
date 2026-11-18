package com.example.ui.editor

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import com.example.engine.parseColorInt
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Shared procedural multi-element illustrative vector renderer for all 34 SlideIconCatalog keys.
 * Used identically in the Icon Picker, live SlideCanvas, PNG/JPG/PDF/Video exports, and PPTX assets.
 */
object IllustrativeIconRenderer {

    fun drawOnCanvas(
        canvas: Canvas,
        symbolKey: String,
        left: Float,
        top: Float,
        width: Float,
        height: Float,
        tintColorInt: Int,
        bgColorInt: Int,
        alpha: Int = 255
    ) {
        val cx = left + width / 2f
        val cy = top + height / 2f
        val size = min(width, height)
        val r = size * 0.46f

        val bgAlpha = ((bgColorInt ushr 24) * alpha) / 255
        if (bgAlpha > 0) {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = bgColorInt
                this.alpha = bgAlpha
                style = Paint.Style.FILL
            }
            canvas.drawCircle(cx, cy, r * 1.04f, bgPaint)
            val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = tintColorInt
                this.alpha = (alpha * 0.22f).toInt()
                style = Paint.Style.STROKE
                strokeWidth = (size * 0.025f).coerceAtLeast(1.5f)
            }
            canvas.drawCircle(cx, cy, r * 1.02f, ringPaint)
        }

        val primary = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = tintColorInt
            this.alpha = alpha
            style = Paint.Style.STROKE
            strokeWidth = (size * 0.055f).coerceAtLeast(2f)
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val fillPrimary = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = tintColorInt
            this.alpha = alpha
            style = Paint.Style.FILL
        }
        val softFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = tintColorInt
            this.alpha = (alpha * 0.22f).toInt()
            style = Paint.Style.FILL
        }
        val accentHex = complementaryAccent(tintColorInt)
        val accentFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentHex
            this.alpha = alpha
            style = Paint.Style.FILL
        }
        val accentStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentHex
            this.alpha = alpha
            style = Paint.Style.STROKE
            strokeWidth = (size * 0.045f).coerceAtLeast(1.8f)
            strokeCap = Paint.Cap.ROUND
        }
        val whiteFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            this.alpha = alpha
            style = Paint.Style.FILL
        }

        val u = size * 0.34f // half-icon span inside circle

        when (symbolKey) {
            "science" -> {
                // Erlenmeyer flask with colored reagent + bubbles
                val neckW = u * 0.34f
                val flask = Path().apply {
                    moveTo(cx - neckW, cy - u * 0.88f)
                    lineTo(cx + neckW, cy - u * 0.88f)
                    lineTo(cx + neckW * 0.8f, cy - u * 0.25f)
                    lineTo(cx + u * 0.88f, cy + u * 0.78f)
                    lineTo(cx - u * 0.88f, cy + u * 0.78f)
                    lineTo(cx - neckW * 0.8f, cy - u * 0.25f)
                    close()
                }
                canvas.drawPath(flask, softFill)
                val liquid = Path().apply {
                    moveTo(cx - u * 0.54f, cy + u * 0.2f)
                    lineTo(cx + u * 0.54f, cy + u * 0.2f)
                    lineTo(cx + u * 0.82f, cy + u * 0.76f)
                    lineTo(cx - u * 0.82f, cy + u * 0.76f)
                    close()
                }
                canvas.drawPath(liquid, accentFill)
                canvas.drawPath(flask, primary)
                canvas.drawLine(cx - neckW * 1.25f, cy - u * 0.88f, cx + neckW * 1.25f, cy - u * 0.88f, primary)
                canvas.drawCircle(cx - u * 0.14f, cy + u * 0.48f, u * 0.09f, whiteFill)
                canvas.drawCircle(cx + u * 0.18f, cy + u * 0.38f, u * 0.07f, whiteFill)
            }
            "atom" -> {
                canvas.drawCircle(cx, cy, u * 0.2f, accentFill)
                val orbitRect = RectF(cx - u * 0.95f, cy - u * 0.36f, cx + u * 0.95f, cy + u * 0.36f)
                listOf(0f, 60f, -60f).forEach { deg ->
                    canvas.save()
                    canvas.rotate(deg, cx, cy)
                    canvas.drawOval(orbitRect, primary)
                    canvas.restore()
                }
            }
            "dna" -> {
                val steps = 8
                for (i in 0..steps) {
                    val t = i.toFloat() / steps
                    val yy = cy - u * 0.85f + t * u * 1.7f
                    val dx = sin(t * PI * 2.0).toFloat() * u * 0.52f
                    canvas.drawLine(cx - dx, yy, cx + dx, yy, if (i % 2 == 0) accentStroke else primary)
                    canvas.drawCircle(cx - dx, yy, u * 0.09f, fillPrimary)
                    canvas.drawCircle(cx + dx, yy, u * 0.09f, accentFill)
                }
            }
            "microscope" -> {
                canvas.drawRoundRect(RectF(cx - u * 0.65f, cy + u * 0.68f, cx + u * 0.65f, cy + u * 0.88f), 6f, 6f, fillPrimary)
                canvas.drawLine(cx - u * 0.3f, cy + u * 0.34f, cx + u * 0.45f, cy + u * 0.34f, accentStroke)
                val arcRect = RectF(cx - u * 0.45f, cy - u * 0.35f, cx + u * 0.72f, cy + u * 0.72f)
                canvas.drawArc(arcRect, -70f, 150f, false, primary)
                canvas.save()
                canvas.rotate(-28f, cx - u * 0.12f, cy - u * 0.25f)
                canvas.drawRoundRect(RectF(cx - u * 0.32f, cy - u * 0.85f, cx + u * 0.08f, cy + u * 0.12f), 8f, 8f, fillPrimary)
                canvas.restore()
            }
            "medical" -> {
                val arm = u * 0.32f
                val len = u * 0.85f
                canvas.drawRoundRect(RectF(cx - arm, cy - len, cx + arm, cy + len), 8f, 8f, fillPrimary)
                canvas.drawRoundRect(RectF(cx - len, cy - arm, cx + len, cy + arm), 8f, 8f, fillPrimary)
                canvas.drawCircle(cx, cy, u * 0.18f, accentFill)
            }
            "heart" -> {
                val heart = Path().apply {
                    moveTo(cx, cy + u * 0.82f)
                    cubicTo(cx - u * 1.1f, cy + u * 0.2f, cx - u * 0.95f, cy - u * 0.8f, cx, cy - u * 0.25f)
                    cubicTo(cx + u * 0.95f, cy - u * 0.8f, cx + u * 1.1f, cy + u * 0.2f, cx, cy + u * 0.82f)
                    close()
                }
                canvas.drawPath(heart, fillPrimary)
                val pulse = Path().apply {
                    moveTo(cx - u * 0.6f, cy + u * 0.02f)
                    lineTo(cx - u * 0.25f, cy + u * 0.02f)
                    lineTo(cx - u * 0.1f, cy - u * 0.28f)
                    lineTo(cx + u * 0.08f, cy + u * 0.32f)
                    lineTo(cx + u * 0.24f, cy + u * 0.02f)
                    lineTo(cx + u * 0.6f, cy + u * 0.02f)
                }
                val wp = Paint(primary).apply { color = Color.WHITE; strokeWidth = (size * 0.045f).coerceAtLeast(2f) }
                canvas.drawPath(pulse, wp)
            }
            "brain" -> {
                canvas.drawOval(RectF(cx - u * 0.85f, cy - u * 0.68f, cx - u * 0.02f, cy + u * 0.68f), softFill)
                canvas.drawOval(RectF(cx + u * 0.02f, cy - u * 0.68f, cx + u * 0.85f, cy + u * 0.68f), softFill)
                canvas.drawOval(RectF(cx - u * 0.85f, cy - u * 0.68f, cx - u * 0.02f, cy + u * 0.68f), primary)
                canvas.drawOval(RectF(cx + u * 0.02f, cy - u * 0.68f, cx + u * 0.85f, cy + u * 0.68f), primary)
                canvas.drawCircle(cx - u * 0.42f, cy - u * 0.15f, u * 0.11f, accentFill)
                canvas.drawCircle(cx + u * 0.42f, cy - u * 0.15f, u * 0.11f, accentFill)
                canvas.drawCircle(cx - u * 0.35f, cy + u * 0.26f, u * 0.09f, fillPrimary)
                canvas.drawCircle(cx + u * 0.35f, cy + u * 0.26f, u * 0.09f, fillPrimary)
            }
            "magnet" -> {
                val magRect = RectF(cx - u * 0.68f, cy - u * 0.55f, cx + u * 0.68f, cy + u * 0.72f)
                canvas.drawArc(magRect, 0f, 180f, false, primary)
                canvas.drawLine(cx - u * 0.68f, cy - u * 0.55f, cx - u * 0.68f, cy + u * 0.08f, primary)
                canvas.drawLine(cx + u * 0.68f, cy - u * 0.55f, cx + u * 0.68f, cy + u * 0.08f, primary)
                canvas.drawRoundRect(RectF(cx - u * 0.82f, cy - u * 0.65f, cx - u * 0.54f, cy - u * 0.32f), 4f, 4f, accentFill)
                canvas.drawRoundRect(RectF(cx + u * 0.54f, cy - u * 0.65f, cx + u * 0.82f, cy - u * 0.32f), 4f, 4f, fillPrimary)
            }
            "engineering" -> {
                val teeth = 8
                for (i in 0 until teeth) {
                    val ang = i * 360f / teeth
                    canvas.save()
                    canvas.rotate(ang, cx, cy)
                    canvas.drawRoundRect(RectF(cx - u * 0.16f, cy - u * 0.94f, cx + u * 0.16f, cy - u * 0.55f), 4f, 4f, fillPrimary)
                    canvas.restore()
                }
                canvas.drawCircle(cx, cy, u * 0.64f, fillPrimary)
                canvas.drawCircle(cx, cy, u * 0.28f, whiteFill)
                canvas.drawCircle(cx, cy, u * 0.14f, accentFill)
            }
            "rocket" -> {
                val body = Path().apply {
                    moveTo(cx, cy - u * 0.95f)
                    cubicTo(cx + u * 0.45f, cy - u * 0.45f, cx + u * 0.42f, cy + u * 0.3f, cx + u * 0.26f, cy + u * 0.55f)
                    lineTo(cx - u * 0.26f, cy + u * 0.55f)
                    cubicTo(cx - u * 0.42f, cy + u * 0.3f, cx - u * 0.45f, cy - u * 0.45f, cx, cy - u * 0.95f)
                    close()
                }
                // Fins
                val leftFin = Path().apply {
                    moveTo(cx - u * 0.33f, cy + u * 0.15f)
                    lineTo(cx - u * 0.72f, cy + u * 0.58f)
                    lineTo(cx - u * 0.25f, cy + u * 0.5f)
                    close()
                }
                val rightFin = Path().apply {
                    moveTo(cx + u * 0.33f, cy + u * 0.15f)
                    lineTo(cx + u * 0.72f, cy + u * 0.58f)
                    lineTo(cx + u * 0.25f, cy + u * 0.5f)
                    close()
                }
                canvas.drawPath(leftFin, accentFill)
                canvas.drawPath(rightFin, accentFill)
                canvas.drawPath(body, fillPrimary)
                canvas.drawCircle(cx, cy - u * 0.2f, u * 0.19f, whiteFill)
                val flame = Path().apply {
                    moveTo(cx - u * 0.16f, cy + u * 0.56f)
                    lineTo(cx, cy + u * 0.96f)
                    lineTo(cx + u * 0.16f, cy + u * 0.56f)
                    close()
                }
                canvas.drawPath(flame, accentFill)
            }
            "bolt" -> {
                val bolt = Path().apply {
                    moveTo(cx + u * 0.12f, cy - u * 0.92f)
                    lineTo(cx - u * 0.55f, cy + u * 0.08f)
                    lineTo(cx - u * 0.04f, cy + u * 0.08f)
                    lineTo(cx - u * 0.20f, cy + u * 0.92f)
                    lineTo(cx + u * 0.56f, cy - u * 0.12f)
                    lineTo(cx + u * 0.04f, cy - u * 0.12f)
                    close()
                }
                canvas.drawPath(bolt, fillPrimary)
            }
            "laptop" -> {
                val screen = RectF(cx - u * 0.72f, cy - u * 0.64f, cx + u * 0.72f, cy + u * 0.34f)
                canvas.drawRoundRect(screen, 10f, 10f, softFill)
                canvas.drawRoundRect(screen, 10f, 10f, primary)
                canvas.drawLine(cx - u * 0.45f, cy - u * 0.28f, cx + u * 0.25f, cy - u * 0.28f, accentStroke)
                canvas.drawLine(cx - u * 0.45f, cy - u * 0.02f, cx + u * 0.45f, cy - u * 0.02f, primary)
                canvas.drawRoundRect(RectF(cx - u * 0.92f, cy + u * 0.40f, cx + u * 0.92f, cy + u * 0.62f), 8f, 8f, fillPrimary)
            }
            "satellite" -> {
                canvas.drawArc(RectF(cx - u * 0.82f, cy - u * 0.35f, cx + u * 0.45f, cy + u * 0.85f), 20f, 160f, false, primary)
                canvas.drawLine(cx - u * 0.55f, cy + u * 0.55f, cx + u * 0.25f, cy - u * 0.25f, primary)
                canvas.drawCircle(cx + u * 0.28f, cy - u * 0.28f, u * 0.11f, accentFill)
                canvas.drawArc(RectF(cx + u * 0.1f, cy - u * 0.72f, cx + u * 0.75f, cy - u * 0.05f), -85f, 85f, false, accentStroke)
                canvas.drawArc(RectF(cx + u * 0.02f, cy - u * 0.92f, cx + u * 0.95f, cy + u * 0.05f), -85f, 85f, false, accentStroke)
            }
            "shield" -> {
                val shield = Path().apply {
                    moveTo(cx, cy - u * 0.9f)
                    lineTo(cx + u * 0.76f, cy - u * 0.58f)
                    lineTo(cx + u * 0.68f, cy + u * 0.12f)
                    cubicTo(cx + u * 0.58f, cy + u * 0.58f, cx + u * 0.24f, cy + u * 0.82f, cx, cy + u * 0.94f)
                    cubicTo(cx - u * 0.24f, cy + u * 0.82f, cx - u * 0.58f, cy + u * 0.58f, cx - u * 0.68f, cy + u * 0.12f)
                    lineTo(cx - u * 0.76f, cy - u * 0.58f)
                    close()
                }
                canvas.drawPath(shield, softFill)
                canvas.drawPath(shield, primary)
                val check = Path().apply {
                    moveTo(cx - u * 0.32f, cy + u * 0.02f)
                    lineTo(cx - u * 0.06f, cy + u * 0.28f)
                    lineTo(cx + u * 0.36f, cy - u * 0.22f)
                }
                canvas.drawPath(check, accentStroke)
            }
            "chart" -> {
                canvas.drawLine(cx - u * 0.82f, cy + u * 0.68f, cx + u * 0.82f, cy + u * 0.68f, primary)
                canvas.drawRoundRect(RectF(cx - u * 0.66f, cy + u * 0.10f, cx - u * 0.32f, cy + u * 0.68f), 6f, 6f, softFill)
                canvas.drawRoundRect(RectF(cx - u * 0.66f, cy + u * 0.10f, cx - u * 0.32f, cy + u * 0.68f), 6f, 6f, primary)
                canvas.drawRoundRect(RectF(cx - u * 0.17f, cy - u * 0.34f, cx + u * 0.17f, cy + u * 0.68f), 6f, 6f, fillPrimary)
                canvas.drawRoundRect(RectF(cx + u * 0.32f, cy - u * 0.72f, cx + u * 0.66f, cy + u * 0.68f), 6f, 6f, accentFill)
            }
            "trend_up" -> {
                canvas.drawLine(cx - u * 0.82f, cy - u * 0.75f, cx - u * 0.82f, cy + u * 0.72f, primary)
                canvas.drawLine(cx - u * 0.82f, cy + u * 0.72f, cx + u * 0.82f, cy + u * 0.72f, primary)
                val line = Path().apply {
                    moveTo(cx - u * 0.62f, cy + u * 0.40f)
                    lineTo(cx - u * 0.16f, cy - u * 0.04f)
                    lineTo(cx + u * 0.16f, cy + u * 0.18f)
                    lineTo(cx + u * 0.70f, cy - u * 0.56f)
                }
                canvas.drawPath(line, accentStroke)
                canvas.drawLine(cx + u * 0.36f, cy - u * 0.56f, cx + u * 0.70f, cy - u * 0.56f, accentStroke)
                canvas.drawLine(cx + u * 0.70f, cy - u * 0.22f, cx + u * 0.70f, cy - u * 0.56f, accentStroke)
            }
            "briefcase" -> {
                canvas.drawRoundRect(RectF(cx - u * 0.32f, cy - u * 0.72f, cx + u * 0.32f, cy - u * 0.35f), 8f, 8f, primary)
                val body = RectF(cx - u * 0.84f, cy - u * 0.38f, cx + u * 0.84f, cy + u * 0.68f)
                canvas.drawRoundRect(body, 14f, 14f, fillPrimary)
                canvas.drawLine(cx - u * 0.84f, cy + u * 0.04f, cx + u * 0.84f, cy + u * 0.04f, whiteFill)
                canvas.drawRoundRect(RectF(cx - u * 0.16f, cy - u * 0.06f, cx + u * 0.16f, cy + u * 0.18f), 4f, 4f, accentFill)
            }
            "target" -> {
                canvas.drawCircle(cx, cy, u * 0.85f, softFill)
                canvas.drawCircle(cx, cy, u * 0.85f, primary)
                canvas.drawCircle(cx, cy, u * 0.52f, whiteFill)
                canvas.drawCircle(cx, cy, u * 0.52f, primary)
                canvas.drawCircle(cx, cy, u * 0.22f, accentFill)
                canvas.drawLine(cx + u * 0.12f, cy - u * 0.12f, cx + u * 0.82f, cy - u * 0.82f, accentStroke)
            }
            "balance" -> {
                canvas.drawLine(cx, cy - u * 0.78f, cx, cy + u * 0.75f, primary)
                canvas.drawLine(cx - u * 0.45f, cy + u * 0.75f, cx + u * 0.45f, cy + u * 0.75f, primary)
                canvas.drawLine(cx - u * 0.78f, cy - u * 0.48f, cx + u * 0.78f, cy - u * 0.48f, primary)
                canvas.drawLine(cx - u * 0.72f, cy - u * 0.48f, cx - u * 0.90f, cy + u * 0.05f, primary)
                canvas.drawLine(cx - u * 0.72f, cy - u * 0.48f, cx - u * 0.54f, cy + u * 0.05f, primary)
                canvas.drawArc(RectF(cx - u * 0.96f, cy - u * 0.1f, cx - u * 0.48f, cy + u * 0.28f), 0f, 180f, true, accentFill)
                canvas.drawLine(cx + u * 0.72f, cy - u * 0.48f, cx + u * 0.54f, cy + u * 0.05f, primary)
                canvas.drawLine(cx + u * 0.72f, cy - u * 0.48f, cx + u * 0.90f, cy + u * 0.05f, primary)
                canvas.drawArc(RectF(cx + u * 0.48f, cy - u * 0.1f, cx + u * 0.96f, cy + u * 0.28f), 0f, 180f, true, accentFill)
            }
            "coins" -> {
                canvas.drawCircle(cx - u * 0.22f, cy + u * 0.16f, u * 0.52f, fillPrimary)
                canvas.drawCircle(cx - u * 0.22f, cy + u * 0.16f, u * 0.34f, whiteFill)
                canvas.drawCircle(cx + u * 0.25f, cy - u * 0.18f, u * 0.52f, accentFill)
                canvas.drawCircle(cx + u * 0.25f, cy - u * 0.18f, u * 0.52f, primary)
            }
            "book" -> {
                val leftPage = Path().apply {
                    moveTo(cx, cy - u * 0.45f)
                    cubicTo(cx - u * 0.35f, cy - u * 0.72f, cx - u * 0.75f, cy - u * 0.65f, cx - u * 0.88f, cy - u * 0.48f)
                    lineTo(cx - u * 0.88f, cy + u * 0.58f)
                    cubicTo(cx - u * 0.55f, cy + u * 0.45f, cx - u * 0.2f, cy + u * 0.52f, cx, cy + u * 0.68f)
                    close()
                }
                val rightPage = Path().apply {
                    moveTo(cx, cy - u * 0.45f)
                    cubicTo(cx + u * 0.35f, cy - u * 0.72f, cx + u * 0.75f, cy - u * 0.65f, cx + u * 0.88f, cy - u * 0.48f)
                    lineTo(cx + u * 0.88f, cy + u * 0.58f)
                    cubicTo(cx + u * 0.55f, cy + u * 0.45f, cx + u * 0.2f, cy + u * 0.52f, cx, cy + u * 0.68f)
                    close()
                }
                canvas.drawPath(leftPage, softFill)
                canvas.drawPath(rightPage, softFill)
                canvas.drawPath(leftPage, primary)
                canvas.drawPath(rightPage, primary)
                canvas.drawLine(cx - u * 0.62f, cy - u * 0.1f, cx - u * 0.22f, cy - u * 0.05f, accentStroke)
                canvas.drawLine(cx + u * 0.22f, cy - u * 0.05f, cx + u * 0.62f, cy - u * 0.1f, accentStroke)
            }
            "grad" -> {
                val cap = Path().apply {
                    moveTo(cx, cy - u * 0.68f)
                    lineTo(cx + u * 0.94f, cy - u * 0.18f)
                    lineTo(cx, cy + u * 0.28f)
                    lineTo(cx - u * 0.94f, cy - u * 0.18f)
                    close()
                }
                canvas.drawPath(cap, fillPrimary)
                canvas.drawArc(RectF(cx - u * 0.52f, cy + u * 0.08f, cx + u * 0.52f, cy + u * 0.68f), 0f, 180f, true, fillPrimary)
                canvas.drawLine(cx + u * 0.82f, cy - u * 0.12f, cx + u * 0.82f, cy + u * 0.52f, accentStroke)
                canvas.drawCircle(cx + u * 0.82f, cy + u * 0.56f, u * 0.11f, accentFill)
            }
            "lightbulb" -> {
                canvas.drawCircle(cx, cy - u * 0.22f, u * 0.52f, softFill)
                canvas.drawCircle(cx, cy - u * 0.22f, u * 0.52f, primary)
                canvas.drawRoundRect(RectF(cx - u * 0.26f, cy + u * 0.34f, cx + u * 0.26f, cy + u * 0.72f), 6f, 6f, fillPrimary)
                // Rays
                canvas.drawLine(cx, cy - u * 0.95f, cx, cy - u * 0.82f, accentStroke)
                canvas.drawLine(cx - u * 0.75f, cy - u * 0.62f, cx - u * 0.62f, cy - u * 0.52f, accentStroke)
                canvas.drawLine(cx + u * 0.75f, cy - u * 0.62f, cx + u * 0.62f, cy - u * 0.52f, accentStroke)
                canvas.drawLine(cx - u * 0.16f, cy - u * 0.15f, cx + u * 0.16f, cy - u * 0.15f, accentStroke)
            }
            "pen" -> {
                val nib = Path().apply {
                    moveTo(cx + u * 0.62f, cy - u * 0.78f)
                    lineTo(cx + u * 0.82f, cy - u * 0.58f)
                    lineTo(cx - u * 0.38f, cy + u * 0.62f)
                    lineTo(cx - u * 0.78f, cy + u * 0.78f)
                    lineTo(cx - u * 0.62f, cy + u * 0.38f)
                    close()
                }
                canvas.drawPath(nib, fillPrimary)
                canvas.drawLine(cx - u * 0.85f, cy + u * 0.82f, cx + u * 0.75f, cy + u * 0.82f, accentStroke)
            }
            "search" -> {
                canvas.drawCircle(cx - u * 0.18f, cy - u * 0.18f, u * 0.56f, softFill)
                canvas.drawCircle(cx - u * 0.18f, cy - u * 0.18f, u * 0.56f, primary)
                canvas.drawLine(cx + u * 0.24f, cy + u * 0.24f, cx + u * 0.82f, cy + u * 0.82f, accentStroke)
            }
            "clock" -> {
                canvas.drawCircle(cx, cy, u * 0.82f, softFill)
                canvas.drawCircle(cx, cy, u * 0.82f, primary)
                canvas.drawLine(cx, cy, cx, cy - u * 0.48f, primary)
                canvas.drawLine(cx, cy, cx + u * 0.38f, cy + u * 0.20f, accentStroke)
                canvas.drawCircle(cx, cy, u * 0.1f, fillPrimary)
            }
            "mosque" -> {
                val dome = Path().apply {
                    moveTo(cx - u * 0.68f, cy + u * 0.18f)
                    cubicTo(cx - u * 0.7f, cy - u * 0.4f, cx - u * 0.25f, cy - u * 0.55f, cx, cy - u * 0.78f)
                    cubicTo(cx + u * 0.25f, cy - u * 0.55f, cx + u * 0.7f, cy - u * 0.4f, cx + u * 0.68f, cy + u * 0.18f)
                    close()
                }
                canvas.drawPath(dome, fillPrimary)
                canvas.drawRoundRect(RectF(cx - u * 0.75f, cy + u * 0.16f, cx + u * 0.75f, cy + u * 0.74f), 6f, 6f, softFill)
                canvas.drawRoundRect(RectF(cx - u * 0.75f, cy + u * 0.16f, cx + u * 0.75f, cy + u * 0.74f), 6f, 6f, primary)
                canvas.drawLine(cx, cy - u * 0.94f, cx, cy - u * 0.74f, accentStroke)
            }
            "history" -> {
                val roof = Path().apply {
                    moveTo(cx, cy - u * 0.86f)
                    lineTo(cx + u * 0.88f, cy - u * 0.40f)
                    lineTo(cx - u * 0.88f, cy - u * 0.40f)
                    close()
                }
                canvas.drawPath(roof, fillPrimary)
                listOf(-0.56f, 0f, 0.56f).forEach { ox ->
                    canvas.drawRoundRect(RectF(cx + u * ox - u * 0.12f, cy - u * 0.34f, cx + u * ox + u * 0.12f, cy + u * 0.56f), 4f, 4f, primary)
                }
                canvas.drawRoundRect(RectF(cx - u * 0.88f, cy + u * 0.58f, cx + u * 0.88f, cy + u * 0.80f), 4f, 4f, accentFill)
            }
            "globe" -> {
                canvas.drawCircle(cx, cy, u * 0.84f, softFill)
                canvas.drawCircle(cx, cy, u * 0.84f, primary)
                canvas.drawOval(RectF(cx - u * 0.42f, cy - u * 0.84f, cx + u * 0.42f, cy + u * 0.84f), primary)
                canvas.drawLine(cx - u * 0.84f, cy, cx + u * 0.84f, cy, accentStroke)
                canvas.drawLine(cx, cy - u * 0.84f, cx, cy + u * 0.84f, primary)
            }
            "compass" -> {
                canvas.drawCircle(cx, cy, u * 0.84f, softFill)
                canvas.drawCircle(cx, cy, u * 0.84f, primary)
                val needleN = Path().apply {
                    moveTo(cx + u * 0.48f, cy - u * 0.48f)
                    lineTo(cx - u * 0.14f, cy - u * 0.14f)
                    lineTo(cx + u * 0.14f, cy + u * 0.14f)
                    close()
                }
                val needleS = Path().apply {
                    moveTo(cx - u * 0.48f, cy + u * 0.48f)
                    lineTo(cx - u * 0.14f, cy - u * 0.14f)
                    lineTo(cx + u * 0.14f, cy + u * 0.14f)
                    close()
                }
                canvas.drawPath(needleN, accentFill)
                canvas.drawPath(needleS, fillPrimary)
            }
            "trophy" -> {
                canvas.drawArc(RectF(cx - u * 0.54f, cy - u * 0.78f, cx + u * 0.54f, cy + u * 0.25f), 0f, 180f, true, fillPrimary)
                canvas.drawRect(RectF(cx - u * 0.54f, cy - u * 0.78f, cx + u * 0.54f, cy - u * 0.26f), fillPrimary)
                canvas.drawArc(RectF(cx - u * 0.86f, cy - u * 0.66f, cx - u * 0.46f, cy - u * 0.12f), 90f, 180f, false, accentStroke)
                canvas.drawArc(RectF(cx + u * 0.46f, cy - u * 0.66f, cx + u * 0.86f, cy - u * 0.12f), -90f, 180f, false, accentStroke)
                canvas.drawLine(cx, cy + u * 0.22f, cx, cy + u * 0.62f, primary)
                canvas.drawRoundRect(RectF(cx - u * 0.42f, cy + u * 0.62f, cx + u * 0.42f, cy + u * 0.82f), 6f, 6f, accentFill)
            }
            "check" -> {
                canvas.drawCircle(cx, cy, u * 0.80f, fillPrimary)
                val check = Path().apply {
                    moveTo(cx - u * 0.40f, cy + u * 0.02f)
                    lineTo(cx - u * 0.10f, cy + u * 0.34f)
                    lineTo(cx + u * 0.44f, cy - u * 0.28f)
                }
                val wp = Paint(primary).apply { color = Color.WHITE; strokeWidth = (size * 0.07f).coerceAtLeast(3f) }
                canvas.drawPath(check, wp)
            }
            "sparkles" -> {
                drawFourPointStar(canvas, cx - u * 0.12f, cy - u * 0.05f, u * 0.68f, fillPrimary)
                drawFourPointStar(canvas, cx + u * 0.55f, cy - u * 0.52f, u * 0.28f, accentFill)
                drawFourPointStar(canvas, cx + u * 0.52f, cy + u * 0.50f, u * 0.22f, accentFill)
            }
            else -> {
                // Render distinct dual-ring medallion emblem for all expanded 100-icon catalog entries
                val spec = com.example.engine.SlideIconCatalog.icons.firstOrNull { it.key == symbolKey }
                if (spec != null && symbolKey != "star") {
                    canvas.drawCircle(cx, cy, u * 0.90f, softFill)
                    canvas.drawCircle(cx, cy, u * 0.86f, primary)
                    canvas.drawCircle(cx, cy, u * 0.62f, accentFill)
                    val glyphPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.WHITE
                        this.alpha = alpha
                        textAlign = Paint.Align.CENTER
                        textSize = (u * 0.88f).coerceAtLeast(14f)
                        isFakeBoldText = true
                    }
                    val fm = glyphPaint.fontMetrics
                    val baseline = cy - (fm.ascent + fm.descent) / 2f
                    canvas.drawText(spec.glyph, cx, baseline, glyphPaint)
                } else {
                    val star = Path()
                    val outerR = u * 0.88f
                    val innerR = u * 0.40f
                    for (i in 0 until 10) {
                        val rr = if (i % 2 == 0) outerR else innerR
                        val rad = (i * 36.0 - 90.0) * PI / 180.0
                        val px = cx + (rr * cos(rad)).toFloat()
                        val py = cy + (rr * sin(rad)).toFloat()
                        if (i == 0) star.moveTo(px, py) else star.lineTo(px, py)
                    }
                    star.close()
                    canvas.drawPath(star, fillPrimary)
                    canvas.drawCircle(cx, cy, u * 0.2f, accentFill)
                }
            }
        }
    }

    private fun drawFourPointStar(canvas: Canvas, cx: Float, cy: Float, radius: Float, paint: Paint) {
        val path = Path().apply {
            moveTo(cx, cy - radius)
            quadTo(cx, cy, cx + radius, cy)
            quadTo(cx, cy, cx, cy + radius)
            quadTo(cx, cy, cx - radius, cy)
            quadTo(cx, cy, cx, cy - radius)
            close()
        }
        canvas.drawPath(path, paint)
    }

    private fun complementaryAccent(colorInt: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(colorInt, hsv)
        hsv[0] = (hsv[0] + 150f) % 360f
        hsv[1] = hsv[1].coerceAtLeast(0.65f)
        hsv[2] = hsv[2].coerceIn(0.65f, 0.92f)
        return Color.HSVToColor(hsv)
    }
}

@Composable
fun IllustrativeIconArtwork(
    symbolKey: String,
    tintColorHex: String = "#0D47A1",
    bgColorHex: String = "#1ADCE8FF",
    modifier: Modifier = Modifier
) {
    val tintInt = parseColorInt(tintColorHex, 0xFF0D47A1.toInt())
    val bgInt = parseColorInt(bgColorHex, 0x1ADCE8FF)
    ComposeCanvas(modifier = modifier.fillMaxSize()) {
        IllustrativeIconRenderer.drawOnCanvas(
            canvas = drawContext.canvas.nativeCanvas,
            symbolKey = symbolKey,
            left = 0f,
            top = 0f,
            width = size.width,
            height = size.height,
            tintColorInt = tintInt,
            bgColorInt = bgInt,
            alpha = 255
        )
    }
}
