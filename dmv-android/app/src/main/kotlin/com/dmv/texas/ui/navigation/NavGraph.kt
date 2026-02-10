package com.dmv.texas.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.dmv.texas.DMVApp
import com.dmv.texas.data.model.QuizConfig
import com.dmv.texas.data.model.QuizMode
import com.dmv.texas.ui.screen.debug.AssetAuditScreen
import com.dmv.texas.ui.screen.debug.DataQualityScreen
import com.dmv.texas.ui.screen.debug.DebugScreen
import com.dmv.texas.ui.screen.home.HomeScreen
import com.dmv.texas.ui.screen.import_.ImportScreen
import com.dmv.texas.ui.screen.quiz.QuizScreen
import com.dmv.texas.ui.screen.quiz.QuizViewModel
import com.dmv.texas.ui.screen.results.ResultsScreen
import com.dmv.texas.ui.screen.stats.StatsScreen

/**
 * Top-level navigation graph. The quiz flow (Quiz + Results) is nested under
 * [QUIZ_FLOW_ROUTE] so the [QuizViewModel] can be scoped to that sub-graph
 * and shared between both screens without passing data through route arguments.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // ── Import ──────────────────────────────────────────────
        composable(Screen.Import.route) {
            ImportScreen(
                onImportComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Import.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Home ────────────────────────────────────────────────
        composable(Screen.Home.route) {
            val context = LocalContext.current
            HomeScreen(
                onStartQuiz = { config ->
                    // Store config on Application for QuizViewModel to pick up
                    (context.applicationContext as DMVApp).pendingQuizConfig = config
                    navController.navigate(QUIZ_FLOW_ROUTE)
                },
                onOpenStats = {
                    navController.navigate(Screen.Stats.route)
                },
                onOpenDebug = {
                    navController.navigate(Screen.Debug.route)
                }
            )
        }

        // ── Quiz Flow (shared QuizViewModel scope) ──────────────
        navigation(
            startDestination = Screen.Quiz.route,
            route = QUIZ_FLOW_ROUTE
        ) {
            composable(Screen.Quiz.route) {
                // Scope QuizViewModel to the quiz_flow parent graph entry
                val parentEntry = navController.getBackStackEntry(QUIZ_FLOW_ROUTE)
                val quizViewModel: QuizViewModel = viewModel(parentEntry)

                QuizScreen(
                    viewModel = quizViewModel,
                    onFinished = {
                        navController.navigate(Screen.Results.route) {
                            popUpTo(Screen.Quiz.route) { inclusive = true }
                        }
                    },
                    onQuit = {
                        navController.popBackStack(Screen.Home.route, inclusive = false)
                    }
                )
            }

            composable(Screen.Results.route) {
                val parentEntry = navController.getBackStackEntry(QUIZ_FLOW_ROUTE)
                val quizViewModel: QuizViewModel = viewModel(parentEntry)
                val state by quizViewModel.state.collectAsState()
                val context = LocalContext.current

                ResultsScreen(
                    questions = state.questions,
                    answers = state.answers,
                    config = state.config,
                    durationMs = state.durationMs,
                    onBackToHome = {
                        navController.popBackStack(Screen.Home.route, inclusive = false)
                    },
                    onRetryQuiz = {
                        // Re-set the config so the new QuizViewModel can read it
                        (context.applicationContext as DMVApp).pendingQuizConfig = state.config
                        navController.popBackStack(Screen.Home.route, inclusive = false)
                        navController.navigate(QUIZ_FLOW_ROUTE)
                    }
                )
            }
        }

        // ── Stats ───────────────────────────────────────────────
        composable(Screen.Stats.route) {
            val context = LocalContext.current
            StatsScreen(
                onBackToHome = { navController.popBackStack() },
                onPracticeMistakes = {
                    // Set up a Mistakes config and navigate to quiz flow
                    (context.applicationContext as DMVApp).pendingQuizConfig = QuizConfig(
                        mode = QuizMode.MISTAKES,
                        stateCode = "TX",
                        questionCount = 20
                    )
                    navController.popBackStack()
                    navController.navigate(QUIZ_FLOW_ROUTE)
                }
            )
        }

        // ── Debug ─────────────────────────────────────────────────
        composable(Screen.Debug.route) {
            DebugScreen(
                onBackToHome = { navController.popBackStack() },
                onOpenAssetAudit = { navController.navigate(Screen.AssetAudit.route) },
                onOpenDataQuality = { navController.navigate(Screen.DataQuality.route) }
            )
        }

        // ── Asset Audit ──────────────────────────────────────────
        composable(Screen.AssetAudit.route) {
            AssetAuditScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ── Data Quality ────────────────────────────────────────
        composable(Screen.DataQuality.route) {
            DataQualityScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
