package dev.amirg.taskflow

import dev.amirg.taskflow.model.RepeatRule
import dev.amirg.taskflow.model.nextOccurrence
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RepeatRuleTest {
    @Test
    fun dailyMovesToTheNextDay() {
        assertEquals("2026-11-15", RepeatRule.Daily.nextOccurrence("2026-11-14", "2026-11-14"))
    }

    @Test
    fun aStaleOccurrenceLandsOnTodayInsteadOfStayingOverdue() {
        assertEquals("2026-11-14", RepeatRule.Daily.nextOccurrence("2026-11-10", "2026-11-14"))
    }

    @Test
    fun weekdaysSkipTheWeekend() {
        // 2026-11-13 is a Friday.
        assertEquals("2026-11-16", RepeatRule.Weekdays.nextOccurrence("2026-11-13", "2026-11-13"))
    }

    @Test
    fun weeklyAddsSevenDays() {
        assertEquals("2026-11-21", RepeatRule.Weekly.nextOccurrence("2026-11-14", "2026-11-14"))
    }

    @Test
    fun monthlyClampsToTheShortMonth() {
        assertEquals("2026-02-28", RepeatRule.Monthly.nextOccurrence("2026-01-31", "2026-01-31"))
    }

    @Test
    fun yearlyKeepsTheLeapDayInsideFebruary() {
        assertEquals("2029-02-28", RepeatRule.Yearly.nextOccurrence("2028-02-29", "2028-02-29"))
    }

    @Test
    fun noneAndMissingDatesProduceNothing() {
        assertNull(RepeatRule.None.nextOccurrence("2026-11-14", "2026-11-14"))
        assertNull(RepeatRule.Daily.nextOccurrence(null, "2026-11-14"))
        assertNull(RepeatRule.Daily.nextOccurrence("14.11.2026", "2026-11-14"))
    }
}
