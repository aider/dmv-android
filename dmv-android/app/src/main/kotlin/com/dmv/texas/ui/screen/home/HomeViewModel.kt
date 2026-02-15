package com.dmv.texas.ui.screen.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dmv.texas.data.local.dao.TopicCount
import com.dmv.texas.data.local.db.DMVDatabase
import com.dmv.texas.data.model.QuizConfig
import com.dmv.texas.data.model.QuizMode
import com.dmv.texas.data.repository.QuestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    data class HomeState(
        val topics: List<TopicCount> = emptyList(),
        val selectedMode: QuizMode = QuizMode.PRACTICE,
        val selectedTopics: Set<String> = emptySet(),
        val questionCount: Int = 20,
        val minDifficulty: Int = 1,
        val maxDifficulty: Int = 3,
        val mistakeCount: Int = 0,
        val totalQuestions: Int = 0,
        val isLoading: Boolean = true
    )

    private val db = DMVDatabase.getInstance(application)
    private val questionRepo = QuestionRepository(db.questionDao(), db.questionStatsDao())

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val topics = questionRepo.getTopicCounts("TX")
            val mistakeCount = db.questionStatsDao().getMistakeCount()
            val totalQuestions = questionRepo.getQuestionCount("TX")
            _state.value = HomeState(
                topics = topics,
                selectedTopics = topics.map { it.topic }.toSet(),
                mistakeCount = mistakeCount,
                totalQuestions = totalQuestions,
                isLoading = false
            )
        }
    }

    /** Refresh mistake count and topics (called when returning from a quiz). */
    fun refresh() {
        viewModelScope.launch {
            val mistakeCount = db.questionStatsDao().getMistakeCount()
            _state.value = _state.value.copy(mistakeCount = mistakeCount)
        }
    }

    fun setMode(mode: QuizMode) {
        _state.value = _state.value.copy(selectedMode = mode)
    }

    fun toggleTopic(topic: String) {
        val current = _state.value.selectedTopics.toMutableSet()
        if (current.contains(topic)) {
            current.remove(topic)
        } else {
            current.add(topic)
        }
        _state.value = _state.value.copy(selectedTopics = current)
    }

    fun selectAllTopics() {
        _state.value = _state.value.copy(
            selectedTopics = _state.value.topics.map { it.topic }.toSet()
        )
    }

    fun clearAllTopics() {
        _state.value = _state.value.copy(selectedTopics = emptySet())
    }

    fun setQuestionCount(count: Int) {
        _state.value = _state.value.copy(questionCount = count)
    }

    fun setDifficultyRange(min: Int, max: Int) {
        _state.value = _state.value.copy(minDifficulty = min, maxDifficulty = max)
    }

    fun buildConfig(): QuizConfig {
        val s = _state.value
        return QuizConfig(
            mode = s.selectedMode,
            stateCode = "TX",
            topics = if (s.selectedMode == QuizMode.MISTAKES) {
                s.topics.map { it.topic }
            } else {
                s.selectedTopics.toList()
            },
            questionCount = s.questionCount,
            minDifficulty = s.minDifficulty,
            maxDifficulty = s.maxDifficulty,
            timeLimitMs = if (s.selectedMode == QuizMode.EXAM) 30L * 60 * 1000 else null
        )
    }

    /** Returns a default config for one-tap Quick Start (Practice, 20 Qs, all topics). */
    fun quickStartConfig(): QuizConfig {
        val s = _state.value
        return QuizConfig(
            mode = QuizMode.PRACTICE,
            stateCode = "TX",
            topics = s.topics.map { it.topic },
            questionCount = 20,
            minDifficulty = 1,
            maxDifficulty = 3
        )
    }

    /** Returns true if the current config can start a quiz. */
    fun canStart(): Boolean {
        val s = _state.value
        if (s.isLoading) return false
        if (s.selectedMode == QuizMode.MISTAKES) return s.mistakeCount > 0
        return s.selectedTopics.isNotEmpty()
    }
}
