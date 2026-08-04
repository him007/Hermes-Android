package com.qingyu.hermescompanion.ui.format

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.Instant
import java.util.TimeZone

class TimeFormattingTest {
    @Test
    fun `recent session uses chinese relative time`() = withUtc {
        val now = Instant.parse("2026-08-02T10:30:00Z")
        assertEquals("30分钟前", sessionTimeLabel("2026-08-02T10:00:00Z", now))
        assertEquals("2小时前", sessionTimeLabel("2026-08-02T08:00:00Z", now))
    }

    @Test
    fun `older session uses chinese calendar time`() = withUtc {
        val now = Instant.parse("2026-08-02T10:30:00Z")
        assertEquals("昨天 16:40", sessionTimeLabel("2026-08-01T16:40:00Z", now))
        assertEquals("6月9日", sessionTimeLabel("2026-06-09T09:00:00Z", now))
    }

    @Test
    fun `numeric hermes timestamp is parsed`() {
        assertNotNull(parseHermesInstant("1785664800.0"))
        assertNotNull(parseHermesInstant("1785664800000"))
    }

    private fun withUtc(block: () -> Unit) {
        val previous = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            block()
        } finally {
            TimeZone.setDefault(previous)
        }
    }
}
