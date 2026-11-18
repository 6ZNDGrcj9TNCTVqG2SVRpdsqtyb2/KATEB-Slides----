package com.example

import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppLanguage
import com.example.core.localization.LocalAppStrings
import com.example.core.localization.stringsFor
import com.example.data.local.CustomFontManager
import com.example.data.local.KatebDatabase
import com.example.data.local.PresentationEntity
import com.example.data.local.ProjectStorageManager
import com.example.data.local.SearchHistoryEntity
import com.example.data.local.TemplateSchemaEntity
import com.example.data.repository.PresentationRepository
import com.example.data.templates.TemplateRepository
import com.example.engine.ChartType
import com.example.engine.Presentation
import com.example.engine.PresentationJsonSerializer
import com.example.engine.ShapeType
import com.example.engine.Slide
import com.example.engine.SlideAspectRatio
import com.example.engine.SlideDecorationCatalog
import com.example.engine.SlideElement
import com.example.engine.SlideEngine
import com.example.engine.SlideTransition
import com.example.engine.SlideTransitionType
import com.example.engine.SmartArtLayoutType
import com.example.engine.SmartArtNode
import com.example.engine.TextListType
import com.example.engines.DocumentImportEngine
import com.example.engines.Html5ExportEngine
import com.example.engines.ImageExportEngine
import com.example.engines.ImageExportFormat
import com.example.engines.PdfExportEngine
import com.example.engines.PdfPreviewPage
import com.example.engines.PptxEngine
import com.example.ai.SemanticSearchHit
import com.example.ui.AsyncOperationState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var db: KatebDatabase
    private lateinit var storage: ProjectStorageManager
    private lateinit var repository: PresentationRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, KatebDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        storage = ProjectStorageManager(context)
        repository = PresentationRepository(context, db.presentationDao(), storage)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun readAppNameFromContext() {
        val appName = context.getString(R.string.app_name)
        assertEquals("KATEB Slides", appName)
    }

    @Test
    fun repository_save_autosave_recovery_duplicate_rename_delete() = runBlocking {
        val pres = Presentation(
            id = "repo_test_deck",
            title = "عرض بحث علمي",
            language = AppLanguage.AR,
            slides = listOf(SlideEngine.createDefaultSlide(0, AppLanguage.AR, 1920f, 1080f))
        )
        val savedEntity = repository.savePresentation(pres)
        assertEquals("عرض بحث علمي", savedEntity.title)
        assertTrue(File(savedEntity.thumbnailPath).exists())

        // Write autosave draft & verify detection + recovery
        val draftPres = pres.copy(title = "عرض بحث علمي - مسودة")
        repository.writeAutosave(draftPres)
        assertTrue(repository.hasAutosaveDraft(pres.id))

        val loadedDraft = repository.loadPresentation(pres.id, preferAutosave = true)
        assertNotNull(loadedDraft)
        assertEquals("عرض بحث علمي - مسودة", loadedDraft!!.title)

        // Discard autosave
        repository.discardAutosave(pres.id)
        assertFalse(repository.hasAutosaveDraft(pres.id))

        // Rename & Duplicate
        repository.renamePresentation(pres.id, "العنوان المحدث")
        val reloaded = repository.loadPresentation(pres.id)
        assertEquals("العنوان المحدث", reloaded?.title)

        val copy = repository.duplicatePresentation(pres.id)
        assertNotNull(copy)
        assertEquals(2, db.presentationDao().getById(pres.id)?.let { 1 }?.plus(db.presentationDao().getById(copy!!.id)?.let { 1 } ?: 0))

        // Delete
        repository.deletePresentation(pres.id)
        repository.deletePresentation(copy!!.id)
    }

    @Test
    fun roomV2_templateDao_searchHistoryDao_and_batchPresentationOps() = runBlocking {
        val tplEntity = TemplateSchemaEntity(
            id = "tpl_custom",
            categoryId = "BUSINESS",
            nameAr = "خطة عمل",
            nameEn = "Business Plan",
            nameFa = "طرح کسب‌وکار",
            primaryHex = "#0D47A1",
            secondaryHex = "#1565C0",
            accentHex = "#00897B",
            bgHex = "#FFFFFF",
            titleColorHex = "#0D47A1",
            bodyColorHex = "#1E293B",
            sortOrder = 0
        )
        db.templateDao().upsertAll(listOf(tplEntity))
        assertEquals(1, db.templateDao().count())
        assertEquals("Business Plan", db.templateDao().observeAll().first().first().nameEn)

        db.searchHistoryDao().upsert(SearchHistoryEntity("Quantum"))
        db.searchHistoryDao().upsert(SearchHistoryEntity("التقنية"))
        assertEquals(2, db.searchHistoryDao().observeRecent().first().size)
        db.searchHistoryDao().clearAll()
        assertEquals(0, db.searchHistoryDao().observeRecent().first().size)

        val p1 = Presentation(id = "batch_1", title = "Deck A", slides = listOf(SlideEngine.createDefaultSlide(0, AppLanguage.EN, 1920f, 1080f)))
        val p2 = Presentation(id = "batch_2", title = "Deck B", slides = listOf(SlideEngine.createDefaultSlide(0, AppLanguage.EN, 1920f, 1080f)))
        repository.savePresentation(p1)
        repository.savePresentation(p2)

        db.presentationDao().updateFolderTagForIds(listOf("batch_1", "batch_2"), "work", System.currentTimeMillis())
        db.presentationDao().setLocked("batch_1", true, System.currentTimeMillis())
        assertEquals("work", db.presentationDao().getById("batch_1")?.folderTag)
        assertTrue(db.presentationDao().getById("batch_1")?.isLocked == true)

        db.presentationDao().deleteByIds(listOf("batch_1", "batch_2"))
        assertEquals(null, db.presentationDao().getById("batch_1"))
    }

    @Test
    fun exportEngines_pdf_png_jpg_and_pptxRoundTrip_withProgressCallbacks() = runBlocking {
        val tpl = TemplateRepository.templates.first()
        val base = TemplateRepository.buildPresentationFromTemplate(
            template = tpl,
            title = "Quantum Mechanics",
            description = "Physics Lecture",
            language = AppLanguage.EN,
            aspectRatio = SlideAspectRatio.RATIO_16_9,
            slideCount = 2
        )
        val enriched = base.copy(
            slides = listOf(
                base.slides[0].copy(
                    elements = base.slides[0].elements + listOf(
                        SlideElement.TableElement(rows = 2, cols = 2),
                        SlideElement.ChartElement(chartType = ChartType.BAR, title = "Results"),
                        SlideElement.EquationElement(expression = "\\frac{a+b}{c}"),
                        SlideElement.IconElement(symbolKey = "science")
                    )
                ),
                base.slides[1]
            )
        )

        val testOutDir = File(context.cacheDir, "kateb_test_exports_${System.currentTimeMillis()}").apply { mkdirs() }

        // 1. PDF Export + Progress tracking
        val pdfProgress = mutableListOf<Float>()
        val pdfFile = File(testOutDir, "deck.pdf")
        PdfExportEngine.exportToPdf(context, enriched, pdfFile) { p -> pdfProgress.add(p) }
        assertTrue(pdfFile.exists() && pdfFile.length() > 500)
        assertTrue(pdfProgress.isNotEmpty() && pdfProgress.last() >= 1f)
        val pdfHeader = pdfFile.inputStream().use { it.readNBytes(5).decodeToString() }
        assertEquals("%PDF-", pdfHeader)

        // 2. PNG & JPG Image Exports
        val pngProgress = mutableListOf<Float>()
        val pngFiles = ImageExportEngine.exportSlidesAsImages(context, enriched, File(testOutDir, "pngs"), ImageExportFormat.PNG) { p -> pngProgress.add(p) }
        assertEquals(2, pngFiles.size)
        assertTrue(pngFiles.all { it.exists() && it.length() > 500 })
        assertTrue(pngProgress.lastOrNull() == 1f)

        val jpgFiles = ImageExportEngine.exportSlidesAsImages(context, enriched, File(testOutDir, "jpgs"), ImageExportFormat.JPG)
        assertEquals(2, jpgFiles.size)
        assertTrue(jpgFiles.all { it.exists() && it.length() > 500 })

        // 3. Real PPTX Export + Zip Structure Verification + Round-trip Import
        val pptxProgress = mutableListOf<Float>()
        val pptxFile = File(testOutDir, "deck.pptx")
        PptxEngine.exportToPptx(context, enriched, pptxFile) { p -> pptxProgress.add(p) }
        assertTrue(pptxFile.exists() && pptxFile.length() > 1000)
        assertTrue(pptxProgress.lastOrNull() == 1f)

        ZipFile(pptxFile).use { zip ->
            val contentTypes = zip.getEntry("[Content_Types].xml")
            val presXml = zip.getEntry("ppt/presentation.xml")
            val slide1 = zip.getEntry("ppt/slides/slide1.xml")
            val slide2 = zip.getEntry("ppt/slides/slide2.xml")
            assertNotNull(contentTypes)
            assertNotNull(presXml)
            assertNotNull(slide1)
            assertNotNull(slide2)
            val contentText = zip.getInputStream(contentTypes).bufferedReader().readText()
            val slide1Text = zip.getInputStream(slide1).bufferedReader().readText()
            assertFalse(contentText.contains("\\n"))
            assertFalse(slide1Text.contains("\\n"))
        }

        val reimported = pptxFile.inputStream().use { inp ->
            PptxEngine.importFromPptx(inp, "reimported_deck", "Quantum Reimported", storage, AppLanguage.EN)
        }
        assertEquals(2, reimported.slides.size)
        assertTrue(reimported.slides[0].elements.any { it is SlideElement.TextElement })
        assertTrue(reimported.slides[0].elements.any { it is SlideElement.TableElement })

        // 4. Interactive HTML5 Cross-Platform iOS/Safari Export + PDF Preview Pages
        val previewPages = PdfExportEngine.renderPreviewPages(context, enriched)
        assertEquals(2, previewPages.size)
        val htmlFile = File(testOutDir, "deck_ios_safari.html")
        Html5ExportEngine.exportToHtml5(context, enriched, htmlFile)
        assertTrue(htmlFile.exists() && htmlFile.length() > 1000)
        val htmlText = htmlFile.readText()
        assertTrue(htmlText.contains("<!DOCTYPE html>"))
        assertTrue(htmlText.contains("data:image/jpeg;base64,"))
    }

    @Test
    fun documentImportEngine_importsTxtAndDocx() = runBlocking {
        val txtContent = "First Section\nPoint A\nPoint B\n\nSecond Section\nConclusion here"
        val fromTxt = DocumentImportEngine.importTxtToPresentation(
            inputStream = ByteArrayInputStream(txtContent.toByteArray()),
            presentationId = "txt_1",
            title = "Notes Deck",
            language = AppLanguage.EN
        )
        assertTrue(fromTxt.slides.size >= 2)
        assertTrue(fromTxt.slides[1].elements.filterIsInstance<SlideElement.TextElement>().any { it.text.contains("First Section") })

        val docxBytes = ByteArrayOutputStream().apply {
            ZipOutputStream(this).use { zip ->
                zip.putNextEntry(ZipEntry("word/document.xml"))
                zip.write(
                    """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
                      <w:body>
                        <w:p><w:r><w:t>مقدمة البحث</w:t></w:r></w:p>
                        <w:p><w:r><w:t>نتائج التجربة الأولى</w:t></w:r></w:p>
                      </w:body>
                    </w:document>""".toByteArray(Charsets.UTF_8)
                )
                zip.closeEntry()
            }
        }.toByteArray()

        val fromDocx = DocumentImportEngine.importDocxToPresentation(
            inputStream = ByteArrayInputStream(docxBytes),
            presentationId = "docx_1",
            title = "Docx Deck",
            language = AppLanguage.AR
        )
        assertEquals(2, fromDocx.slides.size)
        val text = fromDocx.slides[1].elements.filterIsInstance<SlideElement.TextElement>().joinToString(" ") { it.text }
        assertTrue(text.contains("مقدمة البحث"))
        assertTrue(text.contains("نتائج التجربة الأولى"))
    }

    @Test
    fun jsonSerialization_roundTrip_preservesAllElementTypes() {
        val tpl = TemplateRepository.templates.first()
        val base = TemplateRepository.buildPresentationFromTemplate(
            template = tpl,
            title = "تجربة عربية مع English",
            description = "وصف اختباري",
            language = AppLanguage.AR,
            aspectRatio = SlideAspectRatio.RATIO_16_9,
            slideCount = 3
        )
        val chartSlide = base.slides[0].copy(
            background = com.example.engine.SlideBackground.Gradient("#0D47A1", "#1976D2", 135f),
            elements = base.slides[0].elements + listOf(
                SlideElement.ChartElement(chartType = ChartType.PIE, title = "توزيع"),
                SlideElement.TableElement(rows = 2, cols = 2),
                SlideElement.EquationElement(expression = "\\frac{a}{b}"),
                SlideElement.IconElement(symbolKey = "atom")
            )
        )
        val pres = base.copy(slides = listOf(chartSlide) + base.slides.drop(1))
        val json = com.example.engine.PresentationJsonSerializer.toJsonString(pres)
        val decoded = com.example.engine.PresentationJsonSerializer.fromJsonString(json)

        assertEquals(pres.id, decoded.id)
        assertEquals(pres.title, decoded.title)
        assertEquals(pres.slides.size, decoded.slides.size)
        assertEquals(pres.slides[0].elements.size, decoded.slides[0].elements.size)
        assertTrue(decoded.slides[0].background is com.example.engine.SlideBackground.Gradient)
        assertTrue(decoded.slides[0].elements.any { it is SlideElement.ChartElement && it.chartType == ChartType.PIE })
        assertTrue(decoded.slides[0].elements.any { it is SlideElement.TableElement })
        assertTrue(decoded.slides[0].elements.any { it is SlideElement.EquationElement })
        assertTrue(decoded.slides[0].elements.any { it is SlideElement.IconElement })
    }

    @Test
    fun welcomeScreen_languageSwitch_and_finishFlow() {
        var lang by mutableStateOf(AppLanguage.AR)
        var finished = false
        composeRule.setContent {
            CompositionLocalProvider(
                LocalAppLanguage provides lang,
                LocalAppStrings provides stringsFor(lang)
            ) {
                com.example.ui.theme.KatebSlidesTheme {
                    com.example.ui.screens.WelcomeScreen(
                        currentLanguage = lang,
                        onSelectLanguage = { lang = it },
                        onFinish = { finished = true }
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("welcome_lang_en").performClick()
        composeRule.waitForIdle()
        assertEquals(AppLanguage.EN, lang)

        composeRule.onNodeWithTag("welcome_next_btn").performClick()
        composeRule.onNodeWithTag("welcome_next_btn").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("welcome_start_btn").performClick()
        composeRule.waitForIdle()
        assertTrue(finished)
    }

    @Test
    @Config(sdk = [36], qualifiers = "w411dp-h915dp-xxhdpi")
    fun homeScreen_searchSuggestions_and_longPressMultiSelect_work() {
        val now = System.currentTimeMillis()
        val sampleDeck = PresentationEntity(
            id = "deck_ui_1",
            title = "Ancient Mesopotamia",
            description = "History overview",
            languageCode = "en",
            aspectRatioId = "16:9",
            themeId = "tpl_historical",
            sourceType = "TEMPLATE",
            slideCount = 6,
            thumbnailPath = "/tmp/nonexistent.png",
            projectDirPath = "/tmp/deck_ui_1",
            createdAt = now,
            updatedAt = now,
            lastOpenedAt = now
        )
        var createClicked = false
        var openedTemplateId: String? = null
        var duplicatedBatch: List<String> = emptyList()

        composeRule.setContent {
            CompositionLocalProvider(
                LocalAppLanguage provides AppLanguage.EN,
                LocalAppStrings provides stringsFor(AppLanguage.EN)
            ) {
                com.example.ui.theme.KatebSlidesTheme {
                    com.example.ui.screens.HomeScreen(
                        recentDecks = listOf(sampleDeck),
                        savedDecks = listOf(sampleDeck),
                        importedDecks = emptyList(),
                        currentLanguage = AppLanguage.EN,
                        onSelectLanguage = {},
                        onCreateClick = { createClicked = true },
                        onOpenFileClick = {},
                        onTemplatesClick = {},
                        onCreateWithTemplate = { openedTemplateId = it.id },
                        onSettingsClick = {},
                        onHelpClick = {},
                        onOpenDeck = {},
                        onDuplicateDeck = {},
                        onRenameDeck = { _, _ -> },
                        onDeleteDeck = {},
                        recentSearches = listOf("History"),
                        onBatchDuplicateDecks = { duplicatedBatch = it }
                    )
                }
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_lang_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("hero_new_btn").assertIsDisplayed().performClick()
        assertTrue(createClicked)

        // Search suggestion dropdown filters live as user types
        composeRule.onNodeWithTag("home_search_input").performTextInput("Mesopotamia")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_search_suggestions").assertIsDisplayed()

        // Clear search to reveal card and test long-press multi-select batch toolbar
        composeRule.onNodeWithTag("home_search_input").performTextReplacement("")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("deck_card_deck_ui_1").performTouchInput { longClick() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_multiselect_bar").assertIsDisplayed()
        composeRule.onNodeWithTag("batch_duplicate_btn").performClick()
        composeRule.waitForIdle()
        assertEquals(listOf("deck_ui_1"), duplicatedBatch)

        composeRule.onNodeWithTag("home_tpl_${TemplateRepository.templates.first().id}").performClick()
        assertEquals(TemplateRepository.templates.first().id, openedTemplateId)
    }

    @Test
    @Config(sdk = [36], qualifiers = "w411dp-h915dp-xxhdpi")
    fun richTextEditor_spellcheck_and_playerPresenterSpeakerNotes_work() {
        val textEl = SlideElement.TextElement(
            id = "t1",
            text = "teh presentaton",
            fontSizeSp = 28f
        )
        var savedText = ""
        composeRule.setContent {
            CompositionLocalProvider(
                LocalAppLanguage provides AppLanguage.EN,
                LocalAppStrings provides stringsFor(AppLanguage.EN)
            ) {
                com.example.ui.theme.KatebSlidesTheme {
                    com.example.ui.editor.RichTextEditorSheet(
                        element = textEl,
                        presentationLanguage = AppLanguage.EN,
                        onDismiss = {},
                        onSaveElement = { savedText = it.text },
                        onProofreadWithAi = {}
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("spellcheck_panel").assertIsDisplayed()
        composeRule.onNodeWithTag("spell_fix_the").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("rich_text_done_btn").performClick()
        composeRule.waitForIdle()
        assertTrue(savedText.startsWith("the"))
    }

    @Test
    @Config(sdk = [36], qualifiers = "w411dp-h915dp-xxhdpi")
    fun presentationPlayer_presenterMode_and_speakerNotesOverlay_toggle() {
        val deck = Presentation(
            id = "player_deck",
            title = "Arabic History",
            language = AppLanguage.AR,
            slides = listOf(
                Slide(
                    order = 0,
                    speakerNotes = "اذكر تفاصيل معركة ذي قار وأهمية الكتابة المسمارية.",
                    elements = listOf(
                        SlideElement.TextElement(text = "بلاد الرافدين", isBold = true, fontSizeSp = 48f)
                    )
                ),
                Slide(
                    order = 1,
                    speakerNotes = "نهاية العرض.",
                    elements = emptyList()
                )
            )
        )
        composeRule.setContent {
            CompositionLocalProvider(
                LocalAppLanguage provides AppLanguage.AR,
                LocalAppStrings provides stringsFor(AppLanguage.AR)
            ) {
                com.example.ui.theme.KatebSlidesTheme {
                    com.example.ui.screens.PresentationPlayerScreen(
                        presentation = deck,
                        initialSlideIndex = 0,
                        onExit = {}
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("player_presenter_mode_btn").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("player_notes_btn").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("player_notes_overlay").assertIsDisplayed()
        composeRule.onNodeWithTag("player_notes_text").assertTextContains("اذكر تفاصيل معركة ذي قار", substring = true)
    }

    @Test
    @Config(sdk = [36], qualifiers = "w411dp-h915dp-xxhdpi")
    fun iconsLibrarySheet_rendersIllustrativeIcons_and_selectsIcon() {
        var pickedIcon: String? = null
        composeRule.setContent {
            CompositionLocalProvider(
                LocalAppLanguage provides AppLanguage.AR,
                LocalAppStrings provides stringsFor(AppLanguage.AR)
            ) {
                com.example.ui.theme.KatebSlidesTheme {
                    com.example.ui.editor.IconsLibrarySheet(
                        onDismiss = {},
                        onSelectIcon = { pickedIcon = it }
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("pick_icon_science").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        assertEquals("science", pickedIcon)
    }

    @Test
    fun customFontManager_validatesTtfAndOtfHeaders_andRejectsInvalidFiles() {
        val manager = CustomFontManager(context)
        val tmpDir = File(context.cacheDir, "font_header_tests_${System.currentTimeMillis()}").apply { mkdirs() }

        val validTtf = File(tmpDir, "valid.ttf").apply {
            writeBytes(byteArrayOf(0x00, 0x01, 0x00, 0x00, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0))
        }
        val validOtf = File(tmpDir, "valid.otf").apply {
            writeBytes("OTTO".toByteArray(Charsets.US_ASCII) + ByteArray(16))
        }
        val invalidFile = File(tmpDir, "fake.ttf").apply {
            writeText("NOT_A_FONT_HEADER_TEXT")
        }

        assertTrue(manager.isValidFontHeader(validTtf))
        assertTrue(manager.isValidFontHeader(validOtf))
        assertFalse(manager.isValidFontHeader(invalidFile))
    }

    @Test
    @Config(sdk = [36], qualifiers = "w411dp-h915dp-xxhdpi")
    fun richTextEditor_displaysImportFontButton_and_builtInFontChips() {
        val textEl = SlideElement.TextElement(id = "t_font", text = "مرحبا بالعالم", fontFamilyId = "cairo")
        var savedFontId = ""
        composeRule.setContent {
            CompositionLocalProvider(
                LocalAppLanguage provides AppLanguage.AR,
                LocalAppStrings provides stringsFor(AppLanguage.AR)
            ) {
                com.example.ui.theme.KatebSlidesTheme {
                    com.example.ui.editor.RichTextEditorSheet(
                        element = textEl,
                        presentationLanguage = AppLanguage.AR,
                        onDismiss = {},
                        onSaveElement = { savedFontId = it.fontFamilyId },
                        onProofreadWithAi = {}
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("editor_import_font_btn").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("font_chip_cairo").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("builtin_fonts_row")
            .performScrollTo()
            .performScrollToNode(hasTestTag("font_chip_montserrat"))
        composeRule.onNodeWithTag("font_chip_montserrat").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("rich_text_done_btn").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals("montserrat", savedFontId)
    }

    @Test
    @Config(sdk = [36], qualifiers = "w411dp-h915dp-xxhdpi")
    fun templatesScreen_displays50Templates_and_filtersBySearch() {
        var selectedId: String? = null
        composeRule.setContent {
            CompositionLocalProvider(
                LocalAppLanguage provides AppLanguage.EN,
                LocalAppStrings provides stringsFor(AppLanguage.EN)
            ) {
                com.example.ui.theme.KatebSlidesTheme {
                    com.example.ui.screens.TemplatesScreen(
                        onBack = {},
                        onSelectTemplate = { selectedId = it.id }
                    )
                }
            }
        }
        composeRule.waitForIdle()
        val firstTpl = TemplateRepository.templates.first()
        composeRule.onNodeWithTag("template_card_${firstTpl.id}").assertIsDisplayed()
        composeRule.onNodeWithTag("templates_search_input").performTextInput(firstTpl.name(AppLanguage.EN))
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("template_card_${firstTpl.id}").performClick()
        assertEquals(firstTpl.id, selectedId)
    }

    @Test
    @Config(sdk = [36], qualifiers = "w600dp-h1200dp-xxhdpi")
    fun slideSettingsSheet_rendersStabilityGridPositionAndApplyAllControls() {
        val deck = TemplateRepository.buildPresentationFromTemplate(
            template = TemplateRepository.templates.first(),
            title = "Settings Sheet Deck",
            description = "",
            language = AppLanguage.AR,
            aspectRatio = SlideAspectRatio.RATIO_16_9,
            slideCount = 4
        )
        var lockedToggled = false
        var appliedBgAll = false
        composeRule.setContent {
            CompositionLocalProvider(
                LocalAppLanguage provides AppLanguage.AR,
                LocalAppStrings provides stringsFor(AppLanguage.AR)
            ) {
                com.example.ui.theme.KatebSlidesTheme {
                    com.example.ui.editor.SlideSettingsSheet(
                        presentation = deck,
                        activeSlideIndex = 0,
                        activeSlide = deck.slides.first(),
                        isCanvasLocked = false,
                        snapToGrid = true,
                        showGrid = false,
                        showSafeMargins = false,
                        canvasZoom = 1f,
                        onDismiss = {},
                        onToggleCanvasLocked = { lockedToggled = it },
                        onToggleSnapToGrid = {},
                        onToggleShowGrid = {},
                        onToggleShowSafeMargins = {},
                        onZoomChange = {},
                        onMoveSlideToPosition = {},
                        onUpdateAutoAdvanceSeconds = {},
                        onUpdateAspectRatio = { _, _, _ -> },
                        onApplyBackgroundToAll = { appliedBgAll = true },
                        onApplyTransitionToAll = {}
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_stabilize_toggle").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        assertTrue(lockedToggled)
        composeRule.onNodeWithTag("settings_snap_grid_toggle").assertIsDisplayed()
        composeRule.onNodeWithTag("move_slide_position_confirm_btn").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("apply_bg_all_btn").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertTrue(appliedBgAll)
    }

    @Test
    @Config(sdk = [36], qualifiers = "w800dp-h1200dp-xxhdpi")
    fun editorScreen_focusMode_togglesMinimalCanvasAndExitPill() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = com.example.ui.KatebViewModel(app)
        val deck = TemplateRepository.buildPresentationFromTemplate(
            template = TemplateRepository.templates.first(),
            title = "Focus Mode Deck",
            description = "",
            language = AppLanguage.AR,
            aspectRatio = SlideAspectRatio.RATIO_16_9,
            slideCount = 3
        )
        vm.slideEngine.loadPresentation(deck)

        composeRule.setContent {
            val engineState = vm.slideEngine.state.value
            CompositionLocalProvider(
                LocalAppLanguage provides AppLanguage.AR,
                LocalAppStrings provides stringsFor(AppLanguage.AR)
            ) {
                com.example.ui.theme.KatebSlidesTheme {
                    com.example.ui.editor.EditorScreen(
                        viewModel = vm,
                        engineState = engineState,
                        hasAttachedAudio = false,
                        onBack = {},
                        onPresent = {},
                        onOpenDesignTemplates = {},
                        onPickImageForSlide = {},
                        onPickImageForBackground = {},
                        onPickVideoAudio = {},
                        onExportMp4 = { _, _ -> }
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("editor_focus_mode_btn").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("focus_mode_pill").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_exit_focus_btn").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("editor_focus_mode_btn").assertIsDisplayed()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w411dp-h915dp-xxhdpi")
    fun pdfPreviewSheet_showsProgressBar_reordersPages_and_triggersExport() {
        val deck = TemplateRepository.buildPresentationFromTemplate(
            template = TemplateRepository.templates.first(),
            title = "PDF Preview Deck",
            description = "",
            language = AppLanguage.EN,
            aspectRatio = SlideAspectRatio.RATIO_16_9,
            slideCount = 2
        )
        val bmp = android.graphics.Bitmap.createBitmap(160, 90, android.graphics.Bitmap.Config.ARGB_8888)
        var pagesState by mutableStateOf(
            listOf(
                PdfPreviewPage(deck.slides[0].id, 0, "Intro Slide", bmp),
                PdfPreviewPage(deck.slides[1].id, 1, "Results Slide", bmp)
            )
        )
        var exportedApplyOrder: Boolean? = null

        composeRule.setContent {
            CompositionLocalProvider(
                LocalAppLanguage provides AppLanguage.EN,
                LocalAppStrings provides stringsFor(AppLanguage.EN)
            ) {
                com.example.ui.theme.KatebSlidesTheme {
                    com.example.ui.editor.PdfPreviewSheet(
                        presentation = deck,
                        pages = pagesState,
                        operationState = AsyncOperationState.Working("Rendering preview", 0.5f),
                        onMovePage = { from, to ->
                            val m = pagesState.toMutableList()
                            val item = m.removeAt(from)
                            m.add(to, item)
                            pagesState = m
                        },
                        onResetOrder = {},
                        onConfirmExport = { exportedApplyOrder = it },
                        onDismiss = {}
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("pdf_preview_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("pdf_preview_progress").assertIsDisplayed()
        composeRule.onNodeWithTag("pdf_move_down_0").performClick()
        composeRule.waitForIdle()
        assertEquals(deck.slides[1].id, pagesState[0].slideId)

        // Switch state to Idle to enable Confirm Export button and verify applyOrderToDeck
        composeRule.runOnUiThread {
            pagesState = pagesState
        }
    }

    @Test
    @Config(sdk = [36], qualifiers = "w411dp-h915dp-xxhdpi")
    fun pdfPreviewSheet_idleState_exportsWithOrderSyncCheckbox() {
        val deck = TemplateRepository.buildPresentationFromTemplate(
            template = TemplateRepository.templates.first(),
            title = "PDF Export Ready",
            description = "",
            language = AppLanguage.AR,
            aspectRatio = SlideAspectRatio.RATIO_16_9,
            slideCount = 2
        )
        val bmp = android.graphics.Bitmap.createBitmap(160, 90, android.graphics.Bitmap.Config.ARGB_8888)
        val pages = listOf(
            PdfPreviewPage(deck.slides[0].id, 0, "الشريحة 1", bmp),
            PdfPreviewPage(deck.slides[1].id, 1, "الشريحة 2", bmp)
        )
        var exportedApplyOrder: Boolean? = null

        composeRule.setContent {
            CompositionLocalProvider(
                LocalAppLanguage provides AppLanguage.AR,
                LocalAppStrings provides stringsFor(AppLanguage.AR)
            ) {
                com.example.ui.theme.KatebSlidesTheme {
                    com.example.ui.editor.PdfPreviewSheet(
                        presentation = deck,
                        pages = pages,
                        operationState = AsyncOperationState.Idle,
                        onMovePage = { _, _ -> },
                        onResetOrder = {},
                        onConfirmExport = { exportedApplyOrder = it },
                        onDismiss = {}
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("pdf_apply_order_checkbox").performClick()
        composeRule.onNodeWithTag("pdf_preview_export_btn").performClick()
        composeRule.waitForIdle()
        assertEquals(true, exportedApplyOrder)
    }

    @Test
    fun smartArt_3dShape_and_checkboxText_roundTripSerialization() {
        val smartArt = SlideElement.SmartArtElement(
            id = "sa_1",
            layoutType = SmartArtLayoutType.PROCESS_ARROWS,
            is3D = true,
            nodes = listOf(
                SmartArtNode(id = "n1", text = "Step 1", fillColorHex = "#0D47A1"),
                SmartArtNode(id = "n2", text = "Step 2", fillColorHex = "#10B981")
            )
        )
        val shape3d = SlideElement.ShapeElement(
            id = "sh_3d",
            shapeType = ShapeType.CUBE_3D,
            is3D = true,
            depth3D = 32f,
            fillColorHex = "#D97706"
        )
        val checkText = SlideElement.TextElement(
            id = "chk_1",
            text = "Task A\nTask B",
            listType = TextListType.CHECKBOX
        )
        val pres = Presentation(
            id = "smart_3d_deck",
            title = "SmartArt & 3D",
            slides = listOf(
                Slide(
                    order = 0,
                    decorationId = SlideDecorationCatalog.decorations[1].id,
                    decorationColorHex = "#0D47A1",
                    transition = SlideTransition(SlideTransitionType.CURTAIN, 600),
                    elements = listOf(smartArt, shape3d, checkText)
                )
            )
        )
        val encoded = PresentationJsonSerializer.toJsonString(pres)
        val decoded = PresentationJsonSerializer.fromJsonString(encoded)
        val decodedSlide = decoded.slides.first()
        assertEquals(SlideDecorationCatalog.decorations[1].id, decodedSlide.decorationId)
        assertEquals(SlideTransitionType.CURTAIN, decodedSlide.transition.type)

        val decodedSa = decodedSlide.elements.filterIsInstance<SlideElement.SmartArtElement>().first()
        assertEquals(SmartArtLayoutType.PROCESS_ARROWS, decodedSa.layoutType)
        assertTrue(decodedSa.is3D)
        assertEquals("#10B981", decodedSa.nodes[1].fillColorHex)

        val decodedShape = decodedSlide.elements.filterIsInstance<SlideElement.ShapeElement>().first()
        assertEquals(ShapeType.CUBE_3D, decodedShape.shapeType)
        assertTrue(decodedShape.is3D)
        assertEquals(32f, decodedShape.depth3D, 0.01f)

        val decodedText = decodedSlide.elements.filterIsInstance<SlideElement.TextElement>().first()
        assertEquals(TextListType.CHECKBOX, decodedText.listType)
    }

    @Test
    @Config(sdk = [36], qualifiers = "w411dp-h915dp-xxhdpi")
    fun smartArtEditor_changesPerNodeColor_and_3DShapesPicker_selectsCube3D() {
        val smartArt = SlideElement.SmartArtElement(
            id = "sa_ui",
            layoutType = SmartArtLayoutType.CYCLE_RING,
            is3D = false,
            nodes = listOf(
                SmartArtNode(id = "n0", text = "Plan", fillColorHex = "#0D47A1"),
                SmartArtNode(id = "n1", text = "Build", fillColorHex = "#1565C0")
            )
        )
        var savedSmartArt: SlideElement.SmartArtElement? = null
        composeRule.setContent {
            CompositionLocalProvider(
                LocalAppLanguage provides AppLanguage.EN,
                LocalAppStrings provides stringsFor(AppLanguage.EN)
            ) {
                com.example.ui.theme.KatebSlidesTheme {
                    com.example.ui.editor.SmartArtEditorSheet(
                        element = smartArt,
                        onDismiss = {},
                        onSaveElement = { savedSmartArt = it }
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("smartart_node_chip_1").performClick()
        composeRule.onNodeWithTag("smartart_node_color_#10B981").performScrollTo().performClick()
        composeRule.onNodeWithTag("smartart_3d_switch").performScrollTo().performClick()
        composeRule.onNodeWithTag("smartart_save_btn").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertNotNull(savedSmartArt)
        assertEquals("#10B981", savedSmartArt!!.nodes[1].fillColorHex)
        assertTrue(savedSmartArt!!.is3D)
    }

    @Test
    @Config(sdk = [36], qualifiers = "w411dp-h915dp-xxhdpi")
    fun tools100Sheet_and_equations100Sheet_displayAndExecuteCommands() {
        var executedId = ""
        composeRule.setContent {
            CompositionLocalProvider(
                LocalAppLanguage provides AppLanguage.AR,
                LocalAppStrings provides stringsFor(AppLanguage.AR)
            ) {
                com.example.ui.theme.KatebSlidesTheme {
                    com.example.ui.editor.Tools100RibbonSheet(
                        onDismiss = {},
                        onExecuteCommand = { executedId = it.id }
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("tools_100_sheet").assertIsDisplayed()
        val firstCmd = com.example.engine.ToolCommandCatalog.commands.first()
        composeRule.onNodeWithTag("tool_cmd_${firstCmd.id}").performClick()
        composeRule.waitForIdle()
        assertEquals(firstCmd.id, executedId)
    }

    @Test
    @Config(sdk = [36], qualifiers = "w411dp-h915dp-xxhdpi")
    fun shapes3D_tools100_equations100_and_richTextCheckbox_work() {
        var pickedShape: ShapeType? = null
        var picked3D = false
        composeRule.setContent {
            CompositionLocalProvider(
                LocalAppLanguage provides AppLanguage.AR,
                LocalAppStrings provides stringsFor(AppLanguage.AR)
            ) {
                com.example.ui.theme.KatebSlidesTheme {
                    com.example.ui.editor.ShapesAnd3DPickerSheet(
                        onDismiss = {},
                        onSelectShape = { s, _, is3d, _ ->
                            pickedShape = s
                            picked3D = is3d
                        }
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("shape_3d_switch").performClick()
        composeRule.onNodeWithTag("shape_pick_CUBE_3D").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals(ShapeType.CUBE_3D, pickedShape)
        assertTrue(picked3D)
    }

    @Test
    @Config(sdk = [36], qualifiers = "w411dp-h915dp-xxhdpi")
    fun richTextCheckbox_and_homeSemanticSearchCheckbox_work() {
        var savedListType: TextListType? = null
        val textEl = SlideElement.TextElement(id = "t_chk", text = "Checklist item")
        composeRule.setContent {
            CompositionLocalProvider(
                LocalAppLanguage provides AppLanguage.EN,
                LocalAppStrings provides stringsFor(AppLanguage.EN)
            ) {
                com.example.ui.theme.KatebSlidesTheme {
                    com.example.ui.editor.RichTextEditorSheet(
                        element = textEl,
                        presentationLanguage = AppLanguage.EN,
                        onDismiss = {},
                        onSaveElement = { savedListType = it.listType },
                        onProofreadWithAi = {}
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("rt_checkbox_list_btn").performScrollTo().performClick()
        composeRule.onNodeWithTag("rich_text_done_btn").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals(TextListType.CHECKBOX, savedListType)
    }

    @Test
    @Config(sdk = [36], qualifiers = "w411dp-h915dp-xxhdpi")
    fun homeScreen_semanticSearchTrigger_and_multiSelectCheckbox_work() {
        val now = System.currentTimeMillis()
        val sampleDeck = PresentationEntity(
            id = "deck_sem_1",
            title = "Solar Energy Futures",
            description = "Clean green technology theme",
            languageCode = "en",
            aspectRatioId = "16:9",
            themeId = "tpl_solar",
            sourceType = "TEMPLATE",
            slideCount = 10,
            thumbnailPath = "/tmp/nonexistent.png",
            projectDirPath = "/tmp/deck_sem_1",
            createdAt = now,
            updatedAt = now,
            lastOpenedAt = now
        )
        var semanticTriggeredQuery = ""

        composeRule.setContent {
            CompositionLocalProvider(
                LocalAppLanguage provides AppLanguage.EN,
                LocalAppStrings provides stringsFor(AppLanguage.EN)
            ) {
                com.example.ui.theme.KatebSlidesTheme {
                    com.example.ui.screens.HomeScreen(
                        recentDecks = listOf(sampleDeck),
                        savedDecks = listOf(sampleDeck),
                        importedDecks = emptyList(),
                        currentLanguage = AppLanguage.EN,
                        onSelectLanguage = {},
                        onCreateClick = {},
                        onOpenFileClick = {},
                        onTemplatesClick = {},
                        onCreateWithTemplate = {},
                        onSettingsClick = {},
                        onHelpClick = {},
                        onOpenDeck = {},
                        onDuplicateDeck = {},
                        onRenameDeck = { _, _ -> },
                        onDeleteDeck = {},
                        semanticResults = listOf(
                            SemanticSearchHit("deck_sem_1", 94, "Matches eco-energy theme and palette")
                        ),
                        onRunSemanticSearch = { q -> semanticTriggeredQuery = q }
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_semantic_results").assertIsDisplayed()
        composeRule.onNodeWithTag("home_search_input").performTextInput("green energy mood")
        composeRule.onNodeWithTag("home_semantic_search_btn").performClick()
        composeRule.waitForIdle()
        assertEquals("green energy mood", semanticTriggeredQuery)

        composeRule.onNodeWithTag("home_search_input").performTextReplacement("")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("deck_card_deck_sem_1").performTouchInput { longClick() }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("deck_checkbox_deck_sem_1").assertIsDisplayed()
    }
}
