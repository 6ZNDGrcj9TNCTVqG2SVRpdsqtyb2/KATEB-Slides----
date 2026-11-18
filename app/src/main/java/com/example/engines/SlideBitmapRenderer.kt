package com.example.engines

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.engine.ChartType
import com.example.engine.Presentation
import com.example.engine.ShapeType
import com.example.engine.Slide
import com.example.engine.SlideBackground
import com.example.engine.SlideElement
import com.example.engine.TextAlignment
import com.example.engine.TextListType
import com.example.engine.VerticalAlignment
import com.example.engine.detectTextIsRtl
import com.example.engine.parseColorInt
import com.example.ui.theme.SlideFontRegistry
import java.io.File
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

object SlideBitmapRenderer {

    fun renderSlideToBitmap(
        context: Context,
        presentation: Presentation,
        slide: Slide,
        targetWidth: Int = presentation.canvasWidth.toInt().coerceAtLeast(640),
        targetHeight: Int = presentation.canvasHeight.toInt().coerceAtLeast(360)
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawSlideOnCanvas(context, presentation, slide, canvas, targetWidth.toFloat(), targetHeight.toFloat())
        return bitmap
    }

    fun drawSlideOnCanvas(
        context: Context,
        presentation: Presentation,
        slide: Slide,
        canvas: Canvas,
        targetWidth: Float,
        targetHeight: Float
    ) {
        val sx = targetWidth / presentation.canvasWidth.coerceAtLeast(1f)
        val sy = targetHeight / presentation.canvasHeight.coerceAtLeast(1f)
        canvas.save()
        canvas.scale(sx, sy)

        drawBackground(slide.background, canvas, presentation.canvasWidth, presentation.canvasHeight)
        SmartArtAndOrnamentRenderer.drawSlideDecorationCanvas(
            canvas = canvas,
            decorationId = slide.decorationId,
            colorHex = slide.decorationColorHex,
            w = presentation.canvasWidth,
            h = presentation.canvasHeight
        )

        val sorted = slide.elements.sortedBy { it.zIndex }
        val slideFallbackFont = sorted
            .filterIsInstance<SlideElement.TextElement>()
            .firstOrNull()?.fontFamilyId ?: "cairo"
        for (el in sorted) {
            drawElement(context, el, canvas, slideFallbackFont)
        }

        canvas.restore()
    }

    private fun drawBackground(bg: SlideBackground, canvas: Canvas, w: Float, h: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        when (bg) {
            is SlideBackground.Solid -> {
                paint.color = parseColorInt(bg.colorHex, 0xFFFFFFFF.toInt())
                canvas.drawRect(0f, 0f, w, h, paint)
            }
            is SlideBackground.Gradient -> {
                val rad = Math.toRadians(bg.angleDegrees.toDouble())
                val x1 = (w / 2f) + (cos(rad) * w / 2f).toFloat()
                val y1 = (h / 2f) + (sin(rad) * h / 2f).toFloat()
                val x0 = w - x1
                val y0 = h - y1
                paint.shader = LinearGradient(
                    x0, y0, x1, y1,
                    parseColorInt(bg.startColorHex, 0xFF0D47A1.toInt()),
                    parseColorInt(bg.endColorHex, 0xFF1976D2.toInt()),
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w, h, paint)
            }
            is SlideBackground.Image -> {
                val bmp = decodeBitmapSampled(bg.localImagePath, 1280, 720)
                if (bmp != null) {
                    canvas.drawBitmap(bmp, null, RectF(0f, 0f, w, h), paint)
                    bmp.recycle()
                } else {
                    paint.color = 0xFF1E293B.toInt()
                    canvas.drawRect(0f, 0f, w, h, paint)
                }
                paint.color = parseColorInt(bg.overlayColorHex, 0x33000000)
                canvas.drawRect(0f, 0f, w, h, paint)
            }
        }
    }

