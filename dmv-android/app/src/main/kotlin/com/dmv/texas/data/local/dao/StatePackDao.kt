package com.dmv.texas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dmv.texas.data.local.entity.StatePackEntity

@Dao
interface StatePackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(pack: StatePackEntity)

    @Query("SELECT * FROM state_packs WHERE stateCode = :stateCode")
    suspend fun getByStateCode(stateCode: String): StatePackEntity?

    @Query("SELECT * FROM state_packs")
    suspend fun getAll(): List<StatePackEntity>
}
