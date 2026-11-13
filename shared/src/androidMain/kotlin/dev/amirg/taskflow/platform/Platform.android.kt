package dev.amirg.taskflow.platform

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

internal actual fun nowMillis(): Long = System.currentTimeMillis()

internal actual fun todayIsoDate(): String = LocalDate.now().toString()

internal actual fun isoDateFromEpochMillisUtc(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()

internal actual fun epochMillisFromIsoDateUtc(isoDate: String): Long? =
    runCatching {
        LocalDate.parse(isoDate).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    }.getOrNull()
