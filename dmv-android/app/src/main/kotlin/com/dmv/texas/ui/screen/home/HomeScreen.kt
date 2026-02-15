package com.dmv.texas.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dmv.texas.data.local.dao.TopicCount
import com.dmv.texas.data.model.QuizConfig
import com.dmv.texas.data.model.QuizMode
import com.dmv.texas.ui.theme.DMVTheme
import com.dmv.texas.ui.util.formatTopicDisplayName

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    onStartQuiz: (QuizConfig) -> Unit,
    onOpenStats: () -> Unit,
    onOpenDebug: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title + value proposition
            Text(
                text = "TX DMV Practice",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Pass your Texas permit test on the first try",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Text(
                text = "${state.totalQuestions} questions \u2022 ${state.topics.size} topics \u2022 100% offline",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            // Quick Start button
            Button(
                onClick = { onStartQuiz(viewModel.quickStartConfig()) },
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Quick Start \u2022 20 Questions",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            HorizontalDivider()

            // Mode selector
            Text(
                text = "Mode",
                style = MaterialTheme.typography.titleLarge
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuizMode.entries.forEach { mode ->
                    val label = when (mode) {
                        QuizMode.MISTAKES -> "${mode.displayName} (${state.mistakeCount})"
                        else -> mode.displayName
                    }
                    FilterChip(
                        selected = state.selectedMode == mode,
                        onClick = { viewModel.setMode(mode) },
                        label = { Text(label) },
                        enabled = mode != QuizMode.MISTAKES || state.mistakeCount > 0
                    )
                }
            }

            // Topic selection (hidden for MISTAKES mode)
            if (state.selectedMode != QuizMode.MISTAKES) {
                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Topics",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Row {
                        TextButton(onClick = { viewModel.selectAllTopics() }) {
                            Text("All")
                        }
                        TextButton(onClick = { viewModel.clearAllTopics() }) {
                            Text("None")
                        }
                    }
                }

                state.topics.forEach { topicCount ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = state.selectedTopics.contains(topicCount.topic),
                            onCheckedChange = { viewModel.toggleTopic(topicCount.topic) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = formatTopicDisplayName(topicCount.topic),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${topicCount.count}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider()

            // Question count selector
            Text(
                text = "Questions",
                style = MaterialTheme.typography.titleLarge
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(10, 20, 30, 50).forEach { count ->
                    FilterChip(
                        selected = state.questionCount == count,
                        onClick = { viewModel.setQuestionCount(count) },
                        label = { Text("$count") }
                    )
                }
            }

            HorizontalDivider()

            // Difficulty range slider
            Text(
                text = "Difficulty",
                style = MaterialTheme.typography.titleLarge
            )

            val difficultyLabels = mapOf(1 to "Easy", 2 to "Medium", 3 to "Hard")

            Text(
                text = "${difficultyLabels[state.minDifficulty] ?: state.minDifficulty}" +
                    " - ${difficultyLabels[state.maxDifficulty] ?: state.maxDifficulty}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            RangeSlider(
                value = state.minDifficulty.toFloat()..state.maxDifficulty.toFloat(),
                onValueChange = { range ->
                    viewModel.setDifficultyRange(
                        range.start.toInt(),
                        range.endInclusive.toInt()
                    )
                },
                valueRange = 1f..3f,
                steps = 1
            )

            // Stats button
            OutlinedButton(
                onClick = onOpenStats,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Stats")
            }

            // Debug button
            TextButton(
                onClick = onOpenDebug,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Debug",
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // About button
            TextButton(
                onClick = onOpenAbout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "About",
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Bottom padding for scrollable content
            Spacer(Modifier.height(8.dp))
        }

        // Sticky bottom bar with Start Quiz CTA
        Surface(
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = { onStartQuiz(viewModel.buildConfig()) },
                    enabled = viewModel.canStart(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "Start Quiz",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    val sampleTopics = listOf(
        TopicCount("ROAD_SIGNS", 25),
        TopicCount("RIGHT_OF_WAY", 20),
        TopicCount("PARKING", 15),
        TopicCount("SPEED_LIMITS", 18),
        TopicCount("SAFETY", 12)
    )
    val totalQuestions = sampleTopics.sumOf { it.count }

    DMVTheme(dynamicColor = false) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "TX DMV Practice",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = "$totalQuestions questions available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            HorizontalDivider()
            Text(text = "Mode", style = MaterialTheme.typography.titleLarge)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = true, onClick = {}, label = { Text("Practice") })
                FilterChip(selected = false, onClick = {}, label = { Text("Exam") })
                FilterChip(selected = false, onClick = {}, label = { Text("Topic Drill") })
                FilterChip(selected = false, onClick = {}, enabled = false, label = { Text("Mistakes (0)") })
            }
            HorizontalDivider()
            Text(text = "Topics", style = MaterialTheme.typography.titleLarge)
            sampleTopics.forEach { topicCount ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = true, onCheckedChange = {})
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = formatTopicDisplayName(topicCount.topic),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${topicCount.count}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            HorizontalDivider()
            Text(text = "Questions", style = MaterialTheme.typography.titleLarge)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(10, 20, 30, 50).forEach { count ->
                    FilterChip(
                        selected = count == 20,
                        onClick = {},
                        label = { Text("$count") }
                    )
                }
            }
            HorizontalDivider()
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(text = "Start Quiz", style = MaterialTheme.typography.titleLarge)
            }
            OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text("View Stats")
            }
            TextButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Debug", color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}
