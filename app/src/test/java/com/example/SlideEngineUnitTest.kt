package com.example

import com.example.core.localization.AppLanguage
import com.example.core.localization.stringsFor
import com.example.data.local.ImportedFontSpec
import com.example.data.templates.SlideLayoutPreset
import com.example.data.templates.TemplateRepository
import com.example.engine.ChartType
import com.example.engine.ElementAlignment
import com.example.engine.EquationTemplateCatalog
import com.example.engine.MAX_SLIDES_PER_DECK
import com.example.engine.MotionThemeCatalog
import com.example.engine.MultiLanguageSpellChecker
import com.example.engine.Presentation
import com.example.engine.PresentationCaretaker
import com.example.engine.PresentationMemento
import com.example.engine.ShapeType
import com.example.engine.Slide
import com.example.engine.SlideAspectRatio
import com.example.engine.SlideBackground
import com.example.engine.SlideColorCatalog
import com.example.engine.SlideDecorationCatalog
import com.example.engine.SlideElement
import com.example.engine.SlideEngine
import com.example.engine.SlideIconCatalog
import com.example.engine.SlideTransition
import com.example.engine.SlideTransitionType
import com.example.engine.SmartArtLayoutType
import com.example.engine.SmartArtNode
import com.example.engine.SmartLayoutGeometryService
import com.example.engine.TextListType
import com.example.engine.ToolCommandCatalog
import com.example.engine.WritingDirection
import com.example.engine.detectTextIsRtl
import com.example.ui.theme.SlideFontRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SlideEngineUnitTest {

    @Test
    fun slideEngine_slideCrud_and_undoRedo_work() {
        val engine = SlideEngine()
        val initial = Presentation(
            id = "deck_1",
            title = "Babylonian History",
            language = AppLanguage.AR,
            aspectRatio = SlideAspectRatio.RATIO_16_9,
            slides = listOf(SlideEngine.createDefaultSlide(0, AppLanguage.AR, 1920f, 1080f))
        )
        engine.loadPresentation(initial)
        assertEquals(1, engine.state.value.presentation?.slides?.size)
        assertFalse(engine.state.value.canUndo)

        // Add, duplicate, move, delete
        engine.addSlide(0)
        assertEquals(2, engine.state.value.presentation?.slides?.size)
        assertTrue(engine.state.value.canUndo)

        engine.duplicateSlide(1)
        assertEquals(3, engine.state.value.presentation?.slides?.size)

        engine.moveSlide(2, 0)
        assertEquals(0, engine.state.value.selectedSlideIndex)

        engine.deleteSlide(0)
        assertEquals(2, engine.state.value.presentation?.slides?.size)

        // Undo back step by step
        engine.undo()
        assertEquals(3, engine.state.value.presentation?.slides?.size)
        assertTrue(engine.state.value.canRedo)

        engine.redo()
        assertEquals(2, engine.state.value.presentation?.slides?.size)
    }

    @Test
    fun slideEngine_max100SlidesCap_directMovePosition_and_reorderByIds_work() {
        val engine = SlideEngine()
        val initial = Presentation(
            id = "deck_100",
            title = "Large Deck",
            language = AppLanguage.EN,
            slides = listOf(SlideEngine.createDefaultSlide(0, AppLanguage.EN, 1920f, 1080f))
        )
        engine.loadPresentation(initial)
        assertEquals(100, MAX_SLIDES_PER_DECK)

        repeat(110) {
            engine.addSlide()
        }
        assertEquals(MAX_SLIDES_PER_DECK, engine.state.value.presentation?.slides?.size)

        engine.duplicateSlide(0)
        assertEquals(MAX_SLIDES_PER_DECK, engine.state.value.presentation?.slides?.size)

        // Move slide 0 directly to position #50 (index 49), then #99 to #2
        val firstId = engine.state.value.presentation!!.slides[0].id
        engine.moveSlide(0, 49)
        assertEquals(49, engine.state.value.selectedSlideIndex)
        assertEquals(firstId, engine.state.value.presentation!!.slides[49].id)
        engine.state.value.presentation!!.slides.forEachIndexed { idx, slide ->
            assertEquals(idx, slide.order)
        }

        val lastId = engine.state.value.presentation!!.slides[99].id
        engine.moveSlide(99, 2)
        assertEquals(2, engine.state.value.selectedSlideIndex)
        assertEquals(lastId, engine.state.value.presentation!!.slides[2].id)

        // Verify reorderSlidesByIds (used by PDF preview order sync)
        val reversedIds = engine.state.value.presentation!!.slides.map { it.id }.reversed()
        engine.reorderSlidesByIds(reversedIds)
        assertEquals(reversedIds.first(), engine.state.value.presentation!!.slides.first().id)
        assertEquals(0, engine.state.value.presentation!!.slides.first().order)
    }

    @Test
    fun slideEngine_elementTransforms_grouping_and_tableOps() {
        val engine = SlideEngine()
        val initial = Presentation(
            id = "deck_2",
            title = "Science Deck",
            language = AppLanguage.EN,
            slides = listOf(SlideEngine.createDefaultSlide(0, AppLanguage.EN, 1920f, 1080f))
        )
        engine.loadPresentation(initial)

        val shape = SlideElement.ShapeElement(
            x = 100f, y = 100f, width = 300f, height = 200f,
            shapeType = ShapeType.HEXAGON
        )
        val eq = SlideElement.EquationElement(
            x = 500f, y = 400f, width = 600f, height = 180f,
            expression = "E = mc^2"
        )
        engine.addElement(shape)
        engine.addElement(eq)

        // Multi-select & group
        engine.selectElement(shape.id, multiSelect = false)
        engine.selectElement(eq.id, multiSelect = true)
        assertEquals(2, engine.state.value.selectedElementIds.size)
        engine.groupSelected()

        val grouped = engine.state.value.activeSlide!!.elements.filter { it.id == shape.id || it.id == eq.id }
        assertNotNull(grouped[0].groupId)
        assertEquals(grouped[0].groupId, grouped[1].groupId)

        // Align horizontal center
        engine.alignSelected(ElementAlignment.CENTER_H)
        val centeredShape = engine.state.value.activeSlide!!.elements.first { it.id == shape.id }
        assertEquals((1920f - 300f) / 2f, centeredShape.x, 0.5f)

        // Transform preview + commit snapshot + undo
        val before = engine.state.value.presentation!!
        engine.previewTransformElement(shape.id, dx = 50f, dy = 25f, newWidth = 420f, newHeight = null, newRotation = 15f)
        engine.pushTransformUndoSnapshot(before, 0)
        val transformed = engine.state.value.activeSlide!!.elements.first { it.id == shape.id }
        assertEquals(420f, transformed.width, 0.5f)
        assertEquals(15f, transformed.rotation, 0.1f)

        engine.undo()
        val reverted = engine.state.value.activeSlide!!.elements.first { it.id == shape.id }
        assertEquals(300f, reverted.width, 0.5f)
        assertEquals(0f, reverted.rotation, 0.1f)

        // Table operations
        val table = SlideElement.TableElement(rows = 2, cols = 3)
        engine.addElement(table)
        engine.addTableRow(table.id)
        engine.addTableCol(table.id)
        engine.mergeTableRowCells(table.id, 1)
        val updatedTable = engine.state.value.activeSlide!!.elements.filterIsInstance<SlideElement.TableElement>().first()
        assertEquals(3, updatedTable.rows)
        assertEquals(4, updatedTable.cols)
        assertEquals(4, updatedTable.cells.first { it.row == 1 && it.col == 0 }.colSpan)
    }

    @Test
    fun slideEngine_expandedTools_stabilityZoom_and_deckWideSettings_work() {
        val engine = SlideEngine()
        val initial = Presentation(
            id = "deck_tools",
            title = "Tools Deck",
            language = AppLanguage.AR,
            slides = listOf(
                SlideEngine.createDefaultSlide(0, AppLanguage.AR, 1920f, 1080f),
                SlideEngine.createDefaultSlide(1, AppLanguage.AR, 1920f, 1080f)
            )
        )
        engine.loadPresentation(initial)

        val el = SlideElement.ShapeElement(id = "box1", x = 200f, y = 200f, width = 300f, height = 200f)
        engine.addElement(el, useSmartPlacement = false)
        engine.selectElement("box1")

        engine.nudgeSelectedElements(20f, -15f)
        engine.resizeSelectedElements(30f, -20f)
        engine.rotateSelectedElements(15f)
        val modified = engine.state.value.activeSlide!!.elements.first { it.id == "box1" }
        assertEquals(220f, modified.x, 0.5f)
        assertEquals(185f, modified.y, 0.5f)
        assertEquals(330f, modified.width, 0.5f)
        assertEquals(180f, modified.height, 0.5f)
        assertEquals(15f, modified.rotation, 0.1f)

        // Apply background & transition to all slides
        val bg = SlideBackground.Solid("#0F172A")
        val tr = SlideTransition(SlideTransitionType.DISSOLVE, 650)
        engine.applyBackgroundToAllSlides(bg)
        engine.applyTransitionToAllSlides(tr)
        assertTrue(engine.state.value.presentation!!.slides.all { it.background == bg })
        assertTrue(engine.state.value.presentation!!.slides.all { it.transition == tr })

        // Auto-advance & Aspect Ratio scaling
        engine.updateSlideAutoAdvance(12)
        assertEquals(12, engine.state.value.activeSlide!!.autoAdvanceSeconds)

        engine.updateCanvasAspectRatio(SlideAspectRatio.RATIO_4_3)
        assertEquals(SlideAspectRatio.RATIO_4_3, engine.state.value.presentation!!.aspectRatio)
        assertEquals(1440f, engine.state.value.presentation!!.canvasWidth, 0.1f)

        // Canvas lock & zoom bounds
        engine.setCanvasLocked(true)
        assertTrue(engine.state.value.isCanvasLocked)
        engine.setCanvasZoom(5.0f)
        assertEquals(2.25f, engine.state.value.canvasZoom, 0.01f)
        engine.setCanvasZoom(0.1f)
        assertEquals(0.65f, engine.state.value.canvasZoom, 0.01f)
    }

    @Test
    fun rtlDetection_handlesArabicPersianAndEnglish() {
        assertTrue(detectTextIsRtl("مرحبا بكم في كاتب سلايدز", WritingDirection.AUTO))
        assertTrue(detectTextIsRtl("سلام دنیا", WritingDirection.AUTO))
        assertFalse(detectTextIsRtl("Hello KATEB Slides", WritingDirection.AUTO))
        assertFalse(detectTextIsRtl("مرحبا", WritingDirection.LTR))
        assertTrue(detectTextIsRtl("Hello", WritingDirection.RTL))
    }

    @Test
    fun templateRepository_has100TemplatesAcrossCategories_and_layoutPresets() {
        assertEquals(100, TemplateRepository.templates.size)
        assertTrue(TemplateRepository.templates.map { it.category }.toSet().size >= 14)

        // Verify all 100 templates build multi-slide decks cleanly with translated names
        TemplateRepository.templates.forEach { tpl ->
            AppLanguage.entries.forEach { lang ->
                assertTrue("Template ${tpl.id} blank name for $lang", tpl.name(lang).isNotBlank())
            }
            val built = TemplateRepository.buildPresentationFromTemplate(
                template = tpl,
                title = "Deck ${tpl.id}",
                description = "",
                language = AppLanguage.AR,
                aspectRatio = SlideAspectRatio.RATIO_16_9,
                slideCount = 6
            )
            assertEquals(6, built.slides.size)
        }

        val baseDeck = TemplateRepository.buildPresentationFromTemplate(
            template = TemplateRepository.templates.first(),
            title = "Preset Check",
            description = "",
            language = AppLanguage.AR,
            aspectRatio = SlideAspectRatio.RATIO_16_9,
            slideCount = 5
        )
        SlideLayoutPreset.entries.forEachIndexed { idx, preset ->
            val slide = TemplateRepository.buildSlideFromLayoutPreset(preset, idx, baseDeck)
            assertEquals(idx, slide.order)
            if (preset == SlideLayoutPreset.BLANK) {
                assertTrue(slide.elements.isEmpty())
            } else {
                assertTrue(slide.elements.isNotEmpty())
            }
        }
    }

    @Test
    fun slideFontRegistry_has13BuiltInFonts_and_registersCustomFontInMemory() {
        assertEquals(13, SlideFontRegistry.builtInOptions.size)
        assertTrue(SlideFontRegistry.builtInOptions.any { it.id == "cairo" })
        assertTrue(SlideFontRegistry.builtInOptions.any { it.id == "montserrat" })
        assertTrue(SlideFontRegistry.builtInOptions.any { it.id == "playfair_display" })

        val customSpec = ImportedFontSpec(
            id = "custom_unit_test",
            displayName = "My Studio Font",
            filePath = "/tmp/nonexistent_font.ttf"
        )
        SlideFontRegistry.registerCustomFontInMemory(customSpec)
        assertTrue(SlideFontRegistry.options.any { it.id == "custom_unit_test" })
        assertEquals("My Studio Font", SlideFontRegistry.displayNameFor("custom_unit_test"))
    }

    @Test
    fun catalogs100_verifyExact100CountsAcrossAllDesignAndToolCatalogs() {
        assertEquals(100, SlideIconCatalog.icons.size)
        assertEquals(100, SlideDecorationCatalog.decorations.size)
        assertEquals(100, SlideColorCatalog.colors.size)
        assertEquals(100, EquationTemplateCatalog.equations.size)
        assertEquals(100, MotionThemeCatalog.themes.size)
        assertEquals(100, ToolCommandCatalog.commands.size)
        assertEquals(100, TemplateRepository.templates.size)

        assertEquals("🚀", SlideIconCatalog.glyphFor("rocket"))
        assertEquals("⚗", SlideIconCatalog.glyphFor("science"))
        val science = SlideIconCatalog.specFor("science")
        assertTrue(science.usageHint(AppLanguage.AR).isNotBlank())
        assertTrue(science.usageHint(AppLanguage.EN).isNotBlank())
        AppLanguage.entries.forEach { lang ->
            assertTrue(science.label(lang).isNotBlank())
        }
    }

    @Test
    fun presentationCaretaker_mementoUndoRedo_preservesState() {
        val caretaker = PresentationCaretaker(maxHistory = 10)
        val s1 = Presentation(id = "d", title = "V1", slides = emptyList())
        val s2 = Presentation(id = "d", title = "V2", slides = emptyList())
        val s3 = Presentation(id = "d", title = "V3", slides = emptyList())

        caretaker.saveMemento(PresentationMemento(s1, 0))
        caretaker.saveMemento(PresentationMemento(s2, 1))
        assertTrue(caretaker.canUndo)
        assertFalse(caretaker.canRedo)

        val undone1 = caretaker.undo(PresentationMemento(s3, 2))
        assertEquals("V2", undone1?.presentation?.title)
        assertEquals(1, undone1?.selectedSlideIndex)
        assertTrue(caretaker.canRedo)

        val undone2 = caretaker.undo(undone1!!)
        assertEquals("V1", undone2?.presentation?.title)
        assertFalse(caretaker.canUndo)

        val redone = caretaker.redo(undone2!!)
        assertEquals("V2", redone?.presentation?.title)
        caretaker.clear()
        assertNull(caretaker.undo(redone!!))
    }

    @Test
    fun smartLayoutGeometryService_avoidsOverlap_and_autoArrangesCrowdedSlide() {
        val existingHeader = SlideElement.TextElement(
            id = "h1", x = 100f, y = 80f, width = 1720f, height = 140f,
            text = "Header", isBold = true, fontSizeSp = 44f
        )
        val existingBlock = SlideElement.TextElement(
            id = "b1", x = 100f, y = 260f, width = 800f, height = 500f,
            text = "Body"
        )
        val candidate = SlideElement.ShapeElement(
            id = "s1", x = 120f, y = 280f, width = 400f, height = 280f
        )
        val placed = SmartLayoutGeometryService.suggestPlacement(
            newElement = candidate,
            existingElements = listOf(existingHeader, existingBlock),
            canvasW = 1920f,
            canvasH = 1080f
        )
        val slideAfterAdd = Slide(order = 0, elements = listOf(existingHeader, existingBlock, placed))
        assertEquals(0, SmartLayoutGeometryService.countOverlaps(slideAfterAdd, 1920f, 1080f))

        // Deliberately overlap all 3 elements then auto-arrange
        val crowded = Slide(order = 0, elements = listOf(existingHeader, existingBlock, candidate))
        assertTrue(SmartLayoutGeometryService.countOverlaps(crowded, 1920f, 1080f) > 0)
        val arranged = SmartLayoutGeometryService.autoArrangeSlide(crowded, 1920f, 1080f)
        assertEquals(0, SmartLayoutGeometryService.countOverlaps(arranged, 1920f, 1080f))
    }

    @Test
    fun multiLanguageSpellChecker_detectsAndFixesAllSevenLanguages() {
        // English
        val enIssues = MultiLanguageSpellChecker.checkText("teh reasearch presentaton", AppLanguage.EN)
        assertTrue(enIssues.any { it.word == "teh" && "the" in it.suggestions })
        assertTrue(enIssues.any { it.word == "presentaton" && "presentation" in it.suggestions })
        val fixedEn = MultiLanguageSpellChecker.applyFix("teh presentaton", enIssues.first(), "the")
        assertTrue(fixedEn.startsWith("the"))

        // Arabic
        val arIssues = MultiLanguageSpellChecker.checkText("العرظ التقديمي مقدمه", AppLanguage.AR)
        assertTrue(arIssues.any { it.word == "العرظ" && "العرض" in it.suggestions })
        assertTrue(arIssues.any { it.word == "مقدمه" && "مقدمة" in it.suggestions })

        // Persian
        val faIssues = MultiLanguageSpellChecker.checkText("ارایه اسلايد", AppLanguage.FA)
        assertTrue(faIssues.any { it.word == "ارایه" && "ارائه" in it.suggestions })

        // Russian
        val ruIssues = MultiLanguageSpellChecker.checkText("призентация слаид", AppLanguage.RU)
        assertTrue(ruIssues.any { it.word == "призентация" && "презентация" in it.suggestions })

        // Italian
        val itIssues = MultiLanguageSpellChecker.checkText("presentazzione obbiettivi", AppLanguage.IT)
        assertTrue(itIssues.any { it.word == "presentazzione" && "presentazione" in it.suggestions })

        // Urdu
        val urIssues = MultiLanguageSpellChecker.checkText("پریزینٹیشن شکریه", AppLanguage.UR)
        assertTrue(urIssues.any { it.word == "شکریه" && "شکریہ" in it.suggestions })

        // Chinese
        val zhIssues = MultiLanguageSpellChecker.checkText("演试文稿与分祈", AppLanguage.ZH)
        assertTrue(zhIssues.any { it.word == "演试" && "演示" in it.suggestions })
        assertTrue(zhIssues.any { it.word == "分祈" && "分析" in it.suggestions })
    }

    @Test
    fun allSevenLanguageLocalizations_haveTranslatedToolsAndNoBlankKeys() {
        AppLanguage.entries.forEach { lang ->
            val s = stringsFor(lang)
            assertTrue(s.appName.isNotBlank())
            assertTrue(s.rowsLabel.isNotBlank())
            assertTrue(s.colsLabel.isNotBlank())
            assertTrue(s.alignLeft.isNotBlank())
            assertTrue(s.batterySaverTitle.isNotBlank())
            assertTrue(s.biometricLockSetting.isNotBlank())
            assertTrue(s.spellCheckIssuesTitle.isNotBlank())
            assertTrue(s.presenterModeLabel.isNotBlank())
            assertTrue(s.aiSmartThemeEngine.isNotBlank())
            assertTrue(s.aiDesignAdvisor.isNotBlank())

            // 30 new keys for Focus Mode, Custom Fonts, Slide Settings, Stability, and Tools
            assertTrue(s.focusMode.isNotBlank())
            assertTrue(s.exitFocusMode.isNotBlank())
            assertTrue(s.focusModeHint.isNotBlank())
            assertTrue(s.importCustomFont.isNotBlank())
            assertTrue(s.importedFonts.isNotBlank())
            assertTrue(s.builtinFontsLabel.isNotBlank())
            assertTrue(s.deleteFontAction.isNotBlank())
            assertTrue(s.fontImportedSuccess.isNotBlank())
            assertTrue(s.invalidFontFile.isNotBlank())
            assertTrue(s.slideSettings.isNotBlank())
            assertTrue(s.stabilizeScreen.isNotBlank())
            assertTrue(s.stabilizeScreenDesc.isNotBlank())
            assertTrue(s.snapToGridLabel.isNotBlank())
            assertTrue(s.showGridLabel.isNotBlank())
            assertTrue(s.showSafeMarginsLabel.isNotBlank())
            assertTrue(s.canvasZoomLabel.isNotBlank())
            assertTrue(s.resetZoomLabel.isNotBlank())
            assertTrue(s.moveSlideToPosition.isNotBlank())
            assertTrue(s.targetPositionLabel.isNotBlank())
            assertTrue(s.slideDurationLabel.isNotBlank())
            assertTrue(s.applyBgAllSlides.isNotBlank())
            assertTrue(s.applyTransitionAllSlides.isNotBlank())
            assertTrue(s.heightPlusLabel.isNotBlank())
            assertTrue(s.heightMinusLabel.isNotBlank())
            assertTrue(s.rotateLeft15.isNotBlank())
            assertTrue(s.rotateRight15.isNotBlank())
            assertTrue(s.unlockElementAction.isNotBlank())
            assertTrue(s.quickFontTool.isNotBlank())
            assertTrue(s.moveToolLabel.isNotBlank())
            assertTrue(s.stabilizeShort.isNotBlank())

            // Pro / 100-catalog / PDF Preview / Semantic Search / SmartArt / 3D / iOS strings
            assertTrue(s.pdfPreviewTitle.isNotBlank())
            assertTrue(s.pdfPreviewSubtitle.isNotBlank())
            assertTrue(s.applyPageOrderToDeck.isNotBlank())
            assertTrue(s.resetOrderAction.isNotBlank())
            assertTrue(s.confirmExportPdf.isNotBlank())
            assertTrue(s.exportHtml5Ios.isNotBlank())
            assertTrue(s.iosCrossPlatformNote.isNotBlank())
            assertTrue(s.semanticSearchAction.isNotBlank())
            assertTrue(s.semanticSearchHint.isNotBlank())
            assertTrue(s.semanticSearchResultsTitle.isNotBlank())
            assertTrue(s.checkboxListLabel.isNotBlank())
            assertTrue(s.smartArtTool.isNotBlank())
            assertTrue(s.smartArtGalleryTitle.isNotBlank())
            assertTrue(s.smartArtEditorTitle.isNotBlank())
            assertTrue(s.shapes3DTitle.isNotBlank())
            assertTrue(s.ornaments100Title.isNotBlank())
            assertTrue(s.colors100Label.isNotBlank())
            assertTrue(s.equations100Title.isNotBlank())
            assertTrue(s.motion100Title.isNotBlank())
            assertTrue(s.tools100Title.isNotBlank())
            assertTrue(s.viewDesign.isNotBlank())
            assertTrue(s.viewOutline.isNotBlank())
            assertTrue(s.viewSorter.isNotBlank())
        }
        val ar = stringsFor(AppLanguage.AR)
        assertEquals("الصفوف", ar.rowsLabel)
        assertEquals("الأعمدة", ar.colsLabel)
        assertEquals("محاذاة لليسار", ar.alignLeft)
        assertEquals("وضع مقدم العرض", ar.presenterModeLabel)
        assertEquals("وضع التركيز", ar.focusMode)
    }
}
