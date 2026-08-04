package com.qingyu.hermescompanion.ui.screen

import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileAvatarCropTest {
    @Test fun `horizontal bias moves zoomed image in opposite direction`() {
        assertEquals(-100f, avatarPreviewTranslation(bias = 1f, zoom = 2f, axisSize = 200f))
        assertEquals(100f, avatarPreviewTranslation(bias = -1f, zoom = 2f, axisSize = 200f))
    }

    @Test fun `unzoomed image has no extra translation`() {
        assertEquals(0f, avatarPreviewTranslation(bias = 1f, zoom = 1f, axisSize = 200f))
    }
}
