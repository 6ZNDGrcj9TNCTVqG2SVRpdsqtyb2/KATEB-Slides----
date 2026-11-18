package com.example.ui.editor

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material.icons.rounded.RotateRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.engine.ChartType
import com.example.engine.Presentation
import com.example.engine.ShapeType
import com.example.engine.Slide
import com.example.engine.SlideBackground
import com.example.engine.SlideElement
import com.example.engine.SlideIconCatalog
import com.example.engine.TextAlignment
import com.example.engine.VerticalAlignment
import com.example.engine.detectTextIsRtl
import com.example.engine.parseColorInt
import com.example.engines.SlideBitmapRenderer
import com.example.ui.theme.SlideFontRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun SlideCanvas(
    presentation: Presentation,
    slide: Slide,
    selectedElementIds: Set<String>,
    isEditable: Boolean,
    isMultiSelectMode: Boolean = false,
    isCanvasLocked: Boolean = false,
    snapToGrid: Boolean = true,
    showGrid: Boolean = false,
    showSafeMargins: Boolean = false,
    canvasZoom: Float = 1f,
    onSelectElement: (String?, Boolean) -> Unit,
    onDoubleTapElement: (SlideElement) -> Unit,
    onStartTransformGesture: () -> Unit,
    onTransformDelta: (elementId: String, dx: Float, dy: Float, newW: Float?, newH: Float?, newRot: Float?) -> Unit,
    onCommitTransform: (beforeSnapshot: Presentation) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier.then(
            if (canvasZoom > 1.02f) {
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .verticalScroll(rememberScrollState())
            } else Modifier
        ),
        contentAlignment = Alignment.Center
    ) {
        val maxW = with(density) { maxWidth.toPx() }.coerceAtLeast(100f)
        val maxH = with(density) { maxHeight.toPx() }.coerceAtLeast(100f)
        val baseScale = min(maxW / presentation.canvasWidth, maxH / presentation.canvasHeight)
        val scale = baseScale * canvasZoom.coerceIn(0.65f, 2.25f)
        val renderedW = with(density) { (presentation.canvasWidth * scale).toDp() }
        val renderedH = with(density) { (presentation.canvasHeight * scale).toDp() }

        // Force LTR coordinate layout inside the slide viewport so logical (x, y) never flips across UI languages
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Box(
                modifier = Modifier
                    .width(renderedW)
                    .height(renderedH)
                    .shadow(if (isEditable) 8.dp else 0.dp, RoundedCornerShape(if (isEditable) 10.dp else 0.dp))
                    .clip(RoundedCornerShape(if (isEditable) 10.dp else 0.dp))
                    .pointerInput(isEditable, slide.id) {
                        if (isEditable) {
                            detectTapGestures { onSelectElement(null, false) }
                        }
                    }
                    .testTag("slide_canvas")
            ) {
                SlideBackgroundLayer(slide.background)
                OrnamentDecorationLayer(
                    decorationId = slide.decorationId,
                    decorationColorHex = slide.decorationColorHex
                )

                if (showGrid || showSafeMargins) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        if (showGrid) {
                            val stepPx = 80f * scale
                            if (stepPx >= 8f) {
                                var gx = stepPx
                                while (gx < size.width) {
                                    drawLine(
                                        color = Color(0x3864748B),
                                        start = Offset(gx, 0f),
                                        end = Offset(gx, size.height),
                                        strokeWidth = 1f
                                    )
                                    gx += stepPx
                                }
                                var gy = stepPx
                                while (gy < size.height) {
                                    drawLine(
                                        color = Color(0x3864748B),
                                        start = Offset(0f, gy),
                                        end = Offset(size.width, gy),
                                        strokeWidth = 1f
                                    )
                                    gy += stepPx
                                }
                            }
                        }
                        if (showSafeMargins) {
                            val inset = 64f * scale
                            if (size.width > inset * 2 && size.height > inset * 2) {
                                drawRect(
                                    color = Color(0x880284C7),
                                    topLeft = Offset(inset, inset),
                                    size = Size(size.width - inset * 2f, size.height - inset * 2f),
                                    style = Stroke(
                                        width = 1.5f,
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                                    )
                                )
                            }
                        }
                    }
                }

                var showCenterGuideX by remember { mutableStateOf(false) }
                var showCenterGuideY by remember { mutableStateOf(false) }

                val sortedElements = remember(slide.elements) {
                    slide.elements.sortedBy { it.zIndex }
                }

                sortedElements.forEach { element ->
                    val isSelected = element.id in selectedElementIds
                    val elX = with(density) { (element.x * scale).toDp() }
                    val elY = with(density) { (element.y * scale).toDp() }
                    val elW = with(density) { (element.width * scale).toDp() }
                    val elH = with(density) { (element.height * scale).toDp() }

                    var snapshotBeforeDrag by remember { mutableStateOf<Presentation?>(null) }

                    Box(
                        modifier = Modifier
                            .offset(x = elX, y = elY)
                            .size(width = elW, height = elH)
                            .graphicsLayer {
                                rotationZ = element.rotation
                                alpha = element.opacity.coerceIn(0f, 1f)
                            }
                            .then(
                                if (isEditable) {
                                    Modifier
                                        .pointerInput(element.id, isMultiSelectMode) {
                                            detectTapGestures(
                                                onTap = { onSelectElement(element.id, isMultiSelectMode) },
                                                onDoubleTap = {
                                                    onSelectElement(element.id, false)
                                                    onDoubleTapElement(element)
                                                }
                                            )
                                        }
                                        .then(
                                            if (!isCanvasLocked) {
                                                Modifier.pointerInput(element.id, scale, presentation.id, snapToGrid) {
                                                    var accumX = 0f
                                                    var accumY = 0f
                                                    detectDragGestures(
                                                        onDragStart = {
                                                            accumX = 0f
                                                            accumY = 0f
                                                            snapshotBeforeDrag = presentation
                                                            onSelectElement(element.id, false)
                                                            onStartTransformGesture()
                                                        },
                                                        onDragEnd = {
                                                            showCenterGuideX = false
                                                            showCenterGuideY = false
                                                            snapshotBeforeDrag?.let { onCommitTransform(it) }
                                                            snapshotBeforeDrag = null
                                                        },
                                                        onDragCancel = {
                                                            showCenterGuideX = false
                                                            showCenterGuideY = false
                                                            snapshotBeforeDrag = null
                                                        },
                                                        onDrag = { change, dragAmount ->
                                                            change.consume()
                                                            val rawDx = dragAmount.x / scale
                                                            val rawDy = dragAmount.y / scale
                                                            val emitDx: Float
                                                            val emitDy: Float
                                                            if (snapToGrid) {
                                                                accumX += rawDx
                                                                accumY += rawDy
                                                                val gridStep = 10f
                                                                emitDx = (accumX / gridStep).toInt() * gridStep
                                                                emitDy = (accumY / gridStep).toInt() * gridStep
                                                                accumX -= emitDx
                                                                accumY -= emitDy
                                                            } else {
                                                                emitDx = rawDx
                                                                emitDy = rawDy
                                                            }
                                                            if (emitDx != 0f || emitDy != 0f) {
                                                                val centerElX = element.x + emitDx + element.width / 2f
                                                                val centerElY = element.y + emitDy + element.height / 2f
                                                                showCenterGuideX = abs(centerElX - presentation.canvasWidth / 2f) < 18f
                                                                showCenterGuideY = abs(centerElY - presentation.canvasHeight / 2f) < 18f
                                                                onTransformDelta(element.id, emitDx, emitDy, null, null, null)
                                                            }
                                                        }
                                                    )
                                                }
                                            } else Modifier
                                        )
                                } else Modifier
                            )
                            .testTag("slide_element_${element.id}")
                    ) {
                        SlideElementContent(element = element, scale = scale)

                        if (isEditable && isSelected) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(2.dp, if (isCanvasLocked) Color(0xFFF59E0B) else Color(0xFF2563EB))
                            )
                        }
                    }

                    // Selection Resize + Rotation Handles rendered cleanly above element (disabled when stabilized/locked)
                    if (isEditable && !isCanvasLocked && isSelected && selectedElementIds.size == 1) {
                        ElementTransformHandles(
                            element = element,
                            scale = scale,
                            presentation = presentation,
                            onStartGesture = onStartTransformGesture,
                            onTransformDelta = onTransformDelta,
                            onCommitTransform = onCommitTransform
                        )
                    }
                }

                // Alignment snap guides
                if (showCenterGuideX) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(Color(0xFFEF4444).copy(alpha = 0.7f))
                    )
                }
                if (showCenterGuideY) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .height(1.dp)
                            .fillMaxWidth()
                            .background(Color(0xFFEF4444).copy(alpha = 0.7f))
                    )
                }
            }
        }
    }
}

