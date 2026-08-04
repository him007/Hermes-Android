package com.qingyu.hermescompanion.storage

import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AvatarStorageTest {
    // The UI is allowed to reopen only copied private files after process recreation.
    @Test
    fun acceptsExistingPrivateAvatarFile() {
        val root = Files.createTempDirectory("hermes-avatars").toFile()
        val avatar = root.resolve("user-test.avatar").apply { writeBytes(byteArrayOf(1, 2, 3)) }

        assertEquals(avatar.canonicalFile, resolvePrivateAvatarFile(avatar.toURI().toString(), root))
    }

    @Test
    fun rejectsExpiredContentUriAndFilesOutsidePrivateDirectory() {
        val root = Files.createTempDirectory("hermes-avatars").toFile()
        val outside = Files.createTempFile("outside-avatar", ".jpg").toFile()

        assertNull(
            resolvePrivateAvatarFile(
                "content://com.android.providers.media.documents/document/image%3A10327",
                root,
            ),
        )
        assertNull(resolvePrivateAvatarFile(outside.toURI().toString(), root))
    }
}
