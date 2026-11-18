package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.data.local.PresentationDao
import com.example.data.local.PresentationEntity
import com.example.data.local.ProjectStorageManager
import com.example.data.local.SearchHistoryDao
import com.example.data.local.SearchHistoryEntity
import com.example.data.local.TemplateDao
import com.example.data.local.TemplateSchemaEntity
import com.example.data.templates.PresentationTemplate
import com.example.data.templates.TemplateCategory
import com.example.data.templates.TemplateRepository
import com.example.engine.Presentation
import com.example.engine.PresentationJsonSerializer
import com.example.engines.SlideBitmapRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.util.UUID

class PresentationRepository(
    private val context: Context,
    private val dao: PresentationDao,
    val storage: ProjectStorageManager,
    private val templateDao: TemplateDao? = null,
    private val searchHistoryDao: SearchHistoryDao? = null
) {
    init {
        storage.ensureAllDirectories()
    }

    val recentPresentations: Flow<List<PresentationEntity>> = dao.observeRecent()
    val savedPresentations: Flow<List<PresentationEntity>> = dao.observeAllSaved()
    val importedPresentations: Flow<List<PresentationEntity>> = dao.observeImported()
    val recentSearches: Flow<List<String>> =
        searchHistoryDao?.observeRecent()?.map { list -> list.map { it.queryText } } ?: flowOf(emptyList())

    fun observeTemplatesSeeded(): Flow<List<PresentationTemplate>> {
        val daoRef = templateDao ?: return flowOf(TemplateRepository.templates)
        return daoRef.observeAll().map { rows ->
            if (rows.size < TemplateRepository.templates.size) {
                TemplateRepository.templates
            } else {
                rows.map { row ->
                    val rich = TemplateRepository.byIdOrNull(row.id)
                    rich ?: PresentationTemplate(
                        id = row.id,
                        category = TemplateCategory.entries.firstOrNull { it.id == row.categoryId }
                            ?: TemplateCategory.EDUCATIONAL,
                        nameAr = row.nameAr,
                        nameEn = row.nameEn,
                        nameFa = row.nameFa,
                        primaryHex = row.primaryHex,
                        secondaryHex = row.secondaryHex,
                        accentHex = row.accentHex,
                        bgHex = row.bgHex,
                        titleColorHex = row.titleColorHex,
                        bodyColorHex = row.bodyColorHex,
                        headingFontId = row.headingFontId,
                        bodyFontId = row.bodyFontId,
                        defaultTransition = runCatching {
                            com.example.engine.SlideTransitionType.valueOf(row.defaultTransitionId)
                        }.getOrDefault(com.example.engine.SlideTransitionType.FADE),
                        coverStyle = runCatching {
                            com.example.data.templates.CoverDesignStyle.valueOf(row.coverStyleId)
                        }.getOrDefault(com.example.data.templates.CoverDesignStyle.SIDE_RIBBON)
                    )
                }
            }
        }
    }

    suspend fun ensureTemplatesSeeded() = withContext(Dispatchers.IO) {
        val daoRef = templateDao ?: return@withContext
        val entities = TemplateRepository.templates.mapIndexed { index, tpl ->
            TemplateSchemaEntity(
                id = tpl.id,
                categoryId = tpl.category.id,
                nameAr = tpl.nameAr,
                nameEn = tpl.nameEn,
                nameFa = tpl.nameFa,
                primaryHex = tpl.primaryHex,
                secondaryHex = tpl.secondaryHex,
                accentHex = tpl.accentHex,
                bgHex = tpl.bgHex,
                titleColorHex = tpl.titleColorHex,
                bodyColorHex = tpl.bodyColorHex,
                sortOrder = index,
                headingFontId = tpl.headingFontId,
                bodyFontId = tpl.bodyFontId,
                defaultTransitionId = tpl.defaultTransition.name,
                coverStyleId = tpl.coverStyle.name
            )
        }
        if (daoRef.count() != entities.size) {
            daoRef.clearAll()
        }
        daoRef.upsertAll(entities)
    }

    suspend fun savePresentation(
        presentation: Presentation,
        clearAutosave: Boolean = true,
        folderTagOverride: String? = null,
        isLockedOverride: Boolean? = null
    ): PresentationEntity = withContext(Dispatchers.IO) {
        val updated = presentation.copy(updatedAt = System.currentTimeMillis())
        val json = PresentationJsonSerializer.toJsonString(updated)
        val jsonFile = storage.presentationJsonFile(updated.id)
        jsonFile.writeText(json, Charsets.UTF_8)

        if (clearAutosave) {
            storage.autosaveJsonFile(updated.id).delete()
        }

        val thumbFile = storage.coverThumbnailFile(updated.id)
        val firstSlide = updated.slides.firstOrNull()
        if (firstSlide != null) {
            val bmp = SlideBitmapRenderer.renderSlideToBitmap(
                context = context,
                presentation = updated,
                slide = firstSlide,
                targetWidth = 640,
                targetHeight = (640f * updated.canvasHeight / updated.canvasWidth.coerceAtLeast(1f))
                    .toInt().coerceIn(320, 900)
            )
            FileOutputStream(thumbFile).use { out ->
                bmp.compress(Bitmap.CompressFormat.PNG, 90, out)
            }
            bmp.recycle()
        }

        val existing = dao.getById(updated.id)
        val entity = PresentationEntity(
            id = updated.id,
            title = updated.title,
            description = updated.description,
            languageCode = updated.language.code,
            aspectRatioId = updated.aspectRatio.id,
            themeId = updated.themeId,
            sourceType = updated.sourceType.name,
            slideCount = updated.slides.size,
            thumbnailPath = thumbFile.absolutePath,
            projectDirPath = storage.projectDir(updated.id).absolutePath,
            createdAt = existing?.createdAt ?: updated.createdAt,
            updatedAt = updated.updatedAt,
            lastOpenedAt = System.currentTimeMillis(),
            hasPendingAutosave = !clearAutosave,
            folderTag = folderTagOverride ?: existing?.folderTag ?: "general",
            isLocked = isLockedOverride ?: existing?.isLocked ?: false,
            pendingCloudSync = true,
            cloudSyncedAt = existing?.cloudSyncedAt ?: 0L,
            updatedByUid = existing?.updatedByUid ?: ""
        )
        dao.upsert(entity)
        entity
    }

    suspend fun writeAutosave(presentation: Presentation) = withContext(Dispatchers.IO) {
        val json = PresentationJsonSerializer.toJsonString(presentation)
        storage.autosaveJsonFile(presentation.id).writeText(json, Charsets.UTF_8)
        dao.setPendingAutosave(presentation.id, true)
    }

    suspend fun hasAutosaveDraft(presentationId: String): Boolean = withContext(Dispatchers.IO) {
        val autoFile = storage.autosaveJsonFile(presentationId)
        val mainFile = storage.presentationJsonFile(presentationId)
        autoFile.exists() && (!mainFile.exists() || autoFile.lastModified() >= mainFile.lastModified())
    }

    suspend fun loadPresentation(presentationId: String, preferAutosave: Boolean = false): Presentation? =
        withContext(Dispatchers.IO) {
            val targetFile = if (preferAutosave && storage.autosaveJsonFile(presentationId).exists()) {
                storage.autosaveJsonFile(presentationId)
            } else {
                storage.presentationJsonFile(presentationId)
            }
            if (!targetFile.exists()) return@withContext null
            val parsed = runCatching {
                PresentationJsonSerializer.fromJsonString(targetFile.readText(Charsets.UTF_8))
            }.getOrNull()
            if (parsed != null) {
                dao.markOpened(presentationId, System.currentTimeMillis())
            }
            parsed
        }

    suspend fun discardAutosave(presentationId: String) = withContext(Dispatchers.IO) {
        storage.autosaveJsonFile(presentationId).delete()
        dao.setPendingAutosave(presentationId, false)
    }

    suspend fun renamePresentation(presentationId: String, newTitle: String) = withContext(Dispatchers.IO) {
        val cleanTitle = newTitle.trim().ifEmpty { return@withContext }
        val loaded = loadPresentation(presentationId)
        if (loaded != null) {
            savePresentation(loaded.copy(title = cleanTitle))
        } else {
            dao.rename(presentationId, cleanTitle, System.currentTimeMillis())
        }
    }

    suspend fun duplicatePresentation(presentationId: String, copySuffix: String = "Copy"): PresentationEntity? =
        withContext(Dispatchers.IO) {
            val existing = dao.getById(presentationId)
            val original = loadPresentation(presentationId) ?: return@withContext null
            val newId = UUID.randomUUID().toString()
            storage.copyProjectAssets(presentationId, newId)
            val cloned = original.copy(
                id = newId,
                title = "${original.title} ($copySuffix)",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            savePresentation(cloned, folderTagOverride = existing?.folderTag ?: "general")
        }

    suspend fun deletePresentation(presentationId: String) = withContext(Dispatchers.IO) {
        storage.deleteProjectDir(presentationId)
        dao.deleteById(presentationId)
    }

    suspend fun batchDeletePresentations(ids: List<String>) = withContext(Dispatchers.IO) {
        if (ids.isEmpty()) return@withContext
        ids.forEach { storage.deleteProjectDir(it) }
        dao.deleteByIds(ids)
    }

    suspend fun batchDuplicatePresentations(ids: List<String>, copySuffix: String = "Copy") =
        withContext(Dispatchers.IO) {
            ids.forEach { id -> duplicatePresentation(id, copySuffix) }
        }

    suspend fun batchMoveToFolder(ids: List<String>, folderTag: String) = withContext(Dispatchers.IO) {
        if (ids.isEmpty()) return@withContext
        dao.updateFolderTagForIds(ids, folderTag, System.currentTimeMillis())
    }

    suspend fun setPresentationLocked(presentationId: String, locked: Boolean) = withContext(Dispatchers.IO) {
        dao.setLocked(presentationId, locked, System.currentTimeMillis())
    }

    suspend fun getEntity(presentationId: String): PresentationEntity? = withContext(Dispatchers.IO) {
        dao.getById(presentationId)
    }

    suspend fun recordSearchQuery(query: String) = withContext(Dispatchers.IO) {
        val clean = query.trim()
        if (clean.length < 2) return@withContext
        searchHistoryDao?.upsert(SearchHistoryEntity(queryText = clean, searchedAt = System.currentTimeMillis()))
    }

    suspend fun clearSearchHistory() = withContext(Dispatchers.IO) {
        searchHistoryDao?.clearAll()
    }
}