    private fun drawElement(context: Context, el: SlideElement, canvas: Canvas, fallbackFontId: String) {
        val cx = el.x + el.width / 2f
        val cy = el.y + el.height / 2f
        canvas.save()
        if (el.rotation != 0f) {
            canvas.rotate(el.rotation, cx, cy)
        }
        val alpha = (el.opacity.coerceIn(0f, 1f) * 255).toInt()
        when (el) {
            is SlideElement.TextElement -> drawTextElement(context, el, canvas, alpha)
            is SlideElement.ImageElement -> drawImageElement(el, canvas, alpha)
            is SlideElement.ShapeElement -> SmartArtAndOrnamentRenderer.drawExtendedShapeCanvas(context, el, canvas, alpha, fallbackFontId)
            is SlideElement.SmartArtElement -> SmartArtAndOrnamentRenderer.drawSmartArtElementCanvas(context, el, canvas, alpha)
            is SlideElement.TableElement -> drawTableElement(context, el, canvas, alpha, fallbackFontId)
            is SlideElement.ChartElement -> drawChartElement(context, el, canvas, alpha, fallbackFontId)
            is SlideElement.EquationElement -> drawEquationElement(el, canvas, alpha)
            is SlideElement.IconElement -> drawIconElement(el, canvas, alpha)
        }
        canvas.restore()
    }

    private fun drawTextElement(
        context: Context,
        el: SlideElement.TextElement,
        canvas: Canvas,
        alpha: Int
    ) {
        val rect = RectF(el.x, el.y, el.x + el.width, el.y + el.height)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val bgColor = parseColorInt(el.backgroundColorHex, 0x00000000)
        if ((bgColor ushr 24) > 0) {
            bgPaint.color = bgColor
            bgPaint.alpha = ((bgColor ushr 24) * alpha) / 255
            canvas.drawRoundRect(rect, 12f, 12f, bgPaint)
        }
        if (el.borderWidth > 0f && (parseColorInt(el.borderColorHex, 0) ushr 24) > 0) {
            bgPaint.style = Paint.Style.STROKE
            bgPaint.strokeWidth = el.borderWidth
            bgPaint.color = parseColorInt(el.borderColorHex)
            bgPaint.alpha = alpha
            canvas.drawRoundRect(rect, 12f, 12f, bgPaint)
        }
        if (el.isBlockQuote) {
            val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = parseColorInt(el.textColorHex, 0xFF0D47A1.toInt())
                this.alpha = alpha
            }
            val isRtl = detectTextIsRtl(el.text, el.writingDirection)
            val barX = if (isRtl) rect.right - 12f else rect.left + 4f
            canvas.drawRoundRect(RectF(barX, rect.top + 8f, barX + 8f, rect.bottom - 8f), 4f, 4f, barPaint)
        }

