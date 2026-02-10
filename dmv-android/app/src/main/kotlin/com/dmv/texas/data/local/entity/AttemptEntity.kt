package com.dmv.texas.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attempts",
    indices = [Index(value = ["stateCode", "createdAt"])]
)
data class AttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val stateCode: String,
    val createdAt: Long,
    val mode: String,
    val topics: List<String>,
    val total: Int,
    val correct: Int,
    val durationMs: Long
)
