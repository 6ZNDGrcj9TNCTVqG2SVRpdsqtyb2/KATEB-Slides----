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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.engine.AnimationDirection
import com.example.engine.DecorationCategory
import com.example.engine.ElementAnimation
import com.example.engine.ElementAnimationType
import com.example.engine.EquationCategory
import com.example.engine.EquationTemplateCatalog
import com.example.engine.MotionRange
import com.example.engine.MotionThemeCatalog
import com.example.engine.MotionThemeCategory
import com.example.engine.MotionThemeSpec
import com.example.engine.SlideDecorationCatalog
import com.example.engine.SlideTransition
import com.example.engine.SlideTransitionType
import com.example.engine.ToolCommandCatalog
import com.example.engine.ToolCommandCategory
import com.example.engine.ToolCommandSpec
import com.example.engines.SlideBitmapRenderer

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DecorationsAndColorsSheet(
    currentDecorationId: String,
    currentDecorationColorHex: String,
    onApplyDecoration: (decorationId: String, colorHex: String) -> Unit,
    onApplyAllSlides: (decorationId: String, colorHex: String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    var selectedId by remember { mutableStateOf(currentDecorationId) }
    var selectedHex by remember { mutableStateOf(currentDecorationColorHex) }
    var activeCat by remember { mutableStateOf<DecorationCategory?>(null) }

    val list = remember(activeCat) {
        if (activeCat == null) SlideDecorationCatalog.decorations
        else SlideDecorationCatalog.decorations.filter { it.category == activeCat }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp)
                .testTag("decorations_100_sheet"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AutoFixHigh, null, tint = MaterialTheme.colorScheme.primary)
                Text(strings.ornaments100Title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(
                        selected = activeCat == null,
                        onClick = { activeCat = null },
                        label = { Text("${strings.allLabel} (${SlideDecorationCatalog.decorations.size})") }
                    )
                }
                items(DecorationCategory.entries) { cat ->
                    FilterChip(
                        selected = activeCat == cat,
                        onClick = { activeCat = cat },
                        label = { Text(cat.label(lang)) }
                    )
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(list, key = { it.id }) { spec ->
                    val isSelected = selectedId == spec.id
                    Card(
                        onClick = {
                            selectedId = spec.id
                            if (selectedHex.isBlank()) selectedHex = spec.defaultHex
                            onApplyDecoration(spec.id, selectedHex)
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier
                            .width(138.dp)
                            .testTag("ornament_card_${spec.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            ) {
                                OrnamentDecorationLayer(
                                    decorationId = spec.id,
                                    decorationColorHex = selectedHex.ifBlank { spec.defaultHex }
                                )
                            }
                            Text(
                                text = spec.label(lang),
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Color100PaletteGrid(
                selectedHex = selectedHex,
                onSelectColor = { hex ->
                    selectedHex = hex
                    onApplyDecoration(selectedId, hex)
                },
                testTagPrefix = "ornament_color"
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        onApplyAllSlides(selectedId, selectedHex)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f).testTag("ornament_apply_all_btn")
                ) {
                    Text(strings.applyOrnamentAllSlides, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Button(onClick = onDismiss, modifier = Modifier.weight(0.6f)) {
                    Text(strings.done)
                }
            }
            Spacer(Modifier.height(14.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MotionChoreographySheet(
    currentTransition: SlideTransition,
    selectedAnimation: ElementAnimation?,
    onApplyMotionTheme: (MotionThemeSpec) -> Unit,
    onApplyMotionThemeAllSlides: (MotionThemeSpec) -> Unit,
    onApplyManualAnimation: (SlideTransition, ElementAnimation?) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    var activeCat by remember { mutableStateOf<MotionThemeCategory?>(null) }
    var transitionType by remember { mutableStateOf(currentTransition.type) }
    var entranceType by remember { mutableStateOf(selectedAnimation?.type ?: ElementAnimationType.NONE) }
    var exitType by remember { mutableStateOf(selectedAnimation?.exitType ?: ElementAnimationType.NONE) }
    var direction by remember { mutableStateOf(selectedAnimation?.direction ?: AnimationDirection.FROM_BOTTOM) }
    var motionRange by remember { mutableStateOf(selectedAnimation?.motionRange ?: MotionRange.NORMAL) }
    var durationMs by remember { mutableIntStateOf(currentTransition.durationMs) }

    val themes = remember(activeCat) {
        if (activeCat == null) MotionThemeCatalog.themes
        else MotionThemeCatalog.themes.filter { it.category == activeCat }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp)
                .testTag("motion_100_sheet"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Animation, null, tint = MaterialTheme.colorScheme.primary)
                Text(strings.motion100Title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            // Category filter for Stealth / Exciting / Dynamic / Scientific / Academic
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(
                        selected = activeCat == null,
                        onClick = { activeCat = null },
                        label = { Text("${strings.allLabel} (${MotionThemeCatalog.themes.size})") }
                    )
                }
                items(MotionThemeCategory.entries) { cat ->
                    FilterChip(
                        selected = activeCat == cat,
                        onClick = { activeCat = cat },
                        label = { Text(cat.label(lang)) }
                    )
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(themes, key = { it.id }) { spec ->
                    Card(
                        onClick = {
                            onApplyMotionTheme(spec)
                            onDismiss()
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .width(172.dp)
                            .testTag("motion_theme_${spec.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(spec.label(lang), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(spec.transitionType.label(lang), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(
                                "${spec.entranceType.label(lang)} → ${spec.exitType.label(lang)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "${spec.motionRange.label(lang)} • ${spec.durationMs}ms",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedButton(
                                onClick = {
                                    onApplyMotionThemeAllSlides(spec)
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(strings.applyMotionAllSlides, fontSize = 11.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            Text(strings.slideTransitionLabel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(SlideTransitionType.entries) { t ->
                    FilterChip(
                        selected = transitionType == t,
                        onClick = { transitionType = t },
                        label = { Text(t.label(lang)) }
                    )
                }
            }

            if (selectedAnimation != null) {
                Text(strings.elementAnimationLabel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(ElementAnimationType.entries.filter { !it.isExit }) { e ->
                        FilterChip(
                            selected = entranceType == e,
                            onClick = { entranceType = e },
                            label = { Text(e.label(lang)) }
                        )
                    }
                }

                Text(strings.exitAnimationLabel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val exitOptions = listOf(ElementAnimationType.NONE) + ElementAnimationType.entries.filter { it.isExit }
                    items(exitOptions) { ex ->
                        FilterChip(
                            selected = exitType == ex,
                            onClick = { exitType = ex },
                            label = { Text(ex.label(lang)) }
                        )
                    }
                }

                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AnimationDirection.entries.forEach { d ->
                        FilterChip(
                            selected = direction == d,
                            onClick = { direction = d },
                            label = { Text(d.label(lang)) }
                        )
                    }
                }

                Text(strings.motionExtentLabel, style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MotionRange.entries.forEach { range ->
                        FilterChip(
                            selected = motionRange == range,
                            onClick = { motionRange = range },
                            label = { Text(range.label(lang)) }
                        )
                    }
                }
            }

            Text("${strings.durationMsLabel}: $durationMs", style = MaterialTheme.typography.labelMedium)
            Slider(
                value = durationMs.toFloat(),
                onValueChange = { durationMs = it.toInt() },
                valueRange = 200f..1400f
            )

            Button(
                onClick = {
                    val tr = SlideTransition(transitionType, durationMs)
                    val anim = selectedAnimation?.copy(
                        type = entranceType,
                        exitType = exitType,
                        direction = direction,
                        motionRange = motionRange,
                        durationMs = durationMs
                    )
                    onApplyManualAnimation(tr, anim)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().testTag("motion_apply_manual_btn")
            ) {
                Text(strings.done)
            }
            Spacer(Modifier.height(14.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Equations100Sheet(
    onDismiss: () -> Unit,
    onInsertEquation: (expression: String) -> Unit
) {
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    var query by remember { mutableStateOf("") }
    var activeCat by remember { mutableStateOf<EquationCategory?>(null) }
    var customLatex by remember { mutableStateOf("") }

    val filtered = remember(query, activeCat) {
        EquationTemplateCatalog.equations.filter { eq ->
            val catOk = activeCat == null || eq.category == activeCat
            val qOk = query.isBlank() ||
                eq.titleAr.contains(query, ignoreCase = true) ||
                eq.titleEn.contains(query, ignoreCase = true) ||
                eq.expression.contains(query, ignoreCase = true)
            catOk && qOk
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp)
                .testTag("equations_100_sheet"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Functions, null, tint = MaterialTheme.colorScheme.primary)
                Text(strings.equations100Title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                placeholder = { Text(strings.equationHint, maxLines = 1) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(
                        selected = activeCat == null,
                        onClick = { activeCat = null },
                        label = { Text("${strings.allLabel} (${EquationTemplateCatalog.equations.size})") }
                    )
                }
                items(EquationCategory.entries) { cat ->
                    FilterChip(
                        selected = activeCat == cat,
                        onClick = { activeCat = cat },
                        label = { Text(cat.label(lang)) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp, max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filtered, key = { it.id }) { eq ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        tonalElevation = 1.dp,
                        onClick = {
                            onInsertEquation(eq.expression)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("eq_preset_${eq.id}")
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(eq.label(lang), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                            Text(
                                SlideBitmapRenderer.formatMathExpression(eq.expression),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = customLatex,
                    onValueChange = { customLatex = it },
                    placeholder = { Text("E = mc^2") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        if (customLatex.isNotBlank()) {
                            onInsertEquation(customLatex)
                            onDismiss()
                        }
                    }
                ) {
                    Text(strings.confirm)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun Tools100RibbonSheet(
    onDismiss: () -> Unit,
    onExecuteCommand: (ToolCommandSpec) -> Unit
) {
    val strings = LocalAppStrings.current
    val lang = LocalAppLanguage.current
    var query by remember { mutableStateOf("") }
    var activeCat by remember { mutableStateOf<ToolCommandCategory?>(null) }

    val filtered = remember(query, activeCat) {
        ToolCommandCatalog.commands.filter { cmd ->
            val catOk = activeCat == null || cmd.category == activeCat
            val qOk = query.isBlank() ||
                cmd.labelAr.contains(query, ignoreCase = true) ||
                cmd.labelEn.contains(query, ignoreCase = true)
            catOk && qOk
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp)
                .testTag("tools_100_sheet"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Tune, null, tint = MaterialTheme.colorScheme.primary)
                Text(strings.tools100Title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                placeholder = { Text(strings.searchToolsHint) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(
                        selected = activeCat == null,
                        onClick = { activeCat = null },
                        label = { Text("${strings.allLabel} (${ToolCommandCatalog.commands.size})") }
                    )
                }
                items(ToolCommandCategory.entries) { cat ->
                    FilterChip(
                        selected = activeCat == cat,
                        onClick = { activeCat = cat },
                        label = { Text(cat.label(lang)) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filtered, key = { it.id }) { cmd ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        tonalElevation = 1.dp,
                        onClick = {
                            onDismiss()
                            onExecuteCommand(cmd)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tool_cmd_${cmd.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = cmd.label(lang),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = cmd.category.label(lang),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
        }
    }
}
