package com.example.engine

import com.example.core.localization.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class ElementAlignment { LEFT, CENTER_H, RIGHT, TOP, CENTER_V, BOTTOM }

data class SlideEngineState(
    val presentation: Presentation? = null,
    val selectedSlideIndex: Int = 0,
    val selectedElementIds: Set<String> = emptySet(),
    val clipboardElements: List<SlideElement> = emptyList(),
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isDirty: Boolean = false,
    val isCanvasLocked: Boolean = false,
    val snapToGrid: Boolean = true,
    val showGrid: Boolean = false,
    val showSafeMargins: Boolean = false,
    val canvasZoom: Float = 1f
) {
    val activeSlide: Slide?
        get() = presentation?.slides?.getOrNull(selectedSlideIndex)

    val selectedElement: SlideElement?
        get() = activeSlide?.elements?.firstOrNull { it.id in selectedElementIds }
}

class SlideEngine {
    private val history = PresentationCaretaker()
    private val _state = MutableStateFlow(SlideEngineState())
    val state: StateFlow<SlideEngineState> = _state.asStateFlow()

    fun setCanvasLocked(locked: Boolean) {
        _state.value = _state.value.copy(isCanvasLocked = locked)
    }

    fun setSnapToGrid(enabled: Boolean) {
        _state.value = _state.value.copy(snapToGrid = enabled)
    }

    fun setShowGrid(visible: Boolean) {
        _state.value = _state.value.copy(showGrid = visible)
    }

    fun setShowSafeMargins(visible: Boolean) {
        _state.value = _state.value.copy(showSafeMargins = visible)
    }

    fun setCanvasZoom(zoom: Float) {
        _state.value = _state.value.copy(canvasZoom = zoom.coerceIn(0.65f, 2.25f))
    }

    fun loadPresentation(presentation: Presentation) {
        history.clear()
        val normalized = presentation.copy(
            slides = presentation.slides.ifEmpty {
                listOf(createDefaultSlide(0, presentation.language, presentation.canvasWidth, presentation.canvasHeight))
            }.take(MAX_SLIDES_PER_DECK).mapIndexed { idx, s -> s.copy(order = idx) }
        )
        _state.value = _state.value.copy(
            presentation = normalized,
            selectedSlideIndex = 0,
            selectedElementIds = emptySet(),
            canUndo = false,
            canRedo = false,
            isDirty = false,
            canvasZoom = 1f
        )
    }

    fun replacePresentation(newPresentation: Presentation, recordUndo: Boolean = true) {
        val current = _state.value.presentation ?: return
        if (recordUndo) {
            history.saveMemento(PresentationMemento(current, _state.value.selectedSlideIndex))
        }
        val clampedIndex = _state.value.selectedSlideIndex
            .coerceIn(0, (newPresentation.slides.size - 1).coerceAtLeast(0))
        _state.value = _state.value.copy(
            presentation = newPresentation.copy(updatedAt = System.currentTimeMillis()),
            selectedSlideIndex = clampedIndex,
            selectedElementIds = emptySet(),
            canUndo = history.canUndo,
            canRedo = history.canRedo,
            isDirty = true
        )
    }

    fun markSaved() {
        _state.value = _state.value.copy(isDirty = false)
    }

    fun selectSlide(index: Int) {
        val pres = _state.value.presentation ?: return
        if (index in pres.slides.indices) {
            _state.value = _state.value.copy(
                selectedSlideIndex = index,
                selectedElementIds = emptySet()
            )
        }
    }

    fun selectElement(elementId: String?, multiSelect: Boolean = false) {
        val slide = _state.value.activeSlide ?: return
        if (elementId == null) {
            _state.value = _state.value.copy(selectedElementIds = emptySet())
            return
        }
        val tapped = slide.elements.firstOrNull { it.id == elementId } ?: return
        val groupIds = if (tapped.groupId != null) {
            slide.elements.filter { it.groupId == tapped.groupId }.map { it.id }.toSet()
        } else {
            setOf(elementId)
        }
        val updated = if (multiSelect) {
            val current = _state.value.selectedElementIds
            if (elementId in current) current - groupIds else current + groupIds
        } else {
            groupIds
        }
        _state.value = _state.value.copy(selectedElementIds = updated)
    }

