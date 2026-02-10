package com.dmv.texas.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dmv.texas.data.local.dao.AttemptAnswerDao
import com.dmv.texas.data.local.dao.AttemptDao
import com.dmv.texas.data.local.dao.QuestionDao
import com.dmv.texas.data.local.dao.QuestionStatsDao
import com.dmv.texas.data.local.dao.StatePackDao
import com.dmv.texas.data.local.entity.AttemptAnswerEntity
import com.dmv.texas.data.local.entity.AttemptEntity
import com.dmv.texas.data.local.entity.QuestionEntity
import com.dmv.texas.data.local.entity.QuestionStatsEntity
import com.dmv.texas.data.local.entity.StatePackEntity

@Database(
    entities = [
        StatePackEntity::class,
        QuestionEntity::class,
        AttemptEntity::class,
        AttemptAnswerEntity::class,
        QuestionStatsEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class DMVDatabase : RoomDatabase() {
    abstract fun statePackDao(): StatePackDao
    abstract fun questionDao(): QuestionDao
    abstract fun attemptDao(): AttemptDao
    abstract fun attemptAnswerDao(): AttemptAnswerDao
    abstract fun questionStatsDao(): QuestionStatsDao

    companion object {
        @Volatile
        private var INSTANCE: DMVDatabase? = null

        fun getInstance(context: Context): DMVDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    DMVDatabase::class.java,
                    "dmv_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
