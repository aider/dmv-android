package com.dmv.texas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dmv.texas.data.local.entity.AttemptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttemptDao {
    @Insert
    suspend fun insert(attempt: AttemptEntity): Long

    @Query(
        """
        SELECT * FROM attempts
        WHERE stateCode = :stateCode
        ORDER BY createdAt DESC
        LIMIT :limit
        """
    )
    fun getRecent(stateCode: String, limit: Int = 20): Flow<List<AttemptEntity>>

    @Query("SELECT * FROM attempts WHERE id = :id")
    suspend fun getById(id: Long): AttemptEntity?

    @Query(
        """
        SELECT * FROM attempts
        WHERE stateCode = :stateCode
        ORDER BY createdAt DESC
        LIMIT 1
        """
    )
    suspend fun getMostRecent(stateCode: String): AttemptEntity?
}
