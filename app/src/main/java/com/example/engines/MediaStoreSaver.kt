package com.example.engines

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

enum class ExportMediaKind { DOCUMENT_OR_PPTX, IMAGE, VIDEO }

/**
 * Saves exported files directly to device MediaStore collections (Downloads / Pictures / Movies)
 * inside the KATEB_SLIDES folder with zero broad storage permissions required on Android 10+.
 */
object MediaStoreSaver {

    suspend fun saveFileToPublicCollection(
        context: Context,
        sourceFile: File,
        displayName: String,
        mimeType: String,
        kind: ExportMediaKind
    ): Uri? = withContext(Dispatchers.IO) {
        if (!sourceFile.exists() || sourceFile.length() == 0L) return@withContext null
        val subFolder = "KATEB_SLIDES"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val (collection, relativePath) = when (kind) {
                ExportMediaKind.DOCUMENT_OR_PPTX -> MediaStore.Downloads.EXTERNAL_CONTENT_URI to
                    "${Environment.DIRECTORY_DOWNLOADS}/$subFolder"
                ExportMediaKind.IMAGE -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI to
                    "${Environment.DIRECTORY_PICTURES}/$subFolder"
                ExportMediaKind.VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI to
                    "${Environment.DIRECTORY_MOVIES}/$subFolder"
            }

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            val resolver = context.contentResolver
            val itemUri = runCatching { resolver.insert(collection, values) }.getOrNull()
                ?: return@withContext null
            try {
                val copied = resolver.openOutputStream(itemUri)?.use { out ->
                    FileInputStream(sourceFile).use { input ->
                        val bytes = input.copyTo(out)
                        out.flush()
                        bytes
                    }
                } ?: 0L
                if (copied <= 0L) {
                    runCatching { resolver.delete(itemUri, null, null) }
                    return@withContext null
                }
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(itemUri, values, null, null)
                itemUri
            } catch (_: Exception) {
                runCatching { resolver.delete(itemUri, null, null) }
                null
            }
        } else {
            runCatching {
                val baseDir = context.getExternalFilesDir(
                    when (kind) {
                        ExportMediaKind.DOCUMENT_OR_PPTX -> Environment.DIRECTORY_DOWNLOADS
                        ExportMediaKind.IMAGE -> Environment.DIRECTORY_PICTURES
                        ExportMediaKind.VIDEO -> Environment.DIRECTORY_MOVIES
                    }
                ) ?: context.filesDir
                val targetDir = File(baseDir, subFolder).apply { mkdirs() }
                val dest = File(targetDir, displayName)
                FileInputStream(sourceFile).use { input ->
                    FileOutputStream(dest).use { out ->
                        input.copyTo(out)
                        out.flush()
                    }
                }
                Uri.fromFile(dest)
            }.getOrNull()
        }
    }
}
