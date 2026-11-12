package dev.amirg.taskflow.platform

import java.time.LocalDate

internal actual fun nowMillis(): Long = System.currentTimeMillis()

internal actual fun todayIsoDate(): String = LocalDate.now().toString()
