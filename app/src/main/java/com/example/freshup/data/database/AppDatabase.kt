package com.example.freshup.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ProductEntity::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Миграция v1→v2: добавляем колонку notificationShown без потери данных.
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE products ADD COLUMN notificationShown INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        // Миграция v2→v3: фото продукта + индивидуальный срок уведомления.
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN photoPath TEXT")
                db.execSQL(
                    "ALTER TABLE products ADD COLUMN notifyDaysBefore INTEGER NOT NULL DEFAULT 3"
                )
            }
        }

        // Миграция v3→v4: место хранения, статус (съедено/выброшено), дата списания и штрихкод.
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN storageLocation TEXT NOT NULL DEFAULT 'FRIDGE'")
                db.execSQL("ALTER TABLE products ADD COLUMN status TEXT NOT NULL DEFAULT 'ACTIVE'")
                db.execSQL("ALTER TABLE products ADD COLUMN consumedTimestamp INTEGER")
                db.execSQL("ALTER TABLE products ADD COLUMN barcode TEXT")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "freshup_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
