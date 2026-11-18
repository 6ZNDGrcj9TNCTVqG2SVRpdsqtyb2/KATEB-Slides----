package com.example.engines

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.engine.OrnamentRenderKind
import com.example.engine.ShapeType
import com.example.engine.SlideDecorationCatalog
import com.example.engine.SlideElement
import com.example.engine.SmartArtLayoutType
import com.example.engine.SmartArtNode
import com.example.engine.parseColorInt
import com.example.ui.theme.SlideFontRegistry
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

object SmartArtAndOrnamentRenderer {

    fun drawSlideDecorationCanvas(
        canvas: Canvas,
        decorationId: String,
        colorHex: String,
        w: Float,
        h: Float
    ) {
        if (decorationId == "none" || decorationId.isBlank()) return
        val spec = SlideDecorationCatalog.findById(decorationId)
        if (spec.renderKind == OrnamentRenderKind.NONE) return

        val baseColor = parseColorInt(colorHex.ifBlank { spec.defaultHex }, 0xFF1565C0.toInt())
        val density = spec.strokeDensity.coerceIn(1, 3)
        val strokeW = (min(w, h) * 0.004f * density).coerceIn(2f, 12f)
        val pad = min(w, h) * 0.032f

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = baseColor
            alpha = 185
            strokeWidth = strokeW
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val softFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = baseColor
            alpha = 34
        }
        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = baseColor
            alpha = 140
        }

        when (spec.renderKind) {
            OrnamentRenderKind.NONE -> Unit
            OrnamentRenderKind.L_CORNERS -> {
                val len = min(w, h) * (0.10f + density * 0.02f)
                // Top-left
                canvas.drawLine(pad, pad + len, pad, pad, strokePaint)
                canvas.drawLine(pad, pad, pad + len, pad, strokePaint)
                // Top-right
                canvas.drawLine(w - pad - len, pad, w - pad, pad, strokePaint)
                canvas.drawLine(w - pad, pad, w - pad, pad + len, strokePaint)
                // Bottom-left
                canvas.drawLine(pad, h - pad - len, pad, h - pad, strokePaint)
                canvas.drawLine(pad, h - pad, pad + len, h - pad, strokePaint)
                // Bottom-right
                canvas.drawLine(w - pad - len, h - pad, w - pad, h - pad, strokePaint)
                canvas.drawLine(w - pad, h - pad - len, w - pad, h - pad, strokePaint)
            }
            OrnamentRenderKind.DOUBLE_FRAME -> {
                canvas.drawRoundRect(RectF(pad, pad, w - pad, h - pad), 22f, 22f, strokePaint)
                val inner = pad + strokeW * 2.4f
                val thin = Paint(strokePaint).apply {
                    strokeWidth = (strokeW * 0.5f).coerceAtLeast(1.5f)
                    alpha = 130
                }
                canvas.drawRoundRect(RectF(inner, inner, w - inner, h - inner), 16f, 16f, thin)
            }
            OrnamentRenderKind.ARABESQUE_ARCH -> {
                val archH = min(w, h) * 0.14f
                val path = Path().apply {
                    moveTo(pad, h - pad)
                    lineTo(pad, pad + archH)
                    quadTo(pad, pad, pad + archH, pad)
                    lineTo(w * 0.36f, pad)
                    quadTo(w * 0.5f, pad + archH * 0.55f, w * 0.64f, pad)
                    lineTo(w - pad - archH, pad)
                    quadTo(w - pad, pad, w - pad, pad + archH)
                    lineTo(w - pad, h - pad)
                    close()
                }
                canvas.drawPath(path, strokePaint)
                // Corner 8-point rosettes
                drawRosette(canvas, pad + archH * 0.45f, pad + archH * 0.45f, archH * 0.28f, accentPaint)
                drawRosette(canvas, w - pad - archH * 0.45f, pad + archH * 0.45f, archH * 0.28f, accentPaint)
            }
            OrnamentRenderKind.DIAMOND_CHAIN -> {
                val r = (min(w, h) * 0.015f * density).coerceIn(6f, 22f)
                val step = r * 3.1f
                var cx = pad + r * 2
                while (cx < w - pad - r) {
                    drawDiamond(canvas, cx, pad, r, accentPaint)
                    drawDiamond(canvas, cx, h - pad, r, accentPaint)
                    cx += step
                }
            }
            OrnamentRenderKind.CIRCUIT_TRACE -> {
                val seg = min(w, h) * 0.12f
                val corners = listOf(
                    Pair(pad, pad) to Pair(1f, 1f),
                    Pair(w - pad, pad) to Pair(-1f, 1f),
                    Pair(pad, h - pad) to Pair(1f, -1f),
                    Pair(w - pad, h - pad) to Pair(-1f, -1f)
                )
                corners.forEach { (pt, dir) ->
                    val (ox, oy) = pt
                    val (dx, dy) = dir
                    val p = Path().apply {
                        moveTo(ox, oy + dy * seg)
                        lineTo(ox + dx * seg * 0.4f, oy + dy * seg * 0.6f)
                        lineTo(ox + dx * seg * 0.4f, oy + dy * seg * 0.25f)
                        lineTo(ox + dx * seg, oy + dy * seg * 0.25f)
                    }
                    canvas.drawPath(p, strokePaint)
                    canvas.drawCircle(ox + dx * seg, oy + dy * seg * 0.25f, strokeW * 1.4f, accentPaint)
                    canvas.drawCircle(ox, oy + dy * seg, strokeW * 1.4f, accentPaint)
                }
            }
            OrnamentRenderKind.TOP_BOTTOM_RIBBON -> {
                val bandH = min(w, h) * (0.020f + density * 0.008f)
                canvas.drawRect(RectF(0f, 0f, w, bandH), accentPaint)
                canvas.drawRect(RectF(0f, h - bandH * 0.65f, w, h), softFillPaint)
                canvas.drawLine(pad, bandH * 1.6f, w - pad, bandH * 1.6f, strokePaint)
            }
            OrnamentRenderKind.DOT_MATRIX -> {
                val radius = (min(w, h) * 0.006f).coerceIn(2.5f, 8f)
                val gap = radius * 3.4f
                for (r in 0 until 5) {
                    for (c in 0 until 5) {
                        canvas.drawCircle(pad + c * gap, pad + r * gap, radius, accentPaint)
                        canvas.drawCircle(w - pad - c * gap, h - pad - r * gap, radius, accentPaint)
                    }
                }
            }
            OrnamentRenderKind.DIAGONAL_WAVES -> {
                for (i in 0 until (2 + density)) {
                    val offset = i * pad * 0.55f
                    val path = Path().apply {
                        moveTo(0f, h * 0.85f + offset * 0.25f)
                        cubicTo(w * 0.35f, h * 0.72f + offset, w * 0.68f, h * 1.02f - offset, w, h * 0.88f)
                    }
                    canvas.drawPath(path, strokePaint)
                }
            }
            OrnamentRenderKind.HEX_WATERMARK -> {
                val hr = min(w, h) * 0.085f
                listOf(
                    w - pad - hr * 1.1f to pad + hr * 1.1f,
                    w - pad - hr * 2.5f to pad + hr * 0.85f,
                    pad + hr * 1.1f to h - pad - hr * 1.1f
                ).forEach { (hx, hy) ->
                    val p = Path()
                    for (i in 0 until 6) {
                        val a = (i * 60.0 - 30.0) * PI / 180.0
                        val px = hx + (hr * cos(a)).toFloat()
                        val py = hy + (hr * sin(a)).toFloat()
                        if (i == 0) p.moveTo(px, py) else p.lineTo(px, py)
                    }
                    p.close()
                    canvas.drawPath(p, softFillPaint)
                    canvas.drawPath(p, strokePaint)
                }
            }
            OrnamentRenderKind.ART_DECO_CORNER -> {
                val span = min(w, h) * 0.14f
                canvas.drawRect(RectF(pad, pad, w - pad, h - pad), strokePaint)
                listOf(
                    pad to pad, w - pad to pad, pad to h - pad, w - pad to h - pad
                ).forEach { (cx, cy) ->
                    canvas.drawCircle(cx, cy, span * 0.22f, softFillPaint)
                    canvas.drawCircle(cx, cy, span * 0.22f, strokePaint)
                }
            }
            OrnamentRenderKind.GOLD_BOOK_SEAL -> {
                val frame = RectF(pad, pad, w - pad, h - pad)
                canvas.drawRect(frame, strokePaint)
                val sealR = min(w, h) * 0.048f
                val sx = w - pad - sealR * 1.4f
                val sy = pad + sealR * 1.4f
                drawRosette(canvas, sx, sy, sealR, softFillPaint)
                drawRosette(canvas, sx, sy, sealR * 0.72f, accentPaint)
            }
        }
    }

    private fun drawDiamond(canvas: Canvas, cx: Float, cy: Float, r: Float, paint: Paint) {
        val p = Path().apply {
            moveTo(cx, cy - r)
            lineTo(cx + r, cy)
            lineTo(cx, cy + r)
            lineTo(cx - r, cy)
            close()
        }
        canvas.drawPath(p, paint)
    }

    private fun drawRosette(canvas: Canvas, cx: Float, cy: Float, r: Float, paint: Paint) {
        for (i in 0 until 4) {
            canvas.save()
            canvas.rotate(i * 45f, cx, cy)
            canvas.drawRect(RectF(cx - r * 0.55f, cy - r * 0.55f, cx + r * 0.55f, cy + r * 0.55f), paint)
            canvas.restore()
        }
    }

    fun drawExtendedShapeCanvas(
        context: Context,
        el: SlideElement.ShapeElement,
        canvas: Canvas,
        alpha: Int,
        fallbackFontId: String
    ) {
        val rect = RectF(el.x, el.y, el.x + el.width, el.y + el.height)
        val fillColor = parseColorInt(el.fillColorHex, 0xFF1565C0.toInt())
        val strokeColor = parseColorInt(el.strokeColorHex, 0xFF0D47A1.toInt())
        val hasFill = (fillColor ushr 24) > 0
        val use3D = el.is3D || el.shapeType.is3DShape
        val depth = el.depth3D.coerceIn(6f, 64f)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = fillColor
            this.alpha = ((fillColor ushr 24) * alpha) / 255
            if (el.hasShadow) setShadowLayer(12f, 0f, 6f, 0x44000000)
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = el.strokeWidth
            color = strokeColor
            this.alpha = alpha
        }

        if (use3D && hasFill && el.shapeType != ShapeType.LINE) {
            val darkSideColor = shadeColor(fillColor, 0.62f)
            val lightTopColor = tintColor(fillColor, 0.28f)
            val sidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = darkSideColor
                this.alpha = alpha
            }
            val topPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = lightTopColor
                this.alpha = alpha
            }

            when (el.shapeType) {
                ShapeType.SPHERE_3D, ShapeType.CIRCLE -> {
                    fillPaint.shader = RadialGradient(
                        rect.left + rect.width() * 0.34f,
                        rect.top + rect.height() * 0.30f,
                        max(rect.width(), rect.height()) * 0.72f,
                        intArrayOf(lightTopColor, fillColor, darkSideColor),
                        floatArrayOf(0f, 0.55f, 1f),
                        Shader.TileMode.CLAMP
                    )
                    canvas.drawOval(rect, fillPaint)
                    if (el.strokeWidth > 0f) canvas.drawOval(rect, strokePaint)
                }
                ShapeType.CYLINDER_3D -> {
                    val capH = (rect.height() * 0.22f).coerceIn(14f, depth * 1.4f)
                    val bodyRect = RectF(rect.left, rect.top + capH / 2f, rect.right, rect.bottom - capH / 2f)
                    canvas.drawRect(bodyRect, fillPaint)
                    canvas.drawOval(RectF(rect.left, rect.bottom - capH, rect.right, rect.bottom), sidePaint)
                    canvas.drawOval(RectF(rect.left, rect.top, rect.right, rect.top + capH), topPaint)
                    if (el.strokeWidth > 0f) {
                        canvas.drawOval(RectF(rect.left, rect.top, rect.right, rect.top + capH), strokePaint)
                        canvas.drawLine(rect.left, rect.top + capH / 2f, rect.left, rect.bottom - capH / 2f, strokePaint)
                        canvas.drawLine(rect.right, rect.top + capH / 2f, rect.right, rect.bottom - capH / 2f, strokePaint)
                    }
                }
                ShapeType.CONE_3D -> {
                    val baseH = (rect.height() * 0.22f).coerceIn(14f, depth * 1.4f)
                    val cone = Path().apply {
                        moveTo(rect.centerX(), rect.top)
                        lineTo(rect.right, rect.bottom - baseH / 2f)
                        lineTo(rect.left, rect.bottom - baseH / 2f)
                        close()
                    }
                    canvas.drawOval(RectF(rect.left, rect.bottom - baseH, rect.right, rect.bottom), sidePaint)
                    canvas.drawPath(cone, fillPaint)
                    val rightFace = Path().apply {
                        moveTo(rect.centerX(), rect.top)
                        lineTo(rect.right, rect.bottom - baseH / 2f)
                        lineTo(rect.centerX(), rect.bottom - baseH / 2f)
                        close()
                    }
                    canvas.drawPath(rightFace, Paint(sidePaint).also { it.alpha = (alpha * 0.45f).toInt() })
                    if (el.strokeWidth > 0f) canvas.drawPath(cone, strokePaint)
                }
                ShapeType.PYRAMID_3D -> {
                    val pyrLeft = Path().apply {
                        moveTo(rect.centerX(), rect.top)
                        lineTo(rect.left, rect.bottom - depth * 0.4f)
                        lineTo(rect.centerX() + rect.width() * 0.08f, rect.bottom)
                        close()
                    }
                    val pyrRight = Path().apply {
                        moveTo(rect.centerX(), rect.top)
                        lineTo(rect.centerX() + rect.width() * 0.08f, rect.bottom)
                        lineTo(rect.right, rect.bottom - depth * 0.55f)
                        close()
                    }
                    canvas.drawPath(pyrLeft, fillPaint)
                    canvas.drawPath(pyrRight, sidePaint)
                    if (el.strokeWidth > 0f) {
                        canvas.drawPath(pyrLeft, strokePaint)
                        canvas.drawPath(pyrRight, strokePaint)
                    }
                }
                else -> {
                    // Extruded 3D cube/prism/bevel/pillar/any 2D shape with 3D enabled
                    val dx = depth * 0.65f
                    val dy = -depth * 0.50f
                    val frontRect = RectF(rect.left, rect.top + absFloat(dy), rect.right - dx, rect.bottom)
                    val topFace = Path().apply {
                        moveTo(frontRect.left, frontRect.top)
                        lineTo(frontRect.left + dx, frontRect.top + dy)
                        lineTo(rect.right, frontRect.top + dy)
                        lineTo(frontRect.right, frontRect.top)
                        close()
                    }
                    val sideFace = Path().apply {
                        moveTo(frontRect.right, frontRect.top)
                        lineTo(rect.right, frontRect.top + dy)
                        lineTo(rect.right, frontRect.bottom + dy)
                        lineTo(frontRect.right, frontRect.bottom)
                        close()
                    }
                    canvas.drawPath(topFace, topPaint)
                    canvas.drawPath(sideFace, sidePaint)
                    val frontPath = SlideBitmapRenderer.buildShapePath(el.shapeType, frontRect)
                    canvas.drawPath(frontPath, fillPaint)
                    if (el.shapeType == ShapeType.BEVEL_3D) {
                        val inset = min(frontRect.width(), frontRect.height()) * 0.14f
                        val innerRect = RectF(
                            frontRect.left + inset, frontRect.top + inset,
                            frontRect.right - inset, frontRect.bottom - inset
                        )
                        canvas.drawRect(innerRect, topPaint)
                    }
                    if (el.strokeWidth > 0f) {
                        canvas.drawPath(frontPath, strokePaint)
                        canvas.drawPath(topFace, strokePaint)
                        canvas.drawPath(sideFace, strokePaint)
                    }
                }
            }
        } else {
            val path = SlideBitmapRenderer.buildShapePath(el.shapeType, rect)
            if (el.shapeType != ShapeType.LINE) {
                if (hasFill) canvas.drawPath(path, fillPaint)
                if (el.strokeWidth > 0f && (strokeColor ushr 24) > 0) canvas.drawPath(path, strokePaint)
            } else {
                strokePaint.strokeWidth = el.strokeWidth.coerceAtLeast(4f)
                canvas.drawLine(rect.left, rect.centerY(), rect.right, rect.centerY(), strokePaint)
            }
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

    fun drawSmartArtElementCanvas(
        context: Context,
        el: SlideElement.SmartArtElement,
        canvas: Canvas,
        alpha: Int
    ) {
        val nodes = el.nodes.ifEmpty { return }
        val connPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = parseColorInt(el.connectorColorHex, 0xFF64748B.toInt())
            this.alpha = alpha
            style = Paint.Style.STROKE
            strokeWidth = 5f
            strokeCap = Paint.Cap.ROUND
        }

        fun drawNodeBox(rect: RectF, node: SmartArtNode, isOval: Boolean = false, corner: Float = 20f) {
            val baseColor = parseColorInt(node.fillColorHex, 0xFF1565C0.toInt())
            if (el.is3D) {
                val depthPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = shadeColor(baseColor, 0.58f)
                    this.alpha = alpha
                }
                val shifted = RectF(rect.left + 8f, rect.top + 8f, rect.right + 8f, rect.bottom + 8f)
                if (isOval) canvas.drawOval(shifted, depthPaint)
                else canvas.drawRoundRect(shifted, corner, corner, depthPaint)
            }
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = baseColor
                this.alpha = alpha
            }
            if (isOval) canvas.drawOval(rect, fillPaint)
            else canvas.drawRoundRect(rect, corner, corner, fillPaint)

            val topHighlight = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2.5f
                color = tintColor(baseColor, 0.45f)
                this.alpha = alpha
            }
            if (isOval) canvas.drawOval(rect, topHighlight)
            else canvas.drawRoundRect(rect, corner, corner, topHighlight)

            drawCenteredNodeText(context, canvas, node.text, rect, node.textColorHex, el.fontFamilyId, alpha)
        }

        val r = RectF(el.x, el.y, el.x + el.width, el.y + el.height)
        val n = nodes.size

        when (el.layoutType) {
            SmartArtLayoutType.PROCESS_ARROWS, SmartArtLayoutType.CHEVRON_FLOW, SmartArtLayoutType.TIMELINE_ROADMAP -> {
                val gap = min(32f, r.width() * 0.04f)
                val boxW = (r.width() - gap * (n - 1)) / n
                val boxH = r.height() * 0.62f
                val top = r.centerY() - boxH / 2f
                if (el.layoutType == SmartArtLayoutType.TIMELINE_ROADMAP) {
                    canvas.drawLine(r.left + boxW / 2f, r.centerY(), r.right - boxW / 2f, r.centerY(), connPaint)
                }
                nodes.forEachIndexed { i, node ->
                    val left = r.left + i * (boxW + gap)
                    val boxRect = RectF(left, top, left + boxW, top + boxH)
                    if (el.layoutType == SmartArtLayoutType.CHEVRON_FLOW) {
                        val chevron = buildChevronPath(boxRect)
                        drawPathNode(context, canvas, chevron, boxRect, node, el.is3D, el.fontFamilyId, alpha)
                    } else {
                        drawNodeBox(boxRect, node)
                    }
                    if (i < n - 1 && el.layoutType != SmartArtLayoutType.TIMELINE_ROADMAP) {
                        val ax1 = boxRect.right + 4f
                        val ax2 = boxRect.right + gap - 4f
                        canvas.drawLine(ax1, r.centerY(), ax2, r.centerY(), connPaint)
                        canvas.drawLine(ax2 - 10f, r.centerY() - 8f, ax2, r.centerY(), connPaint)
                        canvas.drawLine(ax2 - 10f, r.centerY() + 8f, ax2, r.centerY(), connPaint)
                    }
                }
            }
            SmartArtLayoutType.CYCLE_RING, SmartArtLayoutType.RADIAL_HUB, SmartArtLayoutType.GEAR_SYSTEM -> {
                val ringR = min(r.width(), r.height()) * 0.34f
                val nodeD = min(r.width(), r.height()) * 0.30f
                if (el.layoutType == SmartArtLayoutType.CYCLE_RING) {
                    canvas.drawCircle(r.centerX(), r.centerY(), ringR, connPaint)
                } else {
                    nodes.forEachIndexed { i, _ ->
                        val ang = (i.toDouble() / n) * 2.0 * PI - PI / 2.0
                        val nx = r.centerX() + (ringR * cos(ang)).toFloat()
                        val ny = r.centerY() + (ringR * sin(ang)).toFloat()
                        canvas.drawLine(r.centerX(), r.centerY(), nx, ny, connPaint)
                    }
                }
                nodes.forEachIndexed { i, node ->
                    val ang = (i.toDouble() / n) * 2.0 * PI - PI / 2.0
                    val nx = r.centerX() + (ringR * cos(ang)).toFloat()
                    val ny = r.centerY() + (ringR * sin(ang)).toFloat()
                    val nr = RectF(nx - nodeD / 2f, ny - nodeD / 2f, nx + nodeD / 2f, ny + nodeD / 2f)
                    drawNodeBox(nr, node, isOval = true)
                }
            }
            SmartArtLayoutType.HIERARCHY_ORG -> {
                val topNode = nodes.first()
                val children = nodes.drop(1).ifEmpty { listOf(topNode) }
                val nodeH = r.height() * 0.32f
                val topW = min(r.width() * 0.42f, 360f)
                val topRect = RectF(r.centerX() - topW / 2f, r.top + 8f, r.centerX() + topW / 2f, r.top + 8f + nodeH)
                val childCount = children.size.coerceAtLeast(1)
                val gap = 22f
                val childW = (r.width() - gap * (childCount - 1)) / childCount
                val childTop = r.bottom - nodeH - 8f
                val midY = (topRect.bottom + childTop) / 2f
                canvas.drawLine(topRect.centerX(), topRect.bottom, topRect.centerX(), midY, connPaint)
                children.forEachIndexed { idx, node ->
                    val left = r.left + idx * (childW + gap)
                    val cr = RectF(left, childTop, left + childW, childTop + nodeH)
                    canvas.drawLine(topRect.centerX(), midY, cr.centerX(), midY, connPaint)
                    canvas.drawLine(cr.centerX(), midY, cr.centerX(), cr.top, connPaint)
                    drawNodeBox(cr, node)
                }
                drawNodeBox(topRect, topNode)
            }
            SmartArtLayoutType.PYRAMID_LEVELS, SmartArtLayoutType.FUNNEL_STAGES -> {
                val isFunnel = el.layoutType == SmartArtLayoutType.FUNNEL_STAGES
                val gap = 12f
                val rowH = (r.height() - gap * (n - 1)) / n
                nodes.forEachIndexed { idx, node ->
                    val frac = if (isFunnel) 1f - idx * (0.55f / max(1, n - 1))
                    else 0.38f + idx * (0.62f / max(1, n - 1))
                    val rowW = r.width() * frac
                    val top = r.top + idx * (rowH + gap)
                    val rect = RectF(r.centerX() - rowW / 2f, top, r.centerX() + rowW / 2f, top + rowH)
                    drawNodeBox(rect, node, corner = 16f)
                }
            }
            SmartArtLayoutType.VENN_OVERLAP -> {
                val d = min(r.width() * 0.48f, r.height() * 0.72f)
                nodes.take(4).forEachIndexed { idx, node ->
                    val ang = (idx.toDouble() / min(4, n)) * 2.0 * PI
                    val cx = r.centerX() + (d * 0.30f * cos(ang)).toFloat()
                    val cy = r.centerY() + (d * 0.25f * sin(ang)).toFloat()
                    val vr = RectF(cx - d / 2f, cy - d / 2f, cx + d / 2f, cy + d / 2f)
                    val base = parseColorInt(node.fillColorHex, 0xFF1565C0.toInt())
                    val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = base
                        this.alpha = (alpha * 0.78f).toInt()
                    }
                    canvas.drawOval(vr, p)
                    drawCenteredNodeText(context, canvas, node.text, vr, node.textColorHex, el.fontFamilyId, alpha)
                }
            }
            SmartArtLayoutType.MATRIX_2X2, SmartArtLayoutType.SWOT_GRID, SmartArtLayoutType.ICON_GRID -> {
                val cols = if (n <= 2) n else 2
                val rows = (n + cols - 1) / cols
                val gap = 20f
                val cellW = (r.width() - gap * (cols - 1)) / cols
                val cellH = (r.height() - gap * (rows - 1)) / rows
                nodes.forEachIndexed { idx, node ->
                    val rr = idx / cols
                    val cc = idx % cols
                    val left = r.left + cc * (cellW + gap)
                    val top = r.top + rr * (cellH + gap)
                    drawNodeBox(RectF(left, top, left + cellW, top + cellH), node)
                }
            }
            SmartArtLayoutType.VERTICAL_LIST -> {
                val gap = 14f
                val rowH = (r.height() - gap * (n - 1)) / n
                nodes.forEachIndexed { idx, node ->
                    val top = r.top + idx * (rowH + gap)
                    drawNodeBox(RectF(r.left, top, r.right, top + rowH), node)
                }
            }
            SmartArtLayoutType.STEP_UP_STAIRS -> {
                val gap = 14f
                val boxW = (r.width() - gap * (n - 1)) / n
                val boxH = r.height() * 0.32f
                nodes.forEachIndexed { idx, node ->
                    val t = if (n > 1) idx.toFloat() / (n - 1) else 0f
                    val left = r.left + idx * (boxW + gap)
                    val top = r.bottom - boxH - t * (r.height() - boxH)
                    drawNodeBox(RectF(left, top, left + boxW, top + boxH), node)
                }
            }
            SmartArtLayoutType.BALANCE_COMPARE -> {
                val leftNode = nodes.first()
                val rightNode = nodes.getOrElse(1) { leftNode }
                val gap = 36f
                val colW = (r.width() - gap) / 2f
                canvas.drawLine(r.centerX(), r.top + 16f, r.centerX(), r.bottom - 16f, connPaint)
                drawNodeBox(RectF(r.left, r.top + 24f, r.left + colW, r.bottom - 24f), leftNode)
                drawNodeBox(RectF(r.left + colW + gap, r.top + 24f, r.right, r.bottom - 24f), rightNode)
            }
        }
    }

    private fun buildChevronPath(rect: RectF): Path {
        val tip = min(rect.width() * 0.16f, 44f)
        return Path().apply {
            moveTo(rect.left, rect.top)
            lineTo(rect.right - tip, rect.top)
            lineTo(rect.right, rect.centerY())
            lineTo(rect.right - tip, rect.bottom)
            lineTo(rect.left, rect.bottom)
            lineTo(rect.left + tip, rect.centerY())
            close()
        }
    }

    private fun drawPathNode(
        context: Context,
        canvas: Canvas,
        path: Path,
        bounds: RectF,
        node: SmartArtNode,
        is3D: Boolean,
        fontId: String,
        alpha: Int
    ) {
        val baseColor = parseColorInt(node.fillColorHex, 0xFF1565C0.toInt())
        if (is3D) {
            canvas.save()
            canvas.translate(8f, 8f)
            canvas.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = shadeColor(baseColor, 0.58f)
                this.alpha = alpha
            })
            canvas.restore()
        }
        canvas.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = baseColor
            this.alpha = alpha
        })
        drawCenteredNodeText(context, canvas, node.text, bounds, node.textColorHex, fontId, alpha)
    }

    private fun drawCenteredNodeText(
        context: Context,
        canvas: Canvas,
        text: String,
        rect: RectF,
        textColorHex: String,
        fontId: String,
        alpha: Int
    ) {
        if (text.isBlank()) return
        val tp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = (min(rect.width(), rect.height()) * 0.21f).coerceIn(16f, 34f)
            color = parseColorInt(textColorHex, 0xFFFFFFFF.toInt())
            this.alpha = alpha
            typeface = SlideFontRegistry.typefaceFor(context, fontId, isBold = true, isItalic = false)
        }
        val avail = (rect.width() - 24f).toInt().coerceAtLeast(24)
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, tp, avail)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .build()
        canvas.save()
        canvas.clipRect(rect)
        canvas.translate(rect.left + 12f, rect.top + ((rect.height() - layout.height) / 2f).coerceAtLeast(4f))
        layout.draw(canvas)
        canvas.restore()
    }

    fun shadeColor(colorInt: Int, factor: Float): Int {
        val a = Color.alpha(colorInt)
        val r = (Color.red(colorInt) * factor).toInt().coerceIn(0, 255)
        val g = (Color.green(colorInt) * factor).toInt().coerceIn(0, 255)
        val b = (Color.blue(colorInt) * factor).toInt().coerceIn(0, 255)
        return Color.argb(a, r, g, b)
    }

    fun tintColor(colorInt: Int, amount: Float): Int {
        val a = Color.alpha(colorInt)
        val r = (Color.red(colorInt) + (255 - Color.red(colorInt)) * amount).toInt().coerceIn(0, 255)
        val g = (Color.green(colorInt) + (255 - Color.green(colorInt)) * amount).toInt().coerceIn(0, 255)
        val b = (Color.blue(colorInt) + (255 - Color.blue(colorInt)) * amount).toInt().coerceIn(0, 255)
        return Color.argb(a, r, g, b)
    }

    private fun absFloat(v: Float): Float = if (v < 0f) -v else v
}
