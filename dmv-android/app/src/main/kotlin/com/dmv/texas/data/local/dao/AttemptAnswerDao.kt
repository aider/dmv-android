package com.dmv.texas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dmv.texas.data.local.entity.AttemptAnswerEntity

@Dao
interface AttemptAnswerDao {
    @Insert
    suspend fun insertAll(answers: List<AttemptAnswerEntity>)

    @Query(
        """
        SELECT aa.attemptId, aa.questionId, aa.selectedIndex, aa.isCorrect,
               aa.answeredAtTimestamp,
               q.text AS questionText, q.choices AS questionChoices,
               q.correctIndex AS questionCorrectIndex,
               q.explanation AS questionExplanation,
               q.imageAssetId AS questionImageAssetId
        FROM attempt_answers aa
        JOIN questions q ON aa.questionId = q.id
        WHERE aa.attemptId = :attemptId AND aa.isCorrect = 0
        """
    )
    suspend fun getMissedAnswers(attemptId: Long): List<MissedAnswer>
}

data class MissedAnswer(
    val attemptId: Long,
    val questionId: String,
    val selectedIndex: Int,
    val isCorrect: Boolean,
    val answeredAtTimestamp: Long,
    val questionText: String,
    val questionChoices: String,
    val questionCorrectIndex: Int,
    val questionExplanation: String,
    val questionImageAssetId: String?
)
