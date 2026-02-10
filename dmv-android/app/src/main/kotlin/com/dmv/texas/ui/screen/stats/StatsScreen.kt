package com.dmv.texas.ui.screen.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dmv.texas.data.local.entity.AttemptEntity
import com.dmv.texas.ui.component.TopicAccuracyBar
import com.dmv.texas.ui.theme.CorrectGreen
import com.dmv.texas.ui.theme.IncorrectRed
import com.dmv.texas.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Stats screen showing overall accuracy, per-topic breakdown, recent attempts,
 * and a "Practice Mistakes" shortcut.
 */
@Composable
fun StatsScreen(
    onBackToHome: () -> Unit,
    onPracticeMistakes: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = viewModel()
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
                Text("Loading stats...")
            }
        }
        return
    }

    // Empty state
    if (state.overallTotal == 0 && state.recentAttempts.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "No Stats Yet",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Complete a quiz to see your stats here.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                OutlinedButton(onClick = onBackToHome) {
                    Text("Back to Home")
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Your Stats",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        // Overall accuracy
        item {
            OverallAccuracySection(
                percentage = state.overallPercentage,
                correct = state.overallCorrect,
                total = state.overallTotal
            )
        }

        // Practice mistakes button
        if (state.mistakeCount > 0) {
            item {
                Button(
                    onClick = onPracticeMistakes,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Practice ${state.mistakeCount} Mistake Questions")
                }
            }
        }

        // Topic accuracy breakdown
        if (state.topicAccuracies.isNotEmpty()) {
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = "Accuracy by Topic",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            items(state.topicAccuracies, key = { it.topic }) { topicAccuracy ->
                TopicAccuracyBar(
                    topic = topicAccuracy.topic,
                    correct = topicAccuracy.totalCorrect,
                    total = topicAccuracy.totalSeen
                )
            }
        }

        // Recent attempts
        if (state.recentAttempts.isNotEmpty()) {
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = "Recent Attempts",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            items(state.recentAttempts, key = { it.id }) { attempt ->
                AttemptCard(attempt)
            }
        }

        // Back button
        item {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onBackToHome,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Home")
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun OverallAccuracySection(
    percentage: Int,
    correct: Int,
    total: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Overall Accuracy",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // Circular progress indicator as a visual element
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                CircularProgressIndicator(
                    progress = { percentage / 100f },
                    modifier = Modifier.size(120.dp),
                    strokeWidth = 10.dp,
                    color = when {
                        percentage >= 70 -> CorrectGreen
                        percentage >= 50 -> WarningAmber
                        else -> IncorrectRed
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                text = "$correct / $total questions answered correctly",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun AttemptCard(attempt: AttemptEntity) {
    val percentage = if (attempt.total > 0) (attempt.correct * 100) / attempt.total else 0
    val passed = percentage >= 70
    val dateText = remember(attempt.createdAt) { formatDate(attempt.createdAt) }
    val durationText = remember(attempt.durationMs) { formatDuration(attempt.durationMs) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = attempt.mode,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = durationText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (passed) CorrectGreen else IncorrectRed
                )
                Text(
                    text = "${attempt.correct}/${attempt.total}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy  h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = (ms / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return when {
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}
