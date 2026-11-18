package com.example.ui.editor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.localization.LocalAppStrings
import com.example.engine.Presentation
import com.example.engines.PdfPreviewPage
import com.example.ui.AsyncOperationState

/**
 * Dedicated PDF Preview Window displaying rendered slide previews, real-time formatting metadata,
 * page reordering controls, existing LinearProgressIndicator progress tracking, and final PDF export CTA.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfPreviewSheet(
    presentation: Presentation,
    pages: List<PdfPreviewPage>,
    operationState: AsyncOperationState,
    onMovePage: (fromIndex: Int, toIndex: Int) -> Unit,
    onResetOrder: () -> Unit,
    onConfirmExport: (applyOrderToDeck: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var applyOrderToDeck by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp)
                .testTag("pdf_preview_sheet"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.PictureAsPdf, null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text(strings.pdfPreviewTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            strings.pdfPreviewSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                TextButton(onClick = onResetOrder, modifier = Modifier.testTag("pdf_reset_order_btn")) {
                    Icon(Icons.Default.RestartAlt, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(strings.resetOrderAction)
                }
            }

            // Formatting summary banner
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${presentation.slides.size} ${strings.slidesCountLabel}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "${presentation.aspectRatio.id} (${presentation.canvasWidth.toInt()}×${presentation.canvasHeight.toInt()} px)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Real-time progress bar integrated with existing AsyncOperationState
            if (operationState is AsyncOperationState.Working) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        operationState.message,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (operationState.progress != null) {
                        LinearProgressIndicator(
                            progress = { operationState.progress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pdf_preview_progress")
                        )
                    } else {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pdf_preview_progress")
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp, max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(pages, key = { _, p -> p.slideId }) { idx, page ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pdf_page_row_$idx")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Image(
                                bitmap = page.bitmap.asImageBitmap(),
                                contentDescription = "Page ${idx + 1}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(width = 112.dp, height = 64.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "#${idx + 1} (${strings.slidesCountLabel} ${page.originalIndex + 1})",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    page.titleSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Column {
                                IconButton(
                                    onClick = { onMovePage(idx, idx - 1) },
                                    enabled = idx > 0,
                                    modifier = Modifier.testTag("pdf_move_up_$idx")
                                ) {
                                    Icon(Icons.Default.ArrowUpward, strings.moveSlideUp, modifier = Modifier.size(18.dp))
                                }
                                IconButton(
                                    onClick = { onMovePage(idx, idx + 1) },
                                    enabled = idx < pages.lastIndex,
                                    modifier = Modifier.testTag("pdf_move_down_$idx")
                                ) {
                                    Icon(Icons.Default.ArrowDownward, strings.moveSlideDown, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { applyOrderToDeck = !applyOrderToDeck }
            ) {
                Checkbox(
                    checked = applyOrderToDeck,
                    onCheckedChange = { applyOrderToDeck = it },
                    modifier = Modifier.testTag("pdf_apply_order_checkbox")
                )
                Text(strings.applyPageOrderToDeck, style = MaterialTheme.typography.bodyMedium)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(strings.cancel)
                }
                Button(
                    onClick = {
                        onConfirmExport(applyOrderToDeck)
                        onDismiss()
                    },
                    enabled = pages.isNotEmpty() && operationState !is AsyncOperationState.Working,
                    modifier = Modifier
                        .weight(1.4f)
                        .testTag("pdf_preview_export_btn")
                ) {
                    Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.confirmExportPdf)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
