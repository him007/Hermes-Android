package com.qingyu.hermescompanion.ui.screen

import com.qingyu.hermescompanion.model.HermesProfile
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileDisplayTest {
    @Test fun `boolean shaped descriptions are not shown`() {
        assertEquals("", profileDetailText(HermesProfile(name = "default", description = "false")))
    }

    @Test fun `model is used when description is invalid`() {
        assertEquals(
            "openai · gpt-5",
            profileDetailText(HermesProfile(name = "default", provider = "openai", model = "gpt-5", description = "false")),
        )
    }
}
