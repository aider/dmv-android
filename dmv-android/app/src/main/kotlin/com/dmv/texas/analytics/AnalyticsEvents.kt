package com.dmv.texas.analytics

/**
 * Centralized event name constants and helper functions for the baseline funnel.
 *
 * ## Event Schema
 *
 * | Event                   | Trigger                        | Properties                                          |
 * |-------------------------|--------------------------------|-----------------------------------------------------|
 * | home_viewed             | Home screen displayed          | question_count: Int                                 |
 * | quick_start_tapped      | Quick Start button tapped      | —                                                   |
 * | customize_expanded      | Customize section opened       | —                                                   |
 * | customize_collapsed     | Customize section closed       | —                                                   |
 * | custom_quiz_started     | Start Custom Quiz tapped       | mode, question_count, topic_count                   |
 * | review_mistakes_tapped  | Review Mistakes from Progress  | mistake_count                                       |
 * | drill_weak_topic_tapped | Drill Weak Topic from Progress | topic                                               |
 * | quiz_started            | Quiz begins (questions loaded) | mode, question_count, topics (comma-separated)      |
 * | quiz_completed          | Quiz finished (results shown)  | mode, question_count, correct, score_pct, duration_s|
 * | stats_opened            | Stats screen displayed         | —                                                   |
 * | retry_quiz_clicked      | "Retry Quiz" tapped on Results | mode, question_count                                |
 */
object AnalyticsEvents {
    const val HOME_VIEWED = "home_viewed"
    const val QUICK_START_TAPPED = "quick_start_tapped"
    const val CUSTOMIZE_EXPANDED = "customize_expanded"
    const val CUSTOMIZE_COLLAPSED = "customize_collapsed"
    const val CUSTOM_QUIZ_STARTED = "custom_quiz_started"
    const val REVIEW_MISTAKES_TAPPED = "review_mistakes_tapped"
    const val DRILL_WEAK_TOPIC_TAPPED = "drill_weak_topic_tapped"
    const val QUIZ_STARTED = "quiz_started"
    const val QUIZ_COMPLETED = "quiz_completed"
    const val STATS_OPENED = "stats_opened"
    const val RETRY_QUIZ_CLICKED = "retry_quiz_clicked"
}
