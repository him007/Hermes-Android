package com.qingyu.hermescompanion.storage

import org.junit.Assert.assertEquals
import org.junit.Test

class AvatarCropTest {
    @Test fun `landscape image defaults to centered square`() {
        assertEquals(AvatarCropBounds(500, 0, 1000), avatarCropBounds(2000, 1000, AvatarCropSpec()))
    }

    @Test fun `bias selects edge after zoom`() {
        val result = avatarCropBounds(
            width = 1200,
            height = 1800,
            spec = AvatarCropSpec(zoom = 2f, horizontalBias = 1f, verticalBias = -1f),
        )
        assertEquals(AvatarCropBounds(600, 0, 600), result)
    }

    @Test fun `unsafe crop values are clamped`() {
        val result = avatarCropBounds(100, 80, AvatarCropSpec(zoom = 99f, horizontalBias = 5f, verticalBias = -5f))
        assertEquals(AvatarCropBounds(80, 0, 20), result)
    }
}
