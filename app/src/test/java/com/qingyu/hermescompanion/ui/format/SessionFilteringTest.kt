package com.qingyu.hermescompanion.ui.format

import java.time.Instant
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionFilteringTest {
    private val now = Instant.parse("2026-08-03T12:00:00Z")

    @Test fun `all accepts sessions without a timestamp`() {
        assertTrue(sessionMatchesTime("", SessionTimeFilter.ALL, now))
    }

    @Test fun `seven day filter accepts ISO timestamps inside range`() {
        assertTrue(sessionMatchesTime("2026-07-30T08:00:00Z", SessionTimeFilter.SEVEN_DAYS, now))
    }

    @Test fun `today filter rejects older timestamps`() {
        assertFalse(sessionMatchesTime("2026-08-01T08:00:00Z", SessionTimeFilter.TODAY, now))
    }

    @Test fun `unix seconds are supported`() {
        assertTrue(sessionMatchesTime("1785754800", SessionTimeFilter.TODAY, now))
    }
}
