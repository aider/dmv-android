package com.dmv.texas.ui.screen.debug

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
import com.dmv.texas.ui.theme.CorrectGreen
import com.dmv.texas.ui.theme.IncorrectRed
import com.dmv.texas.util.Constants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Diagnostic dashboard showing pack info, question/asset counts,
 * and missing asset detection. Debug-only screen accessible from Home.
 */
@Composable
fun DebugScreen(
    onBackToHome: () -> Unit,
    onOpenAssetAudit: () -> Unit = {},
    onOpenDataQuality: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: DebugViewModel = viewModel()
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
                Text("Loading debug info...")
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
            text = "Debug Dashboard",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        // Pack info card
        PackInfoCard(
            stateCode = Constants.STATE_CODE,
            version = state.packVersion,
            installedAt = state.packInstalledAt
        )

        // Question counts card
        QuestionCountsCard(
            totalCount = state.questionCount,
            topicCounts = state.topicCounts
        )

        // Asset info card
        AssetInfoCard(
            svgAssetCount = state.svgAssetCount,
            questionsWithImages = state.questionsWithImages,
            missingAssets = state.missingAssets
        )

        // Actions
        OutlinedButton(
            onClick = { viewModel.refresh() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Refresh")
        }

        OutlinedButton(
            onClick = onOpenAssetAudit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Asset Audit")
        }

        OutlinedButton(
            onClick = onOpenDataQuality,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Data Quality Report")
        }

        OutlinedButton(
            onClick = onBackToHome,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Home")
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun PackInfoCard(
    stateCode: String,
    version: Int,
    installedAt: Long
) {
    val dateText = remember(installedAt) {
        if (installedAt > 0) {
            val sdf = SimpleDateFormat("MMM d, yyyy  h:mm a", Locale.getDefault())
            sdf.format(Date(installedAt))
        } else {
            "Not installed"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Pack Info",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            InfoRow("State Code", stateCode)
            InfoRow("Version", version.toString())
            InfoRow("Installed", dateText)
        }
    }
}

@Composable
private fun QuestionCountsCard(
    totalCount: Int,
    topicCounts: Map<String, Int>
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Questions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            InfoRow("Total", totalCount.toString())

            if (topicCounts.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = "By Topic:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                topicCounts.entries.sortedBy { it.key }.forEach { (topic, count) ->
                    InfoRow(topic, count.toString())
                }
            }
        }
    }
}

@Composable
private fun AssetInfoCard(
    svgAssetCount: Int,
    questionsWithImages: Int,
    missingAssets: List<String>
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Assets",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            InfoRow("SVGs in manifest", svgAssetCount.toString())
            InfoRow("Questions with images", questionsWithImages.toString())

            if (missingAssets.isEmpty()) {
                Text(
                    text = "No missing assets",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CorrectGreen,
                    fontWeight = FontWeight.Medium
                )
            } else {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = "Missing assets (${missingAssets.size}):",
                    style = MaterialTheme.typography.titleSmall,
                    color = IncorrectRed,
                    fontWeight = FontWeight.Bold
                )
                missingAssets.forEach { assetId ->
                    Text(
                        text = assetId,
                        style = MaterialTheme.typography.bodySmall,
                        color = IncorrectRed
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
