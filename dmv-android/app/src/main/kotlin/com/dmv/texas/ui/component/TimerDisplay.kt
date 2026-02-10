package com.dmv.texas.ui.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dmv.texas.ui.theme.DMVTheme
import com.dmv.texas.ui.theme.IncorrectRed

@Composable
fun TimerDisplay(
    remainingMs: Long,
    modifier: Modifier = Modifier
) {
    val totalSeconds = (remainingMs / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val text = String.format("%02d:%02d", minutes, seconds)

    val color = if (remainingMs < 60_000) IncorrectRed else MaterialTheme.colorScheme.onSurface

    // Pulse animation when < 30 seconds remain
    val pulseAlpha = if (remainingMs < 30_000) {
        val transition = rememberInfiniteTransition(label = "timerPulse")
        val alpha by transition.animateFloat(
            initialValue = 1f,
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(500),
                repeatMode = RepeatMode.Reverse
            ),
            label = "timerAlpha"
        )
        alpha
    } else {
        1f
    }

    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        color = color,
        modifier = modifier.alpha(pulseAlpha)
    )
}

@Preview(showBackground = true)
@Composable
private fun TimerDisplayPreview() {
    DMVTheme(dynamicColor = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Normal time: 15 minutes remaining
            TimerDisplay(remainingMs = 15 * 60 * 1000L)
            // Warning time: 45 seconds remaining
            TimerDisplay(remainingMs = 45 * 1000L)
        }
    }
}