    fun addSlide(afterIndex: Int = _state.value.selectedSlideIndex) {
        mutate { pres ->
            if (pres.slides.size >= MAX_SLIDES_PER_DECK) return@mutate pres to _state.value.selectedSlideIndex
            val insertAt = (afterIndex + 1).coerceIn(0, pres.slides.size)
            val newSlide = createDefaultSlide(
                order = insertAt,
                language = pres.language,
                canvasWidth = pres.canvasWidth,
                canvasHeight = pres.canvasHeight,
                templateBackground = pres.slides.getOrNull(_state.value.selectedSlideIndex)?.background
                    ?: SlideBackground.Solid("#FFFFFF")
            )
            val updatedSlides = pres.slides.toMutableList().apply {
                add(insertAt, newSlide)
            }.mapIndexed { idx, s -> s.copy(order = idx) }
            pres.copy(slides = updatedSlides) to insertAt
        }
    }

    fun insertSlide(newSlide: Slide, afterIndex: Int = _state.value.selectedSlideIndex) {
        mutate { pres ->
            if (pres.slides.size >= MAX_SLIDES_PER_DECK) return@mutate pres to _state.value.selectedSlideIndex
            val insertAt = (afterIndex + 1).coerceIn(0, pres.slides.size)
            val updatedSlides = pres.slides.toMutableList().apply {
                add(insertAt, newSlide.copy(order = insertAt))
            }.mapIndexed { idx, s -> s.copy(order = idx) }
            pres.copy(slides = updatedSlides) to insertAt
        }
    }

    fun duplicateSlide(index: Int = _state.value.selectedSlideIndex) {
        mutate { pres ->
            if (pres.slides.size >= MAX_SLIDES_PER_DECK) return@mutate pres to index
            val target = pres.slides.getOrNull(index) ?: return@mutate pres to index
            val copySlide = target.copy(
                id = UUID.randomUUID().toString(),
                elements = target.elements.map { it.withNewId() }
            )
            val insertAt = (index + 1).coerceIn(0, pres.slides.size)
            val updated = pres.slides.toMutableList().apply {
                add(insertAt, copySlide)
            }.mapIndexed { i, s -> s.copy(order = i) }
            pres.copy(slides = updated) to insertAt
        }
    }

    fun deleteSlide(index: Int = _state.value.selectedSlideIndex) {
        mutate { pres ->
            if (pres.slides.size <= 1) return@mutate pres to 0
            val updated = pres.slides.toMutableList().apply {
                removeAt(index.coerceIn(0, size - 1))
            }.mapIndexed { i, s -> s.copy(order = i) }
            val nextIdx = index.coerceIn(0, updated.size - 1)
            pres.copy(slides = updated) to nextIdx
        }
    }

    fun moveSlide(fromIndex: Int, toIndex: Int) {
        mutate { pres ->
            if (fromIndex !in pres.slides.indices || toIndex !in pres.slides.indices || fromIndex == toIndex) {
                return@mutate pres to _state.value.selectedSlideIndex
            }
            val list = pres.slides.toMutableList()
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            val reindexed = list.mapIndexed { idx, s -> s.copy(order = idx) }
            pres.copy(slides = reindexed) to toIndex
        }
    }

    fun reorderSlidesByIds(orderedSlideIds: List<String>) {
        mutate { pres ->
            if (orderedSlideIds.isEmpty()) return@mutate pres to _state.value.selectedSlideIndex
            val byId = pres.slides.associateBy { it.id }
            val ordered = orderedSlideIds.mapNotNull { byId[it] }
            val remaining = pres.slides.filterNot { it.id in orderedSlideIds.toSet() }
            val combined = (ordered + remaining).mapIndexed { idx, s -> s.copy(order = idx) }
            val activeId = pres.slides.getOrNull(_state.value.selectedSlideIndex)?.id
            val nextIdx = combined.indexOfFirst { it.id == activeId }.takeIf { it >= 0 } ?: 0
            pres.copy(slides = combined) to nextIdx
        }
    }

