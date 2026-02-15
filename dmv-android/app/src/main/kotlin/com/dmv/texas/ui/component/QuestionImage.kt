package com.dmv.texas.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.dmv.texas.util.AssetResolver

/**
 * Displays an SVG image from the assets directory, resolved via the asset manifest.
 * Uses Coil 3 with the global ImageLoader (which has SvgDecoder configured in DMVApp).
 * Returns nothing if the assetId cannot be resolved to a valid URI.
 *
 * Tapping the image opens a fullscreen viewer with pinch-to-zoom and pan support.
 */
@Composable
fun QuestionImage(
    assetId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val assetResolver = remember { AssetResolver(context) }
    val uri = remember(assetId) { assetResolver.getAssetUri(assetId) }
    var showFullscreen by remember { mutableStateOf(false) }

    if (uri != null) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(uri)
                .build(),
            contentDescription = "Question illustration – tap to enlarge",
            contentScale = ContentScale.Fit,
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp)
                .clickable { showFullscreen = true }
        )

        if (showFullscreen) {
            FullscreenImageViewer(
                imageUri = uri,
                onDismiss = { showFullscreen = false }
            )
        }
    }
}
