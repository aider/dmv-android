package com.dmv.texas.ui.screen.import_

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dmv.texas.data.local.db.DMVDatabase
import com.dmv.texas.import_.PackImporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ImportViewModel(application: Application) : AndroidViewModel(application) {

    sealed class ImportState {
        data object Idle : ImportState()
        data class Importing(val message: String) : ImportState()
        data class Done(val totalImported: Int, val alreadyUpToDate: Boolean) : ImportState()
        data class Error(val message: String) : ImportState()
    }

    private val _state = MutableStateFlow<ImportState>(ImportState.Idle)
    val state: StateFlow<ImportState> = _state

    fun startImport() {
        if (_state.value is ImportState.Importing) return

        viewModelScope.launch {
            try {
                _state.value = ImportState.Importing("Checking for packs...")
                val db = DMVDatabase.getInstance(getApplication())
                val importer = PackImporter(getApplication(), db)
                val results = importer.importAllPacks { message ->
                    _state.value = ImportState.Importing(message)
                }
                val totalImported = results.sumOf { it.questionsImported }
                val allUpToDate = results.all { it.alreadyUpToDate }
                _state.value = ImportState.Done(totalImported, allUpToDate)
            } catch (e: Exception) {
                _state.value = ImportState.Error(e.message ?: "Unknown error during import")
            }
        }
    }
}
