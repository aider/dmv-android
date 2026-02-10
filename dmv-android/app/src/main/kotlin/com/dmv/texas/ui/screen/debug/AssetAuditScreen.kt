package com.dmv.texas.ui.screen.debug

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.dmv.texas.ui.theme.CorrectGreen
import com.dmv.texas.ui.theme.IncorrectRed
import com.dmv.texas.util.AssetResolver

/**
 * Full-screen asset audit showing every SVG in the manifest and every asset
 * referenced by questions, with status indicators (OK / MISSING / UNUSED)
 * and filter chips to narrow the view.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AssetAuditScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AssetAuditViewModel = viewModel()
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
                Text("Auditing assets...")
            }
        }
        return
    }

    val okCount = viewModel.countByStatus(AssetStatus.OK)
    val missingCount = viewModel.countByStatus(AssetStatus.MISSING)
    val unusedCount = viewModel.countByStatus(AssetStatus.UNUSED)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // Header
        Text(
            text = "Asset Audit",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        // Summary
        Text(
            text = "$okCount OK, $missingCount Missing, $unusedCount Unused",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        // Filter chips
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            AssetFilter.entries.forEach { filter ->
                val label = when (filter) {
                    AssetFilter.ALL -> "All (${okCount + missingCount + unusedCount})"
                    AssetFilter.OK -> "OK ($okCount)"
                    AssetFilter.MISSING -> "Missing ($missingCount)"
                    AssetFilter.UNUSED -> "Unused ($unusedCount)"
                }
                FilterChip(
                    selected = state.filter == filter,
                    onClick = { viewModel.setFilter(filter) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when (filter) {
                            AssetFilter.ALL -> MaterialTheme.colorScheme.primaryContainer
                            AssetFilter.OK -> CorrectGreen.copy(alpha = 0.15f)
                            AssetFilter.MISSING -> IncorrectRed.copy(alpha = 0.15f)
                            AssetFilter.UNUSED -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Item list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.items, key = { it.assetId }) { item ->
                AssetAuditItemRow(item)
            }

            if (state.items.isEmpty()) {
                item {
                    Text(
                        text = "No assets match this filter.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("Back")
        }
    }
}

@Composable
private fun AssetAuditItemRow(item: AssetAuditItem) {
    val context = LocalContext.current
    val assetResolver = remember { AssetResolver(context) }
    val uri = remember(item.assetId) { assetResolver.getAssetUri(item.assetId) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // SVG preview
            if (uri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(uri)
                        .build(),
                    contentDescription = item.assetId,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(48.dp)
                )
            } else {
                // Placeholder for missing assets
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "?",
                        style = MaterialTheme.typography.headlineSmall,
                        color = IncorrectRed
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Asset ID and reference count
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.assetId,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.referencedByCount > 0) {
                    Text(
                        text = "Referenced by ${item.referencedByCount} question${if (item.referencedByCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Status chip
            StatusChip(item.status)

            // Copy button
            IconButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("assetId", item.assetId))
                    Toast.makeText(context, "Copied: ${item.assetId}", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(36.dp)
            ) {
                Text(
                    text = "C",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun StatusChip(status: AssetStatus) {
    val (label, containerColor, textColor) = when (status) {
        AssetStatus.OK -> Triple("OK", CorrectGreen.copy(alpha = 0.15f), CorrectGreen)
        AssetStatus.MISSING -> Triple("MISSING", IncorrectRed.copy(alpha = 0.15f), IncorrectRed)
        AssetStatus.UNUSED -> Triple("UNUSED", Color.Gray.copy(alpha = 0.15f), Color.Gray)
    }

    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