    fun updateSlideBackground(background: SlideBackground) {
        mutateActiveSlide { slide -> slide.copy(background = background) }
    }

    fun applyBackgroundToAllSlides(background: SlideBackground) {
        mutate { pres ->
            val updated = pres.slides.map { it.copy(background = background) }
            pres.copy(slides = updated) to _state.value.selectedSlideIndex
        }
    }

    fun updateSlideDecoration(decorationId: String, colorHex: String) {
        mutateActiveSlide { slide ->
            slide.copy(decorationId = decorationId, decorationColorHex = colorHex)
        }
    }

    fun applyDecorationToAllSlides(decorationId: String, colorHex: String) {
        mutate { pres ->
            val updated = pres.slides.map {
                it.copy(decorationId = decorationId, decorationColorHex = colorHex)
            }
            pres.copy(slides = updated) to _state.value.selectedSlideIndex
        }
    }

    fun updateSlideTransition(transition: SlideTransition) {
        mutateActiveSlide { slide -> slide.copy(transition = transition) }
    }

    fun applyTransitionToAllSlides(transition: SlideTransition) {
        mutate { pres ->
            val updated = pres.slides.map { it.copy(transition = transition) }
            pres.copy(slides = updated) to _state.value.selectedSlideIndex
        }
    }

    fun applyMotionThemeToSlide(theme: MotionThemeSpec) {
        mutateActiveSlide { slide ->
            val updatedEls = slide.elements.mapIndexed { idx, el ->
                el.withTransform(
                    animation = ElementAnimation(
                        type = theme.entranceType,
                        exitType = theme.exitType,
                        direction = theme.direction,
                        motionRange = theme.motionRange,
                        durationMs = theme.durationMs,
                        delayMs = idx * theme.staggerMs,
                        orderIndex = idx
                    )
                )
            }
            slide.copy(
                transition = SlideTransition(theme.transitionType, theme.durationMs),
                elements = updatedEls
            )
        }
    }

    fun applyMotionThemeToAllSlides(theme: MotionThemeSpec) {
        mutate { pres ->
            val updatedSlides = pres.slides.map { slide ->
                val updatedEls = slide.elements.mapIndexed { idx, el ->
                    el.withTransform(
                        animation = ElementAnimation(
                            type = theme.entranceType,
                            exitType = theme.exitType,
                            direction = theme.direction,
                            motionRange = theme.motionRange,
                            durationMs = theme.durationMs,
                            delayMs = idx * theme.staggerMs,
                            orderIndex = idx
                        )
                    )
                }
                slide.copy(
                    transition = SlideTransition(theme.transitionType, theme.durationMs),
                    elements = updatedEls
                )
            }
            pres.copy(slides = updatedSlides) to _state.value.selectedSlideIndex
        }
    }

    fun updateSmartArtNodeColor(elementId: String, nodeIndex: Int, fillColorHex: String) {
        val smartArt = _state.value.activeSlide?.elements
            ?.filterIsInstance<SlideElement.SmartArtElement>()
            ?.firstOrNull { it.id == elementId } ?: return
        if (nodeIndex !in smartArt.nodes.indices) return
        val updatedNodes = smartArt.nodes.mapIndexed { idx, node ->
            if (idx == nodeIndex) node.copy(fillColorHex = fillColorHex) else node
        }
        updateElement(smartArt.copy(nodes = updatedNodes))
    }

    fun updateSmartArtNodeText(elementId: String, nodeIndex: Int, text: String) {
        val smartArt = _state.value.activeSlide?.elements
            ?.filterIsInstance<SlideElement.SmartArtElement>()
            ?.firstOrNull { it.id == elementId } ?: return
        if (nodeIndex !in smartArt.nodes.indices) return
        val updatedNodes = smartArt.nodes.mapIndexed { idx, node ->
            if (idx == nodeIndex) node.copy(text = text) else node
        }
        updateElement(smartArt.copy(nodes = updatedNodes))
    }

    fun updateSlideAutoAdvance(seconds: Int) {
        val clamped = seconds.coerceIn(1, 60)
        mutateActiveSlide { slide -> slide.copy(autoAdvanceSeconds = clamped) }
    }

