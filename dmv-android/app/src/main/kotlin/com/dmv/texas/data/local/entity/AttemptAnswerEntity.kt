package com.dmv.texas.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "attempt_answers",
    primaryKeys = ["attemptId", "questionId"],
    foreignKeys = [
        ForeignKey(
            entity = AttemptEntity::class,
            parentColumns = ["id"],
            childColumns = ["attemptId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AttemptAnswerEntity(
    val attemptId: Long,
    val questionId: String,
    val selectedIndex: Int,
    val isCorrect: Boolean,
    val answeredAtTimestamp: Long
)
