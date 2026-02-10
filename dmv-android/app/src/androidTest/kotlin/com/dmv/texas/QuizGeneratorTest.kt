package com.dmv.texas

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dmv.texas.data.local.dao.QuestionDao
import com.dmv.texas.data.local.dao.QuestionStatsDao
import com.dmv.texas.data.local.db.DMVDatabase
import com.dmv.texas.data.local.entity.QuestionEntity
import com.dmv.texas.data.model.QuizConfig
import com.dmv.texas.data.model.QuizMode
import com.dmv.texas.data.repository.QuestionRepository
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for quiz question generation.
 *
 * Exercises QuestionDao.getRandomQuestions (via QuestionRepository) to verify
 * that topic filtering, difficulty ranges, limits, isActive filtering, and
 * empty-result scenarios all work correctly against a real in-memory Room DB.
 */
@RunWith(AndroidJUnit4::class)
class QuizGeneratorTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: DMVDatabase
    private lateinit var questionDao: QuestionDao
    private lateinit var questionStatsDao: QuestionStatsDao
    private lateinit var repository: QuestionRepository

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, DMVDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        questionDao = database.questionDao()
        questionStatsDao = database.questionStatsDao()
        repository = QuestionRepository(questionDao, questionStatsDao)
    }

    @After
    fun teardown() {
        database.close()
    }

    // -- Helpers --

    private fun makeQuestion(
        id: String,
        stateCode: String = "TX",
        topic: String = "Road Signs",
        difficulty: Int = 1,
        isActive: Boolean = true
    ) = QuestionEntity(
        id = id,
        stateCode = stateCode,
        packVersion = 1,
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

    private fun quizConfig(
        topics: List<String> = listOf("Road Signs", "Right of Way", "Parking"),
        minDiff: Int = 1,
        maxDiff: Int = 3,
        count: Int = 20
    ) = QuizConfig(
        mode = QuizMode.PRACTICE,
        stateCode = "TX",
        topics = topics,
        questionCount = count,
        minDifficulty = minDiff,
        maxDifficulty = maxDiff
    )

    // ---------------------------------------------------------------
    // Test 1: Respects topic filter -- only matching topics returned
    // ---------------------------------------------------------------

    @Test
    fun respectsTopicFilter() = runTest {
        val questions = listOf(
            makeQuestion("TX-001", topic = "Road Signs"),
            makeQuestion("TX-002", topic = "Road Signs"),
            makeQuestion("TX-003", topic = "Right of Way"),
            makeQuestion("TX-004", topic = "Parking"),
            makeQuestion("TX-005", topic = "Speed Limits")
        )
        questionDao.insertAll(questions)

        val config = quizConfig(topics = listOf("Road Signs"), count = 10)
        val result = repository.getQuestionsForQuiz(config)

        assertEquals("Should return 2 Road Signs questions", 2, result.size)
        assertTrue(
            "All returned questions should be Road Signs",
            result.all { it.topic == "Road Signs" }
        )
    }

    @Test
    fun respectsMultipleTopicFilter() = runTest {
        val questions = listOf(
            makeQuestion("TX-001", topic = "Road Signs"),
            makeQuestion("TX-002", topic = "Right of Way"),
            makeQuestion("TX-003", topic = "Parking"),
            makeQuestion("TX-004", topic = "Speed Limits"),
            makeQuestion("TX-005", topic = "DUI Laws")
        )
        questionDao.insertAll(questions)

        val config = quizConfig(topics = listOf("Road Signs", "Parking"), count = 10)
        val result = repository.getQuestionsForQuiz(config)

        assertEquals("Should return 2 questions (Road Signs + Parking)", 2, result.size)
        assertTrue(
            "All returned should be Road Signs or Parking",
            result.all { it.topic in listOf("Road Signs", "Parking") }
        )
    }

    // ---------------------------------------------------------------
    // Test 2: Respects difficulty range
    // ---------------------------------------------------------------

    @Test
    fun respectsDifficultyRange() = runTest {
        val questions = listOf(
            makeQuestion("TX-001", difficulty = 1),
            makeQuestion("TX-002", difficulty = 1),
            makeQuestion("TX-003", difficulty = 2),
            makeQuestion("TX-004", difficulty = 2),
            makeQuestion("TX-005", difficulty = 3),
            makeQuestion("TX-006", difficulty = 3)
        )
        questionDao.insertAll(questions)

        val config = quizConfig(minDiff = 2, maxDiff = 3, count = 20)
        val result = repository.getQuestionsForQuiz(config)

        assertEquals("Should return 4 questions with difficulty 2-3", 4, result.size)
        assertTrue(
            "No difficulty=1 questions should appear",
            result.none { it.difficulty == 1 }
        )
        assertTrue(
            "All difficulties should be 2 or 3",
            result.all { it.difficulty in 2..3 }
        )
    }

    @Test
    fun respectsDifficultyRange_exactMatch() = runTest {
        val questions = listOf(
            makeQuestion("TX-001", difficulty = 1),
            makeQuestion("TX-002", difficulty = 2),
            makeQuestion("TX-003", difficulty = 3)
        )
        questionDao.insertAll(questions)

        val config = quizConfig(minDiff = 2, maxDiff = 2, count = 20)
        val result = repository.getQuestionsForQuiz(config)

        assertEquals("Should return 1 question with difficulty=2", 1, result.size)
        assertEquals("Should be difficulty 2", 2, result[0].difficulty)
    }

    // ---------------------------------------------------------------
    // Test 3: Respects limit
    // ---------------------------------------------------------------

    @Test
    fun respectsLimit() = runTest {
        val questions = (1..20).map { makeQuestion("TX-%03d".format(it)) }
        questionDao.insertAll(questions)

        val config = quizConfig(count = 10)
        val result = repository.getQuestionsForQuiz(config)

        assertEquals("Should return exactly 10 questions", 10, result.size)
    }

    @Test
    fun respectsLimit_fewerAvailableThanRequested() = runTest {
        val questions = (1..5).map { makeQuestion("TX-%03d".format(it)) }
        questionDao.insertAll(questions)

        val config = quizConfig(count = 20)
        val result = repository.getQuestionsForQuiz(config)

        assertEquals("Should return all 5 available questions", 5, result.size)
    }

    // ---------------------------------------------------------------
    // Test 4: No duplicates within a single query result
    // ---------------------------------------------------------------

    @Test
    fun noDuplicatesInResult() = runTest {
        val questions = (1..30).map { makeQuestion("TX-%03d".format(it)) }
        questionDao.insertAll(questions)

        // Run multiple times to catch any randomness issues
        repeat(5) { iteration ->
            val config = quizConfig(count = 15)
            val result = repository.getQuestionsForQuiz(config)
            val ids = result.map { it.id }
            val uniqueIds = ids.toSet()

            assertEquals(
                "Iteration $iteration: No duplicate IDs in result (got ${ids.size} results, ${uniqueIds.size} unique)",
                ids.size,
                uniqueIds.size
            )
        }
    }

    // ---------------------------------------------------------------
    // Test 5: isActive filtering -- only active questions returned
    // ---------------------------------------------------------------

    @Test
    fun isActiveFiltering() = runTest {
        val questions = listOf(
            makeQuestion("TX-001", isActive = true),
            makeQuestion("TX-002", isActive = true),
            makeQuestion("TX-003", isActive = false),
            makeQuestion("TX-004", isActive = false),
            makeQuestion("TX-005", isActive = true)
        )
        questionDao.insertAll(questions)

        val config = quizConfig(count = 20)
        val result = repository.getQuestionsForQuiz(config)

        assertEquals("Should return 3 active questions", 3, result.size)
        assertTrue(
            "All returned questions should be active",
            result.all { it.isActive }
        )
        assertTrue(
            "Inactive question TX-003 should not be in result",
            result.none { it.id == "TX-003" }
        )
        assertTrue(
            "Inactive question TX-004 should not be in result",
            result.none { it.id == "TX-004" }
        )
    }

    @Test
    fun isActiveFiltering_allInactive() = runTest {
        val questions = (1..5).map { makeQuestion("TX-%03d".format(it), isActive = false) }
        questionDao.insertAll(questions)

        val config = quizConfig(count = 20)
        val result = repository.getQuestionsForQuiz(config)

        assertTrue("Should return empty list when all questions inactive", result.isEmpty())
    }

    // ---------------------------------------------------------------
    // Test 6: Empty result for nonexistent topic
    // ---------------------------------------------------------------

    @Test
    fun emptyResultForNonexistentTopic() = runTest {
        val questions = (1..5).map { makeQuestion("TX-%03d".format(it), topic = "Road Signs") }
        questionDao.insertAll(questions)

        val config = quizConfig(topics = listOf("Underwater Driving"), count = 10)
        val result = repository.getQuestionsForQuiz(config)

        assertTrue("Should return empty list for nonexistent topic", result.isEmpty())
    }

    @Test
    fun emptyResultForNonexistentState() = runTest {
        val questions = (1..5).map { makeQuestion("TX-%03d".format(it), stateCode = "TX") }
        questionDao.insertAll(questions)

        val config = QuizConfig(
            mode = QuizMode.PRACTICE,
            stateCode = "ZZ",
            topics = listOf("Road Signs"),
            questionCount = 10,
            minDifficulty = 1,
            maxDifficulty = 3
        )
        val result = repository.getQuestionsForQuiz(config)

        assertTrue("Should return empty list for nonexistent state", result.isEmpty())
    }

    @Test
    fun emptyResultForDifficultyRangeOutOfBounds() = runTest {
        val questions = (1..5).map { makeQuestion("TX-%03d".format(it), difficulty = 1) }
        questionDao.insertAll(questions)

        val config = quizConfig(minDiff = 4, maxDiff = 5, count = 10)
        val result = repository.getQuestionsForQuiz(config)

        assertTrue("Should return empty list when no questions match difficulty", result.isEmpty())
    }

    // ---------------------------------------------------------------
    // Test 7: Combined filters (topic + difficulty + isActive + limit)
    // ---------------------------------------------------------------

    @Test
    fun combinedFilters() = runTest {
        val questions = listOf(
            makeQuestion("TX-001", topic = "Road Signs", difficulty = 1, isActive = true),
            makeQuestion("TX-002", topic = "Road Signs", difficulty = 2, isActive = true),
            makeQuestion("TX-003", topic = "Road Signs", difficulty = 3, isActive = true),
            makeQuestion("TX-004", topic = "Road Signs", difficulty = 2, isActive = false), // inactive
            makeQuestion("TX-005", topic = "Parking", difficulty = 2, isActive = true),     // wrong topic
            makeQuestion("TX-006", topic = "Road Signs", difficulty = 2, isActive = true),
            makeQuestion("TX-007", topic = "Road Signs", difficulty = 2, isActive = true)
        )
        questionDao.insertAll(questions)

        // Want: Road Signs, difficulty 2-3, active, limit 2
        val config = quizConfig(
            topics = listOf("Road Signs"),
            minDiff = 2,
            maxDiff = 3,
            count = 2
        )
        val result = repository.getQuestionsForQuiz(config)

        assertEquals("Should return exactly 2 questions", 2, result.size)
        assertTrue("All should be Road Signs", result.all { it.topic == "Road Signs" })
        assertTrue("All should be difficulty 2-3", result.all { it.difficulty in 2..3 })
        assertTrue("All should be active", result.all { it.isActive })
        assertTrue("TX-004 (inactive) must not appear", result.none { it.id == "TX-004" })
        assertTrue("TX-005 (wrong topic) must not appear", result.none { it.id == "TX-005" })
    }
}