@Composable
private fun ElementTransformHandles(
    element: SlideElement,
    scale: Float,
    presentation: Presentation,
    onStartGesture: () -> Unit,
    onTransformDelta: (elementId: String, dx: Float, dy: Float, newW: Float?, newH: Float?, newRot: Float?) -> Unit,
    onCommitTransform: (beforeSnapshot: Presentation) -> Unit
) {
    val density = LocalDensity.current
    val handleSize = 18.dp
    val halfHandlePx = with(density) { (handleSize / 2).toPx() }
    val elLeftPx = element.x * scale
    val elTopPx = element.y * scale
    val elWidthPx = element.width * scale
    val elHeightPx = element.height * scale

    var snap by remember { mutableStateOf<Presentation?>(null) }

    // Bottom-Right Resize Handle
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (elLeftPx + elWidthPx - halfHandlePx).roundToInt(),
                    (elTopPx + elHeightPx - halfHandlePx).roundToInt()
                )
            }
            .size(handleSize)
            .clip(CircleShape)
            .background(Color.White)
            .border(2.dp, Color(0xFF2563EB), CircleShape)
            .pointerInput(element.id, scale) {
                detectDragGestures(
                    onDragStart = { snap = presentation; onStartGesture() },
                    onDragEnd = { snap?.let { onCommitTransform(it) }; snap = null },
                    onDrag = { change, drag ->
                        change.consume()
                        onTransformDelta(
                            element.id,
                            0f,
                            0f,
                            element.width + drag.x / scale,
                            element.height + drag.y / scale,
                            null
                        )
                    }
                )
            }
            .testTag("resize_br_handle")
    )

    // Top-Right Resize Handle
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (elLeftPx + elWidthPx - halfHandlePx).roundToInt(),
                    (elTopPx - halfHandlePx).roundToInt()
                )
            }
            .size(handleSize)
            .clip(CircleShape)
            .background(Color.White)
            .border(2.dp, Color(0xFF2563EB), CircleShape)
            .pointerInput(element.id, scale) {
                detectDragGestures(
                    onDragStart = { snap = presentation; onStartGesture() },
                    onDragEnd = { snap?.let { onCommitTransform(it) }; snap = null },
                    onDrag = { change, drag ->
                        change.consume()
                        val dH = -drag.y / scale
                        onTransformDelta(
                            element.id,
                            0f,
                            drag.y / scale,
                            element.width + drag.x / scale,
                            element.height + dH,
                            null
                        )
                    }
                )
            }
    )

    // Bottom-Center Rotation Handle
    val rotHandleSize = 26.dp
    val rotHalfPx = with(density) { (rotHandleSize / 2).toPx() }
    Surface(
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 3.dp,
        modifier = Modifier
            .offset {
                IntOffset(
                    (elLeftPx + elWidthPx / 2f - rotHalfPx).roundToInt(),
                    (elTopPx + elHeightPx + with(density) { 10.dp.toPx() }).roundToInt()
                )
            }
            .size(rotHandleSize)
            .border(1.5.dp, Color(0xFF2563EB), CircleShape)
            .pointerInput(element.id, scale) {
                detectDragGestures(
                    onDragStart = { snap = presentation; onStartGesture() },
                    onDragEnd = { snap?.let { onCommitTransform(it) }; snap = null },
                    onDrag = { change, drag ->
                        change.consume()
                        val cx = elLeftPx + elWidthPx / 2f
                        val cy = elTopPx + elHeightPx / 2f
                        val curX = elLeftPx + elWidthPx / 2f + drag.x
                        val curY = elTopPx + elHeightPx + 20f + drag.y
                        val angleRad = atan2((curY - cy).toDouble(), (curX - cx).toDouble())
                        val deg = ((angleRad * 180.0 / PI).toFloat() - 90f)
                        onTransformDelta(element.id, 0f, 0f, null, null, (element.rotation + drag.x * 0.45f + deg * 0f) % 360f)
                    }
                )
            }
            .testTag("rotate_handle")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                Icons.Rounded.RotateRight,
                contentDescription = "Rotate",
                tint = Color(0xFF2563EB),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun SlideBackgroundLayer(bg: SlideBackground) {
    when (bg) {
        is SlideBackground.Solid -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(parseColorInt(bg.colorHex, 0xFFFFFFFF.toInt())))
            )
        }
        is SlideBackground.Gradient -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(parseColorInt(bg.startColorHex, 0xFF0D47A1.toInt())),
                                Color(parseColorInt(bg.endColorHex, 0xFF1976D2.toInt()))
                            )
                        )
                    )
            )
        }
        is SlideBackground.Image -> {
            Box(modifier = Modifier.fillMaxSize()) {
                val file = File(bg.localImagePath)
                if (file.exists()) {
                    AsyncImage(
                        model = file,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1E293B)))
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(parseColorInt(bg.overlayColorHex, 0x33000000)))
                )
            }
        }
    }
}

