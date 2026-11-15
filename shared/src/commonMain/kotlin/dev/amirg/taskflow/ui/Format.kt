package dev.amirg.taskflow.ui

import androidx.compose.ui.graphics.Color
import dev.amirg.taskflow.model.RepeatRule
import dev.amirg.taskflow.model.epochDay
import dev.amirg.taskflow.model.parseIsoDate

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
fun isoDatePlusDays(iso: String, days: Int): String? =
    dev.amirg.taskflow.model.isoDatePlusDays(iso, days)

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

/** Russian label for the repeat options shown in the details pane. */
fun RepeatRule.label(): String = when (this) {
    RepeatRule.None -> "Не повторять"
    RepeatRule.Daily -> "Каждый день"
    RepeatRule.Weekdays -> "По будням"
    RepeatRule.Weekly -> "Каждую неделю"
    RepeatRule.Monthly -> "Каждый месяц"
    RepeatRule.Yearly -> "Каждый год"
}

/** Цвет звёздочки «Важно», используется в карточке задачи и быстром вводе. */
internal val ImportantColor = Color(0xFFF2B53D)

/** Зелёная заливка выполненной задачи — как галочка «сделано» в To Do. */
internal val DoneColor = Color(0xFF2FA84F)
