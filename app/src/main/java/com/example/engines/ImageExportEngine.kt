package com.example.engines

import android.content.Context
import android.graphics.Bitmap
import com.example.engine.Presentation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class ImageExportFormat(val ext: String, val compressFormat: Bitmap.CompressFormat) {
    PNG("png", Bitmap.CompressFormat.PNG),
    JPG("jpg", Bitmap.CompressFormat.JPEG)
}

object ImageExportEngine {

    suspend fun exportSlidesAsImages(
        context: Context,
        presentation: Presentation,
        outputDir: File,
        format: ImageExportFormat,
        onProgress: (Float) -> Unit = {}
    ): List<File> = withContext(Dispatchers.IO) {
        outputDir.mkdirs()
        val w = presentation.canvasWidth.toInt().coerceIn(640, 1920)
        val h = presentation.canvasHeight.toInt().coerceIn(360, 1920)
        val total = presentation.slides.size.coerceAtLeast(1)

        presentation.slides.mapIndexed { index, slide ->
            val outFile = File(outputDir, "slide_${index + 1}.${format.ext}")
            val bmp = SlideBitmapRenderer.renderSlideToBitmap(context, presentation, slide, w, h)
            FileOutputStream(outFile).use { out ->
                bmp.compress(format.compressFormat, 95, out)
            }
            bmp.recycle()
            onProgress((index + 1).toFloat() / total.toFloat())
            outFile
        }
    }
}
