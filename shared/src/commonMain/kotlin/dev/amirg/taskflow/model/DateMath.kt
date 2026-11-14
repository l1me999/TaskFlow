package dev.amirg.taskflow.model

/**
 * Small civil-date helpers for `YYYY-MM-DD` strings.
 *
 * `commonMain` cannot use `java.time`, so the arithmetic is done with the
 * well-known "days since epoch" algorithm. Only what the app needs lives here:
 * parsing, day/month shifts and the weekday.
 */
internal data class IsoDate(val year: Int, val month: Int, val day: Int)

internal fun parseIsoDate(iso: String): IsoDate? {
    val parts = iso.split('-')
    if (parts.size != 3) return null
    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val day = parts[2].toIntOrNull() ?: return null
    if (month !in 1..12) return null
    if (day !in 1..daysInMonth(year, month)) return null
    return IsoDate(year, month, day)
}

internal fun IsoDate.toIso(): String = buildString {
    append(year.toString().padStart(4, '0'))
    append('-')
    append(month.toString().padStart(2, '0'))
    append('-')
    append(day.toString().padStart(2, '0'))
}

/** Shifts an ISO date by [days]; `null` when [iso] is not a valid date. */
internal fun isoDatePlusDays(iso: String, days: Int): String? {
    val date = parseIsoDate(iso) ?: return null
    return civilFromDays(date.epochDay() + days).toIso()
}

/**
 * Shifts an ISO date by [months], keeping the day of month but clamping it to
 * the target month: 31 January + 1 month is 28 February, not 3 March.
 */
internal fun isoDatePlusMonths(iso: String, months: Int): String? {
    val date = parseIsoDate(iso) ?: return null
    val zeroBased = date.year * 12 + (date.month - 1) + months
    val year = zeroBased.floorDiv(12)
    val month = zeroBased - year * 12 + 1
    val day = minOf(date.day, daysInMonth(year, month))
    return IsoDate(year, month, day).toIso()
}

/** Weekday of an ISO date: 1 = Monday ... 7 = Sunday, `null` when invalid. */
internal fun isoWeekday(iso: String): Int? {
    val date = parseIsoDate(iso) ?: return null
    // 1970-01-01 was a Thursday; shift so Monday becomes 1.
    val days = date.epochDay()
    return (((days + 3) % 7 + 7) % 7 + 1).toInt()
}

private fun daysInMonth(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    else -> if (isLeapYear(year)) 29 else 28
}

private fun isLeapYear(year: Int): Boolean =
    year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)

/** Days since 1970-01-01 (Howard Hinnant's civil-from-days algorithm). */
internal fun IsoDate.epochDay(): Long {
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
internal fun civilFromDays(epochDay: Long): IsoDate {
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
