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

data class AssetAuditState(
    val items: List<AssetAuditItem> = emptyList(),
    val filter: AssetFilter = AssetFilter.ALL,
    val isLoading: Boolean = true
)

data class AssetAuditItem(
    val assetId: String,
    val status: AssetStatus,
    val referencedByCount: Int
)

enum class AssetStatus { OK, MISSING, UNUSED }

enum class AssetFilter { ALL, OK, MISSING, UNUSED }

class AssetAuditViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(AssetAuditState())
    val state: StateFlow<AssetAuditState> = _state

    private var allItems: List<AssetAuditItem> = emptyList()

    init {
        loadAudit()
    }

    private fun loadAudit() {
        viewModelScope.launch {
            val db = DMVDatabase.getInstance(getApplication())
            val assetResolver = AssetResolver(getApplication())
            val stateCode = Constants.STATE_CODE

            val manifestIds = assetResolver.getAllAssetIds()
            val referenceCounts = db.questionDao().getAllImageAssetIdCounts(stateCode)
            val referenceMap = referenceCounts.associate { it.assetId to it.count }
            val referencedIds = referenceMap.keys

            val items = mutableListOf<AssetAuditItem>()

            // For each referenced assetId: OK if in manifest, MISSING if not
            for (refId in referencedIds) {
                val status = if (refId in manifestIds) AssetStatus.OK else AssetStatus.MISSING
                items.add(
                    AssetAuditItem(
                        assetId = refId,
                        status = status,
                        referencedByCount = referenceMap[refId] ?: 0
                    )
                )
            }

            // For each manifest assetId NOT referenced by any question -> UNUSED
            for (manifestId in manifestIds) {
                if (manifestId !in referencedIds) {
                    items.add(
                        AssetAuditItem(
                            assetId = manifestId,
                            status = AssetStatus.UNUSED,
                            referencedByCount = 0
                        )
                    )
                }
            }

            // Sort: MISSING first, then OK, then UNUSED; alphabetical within each group
            items.sortWith(compareBy({ it.status.ordinal }, { it.assetId }))

            allItems = items.toList()
            _state.value = AssetAuditState(
                items = applyFilter(allItems, _state.value.filter),
                filter = _state.value.filter,
                isLoading = false
            )
        }
    }

    fun setFilter(filter: AssetFilter) {
        _state.value = _state.value.copy(
            filter = filter,
            items = applyFilter(allItems, filter)
        )
    }

    private fun applyFilter(items: List<AssetAuditItem>, filter: AssetFilter): List<AssetAuditItem> {
        return when (filter) {
            AssetFilter.ALL -> items
            AssetFilter.OK -> items.filter { it.status == AssetStatus.OK }
            AssetFilter.MISSING -> items.filter { it.status == AssetStatus.MISSING }
            AssetFilter.UNUSED -> items.filter { it.status == AssetStatus.UNUSED }
        }
    }

    /** Summary counts for display above the list. */
    fun countByStatus(status: AssetStatus): Int = allItems.count { it.status == status }
}
