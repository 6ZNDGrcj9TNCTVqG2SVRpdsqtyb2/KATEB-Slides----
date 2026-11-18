package com.example.engines

import android.content.Context
import android.graphics.Bitmap
import android.util.Xml
import com.example.core.localization.AppLanguage
import com.example.data.local.ProjectStorageManager
import com.example.engine.Presentation
import com.example.engine.PresentationSourceType
import com.example.engine.ShapeType
import com.example.engine.Slide
import com.example.engine.SlideAspectRatio
import com.example.engine.SlideBackground
import com.example.engine.SlideElement
import com.example.engine.TableCell
import com.example.engine.TextAlignment
import com.example.engine.WritingDirection
import com.example.engine.detectTextIsRtl
import com.example.engine.parseColorInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object PptxEngine {

    private const val EMU_PER_LOGICAL_PX = 6350L // 1920 * 6350 = 12,192,000 EMU (13.333 in widescreen)

    suspend fun exportToPptx(
        context: Context,
        presentation: Presentation,
        outputFile: File,
        onProgress: (Float) -> Unit = {}
    ): File = withContext(Dispatchers.IO) {
        outputFile.parentFile?.mkdirs()
        onProgress(0.05f)
        val slideWidthEmu = (presentation.canvasWidth * EMU_PER_LOGICAL_PX).toLong().coerceAtLeast(3000000L)
        val slideHeightEmu = (presentation.canvasHeight * EMU_PER_LOGICAL_PX).toLong().coerceAtLeast(2000000L)

        val mediaEntries = mutableListOf<Pair<String, ByteArray>>()
        var mediaCounter = 1

        ZipOutputStream(FileOutputStream(outputFile)).use { zip ->
            val slideCount = presentation.slides.size.coerceAtLeast(1)

            // 1. _rels/.rels
            writeZipEntry(
                zip,
                "_rels/.rels",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="ppt/presentation.xml"/>
</Relationships>"""
            )

            // 2. ppt/theme/theme1.xml
            writeZipEntry(zip, "ppt/theme/theme1.xml", defaultThemeXml())

            // 3. ppt/slideMasters/slideMaster1.xml & rels
            writeZipEntry(zip, "ppt/slideMasters/slideMaster1.xml", defaultSlideMasterXml())
            writeZipEntry(
                zip,
                "ppt/slideMasters/_rels/slideMaster1.xml.rels",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout" Target="../slideLayouts/slideLayout1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme" Target="../theme/theme1.xml"/>
</Relationships>"""
            )

            // 4. ppt/slideLayouts/slideLayout1.xml & rels
            writeZipEntry(zip, "ppt/slideLayouts/slideLayout1.xml", defaultSlideLayoutXml())
            writeZipEntry(
                zip,
                "ppt/slideLayouts/_rels/slideLayout1.xml.rels",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster" Target="../slideMasters/slideMaster1.xml"/>
</Relationships>"""
            )

            // 5. Slides
            val slideRels = StringBuilder()
            val slideIdList = StringBuilder()
            val contentTypeOverrides = StringBuilder()

            presentation.slides.forEachIndexed { idx, slide ->
                val slideNum = idx + 1
                val relId = "rId${idx + 2}"
                slideRels.appendLine("""  <Relationship Id="$relId" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide" Target="slides/slide$slideNum.xml"/>""")
                slideIdList.appendLine("""    <p:sldId id="${256 + idx}" r:id="$relId"/>""")
                contentTypeOverrides.appendLine("""  <Override PartName="/ppt/slides/slide$slideNum.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>""")

                val thisSlideRels = StringBuilder()
                thisSlideRels.appendLine("""  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout" Target="../slideLayouts/slideLayout1.xml"/>""")

                val shapesXml = StringBuilder()
                var shapeId = 2
                var localRelIdx = 2

                slide.elements.sortedBy { it.zIndex }.forEach { el ->
                    val xEmu = (el.x * EMU_PER_LOGICAL_PX).toLong()
                    val yEmu = (el.y * EMU_PER_LOGICAL_PX).toLong()
                    val wEmu = (el.width * EMU_PER_LOGICAL_PX).toLong().coerceAtLeast(12700L)
                    val hEmu = (el.height * EMU_PER_LOGICAL_PX).toLong().coerceAtLeast(12700L)
                    val rotAttr = if (el.rotation != 0f) """ rot="${(el.rotation * 60000f).toInt()}"""" else ""

                    when (el) {
                        is SlideElement.TextElement -> {
                            shapesXml.append(
                                buildTextBoxXml(shapeId++, el, xEmu, yEmu, wEmu, hEmu, rotAttr)
                            )
                        }
                        is SlideElement.ShapeElement -> {
                            shapesXml.append(
                                buildShapeXml(shapeId++, el, xEmu, yEmu, wEmu, hEmu, rotAttr)
                            )
                        }
                        is SlideElement.TableElement -> {
                            shapesXml.append(
                                buildTableXml(shapeId++, el, xEmu, yEmu, wEmu, hEmu)
                            )
                        }
                        is SlideElement.ImageElement -> {
                            val imgBytes = readBytesSafe(el.localImagePath)
                            if (imgBytes != null) {
                                val mediaName = "image${mediaCounter++}.png"
                                mediaEntries.add("ppt/media/$mediaName" to imgBytes)
                                val rId = "rId${localRelIdx++}"
                                thisSlideRels.appendLine("""  <Relationship Id="$rId" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="../media/$mediaName"/>""")
                                shapesXml.append(buildPictureXml(shapeId++, rId, xEmu, yEmu, wEmu, hEmu, rotAttr))
                            }
                        }
                        is SlideElement.SmartArtElement -> {
                            shapesXml.append(
                                buildSmartArtXml(
                                    startId = shapeId,
                                    el = el,
                                    xEmu = xEmu,
                                    yEmu = yEmu,
                                    wEmu = wEmu,
                                    hEmu = hEmu
                                )
                            )
                            shapeId += el.nodes.size.coerceAtLeast(1)
                        }
                        is SlideElement.ChartElement,
                        is SlideElement.EquationElement,
                        is SlideElement.IconElement -> {
                            // Render complex visual elements to embedded crisp PNG so PowerPoint preserves exact fidelity
                            val tempSlide = slide.copy(
                                background = SlideBackground.Solid("#00000000"),
                                elements = listOf(el.withTransform(x = 0f, y = 0f))
                            )
                            val tempPres = presentation.copy(
                                canvasWidth = el.width.coerceAtLeast(100f),
                                canvasHeight = el.height.coerceAtLeast(100f)
                            )
                            val bmp = SlideBitmapRenderer.renderSlideToBitmap(
                                context,
                                tempPres,
                                tempSlide,
                                el.width.toInt().coerceIn(200, 1200),
                                el.height.toInt().coerceIn(200, 1200)
                            )
                            val baos = ByteArrayOutputStream()
                            bmp.compress(Bitmap.CompressFormat.PNG, 95, baos)
                            bmp.recycle()
                            val mediaName = "image${mediaCounter++}.png"
                            mediaEntries.add("ppt/media/$mediaName" to baos.toByteArray())
                            val rId = "rId${localRelIdx++}"
                            thisSlideRels.appendLine("""  <Relationship Id="$rId" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="../media/$mediaName"/>""")
                            shapesXml.append(buildPictureXml(shapeId++, rId, xEmu, yEmu, wEmu, hEmu, rotAttr))
                        }
                    }
                }

                if (slide.speakerNotes.isNotBlank()) {
                    val notesRelId = "rId${localRelIdx++}"
                    thisSlideRels.appendLine("""  <Relationship Id="$notesRelId" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/notesSlide" Target="../notesSlides/notesSlide$slideNum.xml"/>""")
                    contentTypeOverrides.appendLine("""  <Override PartName="/ppt/notesSlides/notesSlide$slideNum.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.notesSlide+xml"/>""")
                    writeZipEntry(
                        zip,
                        "ppt/notesSlides/notesSlide$slideNum.xml",
                        buildNotesSlideXml(slide.speakerNotes)
                    )
                    writeZipEntry(
                        zip,
                        "ppt/notesSlides/_rels/notesSlide$slideNum.xml.rels",
                        """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide" Target="../slides/slide$slideNum.xml"/>
</Relationships>"""
                    )
                }

                val bgXml = buildBackgroundXml(slide.background)
                val slideXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sld xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
  <p:cSld>
    $bgXml
    <p:spTree>
      <p:nvGrpSpPr>
        <p:cNvPr id="1" name=""/>
        <p:cNvGrpSpPr/>
        <p:nvPr/>
      </p:nvGrpSpPr>
      <p:grpSpPr>
        <a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm>
      </p:grpSpPr>
      $shapesXml
    </p:spTree>
  </p:cSld>
</p:sld>"""
                writeZipEntry(zip, "ppt/slides/slide$slideNum.xml", slideXml)
                writeZipEntry(
                    zip,
                    "ppt/slides/_rels/slide$slideNum.xml.rels",
                    """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/relationships">
$thisSlideRels</Relationships>"""
                )
                onProgress(0.1f + (slideNum.toFloat() / slideCount.toFloat()) * 0.72f)
            }

            // 6. Media bytes
            mediaEntries.forEach { (entryPath, bytes) ->
                zip.putNextEntry(ZipEntry(entryPath))
                zip.write(bytes)
                zip.closeEntry()
            }

            // 7. ppt/presentation.xml & rels
            writeZipEntry(
                zip,
                "ppt/presentation.xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:presentation xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
  <p:sldMasterIdLst><p:sldMasterId id="2147483648" r:id="rId1"/></p:sldMasterIdLst>
  <p:sldIdLst>
$slideIdList  </p:sldIdLst>
  <p:sldSz cx="$slideWidthEmu" cy="$slideHeightEmu"/>
  <p:notesSz cx="6858000" cy="9144000"/>
</p:presentation>"""
            )
            writeZipEntry(
                zip,
                "ppt/_rels/presentation.xml.rels",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster" Target="slideMasters/slideMaster1.xml"/>
$slideRels</Relationships>"""
            )

            // 8. [Content_Types].xml
            writeZipEntry(
                zip,
                "[Content_Types].xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Default Extension="png" ContentType="image/png"/>
  <Default Extension="jpg" ContentType="image/jpeg"/>
  <Override PartName="/ppt/presentation.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml"/>
  <Override PartName="/ppt/slideMasters/slideMaster1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideMaster+xml"/>
  <Override PartName="/ppt/slideLayouts/slideLayout1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml"/>
  <Override PartName="/ppt/theme/theme1.xml" ContentType="application/vnd.openxmlformats-officedocument.theme+xml"/>
$contentTypeOverrides</Types>"""
            )
            onProgress(1f)
        }
        outputFile
    }

    suspend fun importFromPptx(
        inputStream: InputStream,
        presentationId: String,
        title: String,
        storage: ProjectStorageManager,
        language: AppLanguage
    ): Presentation = withContext(Dispatchers.IO) {
        val entries = mutableMapOf<String, ByteArray>()
        ZipInputStream(inputStream).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    entries[entry.name] = zis.readBytes()
                }
                entry = zis.nextEntry
            }
        }

        var slideWidth = 1920f
        var slideHeight = 1080f
        var scaleFromEmu = 1f / EMU_PER_LOGICAL_PX

        entries["ppt/presentation.xml"]?.let { bytes ->
            val parser = Xml.newPullParser().apply {
                setInput(ByteArrayInputStream(bytes), "UTF-8")
            }
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG && parser.name == "sldSz") {
                    val cx = parser.getAttributeValue(null, "cx")?.toLongOrNull() ?: 12192000L
                    val cy = parser.getAttributeValue(null, "cy")?.toLongOrNull() ?: 6858000L
                    if (cx > 0 && cy > 0) {
                        val ratio = cy.toFloat() / cx.toFloat()
                        slideWidth = 1920f
                        slideHeight = (1920f * ratio).coerceIn(720f, 2800f)
                        scaleFromEmu = slideWidth / cx.toFloat()
                    }
                }
                event = parser.next()
            }
        }

        // Save all media files into Project_<id>/images/
        val savedMediaPaths = mutableMapOf<String, String>()
        entries.filterKeys { it.startsWith("ppt/media/") }.forEach { (entryName, bytes) ->
            val fileName = entryName.substringAfterLast('/')
            val outFile = File(File(storage.projectDir(presentationId), "images"), fileName)
            outFile.parentFile?.mkdirs()
            FileOutputStream(outFile).use { it.write(bytes) }
            savedMediaPaths[entryName] = outFile.absolutePath
        }

        // Parse slide entries sorted numerically
        val slideKeys = entries.keys
            .filter { it.matches(Regex("ppt/slides/slide\\d+\\.xml")) }
            .sortedBy { it.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0 }

        val slides = slideKeys.mapIndexed { idx, slideKey ->
            val slideName = slideKey.substringAfterLast('/')
            val relsKey = "ppt/slides/_rels/$slideName.rels"
            val relMap = parseRels(entries[relsKey])
            parseSlideXml(
                xmlBytes = entries[slideKey]!!,
                slideOrder = idx,
                relMap = relMap,
                savedMediaPaths = savedMediaPaths,
                scaleFromEmu = scaleFromEmu,
                language = language
            )
        }

        Presentation(
            id = presentationId,
            title = title,
            description = "Imported PPTX",
            language = language,
            aspectRatio = SlideAspectRatio.RATIO_16_9,
            canvasWidth = slideWidth,
            canvasHeight = slideHeight,
            sourceType = PresentationSourceType.IMPORTED_PPTX,
            slides = slides.ifEmpty {
                listOf(
                    Slide(
                        order = 0,
                        elements = listOf(
                            SlideElement.TextElement(text = title, fontSizeSp = 44f, isBold = true)
                        )
                    )
                )
            }
        )
    }

    private fun parseRels(bytes: ByteArray?): Map<String, String> {
        if (bytes == null) return emptyMap()
        val map = mutableMapOf<String, String>()
        val parser = Xml.newPullParser().apply { setInput(ByteArrayInputStream(bytes), "UTF-8") }
        var ev = parser.eventType
        while (ev != XmlPullParser.END_DOCUMENT) {
            if (ev == XmlPullParser.START_TAG && parser.name == "Relationship") {
                val id = parser.getAttributeValue(null, "Id")
                val target = parser.getAttributeValue(null, "Target")
                if (id != null && target != null) {
                    val normalized = if (target.startsWith("../")) "ppt/" + target.removePrefix("../") else target
                    map[id] = normalized
                }
            }
            ev = parser.next()
        }
        return map
    }

    private fun parseSlideXml(
        xmlBytes: ByteArray,
        slideOrder: Int,
        relMap: Map<String, String>,
        savedMediaPaths: Map<String, String>,
        scaleFromEmu: Float,
        language: AppLanguage
    ): Slide {
        val elements = mutableListOf<SlideElement>()
        var bg: SlideBackground = SlideBackground.Solid("#FFFFFF")
        val parser = Xml.newPullParser().apply { setInput(ByteArrayInputStream(xmlBytes), "UTF-8") }

        var zCounter = 1
        var inBg = false
        var inSp = false
        var inPic = false
        var inTbl = false
        var x = 100f
        var y = 100f
        var w = 600f
        var h = 180f
        var rot = 0f
        var presetShape: String? = null
        var fillHex: String? = null
        var textColorHex: String? = null
        var fontSize = 32f
        var isBold = false
        var isItalic = false
        var isRtl = false
        var textAlign = TextAlignment.START
        val textBuffer = StringBuilder()
        var picEmbedId: String? = null
        val tableRows = mutableListOf<MutableList<String>>()
        var currentRow: MutableList<String>? = null
        var currentCellText = StringBuilder()
        var inCell = false

        var ev = parser.eventType
        while (ev != XmlPullParser.END_DOCUMENT) {
            when (ev) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "bg" -> inBg = true
                        "sp" -> {
                            inSp = true
                            x = 120f; y = 120f; w = 800f; h = 200f; rot = 0f
                            presetShape = null; fillHex = null; textColorHex = null
                            fontSize = 32f; isBold = false; isItalic = false; isRtl = false
                            textAlign = TextAlignment.START
                            textBuffer.clear()
                        }
                        "pic" -> {
                            inPic = true
                            x = 120f; y = 120f; w = 600f; h = 400f; rot = 0f
                            picEmbedId = null
                        }
                        "tbl" -> {
                            inTbl = true
                            tableRows.clear()
                        }
                        "tr" -> if (inTbl) currentRow = mutableListOf()
                        "tc" -> if (inTbl) {
                            inCell = true
                            currentCellText.clear()
                        }
                        "xfrm" -> {
                            val rAttr = parser.getAttributeValue(null, "rot")?.toFloatOrNull()
                            if (rAttr != null) rot = rAttr / 60000f
                        }
                        "off" -> if (inSp || inPic || inTbl) {
                            x = (parser.getAttributeValue(null, "x")?.toLongOrNull() ?: 0L) * scaleFromEmu
                            y = (parser.getAttributeValue(null, "y")?.toLongOrNull() ?: 0L) * scaleFromEmu
                        }
                        "ext" -> if (inSp || inPic || inTbl) {
                            val cx = parser.getAttributeValue(null, "cx")?.toLongOrNull()
                            val cy = parser.getAttributeValue(null, "cy")?.toLongOrNull()
                            if (cx != null && cx > 0) w = cx * scaleFromEmu
                            if (cy != null && cy > 0) h = cy * scaleFromEmu
                        }
                        "prstGeom" -> if (inSp) {
                            presetShape = parser.getAttributeValue(null, "prst")
                        }
                        "srgbClr" -> {
                            val hex = parser.getAttributeValue(null, "val")
                            if (hex != null && hex.length == 6) {
                                if (inBg) {
                                    bg = SlideBackground.Solid("#$hex")
                                } else if (inSp) {
                                    if (textColorHex == null && textBuffer.isEmpty() && fillHex == null) {
                                        fillHex = "#$hex"
                                    } else {
                                        textColorHex = "#$hex"
                                    }
                                }
                            }
                        }
                        "pPr" -> if (inSp) {
                            val rtlAttr = parser.getAttributeValue(null, "rtl")
                            if (rtlAttr == "1" || rtlAttr == "true") isRtl = true
                            textAlign = when (parser.getAttributeValue(null, "algn")) {
                                "ctr" -> TextAlignment.CENTER
                                "r" -> if (isRtl) TextAlignment.START else TextAlignment.END
                                "l" -> if (isRtl) TextAlignment.END else TextAlignment.START
                                "just" -> TextAlignment.JUSTIFY
                                else -> TextAlignment.START
                            }
                        }
                        "rPr" -> if (inSp) {
                            parser.getAttributeValue(null, "sz")?.toFloatOrNull()?.let {
                                fontSize = (it / 100f * 1.4f).coerceIn(14f, 96f)
                            }
                            if (parser.getAttributeValue(null, "b") == "1") isBold = true
                            if (parser.getAttributeValue(null, "i") == "1") isItalic = true
                        }
                        "blip" -> if (inPic) {
                            for (i in 0 until parser.attributeCount) {
                                if (parser.getAttributeName(i).endsWith("embed")) {
                                    picEmbedId = parser.getAttributeValue(i)
                                }
                            }
                        }
                        "t" -> {
                            val txt = parser.nextText()
                            if (inTbl && inCell) {
                                currentCellText.append(txt)
                            } else if (inSp) {
                                textBuffer.append(txt)
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "bg" -> inBg = false
                        "p" -> if (inSp && textBuffer.isNotEmpty()) textBuffer.append("\n")
                        "tc" -> if (inTbl) {
                            currentRow?.add(currentCellText.toString().trim())
                            inCell = false
                        }
                        "tr" -> if (inTbl) {
                            currentRow?.let { if (it.isNotEmpty()) tableRows.add(it) }
                            currentRow = null
                        }
                        "tbl" -> if (inTbl) {
                            if (tableRows.isNotEmpty()) {
                                val rCount = tableRows.size
                                val cCount = tableRows.maxOf { it.size }.coerceAtLeast(1)
                                val cells = buildList {
                                    for (r in 0 until rCount) {
                                        for (c in 0 until cCount) {
                                            add(
                                                TableCell(
                                                    row = r,
                                                    col = c,
                                                    text = tableRows[r].getOrElse(c) { "" },
                                                    bgColorHex = if (r == 0) "#0D47A1" else "#FFFFFF",
                                                    textColorHex = if (r == 0) "#FFFFFF" else "#0F172A",
                                                    isHeader = r == 0,
                                                    isBold = r == 0
                                                )
                                            )
                                        }
                                    }
                                }
                                elements.add(
                                    SlideElement.TableElement(
                                        x = x, y = y, width = w.coerceAtLeast(300f), height = h.coerceAtLeast(160f),
                                        zIndex = zCounter++, rows = rCount, cols = cCount, cells = cells
                                    )
                                )
                            }
                            inTbl = false
                        }
                        "pic" -> {
                            val mediaTarget = picEmbedId?.let { relMap[it] }
                            val localPath = mediaTarget?.let { savedMediaPaths[it] }
                            if (localPath != null) {
                                elements.add(
                                    SlideElement.ImageElement(
                                        x = x, y = y, width = w.coerceAtLeast(80f), height = h.coerceAtLeast(80f),
                                        rotation = rot, zIndex = zCounter++, localImagePath = localPath
                                    )
                                )
                            }
                            inPic = false
                        }
                        "sp" -> {
                            val cleanText = textBuffer.toString().trim()
                            if (cleanText.isNotEmpty()) {
                                val dir = if (isRtl) WritingDirection.RTL else WritingDirection.AUTO
                                val fontId = when {
                                    language == AppLanguage.FA -> "vazirmatn"
                                    detectTextIsRtl(cleanText, dir) -> "cairo"
                                    else -> "plus_jakarta_sans"
                                }
                                elements.add(
                                    SlideElement.TextElement(
                                        x = x, y = y, width = w.coerceAtLeast(120f), height = h.coerceAtLeast(80f),
                                        rotation = rot, zIndex = zCounter++,
                                        text = cleanText,
                                        fontFamilyId = fontId,
                                        fontSizeSp = fontSize,
                                        textColorHex = textColorHex ?: "#0F172A",
                                        backgroundColorHex = fillHex ?: "#00000000",
                                        isBold = isBold,
                                        isItalic = isItalic,
                                        textAlign = textAlign,
                                        writingDirection = dir
                                    )
                                )
                            } else if (presetShape != null && fillHex != null) {
                                val mappedShape = ShapeType.entries.firstOrNull { it.pptxPreset == presetShape }
                                    ?: ShapeType.RECTANGLE
                                elements.add(
                                    SlideElement.ShapeElement(
                                        x = x, y = y, width = w.coerceAtLeast(60f), height = h.coerceAtLeast(60f),
                                        rotation = rot, zIndex = zCounter++,
                                        shapeType = mappedShape,
                                        fillColorHex = fillHex!!,
                                        strokeColorHex = fillHex!!
                                    )
                                )
                            }
                            inSp = false
                        }
                    }
                }
            }
            ev = parser.next()
        }

        return Slide(
            id = UUID.randomUUID().toString(),
            order = slideOrder,
            background = bg,
            elements = elements
        )
    }

    private fun buildBackgroundXml(bg: SlideBackground): String {
        return when (bg) {
            is SlideBackground.Solid -> {
                val hex = toRgb6Hex(bg.colorHex)
                """<p:bg><p:bgPr><a:solidFill><a:srgbClr val="$hex"/></a:solidFill><a:effectLst/></p:bgPr></p:bg>"""
            }
            is SlideBackground.Gradient -> {
                val c1 = toRgb6Hex(bg.startColorHex)
                val c2 = toRgb6Hex(bg.endColorHex)
                val ang = ((bg.angleDegrees.toInt() % 360 + 360) % 360) * 60000
                """<p:bg><p:bgPr><a:gradFill><a:gsLst><a:gs pos="0"><a:srgbClr val="$c1"/></a:gs><a:gs pos="100000"><a:srgbClr val="$c2"/></a:gs></a:gsLst><a:lin ang="$ang" scaled="1"/></a:gradFill><a:effectLst/></p:bgPr></p:bg>"""
            }
            is SlideBackground.Image -> {
                """<p:bg><p:bgPr><a:solidFill><a:srgbClr val="1E293B"/></a:solidFill><a:effectLst/></p:bgPr></p:bg>"""
            }
        }
    }

    private fun buildTextBoxXml(
        id: Int,
        el: SlideElement.TextElement,
        xEmu: Long,
        yEmu: Long,
        wEmu: Long,
        hEmu: Long,
        rotAttr: String
    ): String {
        val isRtl = detectTextIsRtl(el.text, el.writingDirection)
        val alignCode = when (el.textAlign) {
            TextAlignment.CENTER -> "ctr"
            TextAlignment.END -> if (isRtl) "l" else "r"
            TextAlignment.JUSTIFY -> "just"
            TextAlignment.START -> if (isRtl) "r" else "l"
        }
        val szVal = (el.fontSizeSp * 0.72f * 100f).toInt().coerceIn(800, 9600)
        val colorHex = toRgb6Hex(el.textColorHex)
        val boldAttr = if (el.isBold) " b=\"1\"" else ""
        val italicAttr = if (el.isItalic) " i=\"1\"" else ""
        val underlineAttr = if (el.isUnderline) " u=\"sng\"" else ""
        val bgInt = parseColorInt(el.backgroundColorHex, 0)
        val fillXml = if ((bgInt ushr 24) > 10) {
            """<a:solidFill><a:srgbClr val="${toRgb6Hex(el.backgroundColorHex)}"/></a:solidFill>"""
        } else "<a:noFill/>"
        val typefaceName = escapeXml(
            com.example.ui.theme.SlideFontRegistry.displayNameFor(el.fontFamilyId)
                .substringBefore(" (").trim().ifBlank { "Cairo" }
        )

        val paragraphs = SlideBitmapRenderer.formatListText(el.text, el.listType)
            .lines()
            .joinToString("\n") { line ->
                """<a:p><a:pPr algn="$alignCode" rtl="${if (isRtl) "1" else "0"}"/><a:r><a:rPr lang="${if (isRtl) "ar-SA" else "en-US"}" sz="$szVal"$boldAttr$italicAttr$underlineAttr><a:solidFill><a:srgbClr val="$colorHex"/></a:solidFill><a:latin typeface="$typefaceName"/><a:cs typeface="$typefaceName"/></a:rPr><a:t>${escapeXml(line)}</a:t></a:r></a:p>"""
            }

        return """<p:sp>
  <p:nvSpPr><p:cNvPr id="$id" name="TextBox $id"/><p:cNvSpPr txBox="1"/><p:nvPr/></p:nvSpPr>
  <p:spPr>
    <a:xfrm$rotAttr><a:off x="$xEmu" y="$yEmu"/><a:ext cx="$wEmu" cy="$hEmu"/></a:xfrm>
    <a:prstGeom prst="rect"><a:avLst/></a:prstGeom>
    $fillXml
  </p:spPr>
  <p:txBody><a:bodyPr wrap="square"/><a:lstStyle/>$paragraphs</p:txBody>
</p:sp>
"""
    }

    private fun buildShapeXml(
        id: Int,
        el: SlideElement.ShapeElement,
        xEmu: Long,
        yEmu: Long,
        wEmu: Long,
        hEmu: Long,
        rotAttr: String
    ): String {
        val fillHex = toRgb6Hex(el.fillColorHex)
        val strokeHex = toRgb6Hex(el.strokeColorHex)
        val strokeW = (el.strokeWidth * 12700L).toLong().coerceAtLeast(12700L)
        val bodyXml = if (el.labelText.isNotBlank()) {
            """<p:txBody><a:bodyPr/><a:lstStyle/><a:p><a:pPr algn="ctr"/><a:r><a:rPr sz="1800" b="1"><a:solidFill><a:srgbClr val="${toRgb6Hex(el.labelColorHex)}"/></a:solidFill></a:rPr><a:t>${escapeXml(el.labelText)}</a:t></a:r></a:p></p:txBody>"""
        } else ""
        return """<p:sp>
  <p:nvSpPr><p:cNvPr id="$id" name="Shape $id"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr>
  <p:spPr>
    <a:xfrm$rotAttr><a:off x="$xEmu" y="$yEmu"/><a:ext cx="$wEmu" cy="$hEmu"/></a:xfrm>
    <a:prstGeom prst="${el.shapeType.pptxPreset}"><a:avLst/></a:prstGeom>
    <a:solidFill><a:srgbClr val="$fillHex"/></a:solidFill>
    <a:ln w="$strokeW"><a:solidFill><a:srgbClr val="$strokeHex"/></a:solidFill></a:ln>
  </p:spPr>
  $bodyXml
</p:sp>
"""
    }

    private fun buildSmartArtXml(
        startId: Int,
        el: SlideElement.SmartArtElement,
        xEmu: Long,
        yEmu: Long,
        wEmu: Long,
        hEmu: Long
    ): String {
        val nodes = el.nodes
        if (nodes.isEmpty()) return ""
        val count = nodes.size.coerceAtLeast(1)
        val gapEmu = (wEmu * 0.03f).toLong().coerceAtLeast(63500L)
        val nodeW = ((wEmu - gapEmu * (count - 1)) / count).coerceAtLeast(180000L)
        val sb = StringBuilder()
        nodes.forEachIndexed { idx, node ->
            val nx = xEmu + idx * (nodeW + gapEmu)
            val fillHex = toRgb6Hex(node.fillColorHex)
            val txtHex = toRgb6Hex(node.textColorHex)
            val id = startId + idx
            sb.append(
                """<p:sp>
  <p:nvSpPr><p:cNvPr id="$id" name="SmartArtNode $id"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr>
  <p:spPr>
    <a:xfrm><a:off x="$nx" y="$yEmu"/><a:ext cx="$nodeW" cy="$hEmu"/></a:xfrm>
    <a:prstGeom prst="roundRect"><a:avLst/></a:prstGeom>
    <a:solidFill><a:srgbClr val="$fillHex"/></a:solidFill>
  </p:spPr>
  <p:txBody><a:bodyPr anchor="ctr"/><a:lstStyle/><a:p><a:pPr algn="ctr"/><a:r><a:rPr sz="1600" b="1"><a:solidFill><a:srgbClr val="$txtHex"/></a:solidFill></a:rPr><a:t>${escapeXml(node.text)}</a:t></a:r></a:p></p:txBody>
</p:sp>
"""
            )
        }
        return sb.toString()
    }

    private fun buildPictureXml(
        id: Int,
        rId: String,
        xEmu: Long,
        yEmu: Long,
        wEmu: Long,
        hEmu: Long,
        rotAttr: String
    ): String = """<p:pic>
  <p:nvPicPr><p:cNvPr id="$id" name="Image $id"/><p:cNvPicPr/><p:nvPr/></p:nvPicPr>
  <p:blipFill><a:blip r:embed="$rId"/><a:stretch><a:fillRect/></a:stretch></p:blipFill>
  <p:spPr><a:xfrm$rotAttr><a:off x="$xEmu" y="$yEmu"/><a:ext cx="$wEmu" cy="$hEmu"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom></p:spPr>
</p:pic>
"""

    private fun buildTableXml(
        id: Int,
        el: SlideElement.TableElement,
        xEmu: Long,
        yEmu: Long,
        wEmu: Long,
        hEmu: Long
    ): String {
        val colW = (wEmu / el.cols.coerceAtLeast(1)).coerceAtLeast(100000L)
        val rowH = (hEmu / el.rows.coerceAtLeast(1)).coerceAtLeast(100000L)
        val gridCols = (0 until el.cols).joinToString("") { """<a:gridCol w="$colW"/>""" }
        val rowsXml = (0 until el.rows).joinToString("") { r ->
            val cellsXml = (0 until el.cols).joinToString("") { c ->
                val cell = el.cells.firstOrNull { it.row == r && it.col == c }
                val txt = escapeXml(cell?.text.orEmpty())
                val bg = toRgb6Hex(cell?.bgColorHex ?: "#FFFFFF")
                val fg = toRgb6Hex(cell?.textColorHex ?: "#0F172A")
                """<a:tc><a:txBody><a:bodyPr/><a:lstStyle/><a:p><a:pPr algn="ctr"/><a:r><a:rPr sz="1400"><a:solidFill><a:srgbClr val="$fg"/></a:solidFill></a:rPr><a:t>$txt</a:t></a:r></a:p></a:txBody><a:tcPr><a:solidFill><a:srgbClr val="$bg"/></a:solidFill></a:tcPr></a:tc>"""
            }
            """<a:tr h="$rowH">$cellsXml</a:tr>"""
        }
        return """<p:graphicFrame>
  <p:nvGraphicFramePr><p:cNvPr id="$id" name="Table $id"/><p:cNvGraphicFramePr/><p:nvPr/></p:nvGraphicFramePr>
  <p:xfrm><a:off x="$xEmu" y="$yEmu"/><a:ext cx="$wEmu" cy="$hEmu"/></p:xfrm>
  <a:graphic><a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/table"><a:tbl><a:tblPr/><a:tblGrid>$gridCols</a:tblGrid>$rowsXml</a:tbl></a:graphicData></a:graphic>
</p:graphicFrame>
"""
    }

    private fun buildNotesSlideXml(notes: String): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:notes xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
  <p:cSld><p:spTree><p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr><p:grpSpPr/>
    <p:sp><p:nvSpPr><p:cNvPr id="2" name="Notes"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr><p:spPr/><p:txBody><a:bodyPr/><a:lstStyle/><a:p><a:r><a:t>${escapeXml(notes)}</a:t></a:r></a:p></p:txBody></p:sp>
  </p:spTree></p:cSld>
