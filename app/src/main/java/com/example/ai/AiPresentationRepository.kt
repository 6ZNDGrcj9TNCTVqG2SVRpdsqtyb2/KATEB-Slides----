package com.example.ai

import android.graphics.Bitmap
import com.example.core.localization.AppLanguage
import com.example.engine.ChartSeries
import com.example.engine.ChartType
import com.example.engine.Presentation
import com.example.engine.PresentationSourceType
import com.example.engine.ShapeType
import com.example.engine.Slide
import com.example.engine.SlideAspectRatio
import com.example.engine.SlideBackground
import com.example.engine.SlideElement
import com.example.engine.SlideTransition
import com.example.engine.SlideTransitionType
import com.example.engine.SmartLayoutGeometryService
import com.example.engine.TextAlignment
import com.example.engine.TextListType
import com.example.engine.WritingDirection
import com.example.engine.detectTextIsRtl
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.AspectRatio
import com.google.firebase.ai.type.ImageConfig
import com.google.firebase.ai.type.ImagePart
import com.google.firebase.ai.type.ImageSize
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.util.UUID

data class SlideDesignSuggestion(
    val balanceScore: Int,
    val recommendation: String,
    val layoutStrategy: String,
    val suggestedActionLabel: String
)

data class SmartThemeSuggestion(
    val themeName: String,
    val primaryColorHex: String,
    val secondaryColorHex: String,
    val accentColorHex: String,
    val bgColorHex: String,
    val titleColorHex: String,
    val bodyColorHex: String,
    val fontFamilyId: String,
    val backgroundStyle: String,
    val rationale: String
)

data class SemanticSearchCandidate(
    val id: String,
    val title: String,
    val description: String,
    val languageCode: String,
    val themeId: String,
    val themeCategory: String,
    val colorPaletteHint: String,
    val slideCount: Int,
    val sourceType: String
)

data class SemanticSearchHit(
    val presentationId: String,
    val score: Int,
    val reason: String
)

class AiPresentationRepository {

