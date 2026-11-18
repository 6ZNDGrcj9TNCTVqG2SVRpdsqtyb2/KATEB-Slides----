package com.example.engines

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Xml
import com.example.core.localization.AppLanguage
import com.example.data.local.ProjectStorageManager
import com.example.engine.Presentation
import com.example.engine.PresentationSourceType
import com.example.engine.Slide
import com.example.engine.SlideAspectRatio
import com.example.engine.SlideBackground
import com.example.engine.SlideElement
import com.example.engine.TextAlignment
import com.example.engine.WritingDirection
import com.example.engine.detectTextIsRtl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import java.util.zip.ZipInputStream

object DocumentImportEngine {

    suspend fun importDocxToPresentation(
        inputStream: InputStream,
        presentationId: String,
        title: String,
        language: AppLanguage
    ): Presentation = withContext(Dispatchers.IO) {
        var documentXml: ByteArray? = null
        ZipInputStream(inputStream).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (entry.name == "word/document.xml") {
                    documentXml = zis.readBytes()
                    break
                }
                entry = zis.nextEntry
            }
        }
        val paragraphs = if (documentXml != null) extractDocxParagraphs(documentXml!!) else emptyList()
        buildSlidesFromParagraphs(
            presentationId = presentationId,
            title = title,
            paragraphs = paragraphs,
            language = language,
            sourceType = PresentationSourceType.IMPORTED_DOCX
        )
    }

    suspend fun importTxtToPresentation(
        inputStream: InputStream,
        presentationId: String,
        title: String,
        language: AppLanguage
    ): Presentation = withContext(Dispatchers.IO) {
        val text = inputStream.bufferedReader(Charsets.UTF_8).readText()
        val paragraphs = text.split(Regex("\\n\\s*\\n"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        buildSlidesFromParagraphs(
            presentationId = presentationId,
            title = title,
            paragraphs = paragraphs,
            language = language,
            sourceType = PresentationSourceType.IMPORTED_TXT
        )
    }

    suspend fun importPdfToPresentation(
        context: Context,
        uri: Uri,
        presentationId: String,
        title: String,
        storage: ProjectStorageManager,
        language: AppLanguage
    ): Presentation = withContext(Dispatchers.IO) {
        val tempPdf = File(context.cacheDir, "import_${UUID.randomUUID()}.pdf")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempPdf).use { output -> input.copyTo(output) }
        }
        val slides = mutableListOf<Slide>()
        try {
            val pfd = ParcelFileDescriptor.open(tempPdf, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            val maxPages = renderer.pageCount.coerceAtMost(com.example.engine.MAX_SLIDES_PER_DECK)
            for (i in 0 until maxPages) {
                renderer.openPage(i).use { page ->
                    val bmp = Bitmap.createBitmap(1600, (1600f * page.height / page.width.coerceAtLeast(1)).toInt().coerceIn(800, 2200), Bitmap.Config.ARGB_8888)
                    bmp.eraseColor(android.graphics.Color.WHITE)
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    val path = storage.saveBitmapToProjectImages(presentationId, bmp, "pdf_page_$i")
                    bmp.recycle()
                    slides.add(
                        Slide(
                            order = i,
                            background = SlideBackground.Solid("#FFFFFF"),
                            elements = listOf(
                                SlideElement.ImageElement(
                                    x = 80f, y = 40f, width = 1760f, height = 1000f,
                                    localImagePath = path, fillCropMode = false, cornerRadius = 8f
                                )
                            )
                        )
                    )
                }
            }
            renderer.close()
            pfd.close()
        } finally {
            tempPdf.delete()
        }

        Presentation(
            id = presentationId,
            title = title,
            description = "Imported PDF",
            language = language,
            aspectRatio = SlideAspectRatio.RATIO_16_9,
            sourceType = PresentationSourceType.IMPORTED_PDF,
            slides = slides
        )
    }

    suspend fun importImagesToPresentation(
        uris: List<Uri>,
        presentationId: String,
        title: String,
        storage: ProjectStorageManager,
        language: AppLanguage
    ): Presentation = withContext(Dispatchers.IO) {
        val slides = uris.take(com.example.engine.MAX_SLIDES_PER_DECK).mapIndexedNotNull { idx, uri ->
            val path = storage.importUriToProjectImages(presentationId, uri) ?: return@mapIndexedNotNull null
            val caption = when (language) {
                AppLanguage.AR -> "الشريحة ${idx + 1}"
                AppLanguage.FA -> "اسلاید ${idx + 1}"
                AppLanguage.RU -> "Слайд ${idx + 1}"
                AppLanguage.IT -> "Diapositiva ${idx + 1}"
                AppLanguage.UR -> "سلائیڈ ${idx + 1}"
                AppLanguage.ZH -> "幻灯片 ${idx + 1}"
                AppLanguage.EN -> "Slide ${idx + 1}"
            }
            Slide(
                order = idx,
                background = SlideBackground.Gradient("#0F172A", "#1E293B", 135f),
                elements = listOf(
                    SlideElement.ImageElement(
                        x = 160f, y = 110f, width = 1600f, height = 800f,
                        localImagePath = path, cornerRadius = 24f, hasShadow = true
                    ),
                    SlideElement.TextElement(
                        x = 160f, y = 935f, width = 1600f, height = 95f,
                        text = caption,
                        fontSizeSp = 32f,
                        textColorHex = "#F8FAFC",
                        textAlign = TextAlignment.CENTER,
                        isBold = true,
                        writingDirection = when (language) {
                            AppLanguage.AR, AppLanguage.FA, AppLanguage.UR -> WritingDirection.RTL
                            else -> WritingDirection.LTR
                        }
                    )
                )
            )
        }
        Presentation(
            id = presentationId,
            title = title,
            description = "Created from Photos",
            language = language,
            aspectRatio = SlideAspectRatio.RATIO_16_9,
            sourceType = PresentationSourceType.IMPORTED_IMAGES,
            slides = slides
        )
    }

    fun extractDocxParagraphs(xmlBytes: ByteArray): List<String> {
        val list = mutableListOf<String>()
        val parser = Xml.newPullParser().apply { setInput(ByteArrayInputStream(xmlBytes), "UTF-8") }
        var ev = parser.eventType
        val current = StringBuilder()
        var inParagraph = false
        while (ev != XmlPullParser.END_DOCUMENT) {
            when (ev) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "p") {
                        inParagraph = true
                        current.clear()
                    } else if (parser.name == "t" && inParagraph) {
                        current.append(parser.nextText())
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "p" && inParagraph) {
                        val line = current.toString().trim()
                        if (line.isNotEmpty()) list.add(line)
                        inParagraph = false
                    }
                }
            }
            ev = parser.next()
        }
        return list
    }

    private fun buildSlidesFromParagraphs(
        presentationId: String,
        title: String,
        paragraphs: List<String>,
        language: AppLanguage,
        sourceType: PresentationSourceType
    ): Presentation {
        val defaultFont = when (language) {
            AppLanguage.AR, AppLanguage.UR -> "cairo"
            AppLanguage.FA -> "vazirmatn"
            AppLanguage.EN, AppLanguage.RU, AppLanguage.IT, AppLanguage.ZH -> "plus_jakarta_sans"
        }
        val slides = mutableListOf<Slide>()
        slides.add(
            Slide(
                order = 0,
                background = SlideBackground.Gradient("#0D47A1", "#1565C0", 135f),
                elements = listOf(
                    SlideElement.TextElement(
                        x = 140f, y = 320f, width = 1640f, height = 220f,
                        text = title,
                        fontFamilyId = defaultFont,
                        fontSizeSp = 64f,
                        textColorHex = "#FFFFFF",
                        isBold = true,
                        textAlign = TextAlignment.CENTER,
                        writingDirection = if (detectTextIsRtl(title)) WritingDirection.RTL else WritingDirection.AUTO
                    )
                )
            )
        )
        val chunks = paragraphs.chunked(3).take(com.example.engine.MAX_SLIDES_PER_DECK - 1)
        chunks.forEachIndexed { idx, chunk ->
            val heading = chunk.firstOrNull()?.take(80) ?: title
            val body = if (chunk.size > 1) chunk.drop(1).joinToString("\n\n") else chunk.firstOrNull().orEmpty()
            val isRtl = detectTextIsRtl(heading + body)
            slides.add(
                Slide(
                    order = idx + 1,
                    background = SlideBackground.Solid("#F8FAFC"),
                    elements = listOf(
                        SlideElement.ShapeElement(
                            x = 0f, y = 0f, width = 1920f, height = 18f,
                            shapeType = com.example.engine.ShapeType.RECTANGLE,
                            fillColorHex = "#0D47A1", strokeColorHex = "#0D47A1", strokeWidth = 0f
                        ),
                        SlideElement.TextElement(
                            x = 120f, y = 90f, width = 1680f, height = 140f,
                            text = heading,
                            fontFamilyId = defaultFont,
                            fontSizeSp = 44f,
                            textColorHex = "#0D47A1",
                            isBold = true,
                            writingDirection = if (isRtl) WritingDirection.RTL else WritingDirection.LTR
                        ),
                        SlideElement.TextElement(
                            x = 120f, y = 260f, width = 1680f, height = 700f,
                            text = body,
                            fontFamilyId = defaultFont,
                            fontSizeSp = 30f,
                            textColorHex = "#1E293B",
                            writingDirection = if (isRtl) WritingDirection.RTL else WritingDirection.LTR
                        )
                    )
                )
            )
        }
        return Presentation(
            id = presentationId,
            title = title,
            description = "Imported Document",
            language = language,
            aspectRatio = SlideAspectRatio.RATIO_16_9,
            sourceType = sourceType,
            slides = slides
        )
    }
}
