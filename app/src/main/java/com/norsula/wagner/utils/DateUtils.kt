package com.norsula.wagner.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun parseComicDate(dateStr: String?): LocalDate? =
    dateStr
        ?.trim()
        ?.take(10)
        ?.let { runCatching { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull() }

fun formatDate(dateStr: String): String {
    val date = requireNotNull(parseComicDate(dateStr)) {
        "Invalid comic date: $dateStr"
    }
    val day = date.dayOfMonth
    val month = when (date.monthValue) {
        1 -> "січня"
        2 -> "лютого"
        3 -> "березня"
        4 -> "квітня"
        5 -> "травня"
        6 -> "червня"
        7 -> "липня"
        8 -> "серпня"
        9 -> "вересня"
        10 -> "жовтня"
        11 -> "листопада"
        12 -> "грудня"
        else -> ""
    }
    val year = date.year
    return "$day $month $year"
}
