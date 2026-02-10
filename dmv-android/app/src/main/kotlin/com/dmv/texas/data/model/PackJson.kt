package com.dmv.texas.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PackJson(
    val stateCode: String,
    val version: Int,
    val totalQuestions: Int,
    val topics: Map<String, Int>,
    val generatedDate: String,
    val questions: List<QuestionJson>
)

@Serializable
data class QuestionJson(
    val id: String,
    val topic: String,
    val difficulty: Int,
    val text: String,
    val choices: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val reference: String,
    val image: ImageRef? = null
)

@Serializable
data class ImageRef(
    val type: String,
    val assetId: String
)
