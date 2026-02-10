package com.dmv.texas.import_

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.dmv.texas.data.local.db.DMVDatabase
import com.dmv.texas.data.local.entity.QuestionEntity
import com.dmv.texas.data.local.entity.StatePackEntity
import com.dmv.texas.data.model.PackJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Scans bundled asset packs and imports them into Room.
 * Import is idempotent: if the installed version >= bundled version, it skips.
 * Uses Room's withTransaction to ensure atomicity of question + metadata insert.
 */
class PackImporter(
    private val context: Context,
    private val database: DMVDatabase
) {
    data class ImportResult(
        val stateCode: String,
        val questionsImported: Int,
        val alreadyUpToDate: Boolean
    )

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun importAllPacks(
        onProgress: (message: String, current: Int, total: Int) -> Unit = { _, _, _ -> }
    ): List<ImportResult> =
        withContext(Dispatchers.IO) {
            val results = mutableListOf<ImportResult>()

            // Collect all pack files first to know total count
            val packFiles = mutableListOf<Pair<String, String>>() // (stateDir, fileName)
            val packDirs = try {
                context.assets.list("packs") ?: emptyArray()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to list packs directory", e)
                emptyArray()
            }

            for (stateDir in packDirs) {
                val files = try {
                    context.assets.list("packs/$stateDir") ?: emptyArray()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to list packs/$stateDir", e)
                    continue
                }
                for (file in files) {
                    if (file.endsWith(".json")) packFiles.add(stateDir to file)
                }
            }

            val total = packFiles.size
            packFiles.forEachIndexed { index, (stateDir, file) ->
                try {
                    onProgress("Importing $stateDir...", index + 1, total)
                    val result = importPack("packs/$stateDir/$file")
                    results.add(result)
                    Log.i(TAG, "Import result: $result")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to import packs/$stateDir/$file", e)
                }
            }
            results
        }

    private suspend fun importPack(assetPath: String): ImportResult {
        val jsonStr = context.assets.open(assetPath).bufferedReader().use { it.readText() }
        val pack: PackJson = json.decodeFromString(jsonStr)

        // Idempotency check: skip if already at this version or newer
        val existing = database.statePackDao().getByStateCode(pack.stateCode)
        if (existing != null && existing.version >= pack.version) {
            return ImportResult(pack.stateCode, 0, alreadyUpToDate = true)
        }

        val entities = pack.questions.map { q ->
            QuestionEntity(
                id = q.id,
                stateCode = pack.stateCode,
                packVersion = pack.version,
                topic = q.topic,
                difficulty = q.difficulty,
                text = q.text,
                choices = q.choices,
                correctIndex = q.correctIndex,
                explanation = q.explanation,
                reference = q.reference,
                imageAssetId = q.image?.assetId
            )
        }

        // Atomic transaction: deactivate old questions, upsert new ones, update metadata
        database.withTransaction {
            database.questionDao().deactivateByStateCode(pack.stateCode)
            database.questionDao().insertAll(entities)
            database.statePackDao().upsert(
                StatePackEntity(
                    stateCode = pack.stateCode,
                    version = pack.version,
                    installedAt = System.currentTimeMillis(),
                    questionCount = entities.size
                )
            )
        }

        return ImportResult(pack.stateCode, entities.size, alreadyUpToDate = false)
    }

    companion object {
        private const val TAG = "PackImporter"
    }
}
