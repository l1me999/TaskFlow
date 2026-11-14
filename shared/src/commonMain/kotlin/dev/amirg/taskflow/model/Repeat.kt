package dev.amirg.taskflow.model

import kotlinx.serialization.Serializable

/** How often a task comes back after it is marked done. */
@Serializable
enum class RepeatRule {
    None,
    Daily,
    Weekdays,
    Weekly,
    Monthly,
    Yearly,
}

/** Safety stop: a badly stale task must never turn completion into a hang. */
private const val MAX_STEPS = 500

/**
 * The due date of the next occurrence after [dueDate].
 *
 * The result always lands on or after [today]: a daily task completed after a
 * week away should not come back already overdue. Returns `null` when the rule
 * is [RepeatRule.None], there is no due date, or the date is malformed.
 */
fun RepeatRule.nextOccurrence(dueDate: String?, today: String): String? {
    if (this == RepeatRule.None || dueDate == null) return null
    if (parseIsoDate(dueDate) == null || parseIsoDate(today) == null) return null

    var next: String = dueDate
    repeat(MAX_STEPS) {
        next = advance(next) ?: return null
        if (next >= today) return next
    }
    return next
}

private fun RepeatRule.advance(date: String): String? = when (this) {
    RepeatRule.None -> null
    RepeatRule.Daily -> isoDatePlusDays(date, 1)
    RepeatRule.Weekdays -> {
        var next = isoDatePlusDays(date, 1) ?: return null
        while (isoWeekday(next) == 6 || isoWeekday(next) == 7) {
            next = isoDatePlusDays(next, 1) ?: return null
        }
        next
    }

    RepeatRule.Weekly -> isoDatePlusDays(date, 7)
    RepeatRule.Monthly -> isoDatePlusMonths(date, 1)
    RepeatRule.Yearly -> isoDatePlusMonths(date, 12)
}