    fun updateCanvasAspectRatio(
        aspectRatio: SlideAspectRatio,
        customWidth: Float? = null,
        customHeight: Float? = null
    ) {
        mutate { pres ->
            val targetW = (if (aspectRatio == SlideAspectRatio.CUSTOM) customWidth ?: pres.canvasWidth else aspectRatio.defaultWidth)
                .coerceIn(640f, 3840f)
            val targetH = (if (aspectRatio == SlideAspectRatio.CUSTOM) customHeight ?: pres.canvasHeight else aspectRatio.defaultHeight)
                .coerceIn(480f, 3840f)
            val scaleX = targetW / pres.canvasWidth.coerceAtLeast(1f)
            val scaleY = targetH / pres.canvasHeight.coerceAtLeast(1f)
            val scaledSlides = pres.slides.map { slide ->
                slide.copy(
                    elements = slide.elements.map { el ->
                        el.withTransform(
                            x = el.x * scaleX,
                            y = el.y * scaleY,
                            width = (el.width * scaleX).coerceAtLeast(48f),
                            height = (el.height * scaleY).coerceAtLeast(40f)
                        )
                    }
                )
            }
            pres.copy(
                aspectRatio = aspectRatio,
                canvasWidth = targetW,
                canvasHeight = targetH,
                slides = scaledSlides
            ) to _state.value.selectedSlideIndex
        }
    }

    fun updateSpeakerNotes(notes: String) {
        mutateActiveSlide { slide -> slide.copy(speakerNotes = notes) }
    }

    fun nudgeSelectedElements(dx: Float, dy: Float) {
        val pres = _state.value.presentation ?: return
        val selected = _state.value.selectedElementIds
        if (selected.isEmpty()) return
        mutateActiveSlide { slide ->
            slide.copy(elements = slide.elements.map { el ->
                if (el.id in selected) {
                    el.withTransform(
                        x = (el.x + dx).coerceIn(-el.width * 0.5f, pres.canvasWidth - 40f),
                        y = (el.y + dy).coerceIn(-el.height * 0.5f, pres.canvasHeight - 40f)
                    )
                } else el
            })
        }
    }

    fun resizeSelectedElements(dw: Float, dh: Float) {
        val pres = _state.value.presentation ?: return
        val selected = _state.value.selectedElementIds
        if (selected.isEmpty()) return
        mutateActiveSlide { slide ->
            slide.copy(elements = slide.elements.map { el ->
                if (el.id in selected) {
                    el.withTransform(
                        width = (el.width + dw).coerceIn(48f, pres.canvasWidth * 1.5f),
                        height = (el.height + dh).coerceIn(40f, pres.canvasHeight * 1.5f)
                    )
                } else el
            })
        }
    }

    fun rotateSelectedElements(deltaDeg: Float) {
        val selected = _state.value.selectedElementIds
        if (selected.isEmpty()) return
        mutateActiveSlide { slide ->
            slide.copy(elements = slide.elements.map { el ->
                if (el.id in selected) {
                    el.withTransform(rotation = ((el.rotation + deltaDeg) % 360f))
                } else el
            })
        }
    }

    fun addElement(element: SlideElement, useSmartPlacement: Boolean = true) {
        val pres = _state.value.presentation ?: return
        val slide = _state.value.activeSlide ?: return
        val nextZ = (slide.elements.maxOfOrNull { it.zIndex } ?: 0) + 1
        val smartPositioned = if (useSmartPlacement) {
            SmartLayoutGeometryService.suggestPlacement(
                newElement = element,
                existingElements = slide.elements,
                canvasW = pres.canvasWidth,
                canvasH = pres.canvasHeight
            )
        } else {
            element
        }
        val placed = smartPositioned.withTransform(zIndex = nextZ)
        mutateActiveSlide(newSelectedIds = setOf(placed.id)) { s ->
            s.copy(elements = s.elements + placed)
        }
    }

    fun autoArrangeActiveSlide() {
        val pres = _state.value.presentation ?: return
        mutateActiveSlide { slide ->
            SmartLayoutGeometryService.autoArrangeSlide(slide, pres.canvasWidth, pres.canvasHeight)
        }
    }

