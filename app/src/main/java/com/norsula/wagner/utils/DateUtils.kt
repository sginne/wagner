package com.norsula.wagner.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun formatDate(dateStr: String): String {
    val date = LocalDate.parse(
        dateStr.trim().take(10),
        DateTimeFormatter.ISO_LOCAL_DATE
    )
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
