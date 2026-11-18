package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.LocalAppLanguage
import com.example.core.localization.LocalAppStrings
import com.example.engine.ColorPaletteGroup
import com.example.engine.ShapeType
import com.example.engine.SlideColorCatalog
import com.example.engine.SlideElement
import com.example.engine.SmartArtCategory
import com.example.engine.SmartArtLayoutType
import com.example.engine.SmartArtNode
import com.example.engine.defaultSmartArtNodes
import com.example.engine.parseColorInt

/**
 * Reusable 100-color categorized swatch grid powered by SlideColorCatalog.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Color100PaletteGrid(
    selectedHex: String,
    onSelectColor: (String) -> Unit,
    testTagPrefix: String = "c100",
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current
    val strings = LocalAppStrings.current
    var activeGroup by remember { mutableStateOf<ColorPaletteGroup?>(null) }

    val visibleColors = remember(activeGroup) {
        if (activeGroup == null) SlideColorCatalog.colors
        else SlideColorCatalog.colors.filter { it.group == activeGroup }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Default.Palette, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Text(
                "${strings.colors100Label} (${visibleColors.size}/${SlideColorCatalog.colors.size})",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                FilterChip(
                    selected = activeGroup == null,
                    onClick = { activeGroup = null },
                    label = { Text(strings.allLabel) }
                )
            }
            items(ColorPaletteGroup.entries) { group ->
                FilterChip(
                    selected = activeGroup == group,
                    onClick = { activeGroup = group },
                    label = { Text(group.label(lang)) }
                )
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            visibleColors.forEach { spec ->
                val isSelected = selectedHex.equals(spec.hex, ignoreCase = true)
                val col = Color(parseColorInt(spec.hex))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(col)
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0x5594A3B8),
                            shape = CircleShape
                        )
                        .clickable { onSelectColor(spec.hex) }
                        .testTag("${testTagPrefix}_${spec.hex}")
                ) {
                    if (isSelected) {
                        val lum = 0.299f * col.red + 0.587f * col.green + 0.114f * col.blue
                        Icon(
                            Icons.Default.Check,
                            contentDescription = spec.nameEn,
                            tint = if (lum > 0.55f) Color.Black else Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SmartArtGallerySheet(
    onDismiss: () -> Unit,
    onSelectSmartArt: (SmartArtLayoutType) -> Unit
) {
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    var selectedCategory by remember { mutableStateOf<SmartArtCategory?>(null) }

    val itemsList = remember(selectedCategory) {
        if (selectedCategory == null) SmartArtLayoutType.entries
        else SmartArtLayoutType.entries.filter { it.category == selectedCategory }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AccountTree, null, tint = MaterialTheme.colorScheme.primary)
                Text(strings.smartArtGalleryTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("${strings.allLabel} (${SmartArtLayoutType.entries.size})") }
                    )
                }
                items(SmartArtCategory.entries) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat.label(lang)) }
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsList.forEach { layout ->
                    val previewEl = remember(layout, lang) {
                        SlideElement.SmartArtElement(
                            width = 480f,
                            height = 240f,
                            layoutType = layout,
                            nodes = defaultSmartArtNodes(layout, lang),
                            is3D = true
                        )
                    }
                    Card(
                        onClick = {
                            onSelectSmartArt(layout)
                            onDismiss()
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)),
                        modifier = Modifier
                            .width(156.dp)
                            .testTag("smartart_pick_${layout.name}")
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(74.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .padding(4.dp)
                            ) {
                                SmartArtElementView(element = previewEl)
                            }
                            Text(
                                text = layout.label(lang),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = layout.category.label(lang),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartArtEditorSheet(
    element: SlideElement.SmartArtElement,
    onDismiss: () -> Unit,
    onSaveElement: (SlideElement.SmartArtElement) -> Unit
) {
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    var draft by remember(element.id) { mutableStateOf(element) }
    var activeNodeIdx by remember(element.id) { mutableIntStateOf(0) }
    val safeIdx = activeNodeIdx.coerceIn(0, (draft.nodes.size - 1).coerceAtLeast(0))
    val activeNode = draft.nodes.getOrNull(safeIdx)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(strings.smartArtEditorTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Button(
                    onClick = {
                        onSaveElement(draft)
                        onDismiss()
                    },
                    modifier = Modifier.testTag("smartart_save_btn")
                ) {
                    Text(strings.done)
                }
            }

            // Live miniature preview reflecting per-node colors & 3D depth immediately
            Surface(
                shape = RoundedCornerShape(14.dp),
                tonalElevation = 2.dp,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                    .padding(8.dp)
            ) {
                SmartArtElementView(element = draft)
            }

            // Switch SmartArt diagram layout preserving user nodes
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(SmartArtLayoutType.entries) { layout ->
                    FilterChip(
                        selected = draft.layoutType == layout,
                        onClick = { draft = draft.copy(layoutType = layout) },
                        label = { Text(layout.label(lang)) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(strings.enable3DLabel, style = MaterialTheme.typography.titleSmall)
                Switch(
                    checked = draft.is3D,
                    onCheckedChange = { draft = draft.copy(is3D = it) },
                    modifier = Modifier.testTag("smartart_3d_switch")
                )
            }

            HorizontalDivider()

            // Node chips to select which specific SmartArt shape to customize
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(draft.nodes.size) { idx ->
                        val node = draft.nodes[idx]
                        FilterChip(
                            selected = idx == safeIdx,
                            onClick = { activeNodeIdx = idx },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color(parseColorInt(node.fillColorHex)))
                                )
                            },
                            label = { Text("${idx + 1}. ${node.text.take(10)}") },
                            modifier = Modifier.testTag("smartart_node_chip_$idx")
                        )
                    }
                }
                Spacer(Modifier.width(6.dp))
                OutlinedButton(
                    onClick = {
                        if (draft.nodes.size < 8) {
                            val newIdx = draft.nodes.size + 1
                            val label = if (lang.isRtl) "عنصر $newIdx" else "Item $newIdx"
                            val nextNodes = draft.nodes + SmartArtNode(text = label, fillColorHex = "#0284C7")
                            draft = draft.copy(nodes = nextNodes)
                            activeNodeIdx = nextNodes.lastIndex
                        }
                    },
                    modifier = Modifier.testTag("smartart_add_node_btn")
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Text(strings.addNodeAction, fontSize = 12.sp)
                }
            }

            if (activeNode != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = activeNode.text,
                        onValueChange = { txt ->
                            val updated = draft.nodes.mapIndexed { idx, n ->
                                if (idx == safeIdx) n.copy(text = txt) else n
                            }
                            draft = draft.copy(nodes = updated)
                        },
                        label = { Text("${strings.cellTextLabel} #${safeIdx + 1}") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("smartart_node_text_input")
                    )
                    if (draft.nodes.size > 2) {
                        OutlinedButton(
                            onClick = {
                                val updated = draft.nodes.filterIndexed { idx, _ -> idx != safeIdx }
                                draft = draft.copy(nodes = updated)
                                activeNodeIdx = safeIdx.coerceAtMost(updated.lastIndex)
                            }
                        ) {
                            Icon(Icons.Default.DeleteOutline, strings.removeNodeAction, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Text(
                    "${strings.smartArtNodeColorLabel} (#${safeIdx + 1})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Color100PaletteGrid(
                    selectedHex = activeNode.fillColorHex,
                    onSelectColor = { hex ->
                        val updated = draft.nodes.mapIndexed { idx, n ->
                            if (idx == safeIdx) n.copy(fillColorHex = hex) else n
                        }
                        draft = draft.copy(nodes = updated)
                    },
                    testTagPrefix = "smartart_node_color"
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ShapesAnd3DPickerSheet(
    onDismiss: () -> Unit,
    onSelectShape: (ShapeType, fillHex: String, is3D: Boolean, depth3D: Float) -> Unit
) {
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    var selectedColorHex by remember { mutableStateOf("#1565C0") }
    var enable3D by remember { mutableStateOf(false) }
    var depth3D by remember { mutableFloatStateOf(24f) }
    var showOnly3D by remember { mutableStateOf(false) }

    val shapes = remember(showOnly3D) {
        if (showOnly3D) ShapeType.entries.filter { it.is3DShape } else ShapeType.entries
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.ViewInAr, null, tint = MaterialTheme.colorScheme.primary)
                Text(strings.shapes3DTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !showOnly3D,
                    onClick = { showOnly3D = false },
                    label = { Text("${strings.allLabel} (${ShapeType.entries.size})") }
                )
                FilterChip(
                    selected = showOnly3D,
                    onClick = {
                        showOnly3D = true
                        enable3D = true
                    },
                    label = { Text("3D (${ShapeType.entries.count { it.is3DShape }})") }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(strings.enable3DLabel, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Switch(
                    checked = enable3D,
                    onCheckedChange = { enable3D = it },
                    modifier = Modifier.testTag("shape_3d_switch")
                )
            }

            if (enable3D) {
                Column {
                    Text("${strings.depth3DLabel}: ${depth3D.toInt()}", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = depth3D,
                        onValueChange = { depth3D = it },
                        valueRange = 8f..56f
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                shapes.forEach { shape ->
                    val effective3D = enable3D || shape.is3DShape
                    val previewShape = remember(shape, selectedColorHex, effective3D, depth3D) {
                        SlideElement.ShapeElement(
                            width = 160f,
                            height = 110f,
                            shapeType = shape,
                            fillColorHex = selectedColorHex,
                            strokeColorHex = "#0F172A",
                            strokeWidth = 3f,
                            is3D = effective3D,
                            depth3D = depth3D
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 2.dp,
                        onClick = {
                            onSelectShape(shape, selectedColorHex, effective3D, depth3D)
                            onDismiss()
                        },
                        modifier = Modifier
                            .width(104.dp)
                            .testTag("shape_pick_${shape.name}")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Box(modifier = Modifier.size(width = 68.dp, height = 46.dp)) {
                                Shape3DAndExtendedView(element = previewShape)
                            }
                            Text(
                                text = shape.label(lang),
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            Color100PaletteGrid(
                selectedHex = selectedColorHex,
                onSelectColor = { selectedColorHex = it },
                testTagPrefix = "shape_color"
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}
