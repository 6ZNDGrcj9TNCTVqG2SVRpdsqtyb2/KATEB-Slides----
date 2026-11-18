package com.example.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlignHorizontalCenter
import androidx.compose.material.icons.rounded.AlignHorizontalLeft
import androidx.compose.material.icons.rounded.AlignHorizontalRight
import androidx.compose.material.icons.rounded.AlignVerticalBottom
import androidx.compose.material.icons.rounded.AlignVerticalCenter
import androidx.compose.material.icons.rounded.AlignVerticalTop
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.LocalAppLanguage
import com.example.core.localization.LocalAppStrings
import com.example.data.local.ImportedFontSpec
import com.example.data.templates.SlideLayoutPreset
import com.example.engine.ElementAlignment
import com.example.engine.MAX_SLIDES_PER_DECK
import com.example.engine.Presentation
import com.example.engine.Slide
import com.example.engine.SlideAspectRatio
import com.example.engine.SlideBackground
import com.example.engine.SlideElement
import com.example.engine.parseColorInt
import com.example.ui.theme.SlideFontRegistry
import kotlin.math.roundToInt

/**
 * Ultra-lightweight proportional preview used in thumbnail rails and slide overview grids
 * so dragging or zooming the main slide canvas never triggers heavy thumbnail recompositions.
 */
