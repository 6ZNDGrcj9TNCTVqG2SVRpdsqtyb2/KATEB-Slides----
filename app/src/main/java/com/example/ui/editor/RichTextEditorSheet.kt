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
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignCenter
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppLanguage
import com.example.core.localization.LocalAppStrings
import com.example.data.local.ImportedFontSpec
import com.example.engine.MultiLanguageSpellChecker
import com.example.engine.SlideElement
import com.example.engine.TextAlignment
import com.example.engine.TextListType
import com.example.engine.VerticalAlignment
import com.example.engine.WritingDirection
import com.example.engine.detectTextIsRtl
import com.example.engine.parseColorInt
import com.example.ui.theme.SlideFontRegistry

private val textColors = listOf(
    "#0F172A", "#FFFFFF", "#0D47A1", "#00796B", "#DC2626", "#D97706", "#7C3AED", "#334155"
)

private val highlightColors = listOf(
    "#00000000", "#FEF08A", "#BFDBFE", "#BBF7D0", "#FECACA", "#E9D5FF"
)

private val commonSymbols = listOf("•", "→", "←", "✓", "★", "§", "©", "®", "°", "±", "∞", "≈", "«", "»")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RichTextEditorSheet(
    element: SlideElement.TextElement,
    presentationLanguage: AppLanguage = LocalAppLanguage.current,
    customFonts: List<ImportedFontSpec> = emptyList(),
    onImportCustomFont: () -> Unit = {},
    onDeleteCustomFont: (String) -> Unit = {},
    onDismiss: () -> Unit,
    onSaveElement: (SlideElement.TextElement) -> Unit,
    onProofreadWithAi: (SlideElement.TextElement) -> Unit
) {
    val strings = LocalAppStrings.current
    var draft by remember(element.id) { mutableStateOf(element) }
    var showLinkField by remember { mutableStateOf(draft.linkUrl.isNotBlank()) }
    var showAll100Colors by remember { mutableStateOf(false) }

    val isRtl = detectTextIsRtl(draft.text, draft.writingDirection)
    val spellIssues = remember(draft.text, presentationLanguage) {
        MultiLanguageSpellChecker.checkText(draft.text, presentationLanguage)
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(strings.textEditorTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Button(
                    onClick = {
                        onSaveElement(draft)
                        onDismiss()
                    },
                    modifier = Modifier.testTag("rich_text_done_btn")
                ) {
                    Text(strings.done)
                }
            }

            // Text Input with dynamic RTL/LTR & live typography styling
            CompositionLocalProvider(
                LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {
                OutlinedTextField(
                    value = draft.text,
                    onValueChange = { draft = draft.copy(text = it) },
                    minLines = 3,
                    maxLines = 7,
                    textStyle = TextStyle(
                        fontFamily = SlideFontRegistry.fontFamilyFor(draft.fontFamilyId),
                        fontSize = 18.sp,
                        fontWeight = if (draft.isBold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (draft.isItalic) FontStyle.Italic else FontStyle.Normal,
                        textDecoration = TextDecoration.combine(
                            buildList {
                                if (draft.isUnderline) add(TextDecoration.Underline)
                                if (draft.isStrikethrough) add(TextDecoration.LineThrough)
                            }
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rich_text_input")
                )
            }

            // Real-Time Multi-Language Spellcheck Panel
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("spellcheck_panel")
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (spellIssues.isEmpty()) Icons.Default.CheckCircle else Icons.Default.Spellcheck,
                            contentDescription = strings.spellCheckIssuesTitle,
                            tint = if (spellIssues.isEmpty()) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (spellIssues.isEmpty()) {
                                "${strings.spellCheckCleanLabel} (${presentationLanguage.nativeName})"
                            } else {
                                "${strings.spellCheckIssuesTitle} • ${strings.spellCheckTapToFix}"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (spellIssues.isNotEmpty()) {
                        spellIssues.take(4).forEach { issue ->
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "${issue.word} →",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                                issue.suggestions.forEach { fix ->
                                    AssistChip(
                                        onClick = {
                                            draft = draft.copy(
                                                text = MultiLanguageSpellChecker.applyFix(draft.text, issue, fix)
                                            )
                                        },
                                        label = { Text(fix) },
                                        modifier = Modifier.testTag("spell_fix_${fix}")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // AI Proofread quick button
            OutlinedButton(
                onClick = {
                    onSaveElement(draft)
                    onProofreadWithAi(draft)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().testTag("rich_text_ai_proofread_btn")
            ) {
                Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(strings.aiProofread)
            }

            // Font Family Selection + Local Custom Font Import (.ttf / .otf)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(strings.fontLabel, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                OutlinedButton(
                    onClick = onImportCustomFont,
                    modifier = Modifier.testTag("editor_import_font_btn")
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.importCustomFont, style = MaterialTheme.typography.labelMedium)
                }
            }

            if (customFonts.isNotEmpty()) {
                Text(
                    text = strings.importedFonts,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(customFonts, key = { it.id }) { spec ->
                        val family = SlideFontRegistry.fontFamilyFor(spec.id)
                        FilterChip(
                            selected = draft.fontFamilyId == spec.id,
                            onClick = { draft = draft.copy(fontFamilyId = spec.id) },
                            label = { Text(spec.displayName, fontFamily = family) },
                            trailingIcon = {
                                IconButton(
                                    onClick = { onDeleteCustomFont(spec.id) },
                                    modifier = Modifier
                                        .size(20.dp)
                                        .testTag("delete_custom_font_${spec.id}")
                                ) {
                                    Icon(
                                        Icons.Rounded.Close,
                                        contentDescription = strings.deleteFontAction,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            },
                            modifier = Modifier.testTag("font_chip_${spec.id}")
                        )
                    }
                }
            }

            Text(strings.builtinFontsLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.testTag("builtin_fonts_row")
            ) {
                items(SlideFontRegistry.builtInOptions, key = { it.id }) { opt ->
                    FilterChip(
                        selected = draft.fontFamilyId == opt.id,
                        onClick = { draft = draft.copy(fontFamilyId = opt.id) },
                        label = {
                            Text(opt.displayName, fontFamily = opt.composeFamily)
                        },
                        modifier = Modifier.testTag("font_chip_${opt.id}")
                    )
                }
            }

            // Style Toggles (B / I / U / S / Bullet / Numbered / Quote / Link)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.padding(6.dp)
                ) {
                    FormatToggleBtn(
                        selected = draft.isBold,
                        onClick = { draft = draft.copy(isBold = !draft.isBold) },
                        icon = { Icon(Icons.Default.FormatBold, strings.boldLabel) }
                    )
                    FormatToggleBtn(
                        selected = draft.isItalic,
                        onClick = { draft = draft.copy(isItalic = !draft.isItalic) },
                        icon = { Icon(Icons.Default.FormatItalic, strings.italicLabel) }
                    )
                    FormatToggleBtn(
                        selected = draft.isUnderline,
                        onClick = { draft = draft.copy(isUnderline = !draft.isUnderline) },
                        icon = { Icon(Icons.Default.FormatUnderlined, strings.underlineLabel) }
                    )
                    FormatToggleBtn(
                        selected = draft.isStrikethrough,
                        onClick = { draft = draft.copy(isStrikethrough = !draft.isStrikethrough) },
                        icon = { Icon(Icons.Default.FormatStrikethrough, strings.strikeLabel) }
                    )
                    FormatToggleBtn(
                        selected = draft.listType == TextListType.BULLET,
                        onClick = {
                            draft = draft.copy(
                                listType = if (draft.listType == TextListType.BULLET) TextListType.NONE else TextListType.BULLET
                            )
                        },
                        icon = { Icon(Icons.AutoMirrored.Filled.FormatListBulleted, strings.bulletList) }
                    )
                    FormatToggleBtn(
                        selected = draft.listType == TextListType.NUMBERED,
                        onClick = {
                            draft = draft.copy(
                                listType = if (draft.listType == TextListType.NUMBERED) TextListType.NONE else TextListType.NUMBERED
                            )
                        },
                        icon = { Icon(Icons.Default.FormatListNumbered, strings.numberedList) }
                    )
                    IconButton(
                        onClick = {
                            draft = draft.copy(
                                listType = if (draft.listType == TextListType.CHECKBOX) TextListType.NONE else TextListType.CHECKBOX
                            )
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (draft.listType == TextListType.CHECKBOX) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (draft.listType == TextListType.CHECKBOX) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.testTag("rt_checkbox_list_btn")
                    ) {
                        Icon(Icons.Default.CheckBox, strings.checkboxListLabel)
                    }
                    FormatToggleBtn(
                        selected = draft.isBlockQuote,
                        onClick = { draft = draft.copy(isBlockQuote = !draft.isBlockQuote) },
                        icon = { Icon(Icons.Default.FormatQuote, strings.blockQuote) }
                    )
                    FormatToggleBtn(
                        selected = showLinkField,
                        onClick = { showLinkField = !showLinkField },
                        icon = { Icon(Icons.Default.Link, strings.addLink) }
                    )
                }
            }

            if (showLinkField) {
                OutlinedTextField(
                    value = draft.linkUrl,
                    onValueChange = { draft = draft.copy(linkUrl = it) },
                    label = { Text(strings.addLink) },
                    placeholder = { Text("https://...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Font Size Slider + Stepper
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("${strings.fontSizeLabel}: ${draft.fontSizeSp.toInt()}", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(onClick = {
                        draft = draft.copy(fontSizeSp = (draft.fontSizeSp - 2f).coerceAtLeast(12f))
                    }) { Text("-") }
                    OutlinedButton(onClick = {
                        draft = draft.copy(fontSizeSp = (draft.fontSizeSp + 2f).coerceAtMost(96f))
                    }) { Text("+") }
                }
            }
            Slider(
                value = draft.fontSizeSp,
                onValueChange = { draft = draft.copy(fontSizeSp = it) },
                valueRange = 12f..96f
            )

            // Alignment + Vertical Alignment (fully translated)
            Text(strings.alignmentLabel, style = MaterialTheme.typography.labelLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FormatToggleBtn(
                    selected = draft.textAlign == TextAlignment.START,
                    onClick = { draft = draft.copy(textAlign = TextAlignment.START) },
                    icon = { Icon(Icons.AutoMirrored.Filled.FormatAlignLeft, strings.alignStartLabel) }
                )
                FormatToggleBtn(
                    selected = draft.textAlign == TextAlignment.CENTER,
                    onClick = { draft = draft.copy(textAlign = TextAlignment.CENTER) },
                    icon = { Icon(Icons.Default.FormatAlignCenter, strings.alignCenterLabel) }
                )
                FormatToggleBtn(
                    selected = draft.textAlign == TextAlignment.END,
                    onClick = { draft = draft.copy(textAlign = TextAlignment.END) },
                    icon = { Icon(Icons.AutoMirrored.Filled.FormatAlignRight, strings.alignEndLabel) }
                )
                FormatToggleBtn(
                    selected = draft.textAlign == TextAlignment.JUSTIFY,
                    onClick = { draft = draft.copy(textAlign = TextAlignment.JUSTIFY) },
                    icon = { Icon(Icons.Default.FormatAlignJustify, strings.alignJustifyLabel) }
                )
                Spacer(Modifier.weight(1f))
                FormatToggleBtn(
                    selected = draft.verticalAlign == VerticalAlignment.TOP,
                    onClick = { draft = draft.copy(verticalAlign = VerticalAlignment.TOP) },
                    icon = { Icon(Icons.Default.VerticalAlignTop, strings.valignTopLabel) }
                )
                FormatToggleBtn(
                    selected = draft.verticalAlign == VerticalAlignment.CENTER,
                    onClick = { draft = draft.copy(verticalAlign = VerticalAlignment.CENTER) },
                    icon = { Icon(Icons.Default.VerticalAlignCenter, strings.valignMiddleLabel) }
                )
                FormatToggleBtn(
                    selected = draft.verticalAlign == VerticalAlignment.BOTTOM,
                    onClick = { draft = draft.copy(verticalAlign = VerticalAlignment.BOTTOM) },
                    icon = { Icon(Icons.Default.VerticalAlignBottom, strings.valignBottomLabel) }
                )
            }

            // Writing Direction (Auto / RTL / LTR)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = draft.writingDirection == WritingDirection.AUTO,
                    onClick = { draft = draft.copy(writingDirection = WritingDirection.AUTO) },
                    label = { Text(strings.directionAuto) }
                )
                FilterChip(
                    selected = draft.writingDirection == WritingDirection.RTL,
                    onClick = { draft = draft.copy(writingDirection = WritingDirection.RTL) },
                    label = { Text(strings.directionRtl) }
                )
                FilterChip(
                    selected = draft.writingDirection == WritingDirection.LTR,
                    onClick = { draft = draft.copy(writingDirection = WritingDirection.LTR) },
                    label = { Text(strings.directionLtr) }
                )
            }

            // Line Spacing & Letter Spacing
            Text("${strings.lineSpacing}: ${String.format("%.2f", draft.lineHeightMultiplier)}x", style = MaterialTheme.typography.labelMedium)
            Slider(
                value = draft.lineHeightMultiplier,
                onValueChange = { draft = draft.copy(lineHeightMultiplier = it) },
                valueRange = 0.9f..2.2f
            )
            Text("${strings.letterSpacing}: ${draft.letterSpacing.toInt()}", style = MaterialTheme.typography.labelMedium)
            Slider(
                value = draft.letterSpacing,
                onValueChange = { draft = draft.copy(letterSpacing = it) },
                valueRange = 0f..12f
            )

            // Colors & Highlights
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(strings.textColor, style = MaterialTheme.typography.labelLarge)
                FilterChip(
                    selected = showAll100Colors,
                    onClick = { showAll100Colors = !showAll100Colors },
                    label = { Text(strings.colors100Label) },
                    modifier = Modifier.testTag("rt_colors_100_toggle")
                )
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(textColors) { hex ->
                    ColorSwatch(
                        hex = hex,
                        selected = draft.textColorHex.equals(hex, ignoreCase = true),
                        onClick = { draft = draft.copy(textColorHex = hex) }
                    )
                }
            }
            if (showAll100Colors) {
                Color100PaletteGrid(
                    selectedHex = draft.textColorHex,
                    onSelectColor = { draft = draft.copy(textColorHex = it) },
                    testTagPrefix = "rt_c100"
                )
            }

            Text(strings.highlightColor, style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(highlightColors) { hex ->
                    ColorSwatch(
                        hex = hex,
                        selected = draft.highlightColorHex.equals(hex, ignoreCase = true),
                        onClick = { draft = draft.copy(highlightColorHex = hex) }
                    )
                }
            }

            HorizontalDivider()
            Text(strings.insertSymbol, style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(commonSymbols) { sym ->
                    OutlinedButton(onClick = { draft = draft.copy(text = draft.text + sym) }) {
                        Text(sym, fontSize = 18.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FormatToggleBtn(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    ) {
        icon()
    }
}

@Composable
fun ColorSwatch(
    hex: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val parsed = parseColorInt(hex, 0)
    val isTransparent = (parsed ushr 24) == 0
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(if (isTransparent) Color.White else Color(parsed))
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    ) {
        if (isTransparent) {
            Text("∅", fontSize = 14.sp, color = Color.Gray)
        }
    }
}
