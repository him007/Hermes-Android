package com.qingyu.hermescompanion.ui.screen

import org.junit.Assert.assertEquals
import org.junit.Test

class ChatScrollingTest {
    @Test fun `long final message scrolls to its end`() {
        assertEquals(1400, bottomScrollOffset(itemSize = 2200, viewportSize = 800))
    }

    @Test fun `short final message needs no internal offset`() {
        assertEquals(0, bottomScrollOffset(itemSize = 500, viewportSize = 800))
    }
}
