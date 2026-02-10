package com.dmv.texas.util

import android.content.Context
import com.dmv.texas.data.model.AssetManifestEntry
import kotlinx.serialization.json.Json

/**
 * Resolves asset IDs to file paths within the Android assets directory.
 * The manifest maps assetId -> file path. File paths in the manifest use
 * the prefix "assets/" which corresponds to the Android assets root, so
 * we strip that prefix to get the path usable with AssetManager.
 */
class AssetResolver(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    private val assetMap: Map<String, String> by lazy {
        try {
            val raw = context.assets.open("assets_manifest.json").bufferedReader().readText()
            val entries: List<AssetManifestEntry> = json.decodeFromString(raw)
            entries.associate { entry ->
                entry.assetId to entry.file.removePrefix("assets/")
            }
        } catch (e: Exception) {
            android.util.Log.e("AssetResolver", "Failed to load asset manifest", e)
            emptyMap()
        }
    }

    fun getAssetPath(assetId: String): String? = assetMap[assetId]

    fun getAssetUri(assetId: String): String? {
        val path = getAssetPath(assetId) ?: return null
        return "file:///android_asset/$path"
    }

    fun getAllAssetIds(): Set<String> = assetMap.keys

    fun getAssetCount(): Int = assetMap.size
}
