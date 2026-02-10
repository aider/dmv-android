package com.dmv.texas.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "questions",
    indices = [
        Index(value = ["stateCode", "topic"]),
        Index(value = ["stateCode", "difficulty"]),
        Index(value = ["stateCode", "topic", "difficulty"])
    ]
)
data class QuestionEntity(
    @PrimaryKey val id: String,
    val stateCode: String,
    val packVersion: Int,
    val topic: String,
    val difficulty: Int,
    val text: String,
    val choices: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val reference: String,
    val imageAssetId: String?
)
