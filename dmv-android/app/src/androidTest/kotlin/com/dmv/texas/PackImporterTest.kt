package com.dmv.texas

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dmv.texas.data.local.dao.QuestionDao
import com.dmv.texas.data.local.dao.StatePackDao
import com.dmv.texas.data.local.db.DMVDatabase
import com.dmv.texas.data.local.entity.QuestionEntity
import com.dmv.texas.data.local.entity.StatePackEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for the pack import pipeline.
 *
 * These test the DAO-level logic that PackImporter depends on:
 * question insertion, upsert idempotency, soft-delete via deactivate,
 * and state pack version tracking. Uses a real in-memory Room database
 * so we get actual SQLite behavior without needing asset files.
 */
@RunWith(AndroidJUnit4::class)
class PackImporterTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: DMVDatabase
    private lateinit var questionDao: QuestionDao
    private lateinit var statePackDao: StatePackDao

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, DMVDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        questionDao = database.questionDao()
        statePackDao = database.statePackDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    // -- Helper to build test QuestionEntity instances --

    private fun makeQuestion(
        id: String,
        stateCode: String = "TX",
        topic: String = "Road Signs",
        difficulty: Int = 1,
        isActive: Boolean = true,
        packVersion: Int = 1
    ) = QuestionEntity(
        id = id,
        stateCode = stateCode,
        packVersion = packVersion,
        topic = topic,
        difficulty = difficulty,
        text = "Test question $id",
        choices = listOf("A", "B", "C", "D"),
        correctIndex = 0,
        explanation = "Explanation for $id",
        reference = "Test ref",
        imageAssetId = null,
        isActive = isActive
    )

    // ---------------------------------------------------------------
    // Test 1: Insert questions and verify count
    // ---------------------------------------------------------------

    @Test
    fun insertQuestions_verifyCount() = runTest {
        val questions = (1..15).map { makeQuestion("TX-%03d".format(it)) }
        questionDao.insertAll(questions)

        val count = questionDao.getCount("TX")
        assertEquals("Inserted 15 questions, getCount should return 15", 15, count)
    }

    // ---------------------------------------------------------------
    // Test 2: Upsert is idempotent -- inserting same IDs twice does
    // not create duplicates (OnConflictStrategy.REPLACE)
    // ---------------------------------------------------------------

    @Test
    fun upsertIdempotent_noDuplicates() = runTest {
        val questions = (1..10).map { makeQuestion("TX-%03d".format(it)) }

        questionDao.insertAll(questions)
        val countAfterFirst = questionDao.getCount("TX")

        // Insert the exact same batch again
        questionDao.insertAll(questions)
        val countAfterSecond = questionDao.getCount("TX")

        assertEquals("Count should be 10 after first insert", 10, countAfterFirst)
        assertEquals("Count should still be 10 after second insert (idempotent)", 10, countAfterSecond)
    }

    @Test
    fun upsertReplacesFields() = runTest {
        val original = makeQuestion("TX-001", topic = "Road Signs")
        questionDao.insertAll(listOf(original))

        // Re-insert with updated topic (same ID)
        val updated = original.copy(topic = "Right of Way")
        questionDao.insertAll(listOf(updated))

        val fetched = questionDao.getById("TX-001")
        assertNotNull(fetched)
        assertEquals("Topic should be updated after upsert", "Right of Way", fetched!!.topic)
        assertEquals("Count should remain 1", 1, questionDao.getCount("TX"))
    }

    // ---------------------------------------------------------------
    // Test 3: Soft-delete on re-import. The import pipeline calls
    // deactivateByStateCode then inserts the new set. Only new ones
    // are active afterward.
    // ---------------------------------------------------------------

    @Test
    fun softDeleteOnReimport_onlyNewQuestionsActive() = runTest {
        // First import: TX-001 through TX-005
        val v1Questions = (1..5).map { makeQuestion("TX-%03d".format(it), packVersion = 1) }
        questionDao.insertAll(v1Questions)
        assertEquals("5 active after v1", 5, questionDao.getCount("TX"))

        // Simulate re-import: deactivate all TX questions, then insert v2 subset
        questionDao.deactivateByStateCode("TX")

        // v2 has only TX-003, TX-004, TX-005 plus a new TX-006
        val v2Questions = listOf(
            makeQuestion("TX-003", packVersion = 2),
            makeQuestion("TX-004", packVersion = 2),
            makeQuestion("TX-005", packVersion = 2),
            makeQuestion("TX-006", packVersion = 2)
        )
        questionDao.insertAll(v2Questions)

        // Active count should be 4 (the v2 set). TX-001, TX-002 were deactivated
        // and not re-inserted as active.
        val activeCount = questionDao.getCount("TX")
        assertEquals("Only 4 v2 questions should be active", 4, activeCount)

        // TX-001 should still exist in DB but be inactive
        val deactivated = questionDao.getById("TX-001")
        assertNotNull("TX-001 should still exist", deactivated)
        assertEquals("TX-001 should be inactive", false, deactivated!!.isActive)

        // TX-006 should be active
        val newQuestion = questionDao.getById("TX-006")
        assertNotNull("TX-006 should exist", newQuestion)
        assertEquals("TX-006 should be active", true, newQuestion!!.isActive)
    }

    @Test
    fun deactivateDoesNotAffectOtherStates() = runTest {
        val txQuestions = (1..3).map { makeQuestion("TX-%03d".format(it), stateCode = "TX") }
        val caQuestions = (1..3).map { makeQuestion("CA-%03d".format(it), stateCode = "CA") }
        questionDao.insertAll(txQuestions)
        questionDao.insertAll(caQuestions)

        // Deactivate only TX
        questionDao.deactivateByStateCode("TX")

        assertEquals("TX active count should be 0", 0, questionDao.getCount("TX"))
        assertEquals("CA active count should still be 3", 3, questionDao.getCount("CA"))
    }

    // ---------------------------------------------------------------
    // Test 4: Version check -- StatePackDao stores and retrieves
    // pack metadata correctly.
    // ---------------------------------------------------------------

    @Test
    fun versionCheck_storeAndRetrieve() = runTest {
        val pack = StatePackEntity(
            stateCode = "TX",
            version = 2,
            installedAt = System.currentTimeMillis(),
            questionCount = 100
        )
        statePackDao.upsert(pack)

        val retrieved = statePackDao.getByStateCode("TX")
        assertNotNull("Should find TX pack", retrieved)
        assertEquals("Version should be 2", 2, retrieved!!.version)
        assertEquals("Question count should be 100", 100, retrieved.questionCount)
    }

    @Test
    fun versionCheck_upsertUpdatesExisting() = runTest {
        val v1 = StatePackEntity("TX", version = 1, installedAt = 1000L, questionCount = 50)
        statePackDao.upsert(v1)

        val v2 = StatePackEntity("TX", version = 2, installedAt = 2000L, questionCount = 75)
        statePackDao.upsert(v2)

        val retrieved = statePackDao.getByStateCode("TX")
        assertNotNull(retrieved)
        assertEquals("Version should be 2 after upsert", 2, retrieved!!.version)
        assertEquals("Question count should be 75", 75, retrieved.questionCount)

        // Only one entry should exist
        val allPacks = statePackDao.getAll()
        assertEquals("Should have exactly 1 pack entry", 1, allPacks.size)
    }

    @Test
    fun versionCheck_nonexistentStateReturnsNull() = runTest {
        val result = statePackDao.getByStateCode("ZZ")
        assertNull("Nonexistent state code should return null", result)
    }
}
