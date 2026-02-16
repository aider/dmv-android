package com.dmv.texas.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dmv.texas.BuildConfig
import com.dmv.texas.analytics.AnalyticsEvents
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
    val analytics = (viewModel.getApplication() as com.dmv.texas.DMVApp).analytics
    var isCustomizeExpanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Zone A: Header ──────────────────────────────────────
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

        // ── Zone B: Quick Start ─────────────────────────────────
        Button(
            onClick = {
                analytics.logEvent(AnalyticsEvents.QUICK_START_TAPPED)
                onStartQuiz(viewModel.quickStartConfig())
            },
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Quick Start \u2022 20 Questions",
                style = MaterialTheme.typography.titleMedium
            )
        }

        // ── Zone C: Progress Card (returning users only) ────────
        val userState = state.userState
        if (userState is HomeViewModel.HomeUserState.Returning) {
            ProgressContextCard(
                lastScorePct = userState.lastScorePct,
                mistakeCount = userState.mistakeCount,
                weakestTopic = userState.weakestTopic,
                weakestTopicPct = userState.weakestTopicPct,
                onReviewMistakes = {
                    analytics.logEvent(AnalyticsEvents.REVIEW_MISTAKES_TAPPED, mapOf(
                        "mistake_count" to userState.mistakeCount
                    ))
                    onStartQuiz(viewModel.reviewMistakesConfig())
                },
                onDrillWeakTopic = { topic ->
                    analytics.logEvent(AnalyticsEvents.DRILL_WEAK_TOPIC_TAPPED, mapOf(
                        "topic" to topic
                    ))
                    onStartQuiz(viewModel.drillWeakTopicConfig(topic))
                }
            )
        }

        HorizontalDivider()

        // ── Zone D: Customize (collapsible) ─────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    isCustomizeExpanded = !isCustomizeExpanded
                    if (isCustomizeExpanded) {
                        analytics.logEvent(AnalyticsEvents.CUSTOMIZE_EXPANDED)
                    } else {
                        analytics.logEvent(AnalyticsEvents.CUSTOMIZE_COLLAPSED)
                    }
                }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Customize Quiz",
                style = MaterialTheme.typography.titleMedium
            )
            Icon(
                imageVector = if (isCustomizeExpanded) {
                    Icons.Default.KeyboardArrowUp
                } else {
                    Icons.Default.KeyboardArrowDown
                },
                contentDescription = if (isCustomizeExpanded) "Collapse" else "Expand",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AnimatedVisibility(
            visible = isCustomizeExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Mode selector
                Text(
                    text = "Mode",
                    style = MaterialTheme.typography.titleSmall
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
                            style = MaterialTheme.typography.titleSmall
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

                // Question count
                Text(
                    text = "Questions",
                    style = MaterialTheme.typography.titleSmall
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

                // Difficulty range
                Text(
                    text = "Difficulty",
                    style = MaterialTheme.typography.titleSmall
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

                // Start Custom Quiz button
                Button(
                    onClick = {
                        analytics.logEvent(AnalyticsEvents.CUSTOM_QUIZ_STARTED, mapOf(
                            "mode" to state.selectedMode.name,
                            "question_count" to state.questionCount,
                            "topic_count" to state.selectedTopics.size
                        ))
                        onStartQuiz(viewModel.buildConfig())
                    },
                    enabled = viewModel.canStart(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(
                        text = "Start Custom Quiz",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        HorizontalDivider()

        // ── Zone E: Secondary actions ───────────────────────────
        OutlinedButton(
            onClick = onOpenStats,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Stats")
        }

        // Debug button (debug builds only)
        if (BuildConfig.DEBUG) {
            TextButton(
                onClick = onOpenDebug,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Debug",
                    color = MaterialTheme.colorScheme.outline
                )
            }
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

        // Bottom padding
        Spacer(Modifier.height(8.dp))
    }
}

// ── Progress Context Card ────────────────────────────────────────

@Composable
private fun ProgressContextCard(
    lastScorePct: Int,
    mistakeCount: Int,
    weakestTopic: String?,
    weakestTopicPct: Int?,
    onReviewMistakes: () -> Unit,
    onDrillWeakTopic: (String) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Your Progress",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Last score: $lastScorePct%",
                style = MaterialTheme.typography.bodyLarge
            )

            if (mistakeCount > 0) {
                Text(
                    text = "$mistakeCount mistakes to review",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (weakestTopic != null && weakestTopicPct != null) {
                Text(
                    text = "Weakest: ${formatTopicDisplayName(weakestTopic)} ($weakestTopicPct%)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (mistakeCount > 0) {
                    OutlinedButton(
                        onClick = onReviewMistakes,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Review Mistakes", maxLines = 1)
                    }
                }
                if (weakestTopic != null) {
                    OutlinedButton(
                        onClick = { onDrillWeakTopic(weakestTopic) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Drill Weak Topic", maxLines = 1)
                    }
                }
            }
        }
    }
}

// ── Preview ──────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true, name = "Home - First Run")
@Composable
private fun HomeScreenFirstRunPreview() {
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
                text = "Pass your Texas permit test on the first try",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = "$totalQuestions questions \u2022 ${sampleTopics.size} topics \u2022 100% offline",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Quick Start \u2022 20 Questions",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Customize Quiz", style = MaterialTheme.typography.titleMedium)
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            HorizontalDivider()
            OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text("View Stats")
            }
            TextButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text(text = "About", color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Preview(showBackground = true, name = "Home - Returning User")
@Composable
private fun HomeScreenReturningPreview() {
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
                text = "Pass your Texas permit test on the first try",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = "660 questions \u2022 8 topics \u2022 100% offline",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Quick Start \u2022 20 Questions",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            // Progress Card preview
            ProgressContextCard(
                lastScorePct = 85,
                mistakeCount = 3,
                weakestTopic = "PARKING",
                weakestTopicPct = 62,
                onReviewMistakes = {},
                onDrillWeakTopic = {}
            )
            HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Customize Quiz", style = MaterialTheme.typography.titleMedium)
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            HorizontalDivider()
            OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text("View Stats")
            }
        }
    }
}
