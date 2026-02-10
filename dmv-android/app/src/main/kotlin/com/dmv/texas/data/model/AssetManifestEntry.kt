package com.dmv.texas.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AssetManifestEntry(
    val assetId: String,
    val description: String,
    val file: String,
    val sourceUrl: String,
    val license: String
)
