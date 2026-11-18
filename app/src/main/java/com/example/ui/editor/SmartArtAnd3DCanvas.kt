package com.example.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import com.example.engine.SlideElement
import com.example.engines.SmartArtAndOrnamentRenderer

@Composable
fun OrnamentDecorationLayer(
    decorationId: String,
    decorationColorHex: String,
    modifier: Modifier = Modifier
) {
    if (decorationId == "none" || decorationId.isBlank()) return
    Canvas(modifier = modifier.fillMaxSize()) {
        if (size.width <= 4f || size.height <= 4f) return@Canvas
        SmartArtAndOrnamentRenderer.drawSlideDecorationCanvas(
            canvas = drawContext.canvas.nativeCanvas,
            decorationId = decorationId,
            colorHex = decorationColorHex,
            w = size.width,
            h = size.height
        )
    }
}

@Composable
fun Shape3DAndExtendedView(
    element: SlideElement.ShapeElement,
    fallbackFontId: String = "cairo",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Canvas(modifier = modifier.fillMaxSize()) {
        val elW = element.width.coerceAtLeast(1f)
        val elH = element.height.coerceAtLeast(1f)
        val sx = size.width / elW
        val sy = size.height / elH
        val nc = drawContext.canvas.nativeCanvas
        nc.save()
        nc.scale(sx, sy)
        nc.translate(-element.x, -element.y)
        SmartArtAndOrnamentRenderer.drawExtendedShapeCanvas(
            context = context,
            el = element,
            canvas = nc,
            alpha = (element.opacity.coerceIn(0f, 1f) * 255).toInt(),
            fallbackFontId = fallbackFontId
        )
        nc.restore()
    }
}

@Composable
fun SmartArtElementView(
    element: SlideElement.SmartArtElement,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Canvas(modifier = modifier.fillMaxSize()) {
        val elW = element.width.coerceAtLeast(1f)
        val elH = element.height.coerceAtLeast(1f)
        val sx = size.width / elW
        val sy = size.height / elH
        val nc = drawContext.canvas.nativeCanvas
        nc.save()
        nc.scale(sx, sy)
        nc.translate(-element.x, -element.y)
        SmartArtAndOrnamentRenderer.drawSmartArtElementCanvas(
            context = context,
            el = element,
            canvas = nc,
            alpha = (element.opacity.coerceIn(0f, 1f) * 255).toInt()
        )
        nc.restore()
    }
}
