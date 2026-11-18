package com.example.data.local

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.provider.OpenableColumns
import com.example.ui.theme.SlideFontRegistry
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class ImportedFontSpec(
    val id: String,
    val displayName: String,
    val filePath: String,
    val addedAt: Long = System.currentTimeMillis()
)

class CustomFontManager(private val context: Context) {
    private val fontsDir: File = File(context.filesDir, "custom_fonts").apply { mkdirs() }
    private val manifestFile: File = File(fontsDir, "manifest.json")

    fun loadAndSyncFonts(): List<ImportedFontSpec> {
        val loaded = readManifest().filter { File(it.filePath).exists() && File(it.filePath).length() > 128L }
        writeManifest(loaded)
        SlideFontRegistry.setCustomFonts(loaded.map { it.id to it })
        return loaded
    }

    fun importFontFromUri(uri: Uri): Result<ImportedFontSpec> = runCatching {
        val rawName = queryDisplayName(uri) ?: "CustomFont_${System.currentTimeMillis()}.ttf"
        val cleanExt = when {
            rawName.lowercase().endsWith(".otf") -> ".otf"
            rawName.lowercase().endsWith(".ttf") -> ".ttf"
            else -> ".ttf"
        }
        val baseTitle = rawName
            .substringBeforeLast('.')
            .replace(Regex("[^\\p{L}\\p{N}_\\- ]"), " ")
            .trim()
            .ifBlank { "Custom Font" }
            .take(36)

        val fontId = "custom_${UUID.randomUUID().toString().take(8)}"
        val tempFile = File(fontsDir, "tmp_$fontId$cleanExt")
        val resolver = context.contentResolver
        resolver.openInputStream(uri)?.use { inp ->
            FileOutputStream(tempFile).use { out ->
                val buffer = ByteArray(8192)
                var total = 0L
                while (true) {
                    val read = inp.read(buffer)
                    if (read <= 0) break
                    total += read
                    if (total > 25L * 1024L * 1024L) {
                        throw IllegalArgumentException("font_file_too_large")
                    }
                    out.write(buffer, 0, read)
                }
                out.flush()
            }
        } ?: throw IllegalStateException("unable_to_open_font_stream")

        if (!isValidFontHeader(tempFile)) {
            tempFile.delete()
            throw IllegalArgumentException("invalid_font_file")
        }

        val parsedTypeface = runCatching { Typeface.createFromFile(tempFile) }.getOrNull()
        if (parsedTypeface == null) {
            tempFile.delete()
            throw IllegalArgumentException("invalid_font_file")
        }

        val finalFile = File(fontsDir, "$fontId$cleanExt")
        if (!tempFile.renameTo(finalFile)) {
            tempFile.copyTo(finalFile, overwrite = true)
            tempFile.delete()
        }

        val spec = ImportedFontSpec(
            id = fontId,
            displayName = baseTitle,
            filePath = finalFile.absolutePath,
            addedAt = System.currentTimeMillis()
        )
        val updated = (readManifest().filter { File(it.filePath).exists() } + spec)
        writeManifest(updated)
        SlideFontRegistry.setCustomFonts(updated.map { it.id to it })
        spec
    }

    fun deleteCustomFont(fontId: String): List<ImportedFontSpec> {
        val current = readManifest()
        val target = current.firstOrNull { it.id == fontId }
        if (target != null) {
            runCatching { File(target.filePath).delete() }
        }
        val remaining = current.filterNot { it.id == fontId }.filter { File(it.filePath).exists() }
        writeManifest(remaining)
        SlideFontRegistry.setCustomFonts(remaining.map { it.id to it })
        return remaining
    }

    internal fun isValidFontHeader(file: File): Boolean {
        if (!file.exists() || file.length() < 12L) return false
        val header = ByteArray(4)
        file.inputStream().use { inp ->
            if (inp.read(header) < 4) return false
        }
        val b0 = header[0].toInt() and 0xFF
        val b1 = header[1].toInt() and 0xFF
        val b2 = header[2].toInt() and 0xFF
        val b3 = header[3].toInt() and 0xFF
        val tag = String(header, Charsets.US_ASCII)
        // TrueType 0x00010000, OpenType OTTO, Apple true, typ1, TTC ttcf
        return (b0 == 0x00 && b1 == 0x01 && b2 == 0x00 && b3 == 0x00) ||
            tag == "OTTO" ||
            tag == "true" ||
            tag == "typ1" ||
            tag == "ttcf"
    }

    private fun queryDisplayName(uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) c.getString(0) else null
            }
        }.getOrNull() ?: uri.lastPathSegment
    }

    private fun readManifest(): List<ImportedFontSpec> {
        if (!manifestFile.exists()) return emptyList()
        return runCatching {
            val arr = JSONArray(manifestFile.readText(Charsets.UTF_8))
            (0 until arr.length()).mapNotNull { idx ->
                val o = arr.optJSONObject(idx) ?: return@mapNotNull null
                val id = o.optString("id", "")
                val name = o.optString("displayName", "")
                val path = o.optString("filePath", "")
                if (id.isBlank() || name.isBlank() || path.isBlank()) null
                else ImportedFontSpec(
                    id = id,
                    displayName = name,
                    filePath = path,
                    addedAt = o.optLong("addedAt", System.currentTimeMillis())
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun writeManifest(list: List<ImportedFontSpec>) {
        val arr = JSONArray().apply {
            list.forEach { spec ->
                put(
                    JSONObject().apply {
                        put("id", spec.id)
                        put("displayName", spec.displayName)
                        put("filePath", spec.filePath)
                        put("addedAt", spec.addedAt)
                    }
                )
            }
        }
        manifestFile.writeText(arr.toString(2), Charsets.UTF_8)
    }
}
