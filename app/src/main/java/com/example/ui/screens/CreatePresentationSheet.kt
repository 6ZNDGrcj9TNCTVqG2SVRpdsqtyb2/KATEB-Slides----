package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppLanguage
import com.example.core.localization.LocalAppStrings
import com.example.data.templates.PresentationTemplate
import com.example.data.templates.TemplateCategory
import com.example.data.templates.TemplateRepository
import com.example.engine.MAX_SLIDES_PER_DECK
import com.example.engine.SlideAspectRatio
import com.example.engine.parseColorInt

enum class CreationMode { BLANK, TEMPLATE, AI, PPTX, DOCX_TXT, PDF_IMAGES }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreatePresentationSheet(
    defaultAspectRatio: SlideAspectRatio,
    onDismiss: () -> Unit,
    onCreateBlank: (title: String, desc: String, count: Int, lang: AppLanguage, ratio: SlideAspectRatio, customW: Float, customH: Float) -> Unit,
    onCreateFromTemplate: (tpl: PresentationTemplate, title: String, desc: String, count: Int, lang: AppLanguage, ratio: SlideAspectRatio) -> Unit,
    onGenerateWithAi: (prompt: String, count: Int, lang: AppLanguage, ratio: SlideAspectRatio) -> Unit,
    onPickPptx: () -> Unit,
    onPickDocxOrTxt: () -> Unit,
    onPickPdf: () -> Unit,
    onPickPhotos: (title: String) -> Unit
) {
    val strings = LocalAppStrings.current
    val uiLang = LocalAppLanguage.current

    var mode by remember { mutableStateOf(CreationMode.BLANK) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var aiPrompt by remember { mutableStateOf("") }
    var slideCount by remember { mutableFloatStateOf(5f) }
    var deckLang by remember { mutableStateOf(uiLang) }
    var deckLangEdited by remember { mutableStateOf(false) }
    LaunchedEffect(uiLang) {
        if (!deckLangEdited) deckLang = uiLang
    }
    var aspectRatio by remember { mutableStateOf(defaultAspectRatio) }
    var customWidth by remember { mutableStateOf("1920") }
    var customHeight by remember { mutableStateOf("1080") }
    var selectedTemplate by remember { mutableStateOf(TemplateRepository.templates.first()) }
    var templateCategoryFilter by remember { mutableStateOf<TemplateCategory?>(null) }
    var templateSearch by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = strings.createDeckTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = mode == CreationMode.BLANK,
                    onClick = { mode = CreationMode.BLANK },
                    leadingIcon = { Icon(Icons.Default.AddBox, null) },
                    label = { Text(strings.blankDeck) },
                    modifier = Modifier.testTag("mode_blank")
                )
                FilterChip(
                    selected = mode == CreationMode.TEMPLATE,
                    onClick = { mode = CreationMode.TEMPLATE },
                    leadingIcon = { Icon(Icons.Default.GridView, null) },
                    label = { Text(strings.fromTemplate) },
                    modifier = Modifier.testTag("mode_template")
                )
                FilterChip(
                    selected = mode == CreationMode.AI,
                    onClick = { mode = CreationMode.AI },
                    leadingIcon = { Icon(Icons.Default.AutoAwesome, null) },
                    label = { Text(strings.withAi) },
                    modifier = Modifier.testTag("mode_ai")
                )
                FilterChip(
                    selected = mode == CreationMode.PPTX,
                    onClick = { mode = CreationMode.PPTX },
                    leadingIcon = { Icon(Icons.Default.UploadFile, null) },
                    label = { Text(strings.importPptx) },
                    modifier = Modifier.testTag("mode_pptx")
                )
                FilterChip(
                    selected = mode == CreationMode.DOCX_TXT,
                    onClick = { mode = CreationMode.DOCX_TXT },
                    leadingIcon = { Icon(Icons.Default.Description, null) },
                    label = { Text(strings.importDocxTxt) },
                    modifier = Modifier.testTag("mode_docx")
                )
                FilterChip(
                    selected = mode == CreationMode.PDF_IMAGES,
                    onClick = { mode = CreationMode.PDF_IMAGES },
                    leadingIcon = { Icon(Icons.Default.PictureAsPdf, null) },
                    label = { Text(strings.importPdfImages) },
                    modifier = Modifier.testTag("mode_pdf_images")
                )
            }

            HorizontalDivider()

            when (mode) {
                CreationMode.PPTX -> {
                    Button(
                        onClick = { onDismiss(); onPickPptx() },
                        modifier = Modifier.fillMaxWidth().testTag("pick_pptx_btn")
                    ) {
                        Icon(Icons.Default.UploadFile, null)
                        Spacer(Modifier.width(8.dp))
                        Text(strings.importPptx)
                    }
                }
                CreationMode.DOCX_TXT -> {
                    Button(
                        onClick = { onDismiss(); onPickDocxOrTxt() },
                        modifier = Modifier.fillMaxWidth().testTag("pick_docx_btn")
                    ) {
                        Icon(Icons.Default.Description, null)
                        Spacer(Modifier.width(8.dp))
                        Text(strings.importDocxTxt)
                    }
                }
                CreationMode.PDF_IMAGES -> {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(strings.deckTitleLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { onDismiss(); onPickPdf() },
                            modifier = Modifier.weight(1f).testTag("pick_pdf_btn")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, null)
                            Spacer(Modifier.width(6.dp))
                            Text("PDF")
                        }
                        OutlinedButton(
                            onClick = { onDismiss(); onPickPhotos(title) },
                            modifier = Modifier.weight(1f).testTag("pick_photos_btn")
                        ) {
                            Icon(Icons.Default.Image, null)
                            Spacer(Modifier.width(6.dp))
                            Text(strings.toolImage)
                        }
                    }
                }
                else -> {
                    if (mode == CreationMode.AI) {
                        OutlinedTextField(
                            value = aiPrompt,
                            onValueChange = { aiPrompt = it },
                            label = { Text(strings.withAi) },
                            placeholder = { Text(strings.aiPromptHint) },
                            minLines = 3,
                            maxLines = 5,
                            modifier = Modifier.fillMaxWidth().testTag("ai_create_prompt_input")
                        )
                        Text(
                            text = strings.aiOfflineNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text(strings.deckTitleLabel) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("create_title_input")
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text(strings.deckDescLabel) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (mode == CreationMode.TEMPLATE) {
                        Text(
                            text = "${strings.designStyleLabel} (${TemplateRepository.templates.size})",
                            style = MaterialTheme.typography.labelLarge
                        )
                        OutlinedTextField(
                            value = templateSearch,
                            onValueChange = { templateSearch = it },
                            placeholder = { Text(strings.searchTemplates) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item {
                                FilterChip(
                                    selected = templateCategoryFilter == null,
                                    onClick = { templateCategoryFilter = null },
                                    label = { Text("${strings.allLabel} (${TemplateRepository.templates.size})") }
                                )
                            }
                            items(TemplateCategory.entries) { cat ->
                                FilterChip(
                                    selected = templateCategoryFilter == cat,
                                    onClick = {
                                        templateCategoryFilter = if (templateCategoryFilter == cat) null else cat
                                    },
                                    label = { Text(cat.label(uiLang)) }
                                )
                            }
                        }
                        val filteredTpls = remember(templateCategoryFilter, templateSearch, uiLang) {
                            val q = templateSearch.trim()
                            TemplateRepository.templates.filter { tpl ->
                                val matchCat = templateCategoryFilter == null || tpl.category == templateCategoryFilter
                                val matchQ = q.isEmpty() ||
                                    tpl.name(uiLang).contains(q, ignoreCase = true) ||
                                    tpl.category.label(uiLang).contains(q, ignoreCase = true)
                                matchCat && matchQ
                            }
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(filteredTpls, key = { it.id }) { tpl ->
                                val pCol = Color(parseColorInt(tpl.primaryHex))
                                val sCol = Color(parseColorInt(tpl.secondaryHex))
                                val isSel = selectedTemplate.id == tpl.id
                                Column(
                                    modifier = Modifier
                                        .width(148.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(
                                            width = if (isSel) 2.5.dp else 1.dp,
                                            color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { selectedTemplate = tpl }
                                        .padding(8.dp)
                                        .testTag("create_tpl_${tpl.id}"),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(42.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Brush.linearGradient(listOf(pCol, sCol)))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(4.dp)
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(Color(parseColorInt(tpl.accentHex)))
                                        )
                                    }
                                    Text(
                                        text = tpl.name(uiLang),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = tpl.category.label(uiLang),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Slide count (up to 100 slides)
                    Text(
                        "${strings.slideCountLabel}: ${slideCount.toInt()} / $MAX_SLIDES_PER_DECK",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Slider(
                        value = slideCount,
                        onValueChange = { slideCount = it },
                        valueRange = 1f..MAX_SLIDES_PER_DECK.toFloat()
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(5, 10, 25, 50, 100).forEach { presetCount ->
                            FilterChip(
                                selected = slideCount.toInt() == presetCount,
                                onClick = { slideCount = presetCount.toFloat() },
                                label = { Text("$presetCount") }
                            )
                        }
                    }

                    // Presentation Language
                    Text(strings.deckLanguageLabel, style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            FilterChip(
                                selected = deckLang == lang,
                                onClick = {
                                    deckLangEdited = true
                                    deckLang = lang
                                },
                                label = { Text(lang.nativeName) }
                            )
                        }
                    }

                    // Slide Size
                    Text(strings.slideSizeLabel, style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SlideAspectRatio.entries.forEach { ratio ->
                            FilterChip(
                                selected = aspectRatio == ratio,
                                onClick = { aspectRatio = ratio },
                                label = { Text(ratio.label(uiLang)) }
                            )
                        }
                    }

                    if (aspectRatio == SlideAspectRatio.CUSTOM && mode == CreationMode.BLANK) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = customWidth,
                                onValueChange = { customWidth = it.filter { ch -> ch.isDigit() } },
                                label = { Text(strings.customSizeWidth) },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = customHeight,
                                onValueChange = { customHeight = it.filter { ch -> ch.isDigit() } },
                                label = { Text(strings.customSizeHeight) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val count = slideCount.toInt().coerceAtLeast(1)
                            onDismiss()
                            when (mode) {
                                CreationMode.BLANK -> onCreateBlank(
                                    title,
                                    description,
                                    count,
                                    deckLang,
                                    aspectRatio,
                                    customWidth.toFloatOrNull() ?: 1920f,
                                    customHeight.toFloatOrNull() ?: 1080f
                                )
                                CreationMode.TEMPLATE -> onCreateFromTemplate(
                                    selectedTemplate, title, description, count, deckLang, aspectRatio
                                )
                                CreationMode.AI -> onGenerateWithAi(
                                    aiPrompt.ifBlank { title.ifBlank { "KATEB Presentation" } },
                                    count.coerceIn(3, MAX_SLIDES_PER_DECK),
                                    deckLang,
                                    aspectRatio
                                )
                                else -> {}
                            }
                        },
                        enabled = mode != CreationMode.AI || aiPrompt.isNotBlank() || title.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("create_confirm_btn")
                    ) {
                        Text(strings.createAction)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}
