package com.dmv.texas.ui.screen.import_

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ImportScreen(
    onImportComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ImportViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startImport()
    }

    // Auto-navigate when import finishes successfully
    LaunchedEffect(state) {
        if (state is ImportViewModel.ImportState.Done) {
            onImportComplete()
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val s = state) {
                is ImportViewModel.ImportState.Idle -> {
                    Text(
                        text = "Preparing...",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                is ImportViewModel.ImportState.Importing -> {
                    Text(
                        text = s.message,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    if (s.total > 0) {
                        LinearProgressIndicator(
                            progress = { s.current.toFloat() / s.total },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 48.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${s.current} / ${s.total}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 48.dp)
                        )
                    }
                }
                is ImportViewModel.ImportState.Done -> {
                    if (s.alreadyUpToDate) {
                        Text(
                            text = "Questions up to date!",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    } else {
                        Text(
                            text = "${s.totalImported} questions imported!",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
                is ImportViewModel.ImportState.Error -> {
                    Text(
                        text = "Import failed",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = s.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    Button(onClick = { viewModel.startImport() }) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}
