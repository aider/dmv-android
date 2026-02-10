package com.dmv.texas.ui.screen.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dmv.texas.data.local.dao.TopicAccuracy
import com.dmv.texas.data.local.db.DMVDatabase
import com.dmv.texas.data.local.entity.AttemptEntity
import com.dmv.texas.data.repository.StatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    data class StatsState(
        val topicAccuracies: List<TopicAccuracy> = emptyList(),
        val recentAttempts: List<AttemptEntity> = emptyList(),
        val mistakeCount: Int = 0,
        val overallCorrect: Int = 0,
        val overallTotal: Int = 0,
        val isLoading: Boolean = true
    ) {
        val overallPercentage: Int
            get() = if (overallTotal > 0) (overallCorrect * 100) / overallTotal else 0
    }

    private val db = DMVDatabase.getInstance(application)
    private val statsRepo = StatsRepository(db, db.attemptDao(), db.attemptAnswerDao(), db.questionStatsDao())

    private val _state = MutableStateFlow(StatsState())
    val state: StateFlow<StatsState> = _state

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val topicAccuracies = statsRepo.getAccuracyByTopic("TX")
            val mistakeCount = statsRepo.getMistakeCount()

            // Overall accuracy: sum of all topic accuracies
            val overallCorrect = topicAccuracies.sumOf { it.totalCorrect }
            val overallTotal = topicAccuracies.sumOf { it.totalSeen }

            _state.value = _state.value.copy(
                topicAccuracies = topicAccuracies,
                mistakeCount = mistakeCount,
                overallCorrect = overallCorrect,
                overallTotal = overallTotal,
                isLoading = false
            )
        }

        // Collect recent attempts as a Flow
        viewModelScope.launch {
            statsRepo.getRecentAttempts("TX").collect { attempts ->
                _state.value = _state.value.copy(recentAttempts = attempts)
            }
        }
    }
}
