package com.dmv.texas.ui.screen.debug

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dmv.texas.ui.theme.CorrectGreen
import com.dmv.texas.ui.theme.IncorrectRed

@Composable
fun DataQualityScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DataQualityViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    if (state.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Running data quality checks...")
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Data Quality Report",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        // Summary card
        SummaryCard(state)

        // Expandable sections for each category
        ExpandableSection(
            title = "Duplicate Questions",
            count = state.duplicates.size,
            emptyMessage = "No duplicates found"
        ) {
            state.duplicates.forEach { group ->
                Text(
                    text = "\"${group.normalizedText}\"",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                group.questionIds.forEach { id ->
                    Text(
                        text = "  - $id",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        ExpandableSection(
            title = "Invalid correctIndex",
            count = state.invalidCorrectIndex.size,
            emptyMessage = "All correctIndex values are valid"
        ) {
            state.invalidCorrectIndex.forEach { id ->
                Text(
                    text = id,
                    style = MaterialTheme.typography.bodySmall,
                    color = IncorrectRed
                )
            }
        }

        ExpandableSection(
            title = "Unknown Topics",
            count = state.unknownTopics.size,
            emptyMessage = "All topics are recognized"
        ) {
            state.unknownTopics.forEach { (id, topic) ->
                Text(
                    text = "$id -> $topic",
                    style = MaterialTheme.typography.bodySmall,
                    color = IncorrectRed
                )
            }
        }

        ExpandableSection(
            title = "Broken Image References",
            count = state.brokenImages.size,
            emptyMessage = "All image references resolve"
        ) {
            state.brokenImages.forEach { assetId ->
                Text(
                    text = assetId,
                    style = MaterialTheme.typography.bodySmall,
                    color = IncorrectRed
                )
            }
        }

        ExpandableSection(
            title = "Empty / Blank Fields",
            count = state.emptyFields.size,
            emptyMessage = "No empty fields found"
        ) {
            state.emptyFields.forEach { (id, field) ->
                Text(
                    text = "$id -> $field",
                    style = MaterialTheme.typography.bodySmall,
                    color = IncorrectRed
                )
            }
        }

        // Actions
        OutlinedButton(
            onClick = { viewModel.refresh() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Refresh")
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SummaryCard(state: DataQualityState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            SummaryRow("Duplicate groups", state.duplicates.size)
            SummaryRow("Invalid correctIndex", state.invalidCorrectIndex.size)
            SummaryRow("Unknown topics", state.unknownTopics.size)
            SummaryRow("Broken image refs", state.brokenImages.size)
            SummaryRow("Empty fields", state.emptyFields.size)
        }
    }
}

@Composable
private fun SummaryRow(label: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (count == 0) CorrectGreen else IncorrectRed
        )
    }
}

@Composable
private fun ExpandableSection(
    title: String,
    count: Int,
    emptyMessage: String,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$title ($count)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (count == 0) CorrectGreen else IncorrectRed
                )
                Icon(
                    imageVector = if (expanded) {
                        Icons.Default.KeyboardArrowUp
                    } else {
                        Icons.Default.KeyboardArrowDown
                    },
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (count == 0) {
                        Text(
                            text = emptyMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CorrectGreen
                        )
                    } else {
                        content()
                    }
                }
            }
        }
    }
}
