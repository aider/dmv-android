package com.dmv.texas.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dmv.texas.data.local.entity.QuestionEntity
import com.dmv.texas.ui.theme.CorrectGreen
import com.dmv.texas.ui.theme.DMVTheme
import com.dmv.texas.ui.theme.IncorrectRed

/**
 * An expandable card that shows a missed question.
 * Collapsed: shows truncated question text.
 * Expanded: shows full question, optional image, user's wrong answer (red),
 * correct answer (green), and explanation.
 */
@Composable
fun MissedQuestionCard(
    question: QuestionEntity,
    selectedIndex: Int,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = IncorrectRed.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: question text (truncated when collapsed)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = question.text,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (expanded) "^" else "v",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Expanded content
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // SVG image if present
                    if (question.imageAssetId != null) {
                        QuestionImage(
                            assetId = question.imageAssetId!!,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    HorizontalDivider()

                    // User's wrong answer (red, with strikethrough)
                    Text(
                        text = "Your answer:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${'A' + selectedIndex}. ${question.choices.getOrElse(selectedIndex) { "?" }}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = IncorrectRed,
                        fontWeight = FontWeight.Medium,
                        textDecoration = TextDecoration.LineThrough
                    )

                    Spacer(Modifier.height(4.dp))

                    // Correct answer (green)
                    Text(
                        text = "Correct answer:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${'A' + question.correctIndex}. ${question.choices.getOrElse(question.correctIndex) { "?" }}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = CorrectGreen,
                        fontWeight = FontWeight.Bold
                    )

                    // Explanation
                    if (question.explanation.isNotBlank()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            text = question.explanation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Reference
                    if (question.reference.isNotBlank()) {
                        Text(
                            text = question.reference,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MissedQuestionCardPreview() {
    DMVTheme(dynamicColor = false) {
        Column(modifier = Modifier.padding(16.dp)) {
            MissedQuestionCard(
                question = QuestionEntity(
                    id = "TX-001",
                    stateCode = "TX",
                    packVersion = 1,
                    topic = "ROAD_SIGNS",
                    difficulty = 1,
                    text = "What does a red octagonal sign mean?",
                    choices = listOf("Stop", "Yield", "Caution", "Speed Limit"),
                    correctIndex = 0,
                    explanation = "A red octagonal sign is always a STOP sign. You must come to a complete stop at the stop line or crosswalk.",
                    reference = "TX Driver Handbook, Ch. 5",
                    imageAssetId = null
                ),
                selectedIndex = 1
            )
        }
    }
}