        val formattedText = formatListText(el.text, el.listType)
        val tp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = el.fontSizeSp.coerceAtLeast(8f)
            color = parseColorInt(el.textColorHex, 0xFF0F172A.toInt())
            this.alpha = alpha
            typeface = SlideFontRegistry.typefaceFor(context, el.fontFamilyId, el.isBold, el.isItalic)
            isUnderlineText = el.isUnderline
            isStrikeThruText = el.isStrikethrough
            letterSpacing = el.letterSpacing / 24f
            if (el.hasShadow) {
                setShadowLayer(6f, 2f, 3f, 0x55000000)
            }
        }
        val highlightColor = parseColorInt(el.highlightColorHex, 0)
        if ((highlightColor ushr 24) > 0) {
            tp.bgColor = highlightColor
        }

        val isRtl = detectTextIsRtl(formattedText, el.writingDirection)
        val alignment = when (el.textAlign) {
            TextAlignment.CENTER -> Layout.Alignment.ALIGN_CENTER
            TextAlignment.END -> Layout.Alignment.ALIGN_OPPOSITE
            TextAlignment.START, TextAlignment.JUSTIFY -> Layout.Alignment.ALIGN_NORMAL
        }
        val textDir = if (isRtl) {
            android.text.TextDirectionHeuristics.RTL
        } else {
            android.text.TextDirectionHeuristics.LTR
        }
        val pad = 16
        val availWidth = (el.width.toInt() - pad * 2).coerceAtLeast(24)
        val layout = StaticLayout.Builder
            .obtain(formattedText, 0, formattedText.length, tp, availWidth)
            .setAlignment(alignment)
            .setTextDirection(textDir)
            .setLineSpacing(0f, el.lineHeightMultiplier.coerceIn(0.8f, 2.5f))
            .setIncludePad(false)
            .build()

        val dy = when (el.verticalAlign) {
            VerticalAlignment.TOP -> el.y + pad
            VerticalAlignment.CENTER -> el.y + ((el.height - layout.height) / 2f).coerceAtLeast(pad.toFloat())
            VerticalAlignment.BOTTOM -> el.y + (el.height - layout.height - pad).coerceAtLeast(pad.toFloat())
        }
        canvas.save()
        canvas.clipRect(rect)
        canvas.translate(el.x + pad, dy)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun drawImageElement(el: SlideElement.ImageElement, canvas: Canvas, alpha: Int) {
        val rect = RectF(el.x, el.y, el.x + el.width, el.y + el.height)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.alpha = alpha
            if (el.hasShadow) setShadowLayer(12f, 0f, 6f, 0x44000000)
        }
        val clipPath = Path().apply {
            addRoundRect(rect, el.cornerRadius, el.cornerRadius, Path.Direction.CW)
        }
        val bmp = decodeBitmapSampled(el.localImagePath, 1200, 1200)
        if (bmp != null) {
            canvas.save()
            canvas.clipPath(clipPath)
            val srcRect = Rect(
                (el.cropRect.left.coerceIn(0f, 0.9f) * bmp.width).toInt(),
                (el.cropRect.top.coerceIn(0f, 0.9f) * bmp.height).toInt(),
                (el.cropRect.right.coerceIn(0.1f, 1f) * bmp.width).toInt(),
                (el.cropRect.bottom.coerceIn(0.1f, 1f) * bmp.height).toInt()
            )
            canvas.drawBitmap(bmp, srcRect, rect, paint)
            canvas.restore()
            bmp.recycle()
        } else {
            paint.color = 0xFFE2E8F0.toInt()
            canvas.drawRoundRect(rect, el.cornerRadius, el.cornerRadius, paint)
        }

        if (el.borderWidth > 0f && (parseColorInt(el.borderColorHex, 0) ushr 24) > 0) {
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = el.borderWidth
                color = parseColorInt(el.borderColorHex)
                this.alpha = alpha
            }
            canvas.drawRoundRect(rect, el.cornerRadius, el.cornerRadius, borderPaint)
        }
    }

    private fun drawShapeElement(
        context: Context,
        el: SlideElement.ShapeElement,
        canvas: Canvas,
        alpha: Int,
        fallbackFontId: String
    ) {
        val rect = RectF(el.x, el.y, el.x + el.width, el.y + el.height)
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = parseColorInt(el.fillColorHex, 0xFF1565C0.toInt())
            this.alpha = ((color ushr 24) * alpha) / 255
            if (el.hasShadow) setShadowLayer(12f, 0f, 6f, 0x44000000)
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = el.strokeWidth
            color = parseColorInt(el.strokeColorHex, 0xFF0D47A1.toInt())
            this.alpha = alpha
        }

        val path = buildShapePath(el.shapeType, rect)
        if (el.shapeType != ShapeType.LINE) {
            if ((parseColorInt(el.fillColorHex, 0) ushr 24) > 0) {
                canvas.drawPath(path, fillPaint)
            }
            if (el.strokeWidth > 0f && (parseColorInt(el.strokeColorHex, 0) ushr 24) > 0) {
                canvas.drawPath(path, strokePaint)
            }
        } else {
            strokePaint.strokeWidth = el.strokeWidth.coerceAtLeast(4f)
            canvas.drawLine(rect.left, rect.centerY(), rect.right, rect.centerY(), strokePaint)
        }

        if (el.labelText.isNotBlank()) {
            val tp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 26f
                color = parseColorInt(el.labelColorHex, 0xFFFFFFFF.toInt())
                this.alpha = alpha
                typeface = SlideFontRegistry.typefaceFor(context, fallbackFontId, isBold = true, isItalic = false)
            }
            val layout = StaticLayout.Builder
                .obtain(el.labelText, 0, el.labelText.length, tp, (el.width - 24f).toInt().coerceAtLeast(20))
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .build()
            canvas.save()
            canvas.translate(el.x + 12f, el.y + (el.height - layout.height) / 2f)
            layout.draw(canvas)
            canvas.restore()
        }
    }

    private fun drawTableElement(
        context: Context,
        el: SlideElement.TableElement,
        canvas: Canvas,
        alpha: Int,
        fallbackFontId: String
    ) {
        val cols = el.cols.coerceAtLeast(1)
        val rows = el.rows.coerceAtLeast(1)
        val cellW = el.width / cols
        val cellH = el.height / rows
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = el.borderWidth.coerceAtLeast(1f)
            color = parseColorInt(el.borderColorHex, 0xFFCBD5E1.toInt())
            this.alpha = alpha
        }

        for (cell in el.cells) {
            if (cell.mergedIntoRow != null) continue
            val left = el.x + cell.col * cellW
            val top = el.y + cell.row * cellH
            val right = left + cellW * cell.colSpan.coerceAtLeast(1)
            val bottom = top + cellH * cell.rowSpan.coerceAtLeast(1)
            val cellRect = RectF(left, top, right, bottom)

            bgPaint.color = parseColorInt(cell.bgColorHex, 0xFFFFFFFF.toInt())
            bgPaint.alpha = alpha
            canvas.drawRect(cellRect, bgPaint)
            canvas.drawRect(cellRect, borderPaint)

            if (cell.text.isNotBlank()) {
                val tp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    textSize = el.fontSizeSp
                    color = parseColorInt(cell.textColorHex, 0xFF0F172A.toInt())
                    this.alpha = alpha
                    typeface = SlideFontRegistry.typefaceFor(context, fallbackFontId, cell.isBold || cell.isHeader, false)
                }
                val layout = StaticLayout.Builder
                    .obtain(cell.text, 0, cell.text.length, tp, (cellRect.width() - 16f).toInt().coerceAtLeast(16))
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .build()
                canvas.save()
                canvas.clipRect(cellRect)
                canvas.translate(cellRect.left + 8f, cellRect.top + ((cellRect.height() - layout.height) / 2f).coerceAtLeast(4f))
                layout.draw(canvas)
                canvas.restore()
            }
        }
    }

    private fun drawChartElement(
        context: Context,
        el: SlideElement.ChartElement,
        canvas: Canvas,
        alpha: Int,
        fallbackFontId: String
    ) {
        val rect = RectF(el.x, el.y, el.x + el.width, el.y + el.height)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = parseColorInt(el.bgColorHex, 0xFFFFFFFF.toInt())
            this.alpha = alpha
        }
        canvas.drawRoundRect(rect, 20f, 20f, bgPaint)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = 0xFFE2E8F0.toInt()
            this.alpha = alpha
        }
        canvas.drawRoundRect(rect, 20f, 20f, borderPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF0F172A.toInt()
            textSize = 30f
            this.alpha = alpha
            typeface = SlideFontRegistry.typefaceFor(context, fallbackFontId, isBold = true, isItalic = false)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(el.title, rect.centerX(), rect.top + 48f, titlePaint)

        val plotRect = RectF(rect.left + 72f, rect.top + 80f, rect.right - 40f, rect.bottom - if (el.showLegend) 86f else 56f)
        val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFCBD5E1.toInt()
            strokeWidth = 2f
            this.alpha = alpha
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF475569.toInt()
            textSize = 20f
            this.alpha = alpha
            textAlign = Paint.Align.CENTER
        }

        val maxVal = (el.series.flatMap { it.values }.maxOrNull() ?: 100f).coerceAtLeast(1f)
        val labelsCount = el.labels.size.coerceAtLeast(1)

        when (el.chartType) {
            ChartType.PIE -> {
                val values = el.series.firstOrNull()?.values.orEmpty()
                val total = values.sum().coerceAtLeast(1f)
                val diameter = min(plotRect.width(), plotRect.height()) * 0.84f
                val pieRect = RectF(
                    plotRect.centerX() - diameter / 2f,
                    plotRect.centerY() - diameter / 2f,
                    plotRect.centerX() + diameter / 2f,
                    plotRect.centerY() + diameter / 2f
                )
                val seriesColors = el.series.map { parseColorInt(it.colorHex, 0xFF0D47A1.toInt()) }
                val palette = (seriesColors + listOf(
                    0xFF0D47A1.toInt(), 0xFF00897B.toInt(), 0xFFF59E0B.toInt(),
                    0xFF6366F1.toInt(), 0xFFEC4899.toInt()
                )).distinct()
                var startAngle = -90f
                values.forEachIndexed { idx, v ->
                    val sweep = (v / total) * 360f
                    val slicePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = palette[idx % palette.size]
                        this.alpha = alpha
                    }
                    canvas.drawArc(pieRect, startAngle, sweep, true, slicePaint)
                    startAngle += sweep
                }
            }
            ChartType.BAR, ChartType.COMPARISON -> {
                canvas.drawLine(plotRect.left, plotRect.bottom, plotRect.right, plotRect.bottom, axisPaint)
                canvas.drawLine(plotRect.left, plotRect.top, plotRect.left, plotRect.bottom, axisPaint)
                val groupW = plotRect.width() / labelsCount
                val seriesCount = el.series.size.coerceAtLeast(1)
                val barW = (groupW * 0.68f) / seriesCount
                el.labels.forEachIndexed { i, lbl ->
                    val gx = plotRect.left + i * groupW + groupW * 0.16f
                    el.series.forEachIndexed { sIdx, s ->
                        val v = s.values.getOrElse(i) { 0f }
                        val barH = (v / maxVal) * plotRect.height() * 0.9f
                        val bx = gx + sIdx * barW
                        val by = plotRect.bottom - barH
                        val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = parseColorInt(s.colorHex, 0xFF1565C0.toInt())
                            this.alpha = alpha
                        }
                        canvas.drawRoundRect(RectF(bx + 2f, by, bx + barW - 2f, plotRect.bottom), 6f, 6f, barPaint)
                    }
                    canvas.drawText(lbl, plotRect.left + i * groupW + groupW / 2f, plotRect.bottom + 26f, labelPaint)
                }
            }
            ChartType.LINE, ChartType.AREA -> {
                canvas.drawLine(plotRect.left, plotRect.bottom, plotRect.right, plotRect.bottom, axisPaint)
                canvas.drawLine(plotRect.left, plotRect.top, plotRect.left, plotRect.bottom, axisPaint)
                val stepX = if (labelsCount > 1) plotRect.width() / (labelsCount - 1) else plotRect.width()
                el.series.forEach { s ->
                    val sColor = parseColorInt(s.colorHex, 0xFF1565C0.toInt())
                    val linePath = Path()
                    val areaPath = Path()
                    s.values.forEachIndexed { idx, v ->
                        val px = plotRect.left + idx * stepX
                        val py = plotRect.bottom - (v / maxVal) * plotRect.height() * 0.9f
                        if (idx == 0) {
                            linePath.moveTo(px, py)
                            areaPath.moveTo(px, plotRect.bottom)
                            areaPath.lineTo(px, py)
                        } else {
                            linePath.lineTo(px, py)
                            areaPath.lineTo(px, py)
                        }
                        if (idx == s.values.lastIndex) {
                            areaPath.lineTo(px, plotRect.bottom)
                            areaPath.close()
                        }
                    }
                    if (el.chartType == ChartType.AREA) {
                        val areaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = sColor
                            this.alpha = (alpha * 0.25f).toInt()
                        }
                        canvas.drawPath(areaPath, areaPaint)
                    }
                    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = 5f
                        color = sColor
                        this.alpha = alpha
                    }
                    canvas.drawPath(linePath, strokePaint)
                }
                el.labels.forEachIndexed { idx, lbl ->
                    val px = plotRect.left + idx * stepX
                    canvas.drawText(lbl, px, plotRect.bottom + 26f, labelPaint)
                }
            }
        }

        if (el.showLegend && el.series.isNotEmpty()) {
            var lx = rect.left + 48f
            val ly = rect.bottom - 22f
            el.series.forEach { s ->
                val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = parseColorInt(s.colorHex, 0xFF1565C0.toInt())
                    this.alpha = alpha
                }
                canvas.drawCircle(lx, ly - 6f, 8f, dotPaint)
                labelPaint.textAlign = Paint.Align.LEFT
                canvas.drawText(s.name, lx + 16f, ly, labelPaint)
                lx += 160f
            }
        }
    }

    private fun drawEquationElement(el: SlideElement.EquationElement, canvas: Canvas, alpha: Int) {
        val rect = RectF(el.x, el.y, el.x + el.width, el.y + el.height)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = parseColorInt(el.bgColorHex, 0xFFF8FAFC.toInt())
            this.alpha = alpha
        }
        canvas.drawRoundRect(rect, 18f, 18f, bgPaint)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = 0xFFCBD5E1.toInt()
            this.alpha = alpha
        }
        canvas.drawRoundRect(rect, 18f, 18f, borderPaint)

        val readable = formatMathExpression(el.expression)
        val tp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = parseColorInt(el.colorHex, 0xFF0D47A1.toInt())
            textSize = el.fontSizeSp
            this.alpha = alpha
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.SERIF
        }
        canvas.drawText(readable, rect.centerX(), rect.centerY() + el.fontSizeSp * 0.35f, tp)
    }

    private fun drawIconElement(el: SlideElement.IconElement, canvas: Canvas, alpha: Int) {
        com.example.ui.editor.IllustrativeIconRenderer.drawOnCanvas(
            canvas = canvas,
            symbolKey = el.symbolKey,
            left = el.x,
            top = el.y,
            width = el.width,
            height = el.height,
            tintColorInt = parseColorInt(el.tintColorHex, 0xFF0D47A1.toInt()),
            bgColorInt = parseColorInt(el.bgColorHex, 0x1ADCE8FF),
            alpha = alpha
        )
    }

    fun formatMathExpression(raw: String): String {
        return raw
            .replace("\\frac{", "(")
            .replace("}{", ") / (")
            .replace("\\sqrt{", "√(")
            .replace("\\int", "∫")
            .replace("\\sum", "∑")
            .replace("\\pm", "±")
            .replace("\\times", "×")
            .replace("\\div", "÷")
            .replace("\\leq", "≤")
            .replace("\\geq", "≥")
            .replace("\\neq", "≠")
            .replace("\\approx", "≈")
            .replace("\\rightarrow", "→")
            .replace("\\alpha", "α")
            .replace("\\beta", "β")
            .replace("\\theta", "θ")
            .replace("\\pi", "π")
            .replace("\\Delta", "Δ")
            .replace("\\Sigma", "Σ")
            .replace("\\lambda", "λ")
            .replace("\\Omega", "Ω")
            .replace("\\infty", "∞")
            .replace("}", ")")
            .replace("{", "")
            .replace("\\\\", "  ")
    }

    fun formatListText(text: String, listType: TextListType): String {
        if (listType == TextListType.NONE || text.isBlank()) return text
        return text.lines().mapIndexed { index, line ->
            val trimmed = line.trimStart()
            when {
                trimmed.isEmpty() -> line
                listType == TextListType.BULLET -> if (trimmed.startsWith("•")) line else "• $trimmed"
                listType == TextListType.NUMBERED -> "${index + 1}. $trimmed"
                listType == TextListType.CHECKBOX ->
                    if (trimmed.startsWith("☐") || trimmed.startsWith("☑")) line else "☐ $trimmed"
                else -> line
            }
        }.joinToString("\n")
    }

    fun buildShapePath(type: ShapeType, rect: RectF): Path {
        val path = Path()
        val w = rect.width()
        val h = rect.height()
        when (type) {
            ShapeType.RECTANGLE -> path.addRect(rect, Path.Direction.CW)
            ShapeType.ROUNDED_RECT -> path.addRoundRect(rect, min(w, h) * 0.14f, min(w, h) * 0.14f, Path.Direction.CW)
            ShapeType.CIRCLE -> path.addOval(rect, Path.Direction.CW)
            ShapeType.TRIANGLE -> {
                path.moveTo(rect.centerX(), rect.top)
                path.lineTo(rect.right, rect.bottom)
                path.lineTo(rect.left, rect.bottom)
                path.close()
            }
            ShapeType.DIAMOND -> {
                path.moveTo(rect.centerX(), rect.top)
                path.lineTo(rect.right, rect.centerY())
                path.lineTo(rect.centerX(), rect.bottom)
                path.lineTo(rect.left, rect.centerY())
                path.close()
            }
            ShapeType.ARROW_RIGHT -> {
                path.moveTo(rect.left, rect.top + h * 0.25f)
                path.lineTo(rect.left + w * 0.62f, rect.top + h * 0.25f)
                path.lineTo(rect.left + w * 0.62f, rect.top)
                path.lineTo(rect.right, rect.centerY())
                path.lineTo(rect.left + w * 0.62f, rect.bottom)
                path.lineTo(rect.left + w * 0.62f, rect.bottom - h * 0.25f)
                path.lineTo(rect.left, rect.bottom - h * 0.25f)
                path.close()
            }
            ShapeType.ARROW_LEFT -> {
                path.moveTo(rect.right, rect.top + h * 0.25f)
                path.lineTo(rect.left + w * 0.38f, rect.top + h * 0.25f)
                path.lineTo(rect.left + w * 0.38f, rect.top)
                path.lineTo(rect.left, rect.centerY())
                path.lineTo(rect.left + w * 0.38f, rect.bottom)
                path.lineTo(rect.left + w * 0.38f, rect.bottom - h * 0.25f)
                path.lineTo(rect.right, rect.bottom - h * 0.25f)
                path.close()
            }
            ShapeType.STAR -> {
                val cx = rect.centerX()
                val cy = rect.centerY()
                val outerR = min(w, h) / 2f
                val innerR = outerR * 0.42f
                for (i in 0 until 10) {
                    val r = if (i % 2 == 0) outerR else innerR
                    val angle = (i * 36.0 - 90.0) * PI / 180.0
                    val px = cx + (r * cos(angle)).toFloat()
                    val py = cy + (r * sin(angle)).toFloat()
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
            }
            ShapeType.HEXAGON -> {
                val cx = rect.centerX()
                val cy = rect.centerY()
                val rx = w / 2f
                val ry = h / 2f
                for (i in 0 until 6) {
                    val angle = (i * 60.0 - 30.0) * PI / 180.0
                    val px = cx + (rx * cos(angle)).toFloat()
                    val py = cy + (ry * sin(angle)).toFloat()
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
            }
            ShapeType.CALLOUT -> {
                val body = RectF(rect.left, rect.top, rect.right, rect.bottom - h * 0.2f)
                path.addRoundRect(body, 20f, 20f, Path.Direction.CW)
                path.moveTo(rect.left + w * 0.2f, body.bottom)
                path.lineTo(rect.left + w * 0.14f, rect.bottom)
                path.lineTo(rect.left + w * 0.36f, body.bottom)
                path.close()
            }
            ShapeType.PENTAGON -> addRegularPolygon(path, rect, 5, -90.0)
            ShapeType.OCTAGON -> addRegularPolygon(path, rect, 8, -67.5)
            ShapeType.CHEVRON -> {
                val tip = min(w * 0.22f, h * 0.45f)
                path.moveTo(rect.left, rect.top)
                path.lineTo(rect.right - tip, rect.top)
                path.lineTo(rect.right, rect.centerY())
                path.lineTo(rect.right - tip, rect.bottom)
                path.lineTo(rect.left, rect.bottom)
                path.lineTo(rect.left + tip, rect.centerY())
                path.close()
            }
            ShapeType.PARALLELOGRAM -> {
                val skew = w * 0.18f
                path.moveTo(rect.left + skew, rect.top)
                path.lineTo(rect.right, rect.top)
                path.lineTo(rect.right - skew, rect.bottom)
                path.lineTo(rect.left, rect.bottom)
                path.close()
            }
            ShapeType.CROSS_PLUS -> {
                val t = 0.32f
                path.moveTo(rect.left + w * t, rect.top)
                path.lineTo(rect.right - w * t, rect.top)
                path.lineTo(rect.right - w * t, rect.top + h * t)
                path.lineTo(rect.right, rect.top + h * t)
                path.lineTo(rect.right, rect.bottom - h * t)
                path.lineTo(rect.right - w * t, rect.bottom - h * t)
                path.lineTo(rect.right - w * t, rect.bottom)
                path.lineTo(rect.left + w * t, rect.bottom)
                path.lineTo(rect.left + w * t, rect.bottom - h * t)
                path.lineTo(rect.left, rect.bottom - h * t)
                path.lineTo(rect.left, rect.top + h * t)
                path.lineTo(rect.left + w * t, rect.top + h * t)
                path.close()
            }
            ShapeType.LINE -> {
                path.moveTo(rect.left, rect.centerY())
                path.lineTo(rect.right, rect.centerY())
            }
            ShapeType.CUBE_3D, ShapeType.PRISM_3D -> path.addRect(rect, Path.Direction.CW)
            ShapeType.CYLINDER_3D, ShapeType.PILLAR_3D, ShapeType.BEVEL_3D ->
                path.addRoundRect(rect, min(w, h) * 0.12f, min(w, h) * 0.12f, Path.Direction.CW)
            ShapeType.PYRAMID_3D, ShapeType.CONE_3D -> {
                path.moveTo(rect.centerX(), rect.top)
                path.lineTo(rect.right, rect.bottom)
                path.lineTo(rect.left, rect.bottom)
                path.close()
            }
            ShapeType.SPHERE_3D -> path.addOval(rect, Path.Direction.CW)
        }
        return path
    }

    private fun addRegularPolygon(path: Path, rect: RectF, sides: Int, startDeg: Double) {
        val cx = rect.centerX()
        val cy = rect.centerY()
        val rx = rect.width() / 2f
        val ry = rect.height() / 2f
        for (i in 0 until sides) {
            val angle = (i * (360.0 / sides) + startDeg) * PI / 180.0
            val px = cx + (rx * cos(angle)).toFloat()
            val py = cy + (ry * sin(angle)).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
    }

    fun decodeBitmapSampled(path: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        if (path.isBlank() || !File(path).exists()) return null
        return runCatching {
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, opts)
            var inSample = 1
            while (opts.outWidth / inSample > reqWidth * 2 || opts.outHeight / inSample > reqHeight * 2) {
                inSample *= 2
            }
            val finalOpts = BitmapFactory.Options().apply { inSampleSize = inSample }
            BitmapFactory.decodeFile(path, finalOpts)
        }.getOrNull()
    }
}
