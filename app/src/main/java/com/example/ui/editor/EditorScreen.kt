package com.example.ui.editor

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowLeft
import androidx.compose.material.icons.automirrored.rounded.ArrowRight
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.BatterySaver
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.CheckBoxOutlineBlank
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DashboardCustomize
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.EmojiSymbols
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material.icons.rounded.Functions
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.ViewModule
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.ViewInAr
import androidx.compose.material.icons.rounded.ViewQuilt
import com.example.core.localization.LocalAppLanguage
import com.example.core.localization.LocalAppStrings
import com.example.engine.ChartSeries
import com.example.engine.ChartType
import com.example.engine.ElementAlignment
import com.example.engine.MAX_SLIDES_PER_DECK
import com.example.engine.ShapeType
import com.example.engine.SlideAspectRatio
import com.example.engine.SlideElement
import com.example.engine.SlideEngineState
import com.example.engine.SlideTransition
import com.example.engine.SlideTransitionType
import com.example.engine.SlideViewMode
import com.example.engine.SmartArtLayoutType
import com.example.engine.TextAlignment
import com.example.engine.TextListType
import com.example.engine.ToolCommandCatalog
import com.example.engine.ToolCommandSpec
import com.example.engine.WritingDirection
import com.example.engines.ImageExportFormat
import com.example.engines.VideoResolution
import com.example.ui.KatebViewModel