    private val deckSchema = Schema.obj(
        properties = mapOf(
            "presentationTitle" to Schema.string("Title of the presentation"),
            "primaryColorHex" to Schema.string("Hex color e.g. #0D47A1"),
            "accentColorHex" to Schema.string("Accent hex color e.g. #00897B"),
            "slides" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "title" to Schema.string("Slide heading"),
                        "bulletPoints" to Schema.array(
                            items = Schema.string("Concise slide bullet point"),
                            description = "3 to 5 informative points"
                        ),
                        "speakerNotes" to Schema.string("Presenter talking points"),
                        "layoutType" to Schema.enumeration(
                            listOf("TITLE_COVER", "CONTENT", "TWO_COLUMN", "DATA_CHART", "REFERENCES"),
                            "Layout style for this slide"
                        ),
                        "chartTitle" to Schema.string("Optional chart title if DATA_CHART", nullable = true),
                        "chartLabels" to Schema.array(Schema.string(), nullable = true),
                        "chartValues" to Schema.array(Schema.double(), nullable = true)
                    ),
                    optionalProperties = listOf("chartTitle", "chartLabels", "chartValues")
                )
            )
        )
    )

    private val translationSchema = Schema.obj(
        properties = mapOf(
            "translatedTitle" to Schema.string("Translated deck title"),
            "slides" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "texts" to Schema.array(Schema.string(), "Translated text elements in exact order"),
                        "speakerNotes" to Schema.string("Translated speaker notes")
                    )
                )
            )
        )
    )

    private val designAdvisorSchema = Schema.obj(
        properties = mapOf(
            "balanceScore" to Schema.integer("Layout & text-vs-visual balance score 1..100"),
            "recommendation" to Schema.string("Concise real-time design advice in requested language"),
            "layoutStrategy" to Schema.enumeration(
                listOf("SPLIT_VISUAL", "TWO_COLUMN", "COMPACT_GRID", "BREATHING_ROOM"),
                "Geometric layout adjustment strategy"
            ),
            "suggestedActionLabel" to Schema.string("Short button label in requested language")
        )
    )

    private val smartThemeSchema = Schema.obj(
        properties = mapOf(
            "themeName" to Schema.string("Descriptive theme title"),
            "primaryColorHex" to Schema.string("6-digit hex color e.g. #1E3A8A"),
            "secondaryColorHex" to Schema.string("6-digit hex color e.g. #3B82F6"),
            "accentColorHex" to Schema.string("6-digit hex accent e.g. #F59E0B"),
            "bgColorHex" to Schema.string("Slide background 6-digit hex e.g. #F8FAFC"),
            "titleColorHex" to Schema.string("High-contrast title hex color"),
            "bodyColorHex" to Schema.string("High-contrast body hex color"),
            "fontFamilyId" to Schema.enumeration(
                listOf(
                    "cairo", "amiri", "tajawal", "almarai", "noto_kufi_arabic", "vazirmatn",
                    "plus_jakarta_sans", "montserrat", "outfit", "playfair_display", "lora", "roboto_mono"
                ),
                "Typography pairing"
            ),
            "backgroundStyle" to Schema.enumeration(
                listOf("SOLID", "GRADIENT"),
                "Recommended slide backdrop mode"
            ),
            "rationale" to Schema.string("Short explanation of palette & typography fit in requested language")
        )
    )

    private val structuredModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = deckSchema
            }
        )
    }

    private val translationModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = translationSchema
            }
        )
    }

    private val designAdvisorModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = designAdvisorSchema
            }
        )
    }

    private val semanticSearchSchema = Schema.obj(
        properties = mapOf(
            "matches" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "presentationId" to Schema.string("Exact candidate presentation ID"),
                        "score" to Schema.integer("Semantic relevance score 0..100"),
                        "reason" to Schema.string("Brief explanation of semantic/theme/palette/mood match in requested language")
                    )
                )
            )
        )
    )

    private val smartThemeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = smartThemeSchema
            }
        )
    }

    private val semanticSearchModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = semanticSearchSchema
            }
        )
    }

    private val textModel by lazy {
        Firebase.ai.generativeModel(modelName = "gemini-3.8-flash")
    }

    private val imageModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-nano-banana-2.1",
            generationConfig = generationConfig {
                responseModalities = listOf(ResponseModality.IMAGE)
                imageConfig = ImageConfig.builder()
                    .setAspectRatio(AspectRatio.LANDSCAPE_16x9)
                    .setImageSize(ImageSize.SIZE_1K)
                    .build()
            }
        )
    }

    private fun promptLanguageName(language: AppLanguage): String = when (language) {
        AppLanguage.AR -> "Arabic (العربية الفصحى)"
        AppLanguage.EN -> "English"
        AppLanguage.FA -> "Persian (فارسی)"
        AppLanguage.RU -> "Russian (Русский)"
        AppLanguage.IT -> "Italian (Italiano)"
        AppLanguage.UR -> "Urdu (اردو)"
        AppLanguage.ZH -> "Simplified Chinese (中文)"
    }

    private fun defaultFontFor(language: AppLanguage): String = when (language) {
        AppLanguage.AR, AppLanguage.UR -> "cairo"
        AppLanguage.FA -> "vazirmatn"
        AppLanguage.EN, AppLanguage.RU, AppLanguage.IT, AppLanguage.ZH -> "plus_jakarta_sans"
    }

    suspend fun generatePresentation(
        topicPrompt: String,
        slideCount: Int,
        language: AppLanguage,
        aspectRatio: SlideAspectRatio
    ): Presentation {
        val count = slideCount.coerceIn(3, 100)
        val langName = promptLanguageName(language)
        val fullPrompt = """
            Create a professional, well-structured presentation in $langName with exactly $count slides.
            Topic/Instruction: $topicPrompt
            Requirements:
            - Slide 1 must use layoutType TITLE_COVER.
            - Include at least one DATA_CHART slide with meaningful numeric statistics if relevant to the topic.
            - The final slide must use layoutType REFERENCES and list credible academic/topic references.
            - Every slide needs concise, clear bulletPoints and helpful speakerNotes.
        """.trimIndent()

        return runAiStructuredDeck(fullPrompt, topicPrompt, language, aspectRatio)
    }

    suspend fun summarizeResearchToSlides(
        researchText: String,
        slideCount: Int,
        language: AppLanguage,
        aspectRatio: SlideAspectRatio
    ): Presentation {
        val count = slideCount.coerceIn(4, 100)
        val langName = promptLanguageName(language)
        val prompt = """
            Transform the following academic research or text into a structured scientific presentation in $langName with $count slides.
            Include: Title Cover, Introduction, Objectives, Methodology, Key Results (use DATA_CHART if numeric data exists), Conclusion, and References.
            Source text:
            ${researchText.take(12000)}
        """.trimIndent()

        return runAiStructuredDeck(prompt, "Research Summary", language, aspectRatio)
    }

    suspend fun semanticSearchPresentations(
        query: String,
        candidates: List<SemanticSearchCandidate>,
        uiLanguage: AppLanguage
    ): List<SemanticSearchHit> {
        if (query.isBlank() || candidates.isEmpty()) return emptyList()
        val langName = promptLanguageName(uiLanguage)
        val catalogJson = org.json.JSONArray().apply {
            candidates.take(60).forEach { c ->
                put(
                    JSONObject().apply {
                        put("id", c.id)
                        put("title", c.title)
                        put("description", c.description)
                        put("language", c.languageCode)
                        put("themeId", c.themeId)
                        put("themeCategory", c.themeCategory)
                        put("colorPaletteHint", c.colorPaletteHint)
                        put("slideCount", c.slideCount)
                        put("sourceType", c.sourceType)
                    }
                )
            }
        }
        val prompt = """
            Perform semantic search over the user's presentation catalog based on presentation theme, color palette, mood, domain, or meaning.
            Query: "$query"
            Response reason language: $langName
            Return matches sorted from highest score (0..100) to lowest, excluding irrelevant entries (score < 35).
            Candidates:
            $catalogJson
        """.trimIndent()

        return try {
            val response = withTimeout(30_000L) { semanticSearchModel.generateContent(prompt) }
            val json = JSONObject(response.text ?: "{}")
            val arr = json.optJSONArray("matches") ?: return emptyList()
            val validIds = candidates.map { it.id }.toSet()
            (0 until arr.length()).mapNotNull { i ->
                val obj = arr.optJSONObject(i) ?: return@mapNotNull null
                val id = obj.optString("presentationId", "")
                val score = obj.optInt("score", 0).coerceIn(0, 100)
                if (id !in validIds || score < 35) return@mapNotNull null
                SemanticSearchHit(
                    presentationId = id,
                    score = score,
                    reason = obj.optString("reason", "").trim()
                )
            }.sortedByDescending { it.score }
        } catch (e: TimeoutCancellationException) {
            throw IllegalStateException("ai_timeout", e)
        } catch (e: CancellationException) {
            throw e
        }
    }

    suspend fun summarizePresentation(
        presentation: Presentation,
        uiLanguage: AppLanguage
    ): String {
        val langName = promptLanguageName(uiLanguage)
        val outline = presentation.slides.mapIndexed { idx, s ->
            val texts = s.elements.filterIsInstance<SlideElement.TextElement>()
                .joinToString(" | ") { it.text.replace('\n', ' ') }
            "Slide ${idx + 1}: $texts"
        }.joinToString("\n")
        val prompt = """
            Summarize this presentation "${presentation.title}" concisely in $langName using 4-5 clear bullet points.
            $outline
        """.trimIndent()
        return try {
            val response = withTimeout(45_000L) { textModel.generateContent(prompt) }
            response.text?.trim()?.takeIf { it.isNotEmpty() } ?: ""
        } catch (e: CancellationException) {
            throw e
        }
    }

    suspend fun suggestSlideLayoutDesign(
        slide: Slide,
        aspectRatio: SlideAspectRatio,
        uiLanguage: AppLanguage
    ): SlideDesignSuggestion {
        val langName = promptLanguageName(uiLanguage)
        val textElements = slide.elements.filterIsInstance<SlideElement.TextElement>()
        val imageCount = slide.elements.count { it is SlideElement.ImageElement }
        val chartCount = slide.elements.count { it is SlideElement.ChartElement || it is SlideElement.TableElement }
        val iconOrShapeCount = slide.elements.count { it is SlideElement.IconElement || it is SlideElement.ShapeElement }
        val totalWords = textElements.sumOf { it.text.split(Regex("\\s+")).count { w -> w.isNotBlank() } }
        val overlaps = SmartLayoutGeometryService.countOverlaps(
            slide,
            aspectRatio.defaultWidth,
            aspectRatio.defaultHeight
        )

        val prompt = """
            Analyze the current presentation slide composition and provide real-time design advice in $langName.
            Metrics:
            - AspectRatio: ${aspectRatio.id} (${aspectRatio.defaultWidth}x${aspectRatio.defaultHeight})
            - TextElements: ${textElements.size} (TotalWords: $totalWords)
            - ImageElements: $imageCount
            - ChartOrTableElements: $chartCount
            - ShapesAndIcons: $iconOrShapeCount
            - DetectedElementOverlaps: $overlaps
            Respond strictly as JSON matching the schema. Recommend balancing text density vs visuals or fixing overlaps cleanly.
        """.trimIndent()

        return try {
            val response = withTimeout(45_000L) { designAdvisorModel.generateContent(prompt) }
            val json = JSONObject(response.text ?: "{}")
            SlideDesignSuggestion(
                balanceScore = json.optInt("balanceScore", 75).coerceIn(10, 100),
                recommendation = json.optString("recommendation", "").trim(),
                layoutStrategy = json.optString("layoutStrategy", "BREATHING_ROOM"),
                suggestedActionLabel = json.optString("suggestedActionLabel", "")
            )
        } catch (e: CancellationException) {
            throw e
        }
    }

    fun applyDesignSuggestionToSlide(
        slide: Slide,
        suggestion: SlideDesignSuggestion,
        canvasWidth: Float,
        canvasHeight: Float
    ): Slide {
        val arranged = SmartLayoutGeometryService.autoArrangeSlide(slide, canvasWidth, canvasHeight)
        val texts = arranged.elements.filterIsInstance<SlideElement.TextElement>()
        val visuals = arranged.elements.filter {
            it is SlideElement.ImageElement || it is SlideElement.ChartElement ||
                it is SlideElement.TableElement || it is SlideElement.IconElement
        }
        return when (suggestion.layoutStrategy) {
            "SPLIT_VISUAL" -> {
                if (texts.isNotEmpty() && visuals.isNotEmpty()) {
                    val margin = 64f
                    val gap = 32f
                    val halfW = (canvasWidth - margin * 2f - gap) / 2f
                    val updated = arranged.elements.map { el ->
                        when {
                            el in texts -> el.withTransform(
                                x = margin,
                                width = halfW.coerceAtLeast(220f)
                            )
                            el in visuals -> el.withTransform(
                                x = margin + halfW + gap,
                                width = halfW.coerceAtLeast(220f)
                            )
                            else -> el
                        }
                    }
                    arranged.copy(elements = updated)
                } else {
                    arranged
                }
            }
            else -> arranged
        }
    }

    suspend fun suggestSmartTheme(
        presentation: Presentation,
        uiLanguage: AppLanguage
    ): SmartThemeSuggestion {
        val langName = promptLanguageName(uiLanguage)
        val sampleText = presentation.slides.take(4).flatMap { s ->
            s.elements.filterIsInstance<SlideElement.TextElement>().map { it.text }
        }.joinToString(" | ").take(600)

        val prompt = """
            You are a Smart Theme Engine. Suggest an expressive, high-contrast color palette, typography id, and backdrop style for this presentation.
            Title: "${presentation.title}"
            Description: "${presentation.description}"
            ContentLanguage: ${presentation.language.code}
            SampleContent: "$sampleText"
            Provide the rationale in $langName and pick valid 6-digit #RRGGBB colors with accessible text contrast against bgColorHex.
        """.trimIndent()

        return try {
            val response = withTimeout(45_000L) { smartThemeModel.generateContent(prompt) }
            val json = JSONObject(response.text ?: "{}")
            SmartThemeSuggestion(
                themeName = json.optString("themeName", "Smart Theme").ifBlank { "Smart Theme" },
                primaryColorHex = normalizeHex(json.optString("primaryColorHex"), "#1E3A8A"),
                secondaryColorHex = normalizeHex(json.optString("secondaryColorHex"), "#2563EB"),
                accentColorHex = normalizeHex(json.optString("accentColorHex"), "#F59E0B"),
                bgColorHex = normalizeHex(json.optString("bgColorHex"), "#F8FAFC"),
                titleColorHex = normalizeHex(json.optString("titleColorHex"), "#0F172A"),
                bodyColorHex = normalizeHex(json.optString("bodyColorHex"), "#334155"),
                fontFamilyId = json.optString("fontFamilyId", defaultFontFor(presentation.language)),
                backgroundStyle = json.optString("backgroundStyle", "GRADIENT"),
                rationale = json.optString("rationale", "")
            )
        } catch (e: CancellationException) {
            throw e
        }
    }

    fun applySmartThemeToPresentation(
        presentation: Presentation,
        theme: SmartThemeSuggestion
    ): Presentation {
        val updatedSlides = presentation.slides.mapIndexed { idx, slide ->
            val bg = if (idx == 0 || theme.backgroundStyle == "GRADIENT" && idx == presentation.slides.lastIndex) {
                SlideBackground.Gradient(theme.primaryColorHex, theme.secondaryColorHex, 135f)
            } else {
                SlideBackground.Solid(theme.bgColorHex)
            }
            val isCover = idx == 0
            val updatedElements = slide.elements.map { el ->
                when (el) {
                    is SlideElement.TextElement -> el.copy(
                        fontFamilyId = theme.fontFamilyId,
                        textColorHex = when {
                            isCover -> "#FFFFFF"
                            el.isBold && el.fontSizeSp >= 36f -> theme.titleColorHex
                            else -> theme.bodyColorHex
                        }
                    )
                    is SlideElement.ShapeElement -> el.copy(
                        fillColorHex = theme.primaryColorHex,
                        strokeColorHex = theme.accentColorHex
                    )
                    is SlideElement.IconElement -> el.copy(
                        tintColorHex = theme.accentColorHex
                    )
                    else -> el
                }
            }
            slide.copy(background = bg, elements = updatedElements)
        }
        return presentation.copy(slides = updatedSlides, updatedAt = System.currentTimeMillis())
    }

    suspend fun proofreadAndEnhanceText(text: String, language: AppLanguage): String {
        val langName = promptLanguageName(language)
        val prompt = """
            Proofread, fix grammar/spelling, and elevate the academic clarity of the following $langName presentation text.
            Return ONLY the improved text without quotes or explanations:
            $text
        """.trimIndent()
        return try {
            val response = withTimeout(45_000L) { textModel.generateContent(prompt) }
            response.text?.trim()?.takeIf { it.isNotEmpty() }
                ?: throw IllegalStateException("empty_ai_response")
        } catch (e: TimeoutCancellationException) {
            throw IllegalStateException("ai_timeout", e)
        } catch (e: CancellationException) {
            throw e
        }
    }

    suspend fun generateSpeakerNotes(slide: Slide, deckTitle: String, language: AppLanguage): String {
        val slideText = slide.elements.filterIsInstance<SlideElement.TextElement>()
            .joinToString("\n") { it.text }
        val langName = promptLanguageName(language)
        val prompt = """
            Write concise, natural presenter speaker notes in $langName (3-4 sentences) for a slide in "$deckTitle".
            Slide content:
            $slideText
        """.trimIndent()
        return try {
            val response = withTimeout(45_000L) { textModel.generateContent(prompt) }
            response.text?.trim()?.takeIf { it.isNotEmpty() }
                ?: throw IllegalStateException("empty_ai_response")
        } catch (e: CancellationException) {
            throw e
        }
    }

    suspend fun translatePresentation(
        presentation: Presentation,
        targetLanguage: AppLanguage
    ): Presentation {
        val targetName = promptLanguageName(targetLanguage)
        val payload = JSONObject().apply {
            put("title", presentation.title)
            put("slides", org.json.JSONArray().apply {
                presentation.slides.forEach { s ->
                    put(JSONObject().apply {
                        put("texts", org.json.JSONArray(
                            s.elements.filterIsInstance<SlideElement.TextElement>().map { it.text }
                        ))
                        put("speakerNotes", s.speakerNotes)
                    })
                }
            })
        }

        val prompt = """
            Translate the following presentation JSON into $targetName accurately, preserving the exact number of slides and text items per slide:
            $payload
        """.trimIndent()

        return try {
            val response = withTimeout(60_000L) { translationModel.generateContent(prompt) }
            val jsonStr = response.text ?: throw IllegalStateException("empty_ai_response")
            val root = JSONObject(jsonStr)
            val newTitle = root.optString("translatedTitle", presentation.title)
            val slidesArr = root.optJSONArray("slides")
            val targetFont = defaultFontFor(targetLanguage)
            val targetDir = if (targetLanguage.isRtl) WritingDirection.RTL else WritingDirection.LTR

            val updatedSlides = presentation.slides.mapIndexed { sIdx, slide ->
                val sObj = slidesArr?.optJSONObject(sIdx)
                val translatedTexts = sObj?.optJSONArray("texts")
                var textCursor = 0
                val updatedEls = slide.elements.map { el ->
                    if (el is SlideElement.TextElement) {
                        val t = translatedTexts?.optString(textCursor++, el.text) ?: el.text
                        el.copy(
                            text = t,
                            fontFamilyId = targetFont,
                            writingDirection = targetDir
                        )
                    } else el
                }
                slide.copy(
                    speakerNotes = sObj?.optString("speakerNotes", slide.speakerNotes) ?: slide.speakerNotes,
                    elements = updatedEls
                )
            }
            presentation.copy(
                title = newTitle,
                language = targetLanguage,
                slides = updatedSlides,
                updatedAt = System.currentTimeMillis()
            )
        } catch (e: CancellationException) {
            throw e
        }
    }

    suspend fun generateSlideIllustration(prompt: String): Bitmap {
        return try {
            val response = withTimeout(60_000L) {
                imageModel.generateContent("Clean, modern presentation illustration without messy text: $prompt")
            }
            val imagePart = response.candidates.firstOrNull()
                ?.content?.parts?.filterIsInstance<ImagePart>()
                ?.firstOrNull()
            imagePart?.image ?: throw IllegalStateException("empty_ai_image")
        } catch (e: TimeoutCancellationException) {
            throw IllegalStateException("ai_timeout", e)
        } catch (e: CancellationException) {
            throw e
        }
    }

    private suspend fun runAiStructuredDeck(
        prompt: String,
        fallbackTitle: String,
        language: AppLanguage,
        aspectRatio: SlideAspectRatio
    ): Presentation {
        try {
            val response = withTimeout(60_000L) { structuredModel.generateContent(prompt) }
            val jsonString = response.text ?: throw IllegalStateException("empty_ai_response")
            val root = JSONObject(jsonString)

            val title = root.optString("presentationTitle", fallbackTitle).ifBlank { fallbackTitle }
            val primaryHex = normalizeHex(root.optString("primaryColorHex"), "#0D47A1")
            val accentHex = normalizeHex(root.optString("accentColorHex"), "#00897B")
            val slidesJson = root.getJSONArray("slides")

            val w = aspectRatio.defaultWidth
            val h = aspectRatio.defaultHeight
            val font = defaultFontFor(language)

            val slides = (0 until slidesJson.length()).map { idx ->
                val sObj = slidesJson.getJSONObject(idx)
                val sTitle = sObj.optString("title", "")
                val bulletsArr = sObj.optJSONArray("bulletPoints")
                val bullets = (0 until (bulletsArr?.length() ?: 0)).map { bulletsArr!!.getString(it) }
                val notes = sObj.optString("speakerNotes", "")
                val layoutType = sObj.optString("layoutType", "CONTENT")
                val dir = if (detectTextIsRtl(sTitle + bullets.joinToString())) WritingDirection.RTL else WritingDirection.LTR

                val elements = mutableListOf<SlideElement>()
                when (layoutType) {
                    "TITLE_COVER" -> {
                        elements.add(
                            SlideElement.ShapeElement(
                                x = w * 0.06f, y = h * 0.22f, width = 14f, height = h * 0.42f, zIndex = 1,
                                shapeType = ShapeType.RECTANGLE, fillColorHex = accentHex, strokeColorHex = accentHex, strokeWidth = 0f
                            )
                        )
                        elements.add(
                            SlideElement.TextElement(
                                x = w * 0.10f, y = h * 0.24f, width = w * 0.80f, height = h * 0.22f, zIndex = 2,
                                text = sTitle.ifBlank { title }, fontFamilyId = font, fontSizeSp = 64f,
                                textColorHex = "#FFFFFF", isBold = true, writingDirection = dir
                            )
                        )
                        elements.add(
                            SlideElement.TextElement(
                                x = w * 0.10f, y = h * 0.50f, width = w * 0.80f, height = h * 0.22f, zIndex = 3,
                                text = bullets.joinToString("\n"), fontFamilyId = font, fontSizeSp = 30f,
                                textColorHex = "#E2E8F0", writingDirection = dir
                            )
                        )
                        Slide(
                            order = idx,
                            background = SlideBackground.Gradient(primaryHex, "#1E293B", 135f),
                            transition = SlideTransition(SlideTransitionType.FADE, 500),
                            speakerNotes = notes,
                            elements = elements
                        )
                    }
                    "DATA_CHART" -> {
                        val cTitle = sObj.optString("chartTitle", sTitle).ifBlank { sTitle }
                        val labelsArr = sObj.optJSONArray("chartLabels")
                        val valsArr = sObj.optJSONArray("chartValues")
                        val labels = (0 until (labelsArr?.length() ?: 0)).map { labelsArr!!.getString(it) }
                            .ifEmpty { listOf("A", "B", "C", "D") }
                        val vals = (0 until (valsArr?.length() ?: 0)).map { valsArr!!.getDouble(it).toFloat() }
                            .ifEmpty { listOf(35f, 58f, 74f, 92f) }

                        elements.add(
                            SlideElement.TextElement(
                                x = w * 0.07f, y = h * 0.07f, width = w * 0.86f, height = h * 0.13f, zIndex = 1,
                                text = sTitle, fontFamilyId = font, fontSizeSp = 44f,
                                textColorHex = primaryHex, isBold = true, writingDirection = dir
                            )
                        )
                        elements.add(
                            SlideElement.ChartElement(
                                x = w * 0.07f, y = h * 0.23f, width = w * 0.52f, height = h * 0.66f, zIndex = 2,
                                chartType = ChartType.BAR, title = cTitle, labels = labels,
                                series = listOf(ChartSeries(cTitle.take(16), primaryHex, vals))
                            )
                        )
                        elements.add(
                            SlideElement.TextElement(
                                x = w * 0.62f, y = h * 0.25f, width = w * 0.31f, height = h * 0.62f, zIndex = 3,
                                text = bullets.joinToString("\n"), fontFamilyId = font, fontSizeSp = 28f,
                                textColorHex = "#1E293B", listType = TextListType.BULLET, writingDirection = dir
                            )
                        )
                        Slide(
                            order = idx,
                            background = SlideBackground.Solid("#F8FAFC"),
                            transition = SlideTransition(SlideTransitionType.WIPE, 450),
                            speakerNotes = notes,
                            elements = elements
                        )
                    }
                    "TWO_COLUMN" -> {
                        val half = (bullets.size + 1) / 2
                        val col1 = bullets.take(half)
                        val col2 = bullets.drop(half)
                        elements.add(
                            SlideElement.TextElement(
                                x = w * 0.07f, y = h * 0.08f, width = w * 0.86f, height = h * 0.14f, zIndex = 1,
                                text = sTitle, fontFamilyId = font, fontSizeSp = 44f,
                                textColorHex = primaryHex, isBold = true, writingDirection = dir
                            )
                        )
                        elements.add(
                            SlideElement.ShapeElement(
                                x = w * 0.07f, y = h * 0.26f, width = w * 0.40f, height = h * 0.62f, zIndex = 2,
                                shapeType = ShapeType.ROUNDED_RECT, fillColorHex = "#FFFFFF",
                                strokeColorHex = accentHex, strokeWidth = 3f, hasShadow = true
                            )
                        )
                        elements.add(
                            SlideElement.TextElement(
                                x = w * 0.09f, y = h * 0.30f, width = w * 0.36f, height = h * 0.54f, zIndex = 3,
                                text = col1.joinToString("\n"), fontFamilyId = font, fontSizeSp = 28f,
                                textColorHex = "#1E293B", listType = TextListType.BULLET, writingDirection = dir
                            )
                        )
                        elements.add(
                            SlideElement.ShapeElement(
                                x = w * 0.53f, y = h * 0.26f, width = w * 0.40f, height = h * 0.62f, zIndex = 4,
                                shapeType = ShapeType.ROUNDED_RECT, fillColorHex = "#FFFFFF",
                                strokeColorHex = primaryHex, strokeWidth = 3f, hasShadow = true
                            )
                        )
                        elements.add(
                            SlideElement.TextElement(
                                x = w * 0.55f, y = h * 0.30f, width = w * 0.36f, height = h * 0.54f, zIndex = 5,
                                text = col2.joinToString("\n"), fontFamilyId = font, fontSizeSp = 28f,
                                textColorHex = "#1E293B", listType = TextListType.BULLET, writingDirection = dir
                            )
                        )
                        Slide(
                            order = idx,
                            background = SlideBackground.Solid("#F8FAFC"),
                            transition = SlideTransition(SlideTransitionType.PUSH, 450),
                            speakerNotes = notes,
                            elements = elements
                        )
                    }
                    else -> {
                        val isRef = layoutType == "REFERENCES"
                        elements.add(
                            SlideElement.ShapeElement(
                                x = 0f, y = 0f, width = w, height = 18f, zIndex = 1,
                                shapeType = ShapeType.RECTANGLE, fillColorHex = primaryHex,
                                strokeColorHex = primaryHex, strokeWidth = 0f
                            )
                        )
                        elements.add(
                            SlideElement.TextElement(
                                x = w * 0.08f, y = h * 0.09f, width = w * 0.84f, height = h * 0.15f, zIndex = 2,
                                text = sTitle, fontFamilyId = font, fontSizeSp = 46f,
                                textColorHex = primaryHex, isBold = true, writingDirection = dir
                            )
                        )
                        elements.add(
                            SlideElement.TextElement(
                                x = w * 0.08f, y = h * 0.28f, width = w * 0.84f, height = h * 0.60f, zIndex = 3,
                                text = bullets.joinToString("\n"), fontFamilyId = font, fontSizeSp = 30f,
                                textColorHex = "#1E293B",
                                listType = if (isRef) TextListType.NUMBERED else TextListType.BULLET,
                                writingDirection = dir
                            )
                        )
                        Slide(
                            order = idx,
                            background = SlideBackground.Solid("#FFFFFF"),
                            transition = SlideTransition(SlideTransitionType.FADE, 450),
                            speakerNotes = notes,
                            elements = elements
                        )
                    }
                }
            }

            return Presentation(
                id = UUID.randomUUID().toString(),
                title = title,
                description = fallbackTitle.take(120),
                language = language,
                aspectRatio = aspectRatio,
                canvasWidth = w,
                canvasHeight = h,
                sourceType = PresentationSourceType.AI,
                slides = slides
            )
        } catch (e: TimeoutCancellationException) {
            throw IllegalStateException("ai_timeout", e)
        } catch (e: CancellationException) {
            throw e
        }
    }

    private fun normalizeHex(raw: String, fallback: String): String {
        val clean = raw.trim().removePrefix("#")
        return if (clean.length == 6 && clean.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) {
            "#${clean.uppercase()}"
        } else {
            fallback
        }
    }
}
