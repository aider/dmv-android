package com.dmv.texas.data.repository

import com.dmv.texas.data.local.dao.QuestionDao
import com.dmv.texas.data.local.dao.QuestionStatsDao
import com.dmv.texas.data.local.dao.TopicCount
import com.dmv.texas.data.local.entity.QuestionEntity
import com.dmv.texas.data.model.QuizConfig
import com.dmv.texas.data.model.QuizMode

class QuestionRepository(
    private val questionDao: QuestionDao,
    private val questionStatsDao: QuestionStatsDao
) {
    suspend fun getQuestionsForQuiz(config: QuizConfig): List<QuestionEntity> {
        return when (config.mode) {
            QuizMode.MISTAKES -> {
                val ids = questionStatsDao.getMostMissedIds(
                    config.stateCode,
                    config.questionCount
                )
                ids.mapNotNull { questionDao.getById(it) }
            }
            else -> {
                questionDao.getRandomQuestions(
                    stateCode = config.stateCode,
                    topics = config.topics,
                    minDiff = config.minDifficulty,
                    maxDiff = config.maxDifficulty,
                    limit = config.questionCount
                )
            }
        }
    }

    suspend fun getTopicCounts(stateCode: String): List<TopicCount> {
        return questionDao.getTopicCounts(stateCode)
    }

    suspend fun getQuestionCount(stateCode: String): Int {
        return questionDao.getCount(stateCode)
    }
}