    fun updateElement(updated: SlideElement, recordUndo: Boolean = true) {
        mutateActiveSlide(recordUndo = recordUndo) { slide ->
            slide.copy(
                elements = slide.elements.map { if (it.id == updated.id) updated else it }
            )
        }
    }

    fun previewTransformElement(
        elementId: String,
        dx: Float,
        dy: Float,
        newWidth: Float? = null,
        newHeight: Float? = null,
        newRotation: Float? = null
    ) {
        val pres = _state.value.presentation ?: return
        val slideIdx = _state.value.selectedSlideIndex
        val slide = pres.slides.getOrNull(slideIdx) ?: return
        val updatedElements = slide.elements.map { el ->
            if (el.id == elementId || (el.id in _state.value.selectedElementIds && newWidth == null && newRotation == null)) {
                el.withTransform(
                    x = (el.x + dx).coerceIn(-el.width * 0.5f, pres.canvasWidth - 40f),
                    y = (el.y + dy).coerceIn(-el.height * 0.5f, pres.canvasHeight - 40f),
                    width = (newWidth ?: el.width).coerceIn(48f, pres.canvasWidth * 1.5f),
                    height = (newHeight ?: el.height).coerceIn(40f, pres.canvasHeight * 1.5f),
                    rotation = newRotation ?: el.rotation
                )
            } else el
        }
        val updatedSlides = pres.slides.toMutableList().apply {
            set(slideIdx, slide.copy(elements = updatedElements))
        }
        _state.value = _state.value.copy(
            presentation = pres.copy(slides = updatedSlides, updatedAt = System.currentTimeMillis()),
            isDirty = true
        )
    }

    fun pushTransformUndoSnapshot(beforePresentation: Presentation, slideIndex: Int) {
        history.saveMemento(PresentationMemento(beforePresentation, slideIndex))
        _state.value = _state.value.copy(
            canUndo = history.canUndo,
            canRedo = history.canRedo,
            isDirty = true
        )
    }

    fun deleteSelectedElements() {
        val selected = _state.value.selectedElementIds
        if (selected.isEmpty()) return
        mutateActiveSlide(newSelectedIds = emptySet()) { slide ->
            slide.copy(elements = slide.elements.filterNot { it.id in selected })
        }
    }

    fun duplicateSelectedElements() {
        val selected = _state.value.selectedElementIds
        if (selected.isEmpty()) return
        val slide = _state.value.activeSlide ?: return
        val maxZ = slide.elements.maxOfOrNull { it.zIndex } ?: 0
        val clones = slide.elements.filter { it.id in selected }.mapIndexed { i, el ->
            el.withNewId().withTransform(
                x = el.x + 36f,
                y = el.y + 36f,
                zIndex = maxZ + i + 1,
                groupId = null
            )
        }
        mutateActiveSlide(newSelectedIds = clones.map { it.id }.toSet()) { s ->
            s.copy(elements = s.elements + clones)
        }
    }

    fun copySelectedElements() {
        val selected = _state.value.selectedElementIds
        val slide = _state.value.activeSlide ?: return
        val copied = slide.elements.filter { it.id in selected }
        if (copied.isNotEmpty()) {
            _state.value = _state.value.copy(clipboardElements = copied)
        }
    }

    fun pasteClipboard() {
        val clipboard = _state.value.clipboardElements
        if (clipboard.isEmpty()) return
        val slide = _state.value.activeSlide ?: return
        val maxZ = slide.elements.maxOfOrNull { it.zIndex } ?: 0
        val pasted = clipboard.mapIndexed { i, el ->
            el.withNewId().withTransform(
                x = el.x + 40f,
                y = el.y + 40f,
                zIndex = maxZ + i + 1,
                groupId = null
            )
        }
        mutateActiveSlide(newSelectedIds = pasted.map { it.id }.toSet()) { s ->
            s.copy(elements = s.elements + pasted)
        }
    }

    fun bringToFront() {
        val selected = _state.value.selectedElementIds
        if (selected.isEmpty()) return
        mutateActiveSlide { slide ->
            val maxZ = (slide.elements.maxOfOrNull { it.zIndex } ?: 0) + 1
            slide.copy(elements = slide.elements.mapIndexed { idx, el ->
                if (el.id in selected) el.withTransform(zIndex = maxZ + idx) else el
            })
        }
    }

