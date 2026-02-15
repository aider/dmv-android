package com.dmv.texas.ui.screen.quiz

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dmv.texas.DMVApp
import com.dmv.texas.analytics.AnalyticsEvents
import com.dmv.texas.data.local.db.DMVDatabase
import com.dmv.texas.data.local.entity.QuestionEntity
import com.dmv.texas.data.model.QuizConfig
import com.dmv.texas.data.model.QuizMode
import com.dmv.texas.data.repository.QuestionRepository
import com.dmv.texas.data.repository.StatsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    data class QuizState(
        val questions: List<QuestionEntity> = emptyList(),
        val currentIndex: Int = 0,
        val answers: Map<String, Int> = emptyMap(),
        val isLoading: Boolean = true,
        val isFinished: Boolean = false,
        val isMissingConfig: Boolean = false,
        val timerMs: Long = 0,
        val startTimeMs: Long = 0,
        val config: QuizConfig = QuizConfig(),
        val showFeedback: Boolean = false,
        val lastAttemptId: Long? = null,
        val durationMs: Long = 0
    ) {
        val currentQuestion: QuestionEntity?
            get() = questions.getOrNull(currentIndex)

        val correctCount: Int
            get() = answers.count { (qId, selected) ->
                questions.find { it.id == qId }?.correctIndex == selected
            }

        val answeredCount: Int
            get() = answers.size

        val progress: Float
            get() = if (questions.isEmpty()) 0f else (currentIndex + 1).toFloat() / questions.size
    }

    private val app = getApplication<DMVApp>()
    private val db = DMVDatabase.getInstance(application)
    private val questionRepo = QuestionRepository(db.questionDao(), db.questionStatsDao())
    private val statsRepo = StatsRepository(db, db.attemptDao(), db.attemptAnswerDao(), db.questionStatsDao())
    private val analytics = app.analytics

    private val _state = MutableStateFlow(QuizState())
    val state: StateFlow<QuizState> = _state

    private var timerJob: Job? = null

    init {
        // Read config from Application-level holder (set by HomeScreen before navigation).
        // Clear it immediately so it is not accidentally re-used.
        val app = getApplication<DMVApp>()
        val config = app.pendingQuizConfig
        if (config != null) {
            app.pendingQuizConfig = null
            loadQuestions(config)
        } else {
            Log.w("QuizViewModel", "No pendingQuizConfig found -- quiz cannot start")
            _state.value = QuizState(isLoading = false, isMissingConfig = true)
        }
    }

    fun loadQuestions(config: QuizConfig) {
        viewModelScope.launch {
            val questions = questionRepo.getQuestionsForQuiz(config)
            _state.value = QuizState(
                questions = questions,
                isLoading = false,
                config = config,
                startTimeMs = System.currentTimeMillis(),
                timerMs = config.timeLimitMs ?: 0
            )
            analytics.logEvent(AnalyticsEvents.QUIZ_STARTED, mapOf(
                "mode" to config.mode.name,
                "question_count" to questions.size,
                "topics" to config.topics.joinToString(",")
            ))
            if (config.timeLimitMs != null) {
                startTimer()
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.timerMs > 0 && !_state.value.isFinished) {
                delay(1000)
                val current = _state.value
                if (current.isFinished) break
                val newTimer = current.timerMs - 1000
                _state.value = current.copy(timerMs = newTimer)
                if (newTimer <= 0) {
                    finishQuiz()
                }
            }
        }
    }

    fun selectAnswer(questionId: String, answerIndex: Int) {
        val current = _state.value
        if (current.isFinished) return

        // In practice mode, don't allow changing answer after feedback is shown
        if (current.config.mode == QuizMode.PRACTICE && current.answers.containsKey(questionId)) return

        val newAnswers = current.answers + (questionId to answerIndex)
        _state.value = current.copy(
            answers = newAnswers,
            showFeedback = current.config.mode == QuizMode.PRACTICE
        )
    }

    fun nextQuestion() {
        val current = _state.value
        if (current.currentIndex < current.questions.size - 1) {
            _state.value = current.copy(
                currentIndex = current.currentIndex + 1,
                showFeedback = false
            )
        } else {
            finishQuiz()
        }
    }

    fun previousQuestion() {
        val current = _state.value
        if (current.currentIndex > 0) {
            _state.value = current.copy(
                currentIndex = current.currentIndex - 1,
                showFeedback = false
            )
        }
    }

    fun finishQuiz() {
        timerJob?.cancel()
        val current = _state.value
        if (current.isFinished) return

        val duration = System.currentTimeMillis() - current.startTimeMs
        _state.value = current.copy(isFinished = true, durationMs = duration)

        val correctCount = current.answers.count { (qId, selected) ->
            current.questions.find { it.id == qId }?.correctIndex == selected
        }
        val scorePct = if (current.questions.isNotEmpty()) {
            (correctCount * 100) / current.questions.size
        } else 0
        analytics.logEvent(AnalyticsEvents.QUIZ_COMPLETED, mapOf(
            "mode" to current.config.mode.name,
            "question_count" to current.questions.size,
            "correct" to correctCount,
            "score_pct" to scorePct,
            "duration_s" to (duration / 1000)
        ))

        viewModelScope.launch {
            val attemptId = statsRepo.saveAttempt(
                stateCode = current.config.stateCode,
                mode = current.config.mode,
                topics = current.config.topics,
                questions = current.questions,
                answers = current.answers,
                durationMs = duration
            )
            _state.value = _state.value.copy(lastAttemptId = attemptId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
