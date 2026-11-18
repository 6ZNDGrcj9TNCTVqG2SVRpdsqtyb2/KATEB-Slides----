package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "template_schemas")
data class TemplateSchemaEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val nameAr: String,
    val nameEn: String,
    val nameFa: String,
    val primaryHex: String,
    val secondaryHex: String,
    val accentHex: String,
    val bgHex: String,
    val titleColorHex: String,
    val bodyColorHex: String,
    val sortOrder: Int = 0,
    val headingFontId: String = "cairo",
    val bodyFontId: String = "cairo",
    val defaultTransitionId: String = "FADE",
    val coverStyleId: String = "SIDE_RIBBON"
)

@Dao
interface TemplateDao {
    @Query("SELECT * FROM template_schemas ORDER BY sortOrder ASC")
    fun observeAll(): Flow<List<TemplateSchemaEntity>>

    @Query("SELECT COUNT(*) FROM template_schemas")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(templates: List<TemplateSchemaEntity>)

    @Query("DELETE FROM template_schemas")
    suspend fun clearAll()
}
