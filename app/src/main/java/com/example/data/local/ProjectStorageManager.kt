package com.example.data.local

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class ProjectStorageManager(private val context: Context) {

    val rootDir: File
        get() = File(context.filesDir, "KATEB_SLIDES").apply { mkdirs() }

    val projectsRoot: File
        get() = File(rootDir, "Projects").apply { mkdirs() }

    val templatesRoot: File
        get() = File(rootDir, "Templates").apply { mkdirs() }

    val exportsPdfDir: File
        get() = File(rootDir, "Exports/PDF").apply { mkdirs() }

    val exportsPptxDir: File
        get() = File(rootDir, "Exports/PPTX").apply { mkdirs() }

    val exportsImagesDir: File
        get() = File(rootDir, "Exports/Images").apply { mkdirs() }

    val exportsVideosDir: File
        get() = File(rootDir, "Exports/Videos").apply { mkdirs() }

    val settingsDir: File
        get() = File(rootDir, "Settings").apply { mkdirs() }

    fun ensureAllDirectories() {
        rootDir
        projectsRoot
        templatesRoot
        exportsPdfDir
        exportsPptxDir
        exportsImagesDir
        exportsVideosDir
        settingsDir
    }

    fun projectDir(presentationId: String): File =
        File(projectsRoot, "Project_$presentationId").apply {
            mkdirs()
            File(this, "slides").mkdirs()
            File(this, "images").mkdirs()
            File(this, "audio").mkdirs()
        }

    fun presentationJsonFile(presentationId: String): File =
        File(projectDir(presentationId), "presentation.json")

    fun autosaveJsonFile(presentationId: String): File =
        File(projectDir(presentationId), "autosave.json")

    fun coverThumbnailFile(presentationId: String): File =
        File(File(projectDir(presentationId), "slides"), "cover.png")

    fun slideThumbnailFile(presentationId: String, slideIndex: Int): File =
        File(File(projectDir(presentationId), "slides"), "slide_$slideIndex.png")

    fun saveBitmapToProjectImages(presentationId: String, bitmap: Bitmap, prefix: String = "img"): String {
        val imagesDir = File(projectDir(presentationId), "images").apply { mkdirs() }
        val file = File(imagesDir, "${prefix}_${UUID.randomUUID()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
        }
        return file.absolutePath
    }

    fun importUriToProjectImages(presentationId: String, uri: Uri): String? {
        val imagesDir = File(projectDir(presentationId), "images").apply { mkdirs() }
        val file = File(imagesDir, "photo_${UUID.randomUUID()}.png")
        return runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            if (file.exists() && file.length() > 0L) file.absolutePath else null
        }.getOrNull()
    }

    fun importUriToProjectAudio(presentationId: String, uri: Uri): String? {
        val audioDir = File(projectDir(presentationId), "audio").apply { mkdirs() }
        val file = File(audioDir, "audio_${UUID.randomUUID()}.m4a")
        return runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            if (file.exists() && file.length() > 0L) file.absolutePath else null
        }.getOrNull()
    }

    fun copyProjectAssets(sourcePresentationId: String, targetPresentationId: String) {
        val srcImages = File(projectDir(sourcePresentationId), "images")
        val dstImages = File(projectDir(targetPresentationId), "images").apply { mkdirs() }
        if (srcImages.exists()) {
            srcImages.listFiles()?.forEach { file ->
                file.copyTo(File(dstImages, file.name), overwrite = true)
            }
        }
    }

    fun deleteProjectDir(presentationId: String) {
        projectDir(presentationId).deleteRecursively()
    }
}
