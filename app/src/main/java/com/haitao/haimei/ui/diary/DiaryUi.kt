package com.haitao.haimei.ui.diary

import com.haitao.haimei.R

enum class MoodFilter(val code: String?) {
    ALL(null),
    HAPPY("HAPPY"),
    CALM("CALM"),
    NEUTRAL("NEUTRAL"),
    SAD("SAD"),
    ANGRY("ANGRY")
}

fun moodToEmoji(code: String?): String {
    return when (code) {
        "HAPPY" -> "😊"
        "CALM" -> "🙂"
        "NEUTRAL" -> "😐"
        "SAD" -> "😔"
        "ANGRY" -> "😤"
        else -> ""
    }
}

fun moodFilterFromButtonId(id: Int): MoodFilter {
    return when (id) {
        R.id.diary_filter_happy, R.id.diary_edit_mood_happy -> MoodFilter.HAPPY
        R.id.diary_filter_calm, R.id.diary_edit_mood_calm -> MoodFilter.CALM
        R.id.diary_filter_neutral, R.id.diary_edit_mood_neutral -> MoodFilter.NEUTRAL
        R.id.diary_filter_sad, R.id.diary_edit_mood_sad -> MoodFilter.SAD
        R.id.diary_filter_angry, R.id.diary_edit_mood_angry -> MoodFilter.ANGRY
        else -> MoodFilter.ALL
    }
}

fun moodButtonIdFromCode(code: String?): Int {
    return when (code) {
        "HAPPY" -> R.id.diary_edit_mood_happy
        "CALM" -> R.id.diary_edit_mood_calm
        "NEUTRAL" -> R.id.diary_edit_mood_neutral
        "SAD" -> R.id.diary_edit_mood_sad
        "ANGRY" -> R.id.diary_edit_mood_angry
        else -> R.id.diary_edit_mood_all
    }
}

