package com.dmv.texas.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
 */
@Composable
fun QuestionImage(
    assetId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val assetResolver = remember { AssetResolver(context) }
    val uri = remember(assetId) { assetResolver.getAssetUri(assetId) }

    if (uri != null) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(uri)
                .build(),
            contentDescription = "Question illustration",
            contentScale = ContentScale.Fit,
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
}
