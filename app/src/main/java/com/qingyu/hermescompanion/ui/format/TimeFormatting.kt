package com.qingyu.hermescompanion.ui.format

import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

private val DateTimeWithSpace = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

fun sessionTimeLabel(raw: String, now: Instant = Instant.now()): String {
    val instant = parseHermesInstant(raw) ?: return ""
    val zone = ZoneId.systemDefault()
    val value = instant.atZone(zone)
    val current = now.atZone(zone)
    val seconds = ChronoUnit.SECONDS.between(instant, now).coerceAtLeast(0)

    if (value.toLocalDate() == current.toLocalDate()) {
        return when {
            seconds < 60 -> "刚刚"
            seconds < 3600 -> "${seconds / 60}分钟前"
            seconds < 6 * 3600 -> "${seconds / 3600}小时前"
            else -> value.format(DateTimeFormatter.ofPattern("HH:mm"))
        }
    }
    if (value.toLocalDate() == current.toLocalDate().minusDays(1)) {
        return "昨天 ${value.format(DateTimeFormatter.ofPattern("HH:mm"))}"
    }
    return if (value.year == current.year) {
        value.format(DateTimeFormatter.ofPattern("M月d日"))
    } else {
        value.format(DateTimeFormatter.ofPattern("yyyy年M月d日"))
    }
}

fun messageTimeLabel(raw: String, now: Instant = Instant.now()): String {
    val instant = parseHermesInstant(raw) ?: return ""
    val zone = ZoneId.systemDefault()
    val value = instant.atZone(zone)
    val current = now.atZone(zone)
    return when (value.toLocalDate()) {
        current.toLocalDate() -> value.format(DateTimeFormatter.ofPattern("HH:mm"))
        current.toLocalDate().minusDays(1) -> "昨天 ${value.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        else -> if (value.year == current.year) {
            value.format(DateTimeFormatter.ofPattern("M月d日 HH:mm"))
        } else {
            value.format(DateTimeFormatter.ofPattern("yyyy年M月d日 HH:mm"))
        }
    }
}

fun shouldShowMessageTime(previousRaw: String?, currentRaw: String): Boolean {
    val current = parseHermesInstant(currentRaw) ?: return false
    val previous = previousRaw?.let(::parseHermesInstant) ?: return true
    return ChronoUnit.MINUTES.between(previous, current) >= 10 ||
        previous.atZone(ZoneId.systemDefault()).toLocalDate() != current.atZone(ZoneId.systemDefault()).toLocalDate()
}

fun parseHermesInstant(raw: String): Instant? {
    val text = raw.trim()
    if (text.isBlank()) return null
    text.toDoubleOrNull()?.let { number ->
        return runCatching {
            if (number > 10_000_000_000) Instant.ofEpochMilli(number.toLong())
            else Instant.ofEpochSecond(number.toLong())
        }.getOrNull()
    }
    return tryParse { Instant.parse(text) }
        ?: tryParse { OffsetDateTime.parse(text).toInstant() }
        ?: tryParse { LocalDateTime.parse(text, DateTimeWithSpace).atZone(ZoneId.systemDefault()).toInstant() }
        ?: tryParse { LocalDateTime.parse(text).atZone(ZoneId.systemDefault()).toInstant() }
}

private inline fun tryParse(block: () -> Instant): Instant? = try {
    block()
} catch (_: DateTimeParseException) {
    null
}
