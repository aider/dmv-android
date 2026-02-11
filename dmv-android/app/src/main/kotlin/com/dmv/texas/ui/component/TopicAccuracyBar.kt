package com.dmv.texas.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dmv.texas.ui.theme.CorrectGreen
import com.dmv.texas.ui.theme.DMVTheme
import com.dmv.texas.ui.theme.IncorrectRed
import com.dmv.texas.ui.theme.WarningAmber
import com.dmv.texas.ui.util.formatTopicDisplayName

/**
 * Displays a topic name with a horizontal progress bar showing accuracy percentage.
 * Colors the bar green (>= 70%), amber (>= 50%), or red (< 50%) based on performance.
 */
@Composable
fun TopicAccuracyBar(
    topic: String,
    correct: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val percentage = if (total > 0) correct.toFloat() / total else 0f
    val percentInt = (percentage * 100).toInt()

    val barColor = when {
        percentage >= 0.70f -> CorrectGreen
        percentage >= 0.50f -> WarningAmber
        else -> IncorrectRed
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTopicDisplayName(topic),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$percentInt% ($correct/$total)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun TopicAccuracyBarPreview() {
    DMVTheme(dynamicColor = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TopicAccuracyBar(topic = "road_signs", correct = 17, total = 20)
            TopicAccuracyBar(topic = "right_of_way", correct = 11, total = 20)
            TopicAccuracyBar(topic = "parking_rules", correct = 6, total = 20)
        }
    }
}
