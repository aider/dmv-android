package com.dmv.texas.ui.screen.debug

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dmv.texas.data.local.db.DMVDatabase
import com.dmv.texas.util.AssetResolver
import com.dmv.texas.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DebugViewModel(application: Application) : AndroidViewModel(application) {

    data class DebugState(
        val questionCount: Int = 0,
        val topicCounts: Map<String, Int> = emptyMap(),
        val svgAssetCount: Int = 0,
        val questionsWithImages: Int = 0,
        val missingAssets: List<String> = emptyList(),
        val packVersion: Int = 0,
        val packInstalledAt: Long = 0,
        val isLoading: Boolean = true
    )

    private val _state = MutableStateFlow(DebugState())
    val state: StateFlow<DebugState> = _state

    init {
        loadDebugInfo()
    }

    private fun loadDebugInfo() {
        viewModelScope.launch {
            val db = DMVDatabase.getInstance(getApplication())
            val assetResolver = AssetResolver(getApplication())
            val stateCode = Constants.STATE_CODE

            val questionCount = db.questionDao().getCount(stateCode)
            val topicCounts = db.questionDao().getTopicCounts(stateCode)
                .associate { it.topic to it.count }
            val svgCount = assetResolver.getAssetCount()
            val questionsWithImages = db.questionDao().getImageQuestionCount(stateCode)

            // Find missing assets: imageAssetIds referenced by questions
            // but not present in the asset manifest
            val allManifestIds = assetResolver.getAllAssetIds()
            val referencedIds = db.questionDao().getAllImageAssetIds(stateCode)
            val missing = referencedIds.filter { it !in allManifestIds }

            val pack = db.statePackDao().getByStateCode(stateCode)

            _state.value = DebugState(
                questionCount = questionCount,
                topicCounts = topicCounts,
                svgAssetCount = svgCount,
                questionsWithImages = questionsWithImages,
                missingAssets = missing,
                packVersion = pack?.version ?: 0,
                packInstalledAt = pack?.installedAt ?: 0,
                isLoading = false
            )
        }
    }

    fun refresh() {
        _state.value = _state.value.copy(isLoading = true)
        loadDebugInfo()
    }
}