@Composable
fun SlideMiniPreview(
    presentation: Presentation,
    slide: Slide,
    modifier: Modifier = Modifier
) {
    val bgModifier = when (val bg = slide.background) {
        is SlideBackground.Solid -> Modifier.background(Color(parseColorInt(bg.colorHex, 0xFFFFFFFF.toInt())))
        is SlideBackground.Gradient -> Modifier.background(
            Brush.linearGradient(
                listOf(
                    Color(parseColorInt(bg.startColorHex, 0xFF0D47A1.toInt())),
                    Color(parseColorInt(bg.endColorHex, 0xFF1976D2.toInt()))
                )
            )
        )
        is SlideBackground.Image -> Modifier.background(Color(0xFF1E293B))
    }

    Box(modifier = modifier.then(bgModifier)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cw = presentation.canvasWidth.coerceAtLeast(1f)
            val ch = presentation.canvasHeight.coerceAtLeast(1f)
            val sx = size.width / cw
            val sy = size.height / ch
            slide.elements.sortedBy { it.zIndex }.forEach { el ->
                val x = (el.x * sx).coerceIn(0f, size.width)
                val y = (el.y * sy).coerceIn(0f, size.height)
                val w = (el.width * sx).coerceIn(2f, size.width - x)
                val h = (el.height * sy).coerceIn(2f, size.height - y)
                val col = when (el) {
                    is SlideElement.TextElement -> Color(parseColorInt(el.textColorHex)).copy(alpha = 0.55f)
                    is SlideElement.ShapeElement -> Color(parseColorInt(el.fillColorHex))
                    is SlideElement.ImageElement -> Color(0xFF64748B)
                    is SlideElement.TableElement -> Color(0xFF0D47A1).copy(alpha = 0.5f)
                    is SlideElement.ChartElement -> Color(0xFF00897B).copy(alpha = 0.65f)
                    is SlideElement.EquationElement -> Color(0xFF7C3AED).copy(alpha = 0.5f)
                    is SlideElement.IconElement -> Color(parseColorInt(el.tintColorHex))
                    is SlideElement.SmartArtElement -> Color(0xFF0284C7).copy(alpha = 0.7f)
                }
                drawRoundRect(
                    color = col,
                    topLeft = Offset(x, y),
                    size = Size(w, h),
                    cornerRadius = CornerRadius(2f, 2f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlideLayoutPickerSheet(
    onDismiss: () -> Unit,
    onSelectPreset: (SlideLayoutPreset) -> Unit
) {
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(strings.slideLayouts, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 148.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.height(310.dp)
            ) {
                itemsIndexed(SlideLayoutPreset.entries) { _, preset ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectPreset(preset)
                                onDismiss()
                            }
                            .testTag("layout_preset_${preset.id}")
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            LayoutWireframePreview(preset)
                            Text(
                                text = preset.label(lang),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LayoutWireframePreview(preset: SlideLayoutPreset) {
    val barColor = MaterialTheme.colorScheme.primary
    val blockColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (preset == SlideLayoutPreset.TITLE_COVER || preset == SlideLayoutPreset.SECTION_HEADER) {
                    barColor
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        when (preset) {
            SlideLayoutPreset.BLANK -> {}
            SlideLayoutPreset.TITLE_COVER -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(Modifier.width(72.dp).height(10.dp).background(Color.White, RoundedCornerShape(4.dp)))
                    Box(Modifier.width(48.dp).height(6.dp).background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(4.dp)))
                }
            }
            SlideLayoutPreset.TITLE_CONTENT -> {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.fillMaxWidth(0.6f).height(8.dp).background(barColor, RoundedCornerShape(4.dp)))
                    Box(Modifier.fillMaxWidth().weight(1f).background(blockColor, RoundedCornerShape(6.dp)))
                }
            }
            SlideLayoutPreset.TWO_COLUMN -> {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.fillMaxWidth(0.55f).height(8.dp).background(barColor, RoundedCornerShape(4.dp)))
                    Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(Modifier.weight(1f).fillMaxSize().background(blockColor, RoundedCornerShape(6.dp)))
                        Box(Modifier.weight(1f).fillMaxSize().background(blockColor, RoundedCornerShape(6.dp)))
                    }
                }
            }
            SlideLayoutPreset.THREE_CARDS -> {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.fillMaxWidth(0.55f).height(8.dp).background(barColor, RoundedCornerShape(4.dp)))
                    Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(3) {
                            Box(Modifier.weight(1f).fillMaxSize().background(blockColor, RoundedCornerShape(5.dp)))
                        }
                    }
                }
            }
            SlideLayoutPreset.SECTION_HEADER -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(68.dp)
                        .height(10.dp)
                        .background(Color.White, RoundedCornerShape(4.dp))
                )
            }
            SlideLayoutPreset.KPI_METRICS -> {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(Modifier.fillMaxWidth(0.5f).height(7.dp).background(barColor, RoundedCornerShape(3.dp)))
                    Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(3) {
                            Box(Modifier.weight(1f).fillMaxSize().background(blockColor, RoundedCornerShape(4.dp)))
                        }
                    }
                }
            }
            SlideLayoutPreset.TIMELINE_STEPS -> {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(Modifier.fillMaxWidth(0.5f).height(7.dp).background(barColor, RoundedCornerShape(3.dp)))
                    Box(Modifier.fillMaxWidth().height(3.dp).background(barColor.copy(alpha = 0.5f)))
                    Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(4) {
                            Box(Modifier.weight(1f).fillMaxSize().background(blockColor, RoundedCornerShape(3.dp)))
                        }
                    }
                }
            }
            SlideLayoutPreset.CHART_INSIGHT -> {
                Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.weight(1f).fillMaxSize().background(blockColor, RoundedCornerShape(5.dp)))
                    Box(Modifier.weight(1.2f).fillMaxSize().background(barColor.copy(alpha = 0.25f), RoundedCornerShape(5.dp)))
                }
            }
            SlideLayoutPreset.QUOTE_SPOTLIGHT -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(Modifier.width(80.dp).height(8.dp).background(barColor, RoundedCornerShape(4.dp)))
                    Box(Modifier.width(42.dp).height(5.dp).background(blockColor, RoundedCornerShape(3.dp)))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlidesGridOverviewSheet(
    presentation: Presentation,
    selectedSlideIndex: Int,
    onDismiss: () -> Unit,
    onSelectSlide: (Int) -> Unit,
    onDuplicateSlide: (Int) -> Unit,
    onDeleteSlide: (Int) -> Unit,
    onMoveSlide: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> }
) {
    val strings = LocalAppStrings.current
    val ratio = (presentation.canvasWidth / presentation.canvasHeight).coerceIn(0.7f, 2.1f)

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "${strings.slidesOverview} (${presentation.slides.size} / $MAX_SLIDES_PER_DECK)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 156.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.height(360.dp)
            ) {
                itemsIndexed(presentation.slides, key = { _, s -> s.id }) { idx, slide ->
                    val isSelected = idx == selectedSlideIndex
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                onSelectSlide(idx)
                                onDismiss()
                            }
                            .testTag("grid_slide_$idx")
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SlideMiniPreview(
                                presentation = presentation,
                                slide = slide,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(ratio)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "#${idx + 1}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Row {
                                    IconButton(
                                        onClick = { if (idx > 0) onMoveSlide(idx, idx - 1) },
                                        enabled = idx > 0,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Rounded.ArrowBack, contentDescription = strings.moveSlideUp, modifier = Modifier.size(14.dp))
                                    }
                                    IconButton(
                                        onClick = { if (idx < presentation.slides.lastIndex) onMoveSlide(idx, idx + 1) },
                                        enabled = idx < presentation.slides.lastIndex,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Rounded.ArrowForward, contentDescription = strings.moveSlideDown, modifier = Modifier.size(14.dp))
                                    }
                                    IconButton(
                                        onClick = { onDuplicateSlide(idx) },
                                        enabled = presentation.slides.size < MAX_SLIDES_PER_DECK,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Rounded.ContentCopy, contentDescription = strings.duplicateSlide, modifier = Modifier.size(14.dp))
                                    }
                                    IconButton(
                                        onClick = { onDeleteSlide(idx) },
                                        enabled = presentation.slides.size > 1,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.DeleteOutline,
                                            contentDescription = strings.deleteSlide,
                                            tint = if (presentation.slides.size > 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ElementInspectorSheet(
    element: SlideElement,
    onDismiss: () -> Unit,
    onAlign: (ElementAlignment) -> Unit,
    onUpdateElement: (SlideElement) -> Unit
) {
    val strings = LocalAppStrings.current
    val palette = listOf(
        "#1565C0", "#00897B", "#F59E0B", "#DC2626", "#7C3AED",
        "#0F172A", "#FFFFFF", "#DBEAFE", "#FEF3C7", "#00000000"
    )
    var shapeLabel by remember(element.id) {
        mutableStateOf((element as? SlideElement.ShapeElement)?.labelText.orEmpty())
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(strings.elementInspector, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            // 6-Way Alignment Bar
            Text(strings.alignElements, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { onAlign(ElementAlignment.LEFT) }) {
                    Icon(Icons.Rounded.AlignHorizontalLeft, contentDescription = strings.alignLeft)
                }
                IconButton(onClick = { onAlign(ElementAlignment.CENTER_H) }) {
                    Icon(Icons.Rounded.AlignHorizontalCenter, contentDescription = strings.alignCenterH)
                }
                IconButton(onClick = { onAlign(ElementAlignment.RIGHT) }) {
                    Icon(Icons.Rounded.AlignHorizontalRight, contentDescription = strings.alignRight)
                }
                IconButton(onClick = { onAlign(ElementAlignment.TOP) }) {
                    Icon(Icons.Rounded.AlignVerticalTop, contentDescription = strings.alignTop)
                }
                IconButton(onClick = { onAlign(ElementAlignment.CENTER_V) }) {
                    Icon(Icons.Rounded.AlignVerticalCenter, contentDescription = strings.alignCenterV)
                }
                IconButton(onClick = { onAlign(ElementAlignment.BOTTOM) }) {
                    Icon(Icons.Rounded.AlignVerticalBottom, contentDescription = strings.alignBottom)
                }
            }

            // Nudge Position & Size
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick = { onUpdateElement(element.withTransform(x = element.x - 20f)) }) { Text(strings.nudgeLeft) }
                OutlinedButton(onClick = { onUpdateElement(element.withTransform(x = element.x + 20f)) }) { Text(strings.nudgeRight) }
                OutlinedButton(onClick = { onUpdateElement(element.withTransform(y = element.y - 20f)) }) { Text(strings.nudgeUp) }
                OutlinedButton(onClick = { onUpdateElement(element.withTransform(y = element.y + 20f)) }) { Text(strings.nudgeDown) }
                OutlinedButton(onClick = { onUpdateElement(element.withTransform(width = (element.width - 30f).coerceAtLeast(60f))) }) { Text(strings.narrowLabel) }
                OutlinedButton(onClick = { onUpdateElement(element.withTransform(width = element.width + 30f)) }) { Text(strings.widenLabel) }
            }

            // Rotation Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${strings.rotationLabel}: ${element.rotation.roundToInt()}°", style = MaterialTheme.typography.labelLarge)
                TextButton(onClick = { onUpdateElement(element.withTransform(rotation = 0f)) }) {
                    Icon(Icons.Rounded.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(strings.resetRotation)
                }
            }
            Slider(
                value = ((element.rotation % 360f) + 360f) % 360f,
                onValueChange = { deg -> onUpdateElement(element.withTransform(rotation = deg)) },
                valueRange = 0f..360f
            )

            // Opacity
            Text("${strings.opacityLabel}: ${(element.opacity * 100).roundToInt()}%", style = MaterialTheme.typography.labelLarge)
            Slider(
                value = element.opacity,
                onValueChange = { op -> onUpdateElement(element.withTransform(opacity = op)) },
                valueRange = 0.1f..1f
            )

            // Shape-specific properties (3D Depth + 100 Colors)
            if (element is SlideElement.ShapeElement) {
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(strings.enable3DLabel, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = element.is3D,
                        onCheckedChange = { onUpdateElement(element.copy(is3D = it)) },
                        modifier = Modifier.testTag("inspector_3d_switch")
                    )
                }
                if (element.is3D) {
                    Text("${strings.depth3DLabel}: ${element.depth3D.roundToInt()}", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = element.depth3D,
                        onValueChange = { onUpdateElement(element.copy(depth3D = it)) },
                        valueRange = 8f..64f
                    )
                }
                Text(strings.fillLabel, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(palette) { hex ->
                        ColorSwatch(
                            hex = hex,
                            selected = element.fillColorHex.equals(hex, ignoreCase = true),
                            onClick = { onUpdateElement(element.copy(fillColorHex = hex)) }
                        )
                    }
                }
                Color100PaletteGrid(
                    selectedHex = element.fillColorHex,
                    onSelectColor = { onUpdateElement(element.copy(fillColorHex = it)) },
                    testTagPrefix = "inspector_c100"
                )
                Text(strings.borderLabel, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(palette) { hex ->
                        ColorSwatch(
                            hex = hex,
                            selected = element.strokeColorHex.equals(hex, ignoreCase = true),
                            onClick = { onUpdateElement(element.copy(strokeColorHex = hex)) }
                        )
                    }
                }
                OutlinedTextField(
                    value = shapeLabel,
                    onValueChange = { txt ->
                        shapeLabel = txt
                        val txtCol = if (element.fillColorHex == "#FFFFFF" || element.fillColorHex == "#DBEAFE" || element.fillColorHex == "#FEF3C7") "#0F172A" else "#FFFFFF"
                        onUpdateElement(element.copy(labelText = txt, labelColorHex = txtCol))
                    },
                    label = { Text(strings.shapeLabelInput) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // SmartArt quick 3D toggle
            if (element is SlideElement.SmartArtElement) {
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(strings.enable3DLabel, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = element.is3D,
                        onCheckedChange = { onUpdateElement(element.copy(is3D = it)) },
                        modifier = Modifier.testTag("inspector_smartart_3d_switch")
                    )
                }
            }

            // Icon-specific properties
            if (element is SlideElement.IconElement) {
                HorizontalDivider()
                Text(strings.textColor, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(palette.filter { it != "#00000000" }) { hex ->
                        ColorSwatch(
                            hex = hex,
                            selected = element.tintColorHex.equals(hex, ignoreCase = true),
                            onClick = { onUpdateElement(element.copy(tintColorHex = hex)) }
                        )
                    }
                }
                Text(strings.fillLabel, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(palette) { hex ->
                        ColorSwatch(
                            hex = hex,
                            selected = element.bgColorHex.equals(hex, ignoreCase = true),
                            onClick = { onUpdateElement(element.copy(bgColorHex = hex)) }
                        )
                    }
                }
            }

            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(strings.done)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SlideSettingsSheet(
    presentation: Presentation,
    activeSlideIndex: Int,
    activeSlide: Slide,
    isCanvasLocked: Boolean,
    snapToGrid: Boolean,
    showGrid: Boolean,
    showSafeMargins: Boolean,
    canvasZoom: Float,
    onDismiss: () -> Unit,
    onToggleCanvasLocked: (Boolean) -> Unit,
    onToggleSnapToGrid: (Boolean) -> Unit,
    onToggleShowGrid: (Boolean) -> Unit,
    onToggleShowSafeMargins: (Boolean) -> Unit,
    onZoomChange: (Float) -> Unit,
    onMoveSlideToPosition: (Int) -> Unit,
    onUpdateAutoAdvanceSeconds: (Int) -> Unit,
    onUpdateAspectRatio: (SlideAspectRatio, Float?, Float?) -> Unit,
    onApplyBackgroundToAll: () -> Unit,
    onApplyTransitionToAll: () -> Unit
) {
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    var targetPos by remember(activeSlideIndex, presentation.slides.size) {
        mutableIntStateOf((activeSlideIndex + 1).coerceIn(1, presentation.slides.size))
    }
    var customW by remember(presentation.canvasWidth) { mutableStateOf(presentation.canvasWidth.roundToInt().toString()) }
    var customH by remember(presentation.canvasHeight) { mutableStateOf(presentation.canvasHeight.roundToInt().toString()) }
    var durationSec by remember(activeSlide.id) { mutableFloatStateOf(activeSlide.autoAdvanceSeconds.toFloat()) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(strings.slideSettings, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            // Stability & Grid Toggles
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(strings.stabilizeScreen, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Text(strings.stabilizeScreenDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isCanvasLocked,
                            onCheckedChange = onToggleCanvasLocked,
                            modifier = Modifier.testTag("settings_stabilize_toggle")
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(strings.snapToGridLabel, style = MaterialTheme.typography.labelLarge)
                        Switch(
                            checked = snapToGrid,
                            onCheckedChange = onToggleSnapToGrid,
                            modifier = Modifier.testTag("settings_snap_grid_toggle")
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(strings.showGridLabel, style = MaterialTheme.typography.labelLarge)
                        Switch(
                            checked = showGrid,
                            onCheckedChange = onToggleShowGrid,
                            modifier = Modifier.testTag("settings_show_grid_toggle")
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(strings.showSafeMarginsLabel, style = MaterialTheme.typography.labelLarge)
                        Switch(
                            checked = showSafeMargins,
                            onCheckedChange = onToggleShowSafeMargins,
                            modifier = Modifier.testTag("settings_safe_margins_toggle")
                        )
                    }
                }
            }

            // Canvas Zoom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${strings.canvasZoomLabel}: ${(canvasZoom * 100).roundToInt()}%", style = MaterialTheme.typography.labelLarge)
                TextButton(onClick = { onZoomChange(1f) }) {
                    Text(strings.resetZoomLabel)
                }
            }
            Slider(
                value = canvasZoom,
                onValueChange = onZoomChange,
                valueRange = 0.65f..2.25f
            )

            HorizontalDivider()

            // Move Active Slide directly to Position #
            Text(strings.moveSlideToPosition, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { if (targetPos > 1) targetPos-- },
                    enabled = targetPos > 1
                ) { Text("-") }
                Text(
                    text = "${strings.targetPositionLabel}: #$targetPos / ${presentation.slides.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick = { if (targetPos < presentation.slides.size) targetPos++ },
                    enabled = targetPos < presentation.slides.size
                ) { Text("+") }
                Button(
                    onClick = { onMoveSlideToPosition(targetPos - 1) },
                    enabled = targetPos - 1 != activeSlideIndex,
                    modifier = Modifier.testTag("move_slide_position_confirm_btn")
                ) {
                    Text(strings.confirm)
                }
            }

            // Slide Duration
            Text("${strings.slideDurationLabel}: ${durationSec.toInt()}s", style = MaterialTheme.typography.labelLarge)
            Slider(
                value = durationSec,
                onValueChange = {
                    durationSec = it
                    onUpdateAutoAdvanceSeconds(it.toInt())
                },
                valueRange = 1f..30f,
                steps = 28
            )

            // Deck Aspect Ratio / Custom Size
            Text(strings.slideSizeLabel, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SlideAspectRatio.entries.forEach { ratio ->
                    FilterChip(
                        selected = presentation.aspectRatio == ratio,
                        onClick = {
                            if (ratio == SlideAspectRatio.CUSTOM) {
                                onUpdateAspectRatio(
                                    ratio,
                                    customW.toFloatOrNull() ?: 1920f,
                                    customH.toFloatOrNull() ?: 1080f
                                )
                            } else {
                                onUpdateAspectRatio(ratio, null, null)
                            }
                        },
                        label = { Text(ratio.label(lang)) }
                    )
                }
            }
            if (presentation.aspectRatio == SlideAspectRatio.CUSTOM) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = customW,
                        onValueChange = { customW = it.filter { ch -> ch.isDigit() } },
                        label = { Text(strings.customSizeWidth) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = customH,
                        onValueChange = { customH = it.filter { ch -> ch.isDigit() } },
                        label = { Text(strings.customSizeHeight) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(onClick = {
                        onUpdateAspectRatio(
                            SlideAspectRatio.CUSTOM,
                            customW.toFloatOrNull() ?: 1920f,
                            customH.toFloatOrNull() ?: 1080f
                        )
                    }) { Text(strings.confirm) }
                }
            }

            HorizontalDivider()

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onApplyBackgroundToAll,
                    modifier = Modifier.weight(1f).testTag("apply_bg_all_btn")
                ) {
                    Text(strings.applyBgAllSlides, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                OutlinedButton(
                    onClick = onApplyTransitionToAll,
                    modifier = Modifier.weight(1f).testTag("apply_tr_all_btn")
                ) {
                    Text(strings.applyTransitionAllSlides, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }

            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(strings.done)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickFontPickerSheet(
    selectedFontId: String?,
    customFonts: List<ImportedFontSpec>,
    onSelectFont: (String) -> Unit,
    onImportCustomFont: () -> Unit,
    onDeleteCustomFont: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .testTag("quick_font_picker_sheet"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(strings.fontLabel, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                OutlinedButton(
                    onClick = onImportCustomFont,
                    modifier = Modifier.testTag("quick_import_font_btn")
                ) {
                    Icon(Icons.Rounded.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.importCustomFont)
                }
            }

            if (customFonts.isNotEmpty()) {
                Text(strings.importedFonts, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    customFonts.forEach { spec ->
                        FilterChip(
                            selected = selectedFontId == spec.id,
                            onClick = { onSelectFont(spec.id); onDismiss() },
                            label = { Text(spec.displayName, fontFamily = SlideFontRegistry.fontFamilyFor(spec.id)) },
                            trailingIcon = {
                                IconButton(onClick = { onDeleteCustomFont(spec.id) }, modifier = Modifier.size(18.dp)) {
                                    Icon(Icons.Rounded.Close, contentDescription = strings.deleteFontAction, modifier = Modifier.size(14.dp))
                                }
                            }
                        )
                    }
                }
            }

            Text(strings.builtinFontsLabel, style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SlideFontRegistry.builtInOptions.forEach { opt ->
                    FilterChip(
                        selected = selectedFontId == opt.id,
                        onClick = { onSelectFont(opt.id); onDismiss() },
                        label = { Text(opt.displayName, fontFamily = opt.composeFamily) }
                    )
                }
            }

            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text(strings.done) }
            Spacer(Modifier.height(16.dp))
        }
    }
}