private sealed interface EditorSheet {
    data class RichText(val element: SlideElement.TextElement) : EditorSheet
    data object Shapes : EditorSheet
    data object Shapes3D : EditorSheet
    data object SmartArtGallery : EditorSheet
    data class SmartArtEdit(val element: SlideElement.SmartArtElement) : EditorSheet
    data object Ornaments100 : EditorSheet
    data object Motion100 : EditorSheet
    data object Equations100 : EditorSheet
    data object Tools100 : EditorSheet
    data object PdfPreview : EditorSheet
    data object TableCreate : EditorSheet
    data class TableEdit(val element: SlideElement.TableElement) : EditorSheet
    data class ChartEdit(val element: SlideElement.ChartElement?) : EditorSheet
    data class EquationEdit(val element: SlideElement.EquationElement?) : EditorSheet
    data object IconsLib : EditorSheet
    data object Background : EditorSheet
    data object Layouts : EditorSheet
    data object SlidesGrid : EditorSheet
    data object Inspector : EditorSheet
    data object Motion : EditorSheet
    data object Notes : EditorSheet
    data object Export : EditorSheet
    data object Ai : EditorSheet
    data object SlideSettings : EditorSheet
    data object FontPicker : EditorSheet
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: KatebViewModel,
    engineState: SlideEngineState,
    hasAttachedAudio: Boolean,
    onBack: () -> Unit,
    onPresent: (startIndex: Int) -> Unit,
    onOpenDesignTemplates: () -> Unit,
    onPickImageForSlide: () -> Unit,
    onPickImageForBackground: () -> Unit,
    onPickVideoAudio: () -> Unit,
    onPickCustomFont: () -> Unit = {},
    onExportPptx: () -> Unit = { viewModel.exportActiveToPptx() },
    onExportPdf: () -> Unit = { viewModel.exportActiveToPdf() },
    onExportImages: (ImageExportFormat) -> Unit = { viewModel.exportActiveToImages(it) },
    onExportMp4: (VideoResolution, Int) -> Unit,
    onExportHtml5Ios: () -> Unit = { viewModel.exportActiveToHtml5() }
) {
    val strings = LocalAppStrings.current
    val presentation = engineState.presentation ?: return
    val activeSlide = engineState.activeSlide ?: return

    val batteryAutoSavePaused by viewModel.batteryAutoSavePaused.collectAsStateWithLifecycle()
    val overlapCount by viewModel.overlapCount.collectAsStateWithLifecycle()
    val operationState by viewModel.operationState.collectAsStateWithLifecycle()
    val designSuggestion by viewModel.designSuggestionState.collectAsStateWithLifecycle()
    val smartTheme by viewModel.smartThemeState.collectAsStateWithLifecycle()
    val deckSummary by viewModel.deckSummaryState.collectAsStateWithLifecycle()
    val customFonts by viewModel.customFonts.collectAsStateWithLifecycle()
    val pdfPreviewPages by viewModel.pdfPreviewPages.collectAsStateWithLifecycle()

    var isFocusMode by remember { mutableStateOf(false) }
    var viewMode by remember { mutableStateOf(SlideViewMode.DESIGN) }

    BackHandler(onBack = {
        when {
            isFocusMode -> isFocusMode = false
            viewMode != SlideViewMode.DESIGN -> viewMode = SlideViewMode.DESIGN
            else -> onBack()
        }
    })

    var activeSheet by remember { mutableStateOf<EditorSheet?>(null) }
    var showPresentMenu by remember { mutableStateOf(false) }
    var isMultiSelectMode by remember { mutableStateOf(false) }

    val openEditorForElement: (SlideElement) -> Unit = { el ->
        when (el) {
            is SlideElement.TextElement -> activeSheet = EditorSheet.RichText(el)
            is SlideElement.TableElement -> activeSheet = EditorSheet.TableEdit(el)
            is SlideElement.ChartElement -> activeSheet = EditorSheet.ChartEdit(el)
            is SlideElement.EquationElement -> activeSheet = EditorSheet.EquationEdit(el)
            is SlideElement.SmartArtElement -> activeSheet = EditorSheet.SmartArtEdit(el)
            is SlideElement.ShapeElement, is SlideElement.IconElement -> activeSheet = EditorSheet.Inspector
            else -> {}
        }
    }

    val executeRibbonCommand: (ToolCommandSpec) -> Unit = { cmd ->
        val selectedEl = engineState.selectedElement
        val selectedText = selectedEl as? SlideElement.TextElement
            ?: activeSlide.elements.filterIsInstance<SlideElement.TextElement>().firstOrNull()
        when (cmd.id) {
            "cmd_add_text" -> viewModel.addTextElement()
            "cmd_add_bullet" -> {
                viewModel.addTextElement()
                (viewModel.engineState.value.selectedElement as? SlideElement.TextElement)?.let {
                    viewModel.slideEngine.updateElement(it.copy(listType = TextListType.BULLET))
                }
            }
            "cmd_add_numbered" -> {
                viewModel.addTextElement()
                (viewModel.engineState.value.selectedElement as? SlideElement.TextElement)?.let {
                    viewModel.slideEngine.updateElement(it.copy(listType = TextListType.NUMBERED))
                }
            }
            "cmd_add_checkbox" -> {
                viewModel.addTextElement()
                (viewModel.engineState.value.selectedElement as? SlideElement.TextElement)?.let {
                    viewModel.slideEngine.updateElement(it.copy(listType = TextListType.CHECKBOX))
                }
            }
            "cmd_add_quote" -> {
                viewModel.addTextElement()
                (viewModel.engineState.value.selectedElement as? SlideElement.TextElement)?.let {
                    viewModel.slideEngine.updateElement(it.copy(isBlockQuote = true))
                }
            }
            "cmd_add_image" -> onPickImageForSlide()
            "cmd_add_table" -> activeSheet = EditorSheet.TableCreate
            "cmd_add_chart_bar" -> viewModel.addChartElement(ChartType.BAR)
            "cmd_add_chart_pie" -> viewModel.addChartElement(ChartType.PIE)
            "cmd_add_chart_line" -> viewModel.addChartElement(ChartType.LINE)
            "cmd_add_chart_area" -> viewModel.addChartElement(ChartType.AREA)
            "cmd_add_equation" -> activeSheet = EditorSheet.Equations100
            "cmd_add_icon" -> activeSheet = EditorSheet.IconsLib
            "cmd_add_shape_rect" -> viewModel.addShapeElement(ShapeType.RECTANGLE)
            "cmd_add_shape_circle" -> viewModel.addShapeElement(ShapeType.CIRCLE)
            "cmd_add_shape_arrow" -> viewModel.addShapeElement(ShapeType.ARROW_RIGHT)
            "cmd_add_shape_star" -> viewModel.addShapeElement(ShapeType.STAR)
            "cmd_add_speaker_notes" -> activeSheet = EditorSheet.Notes
            "cmd_font_picker" -> activeSheet = EditorSheet.FontPicker
            "cmd_import_font" -> onPickCustomFont()
            "cmd_font_size_up" -> selectedText?.let {
                viewModel.slideEngine.updateElement(it.copy(fontSizeSp = (it.fontSizeSp + 4f).coerceAtMost(96f)))
            }
            "cmd_font_size_down" -> selectedText?.let {
                viewModel.slideEngine.updateElement(it.copy(fontSizeSp = (it.fontSizeSp - 4f).coerceAtLeast(12f)))
            }
            "cmd_toggle_bold" -> selectedText?.let { viewModel.slideEngine.updateElement(it.copy(isBold = !it.isBold)) }
            "cmd_toggle_italic" -> selectedText?.let { viewModel.slideEngine.updateElement(it.copy(isItalic = !it.isItalic)) }
            "cmd_toggle_underline" -> selectedText?.let { viewModel.slideEngine.updateElement(it.copy(isUnderline = !it.isUnderline)) }
            "cmd_toggle_strike" -> selectedText?.let { viewModel.slideEngine.updateElement(it.copy(isStrikethrough = !it.isStrikethrough)) }
            "cmd_dir_rtl" -> selectedText?.let { viewModel.slideEngine.updateElement(it.copy(writingDirection = WritingDirection.RTL)) }
            "cmd_dir_ltr" -> selectedText?.let { viewModel.slideEngine.updateElement(it.copy(writingDirection = WritingDirection.LTR)) }
            "cmd_dir_auto" -> selectedText?.let { viewModel.slideEngine.updateElement(it.copy(writingDirection = WritingDirection.AUTO)) }
            "cmd_text_align_start" -> selectedText?.let { viewModel.slideEngine.updateElement(it.copy(textAlign = TextAlignment.START)) }
            "cmd_text_align_center" -> selectedText?.let { viewModel.slideEngine.updateElement(it.copy(textAlign = TextAlignment.CENTER)) }
            "cmd_text_align_end" -> selectedText?.let { viewModel.slideEngine.updateElement(it.copy(textAlign = TextAlignment.END)) }
            "cmd_text_line_tight" -> selectedText?.let { viewModel.slideEngine.updateElement(it.copy(lineHeightMultiplier = 1.0f)) }
            "cmd_text_line_wide" -> selectedText?.let { viewModel.slideEngine.updateElement(it.copy(lineHeightMultiplier = 1.65f)) }
            "cmd_text_letter_wide" -> selectedText?.let { viewModel.slideEngine.updateElement(it.copy(letterSpacing = 3.5f)) }
            "cmd_ai_proofread" -> if (selectedText != null) {
                viewModel.slideEngine.selectElement(selectedText.id, false)
                viewModel.proofreadSelectedTextElement()
            } else {
                activeSheet = EditorSheet.Ai
            }
            "cmd_smartart_gallery" -> activeSheet = EditorSheet.SmartArtGallery
            "cmd_smartart_process" -> viewModel.addSmartArtElement(SmartArtLayoutType.PROCESS_ARROWS)
            "cmd_smartart_cycle" -> viewModel.addSmartArtElement(SmartArtLayoutType.CYCLE_RING)
            "cmd_smartart_hierarchy" -> viewModel.addSmartArtElement(SmartArtLayoutType.HIERARCHY_ORG)
            "cmd_smartart_pyramid" -> viewModel.addSmartArtElement(SmartArtLayoutType.PYRAMID_LEVELS)
            "cmd_smartart_venn" -> viewModel.addSmartArtElement(SmartArtLayoutType.VENN_OVERLAP)
            "cmd_smartart_matrix" -> viewModel.addSmartArtElement(SmartArtLayoutType.MATRIX_2X2)
            "cmd_smartart_funnel" -> viewModel.addSmartArtElement(SmartArtLayoutType.FUNNEL_STAGES)
            "cmd_smartart_radial" -> viewModel.addSmartArtElement(SmartArtLayoutType.RADIAL_HUB)
            "cmd_smartart_timeline" -> viewModel.addSmartArtElement(SmartArtLayoutType.TIMELINE_ROADMAP)
            "cmd_smartart_swot" -> viewModel.addSmartArtElement(SmartArtLayoutType.SWOT_GRID)
            "cmd_smartart_edit_colors" -> {
                val smartArt = (selectedEl as? SlideElement.SmartArtElement)
                    ?: activeSlide.elements.filterIsInstance<SlideElement.SmartArtElement>().firstOrNull()
                if (smartArt != null) {
                    activeSheet = EditorSheet.SmartArtEdit(smartArt)
                } else {
                    activeSheet = EditorSheet.SmartArtGallery
                }
            }
            "cmd_shapes_3d_gallery" -> activeSheet = EditorSheet.Shapes3D
            "cmd_3d_cube" -> viewModel.addShapeElement(ShapeType.CUBE_3D, is3D = true)
            "cmd_3d_cylinder" -> viewModel.addShapeElement(ShapeType.CYLINDER_3D, is3D = true)
            "cmd_3d_pyramid" -> viewModel.addShapeElement(ShapeType.PYRAMID_3D, is3D = true)
            "cmd_3d_sphere" -> viewModel.addShapeElement(ShapeType.SPHERE_3D, is3D = true)
            "cmd_3d_toggle" -> when (selectedEl) {
                is SlideElement.ShapeElement -> viewModel.slideEngine.updateElement(selectedEl.copy(is3D = !selectedEl.is3D))
                is SlideElement.SmartArtElement -> viewModel.slideEngine.updateElement(selectedEl.copy(is3D = !selectedEl.is3D))
                else -> {}
            }
            "cmd_3d_depth_plus" -> if (selectedEl is SlideElement.ShapeElement) {
                viewModel.slideEngine.updateElement(selectedEl.copy(is3D = true, depth3D = (selectedEl.depth3D + 8f).coerceAtMost(64f)))
            }
            "cmd_3d_depth_minus" -> if (selectedEl is SlideElement.ShapeElement) {
                viewModel.slideEngine.updateElement(selectedEl.copy(depth3D = (selectedEl.depth3D - 8f).coerceAtLeast(8f)))
            }
            "cmd_ornaments_gallery" -> activeSheet = EditorSheet.Ornaments100
            "cmd_bg_picker" -> activeSheet = EditorSheet.Background
            "cmd_bg_apply_all" -> viewModel.slideEngine.applyBackgroundToAllSlides(activeSlide.background)
            "cmd_ornament_apply_all" -> viewModel.slideEngine.applyDecorationToAllSlides(activeSlide.decorationId, activeSlide.decorationColorHex)
            "cmd_layouts_sheet" -> activeSheet = EditorSheet.Layouts
            "cmd_templates_gallery" -> onOpenDesignTemplates()
            "cmd_ratio_16_9" -> viewModel.slideEngine.updateCanvasAspectRatio(SlideAspectRatio.RATIO_16_9)
            "cmd_ratio_4_3" -> viewModel.slideEngine.updateCanvasAspectRatio(SlideAspectRatio.RATIO_4_3)
            "cmd_ratio_a4" -> viewModel.slideEngine.updateCanvasAspectRatio(SlideAspectRatio.A4)
            "cmd_ratio_square" -> viewModel.slideEngine.updateCanvasAspectRatio(SlideAspectRatio.SQUARE_1_1)
            "cmd_ratio_portrait" -> viewModel.slideEngine.updateCanvasAspectRatio(SlideAspectRatio.PORTRAIT_9_16)
            "cmd_grid_toggle" -> viewModel.setShowGrid(!engineState.showGrid)
            "cmd_margins_toggle" -> viewModel.setShowSafeMargins(!engineState.showSafeMargins)
            "cmd_smart_theme_ai" -> activeSheet = EditorSheet.Ai
            "cmd_motion_themes" -> activeSheet = EditorSheet.Motion100
            "cmd_tr_stealth" -> viewModel.slideEngine.updateSlideTransition(SlideTransition(SlideTransitionType.STEALTH, 320))
            "cmd_tr_exciting" -> viewModel.slideEngine.updateSlideTransition(SlideTransition(SlideTransitionType.EXCITING, 650))
            "cmd_tr_dynamic" -> viewModel.slideEngine.updateSlideTransition(SlideTransition(SlideTransitionType.DYNAMIC, 520))
            "cmd_tr_scientific" -> viewModel.slideEngine.updateSlideTransition(SlideTransition(SlideTransitionType.SCIENTIFIC, 450))
            "cmd_tr_academic" -> viewModel.slideEngine.updateSlideTransition(SlideTransition(SlideTransitionType.ACADEMIC, 480))
            "cmd_tr_apply_all" -> viewModel.slideEngine.applyTransitionToAllSlides(activeSlide.transition)
            "cmd_view_design" -> viewMode = SlideViewMode.DESIGN
            "cmd_view_outline" -> viewMode = SlideViewMode.OUTLINE
            "cmd_view_sorter" -> viewMode = SlideViewMode.SLIDE_SORTER
            "cmd_focus_mode" -> isFocusMode = true
            "cmd_present_start" -> onPresent(0)
            "cmd_present_current" -> onPresent(engineState.selectedSlideIndex)
            "cmd_zoom_in" -> viewModel.setCanvasZoom((engineState.canvasZoom + 0.15f).coerceAtMost(2.25f))
            "cmd_zoom_out" -> viewModel.setCanvasZoom((engineState.canvasZoom - 0.15f).coerceAtLeast(0.65f))
            "cmd_zoom_reset" -> viewModel.setCanvasZoom(1f)
            "cmd_add_slide" -> viewModel.slideEngine.addSlide()
            "cmd_duplicate_slide" -> viewModel.slideEngine.duplicateSlide()
            "cmd_auto_arrange" -> viewModel.autoArrangeActiveSlide()
            "cmd_bring_front" -> viewModel.slideEngine.bringToFront()
            "cmd_send_back" -> viewModel.slideEngine.sendToBack()
            "cmd_duplicate_el" -> viewModel.slideEngine.duplicateSelectedElements()
            "cmd_delete_el" -> viewModel.slideEngine.deleteSelectedElements()
            "cmd_stabilize_lock" -> viewModel.setCanvasStabilized(!engineState.isCanvasLocked)
            "cmd_slide_settings" -> activeSheet = EditorSheet.SlideSettings
            "cmd_pdf_preview" -> {
                viewModel.generatePdfPreviewPages()
                activeSheet = EditorSheet.PdfPreview
            }
            "cmd_export_pptx" -> onExportPptx()
            "cmd_export_mp4" -> activeSheet = EditorSheet.Export
            "cmd_export_html_ios" -> onExportHtml5Ios()
            "cmd_save_now" -> viewModel.saveActivePresentation()
            else -> activeSheet = EditorSheet.Tools100
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type != KeyEventType.KeyDown || activeSheet != null) {
                    return@onPreviewKeyEvent false
                }
                when {
                    keyEvent.isCtrlPressed && keyEvent.isShiftPressed && keyEvent.key == Key.Z -> {
                        viewModel.slideEngine.redo()
                        true
                    }
                    keyEvent.isCtrlPressed && keyEvent.key == Key.Z -> {
                        viewModel.slideEngine.undo()
                        true
                    }
                    keyEvent.isCtrlPressed && keyEvent.key == Key.Y -> {
                        viewModel.slideEngine.redo()
                        true
                    }
                    keyEvent.isCtrlPressed && keyEvent.key == Key.S -> {
                        viewModel.saveActivePresentation()
                        true
                    }
                    keyEvent.isCtrlPressed && keyEvent.key == Key.D -> {
                        viewModel.slideEngine.duplicateSelectedElements()
                        true
                    }
                    (keyEvent.key == Key.Delete || keyEvent.key == Key.Backspace) &&
                        engineState.selectedElementIds.isNotEmpty() -> {
                        viewModel.slideEngine.deleteSelectedElements()
                        true
                    }
                    keyEvent.key == Key.PageUp || keyEvent.key == Key.DirectionLeft -> {
                        val idx = engineState.selectedSlideIndex
                        if (idx > 0) viewModel.slideEngine.selectSlide(idx - 1)
                        true
                    }
                    keyEvent.key == Key.PageDown || keyEvent.key == Key.DirectionRight -> {
                        val idx = engineState.selectedSlideIndex
                        if (idx < presentation.slides.lastIndex) viewModel.slideEngine.selectSlide(idx + 1)
                        true
                    }
                    else -> false
                }
            },
        topBar = {
            if (!isFocusMode) {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("editor_back_btn")) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.cancel)
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = presentation.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (engineState.isDirty) "• ${strings.autosavedLabel}" else strings.savedSuccess,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (engineState.isDirty) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (batteryAutoSavePaused) {
                                    Icon(
                                        Icons.Rounded.BatterySaver,
                                        contentDescription = strings.batteryAutoSavePaused,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.slideEngine.undo() },
                            enabled = engineState.canUndo,
                            modifier = Modifier.testTag("editor_undo_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.Undo, contentDescription = strings.undo)
                        }
                        IconButton(
                            onClick = { viewModel.slideEngine.redo() },
                            enabled = engineState.canRedo,
                            modifier = Modifier.testTag("editor_redo_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.Redo, contentDescription = strings.redo)
                        }
                        IconButton(
                            onClick = { viewModel.setCanvasStabilized(!engineState.isCanvasLocked) },
                            modifier = Modifier.testTag("editor_stabilize_btn")
                        ) {
                            Icon(
                                imageVector = if (engineState.isCanvasLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                                contentDescription = strings.stabilizeScreen,
                                tint = if (engineState.isCanvasLocked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { isFocusMode = true },
                            modifier = Modifier.testTag("editor_focus_mode_btn")
                        ) {
                            Icon(Icons.Rounded.CenterFocusStrong, contentDescription = strings.focusMode)
                        }
                        IconButton(
                            onClick = { activeSheet = EditorSheet.SlideSettings },
                            modifier = Modifier.testTag("editor_slide_settings_btn")
                        ) {
                            Icon(Icons.Rounded.Tune, contentDescription = strings.slideSettings)
                        }
                        IconButton(
                            onClick = { viewModel.saveActivePresentation() },
                            modifier = Modifier.testTag("editor_save_btn")
                        ) {
                            Icon(Icons.Rounded.Save, contentDescription = strings.save)
                        }
                        Box {
                            IconButton(
                                onClick = { showPresentMenu = true },
                                modifier = Modifier.testTag("editor_present_btn")
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = strings.present)
                            }
                            DropdownMenu(
                                expanded = showPresentMenu,
                                onDismissRequest = { showPresentMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(strings.presentFromStart) },
                                    onClick = {
                                        showPresentMenu = false
                                        onPresent(0)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(strings.presentFromCurrent) },
                                    onClick = {
                                        showPresentMenu = false
                                        onPresent(engineState.selectedSlideIndex)
                                    }
                                )
                            }
                        }
                        IconButton(
                            onClick = { activeSheet = EditorSheet.Export },
                            modifier = Modifier.testTag("editor_export_btn")
                        ) {
                            Icon(Icons.Rounded.IosShare, contentDescription = strings.export)
                        }
                        IconButton(
                            onClick = { activeSheet = EditorSheet.Ai },
                            modifier = Modifier.testTag("editor_ai_btn")
                        ) {
                            Icon(
                                Icons.Rounded.AutoAwesome,
                                contentDescription = strings.aiAssistant,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isFocusMode) {
                // Distraction-Free Focus Mode: Clean slide canvas + minimal translucent floating bar
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(14.dp)
                ) {
                    SlideCanvas(
                        presentation = presentation,
                        slide = activeSlide,
                        selectedElementIds = emptySet(),
                        isEditable = false,
                        canvasZoom = engineState.canvasZoom,
                        onSelectElement = { _, _ -> },
                        onDoubleTapElement = {},
                        onStartTransformGesture = {},
                        onTransformDelta = { _, _, _, _, _, _ -> },
                        onCommitTransform = {},
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 64.dp)
                    )

                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        tonalElevation = 6.dp,
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .testTag("focus_mode_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    val idx = engineState.selectedSlideIndex
                                    if (idx > 0) viewModel.slideEngine.selectSlide(idx - 1)
                                },
                                enabled = engineState.selectedSlideIndex > 0,
                                modifier = Modifier.testTag("focus_prev_btn")
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowLeft, contentDescription = strings.prevSlide)
                            }
                            Text(
                                text = "${engineState.selectedSlideIndex + 1} / ${presentation.slides.size}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = {
                                    val idx = engineState.selectedSlideIndex
                                    if (idx < presentation.slides.lastIndex) viewModel.slideEngine.selectSlide(idx + 1)
                                },
                                enabled = engineState.selectedSlideIndex < presentation.slides.lastIndex,
                                modifier = Modifier.testTag("focus_next_btn")
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowRight, contentDescription = strings.nextSlide)
                            }
                            FilledTonalButton(
                                onClick = { isFocusMode = false },
                                modifier = Modifier.testTag("editor_exit_focus_btn")
                            ) {
                                Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(strings.exitFocusMode)
                            }
                        }
                    }
                }
            } else {
                val isWide = maxWidth >= 720.dp

                Column(modifier = Modifier.fillMaxSize()) {
                    // Non-overlapping Geometric Layout Alert Banner
                    AnimatedVisibility(visible = overlapCount > 0) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${strings.overlapDetectedBanner} ($overlapCount)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                FilledTonalButton(
                                    onClick = { viewModel.autoArrangeActiveSlide() },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("editor_auto_arrange_btn")
                                ) {
                                    Icon(Icons.Rounded.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(strings.autoArrangeAction, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }

                    if (isWide) {
                        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            SlideThumbnailsRail(
                                engineState = engineState,
                                isVertical = true,
                                onSelectSlide = { viewModel.slideEngine.selectSlide(it) },
                                onAddSlide = { viewModel.slideEngine.addSlide() },
                                onDuplicateSlide = { viewModel.slideEngine.duplicateSlide() },
                                onDeleteSlide = { viewModel.slideEngine.deleteSlide() },
                                onMoveSlide = { from, to -> viewModel.slideEngine.moveSlide(from, to) },
                                onMovePrev = {
                                    val idx = engineState.selectedSlideIndex
                                    if (idx > 0) viewModel.slideEngine.moveSlide(idx, idx - 1)
                                },
                                onMoveNext = {
                                    val idx = engineState.selectedSlideIndex
                                    if (idx < presentation.slides.lastIndex) viewModel.slideEngine.moveSlide(idx, idx + 1)
                                },
                                onOpenSlidesGrid = { activeSheet = EditorSheet.SlidesGrid },
                                modifier = Modifier
                                    .width(156.dp)
                                    .fillMaxHeight()
                            )
                            EditorCanvasWorkspace(
                                viewModel = viewModel,
                                engineState = engineState,
                                viewMode = viewMode,
                                onChangeViewMode = { viewMode = it },
                                isMultiSelectMode = isMultiSelectMode,
                                batteryAutoSavePaused = batteryAutoSavePaused,
                                onToggleMultiSelect = { isMultiSelectMode = !isMultiSelectMode },
                                onOpenSlidesGrid = { activeSheet = EditorSheet.SlidesGrid },
                                onDoubleTapElement = openEditorForElement,
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )
                        }
                    } else {
                        EditorCanvasWorkspace(
                            viewModel = viewModel,
                            engineState = engineState,
                            viewMode = viewMode,
                            onChangeViewMode = { viewMode = it },
                            isMultiSelectMode = isMultiSelectMode,
                            batteryAutoSavePaused = batteryAutoSavePaused,
                            onToggleMultiSelect = { isMultiSelectMode = !isMultiSelectMode },
                            onOpenSlidesGrid = { activeSheet = EditorSheet.SlidesGrid },
                            onDoubleTapElement = openEditorForElement,
                            modifier = Modifier.weight(1f).fillMaxWidth()
                        )
                        SlideThumbnailsRail(
                            engineState = engineState,
                            isVertical = false,
                            onSelectSlide = { viewModel.slideEngine.selectSlide(it) },
                            onAddSlide = { viewModel.slideEngine.addSlide() },
                            onDuplicateSlide = { viewModel.slideEngine.duplicateSlide() },
                            onDeleteSlide = { viewModel.slideEngine.deleteSlide() },
                            onMoveSlide = { from, to -> viewModel.slideEngine.moveSlide(from, to) },
                            onMovePrev = {
                                val idx = engineState.selectedSlideIndex
                                if (idx > 0) viewModel.slideEngine.moveSlide(idx, idx - 1)
                            },
                            onMoveNext = {
                                val idx = engineState.selectedSlideIndex
                                if (idx < presentation.slides.lastIndex) viewModel.slideEngine.moveSlide(idx, idx + 1)
                            },
                            onOpenSlidesGrid = { activeSheet = EditorSheet.SlidesGrid },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Contextual Properties Bar when element(s) selected
                    val selected = engineState.selectedElement
                    if (selected != null) {
                        ElementPropertiesBar(
                            element = selected,
                            selectedCount = engineState.selectedElementIds.size,
                            canPaste = engineState.clipboardElements.isNotEmpty(),
                            onEditSpecific = { openEditorForElement(selected) },
                            onOpenInspector = { activeSheet = EditorSheet.Inspector },
                            onUpdateOpacity = { op ->
                                viewModel.slideEngine.updateElement(selected.withTransform(opacity = op), recordUndo = false)
                            },
                            onUpdateImageCrop = { rect ->
                                if (selected is SlideElement.ImageElement) {
                                    viewModel.slideEngine.updateElement(selected.copy(cropRect = rect))
                                }
                            },
                            onToggleShadow = {
                                val updated = when (selected) {
                                    is SlideElement.TextElement -> selected.copy(hasShadow = !selected.hasShadow)
                                    is SlideElement.ImageElement -> selected.copy(hasShadow = !selected.hasShadow)
                                    is SlideElement.ShapeElement -> selected.copy(hasShadow = !selected.hasShadow)
                                    is SlideElement.SmartArtElement -> selected.copy(is3D = !selected.is3D)
                                    else -> selected
                                }
                                viewModel.slideEngine.updateElement(updated)
                            },
                            onBringFront = { viewModel.slideEngine.bringToFront() },
                            onSendBack = { viewModel.slideEngine.sendToBack() },
                            onDuplicate = { viewModel.slideEngine.duplicateSelectedElements() },
                            onCopy = { viewModel.slideEngine.copySelectedElements() },
                            onPaste = { viewModel.slideEngine.pasteClipboard() },
                            onGroup = { viewModel.slideEngine.groupSelected() },
                            onUngroup = { viewModel.slideEngine.ungroupSelected() },
                            onAlignCenterH = { viewModel.slideEngine.alignSelected(ElementAlignment.CENTER_H) },
                            onDelete = { viewModel.slideEngine.deleteSelectedElements() },
                            onOpenFontPicker = { activeSheet = EditorSheet.FontPicker },
                            onNudge = { dx, dy -> viewModel.slideEngine.nudgeSelectedElements(dx, dy) },
                            onResize = { dw, dh -> viewModel.slideEngine.resizeSelectedElements(dw, dh) },
                            onRotate = { deg -> viewModel.slideEngine.rotateSelectedElements(deg) }
                        )
                    }

                    HorizontalDivider()

                    // Expanded Bottom Tool Bar (including 100 Tools Ribbon, SmartArt, 3D Shapes, 100 Ornaments, 100 Equations, 100 Motion)
                    EditorBottomToolbar(
                        isCanvasLocked = engineState.isCanvasLocked,
                        onAll100Tools = { activeSheet = EditorSheet.Tools100 },
                        onAddText = { viewModel.addTextElement() },
                        onFontPicker = { activeSheet = EditorSheet.FontPicker },
                        onAddImage = onPickImageForSlide,
                        onSmartArt = { activeSheet = EditorSheet.SmartArtGallery },
                        onShapes = { activeSheet = EditorSheet.Shapes3D },
                        onOrnaments = { activeSheet = EditorSheet.Ornaments100 },
                        onTable = { activeSheet = EditorSheet.TableCreate },
                        onChart = { activeSheet = EditorSheet.ChartEdit(null) },
                        onIcons = { activeSheet = EditorSheet.IconsLib },
                        onEquation = { activeSheet = EditorSheet.Equations100 },
                        onLayouts = { activeSheet = EditorSheet.Layouts },
                        onBackground = { activeSheet = EditorSheet.Background },
                        onDesign = onOpenDesignTemplates,
                        onMotion = { activeSheet = EditorSheet.Motion100 },
                        onNotes = { activeSheet = EditorSheet.Notes },
                        onSlideSettings = { activeSheet = EditorSheet.SlideSettings },
                        onToggleStabilize = { viewModel.setCanvasStabilized(!engineState.isCanvasLocked) },
                        onFocusMode = { isFocusMode = true }
                    )
                }
            }
        }
    }

    // ---- Modal Sheets ----
    when (val sheet = activeSheet) {
        is EditorSheet.RichText -> {
            RichTextEditorSheet(
                element = sheet.element,
                presentationLanguage = presentation.language,
                customFonts = customFonts,
                onImportCustomFont = onPickCustomFont,
                onDeleteCustomFont = { viewModel.deleteCustomFont(it) },
                onDismiss = { activeSheet = null },
                onSaveElement = { updated -> viewModel.slideEngine.updateElement(updated) },
                onProofreadWithAi = {
                    viewModel.slideEngine.selectElement(sheet.element.id, false)
                    viewModel.proofreadSelectedTextElement()
                }
            )
        }
        EditorSheet.Shapes -> {
            ShapePickerSheet(
                onDismiss = { activeSheet = null },
                onSelectShape = { shapeType, fillHex -> viewModel.addShapeElement(shapeType, fillHex) }
            )
        }
        EditorSheet.Shapes3D -> {
            ShapesAnd3DPickerSheet(
                onDismiss = { activeSheet = null },
                onSelectShape = { shapeType, fillHex, is3D, depth3D ->
                    viewModel.addShapeElement(shapeType, fillHex, is3D, depth3D)
                }
            )
        }
        EditorSheet.SmartArtGallery -> {
            SmartArtGallerySheet(
                onDismiss = { activeSheet = null },
                onSelectSmartArt = { layout -> viewModel.addSmartArtElement(layout) }
            )
        }
        is EditorSheet.SmartArtEdit -> {
            val latestSmartArt = activeSlide.elements
                .filterIsInstance<SlideElement.SmartArtElement>()
                .firstOrNull { it.id == sheet.element.id } ?: sheet.element
            SmartArtEditorSheet(
                element = latestSmartArt,
                onDismiss = { activeSheet = null },
                onSaveElement = { updated -> viewModel.slideEngine.updateElement(updated) }
            )
        }
        EditorSheet.Ornaments100 -> {
            DecorationsAndColorsSheet(
                currentDecorationId = activeSlide.decorationId,
                currentDecorationColorHex = activeSlide.decorationColorHex,
                onApplyDecoration = { decoId, colorHex ->
                    viewModel.slideEngine.updateSlideDecoration(decoId, colorHex)
                },
                onApplyAllSlides = { decoId, colorHex ->
                    viewModel.slideEngine.applyDecorationToAllSlides(decoId, colorHex)
                },
                onDismiss = { activeSheet = null }
            )
        }
        EditorSheet.Motion100 -> {
            MotionChoreographySheet(
                currentTransition = activeSlide.transition,
                selectedAnimation = engineState.selectedElement?.animation,
                onApplyMotionTheme = { theme -> viewModel.slideEngine.applyMotionThemeToSlide(theme) },
                onApplyMotionThemeAllSlides = { theme -> viewModel.slideEngine.applyMotionThemeToAllSlides(theme) },
                onApplyManualAnimation = { tr, anim ->
                    viewModel.slideEngine.updateSlideTransition(tr)
                    val sel = engineState.selectedElement
                    if (sel != null && anim != null) {
                        viewModel.slideEngine.updateElement(sel.withTransform(animation = anim))
                    }
                },
                onDismiss = { activeSheet = null }
            )
        }
        EditorSheet.Equations100 -> {
            Equations100Sheet(
                onDismiss = { activeSheet = null },
                onInsertEquation = { expr -> viewModel.addEquationElement(expr) }
            )
        }
        EditorSheet.Tools100 -> {
            Tools100RibbonSheet(
                onDismiss = { activeSheet = null },
                onExecuteCommand = executeRibbonCommand
            )
        }
        EditorSheet.PdfPreview -> {
            PdfPreviewSheet(
                presentation = presentation,
                pages = pdfPreviewPages,
                operationState = operationState,
                onMovePage = { from, to -> viewModel.movePdfPreviewPage(from, to) },
                onResetOrder = { viewModel.resetPdfPreviewOrder() },
                onConfirmExport = { applyOrder -> viewModel.exportPreviewedPdf(applyOrder) },
                onDismiss = {
                    viewModel.clearPdfPreview()
                    activeSheet = null
                }
            )
        }
        EditorSheet.TableCreate -> {
            TableCreateSheet(
                onDismiss = { activeSheet = null },
                onCreateTable = { r, c -> viewModel.addTableElement(r, c) }
            )
        }
        is EditorSheet.TableEdit -> {
            val latestTable = activeSlide.elements
                .filterIsInstance<SlideElement.TableElement>()
                .firstOrNull { it.id == sheet.element.id } ?: sheet.element
            TableEditSheet(
                element = latestTable,
                onDismiss = { activeSheet = null },
                onUpdateTable = { updated -> viewModel.slideEngine.updateElement(updated) },
                onAddRow = { viewModel.slideEngine.addTableRow(latestTable.id) },
                onRemoveRow = { viewModel.slideEngine.removeTableRow(latestTable.id) },
                onAddCol = { viewModel.slideEngine.addTableCol(latestTable.id) },
                onRemoveCol = { viewModel.slideEngine.removeTableCol(latestTable.id) },
                onMergeRow = { rowIdx -> viewModel.slideEngine.mergeTableRowCells(latestTable.id, rowIdx) }
            )
        }
        is EditorSheet.ChartEdit -> {
            ChartEditorSheet(
                element = sheet.element,
                onDismiss = { activeSheet = null },
                onCreateOrUpdate = { chartType, title, labels, values ->
                    val existing = sheet.element
                    if (existing != null) {
                        val updatedSeries = existing.series.mapIndexed { idx, s ->
                            if (idx == 0) s.copy(values = values) else s
                        }.ifEmpty { listOf(ChartSeries("Data", "#1565C0", values)) }
                        viewModel.slideEngine.updateElement(
                            existing.copy(
                                chartType = chartType,
                                title = title,
                                labels = labels,
                                series = updatedSeries
                            )
                        )
                    } else {
                        viewModel.slideEngine.addElement(
                            SlideElement.ChartElement(
                                x = presentation.canvasWidth * 0.15f,
                                y = presentation.canvasHeight * 0.20f,
                                width = presentation.canvasWidth * 0.70f,
                                height = presentation.canvasHeight * 0.62f,
                                chartType = chartType,
                                title = title,
                                labels = labels,
                                series = listOf(ChartSeries("Series 1", "#1565C0", values))
                            )
                        )
                    }
                }
            )
        }
        is EditorSheet.EquationEdit -> {
            EquationEditorSheet(
                initialExpression = sheet.element?.expression ?: "\\frac{-b \\pm \\sqrt{b^2 - 4ac}}{2a}",
                onDismiss = { activeSheet = null },
                onConfirm = { expr ->
                    val existing = sheet.element
                    if (existing != null) {
                        viewModel.slideEngine.updateElement(existing.copy(expression = expr))
                    } else {
                        viewModel.addEquationElement(expr)
                    }
                }
            )
        }
        EditorSheet.IconsLib -> {
            IconsLibrarySheet(
                onDismiss = { activeSheet = null },
                onSelectIcon = { key -> viewModel.addIconElement(key) }
            )
        }
        EditorSheet.Background -> {
            BackgroundPickerSheet(
                onDismiss = { activeSheet = null },
                onSelectBackground = { bg -> viewModel.slideEngine.updateSlideBackground(bg) },
                onPickImageBackground = onPickImageForBackground,
                onApplyBackgroundToAll = { viewModel.slideEngine.applyBackgroundToAllSlides(activeSlide.background) }
            )
        }
        EditorSheet.Layouts -> {
            SlideLayoutPickerSheet(
                onDismiss = { activeSheet = null },
                onSelectPreset = { preset -> viewModel.addSlideWithLayout(preset) }
            )
        }
        EditorSheet.SlidesGrid -> {
            SlidesGridOverviewSheet(
                presentation = presentation,
                selectedSlideIndex = engineState.selectedSlideIndex,
                onDismiss = { activeSheet = null },
                onSelectSlide = { idx -> viewModel.slideEngine.selectSlide(idx) },
                onDuplicateSlide = { idx -> viewModel.slideEngine.duplicateSlide(idx) },
                onDeleteSlide = { idx -> viewModel.slideEngine.deleteSlide(idx) },
                onMoveSlide = { from, to -> viewModel.slideEngine.moveSlide(from, to) }
            )
        }
        EditorSheet.Inspector -> {
            val latestSelected = engineState.selectedElement
            if (latestSelected != null) {
                ElementInspectorSheet(
                    element = latestSelected,
                    onDismiss = { activeSheet = null },
                    onAlign = { alignment -> viewModel.slideEngine.alignSelected(alignment) },
                    onUpdateElement = { updated -> viewModel.slideEngine.updateElement(updated) }
                )
            } else {
                activeSheet = null
            }
        }
        EditorSheet.Motion -> {
            MotionAndTransitionsSheet(
                currentTransition = activeSlide.transition,
                selectedElement = engineState.selectedElement,
                onDismiss = { activeSheet = null },
                onUpdateTransition = { tr -> viewModel.slideEngine.updateSlideTransition(tr) },
                onUpdateElementAnimation = { anim ->
                    engineState.selectedElement?.let { el ->
                        viewModel.slideEngine.updateElement(el.withTransform(animation = anim))
                    }
                },
                onApplyTransitionToAll = { tr -> viewModel.slideEngine.applyTransitionToAllSlides(tr) }
            )
        }
        EditorSheet.Notes -> {
            SpeakerNotesSheet(
                initialNotes = activeSlide.speakerNotes,
                onDismiss = { activeSheet = null },
                onSaveNotes = { notes -> viewModel.slideEngine.updateSpeakerNotes(notes) },
                onGenerateWithAi = { viewModel.generateSpeakerNotesForActiveSlide() }
            )
        }
        EditorSheet.Export -> {
            ExportDeckSheet(
                operationState = operationState,
                onDismiss = { activeSheet = null },
                onExportPptx = onExportPptx,
                onExportPdf = onExportPdf,
                onPreviewPdf = {
                    viewModel.generatePdfPreviewPages()
                    activeSheet = EditorSheet.PdfPreview
                },
                onExportHtml5Ios = onExportHtml5Ios,
                onExportImages = onExportImages,
                onExportMp4 = onExportMp4,
                onPickVideoAudio = onPickVideoAudio,
                hasAttachedAudio = hasAttachedAudio
            )
        }
        EditorSheet.Ai -> {
            AiAssistantSheet(
                hasSelectedText = engineState.selectedElement is SlideElement.TextElement,
                designSuggestion = designSuggestion,
                smartTheme = smartTheme,
                deckSummary = deckSummary,
                onDismiss = { activeSheet = null },
                onGenerateDeck = { prompt, count, lang ->
                    viewModel.generatePresentationWithAi(prompt, count, lang, presentation.aspectRatio)
                },
                onSummarizeResearch = { text, count, lang ->
                    viewModel.summarizeResearchWithAi(text, count, lang)
                },
                onTranslateDeck = { targetLang ->
                    viewModel.translateActivePresentation(targetLang)
                },
                onProofreadSelected = {
                    viewModel.proofreadSelectedTextElement()
                },
                onGenerateSpeakerNotes = {
                    viewModel.generateSpeakerNotesForActiveSlide()
                },
                onGenerateIllustration = { prompt ->
                    viewModel.generateAiIllustrationOnSlide(prompt)
                },
                onAnalyzeSlideDesign = {
                    viewModel.analyzeActiveSlideDesign()
                },
                onApplyDesignSuggestion = {
                    viewModel.applySlideDesignSuggestion()
                },
                onSuggestSmartTheme = {
                    viewModel.requestSmartTheme()
                },
                onApplySmartTheme = {
                    viewModel.applySmartTheme()
                },
                onSummarizeDeck = {
                    viewModel.summarizePresentationDeck()
                }
            )
        }
        EditorSheet.SlideSettings -> {
            SlideSettingsSheet(
                presentation = presentation,
                activeSlideIndex = engineState.selectedSlideIndex,
                activeSlide = activeSlide,
                isCanvasLocked = engineState.isCanvasLocked,
                snapToGrid = engineState.snapToGrid,
                showGrid = engineState.showGrid,
                showSafeMargins = engineState.showSafeMargins,
                canvasZoom = engineState.canvasZoom,
                onDismiss = { activeSheet = null },
                onToggleCanvasLocked = { viewModel.setCanvasStabilized(it) },
                onToggleSnapToGrid = { viewModel.setSnapToGrid(it) },
                onToggleShowGrid = { viewModel.setShowGrid(it) },
                onToggleShowSafeMargins = { viewModel.setShowSafeMargins(it) },
                onZoomChange = { viewModel.setCanvasZoom(it) },
                onMoveSlideToPosition = { targetIdx ->
                    viewModel.slideEngine.moveSlide(engineState.selectedSlideIndex, targetIdx)
                },
                onUpdateAutoAdvanceSeconds = { sec ->
                    viewModel.slideEngine.updateSlideAutoAdvance(sec)
                },
                onUpdateAspectRatio = { ratio, cw, ch ->
                    viewModel.slideEngine.updateCanvasAspectRatio(ratio, cw, ch)
                },
                onApplyBackgroundToAll = {
                    viewModel.slideEngine.applyBackgroundToAllSlides(activeSlide.background)
                },
                onApplyTransitionToAll = {
                    viewModel.slideEngine.applyTransitionToAllSlides(activeSlide.transition)
                }
            )
        }
        EditorSheet.FontPicker -> {
            val selectedText = engineState.selectedElement as? SlideElement.TextElement
            QuickFontPickerSheet(
                selectedFontId = selectedText?.fontFamilyId,
                customFonts = customFonts,
                onSelectFont = { fontId ->
                    if (selectedText != null) {
                        viewModel.slideEngine.updateElement(selectedText.copy(fontFamilyId = fontId))
                    } else {
                        val firstText = activeSlide.elements.filterIsInstance<SlideElement.TextElement>().firstOrNull()
                        if (firstText != null) {
                            viewModel.slideEngine.updateElement(firstText.copy(fontFamilyId = fontId))
                        }
                    }
                },
                onImportCustomFont = onPickCustomFont,
                onDeleteCustomFont = { viewModel.deleteCustomFont(it) },
                onDismiss = { activeSheet = null }
            )
        }
        null -> {}
    }
}

