package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "presentations")
data class PresentationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val languageCode: String,
    val aspectRatioId: String,
    val themeId: String,
    val sourceType: String,
    val slideCount: Int,
    val thumbnailPath: String,
    val projectDirPath: String,
    val createdAt: Long,
    val updatedAt: Long,
    val lastOpenedAt: Long,
    val hasPendingAutosave: Boolean = false,
    val folderTag: String = "general",
    val isLocked: Boolean = false,
    val pendingCloudSync: Boolean = true,
    val cloudSyncedAt: Long = 0L,
    val updatedByUid: String = ""
)
