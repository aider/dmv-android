package com.dmv.texas.analytics

/**
 * Centralized event name constants and helper functions for the baseline funnel.
 *
 * ## Event Schema
 *
 * | Event              | Trigger                        | Properties                                          |
 * |--------------------|--------------------------------|-----------------------------------------------------|
 * | home_viewed        | Home screen displayed          | question_count: Int                                 |
 * | quiz_started       | Quiz begins (questions loaded) | mode, question_count, topics (comma-separated)      |
 * | quiz_completed     | Quiz finished (results shown)  | mode, question_count, correct, score_pct, duration_s|
 * | stats_opened       | Stats screen displayed         | —                                                   |
 * | retry_quiz_clicked | "Retry Quiz" tapped on Results | mode, question_count                                |
 */
object AnalyticsEvents {
    const val HOME_VIEWED = "home_viewed"
    const val QUIZ_STARTED = "quiz_started"
    const val QUIZ_COMPLETED = "quiz_completed"
    const val STATS_OPENED = "stats_opened"
    const val RETRY_QUIZ_CLICKED = "retry_quiz_clicked"
}
