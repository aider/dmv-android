package com.dmv.texas.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dmv.texas.ui.theme.CorrectGreen
import com.dmv.texas.ui.theme.DMVTheme
import com.dmv.texas.ui.theme.IncorrectRed

enum class AnswerState {
    DEFAULT,
    SELECTED,
    CORRECT,
    INCORRECT
}

@Composable
fun AnswerButton(
    text: String,
    index: Int,
    state: AnswerState,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val containerColor = when (state) {
        AnswerState.CORRECT -> CorrectGreen
        AnswerState.INCORRECT -> IncorrectRed
        AnswerState.SELECTED -> MaterialTheme.colorScheme.primaryContainer
        AnswerState.DEFAULT -> MaterialTheme.colorScheme.surface
    }
    val contentColor = when (state) {
        AnswerState.CORRECT, AnswerState.INCORRECT -> Color.White
        AnswerState.SELECTED -> MaterialTheme.colorScheme.onPrimaryContainer
        AnswerState.DEFAULT -> MaterialTheme.colorScheme.onSurface
    }

    val label = "${'A' + index}."

    OutlinedCard(
        onClick = onClick,
        enabled = enabled,
        colors = CardDefaults.outlinedCardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AnswerButtonPreview() {
    DMVTheme(dynamicColor = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnswerButton(
                text = "Default state answer",
                index = 0,
                state = AnswerState.DEFAULT,
                onClick = {}
            )
            AnswerButton(
                text = "Selected state answer",
                index = 1,
                state = AnswerState.SELECTED,
                onClick = {}
            )
            AnswerButton(
                text = "Correct state answer",
                index = 2,
                state = AnswerState.CORRECT,
                onClick = {}
            )
            AnswerButton(
                text = "Incorrect state answer",
                index = 3,
                state = AnswerState.INCORRECT,
                onClick = {}
            )
        }
    }
}
