package com.dmv.texas.ui.util

import java.util.Locale

/**
 * Formats a raw topic key (e.g. "PAVEMENT_MARKINGS") into a human-readable
 * display name (e.g. "Pavement Markings").
 */
fun formatTopicDisplayName(topic: String): String {
    return topic
        .replace('_', ' ')
        .lowercase(Locale.getDefault())
        .split(' ')
        .joinToString(" ") { word ->
            word.replaceFirstChar { it.titlecase(Locale.getDefault()) }
        }
}
