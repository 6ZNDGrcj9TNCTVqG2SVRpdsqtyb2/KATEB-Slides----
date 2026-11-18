package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DesignServices
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.example.ai.SlideDesignSuggestion
import com.example.ai.SmartThemeSuggestion
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppLanguage
import com.example.core.localization.LocalAppStrings
import com.example.engine.MAX_SLIDES_PER_DECK
import com.example.engine.parseColorInt
import com.example.engines.ImageExportFormat
import com.example.engines.VideoResolution
import com.example.ui.AsyncOperationState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExportDeckSheet(
    operationState: AsyncOperationState = AsyncOperationState.Idle,
    onDismiss: () -> Unit,
    onExportPptx: () -> Unit,
    onExportPdf: () -> Unit,
    onPreviewPdf: (() -> Unit)? = null,
    onExportHtml5Ios: (() -> Unit)? = null,
    onExportImages: (ImageExportFormat) -> Unit,
    onExportMp4: (VideoResolution, Int) -> Unit,
    onPickVideoAudio: () -> Unit,
    hasAttachedAudio: Boolean
) {
    val strings = LocalAppStrings.current
    var videoRes by remember { mutableStateOf(VideoResolution.HD_720P) }
    var slideDurationSec by remember { mutableFloatStateOf(4f) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(strings.export, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            if (operationState is AsyncOperationState.Working) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(operationState.message, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        LinearProgressIndicator(
                            progress = { (operationState.progress ?: 0.5f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sheet_export_progress")
                        )
                    }
                }
            }

            Button(
                onClick = { onDismiss(); onExportPptx() },
                modifier = Modifier.fillMaxWidth().testTag("export_pptx_btn")
            ) {
                Icon(Icons.Default.Slideshow, null)
                Spacer(Modifier.width(8.dp))
                Text(strings.exportPptx)
            }

            if (onPreviewPdf != null) {
                Button(
                    onClick = { onDismiss(); onPreviewPdf() },
                    modifier = Modifier.fillMaxWidth().testTag("export_pdf_preview_btn")
                ) {
                    Icon(Icons.Default.Preview, null)
                    Spacer(Modifier.width(8.dp))
                    Text(strings.pdfPreviewTitle)
                }
            }

            OutlinedButton(
                onClick = {
                    onDismiss()
                    if (onPreviewPdf != null) onPreviewPdf() else onExportPdf()
                },
                modifier = Modifier.fillMaxWidth().testTag("export_pdf_btn")
            ) {
                Icon(Icons.Default.PictureAsPdf, null)
                Spacer(Modifier.width(8.dp))
                Text(strings.exportPdf)
            }

            if (onExportHtml5Ios != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onDismiss(); onExportHtml5Ios() },
                            modifier = Modifier.fillMaxWidth().testTag("export_html_ios_btn")
                        ) {
                            Icon(Icons.Default.Language, null)
                            Spacer(Modifier.width(8.dp))
                            Text(strings.exportHtml5Ios)
                        }
                        Text(
                            text = strings.iosCrossPlatformNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { onDismiss(); onExportImages(ImageExportFormat.PNG) },
                    modifier = Modifier.weight(1f).testTag("export_png_btn")
                ) {
                    Icon(Icons.Default.Image, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.exportPng)
                }
                OutlinedButton(
                    onClick = { onDismiss(); onExportImages(ImageExportFormat.JPG) },
                    modifier = Modifier.weight(1f).testTag("export_jpg_btn")
                ) {
                    Icon(Icons.Default.Image, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.exportJpg)
                }
            }

            HorizontalDivider()
            Text(strings.exportMp4, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                VideoResolution.entries.forEach { res ->
                    FilterChip(
                        selected = videoRes == res,
                        onClick = { videoRes = res },
                        label = { Text(res.label) }
                    )
                }
            }
            Text("${strings.exportSlideDurationSec}: ${slideDurationSec.toInt()}s")
            Slider(
                value = slideDurationSec,
                onValueChange = { slideDurationSec = it },
                valueRange = 2f..12f,
                steps = 9
            )
            OutlinedButton(onClick = onPickVideoAudio, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.MusicNote, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (hasAttachedAudio) "✓ ${strings.exportIncludeAudio}" else strings.exportIncludeAudio)
            }
            Button(
                onClick = { onDismiss(); onExportMp4(videoRes, slideDurationSec.toInt()) },
                modifier = Modifier.fillMaxWidth().testTag("export_mp4_btn")
            ) {
                Icon(Icons.Default.Movie, null)
                Spacer(Modifier.width(8.dp))
                Text(strings.exportMp4)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AiAssistantSheet(
    hasSelectedText: Boolean,
    designSuggestion: SlideDesignSuggestion? = null,
    smartTheme: SmartThemeSuggestion? = null,
    deckSummary: String? = null,
    onDismiss: () -> Unit,
    onGenerateDeck: (prompt: String, count: Int, lang: AppLanguage) -> Unit,
    onSummarizeResearch: (text: String, count: Int, lang: AppLanguage) -> Unit,
    onTranslateDeck: (targetLang: AppLanguage) -> Unit,
    onProofreadSelected: () -> Unit,
    onGenerateSpeakerNotes: () -> Unit,
    onGenerateIllustration: (prompt: String) -> Unit,
    onAnalyzeSlideDesign: () -> Unit = {},
    onApplyDesignSuggestion: () -> Unit = {},
    onSuggestSmartTheme: () -> Unit = {},
    onApplySmartTheme: () -> Unit = {},
    onSummarizeDeck: () -> Unit = {}
) {
    val strings = LocalAppStrings.current
    val uiLang = LocalAppLanguage.current
    var tab by remember { mutableIntStateOf(0) }
    var promptText by remember { mutableStateOf("") }
    var slideCount by remember { mutableFloatStateOf(8f) }
    var targetLang by remember { mutableStateOf(uiLang) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary)
                Text(strings.aiAssistant, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Text(strings.aiOfflineNote, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(selected = tab == 0, onClick = { tab = 0 }, label = { Text(strings.aiGenerateDeck) })
                FilterChip(selected = tab == 1, onClick = { tab = 1 }, label = { Text(strings.aiSummarizeResearch) })
                FilterChip(selected = tab == 2, onClick = { tab = 2 }, label = { Text(strings.aiTranslateDeck) })
                FilterChip(selected = tab == 3, onClick = { tab = 3 }, label = { Text(strings.aiGenerateImage) })
                FilterChip(selected = tab == 4, onClick = { tab = 4 }, label = { Text(strings.aiDesignAdvisor) })
                FilterChip(selected = tab == 5, onClick = { tab = 5 }, label = { Text(strings.aiSmartThemeEngine) })
            }

            when (tab) {
                0, 1 -> {
                    OutlinedTextField(
                        value = promptText,
                        onValueChange = { promptText = it },
                        placeholder = { Text(if (tab == 0) strings.aiPromptHint else strings.aiSummarizeResearch) },
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth().testTag("ai_sheet_input")
                    )
                    Text("${strings.slideCountLabel}: ${slideCount.toInt()} / $MAX_SLIDES_PER_DECK")
                    Slider(
                        value = slideCount,
                        onValueChange = { slideCount = it },
                        valueRange = 3f..MAX_SLIDES_PER_DECK.toFloat()
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(5, 10, 25, 50, 100).forEach { c ->
                            FilterChip(
                                selected = slideCount.toInt() == c,
                                onClick = { slideCount = c.toFloat() },
                                label = { Text("$c") }
                            )
                        }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AppLanguage.entries.forEach { l ->
                            FilterChip(selected = targetLang == l, onClick = { targetLang = l }, label = { Text(l.nativeName) })
                        }
                    }
                    Button(
                        onClick = {
                            onDismiss()
                            if (tab == 0) onGenerateDeck(promptText, slideCount.toInt(), targetLang)
                            else onSummarizeResearch(promptText, slideCount.toInt(), targetLang)
                        },
                        enabled = promptText.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().testTag("ai_sheet_run_btn")
                    ) {
                        Icon(Icons.Default.AutoAwesome, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (tab == 0) strings.aiGenerateDeck else strings.aiSummarizeResearch)
                    }
                }
                2 -> {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AppLanguage.entries.forEach { l ->
                            FilterChip(selected = targetLang == l, onClick = { targetLang = l }, label = { Text(l.nativeName) })
                        }
                    }
                    Button(
                        onClick = { onDismiss(); onTranslateDeck(targetLang) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Translate, null)
                        Spacer(Modifier.width(8.dp))
                        Text(strings.aiTranslateDeck)
                    }
                }
                3 -> {
                    OutlinedTextField(
                        value = promptText,
                        onValueChange = { promptText = it },
                        label = { Text(strings.aiGenerateImage) },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { onDismiss(); onGenerateIllustration(promptText) },
                        enabled = promptText.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Image, null)
                        Spacer(Modifier.width(8.dp))
                        Text(strings.aiGenerateImage)
                    }
                }
                4 -> {
                    // Real-Time Design Advisor (Text vs Visual balance & overlap fixing)
                    Button(
                        onClick = onAnalyzeSlideDesign,
                        modifier = Modifier.fillMaxWidth().testTag("ai_analyze_layout_btn")
                    ) {
                        Icon(Icons.Default.DesignServices, null)
                        Spacer(Modifier.width(8.dp))
                        Text(strings.aiAnalyzeLayout)
                    }
                    if (designSuggestion != null) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${strings.aiDesignAdvisor}: ${designSuggestion.balanceScore}/100",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(designSuggestion.recommendation, style = MaterialTheme.typography.bodyMedium)
                                Button(
                                    onClick = {
                                        onApplyDesignSuggestion()
                                        onDismiss()
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("ai_apply_layout_btn")
                                ) {
                                    Text(designSuggestion.suggestedActionLabel.ifBlank { strings.aiApplySuggestion })
                                }
                            }
                        }
                    }
                }
                5 -> {
                    // Smart Theme Engine (Colors, Typography, Backdrop)
                    Button(
                        onClick = onSuggestSmartTheme,
                        modifier = Modifier.fillMaxWidth().testTag("ai_suggest_theme_btn")
                    ) {
                        Icon(Icons.Default.Palette, null)
                        Spacer(Modifier.width(8.dp))
                        Text(strings.aiSuggestTheme)
                    }
                    if (smartTheme != null) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${smartTheme.themeName} (${smartTheme.fontFamilyId} • ${smartTheme.backgroundStyle})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(
                                        smartTheme.primaryColorHex,
                                        smartTheme.secondaryColorHex,
                                        smartTheme.accentColorHex,
                                        smartTheme.bgColorHex
                                    ).forEach { hex ->
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(Color(parseColorInt(hex)))
                                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                        )
                                    }
                                }
                                if (smartTheme.rationale.isNotBlank()) {
                                    Text(smartTheme.rationale, style = MaterialTheme.typography.bodySmall)
                                }
                                Button(
                                    onClick = {
                                        onApplySmartTheme()
                                        onDismiss()
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("ai_apply_theme_btn")
                                ) {
                                    Text(strings.aiApplyTheme)
                                }
                            }
                        }
                    }
                }
            }

            if (deckSummary != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = deckSummary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            HorizontalDivider()
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(onClick = { onDismiss(); onGenerateSpeakerNotes() }) {
                    Text(strings.aiSpeakerNotes)
                }
                OutlinedButton(
                    onClick = onSummarizeDeck,
                    modifier = Modifier.testTag("ai_summarize_deck_btn")
                ) {
                    Icon(Icons.Default.Summarize, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(strings.aiSummarizeDeck)
                }
                if (hasSelectedText) {
                    OutlinedButton(onClick = { onDismiss(); onProofreadSelected() }) {
                        Text(strings.aiProofread)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