    fun sendToBack() {
        val selected = _state.value.selectedElementIds
        if (selected.isEmpty()) return
        mutateActiveSlide { slide ->
            val minZ = (slide.elements.minOfOrNull { it.zIndex } ?: 1) - 1
            slide.copy(elements = slide.elements.map { el ->
                if (el.id in selected) el.withTransform(zIndex = minZ) else el
            })
        }
    }

    fun groupSelected() {
        val selected = _state.value.selectedElementIds
        if (selected.size < 2) return
        val newGroupId = UUID.randomUUID().toString()
        mutateActiveSlide { slide ->
            slide.copy(elements = slide.elements.map { el ->
                if (el.id in selected) el.withTransform(groupId = newGroupId) else el
            })
        }
    }

    fun ungroupSelected() {
        val selected = _state.value.selectedElementIds
        if (selected.isEmpty()) return
        mutateActiveSlide { slide ->
            slide.copy(elements = slide.elements.map { el ->
                if (el.id in selected) el.withTransform(groupId = null) else el
            })
        }
    }

    fun alignSelected(alignment: ElementAlignment) {
        val pres = _state.value.presentation ?: return
        val selected = _state.value.selectedElementIds
        if (selected.isEmpty()) return
        mutateActiveSlide { slide ->
            slide.copy(elements = slide.elements.map { el ->
                if (el.id !in selected) el
                else {
                    when (alignment) {
                        ElementAlignment.LEFT -> el.withTransform(x = 60f)
                        ElementAlignment.CENTER_H -> el.withTransform(x = (pres.canvasWidth - el.width) / 2f)
                        ElementAlignment.RIGHT -> el.withTransform(x = pres.canvasWidth - el.width - 60f)
                        ElementAlignment.TOP -> el.withTransform(y = 60f)
                        ElementAlignment.CENTER_V -> el.withTransform(y = (pres.canvasHeight - el.height) / 2f)
                        ElementAlignment.BOTTOM -> el.withTransform(y = pres.canvasHeight - el.height - 60f)
                    }
                }
            })
        }
    }

    fun addTableRow(tableId: String) {
        val pres = _state.value.presentation
        val itemPrefix = when (pres?.language ?: AppLanguage.EN) {
            AppLanguage.AR -> "بند"
            AppLanguage.FA -> "مورد"
            AppLanguage.UR -> "آئٹم"
            AppLanguage.RU -> "Элем."
            AppLanguage.IT -> "Voce"
            AppLanguage.ZH -> "项目"
            AppLanguage.EN -> "Item"
        }
        val table = _state.value.activeSlide?.elements
            ?.filterIsInstance<SlideElement.TableElement>()
            ?.firstOrNull { it.id == tableId } ?: return
        val newRowIdx = table.rows
        val addedCells = (0 until table.cols).map { c ->
            TableCell(
                row = newRowIdx,
                col = c,
                text = "$itemPrefix ${newRowIdx}.${c + 1}",
                bgColorHex = if (newRowIdx % 2 == 1) "#F8FAFC" else "#FFFFFF"
            )
        }
        updateElement(
            table.copy(
                rows = table.rows + 1,
                height = table.height + 90f,
                cells = table.cells + addedCells
            )
        )
    }

    fun removeTableRow(tableId: String) {
        val table = _state.value.activeSlide?.elements
            ?.filterIsInstance<SlideElement.TableElement>()
            ?.firstOrNull { it.id == tableId } ?: return
        if (table.rows <= 1) return
        val targetRow = table.rows - 1
        updateElement(
            table.copy(
                rows = table.rows - 1,
                height = (table.height - 90f).coerceAtLeast(120f),
                cells = table.cells.filterNot { it.row == targetRow }
            )
        )
    }

