package com.haitao.haimei.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.haitao.haimei.data.dao.DiaryDao
import com.haitao.haimei.data.dao.PlanItemDao
import com.haitao.haimei.data.dao.PreferenceItemDao
import com.haitao.haimei.data.dao.ProfileDao
import com.haitao.haimei.data.entity.DiaryEntryEntity
import com.haitao.haimei.data.entity.PlanItemEntity
import com.haitao.haimei.data.entity.PreferenceItemEntity
import com.haitao.haimei.data.entity.ProfileEntity

@Database(
    entities = [
        ProfileEntity::class,
        PreferenceItemEntity::class,
        PlanItemEntity::class,
        DiaryEntryEntity::class
    ],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun preferenceItemDao(): PreferenceItemDao
    abstract fun planItemDao(): PlanItemDao
    abstract fun diaryDao(): DiaryDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE profile ADD COLUMN lunarMonthIndex INTEGER")
                db.execSQL("ALTER TABLE profile ADD COLUMN lunarDay INTEGER")
                db.execSQL("ALTER TABLE profile ADD COLUMN lunarLeapMonth INTEGER")
            }
        }

        private val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE diary_entries ADD COLUMN imageUris TEXT")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "haimei.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
