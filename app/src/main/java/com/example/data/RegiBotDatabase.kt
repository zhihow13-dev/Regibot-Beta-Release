package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.BotDao
import com.example.data.entity.AutomationLogEntity
import com.example.data.entity.BotProfileEntity
import com.example.data.entity.SessionRecord

@Database(
    entities = [
        SessionRecord::class,
        AutomationLogEntity::class,
        BotProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RegiBotDatabase : RoomDatabase() {
    abstract fun botDao(): BotDao

    companion object {
        @Volatile
        private var INSTANCE: RegiBotDatabase? = null

        fun getDatabase(context: Context): RegiBotDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RegiBotDatabase::class.java,
                    "regibot_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
