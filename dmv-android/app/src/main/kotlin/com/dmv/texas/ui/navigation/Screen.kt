package com.dmv.texas.ui.navigation

/**
 * Sealed class representing all navigation destinations in the app.
 * Using simple string routes (no serialized arguments) since data is shared
 * via scoped ViewModels and the Application-level pendingQuizConfig holder.
 */
sealed class Screen(val route: String) {
    data object Import : Screen("import")
    data object Home : Screen("home")
    data object Quiz : Screen("quiz")
    data object Results : Screen("results")
    data object Stats : Screen("stats")
    data object Debug : Screen("debug")
    data object AssetAudit : Screen("asset_audit")
    data object DataQuality : Screen("data_quality")
}

/** Route for the nested quiz flow graph, scoping the shared QuizViewModel. */
const val QUIZ_FLOW_ROUTE = "quiz_flow"
