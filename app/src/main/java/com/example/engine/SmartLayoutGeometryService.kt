package com.example.engine

import kotlin.math.max
import kotlin.math.min

data class ElementRect(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
) {
    val right: Float get() = x + width
    val bottom: Float get() = y + height
}

/**
 * Geometric constraint analyzer preventing overlaps when adding slide elements and providing
 * one-tap automatic spatial arrangement for crowded slides.
 */
object SmartLayoutGeometryService {

    private const val MARGIN_X = 64f
    private const val MARGIN_TOP = 64f
    private const val MARGIN_BOTTOM = 64f
    private const val GAP = 28f

    fun rectsOverlap(a: ElementRect, b: ElementRect, padding: Float = 8f): Boolean {
        return a.x < b.right + padding &&
            a.right + padding > b.x &&
            a.y < b.bottom + padding &&
            a.bottom + padding > b.y
    }

    private fun elementRect(el: SlideElement): ElementRect =
        ElementRect(el.x, el.y, el.width, el.height)

    private fun isFullBleedDecorative(el: SlideElement, canvasW: Float, canvasH: Float): Boolean {
        // Ignore thin top/side theme accent bars or full-slide backdrop shapes
        val area = el.width * el.height
        val canvasArea = canvasW * canvasH
        val isAccentStrip = el.width <= 36f || el.height <= 36f
        val isFullCard = el is SlideElement.ShapeElement && area > canvasArea * 0.45f
        return isAccentStrip || isFullCard
    }

    fun countOverlaps(slide: Slide, canvasW: Float, canvasH: Float): Int {
        val foreground = slide.elements.filterNot { isFullBleedDecorative(it, canvasW, canvasH) }
        var count = 0
        for (i in foreground.indices) {
            val ra = elementRect(foreground[i])
            for (j in i + 1 until foreground.size) {
                val rb = elementRect(foreground[j])
                // Ignore intentional text inside card shape
                val aIsShape = foreground[i] is SlideElement.ShapeElement
                val bIsShape = foreground[j] is SlideElement.ShapeElement
                if (aIsShape != bIsShape && (foreground[i] is SlideElement.TextElement || foreground[j] is SlideElement.TextElement)) {
                    val container = if (aIsShape) ra else rb
                    val inner = if (aIsShape) rb else ra
                    val inside = inner.x >= container.x - 16f && inner.right <= container.right + 16f &&
                        inner.y >= container.y - 16f && inner.bottom <= container.bottom + 16f
                    if (inside) continue
                }
                if (rectsOverlap(ra, rb, padding = 2f)) {
                    count++
                }
            }
        }
        return count
    }

    /**
     * Suggests a non-overlapping (x, y, width, height) inside canvas bounds for a newly added element.
     */
    fun suggestPlacement(
        newElement: SlideElement,
        existingElements: List<SlideElement>,
        canvasW: Float,
        canvasH: Float
    ): SlideElement {
        val w = newElement.width.coerceIn(100f, (canvasW - MARGIN_X * 2f).coerceAtLeast(120f))
        val h = newElement.height.coerceIn(80f, (canvasH - MARGIN_TOP - MARGIN_BOTTOM).coerceAtLeast(100f))

        val obstacles = existingElements
            .filterNot { isFullBleedDecorative(it, canvasW, canvasH) }
            .map { elementRect(it) }

        if (obstacles.isEmpty()) {
            val centeredX = ((canvasW - w) / 2f).coerceIn(MARGIN_X, canvasW - w - MARGIN_X)
            val centeredY = ((canvasH - h) / 2f).coerceIn(MARGIN_TOP, canvasH - h - MARGIN_BOTTOM)
            return newElement.withTransform(x = centeredX, y = centeredY, width = w, height = h)
        }

        // Candidate anchor grid scanning row by row within safe margins
        val maxX = (canvasW - MARGIN_X - w).coerceAtLeast(MARGIN_X)
        val maxY = (canvasH - MARGIN_BOTTOM - h).coerceAtLeast(MARGIN_TOP)
        val stepX = 80f
        val stepY = 72f

        // First test original intended position clamped to canvas
        val clampedOrig = ElementRect(
            newElement.x.coerceIn(MARGIN_X, maxX),
            newElement.y.coerceIn(MARGIN_TOP, maxY),
            w,
            h
        )
        if (obstacles.none { rectsOverlap(clampedOrig, it, GAP) }) {
            return newElement.withTransform(x = clampedOrig.x, y = clampedOrig.y, width = w, height = h)
        }

        // Search systematic free slots (prefer lower-right or open bands below title)
        var y = max(MARGIN_TOP, canvasH * 0.22f)
        while (y <= maxY) {
            var x = MARGIN_X
            while (x <= maxX) {
                val cand = ElementRect(x, y, w, h)
                if (obstacles.none { rectsOverlap(cand, it, GAP) }) {
                    return newElement.withTransform(x = x, y = y, width = w, height = h)
                }
                x += stepX
            }
            y += stepY
        }

        // If slide is nearly full, gently scale down new element to 72% and re-scan from top
        val compactW = max(120f, w * 0.72f)
        val compactH = max(90f, h * 0.72f)
        val compactMaxX = (canvasW - MARGIN_X - compactW).coerceAtLeast(MARGIN_X)
        val compactMaxY = (canvasH - MARGIN_BOTTOM - compactH).coerceAtLeast(MARGIN_TOP)
        var cy = MARGIN_TOP
        while (cy <= compactMaxY) {
            var cx = MARGIN_X
            while (cx <= compactMaxX) {
                val cand = ElementRect(cx, cy, compactW, compactH)
                if (obstacles.none { rectsOverlap(cand, it, GAP * 0.6f) }) {
                    return newElement.withTransform(x = cx, y = cy, width = compactW, height = compactH)
                }
                cx += stepX
            }
            cy += stepY
        }

        // Clean cascade fallback inside bounds
        val offset = (existingElements.size * 44f)
        val fx = (MARGIN_X + offset).coerceAtMost(maxX)
        val fy = (MARGIN_TOP + offset).coerceAtMost(maxY)
        return newElement.withTransform(x = fx, y = fy, width = w, height = h)
    }

