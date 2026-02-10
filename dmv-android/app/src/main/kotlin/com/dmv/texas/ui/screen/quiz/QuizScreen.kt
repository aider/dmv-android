package com.dmv.texas.ui.screen.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dmv.texas.data.model.QuizMode
import com.dmv.texas.ui.component.AnswerButton
import com.dmv.texas.ui.component.AnswerState
import com.dmv.texas.ui.component.QuestionImage
import com.dmv.texas.ui.component.TimerDisplay
import com.dmv.texas.ui.theme.CorrectGreen
import com.dmv.texas.ui.theme.IncorrectRed

@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    onFinished: () -> Unit,
    onQuit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    var showQuitDialog by remember { mutableStateOf(false) }

    // Navigate when quiz finishes and attempt is saved.
    // Data stays in the shared QuizViewModel for ResultsScreen to read.
    LaunchedEffect(state.isFinished, state.lastAttemptId) {
        if (state.isFinished && state.lastAttemptId != null) {
            onFinished()
        }
    }

    // Quit confirmation dialog
    if (showQuitDialog) {
        AlertDialog(
            onDismissRequest = { showQuitDialog = false },
            title = { Text("Quit Quiz?") },
            text = { Text("Your progress will be lost.") },
            confirmButton = {
                TextButton(onClick = {
                    showQuitDialog = false
                    onQuit()
                }) {
                    Text("Quit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuitDialog = false }) {
                    Text("Continue")
                }
            }
        )
    }

    if (state.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Loading questions...")
            }
        }
        return
    }

    if (state.questions.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "No questions found",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Try adjusting your topic or difficulty filters.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = onQuit) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    val question = state.currentQuestion ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        // Top bar: progress + timer
        QuizTopBar(
            currentIndex = state.currentIndex,
            totalQuestions = state.questions.size,
            progress = state.progress,
            timerMs = state.timerMs,
            isExamMode = state.config.mode == QuizMode.EXAM,
            onQuit = { showQuitDialog = true }
        )

        // Scrollable question + answers area
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Topic badge
            Text(
                text = question.topic,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            // Question text
            Text(
                text = question.text,
                style = MaterialTheme.typography.titleLarge
            )

            // SVG image (if present)
            if (question.imageAssetId != null) {
                QuestionImage(
                    assetId = question.imageAssetId!!,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(Modifier.height(4.dp))

            // Answer choices
            val selectedAnswer = state.answers[question.id]
            val isPracticeWithFeedback = state.config.mode == QuizMode.PRACTICE && state.showFeedback

            question.choices.forEachIndexed { index, choice ->
                val answerState = when {
                    isPracticeWithFeedback && index == question.correctIndex -> AnswerState.CORRECT
                    isPracticeWithFeedback && index == selectedAnswer && index != question.correctIndex -> AnswerState.INCORRECT
                    !isPracticeWithFeedback && index == selectedAnswer -> AnswerState.SELECTED
                    else -> AnswerState.DEFAULT
                }

                AnswerButton(
                    text = choice,
                    index = index,
                    state = answerState,
                    onClick = { viewModel.selectAnswer(question.id, index) },
                    enabled = !isPracticeWithFeedback && !state.isFinished
                )
            }

            // Explanation card (Practice mode only, after answering)
            if (isPracticeWithFeedback && selectedAnswer != null) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically()
                ) {
                    ExplanationCard(
                        isCorrect = selectedAnswer == question.correctIndex,
                        explanation = question.explanation,
                        reference = question.reference
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        // Bottom navigation buttons
        QuizBottomBar(
            currentIndex = state.currentIndex,
            totalQuestions = state.questions.size,
            hasAnswered = state.answers.containsKey(question.id),
            showFeedback = state.showFeedback,
            isPracticeMode = state.config.mode == QuizMode.PRACTICE,
            isFinished = state.isFinished,
            onPrevious = { viewModel.previousQuestion() },
            onNext = { viewModel.nextQuestion() },
            onFinish = { viewModel.finishQuiz() }
        )

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun QuizTopBar(
    currentIndex: Int,
    totalQuestions: Int,
    progress: Float,
    timerMs: Long,
    isExamMode: Boolean,
    onQuit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onQuit) {
                Text("Quit")
            }

            Text(
                text = "${currentIndex + 1} / $totalQuestions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (isExamMode) {
                TimerDisplay(remainingMs = timerMs)
            } else {
                // Spacer to balance the layout
                Spacer(Modifier)
            }
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        )
    }
}

@Composable
private fun ExplanationCard(
    isCorrect: Boolean,
    explanation: String,
    reference: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCorrect) {
                CorrectGreen.copy(alpha = 0.1f)
            } else {
                IncorrectRed.copy(alpha = 0.1f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (isCorrect) "Correct!" else "Incorrect",
                style = MaterialTheme.typography.titleLarge,
                color = if (isCorrect) CorrectGreen else IncorrectRed,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyLarge
            )
            if (reference.isNotBlank()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = reference,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QuizBottomBar(
    currentIndex: Int,
    totalQuestions: Int,
    hasAnswered: Boolean,
    showFeedback: Boolean,
    isPracticeMode: Boolean,
    isFinished: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit
) {
    val isLastQuestion = currentIndex == totalQuestions - 1

    HorizontalDivider()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous button (not on first question)
        if (currentIndex > 0 && !isPracticeMode) {
            OutlinedButton(onClick = onPrevious) {
                Text("Previous")
            }
        } else {
            Spacer(Modifier)
        }

        if (isLastQuestion) {
            Button(
                onClick = onFinish,
                enabled = hasAnswered && !isFinished
            ) {
                Text("Finish")
            }
        } else {
            Button(
                onClick = onNext,
                enabled = if (isPracticeMode) showFeedback else hasAnswered
            ) {
                Text("Next")
            }
        }
    }
}