@Composable
fun SlideElementContent(element: SlideElement, scale: Float) {
    when (element) {
        is SlideElement.TextElement -> {
            val formatted = remember(element.text, element.listType) {
                SlideBitmapRenderer.formatListText(element.text, element.listType)
            }
            val isRtl = remember(formatted, element.writingDirection) {
                detectTextIsRtl(formatted, element.writingDirection)
            }
            val textDir = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
            val align = when (element.textAlign) {
                TextAlignment.CENTER -> TextAlign.Center
                TextAlignment.END -> TextAlign.End
                TextAlignment.JUSTIFY -> TextAlign.Justify
                TextAlignment.START -> TextAlign.Start
            }
            val vAlign = when (element.verticalAlign) {
                VerticalAlignment.TOP -> Alignment.TopStart
                VerticalAlignment.CENTER -> Alignment.CenterStart
                VerticalAlignment.BOTTOM -> Alignment.BottomStart
            }
            val decorations = buildList {
                if (element.isUnderline) add(TextDecoration.Underline)
                if (element.isStrikethrough) add(TextDecoration.LineThrough)
            }
            val bgCol = Color(parseColorInt(element.backgroundColorHex, 0))
            val borderCol = Color(parseColorInt(element.borderColorHex, 0))

            CompositionLocalProvider(LocalLayoutDirection provides textDir) {
                Box(
                    contentAlignment = vAlign,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bgCol, RoundedCornerShape(6.dp))
                        .then(
                            if (element.borderWidth > 0f && borderCol.alpha > 0f) {
                                Modifier.border((element.borderWidth * scale).dp.coerceAtLeast(1.dp), borderCol, RoundedCornerShape(6.dp))
                            } else Modifier
                        )
                        .padding((10f * scale).dp.coerceAtLeast(2.dp))
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        if (element.isBlockQuote) {
                            Box(
                                modifier = Modifier
                                    .width((6f * scale).dp.coerceAtLeast(2.dp))
                                    .height((element.height * 0.7f * scale).dp.coerceAtLeast(16.dp))
                                    .background(Color(parseColorInt(element.textColorHex)), RoundedCornerShape(2.dp))
                            )
                            Box(modifier = Modifier.width((8f * scale).dp.coerceAtLeast(3.dp)))
                        }
                        Text(
                            text = formatted,
                            color = Color(parseColorInt(element.textColorHex)),
                            style = TextStyle(
                                fontFamily = SlideFontRegistry.fontFamilyFor(element.fontFamilyId),
                                fontSize = (element.fontSizeSp * scale * 0.85f).coerceAtLeast(5f).sp,
                                lineHeight = (element.fontSizeSp * element.lineHeightMultiplier * scale * 0.85f).coerceAtLeast(6f).sp,
                                letterSpacing = (element.letterSpacing * scale * 0.4f).sp,
                                fontWeight = if (element.isBold) FontWeight.Bold else FontWeight.Normal,
                                fontStyle = if (element.isItalic) FontStyle.Italic else FontStyle.Normal,
                                textDecoration = TextDecoration.combine(decorations),
                                background = Color(parseColorInt(element.highlightColorHex, 0))
                            ),
                            textAlign = align,
                            overflow = TextOverflow.Clip,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        is SlideElement.ImageElement -> {
            val corner = (element.cornerRadius * scale).dp
            val borderCol = Color(parseColorInt(element.borderColorHex, 0))
            val hasCrop = element.cropRect.left > 0f || element.cropRect.top > 0f ||
                element.cropRect.right < 1f || element.cropRect.bottom < 1f
            val imageFile = remember(element.localImagePath) { File(element.localImagePath) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (element.hasShadow) Modifier.shadow(6.dp, RoundedCornerShape(corner)) else Modifier)
                    .clip(RoundedCornerShape(corner))
                    .then(
                        if (element.borderWidth > 0f && borderCol.alpha > 0f) {
                            Modifier.border((element.borderWidth * scale).dp.coerceAtLeast(1.dp), borderCol, RoundedCornerShape(corner))
                        } else Modifier
                    )
            ) {
                if (!imageFile.exists()) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.BrokenImage,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else if (!hasCrop) {
                    AsyncImage(
                        model = imageFile,
                        contentDescription = null,
                        contentScale = if (element.fillCropMode) ContentScale.Crop else ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    val croppedBitmap by produceState<Bitmap?>(
                        initialValue = null,
                        element.localImagePath,
                        element.cropRect
                    ) {
                        value = withContext(Dispatchers.IO) {
                            val raw = SlideBitmapRenderer.decodeBitmapSampled(element.localImagePath, 900, 900)
                            if (raw != null) {
                                val x = (element.cropRect.left * raw.width).toInt().coerceIn(0, raw.width - 1)
                                val y = (element.cropRect.top * raw.height).toInt().coerceIn(0, raw.height - 1)
                                val cw = ((element.cropRect.right - element.cropRect.left) * raw.width).toInt().coerceIn(1, raw.width - x)
                                val ch = ((element.cropRect.bottom - element.cropRect.top) * raw.height).toInt().coerceIn(1, raw.height - y)
                                Bitmap.createBitmap(raw, x, y, cw, ch)
                            } else null
                        }
                    }
                    val bmp = croppedBitmap
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = null,
                            contentScale = if (element.fillCropMode) ContentScale.Crop else ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE2E8F0)))
                    }
                }
            }
        }
        is SlideElement.ShapeElement -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Shape3DAndExtendedView(element = element)
                if (element.labelText.isNotBlank()) {
                    Text(
                        text = element.labelText,
                        color = Color(parseColorInt(element.labelColorHex)),
                        fontSize = (24f * scale * 0.85f).coerceAtLeast(6f).sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
        is SlideElement.TableElement -> {
            val borderCol = Color(parseColorInt(element.borderColorHex))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, borderCol)
            ) {
                for (r in 0 until element.rows) {
                    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        for (c in 0 until element.cols) {
                            val cell = element.cells.firstOrNull { it.row == r && it.col == c }
                            if (cell?.mergedIntoRow != null) continue
                            val weight = (cell?.colSpan ?: 1).toFloat()
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(weight)
                                    .fillMaxHeight()
                                    .background(Color(parseColorInt(cell?.bgColorHex ?: "#FFFFFF")))
                                    .border(0.75.dp, borderCol)
                                    .padding(2.dp)
                            ) {
                                Text(
                                    text = cell?.text.orEmpty(),
                                    color = Color(parseColorInt(cell?.textColorHex ?: "#0F172A")),
                                    fontSize = (element.fontSizeSp * scale * 0.78f).coerceAtLeast(5f).sp,
                                    fontWeight = if (cell?.isBold == true || cell?.isHeader == true) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
        is SlideElement.ChartElement -> {
            NativeSlideChart(element = element, scale = scale)
        }
        is SlideElement.EquationElement -> {
            val readable = remember(element.expression) {
                SlideBitmapRenderer.formatMathExpression(element.expression)
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(parseColorInt(element.bgColorHex)), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                    .padding(6.dp)
            ) {
                Text(
                    text = readable,
                    color = Color(parseColorInt(element.colorHex)),
                    fontSize = (element.fontSizeSp * scale * 0.8f).coerceAtLeast(7f).sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
        is SlideElement.IconElement -> {
            IllustrativeIconArtwork(
                symbolKey = element.symbolKey,
                tintColorHex = element.tintColorHex,
                bgColorHex = element.bgColorHex,
                modifier = Modifier.fillMaxSize()
            )
        }
        is SlideElement.SmartArtElement -> {
            SmartArtElementView(element = element, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun NativeSlideChart(element: SlideElement.ChartElement, scale: Float) {
    val bgColor = Color(parseColorInt(element.bgColorHex, 0xFFF8FAFC.toInt()))
    val primarySeriesColor = Color(parseColorInt(element.series.firstOrNull()?.colorHex ?: "#1565C0"))
    val slicePalette = remember(element.series) {
        val base = element.series.map { Color(parseColorInt(it.colorHex)) }
        (base + listOf(
            Color(0xFF1565C0),
            Color(0xFF00897B),
            Color(0xFFF59E0B),
            Color(0xFF7C3AED),
            Color(0xFFDC2626),
            Color(0xFF0284C7)
        )).distinct()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor, RoundedCornerShape((10f * scale).dp.coerceAtLeast(4.dp)))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape((10f * scale).dp.coerceAtLeast(4.dp)))
            .padding((10f * scale).dp.coerceAtLeast(4.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = element.title,
                color = Color(0xFF0F172A),
                fontSize = (22f * scale).coerceAtLeast(7f).sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (element.series.isNotEmpty() && scale >= 0.25f) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    element.series.take(3).forEach { s ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Box(
                                modifier = Modifier
                                    .size((8f * scale).dp.coerceAtLeast(4.dp))
                                    .clip(CircleShape)
                                    .background(Color(parseColorInt(s.colorHex)))
                            )
                            Text(
                                text = s.name,
                                color = Color(0xFF475569),
                                fontSize = (12f * scale).coerceAtLeast(5f).sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height((6f * scale).dp.coerceAtLeast(2.dp)))

        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val seriesList = element.series.ifEmpty { return@Canvas }
            val maxVal = seriesList.flatMap { it.values }.maxOrNull()?.coerceAtLeast(1f) ?: 100f
            val chartW = size.width
            val chartH = size.height
            if (chartW <= 4f || chartH <= 4f) return@Canvas

            when (element.chartType) {
                ChartType.PIE -> {
                    val values = seriesList.first().values.ifEmpty { listOf(40f, 35f, 25f) }
                    val total = values.sum().coerceAtLeast(1f)
                    val diameter = min(chartW, chartH) * 0.84f
                    val topLeft = Offset((chartW - diameter) / 2f, (chartH - diameter) / 2f)
                    var startAngle = -90f
                    values.forEachIndexed { idx, v ->
                        val sweep = (v / total) * 360f
                        drawArc(
                            color = slicePalette[idx % slicePalette.size],
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = true,
                            topLeft = topLeft,
                            size = Size(diameter, diameter)
                        )
                        startAngle += sweep
                    }
                    // Donut center cut for modern clarity
                    val holeDiameter = diameter * 0.42f
                    drawCircle(
                        color = bgColor,
                        radius = holeDiameter / 2f,
                        center = Offset(chartW / 2f, chartH / 2f)
                    )
                }

                ChartType.BAR, ChartType.COMPARISON -> {
                    val gridSteps = 3
                    val gridColor = Color(0xFFCBD5E1)
                    for (g in 0..gridSteps) {
                        val gy = chartH - (chartH * g / gridSteps.toFloat())
                        drawLine(gridColor, Offset(0f, gy), Offset(chartW, gy), strokeWidth = 1f)
                    }
                    val count = maxOf(element.labels.size, seriesList.maxOf { it.values.size }).coerceAtLeast(1)
                    val seriesCount = seriesList.size.coerceAtLeast(1)
                    val groupWidth = chartW / count
                    val barW = (groupWidth * 0.68f) / seriesCount
                    val groupStartOffset = (groupWidth - barW * seriesCount) / 2f

                    for (i in 0 until count) {
                        seriesList.forEachIndexed { sIdx, s ->
                            val v = s.values.getOrNull(i) ?: 0f
                            val barH = ((v / maxVal).coerceIn(0f, 1f)) * (chartH * 0.92f)
                            val x = i * groupWidth + groupStartOffset + sIdx * barW
                            val y = chartH - barH
                            drawRoundRect(
                                color = Color(parseColorInt(s.colorHex)),
                                topLeft = Offset(x + 1f, y),
                                size = Size((barW - 2f).coerceAtLeast(2f), barH.coerceAtLeast(1f)),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                        }
                    }
                }

                ChartType.LINE, ChartType.AREA -> {
                    val gridColor = Color(0xFFCBD5E1)
                    for (g in 0..3) {
                        val gy = chartH - (chartH * g / 3f)
                        drawLine(gridColor, Offset(0f, gy), Offset(chartW, gy), strokeWidth = 1f)
                    }
                    seriesList.forEach { s ->
                        val vals = s.values
                        if (vals.isEmpty()) return@forEach
                        val stepX = if (vals.size > 1) chartW / (vals.size - 1) else chartW
                        val sColor = Color(parseColorInt(s.colorHex))
                        val linePath = Path()
                        val areaPath = Path()
                        vals.forEachIndexed { i, v ->
                            val px = if (vals.size == 1) chartW / 2f else i * stepX
                            val py = chartH - ((v / maxVal).coerceIn(0f, 1f) * chartH * 0.88f)
                            if (i == 0) {
                                linePath.moveTo(px, py)
                                areaPath.moveTo(px, chartH)
                                areaPath.lineTo(px, py)
                            } else {
                                linePath.lineTo(px, py)
                                areaPath.lineTo(px, py)
                            }
                        }
                        if (element.chartType == ChartType.AREA && vals.size > 1) {
                            areaPath.lineTo((vals.size - 1) * stepX, chartH)
                            areaPath.close()
                            drawPath(
                                path = areaPath,
                                brush = Brush.verticalGradient(
                                    listOf(sColor.copy(alpha = 0.34f), sColor.copy(alpha = 0.04f))
                                )
                            )
                        }
                        drawPath(
                            path = linePath,
                            color = sColor,
                            style = Stroke(width = (4f * scale).coerceAtLeast(2f), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                        vals.forEachIndexed { i, v ->
                            val px = if (vals.size == 1) chartW / 2f else i * stepX
                            val py = chartH - ((v / maxVal).coerceIn(0f, 1f) * chartH * 0.88f)
                            drawCircle(color = Color.White, radius = (5f * scale).coerceAtLeast(2.5f), center = Offset(px, py))
                            drawCircle(color = sColor, radius = (3.5f * scale).coerceAtLeast(1.8f), center = Offset(px, py))
                        }
                    }
                }
            }
        }

        if (element.chartType != ChartType.PIE && element.labels.isNotEmpty() && scale >= 0.2f) {
            Spacer(Modifier.height(2.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                element.labels.take(6).forEach { lbl ->
                    Text(
                        text = lbl,
                        color = primarySeriesColor.copy(alpha = 0.85f),
                        fontSize = (13f * scale).coerceAtLeast(5f).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
