package com.dmv.texas.data.local.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface QuestionStatsDao {
    @Query(
        """
        INSERT INTO question_stats (questionId, timesSeen, timesCorrect, timesWrong, lastSeenAt)
        VALUES (:questionId, 1, :correct, :wrong, :timestamp)
        ON CONFLICT(questionId) DO UPDATE SET
            timesSeen = timesSeen + 1,
            timesCorrect = timesCorrect + :correct,
            timesWrong = timesWrong + :wrong,
            lastSeenAt = :timestamp
        """
    )
    suspend fun upsert(questionId: String, correct: Int, wrong: Int, timestamp: Long)

    @Query(
        """
        SELECT qs.questionId FROM question_stats qs
        JOIN questions q ON qs.questionId = q.id
        WHERE q.stateCode = :stateCode AND q.isActive = 1 AND qs.timesWrong > 0
        ORDER BY CAST(qs.timesWrong AS REAL) / CAST(qs.timesSeen AS REAL) DESC
        LIMIT :limit
        """
    )
    suspend fun getMostMissedIds(stateCode: String, limit: Int): List<String>

    @Query(
        """
        SELECT q.topic,
            SUM(qs.timesCorrect) as totalCorrect,
            SUM(qs.timesSeen) as totalSeen
        FROM question_stats qs
        JOIN questions q ON qs.questionId = q.id
        WHERE q.stateCode = :stateCode
        GROUP BY q.topic
        """
    )
    suspend fun getAccuracyByTopic(stateCode: String): List<TopicAccuracy>

    @Query("SELECT COUNT(*) FROM question_stats WHERE timesWrong > 0")
    suspend fun getMistakeCount(): Int
}

data class TopicAccuracy(val topic: String, val totalCorrect: Int, val totalSeen: Int)
