package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FlipToBack
import androidx.compose.material.icons.rounded.FlipToFront
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material.icons.rounded.FormatAlignCenter
import androidx.compose.material.icons.rounded.GroupWork
import androidx.compose.material.icons.rounded.LayersClear
import androidx.compose.material.icons.rounded.RotateLeft
import androidx.compose.material.icons.rounded.RotateRight
import androidx.compose.material.icons.rounded.Tune
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.LocalAppLanguage
import com.example.core.localization.LocalAppStrings
import com.example.engine.AnimationDirection
import com.example.engine.ChartType
import com.example.engine.CropRect
import com.example.engine.ElementAnimation
import com.example.engine.ElementAnimationType
import com.example.engine.IconCategory
import com.example.engine.ShapeType
import com.example.engine.SlideBackground
import com.example.engine.SlideElement
import com.example.engine.SlideIconCatalog
import com.example.engine.SlideTransition
import com.example.engine.SlideTransitionType
import com.example.engine.parseColorInt
import com.example.engines.SlideBitmapRenderer

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ShapePickerSheet(
    onDismiss: () -> Unit,
    onSelectShape: (ShapeType, String) -> Unit
) {
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    var fillHex by remember { mutableStateOf("#1565C0") }
    val palette = listOf("#1565C0", "#00897B", "#F59E0B", "#DC2626", "#7C3AED", "#0F172A", "#FFFFFF")

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(strings.toolShapes, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(palette) { hex ->
                    ColorSwatch(hex = hex, selected = fillHex == hex, onClick = { fillHex = hex })
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ShapeType.entries.forEach { shape ->
                    OutlinedButton(
                        onClick = { onSelectShape(shape, fillHex); onDismiss() },
                        modifier = Modifier.testTag("shape_pick_${shape.name}")
                    ) {
                        Text(shape.label(lang))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableCreateSheet(
    onDismiss: () -> Unit,
    onCreateTable: (rows: Int, cols: Int) -> Unit
) {
    val strings = LocalAppStrings.current
    var rows by remember { mutableIntStateOf(3) }
    var cols by remember { mutableIntStateOf(3) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(strings.toolTable, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("${strings.rowsLabel}: $rows • ${strings.colsLabel}: $cols", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { rows = (rows - 1).coerceAtLeast(1) }) { Text(strings.deleteRow) }
                OutlinedButton(onClick = { rows = (rows + 1).coerceAtMost(8) }) { Text(strings.addRow) }
                OutlinedButton(onClick = { cols = (cols - 1).coerceAtLeast(1) }) { Text(strings.deleteCol) }
                OutlinedButton(onClick = { cols = (cols + 1).coerceAtMost(6) }) { Text(strings.addCol) }
            }
            Button(
                onClick = { onCreateTable(rows, cols); onDismiss() },
                modifier = Modifier.fillMaxWidth().testTag("confirm_create_table_btn")
            ) {
                Text(strings.confirm)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TableEditSheet(
    element: SlideElement.TableElement,
    onDismiss: () -> Unit,
    onUpdateTable: (SlideElement.TableElement) -> Unit,
    onAddRow: () -> Unit,
    onRemoveRow: () -> Unit,
    onAddCol: () -> Unit,
    onRemoveCol: () -> Unit,
    onMergeRow: (Int) -> Unit
) {
    val strings = LocalAppStrings.current
    var selectedRow by remember(element.id) { mutableIntStateOf(0) }
    var selectedCol by remember(element.id) { mutableIntStateOf(0) }
    val activeCell = element.cells.firstOrNull { it.row == selectedRow && it.col == selectedCol }
    var cellText by remember(element.id, selectedRow, selectedCol) { mutableStateOf(activeCell?.text.orEmpty()) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(strings.toolTable, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onAddRow) { Text(strings.addRow) }
                OutlinedButton(onClick = onRemoveRow) { Text(strings.deleteRow) }
                OutlinedButton(onClick = onAddCol) { Text(strings.addCol) }
                OutlinedButton(onClick = onRemoveCol) { Text(strings.deleteCol) }
                OutlinedButton(onClick = { onMergeRow(selectedRow) }) { Text(strings.mergeCells) }
            }
            Text("${strings.cellLabel} (${selectedRow + 1}, ${selectedCol + 1})", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick = { selectedRow = (selectedRow + 1) % element.rows }) { Text(strings.nextRow) }
                OutlinedButton(onClick = { selectedCol = (selectedCol + 1) % element.cols }) { Text(strings.nextCol) }
            }
            OutlinedTextField(
                value = cellText,
                onValueChange = { txt ->
                    cellText = txt
                    val updated = element.cells.map { c ->
                        if (c.row == selectedRow && c.col == selectedCol) c.copy(text = txt) else c
                    }
                    onUpdateTable(element.copy(cells = updated))
                },
                label = { Text(strings.cellTextLabel) },
                modifier = Modifier.fillMaxWidth()
            )
            val cellBgs = listOf("#0D47A1", "#00796B", "#FFFFFF", "#F8FAFC", "#FEF3C7", "#DBEAFE")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(cellBgs) { hex ->
                    ColorSwatch(
                        hex = hex,
                        selected = activeCell?.bgColorHex == hex,
                        onClick = {
                            val textCol = if (hex == "#0D47A1" || hex == "#00796B") "#FFFFFF" else "#0F172A"
                            val updated = element.cells.map { c ->
                                if (c.row == selectedRow && c.col == selectedCol) {
                                    c.copy(bgColorHex = hex, textColorHex = textCol)
                                } else c
                            }
                            onUpdateTable(element.copy(cells = updated))
                        }
                    )
                }
            }
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text(strings.done) }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChartEditorSheet(
    element: SlideElement.ChartElement?,
    onDismiss: () -> Unit,
    onCreateOrUpdate: (ChartType, String, List<String>, List<Float>) -> Unit
) {
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    var type by remember { mutableStateOf(element?.chartType ?: ChartType.BAR) }
    var title by remember { mutableStateOf(element?.title ?: strings.toolChart) }
    var labelsStr by remember { mutableStateOf(element?.labels?.joinToString(", ") ?: "Q1, Q2, Q3, Q4") }
    var valuesStr by remember {
        mutableStateOf(element?.series?.firstOrNull()?.values?.joinToString(", ") { it.toInt().toString() } ?: "42, 68, 55, 89")
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(strings.editChartData, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChartType.entries.forEach { ct ->
                    FilterChip(
                        selected = type == ct,
                        onClick = { type = ct },
                        label = { Text(ct.label(lang)) }
                    )
                }
            }
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(strings.deckTitleLabel) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = labelsStr,
                onValueChange = { labelsStr = it },
                label = { Text(strings.chartLabelsLabel) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = valuesStr,
                onValueChange = { valuesStr = it },
                label = { Text(strings.chartValuesLabel) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    val labels = labelsStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }.ifEmpty { listOf("A", "B", "C") }
                    val vals = valuesStr.split(",").mapNotNull { it.trim().toFloatOrNull() }.ifEmpty { listOf(40f, 70f, 90f) }
                    onCreateOrUpdate(type, title, labels, vals)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().testTag("confirm_chart_btn")
            ) {
                Text(strings.done)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EquationEditorSheet(
    initialExpression: String = "\\frac{-b \\pm \\sqrt{b^2 - 4ac}}{2a}",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val strings = LocalAppStrings.current
    var expr by remember { mutableStateOf(initialExpression) }
    val snippets = listOf(
        "\\frac{a}{b}", "\\sqrt{x}", "\\int_{0}^{\\infty}", "\\sum_{i=1}^{n}",
        "x^{2}", "H_{2}O", "\\rightarrow", "\\pm", "\\alpha", "\\beta", "\\theta", "\\pi", "\\Delta"
    )

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(strings.toolEquation, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp)
            ) {
                Text(
                    text = SlideBitmapRenderer.formatMathExpression(expr),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            OutlinedTextField(
                value = expr,
                onValueChange = { expr = it },
                placeholder = { Text(strings.equationHint) },
                modifier = Modifier.fillMaxWidth().testTag("equation_input")
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                snippets.forEach { snip ->
                    OutlinedButton(onClick = { expr += " $snip" }) {
                        Text(SlideBitmapRenderer.formatMathExpression(snip))
                    }
                }
            }
            Button(
                onClick = { onConfirm(expr); onDismiss() },
                modifier = Modifier.fillMaxWidth().testTag("confirm_equation_btn")
            ) {
                Text(strings.done)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconsLibrarySheet(
    onDismiss: () -> Unit,
    onSelectIcon: (String) -> Unit
) {
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    var selectedCategory by remember { mutableStateOf<IconCategory?>(null) }
    val filteredIcons = remember(selectedCategory) {
        if (selectedCategory == null) SlideIconCatalog.icons
        else SlideIconCatalog.icons.filter { it.category == selectedCategory }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "${strings.toolIcons} (${SlideIconCatalog.icons.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = strings.iconsUsageSubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text(strings.allLabel) }
                    )
                }
                items(IconCategory.entries) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                        label = { Text(cat.label(lang)) }
                    )
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 148.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                items(filteredIcons, key = { it.key }) { spec ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectIcon(spec.key)
                                onDismiss()
                            }
                            .testTag("pick_icon_${spec.key}")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Box(modifier = Modifier.size(62.dp)) {
                                IllustrativeIconArtwork(
                                    symbolKey = spec.key,
                                    tintColorHex = "#1565C0",
                                    bgColorHex = "#EFF6FF",
                                    modifier = Modifier.size(62.dp)
                                )
                            }
                            Text(
                                text = spec.label(lang),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = spec.usageHint(lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundPickerSheet(
    onDismiss: () -> Unit,
    onSelectBackground: (SlideBackground) -> Unit,
    onPickImageBackground: () -> Unit,
    onApplyBackgroundToAll: (() -> Unit)? = null
) {
    val strings = LocalAppStrings.current
    val solids = listOf("#FFFFFF", "#F8FAFC", "#0F172A", "#0D47A1", "#064E3B", "#FAF5EE", "#FEF3C7")
    val gradients = listOf(
        "#0D47A1" to "#1976D2",
        "#064E3B" to "#0D9488",
        "#1E1B4B" to "#4338CA",
        "#7C2D12" to "#D97706",
        "#0F172A" to "#334155"
    )

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(strings.selectBackground, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(solids) { hex ->
                    ColorSwatch(hex = hex, selected = false, onClick = {
                        onSelectBackground(SlideBackground.Solid(hex)); onDismiss()
                    })
                }
            }
            Color100PaletteGrid(
                selectedHex = "",
                onSelectColor = { hex ->
                    onSelectBackground(SlideBackground.Solid(hex))
                    onDismiss()
                },
                testTagPrefix = "bg_c100"
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(gradients) { (s, e) ->
                    Box(
                        modifier = Modifier
                            .size(56.dp, 38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.linearGradient(listOf(Color(parseColorInt(s)), Color(parseColorInt(e)))))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                            .clickable {
                                onSelectBackground(SlideBackground.Gradient(s, e, 135f)); onDismiss()
                            }
                    )
                }
            }
            OutlinedButton(
                onClick = { onDismiss(); onPickImageBackground() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(strings.toolImage)
            }
            if (onApplyBackgroundToAll != null) {
                OutlinedButton(
                    onClick = { onApplyBackgroundToAll(); onDismiss() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.applyBgAllSlides)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MotionAndTransitionsSheet(
    currentTransition: SlideTransition,
    selectedElement: SlideElement?,
    onDismiss: () -> Unit,
    onUpdateTransition: (SlideTransition) -> Unit,
    onUpdateElementAnimation: (ElementAnimation) -> Unit,
    onApplyTransitionToAll: ((SlideTransition) -> Unit)? = null
) {
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    var transition by remember { mutableStateOf(currentTransition) }
    var anim by remember { mutableStateOf(selectedElement?.animation ?: ElementAnimation()) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(strings.slideTransitionLabel, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SlideTransitionType.entries.forEach { t ->
                    FilterChip(
                        selected = transition.type == t,
                        onClick = { transition = transition.copy(type = t) },
                        label = { Text(t.label(lang)) }
                    )
                }
            }
            Text("${strings.durationMsLabel}: ${transition.durationMs}ms")
            Slider(
                value = transition.durationMs.toFloat(),
                onValueChange = { transition = transition.copy(durationMs = it.toInt()) },
                valueRange = 150f..1500f
            )

            if (selectedElement != null) {
                HorizontalDivider()
                Text(strings.elementAnimationLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ElementAnimationType.entries.forEach { a ->
                        FilterChip(
                            selected = anim.type == a,
                            onClick = { anim = anim.copy(type = a) },
                            label = { Text(a.label(lang)) }
                        )
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AnimationDirection.entries.forEach { d ->
                        FilterChip(
                            selected = anim.direction == d,
                            onClick = { anim = anim.copy(direction = d) },
                            label = { Text(d.label(lang)) }
                        )
                    }
                }
            }

            if (onApplyTransitionToAll != null) {
                OutlinedButton(
                    onClick = {
                        onApplyTransitionToAll(transition)
                        if (selectedElement != null) onUpdateElementAnimation(anim)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.applyTransitionAllSlides)
                }
            }

            Button(
                onClick = {
                    onUpdateTransition(transition)
                    if (selectedElement != null) onUpdateElementAnimation(anim)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(strings.done)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakerNotesSheet(
    initialNotes: String,
    onDismiss: () -> Unit,
    onSaveNotes: (String) -> Unit,
    onGenerateWithAi: () -> Unit
) {
    val strings = LocalAppStrings.current
    var notes by remember { mutableStateOf(initialNotes) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(strings.toolNotes, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text(strings.speakerNotesHint) },
                minLines = 4,
                maxLines = 8,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedButton(
                onClick = { onSaveNotes(notes); onGenerateWithAi(); onDismiss() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.AutoAwesome, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(strings.aiSpeakerNotes)
            }
            Button(
                onClick = { onSaveNotes(notes); onDismiss() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(strings.done)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun ElementPropertiesBar(
    element: SlideElement,
    selectedCount: Int,
    canPaste: Boolean,
    onEditSpecific: () -> Unit,
    onOpenInspector: () -> Unit,
    onUpdateOpacity: (Float) -> Unit,
    onUpdateImageCrop: (CropRect) -> Unit,
    onToggleShadow: () -> Unit,
    onBringFront: () -> Unit,
    onSendBack: () -> Unit,
    onDuplicate: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    onGroup: () -> Unit,
    onUngroup: () -> Unit,
    onAlignCenterH: () -> Unit,
    onDelete: () -> Unit,
    onOpenFontPicker: (() -> Unit)? = null,
    onNudge: ((Float, Float) -> Unit)? = null,
    onResize: ((Float, Float) -> Unit)? = null,
    onRotate: ((Float) -> Unit)? = null
) {
    val strings = LocalAppStrings.current
    var showOpacity by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                Button(onClick = onEditSpecific, modifier = Modifier.testTag("prop_edit_btn")) {
                    Text(
                        when (element) {
                            is SlideElement.TextElement -> strings.textEditorTitle
                            is SlideElement.TableElement -> strings.toolTable
                            is SlideElement.ChartElement -> strings.editChartData
                            is SlideElement.EquationElement -> strings.toolEquation
                            is SlideElement.SmartArtElement -> strings.smartArtEditorTitle
                            else -> strings.propertiesTitle
                        }
                    )
                }
            }
            if (element is SlideElement.TextElement && onOpenFontPicker != null) {
                item {
                    OutlinedButton(
                        onClick = onOpenFontPicker,
                        modifier = Modifier.testTag("prop_font_btn")
                    ) {
                        Icon(Icons.Rounded.FontDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(strings.quickFontTool)
                    }
                }
            }
            item {
                OutlinedButton(
                    onClick = onOpenInspector,
                    modifier = Modifier.testTag("prop_inspector_btn")
                ) {
                    Icon(Icons.Rounded.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(strings.elementInspector)
                }
            }
            if (onNudge != null) {
                item {
                    IconButton(onClick = { onNudge(-20f, 0f) }, modifier = Modifier.testTag("prop_nudge_left")) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, strings.nudgeLeft)
                    }
                }
                item {
                    IconButton(onClick = { onNudge(0f, -20f) }, modifier = Modifier.testTag("prop_nudge_up")) {
                        Icon(Icons.Rounded.ArrowUpward, strings.nudgeUp)
                    }
                }
                item {
                    IconButton(onClick = { onNudge(0f, 20f) }, modifier = Modifier.testTag("prop_nudge_down")) {
                        Icon(Icons.Rounded.ArrowDownward, strings.nudgeDown)
                    }
                }
                item {
                    IconButton(onClick = { onNudge(20f, 0f) }, modifier = Modifier.testTag("prop_nudge_right")) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, strings.nudgeRight)
                    }
                }
            }
            if (onResize != null) {
                item {
                    OutlinedButton(onClick = { onResize(30f, 0f) }) { Text(strings.widenLabel) }
                }
                item {
                    OutlinedButton(onClick = { onResize(-30f, 0f) }) { Text(strings.narrowLabel) }
                }
                item {
                    OutlinedButton(onClick = { onResize(0f, 30f) }) { Text(strings.heightPlusLabel) }
                }
                item {
                    OutlinedButton(onClick = { onResize(0f, -30f) }) { Text(strings.heightMinusLabel) }
                }
            }
            if (onRotate != null) {
                item {
                    IconButton(onClick = { onRotate(-15f) }) {
                        Icon(Icons.Rounded.RotateLeft, strings.rotateLeft15)
                    }
                }
                item {
                    IconButton(onClick = { onRotate(15f) }) {
                        Icon(Icons.Rounded.RotateRight, strings.rotateRight15)
                    }
                }
            }
            if (element is SlideElement.ImageElement) {
                item {
                    OutlinedButton(onClick = {
                        val nextCrop = if (element.cropRect.left == 0f) CropRect(0.1f, 0.1f, 0.9f, 0.9f) else CropRect()
                        onUpdateImageCrop(nextCrop)
                    }) { Text(strings.cropLabel) }
                }
            }
            item {
                OutlinedButton(onClick = { showOpacity = !showOpacity }) {
                    Text("${strings.opacityLabel} ${(element.opacity * 100).toInt()}%")
                }
            }
            item {
                OutlinedButton(onClick = onToggleShadow) { Text(strings.shadowLabel) }
            }
            item {
                IconButton(onClick = onBringFront) { Icon(Icons.Rounded.FlipToFront, strings.bringFront) }
            }
            item {
                IconButton(onClick = onSendBack) { Icon(Icons.Rounded.FlipToBack, strings.sendBack) }
            }
            item {
                IconButton(onClick = onAlignCenterH) { Icon(Icons.Rounded.FormatAlignCenter, strings.alignElements) }
            }
            item {
                IconButton(onClick = onCopy) { Icon(Icons.Rounded.ContentCopy, strings.copy) }
            }
            if (canPaste) {
                item { OutlinedButton(onClick = onPaste) { Text(strings.paste) } }
            }
            item {
                OutlinedButton(onClick = onDuplicate) { Text(strings.duplicate) }
            }
            if (selectedCount >= 2) {
                item {
                    IconButton(onClick = onGroup) { Icon(Icons.Rounded.GroupWork, strings.group) }
                }
            }
            if (element.groupId != null) {
                item {
                    IconButton(onClick = onUngroup) { Icon(Icons.Rounded.LayersClear, strings.ungroup) }
                }
            }
            item {
                IconButton(onClick = onDelete, modifier = Modifier.testTag("prop_delete_btn")) {
                    Icon(Icons.Rounded.DeleteOutline, strings.delete, tint = MaterialTheme.colorScheme.error)
                }
            }
        }
        if (showOpacity) {
            Slider(
                value = element.opacity,
                onValueChange = onUpdateOpacity,
                valueRange = 0.1f..1f,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}
