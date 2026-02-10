package com.dmv.texas.data.repository

import androidx.room.withTransaction
import com.dmv.texas.data.local.dao.AttemptAnswerDao
import com.dmv.texas.data.local.dao.AttemptDao
import com.dmv.texas.data.local.dao.MissedAnswer
import com.dmv.texas.data.local.dao.QuestionStatsDao
import com.dmv.texas.data.local.dao.TopicAccuracy
import com.dmv.texas.data.local.db.DMVDatabase
import com.dmv.texas.data.local.entity.AttemptAnswerEntity
import com.dmv.texas.data.local.entity.AttemptEntity
import com.dmv.texas.data.local.entity.QuestionEntity
import com.dmv.texas.data.model.QuizMode
import kotlinx.coroutines.flow.Flow

class StatsRepository(
    private val db: DMVDatabase,
    private val attemptDao: AttemptDao,
    private val attemptAnswerDao: AttemptAnswerDao,
    private val questionStatsDao: QuestionStatsDao
) {
    suspend fun saveAttempt(
        stateCode: String,
        mode: QuizMode,
        topics: List<String>,
        questions: List<QuestionEntity>,
        answers: Map<String, Int>,
        durationMs: Long
    ): Long {
        val correct = answers.count { (qId, selected) ->
            questions.find { it.id == qId }?.correctIndex == selected
        }

        return db.withTransaction {
            val attemptId = attemptDao.insert(
                AttemptEntity(
                    stateCode = stateCode,
                    createdAt = System.currentTimeMillis(),
                    mode = mode.name,
                    topics = topics,
                    total = questions.size,
                    correct = correct,
                    durationMs = durationMs
                )
            )

            val now = System.currentTimeMillis()
            val answerEntities = answers.map { (qId, selected) ->
                val question = questions.first { it.id == qId }
                AttemptAnswerEntity(
                    attemptId = attemptId,
                    questionId = qId,
                    selectedIndex = selected,
                    isCorrect = question.correctIndex == selected,
                    answeredAtTimestamp = now
                )
            }
            attemptAnswerDao.insertAll(answerEntities)

            // Update per-question stats
            answers.forEach { (qId, selected) ->
                val question = questions.first { it.id == qId }
                val isCorrect = question.correctIndex == selected
                questionStatsDao.upsert(
                    questionId = qId,
                    correct = if (isCorrect) 1 else 0,
                    wrong = if (isCorrect) 0 else 1,
                    timestamp = now
                )
            }

            attemptId
        }
    }

    fun getRecentAttempts(stateCode: String): Flow<List<AttemptEntity>> {
        return attemptDao.getRecent(stateCode)
    }

    suspend fun getMissedAnswers(attemptId: Long): List<MissedAnswer> {
        return attemptAnswerDao.getMissedAnswers(attemptId)
    }

    suspend fun getAccuracyByTopic(stateCode: String): List<TopicAccuracy> {
        return questionStatsDao.getAccuracyByTopic(stateCode)
    }

    suspend fun getMistakeCount(): Int {
        return questionStatsDao.getMistakeCount()
    }
}
