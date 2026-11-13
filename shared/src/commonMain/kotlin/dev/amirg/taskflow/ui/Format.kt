package dev.amirg.taskflow.ui

import androidx.compose.ui.graphics.Color

private val MONTHS_SHORT = listOf(
    "янв.", "февр.", "мар.", "апр.", "мая", "июн.",
    "июл.", "авг.", "сент.", "окт.", "нояб.", "дек.",
)

private val MONTHS_FULL = listOf(
    "января", "февраля", "марта", "апреля", "мая", "июня",
    "июля", "августа", "сентября", "октября", "ноября", "декабря",
)

/**
 * Human date for a task row: "Сегодня", "Завтра", "Вчера" or "12 нояб."
 * (with a year when it is not the current one). Falls back to [iso] if parsing
 * fails, so a hand-edited file never crashes the UI.
 */
fun formatDueDate(iso: String, today: String): String {
    val due = parseIsoDate(iso) ?: return iso
    val now = parseIsoDate(today) ?: return "${due.day} ${MONTHS_SHORT[due.month - 1]}"

    return when (val diff = due.epochDay() - now.epochDay()) {
        0L -> "Сегодня"
        1L -> "Завтра"
        -1L -> "Вчера"
        else -> {
            val base = "${due.day} ${MONTHS_SHORT[due.month - 1]}"
            if (due.year != now.year || diff > 300 || diff < -300) "$base ${due.year}" else base
        }
    }
}

/** Full date for the details pane: "12 ноября 2026". */
fun formatFullDate(iso: String): String {
    val date = parseIsoDate(iso) ?: return iso
    return "${date.day} ${MONTHS_FULL[date.month - 1]} ${date.year}"
}

/** Shifts an ISO date by [days]; returns `null` when [iso] is not a valid date. */
fun isoDatePlusDays(iso: String, days: Int): String? {
    val date = parseIsoDate(iso) ?: return null
    val shifted = civilFromDays(date.epochDay() + days)
    return buildString {
        append(shifted.year.toString().padStart(4, '0'))
        append('-')
        append(shifted.month.toString().padStart(2, '0'))
        append('-')
        append(shifted.day.toString().padStart(2, '0'))
    }
}

/** Russian plural form: pluralRu(2, "задача", "задачи", "задач") == "задачи". */
fun pluralRu(count: Int, one: String, few: String, many: String): String {
    val n = kotlin.math.abs(count) % 100
    if (n in 11..14) return many
    return when (n % 10) {
        1 -> one
        2, 3, 4 -> few
        else -> many
    }
}

private data class IsoDate(val year: Int, val month: Int, val day: Int)

private fun parseIsoDate(iso: String): IsoDate? {
    val parts = iso.split('-')
    if (parts.size != 3) return null
    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val day = parts[2].toIntOrNull() ?: return null
    if (month !in 1..12) return null
    if (day !in 1..daysInMonth(year, month)) return null
    return IsoDate(year, month, day)
}

private fun daysInMonth(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    else -> if (isLeapYear(year)) 29 else 28
}

private fun isLeapYear(year: Int): Boolean =
    year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)

/** Days since 1970-01-01 (Howard Hinnant's civil-from-days algorithm). */
private fun IsoDate.epochDay(): Long {
    val m = month.toLong()
    val d = day.toLong()
    var y = year.toLong()
    if (m <= 2) y -= 1
    val era = (if (y >= 0) y else y - 399) / 400
    val yearOfEra = y - era * 400
    val dayOfYear = (153 * (if (m > 2) m - 3 else m + 9) + 2) / 5 + d - 1
    val dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear
    return era * 146097 + dayOfEra - 719468
}

/** Inverse of [epochDay]: the calendar date [epochDay] days after 1970-01-01. */
private fun civilFromDays(epochDay: Long): IsoDate {
    val z = epochDay + 719468
    val era = (if (z >= 0) z else z - 146096) / 146097
    val dayOfEra = z - era * 146097
    val yearOfEra = (dayOfEra - dayOfEra / 1460 + dayOfEra / 36524 - dayOfEra / 146096) / 365
    val year = yearOfEra + era * 400
    val dayOfYear = dayOfEra - (365 * yearOfEra + yearOfEra / 4 - yearOfEra / 100)
    val monthPart = (5 * dayOfYear + 2) / 153
    val day = dayOfYear - (153 * monthPart + 2) / 5 + 1
    val month = monthPart + if (monthPart < 10) 3 else -9
    return IsoDate(
        year = (year + if (month <= 2) 1 else 0).toInt(),
        month = month.toInt(),
        day = day.toInt(),
    )
}

/** Цвет звёздочки «Важно», используется в карточке задачи и быстром вводе. */
internal val ImportantColor = Color(0xFFF2B53D)
