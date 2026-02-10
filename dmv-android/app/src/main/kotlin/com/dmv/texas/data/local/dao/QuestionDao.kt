package com.dmv.texas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dmv.texas.data.local.entity.QuestionEntity

@Dao
interface QuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuestionEntity>)

    @Query("UPDATE questions SET isActive = 0 WHERE stateCode = :stateCode")
    suspend fun deactivateByStateCode(stateCode: String)

    @Query(
        """
        SELECT * FROM questions
        WHERE stateCode = :stateCode
          AND isActive = 1
          AND topic IN (:topics)
          AND difficulty BETWEEN :minDiff AND :maxDiff
        ORDER BY RANDOM()
        LIMIT :limit
        """
    )
    suspend fun getRandomQuestions(
        stateCode: String,
        topics: List<String>,
        minDiff: Int,
        maxDiff: Int,
        limit: Int
    ): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun getById(id: String): QuestionEntity?

    @Query(
        """
        SELECT topic, COUNT(*) as count
        FROM questions
        WHERE stateCode = :stateCode AND isActive = 1
        GROUP BY topic
        ORDER BY topic
        """
    )
    suspend fun getTopicCounts(stateCode: String): List<TopicCount>

    @Query("SELECT COUNT(*) FROM questions WHERE stateCode = :stateCode AND isActive = 1")
    suspend fun getCount(stateCode: String): Int

    @Query("SELECT COUNT(*) FROM questions WHERE imageAssetId IS NOT NULL AND stateCode = :stateCode AND isActive = 1")
    suspend fun getImageQuestionCount(stateCode: String): Int

    @Query("SELECT DISTINCT imageAssetId FROM questions WHERE imageAssetId IS NOT NULL AND stateCode = :stateCode")
    suspend fun getAllImageAssetIds(stateCode: String): List<String>

    @Query(
        """
        SELECT imageAssetId AS assetId, COUNT(*) AS count
        FROM questions
        WHERE imageAssetId IS NOT NULL AND stateCode = :stateCode
        GROUP BY imageAssetId
        """
    )
    suspend fun getAllImageAssetIdCounts(stateCode: String): List<AssetIdCount>

    @Query("SELECT * FROM questions WHERE stateCode = :stateCode")
    suspend fun getAllByState(stateCode: String): List<QuestionEntity>
}

data class TopicCount(val topic: String, val count: Int)

data class AssetIdCount(val assetId: String, val count: Int)
