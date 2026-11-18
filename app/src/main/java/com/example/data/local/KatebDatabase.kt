package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PresentationEntity::class,
        TemplateSchemaEntity::class,
        SearchHistoryEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class KatebDatabase : RoomDatabase() {
    abstract fun presentationDao(): PresentationDao
    abstract fun templateDao(): TemplateDao
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE presentations ADD COLUMN folderTag TEXT NOT NULL DEFAULT 'general'")
                db.execSQL("ALTER TABLE presentations ADD COLUMN isLocked INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE presentations ADD COLUMN pendingCloudSync INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE presentations ADD COLUMN cloudSyncedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE presentations ADD COLUMN updatedByUid TEXT NOT NULL DEFAULT ''")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `template_schemas` (
                        `id` TEXT NOT NULL,
                        `categoryId` TEXT NOT NULL,
                        `nameAr` TEXT NOT NULL,
                        `nameEn` TEXT NOT NULL,
                        `nameFa` TEXT NOT NULL,
                        `primaryHex` TEXT NOT NULL,
                        `secondaryHex` TEXT NOT NULL,
                        `accentHex` TEXT NOT NULL,
                        `bgHex` TEXT NOT NULL,
                        `titleColorHex` TEXT NOT NULL,
                        `bodyColorHex` TEXT NOT NULL,
                        `sortOrder` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `search_history` (
                        `queryText` TEXT NOT NULL,
                        `searchedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`queryText`)
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE template_schemas ADD COLUMN headingFontId TEXT NOT NULL DEFAULT 'cairo'")
                db.execSQL("ALTER TABLE template_schemas ADD COLUMN bodyFontId TEXT NOT NULL DEFAULT 'cairo'")
                db.execSQL("ALTER TABLE template_schemas ADD COLUMN defaultTransitionId TEXT NOT NULL DEFAULT 'FADE'")
                db.execSQL("ALTER TABLE template_schemas ADD COLUMN coverStyleId TEXT NOT NULL DEFAULT 'SIDE_RIBBON'")
            }
        }

        @Volatile
        private var instance: KatebDatabase? = null

        fun getInstance(context: Context): KatebDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    KatebDatabase::class.java,
                    "kateb_slides.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