    fun addTableCol(tableId: String) {
        val pres = _state.value.presentation
        val lang = pres?.language ?: AppLanguage.EN
        val headerPrefix = when (lang) {
            AppLanguage.AR -> "عنوان"
            AppLanguage.FA -> "ستون"
            AppLanguage.UR -> "ہیڈر"
            AppLanguage.RU -> "Заголовок"
            AppLanguage.IT -> "Intest."
            AppLanguage.ZH -> "表头"
            AppLanguage.EN -> "Header"
        }
        val itemPrefix = when (lang) {
            AppLanguage.AR -> "بند"
            AppLanguage.FA -> "مورد"
            AppLanguage.UR -> "آئٹم"
            AppLanguage.RU -> "Элем."
            AppLanguage.IT -> "Voce"
            AppLanguage.ZH -> "项目"
            AppLanguage.EN -> "Item"
        }
        val table = _state.value.activeSlide?.elements
            ?.filterIsInstance<SlideElement.TableElement>()
            ?.firstOrNull { it.id == tableId } ?: return
        val newColIdx = table.cols
        val addedCells = (0 until table.rows).map { r ->
            TableCell(
                row = r,
                col = newColIdx,
                text = if (r == 0) "$headerPrefix ${newColIdx + 1}" else "$itemPrefix ${r}.${newColIdx + 1}",
                bgColorHex = if (r == 0) "#0D47A1" else if (r % 2 == 1) "#F8FAFC" else "#FFFFFF",
                textColorHex = if (r == 0) "#FFFFFF" else "#0F172A",
                isHeader = r == 0,
                isBold = r == 0
            )
        }
        updateElement(
            table.copy(
                cols = table.cols + 1,
                width = (table.width + 220f).coerceAtMost(1760f),
                cells = table.cells + addedCells
            )
        )
    }

    fun removeTableCol(tableId: String) {
        val table = _state.value.activeSlide?.elements
            ?.filterIsInstance<SlideElement.TableElement>()
            ?.firstOrNull { it.id == tableId } ?: return
        if (table.cols <= 1) return
        val targetCol = table.cols - 1
        updateElement(
            table.copy(
                cols = table.cols - 1,
                width = (table.width - 220f).coerceAtLeast(240f),
                cells = table.cells.filterNot { it.col == targetCol }
            )
        )
    }

    fun mergeTableRowCells(tableId: String, row: Int) {
        val table = _state.value.activeSlide?.elements
            ?.filterIsInstance<SlideElement.TableElement>()
            ?.firstOrNull { it.id == tableId } ?: return
        if (table.cols < 2) return
        val mergedText = table.cells.filter { it.row == row && it.mergedIntoRow == null }
            .joinToString(" • ") { it.text }.trim()
        val updatedCells = table.cells.map { cell ->
            when {
                cell.row == row && cell.col == 0 -> cell.copy(colSpan = table.cols, text = mergedText)
                cell.row == row && cell.col > 0 -> cell.copy(mergedIntoRow = row, mergedIntoCol = 0)
                else -> cell
            }
        }
        updateElement(table.copy(cells = updatedCells))
    }

    fun undo() {
        val currentPres = _state.value.presentation ?: return
        val previous = history.undo(PresentationMemento(currentPres, _state.value.selectedSlideIndex)) ?: return
        _state.value = _state.value.copy(
            presentation = previous.presentation,
            selectedSlideIndex = previous.selectedSlideIndex.coerceIn(0, (previous.presentation.slides.size - 1).coerceAtLeast(0)),
            selectedElementIds = emptySet(),
            canUndo = history.canUndo,
            canRedo = history.canRedo,
            isDirty = true
        )
    }

    fun redo() {
        val currentPres = _state.value.presentation ?: return
        val next = history.redo(PresentationMemento(currentPres, _state.value.selectedSlideIndex)) ?: return
        _state.value = _state.value.copy(
            presentation = next.presentation,
            selectedSlideIndex = next.selectedSlideIndex.coerceIn(0, (next.presentation.slides.size - 1).coerceAtLeast(0)),
            selectedElementIds = emptySet(),
            canUndo = history.canUndo,
            canRedo = history.canRedo,
            isDirty = true
        )
    }

