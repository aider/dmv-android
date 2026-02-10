package com.dmv.texas.ui.screen.debug

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dmv.texas.data.local.db.DMVDatabase
import com.dmv.texas.util.AssetResolver
import com.dmv.texas.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DataQualityState(
    val isLoading: Boolean = true,
    val duplicates: List<DuplicateGroup> = emptyList(),
    val invalidCorrectIndex: List<String> = emptyList(),
    val unknownTopics: List<Pair<String, String>> = emptyList(),
    val brokenImages: List<String> = emptyList(),
    val emptyFields: List<Pair<String, String>> = emptyList()
)

data class DuplicateGroup(
    val normalizedText: String,
    val questionIds: List<String>
)

class DataQualityViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(DataQualityState())
    val state: StateFlow<DataQualityState> = _state

    companion object {
        private val KNOWN_TOPICS = setOf(
            "SIGNS",
            "RIGHT_OF_WAY",
            "PARKING",
            "INTERSECTIONS",
            "SPEED",
            "SPECIAL_CONDITIONS",
            "GENERAL_KNOWLEDGE",
            "LANE_USAGE"
        )
    }

    init {
        loadReport()
    }

    private fun loadReport() {
        viewModelScope.launch(Dispatchers.IO) {
            val db = DMVDatabase.getInstance(getApplication())
            val assetResolver = AssetResolver(getApplication())
            val stateCode = Constants.STATE_CODE

            val allQuestions = db.questionDao().getAllByState(stateCode)

            // 1. Duplicates: group by normalized text, keep groups with size > 1
            val duplicates = allQuestions
                .groupBy { it.text.trim().lowercase() }
                .filter { it.value.size > 1 }
                .map { (normalizedText, questions) ->
                    DuplicateGroup(
                        normalizedText = normalizedText.take(80) +
                                if (normalizedText.length > 80) "..." else "",
                        questionIds = questions.map { it.id }
                    )
                }

            // 2. Invalid correctIndex: out of range for choices
            val invalidCorrectIndex = allQuestions
                .filter { it.correctIndex < 0 || it.correctIndex >= it.choices.size }
                .map { it.id }

            // 3. Unknown topics: not in the known set
            val unknownTopics = allQuestions
                .filter { it.topic !in KNOWN_TOPICS }
                .map { it.id to it.topic }

            // 4. Broken images: referenced assetIds not in manifest
            val allManifestIds = assetResolver.getAllAssetIds()
            val brokenImages = allQuestions
                .filter { it.imageAssetId != null && it.imageAssetId !in allManifestIds }
                .mapNotNull { it.imageAssetId }
                .distinct()

            // 5. Empty fields: blank text, explanation, reference, or empty choices
            val emptyFields = mutableListOf<Pair<String, String>>()
            for (q in allQuestions) {
                if (q.text.isBlank()) emptyFields.add(q.id to "text")
                if (q.explanation.isBlank()) emptyFields.add(q.id to "explanation")
                if (q.reference.isBlank()) emptyFields.add(q.id to "reference")
                if (q.choices.isEmpty()) emptyFields.add(q.id to "choices")
                if (q.choices.any { it.isBlank() }) emptyFields.add(q.id to "choices (blank entry)")
            }

            _state.value = DataQualityState(
                isLoading = false,
                duplicates = duplicates,
                invalidCorrectIndex = invalidCorrectIndex,
                unknownTopics = unknownTopics,
                brokenImages = brokenImages,
                emptyFields = emptyFields
            )
        }
    }

    fun refresh() {
        _state.value = DataQualityState(isLoading = true)
        loadReport()
    }
}
