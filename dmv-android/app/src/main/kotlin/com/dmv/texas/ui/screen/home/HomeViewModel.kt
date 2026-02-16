package com.dmv.texas.ui.screen.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dmv.texas.DMVApp
import com.dmv.texas.analytics.AnalyticsEvents
import com.dmv.texas.data.local.dao.TopicAccuracy
import com.dmv.texas.data.local.dao.TopicCount
import com.dmv.texas.data.local.db.DMVDatabase
import com.dmv.texas.data.model.QuizConfig
import com.dmv.texas.data.model.QuizMode
import com.dmv.texas.data.repository.QuestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * Whether the user has completed at least one quiz.
     * Drives the Progress Card visibility.
     */
    sealed interface HomeUserState {
        data object FirstRun : HomeUserState
        data class Returning(
            val lastScorePct: Int,
            val mistakeCount: Int,
            val weakestTopic: String?,
            val weakestTopicPct: Int?
        ) : HomeUserState
    }

    data class HomeState(
        val topics: List<TopicCount> = emptyList(),
        val selectedMode: QuizMode = QuizMode.PRACTICE,
        val selectedTopics: Set<String> = emptySet(),
        val questionCount: Int = 20,
        val minDifficulty: Int = 1,
        val maxDifficulty: Int = 3,
        val mistakeCount: Int = 0,
        val totalQuestions: Int = 0,
        val isLoading: Boolean = true,
        val userState: HomeUserState = HomeUserState.FirstRun
    )

    private val app = getApplication<DMVApp>()
    private val db = DMVDatabase.getInstance(application)
    private val questionRepo = QuestionRepository(db.questionDao(), db.questionStatsDao())
    private val analytics = app.analytics

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
            val lastAttempt = db.attemptDao().getMostRecent("TX")

            val userState = if (lastAttempt != null) {
                val topicAccuracy = db.questionStatsDao().getAccuracyByTopic("TX")
                val weakest = findWeakestTopic(topicAccuracy)
                HomeUserState.Returning(
                    lastScorePct = if (lastAttempt.total > 0) {
                        (lastAttempt.correct * 100) / lastAttempt.total
                    } else 0,
                    mistakeCount = mistakeCount,
                    weakestTopic = weakest?.first,
                    weakestTopicPct = weakest?.second
                )
            } else {
                HomeUserState.FirstRun
            }

            _state.value = HomeState(
                topics = topics,
                selectedTopics = topics.map { it.topic }.toSet(),
                mistakeCount = mistakeCount,
                totalQuestions = totalQuestions,
                isLoading = false,
                userState = userState
            )
            analytics.logEvent(AnalyticsEvents.HOME_VIEWED, mapOf(
                "question_count" to totalQuestions
            ))
        }
    }

    /** Find the topic with the lowest accuracy (min 1 question seen). */
    private fun findWeakestTopic(accuracy: List<TopicAccuracy>): Pair<String, Int>? {
        if (accuracy.isEmpty()) return null
        val weakest = accuracy
            .filter { it.totalSeen > 0 }
            .minByOrNull { it.totalCorrect.toFloat() / it.totalSeen }
            ?: return null
        val pct = (weakest.totalCorrect * 100) / weakest.totalSeen
        return weakest.topic to pct
    }

    /** Refresh mistake count and progress context (called when returning from a quiz). */
    fun refresh() {
        viewModelScope.launch {
            val mistakeCount = db.questionStatsDao().getMistakeCount()
            val lastAttempt = db.attemptDao().getMostRecent("TX")
            val topicAccuracy = db.questionStatsDao().getAccuracyByTopic("TX")
            val weakest = findWeakestTopic(topicAccuracy)

            val userState = if (lastAttempt != null) {
                HomeUserState.Returning(
                    lastScorePct = if (lastAttempt.total > 0) {
                        (lastAttempt.correct * 100) / lastAttempt.total
                    } else 0,
                    mistakeCount = mistakeCount,
                    weakestTopic = weakest?.first,
                    weakestTopicPct = weakest?.second
                )
            } else {
                HomeUserState.FirstRun
            }

            _state.value = _state.value.copy(
                mistakeCount = mistakeCount,
                userState = userState
            )
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

    /** Returns a config for drilling the weakest topic. */
    fun drillWeakTopicConfig(topic: String): QuizConfig {
        return QuizConfig(
            mode = QuizMode.TOPIC_DRILL,
            stateCode = "TX",
            topics = listOf(topic),
            questionCount = 20,
            minDifficulty = 1,
            maxDifficulty = 3
        )
    }

    /** Returns a config for reviewing mistakes. */
    fun reviewMistakesConfig(): QuizConfig {
        val s = _state.value
        return QuizConfig(
            mode = QuizMode.MISTAKES,
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