@Composable
private fun EditorCanvasWorkspace(
    viewModel: KatebViewModel,
    engineState: SlideEngineState,
    viewMode: SlideViewMode,
    onChangeViewMode: (SlideViewMode) -> Unit,
    isMultiSelectMode: Boolean,
    batteryAutoSavePaused: Boolean,
    onToggleMultiSelect: () -> Unit,
    onOpenSlidesGrid: () -> Unit,
    onDoubleTapElement: (SlideElement) -> Unit,
    modifier: Modifier = Modifier
) {
    val presentation = engineState.presentation ?: return
    val activeSlide = engineState.activeSlide ?: return
    val strings = LocalAppStrings.current

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${engineState.selectedSlideIndex + 1} / ${presentation.slides.size} • ${presentation.aspectRatio.id}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(
                    onClick = onOpenSlidesGrid,
                    modifier = Modifier.size(36.dp).testTag("editor_grid_btn")
                ) {
                    Icon(
                        Icons.Rounded.ViewModule,
                        contentDescription = strings.slidesOverview,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                if (batteryAutoSavePaused) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = strings.batteryAutoSavePaused,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    item {
                        FilterChip(
                            selected = viewMode == SlideViewMode.DESIGN,
                            onClick = { onChangeViewMode(SlideViewMode.DESIGN) },
                            label = { Text(strings.viewDesign, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("view_mode_design")
                        )
                    }
                    item {
                        FilterChip(
                            selected = viewMode == SlideViewMode.OUTLINE,
                            onClick = { onChangeViewMode(SlideViewMode.OUTLINE) },
                            label = { Text(strings.viewOutline, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("view_mode_outline")
                        )
                    }
                    item {
                        FilterChip(
                            selected = viewMode == SlideViewMode.SLIDE_SORTER,
                            onClick = { onChangeViewMode(SlideViewMode.SLIDE_SORTER) },
                            label = { Text(strings.viewSorter, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("view_mode_sorter")
                        )
                    }
                }
                FilterChip(
                    selected = isMultiSelectMode,
                    onClick = onToggleMultiSelect,
                    leadingIcon = {
                        Icon(
                            if (isMultiSelectMode) Icons.Rounded.CheckBox else Icons.Rounded.CheckBoxOutlineBlank,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = {
                        Text(
                            if (engineState.selectedElementIds.size > 1) {
                                "${strings.group} (${engineState.selectedElementIds.size})"
                            } else strings.group,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    modifier = Modifier.testTag("editor_multiselect_chip")
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        when (viewMode) {
            SlideViewMode.DESIGN -> {
                // Live animated transition preview when switching slides in editor
                AnimatedContent(
                    targetState = activeSlide.id,
                    transitionSpec = {
                        val dur = activeSlide.transition.durationMs.coerceIn(150, 900)
                        when (activeSlide.transition.type) {
                            SlideTransitionType.NONE -> fadeIn(tween(1)) togetherWith fadeOut(tween(1))
                            SlideTransitionType.FADE -> fadeIn(tween(dur)) togetherWith fadeOut(tween(dur))
                            SlideTransitionType.PUSH ->
                                (slideInHorizontally(tween(dur)) { it / 2 } + fadeIn(tween(dur))) togetherWith
                                    (slideOutHorizontally(tween(dur)) { -it / 2 } + fadeOut(tween(dur)))
                            SlideTransitionType.WIPE ->
                                slideInHorizontally(tween(dur)) { it / 3 } + fadeIn(tween(dur)) togetherWith fadeOut(tween(dur))
                            SlideTransitionType.SPLIT, SlideTransitionType.ZOOM, SlideTransitionType.FLIP ->
                                (scaleIn(tween(dur), initialScale = 0.88f) + fadeIn(tween(dur))) togetherWith fadeOut(tween(dur))
                            SlideTransitionType.REVEAL, SlideTransitionType.SLIDE_UP ->
                                (slideInVertically(tween(dur)) { it / 4 } + fadeIn(tween(dur))) togetherWith
                                    (slideOutVertically(tween(dur)) { -it / 4 } + fadeOut(tween(dur)))
                            SlideTransitionType.SLIDE_DOWN ->
                                (slideInVertically(tween(dur)) { -it / 4 } + fadeIn(tween(dur))) togetherWith
                                    (slideOutVertically(tween(dur)) { it / 4 } + fadeOut(tween(dur)))
                            SlideTransitionType.DISSOLVE ->
                                fadeIn(tween((dur * 1.2f).toInt())) togetherWith fadeOut(tween((dur * 1.2f).toInt()))
                            SlideTransitionType.STEALTH ->
                                fadeIn(tween((dur * 0.7f).toInt().coerceAtLeast(120))) togetherWith
                                    fadeOut(tween((dur * 0.7f).toInt().coerceAtLeast(120)))
                            SlideTransitionType.EXCITING ->
                                (scaleIn(tween(dur), initialScale = 0.65f) + slideInHorizontally(tween(dur)) { it / 2 } + fadeIn(tween(dur))) togetherWith
                                    fadeOut(tween(dur / 2))
                            SlideTransitionType.DYNAMIC ->
                                (slideInHorizontally(tween(dur)) { it } + slideInVertically(tween(dur)) { it / 6 } + fadeIn(tween(dur))) togetherWith
                                    (slideOutHorizontally(tween(dur)) { -it / 2 } + fadeOut(tween(dur)))
                            SlideTransitionType.SCIENTIFIC ->
                                (scaleIn(tween(dur), initialScale = 0.92f) + fadeIn(tween(dur))) togetherWith fadeOut(tween(dur))
                            SlideTransitionType.ACADEMIC ->
                                (slideInVertically(tween(dur)) { it / 6 } + fadeIn(tween(dur))) togetherWith fadeOut(tween(dur))
                            SlideTransitionType.CURTAIN ->
                                (slideInHorizontally(tween(dur)) { -it / 2 } + scaleIn(tween(dur), initialScale = 0.9f) + fadeIn(tween(dur))) togetherWith
                                    (slideOutHorizontally(tween(dur)) { it / 2 } + fadeOut(tween(dur)))
                            SlideTransitionType.MORPH ->
                                (scaleIn(tween(dur), initialScale = 0.95f) + fadeIn(tween(dur))) togetherWith fadeOut(tween(dur))
                        }
                    },
                    label = "editor_slide_transition",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { targetSlideId ->
                    val slideToRender = presentation.slides.firstOrNull { it.id == targetSlideId } ?: activeSlide
                    SlideCanvas(
                        presentation = presentation,
                        slide = slideToRender,
                        selectedElementIds = engineState.selectedElementIds,
                        isEditable = true,
                        isMultiSelectMode = isMultiSelectMode,
                        isCanvasLocked = engineState.isCanvasLocked,
                        snapToGrid = engineState.snapToGrid,
                        showGrid = engineState.showGrid,
                        showSafeMargins = engineState.showSafeMargins,
                        canvasZoom = engineState.canvasZoom,
                        onSelectElement = { id, multi -> viewModel.slideEngine.selectElement(id, multi) },
                        onDoubleTapElement = onDoubleTapElement,
                        onStartTransformGesture = {},
                        onTransformDelta = { id, dx, dy, newW, newH, newRot ->
                            viewModel.slideEngine.previewTransformElement(id, dx, dy, newW, newH, newRot)
                        },
                        onCommitTransform = { before ->
                            viewModel.slideEngine.pushTransformUndoSnapshot(before, engineState.selectedSlideIndex)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            SlideViewMode.OUTLINE -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("editor_outline_view")
                ) {
                    itemsIndexed(presentation.slides, key = { _, s -> s.id }) { idx, slide ->
                        val isSel = idx == engineState.selectedSlideIndex
                        val firstText = slide.elements.filterIsInstance<SlideElement.TextElement>().firstOrNull()?.text.orEmpty()
                        val bodyTexts = slide.elements.filterIsInstance<SlideElement.TextElement>().drop(1).joinToString("\n") { it.text }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                            else MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isSel) 2.dp else 1.dp,
                                    color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.slideEngine.selectSlide(idx)
                                    onChangeViewMode(SlideViewMode.DESIGN)
                                }
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "#${idx + 1} • ${firstText.ifBlank { strings.slidesOverview }}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = slide.transition.type.label(LocalAppLanguage.current),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                if (bodyTexts.isNotBlank()) {
                                    Text(
                                        text = bodyTexts,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (slide.speakerNotes.isNotBlank()) {
                                    Text(
                                        text = "${strings.toolNotes}: ${slide.speakerNotes}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
            SlideViewMode.SLIDE_SORTER -> {
                val ratio = (presentation.canvasWidth / presentation.canvasHeight).coerceIn(0.7f, 2.0f)
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 156.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("editor_sorter_view")
                ) {
                    itemsIndexed(presentation.slides, key = { _, s -> s.id }) { idx, slide ->
                        val isSel = idx == engineState.selectedSlideIndex
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isSel) 2.dp else 1.dp,
                                    color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.slideEngine.selectSlide(idx)
                                    onChangeViewMode(SlideViewMode.DESIGN)
                                }
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
                                    Text("#${idx + 1}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                    Row {
                                        IconButton(
                                            onClick = { if (idx > 0) viewModel.slideEngine.moveSlide(idx, idx - 1) },
                                            enabled = idx > 0,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.AutoMirrored.Rounded.ArrowLeft, contentDescription = strings.moveSlideUp, modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(
                                            onClick = { if (idx < presentation.slides.lastIndex) viewModel.slideEngine.moveSlide(idx, idx + 1) },
                                            enabled = idx < presentation.slides.lastIndex,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.AutoMirrored.Rounded.ArrowRight, contentDescription = strings.moveSlideDown, modifier = Modifier.size(16.dp))
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
}

@Composable
private fun SlideThumbnailsRail(
    engineState: SlideEngineState,
    isVertical: Boolean,
    onSelectSlide: (Int) -> Unit,
    onAddSlide: () -> Unit,
    onDuplicateSlide: () -> Unit,
    onDeleteSlide: () -> Unit,
    onMoveSlide: (Int, Int) -> Unit,
    onMovePrev: () -> Unit,
    onMoveNext: () -> Unit,
    onOpenSlidesGrid: () -> Unit,
    modifier: Modifier = Modifier
) {
    val presentation = engineState.presentation ?: return
    val strings = LocalAppStrings.current
    val activeIdx = engineState.selectedSlideIndex
    val ratio = (presentation.canvasWidth / presentation.canvasHeight).coerceIn(0.6f, 2.2f)

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = modifier
    ) {
        if (isVertical) {
            Column(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val canAddMoreSlides = presentation.slides.size < MAX_SLIDES_PER_DECK
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onAddSlide, enabled = canAddMoreSlides, modifier = Modifier.size(36.dp).testTag("add_slide_btn")) {
                        Icon(Icons.Rounded.Add, contentDescription = strings.addSlide)
                    }
                    IconButton(onClick = onDuplicateSlide, enabled = canAddMoreSlides, modifier = Modifier.size(36.dp).testTag("dup_slide_btn")) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = strings.duplicateSlide, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = onDeleteSlide,
                        enabled = presentation.slides.size > 1,
                        modifier = Modifier.size(36.dp).testTag("del_slide_btn")
                    ) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.deleteSlide, modifier = Modifier.size(18.dp))
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = onMovePrev, enabled = activeIdx > 0, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.ArrowUpward, contentDescription = strings.moveSlideUp, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onMoveNext, enabled = activeIdx < presentation.slides.lastIndex, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.ArrowDownward, contentDescription = strings.moveSlideDown, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onOpenSlidesGrid, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.ViewModule, contentDescription = strings.slidesOverview, modifier = Modifier.size(16.dp))
                    }
                }
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    itemsIndexed(presentation.slides, key = { _, s -> s.id }) { idx, slide ->
                        SlideThumbnailItem(
                            index = idx,
                            isSelected = idx == activeIdx,
                            isVerticalRail = true,
                            totalSlides = presentation.slides.size,
                            aspectRatio = ratio,
                            onClick = { onSelectSlide(idx) },
                            onDragReorder = { from, to -> onMoveSlide(from, to) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SlideMiniPreview(
                                presentation = presentation,
                                slide = slide,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                val canAddMoreSlides = presentation.slides.size < MAX_SLIDES_PER_DECK
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    IconButton(onClick = onAddSlide, enabled = canAddMoreSlides, modifier = Modifier.testTag("add_slide_btn")) {
                        Icon(Icons.Rounded.Add, contentDescription = strings.addSlide, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(Modifier.width(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(presentation.slides, key = { _, s -> s.id }) { idx, slide ->
                        SlideThumbnailItem(
                            index = idx,
                            isSelected = idx == activeIdx,
                            isVerticalRail = false,
                            totalSlides = presentation.slides.size,
                            aspectRatio = ratio,
                            onClick = { onSelectSlide(idx) },
                            onDragReorder = { from, to -> onMoveSlide(from, to) },
                            modifier = Modifier.height(54.dp)
                        ) {
                            SlideMiniPreview(
                                presentation = presentation,
                                slide = slide,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
                IconButton(onClick = onMovePrev, enabled = activeIdx > 0, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowLeft, contentDescription = strings.moveSlideUp)
                }
                IconButton(onClick = onMoveNext, enabled = activeIdx < presentation.slides.lastIndex, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowRight, contentDescription = strings.moveSlideDown)
                }
                IconButton(onClick = onDuplicateSlide, enabled = canAddMoreSlides, modifier = Modifier.size(34.dp).testTag("dup_slide_btn")) {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = strings.duplicateSlide, modifier = Modifier.size(18.dp))
                }
                IconButton(
                    onClick = onDeleteSlide,
                    enabled = presentation.slides.size > 1,
                    modifier = Modifier.size(34.dp).testTag("del_slide_btn")
                ) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.deleteSlide, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun SlideThumbnailItem(
    index: Int,
    isSelected: Boolean,
    isVerticalRail: Boolean,
    totalSlides: Int,
    aspectRatio: Float,
    onClick: () -> Unit,
    onDragReorder: (fromIndex: Int, toIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var dragAccumulator by remember(index) { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .pointerInput(index, totalSlides) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { dragAccumulator = 0f },
                    onDragEnd = { dragAccumulator = 0f },
                    onDragCancel = { dragAccumulator = 0f },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragAccumulator += if (isVerticalRail) dragAmount.y else dragAmount.x
                        val threshold = 72f
                        if (dragAccumulator > threshold && index < totalSlides - 1) {
                            onDragReorder(index, index + 1)
                            dragAccumulator = 0f
                        } else if (dragAccumulator < -threshold && index > 0) {
                            onDragReorder(index, index - 1)
                            dragAccumulator = 0f
                        }
                    }
                )
            }
            .testTag("slide_thumb_$index")
    ) {
        content()
        Surface(
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.6f),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(3.dp)
        ) {
            Text(
                text = "${index + 1}",
                fontSize = 10.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }
    }
}

@Composable
private fun EditorBottomToolbar(
    isCanvasLocked: Boolean,
    onAll100Tools: () -> Unit,
    onAddText: () -> Unit,
    onFontPicker: () -> Unit,
    onAddImage: () -> Unit,
    onSmartArt: () -> Unit,
    onShapes: () -> Unit,
    onOrnaments: () -> Unit,
    onTable: () -> Unit,
    onChart: () -> Unit,
    onIcons: () -> Unit,
    onEquation: () -> Unit,
    onLayouts: () -> Unit,
    onBackground: () -> Unit,
    onDesign: () -> Unit,
    onMotion: () -> Unit,
    onNotes: () -> Unit,
    onSlideSettings: () -> Unit,
    onToggleStabilize: () -> Unit,
    onFocusMode: () -> Unit
) {
    val strings = LocalAppStrings.current
    Surface(color = MaterialTheme.colorScheme.surface) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item { ToolActionItem(Icons.Rounded.Apps, strings.tools100Title, "tool_all_100", onAll100Tools) }
            item { ToolActionItem(Icons.Rounded.TextFields, strings.toolText, "tool_text", onAddText) }
            item { ToolActionItem(Icons.Rounded.FontDownload, strings.quickFontTool, "tool_font", onFontPicker) }
            item { ToolActionItem(Icons.Rounded.AccountTree, strings.smartArtTool, "tool_smartart", onSmartArt) }
            item { ToolActionItem(Icons.Rounded.ViewInAr, strings.toolShapes, "tool_shapes", onShapes) }
            item { ToolActionItem(Icons.Rounded.Image, strings.toolImage, "tool_image", onAddImage) }
            item { ToolActionItem(Icons.Rounded.TableChart, strings.toolTable, "tool_table", onTable) }
            item { ToolActionItem(Icons.Rounded.BarChart, strings.toolChart, "tool_chart", onChart) }
            item { ToolActionItem(Icons.Rounded.EmojiSymbols, strings.toolIcons, "tool_icons", onIcons) }
            item { ToolActionItem(Icons.Rounded.Functions, strings.toolEquation, "tool_equation", onEquation) }
            item { ToolActionItem(Icons.Rounded.AutoFixHigh, strings.ornaments100Title, "tool_ornaments", onOrnaments) }
            item { ToolActionItem(Icons.Rounded.DashboardCustomize, strings.toolLayout, "tool_layout", onLayouts) }
            item { ToolActionItem(Icons.Rounded.Palette, strings.toolBackground, "tool_background", onBackground) }
            item { ToolActionItem(Icons.Rounded.GridView, strings.toolDesign, "tool_design", onDesign) }
            item { ToolActionItem(Icons.Rounded.Animation, strings.toolAnimation, "tool_motion", onMotion) }
            item { ToolActionItem(Icons.Rounded.Description, strings.toolNotes, "tool_notes", onNotes) }
            item { ToolActionItem(Icons.Rounded.Tune, strings.slideSettings, "tool_slide_settings", onSlideSettings) }
            item {
                ToolActionItem(
                    if (isCanvasLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                    strings.stabilizeShort,
                    "tool_stabilize",
                    onToggleStabilize
                )
            }
            item { ToolActionItem(Icons.Rounded.CenterFocusStrong, strings.focusMode, "tool_focus", onFocusMode) }
        }
    }
}

@Composable
private fun ToolActionItem(
    icon: ImageVector,
    label: String,
    tag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(tag)
    ) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
}
