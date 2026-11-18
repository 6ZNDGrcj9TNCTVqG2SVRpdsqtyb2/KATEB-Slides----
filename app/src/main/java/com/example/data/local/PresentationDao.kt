package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PresentationDao {
    @Query("SELECT * FROM presentations ORDER BY lastOpenedAt DESC")
    fun observeRecent(): Flow<List<PresentationEntity>>

    @Query("SELECT * FROM presentations ORDER BY updatedAt DESC")
    fun observeAllSaved(): Flow<List<PresentationEntity>>

    @Query("SELECT * FROM presentations WHERE sourceType LIKE 'IMPORTED_%' ORDER BY updatedAt DESC")
    fun observeImported(): Flow<List<PresentationEntity>>

    @Query("SELECT * FROM presentations WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): PresentationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PresentationEntity)

    @Query("UPDATE presentations SET title = :title, updatedAt = :updatedAt, pendingCloudSync = 1 WHERE id = :id")
    suspend fun rename(id: String, title: String, updatedAt: Long)

    @Query("UPDATE presentations SET lastOpenedAt = :timestamp WHERE id = :id")
    suspend fun markOpened(id: String, timestamp: Long)

    @Query("UPDATE presentations SET hasPendingAutosave = :hasPending WHERE id = :id")
    suspend fun setPendingAutosave(id: String, hasPending: Boolean)

    @Query("UPDATE presentations SET isLocked = :locked, updatedAt = :updatedAt, pendingCloudSync = 1 WHERE id = :id")
    suspend fun setLocked(id: String, locked: Boolean, updatedAt: Long)

    @Query("UPDATE presentations SET folderTag = :folderTag, updatedAt = :updatedAt, pendingCloudSync = 1 WHERE id IN (:ids)")
    suspend fun updateFolderTagForIds(ids: List<String>, folderTag: String, updatedAt: Long)

    @Query("SELECT * FROM presentations WHERE pendingCloudSync = 1")
    suspend fun getPendingCloudSync(): List<PresentationEntity>

    @Query("UPDATE presentations SET pendingCloudSync = 0, cloudSyncedAt = :syncedAt, updatedByUid = :uid WHERE id = :id")
    suspend fun markCloudSynced(id: String, syncedAt: Long, uid: String)

    @Query("DELETE FROM presentations WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM presentations WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)
}
