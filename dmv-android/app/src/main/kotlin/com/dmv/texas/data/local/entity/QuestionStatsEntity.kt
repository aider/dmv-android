package com.dmv.texas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "question_stats")
data class QuestionStatsEntity(
    @PrimaryKey val questionId: String,
    val timesSeen: Int = 0,
    val timesCorrect: Int = 0,
    val timesWrong: Int = 0,
    val lastSeenAt: Long? = null
)
