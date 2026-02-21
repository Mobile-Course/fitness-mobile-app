package com.fitness.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        PostEntity::class,
        AchievementEntity::class,
        UserAchievementEntity::class,
        UserXpEntity::class,
        PublicUserProfileCacheEntity::class
    ],
    version = 11,
    exportSchema = false
)
@androidx.room.TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun achievementDao(): AchievementDao
    abstract fun publicUserProfileCacheDao(): PublicUserProfileCacheDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance
                    ?: Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "fitness.db"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                        .also { instance = it }
            }
        }
    }
}
