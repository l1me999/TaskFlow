package dev.amirg.taskflow.platform

/** Wall-clock time in milliseconds. Used for `createdAt` / `updatedAt`. */
internal expect fun nowMillis(): Long

/** Today's local date as `YYYY-MM-DD`. */
internal expect fun todayIsoDate(): String
