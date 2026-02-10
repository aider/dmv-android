package com.dmv.texas.data.model

data class QuizConfig(
    val mode: QuizMode = QuizMode.PRACTICE,
    val stateCode: String = "TX",
    val topics: List<String> = emptyList(),
    val questionCount: Int = 20,
    val minDifficulty: Int = 1,
    val maxDifficulty: Int = 3,
    val timeLimitMs: Long? = null
)
