package dev.amirg.taskflow.platform

/** Wall-clock time in milliseconds. Used for `createdAt` / `updatedAt`. */
internal expect fun nowMillis(): Long

/** Today's local date as `YYYY-MM-DD`. */
internal expect fun todayIsoDate(): String

/**
 * Converts UTC epoch millis (what Material's `DatePicker` works with) to an ISO
 * date in UTC, so a picked day never shifts because of the local time zone.
 */
internal expect fun isoDateFromEpochMillisUtc(millis: Long): String

/** Converts an ISO date to the UTC start-of-day millis the date picker expects. */
internal expect fun epochMillisFromIsoDateUtc(isoDate: String): Long?