    private fun mutate(block: (Presentation) -> Pair<Presentation, Int>) {
        val current = _state.value.presentation ?: return
        history.saveMemento(PresentationMemento(current, _state.value.selectedSlideIndex))
        val (updated, nextSlideIdx) = block(current)
        _state.value = _state.value.copy(
            presentation = updated.copy(updatedAt = System.currentTimeMillis()),
            selectedSlideIndex = nextSlideIdx.coerceIn(0, (updated.slides.size - 1).coerceAtLeast(0)),
            selectedElementIds = emptySet(),
            canUndo = history.canUndo,
            canRedo = history.canRedo,
            isDirty = true
        )
    }

    private fun mutateActiveSlide(
        recordUndo: Boolean = true,
        newSelectedIds: Set<String> = _state.value.selectedElementIds,
        transform: (Slide) -> Slide
    ) {
        val current = _state.value.presentation ?: return
        val slideIdx = _state.value.selectedSlideIndex
        val slide = current.slides.getOrNull(slideIdx) ?: return
        if (recordUndo) {
            history.saveMemento(PresentationMemento(current, slideIdx))
        }
        val updatedSlides = current.slides.toMutableList().apply {
            set(slideIdx, transform(slide))
        }
        _state.value = _state.value.copy(
            presentation = current.copy(slides = updatedSlides, updatedAt = System.currentTimeMillis()),
            selectedElementIds = newSelectedIds,
            canUndo = history.canUndo,
            canRedo = history.canRedo,
            isDirty = true
        )
    }

    companion object {
        fun createDefaultSlide(
            order: Int,
            language: AppLanguage,
            canvasWidth: Float,
            canvasHeight: Float,
            templateBackground: SlideBackground = SlideBackground.Solid("#FFFFFF")
        ): Slide {
            val isRtl = language.isRtl
            val titlePlaceholder = when (language) {
                AppLanguage.AR -> "عنوان الشريحة"
                AppLanguage.FA -> "عنوان اسلاید"
                AppLanguage.UR -> "سلائیڈ کا عنوان"
                AppLanguage.RU -> "Заголовок слайда"
                AppLanguage.IT -> "Titolo Diapositiva"
                AppLanguage.ZH -> "幻灯片标题"
                AppLanguage.EN -> "Slide Title"
            }
            val bodyPlaceholder = when (language) {
                AppLanguage.AR -> "انقر مرتين لإضافة النص والمحتوى"
                AppLanguage.FA -> "برای افزودن متن دوبار ضربه بزنید"
                AppLanguage.UR -> "متن شامل کرنے کے لیے دو بار ٹیپ کریں"
                AppLanguage.RU -> "Нажмите дважды для добавления текста"
                AppLanguage.IT -> "Tocca due volte per aggiungere testo"
                AppLanguage.ZH -> "双击此处添加文字内容"
                AppLanguage.EN -> "Double tap to add text and content"
            }
            val defaultFont = when (language) {
                AppLanguage.AR, AppLanguage.UR -> "cairo"
                AppLanguage.FA -> "vazirmatn"
                AppLanguage.EN, AppLanguage.RU, AppLanguage.IT, AppLanguage.ZH -> "plus_jakarta_sans"
            }
            return Slide(
                order = order,
                background = templateBackground,
                elements = listOf(
                    SlideElement.TextElement(
                        x = canvasWidth * 0.08f,
                        y = canvasHeight * 0.12f,
                        width = canvasWidth * 0.84f,
                        height = canvasHeight * 0.16f,
                        zIndex = 1,
                        text = titlePlaceholder,
                        fontFamilyId = defaultFont,
                        fontSizeSp = 48f,
                        textColorHex = "#0D47A1",
                        isBold = true,
                        writingDirection = if (isRtl) WritingDirection.RTL else WritingDirection.LTR,
                        textAlign = TextAlignment.START
                    ),
                    SlideElement.TextElement(
                        x = canvasWidth * 0.08f,
                        y = canvasHeight * 0.34f,
                        width = canvasWidth * 0.84f,
                        height = canvasHeight * 0.45f,
                        zIndex = 2,
                        text = bodyPlaceholder,
                        fontFamilyId = defaultFont,
                        fontSizeSp = 30f,
                        textColorHex = "#334155",
                        writingDirection = if (isRtl) WritingDirection.RTL else WritingDirection.LTR,
                        textAlign = TextAlignment.START
                    )
                )
            )
        }
    }
}