</p:notes>"""

    private fun toRgb6Hex(hex: String): String {
        val clean = hex.trim().removePrefix("#")
        return when (clean.length) {
            6 -> clean.uppercase()
            8 -> clean.substring(2, 8).uppercase()
            else -> "0F172A"
        }
    }

    private fun escapeXml(input: String): String = input
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")

    private fun writeZipEntry(zip: ZipOutputStream, name: String, content: String) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(content.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun readBytesSafe(path: String): ByteArray? =
        runCatching { File(path).takeIf { it.exists() }?.readBytes() }.getOrNull()

    private fun defaultThemeXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<a:theme xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" name="KATEB Theme">
  <a:themeElements>
    <a:clrScheme name="KATEB">
      <a:dk1><a:srgbClr val="0F172A"/></a:dk1><a:lt1><a:srgbClr val="FFFFFF"/></a:lt1>
      <a:dk2><a:srgbClr val="0D47A1"/></a:dk2><a:lt2><a:srgbClr val="F8FAFC"/></a:lt2>
      <a:accent1><a:srgbClr val="1565C0"/></a:accent1><a:accent2><a:srgbClr val="00897B"/></a:accent2>
      <a:accent3><a:srgbClr val="F59E0B"/></a:accent3><a:accent4><a:srgbClr val="6366F1"/></a:accent4>
      <a:accent5><a:srgbClr val="EC4899"/></a:accent5><a:accent6><a:srgbClr val="0EA5E9"/></a:accent6>
      <a:hlink><a:srgbClr val="1565C0"/></a:hlink><a:folHlink><a:srgbClr val="6366F1"/></a:folHlink>
    </a:clrScheme>
    <a:fontScheme name="KATEB">
      <a:majorFont><a:latin typeface="Cairo"/><a:ea typeface=""/><a:cs typeface="Cairo"/></a:majorFont>
      <a:minorFont><a:latin typeface="Cairo"/><a:ea typeface=""/><a:cs typeface="Cairo"/></a:minorFont>
    </a:fontScheme>
    <a:fmtScheme name="KATEB"><a:fillStyleLst><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:fillStyleLst><a:lnStyleLst><a:ln w="9525"><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:ln></a:lnStyleLst><a:effectStyleLst><a:effectStyle><a:effectLst/></a:effectStyle></a:effectStyleLst><a:bgFillStyleLst><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:bgFillStyleLst></a:fmtScheme>
  </a:themeElements>
</a:theme>"""

    private fun defaultSlideMasterXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sldMaster xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
  <p:cSld><p:spTree><p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr><p:grpSpPr/></p:spTree></p:cSld>
  <p:clrMap bg1="lt1" tx1="dk1" bg2="lt2" tx2="dk2" accent1="accent1" accent2="accent2" accent3="accent3" accent4="accent4" accent5="accent5" accent6="accent6" hlink="hlink" folHlink="folHlink"/>
  <p:sldLayoutIdLst><p:sldLayoutId id="2147483649" r:id="rId1"/></p:sldLayoutIdLst>
</p:sldMaster>"""

    private fun defaultSlideLayoutXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sldLayout xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main" type="blank">
  <p:cSld><p:spTree><p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr><p:grpSpPr/></p:spTree></p:cSld>
</p:sldLayout>"""
}
