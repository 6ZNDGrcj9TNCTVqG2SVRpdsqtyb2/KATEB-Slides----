package com.example.engines

import android.content.Context
import android.graphics.Bitmap
import com.example.engine.Presentation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

data class PdfPreviewPage(
    val slideId: String,
    val originalIndex: Int,
    val titleSummary: String,
    val bitmap: Bitmap
)

object PdfExportEngine {

    suspend fun renderPreviewPages(
        context: Context,
        presentation: Presentation,
        thumbWidth: Int = 540,
        onProgress: (Float) -> Unit = {}
    ): List<PdfPreviewPage> = withContext(Dispatchers.IO) {
        if (presentation.slides.isEmpty()) return@withContext emptyList()
        val ratio = (presentation.canvasHeight / presentation.canvasWidth.coerceAtLeast(1f)).coerceIn(0.4f, 2.2f)
        val w = thumbWidth.coerceIn(280, 960)
        val h = (w * ratio).toInt().coerceIn(200, 1200)
        val total = presentation.slides.size.coerceAtLeast(1)

        presentation.slides.mapIndexed { idx, slide ->
            val bmp = SlideBitmapRenderer.renderSlideToBitmap(
                context = context,
                presentation = presentation,
                slide = slide,
                targetWidth = w,
                targetHeight = h
            )
            val titleText = slide.elements
                .filterIsInstance<com.example.engine.SlideElement.TextElement>()
                .firstOrNull { it.text.isNotBlank() }
                ?.text?.lineSequence()?.firstOrNull()?.take(42)
                ?: "Slide ${idx + 1}"
            onProgress((idx + 1).toFloat() / total.toFloat())
            PdfPreviewPage(
                slideId = slide.id,
                originalIndex = idx,
                titleSummary = titleText,
                bitmap = bmp
            )
        }
    }

    suspend fun exportToPdf(
        context: Context,
        presentation: Presentation,
        outputFile: File,
        onProgress: (Float) -> Unit = {}
    ): File = withContext(Dispatchers.IO) {
        outputFile.parentFile?.mkdirs()
        if (presentation.slides.isEmpty()) {
            throw IllegalStateException("empty_presentation_slides")
        }
        val pageWidth = presentation.canvasWidth.toInt().coerceIn(600, 1920)
        val pageHeight = presentation.canvasHeight.toInt().coerceIn(400, 1920)
        val total = presentation.slides.size.coerceAtLeast(1)

        // Render each slide to JPEG bytes via SlideBitmapRenderer, then package as standard PDF 1.4
        val slideJpegs = presentation.slides.mapIndexed { idx, slide ->
            val bitmap = SlideBitmapRenderer.renderSlideToBitmap(
                context = context,
                presentation = presentation,
                slide = slide,
                targetWidth = pageWidth,
                targetHeight = pageHeight
            )
            val bytes = ByteArrayOutputStream().use { baos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, baos)
                bitmap.recycle()
                baos.toByteArray()
            }
            onProgress(((idx + 1).toFloat() / total.toFloat()) * 0.85f)
            bytes
        }

        writePdfDocument(
            outputFile = outputFile,
            pageWidth = pageWidth,
            pageHeight = pageHeight,
            slideJpegs = slideJpegs
        )
        onProgress(1f)
        outputFile
    }

    private fun writePdfDocument(
        outputFile: File,
        pageWidth: Int,
        pageHeight: Int,
        slideJpegs: List<ByteArray>
    ) {
        val offsets = mutableListOf<Long>()
        var bytesWritten = 0L

        BufferedOutputStream(FileOutputStream(outputFile)).use { out ->
            fun writeAscii(text: String) {
                val b = text.toByteArray(Charsets.US_ASCII)
                out.write(b)
                bytesWritten += b.size
            }

            fun beginObj(id: Int) {
                offsets.add(bytesWritten)
                writeAscii("$id 0 obj\n")
            }

            writeAscii("%PDF-1.4\n")
            val binMarker = byteArrayOf('%'.code.toByte(), 0xE2.toByte(), 0xE3.toByte(), 0xCF.toByte(), 0xD3.toByte(), '\n'.code.toByte())
            out.write(binMarker)
            bytesWritten += binMarker.size
            val pageCount = slideJpegs.size.coerceAtLeast(1)

            // Object IDs:
            // 1: Catalog
            // 2: Pages
            // For slide i (0..N-1):
            //   pageObj = 3 + i * 3
            //   contentObj = 4 + i * 3
            //   imageObj = 5 + i * 3
            val pageIds = (0 until slideJpegs.size).map { 3 + it * 3 }

            beginObj(1)
            writeAscii("<< /Type /Catalog /Pages 2 0 R >>\nendobj\n")

            beginObj(2)
            val kids = pageIds.joinToString(" ") { "$it 0 R" }
            writeAscii("<< /Type /Pages /Kids [ $kids ] /Count ${pageIds.size} >>\nendobj\n")

            slideJpegs.forEachIndexed { idx, jpeg ->
                val pageId = 3 + idx * 3
                val contentId = pageId + 1
                val imgId = pageId + 2
                val imgName = "/Im$idx"

                beginObj(pageId)
                writeAscii(
                    "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 $pageWidth $pageHeight] " +
                        "/Resources << /XObject << $imgName $imgId 0 R >> >> /Contents $contentId 0 R >>\nendobj\n"
                )

                val streamCmd = "q\n$pageWidth 0 0 $pageHeight 0 0 cm\n$imgName Do\nQ\n"
                beginObj(contentId)
                writeAscii("<< /Length ${streamCmd.length} >>\nstream\n$streamCmd\nendstream\nendobj\n")

                beginObj(imgId)
                writeAscii(
                    "<< /Type /XObject /Subtype /Image /Width $pageWidth /Height $pageHeight " +
                        "/ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode /Length ${jpeg.size} >>\nstream\n"
                )
                out.write(jpeg)
                bytesWritten += jpeg.size
                writeAscii("\nendstream\nendobj\n")
            }

            val xrefOffset = bytesWritten
            val totalObjects = 1 + offsets.size
            writeAscii("xref\n0 $totalObjects\n")
            writeAscii("0000000000 65535 f \n")
            offsets.forEach { off ->
                writeAscii(String.format("%010d 00000 n \n", off))
            }
            writeAscii(
                "trailer\n<< /Size $totalObjects /Root 1 0 R >>\nstartxref\n$xrefOffset\n%%EOF\n"
            )
            out.flush()
        }
    }
}
