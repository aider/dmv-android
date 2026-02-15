package com.dmv.texas.ui.screen.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dmv.texas.data.local.entity.QuestionEntity
import com.dmv.texas.data.model.QuizConfig
import com.dmv.texas.ui.component.MissedQuestionCard
import com.dmv.texas.ui.component.TopicAccuracyBar
import com.dmv.texas.ui.theme.CorrectGreen
import com.dmv.texas.ui.theme.DMVTheme
import com.dmv.texas.ui.theme.IncorrectRed

/**
 * Results screen shown after a quiz finishes.
 * Displays score, pass/fail, duration, topic accuracy breakdown,
 * missed questions (expandable), and action buttons.
 */
@Composable
fun ResultsScreen(
    questions: List<QuestionEntity>,
    answers: Map<String, Int>,
    config: QuizConfig,
    durationMs: Long,
    onBackToHome: () -> Unit,
    onRetryQuiz: () -> Unit,
    onPracticeMistakes: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val totalCount = questions.size
    val correctCount = remember(questions, answers) {
        answers.count { (qId, selected) ->
            questions.find { it.id == qId }?.correctIndex == selected
        }
    }
    val percentage = if (totalCount > 0) (correctCount * 100) / totalCount else 0
    val passed = percentage >= 70

    // Per-topic accuracy computed from this quiz's answers
    val topicAccuracies = remember(questions, answers) {
        questions.groupBy { it.topic }.map { (topic, topicQuestions) ->
            val topicCorrect = topicQuestions.count { q ->
                answers[q.id] == q.correctIndex
            }
            TopicResult(topic, topicCorrect, topicQuestions.size)
        }.sortedBy { it.topic }
    }

    // Missed questions: where user answered incorrectly or did not answer
    val missedQuestions = remember(questions, answers) {
        questions.filter { q ->
            val selected = answers[q.id]
            selected == null || selected != q.correctIndex
        }.map { q ->
            MissedQuestionInfo(q, answers[q.id] ?: -1)
        }
    }

    // Format duration
    val durationText = remember(durationMs) { formatDuration(durationMs) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Score header
        item {
            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Quiz Results",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                // Large percentage
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (passed) CorrectGreen else IncorrectRed
                )

                // Fraction
                Text(
                    text = "$correctCount / $totalCount correct",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Pass/fail indicator
                Text(
                    text = if (passed) "PASSED" else "FAILED",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (passed) CorrectGreen else IncorrectRed
                )

                // Duration
                Text(
                    text = "Time: $durationText",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Mode info
                Text(
                    text = "Mode: ${config.mode.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Encouragement message based on score band
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when {
                        percentage >= 90 -> "Outstanding! You really know your stuff."
                        percentage >= 70 -> "Great job! You're on track to pass."
                        percentage >= 50 -> "Good effort! A little more practice and you'll pass."
                        else -> "Keep practicing — every attempt builds knowledge."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Topic accuracy breakdown
        if (topicAccuracies.isNotEmpty()) {
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = "Topic Breakdown",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            items(topicAccuracies, key = { it.topic }) { topicResult ->
                TopicAccuracyBar(
                    topic = topicResult.topic,
                    correct = topicResult.correct,
                    total = topicResult.total
                )
            }
        }

        // Missed questions section
        if (missedQuestions.isNotEmpty()) {
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = "Missed Questions (${missedQuestions.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            items(missedQuestions, key = { it.question.id }) { missed ->
                MissedQuestionCard(
                    question = missed.question,
                    selectedIndex = missed.selectedIndex
                )
            }
        }

        // Action buttons
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Targeted CTA: practice mistakes when there are wrong answers
                if (missedQuestions.isNotEmpty() && onPracticeMistakes != null) {
                    Button(
                        onClick = onPracticeMistakes,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Practice ${missedQuestions.size} Missed Questions")
                    }
                }

                OutlinedButton(
                    onClick = onRetryQuiz,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Retry Quiz")
                }

                OutlinedButton(
                    onClick = onBackToHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Back to Home")
                }
            }

            // Bottom padding for edge-to-edge
            Spacer(Modifier.height(32.dp))
        }
    }
}

private data class TopicResult(
    val topic: String,
    val correct: Int,
    val total: Int
)

private data class MissedQuestionInfo(
    val question: QuestionEntity,
    val selectedIndex: Int
)

/**
 * Formats a duration in milliseconds to a human-readable string like "2m 30s".
 */
private fun formatDuration(ms: Long): String {
    val totalSeconds = (ms / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return when {
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultsScreenPreview() {
    val sampleQuestions = listOf(
        QuestionEntity(
            id = "TX-001", stateCode = "TX", packVersion = 1,
            topic = "ROAD_SIGNS", difficulty = 1,
            text = "What does a red octagonal sign mean?",
            choices = listOf("Stop", "Yield", "Caution", "Speed Limit"),
            correctIndex = 0,
            explanation = "A red octagonal sign is a STOP sign.",
            reference = "TX Driver Handbook, Ch. 5",
            imageAssetId = null
        ),
        QuestionEntity(
            id = "TX-002", stateCode = "TX", packVersion = 1,
            topic = "ROAD_SIGNS", difficulty = 1,
            text = "What shape is a yield sign?",
            choices = listOf("Triangle", "Circle", "Square", "Diamond"),
            correctIndex = 0,
            explanation = "A yield sign is an inverted triangle.",
            reference = "TX Driver Handbook, Ch. 5",
            imageAssetId = null
        ),
        QuestionEntity(
            id = "TX-003", stateCode = "TX", packVersion = 1,
            topic = "RIGHT_OF_WAY", difficulty = 2,
            text = "Who has the right of way at a 4-way stop?",
            choices = listOf("First to arrive", "Largest vehicle", "Turning left", "Turning right"),
            correctIndex = 0,
            explanation = "The first vehicle to arrive has the right of way.",
            reference = "TX Driver Handbook, Ch. 7",
            imageAssetId = null
        )
    )
    // TX-001 correct, TX-002 wrong (picked index 2), TX-003 correct
    val answers = mapOf("TX-001" to 0, "TX-002" to 2, "TX-003" to 0)

    DMVTheme(dynamicColor = false) {
        ResultsScreen(
            questions = sampleQuestions,
            answers = answers,
            config = QuizConfig(),
            durationMs = 120_000L,
            onBackToHome = {},
            onRetryQuiz = {}
        )
    }
}