    /**
     * Re-arranges all foreground elements on a crowded slide into clean non-overlapping bands.
     */
    fun autoArrangeSlide(slide: Slide, canvasW: Float, canvasH: Float): Slide {
        if (slide.elements.isEmpty()) return slide
        val decorative = slide.elements.filter { isFullBleedDecorative(it, canvasW, canvasH) }
        val foreground = slide.elements.filterNot { isFullBleedDecorative(it, canvasW, canvasH) }
        if (foreground.size <= 1) return slide

        // Keep top header/title element at top if there's a large bold text element
        val header = foreground.filterIsInstance<SlideElement.TextElement>()
            .sortedBy { it.y }
            .firstOrNull { it.isBold || it.fontSizeSp >= 36f }

        val content = if (header != null) foreground.filterNot { it.id == header.id } else foreground
        val arranged = mutableListOf<SlideElement>()
        arranged.addAll(decorative)

        var contentTop = MARGIN_TOP
        if (header != null) {
            val headerH = min(header.height, canvasH * 0.17f).coerceAtLeast(96f)
            arranged.add(
                header.withTransform(
                    x = MARGIN_X,
                    y = MARGIN_TOP,
                    width = canvasW - MARGIN_X * 2f,
                    height = headerH
                )
            )
            contentTop = MARGIN_TOP + headerH + GAP
        }

        if (content.isNotEmpty()) {
            val availW = (canvasW - MARGIN_X * 2f).coerceAtLeast(240f)
            val availH = (canvasH - contentTop - MARGIN_BOTTOM).coerceAtLeast(200f)
            val cols = when {
                content.size == 1 -> 1
                content.size <= 4 -> 2
                else -> 3
            }
            val rows = (content.size + cols - 1) / cols
            val cellW = (availW - GAP * (cols - 1)) / cols
            val cellH = (availH - GAP * (rows - 1)) / rows

            content.forEachIndexed { idx, el ->
                val r = idx / cols
                val c = idx % cols
                val cx = MARGIN_X + c * (cellW + GAP)
                val cy = contentTop + r * (cellH + GAP)
                val targetW = if (el is SlideElement.IconElement) min(el.width, min(cellW, cellH) * 0.8f) else cellW
                val targetH = if (el is SlideElement.IconElement) targetW else cellH
                val offsetX = if (el is SlideElement.IconElement) cx + (cellW - targetW) / 2f else cx
                val offsetY = if (el is SlideElement.IconElement) cy + (cellH - targetH) / 2f else cy
                arranged.add(
                    el.withTransform(
                        x = offsetX,
                        y = offsetY,
                        width = targetW.coerceAtLeast(80f),
                        height = targetH.coerceAtLeast(70f),
                        rotation = if (el.rotation != 0f) 0f else el.rotation
                    )
                )
            }
        }
        return slide.copy(elements = arranged)
    }
}
