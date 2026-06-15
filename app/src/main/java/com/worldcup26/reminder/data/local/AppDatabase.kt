package com.worldcup26.reminder.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [MatchEntity::class, SelectionEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        /** v2 adds the per-match broadcaster column; selections are preserved. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE matches ADD COLUMN broadcaster TEXT")
            }
        }

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "worldcup26.db",
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }
    }
}
