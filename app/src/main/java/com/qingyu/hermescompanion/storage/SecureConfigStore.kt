package com.qingyu.hermescompanion.storage

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import com.qingyu.hermescompanion.model.ConnectionConfig
import com.qingyu.hermescompanion.model.NotificationPreferences
import com.qingyu.hermescompanion.model.VoicePreferences
import com.qingyu.hermescompanion.model.UserProfilePreferences
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecureConfigStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun read(): ConnectionConfig? = runCatching {
        val baseUrl = preferences.getString(KEY_GATEWAY_URL, null)?.takeIf { it.isNotBlank() } ?: return null
        val username = preferences.getString(KEY_USERNAME, null)?.takeIf { it.isNotBlank() } ?: return null
        ConnectionConfig(baseUrl = baseUrl, username = username)
    }.getOrNull()

    fun save(config: ConnectionConfig) {
        preferences.edit {
            putString(KEY_GATEWAY_URL, config.baseUrl)
            putString(KEY_USERNAME, config.username)
        }
    }

    fun readCookies(): String? = runCatching {
        val encrypted = preferences.getString(KEY_AUTH_COOKIES, null)
            ?.takeIf { it.isNotBlank() }
            ?: return@runCatching null
        decrypt(encrypted)
    }.getOrNull()

    fun saveCookies(json: String) {
        preferences.edit {
            if (json.isBlank() || json == "[]") remove(KEY_AUTH_COOKIES)
            else putString(KEY_AUTH_COOKIES, encrypt(json))
        }
    }

    fun clearCookies() {
        preferences.edit { remove(KEY_AUTH_COOKIES) }
    }

    fun clear() {
        preferences.edit {
            remove(KEY_GATEWAY_URL)
            remove(KEY_USERNAME)
            remove(KEY_AUTH_COOKIES)
            // Remove keys written by the API-key based 0.1 prototype.
            remove(LEGACY_KEY_BASE_URL)
            remove(LEGACY_KEY_API_KEY)
        }
    }

    fun readThemeMode(): String? = runCatching { preferences.getString(KEY_THEME_MODE, null) }.getOrNull()

    fun saveThemeMode(value: String) {
        preferences.edit { putString(KEY_THEME_MODE, value) }
    }

    fun readSkinMode(): String? = runCatching { preferences.getString(KEY_SKIN_MODE, null) }.getOrNull()

    fun saveSkinMode(value: String) {
        preferences.edit { putString(KEY_SKIN_MODE, value) }
    }

    fun readNotificationPreferences(): NotificationPreferences = runCatching {
        NotificationPreferences(
            enabled = preferences.getBoolean(KEY_NOTIFICATION_ENABLED, true),
            messageAlerts = preferences.getBoolean(KEY_NOTIFICATION_MESSAGES, true),
            taskAlerts = preferences.getBoolean(KEY_NOTIFICATION_TASKS, true),
            sound = preferences.getBoolean(KEY_NOTIFICATION_SOUND, true),
            vibration = preferences.getBoolean(KEY_NOTIFICATION_VIBRATION, true),
            badge = preferences.getBoolean(KEY_NOTIFICATION_BADGE, true),
        )
    }.getOrDefault(NotificationPreferences())

    fun saveNotificationPreferences(value: NotificationPreferences) {
        preferences.edit {
            putBoolean(KEY_NOTIFICATION_ENABLED, value.enabled)
            putBoolean(KEY_NOTIFICATION_MESSAGES, value.messageAlerts)
            putBoolean(KEY_NOTIFICATION_TASKS, value.taskAlerts)
            putBoolean(KEY_NOTIFICATION_SOUND, value.sound)
            putBoolean(KEY_NOTIFICATION_VIBRATION, value.vibration)
            putBoolean(KEY_NOTIFICATION_BADGE, value.badge)
        }
    }

    fun readVoicePreferences(): VoicePreferences = runCatching {
        VoicePreferences(
            enabled = preferences.getBoolean(KEY_VOICE_ENABLED, true),
            language = preferences.getString(KEY_VOICE_LANGUAGE, "zh-CN").orEmpty().ifBlank { "zh-CN" },
            autoSend = preferences.getBoolean(KEY_VOICE_AUTO_SEND, false),
        )
    }.getOrDefault(VoicePreferences())

    fun saveVoicePreferences(value: VoicePreferences) {
        preferences.edit {
            putBoolean(KEY_VOICE_ENABLED, value.enabled)
            putString(KEY_VOICE_LANGUAGE, value.language)
            putBoolean(KEY_VOICE_AUTO_SEND, value.autoSend)
        }
    }

    fun readUserProfile(): UserProfilePreferences = runCatching {
        UserProfilePreferences(
            displayName = preferences.getString(KEY_PROFILE_NAME, "").orEmpty(),
            bio = preferences.getString(KEY_PROFILE_BIO, "个人工作助理").orEmpty().ifBlank { "个人工作助理" },
            avatarUri = preferences.getString(KEY_PROFILE_AVATAR, "").orEmpty(),
            hermesDisplayName = preferences.getString(KEY_HERMES_PROFILE_NAME, "Hermes").orEmpty().ifBlank { "Hermes" },
            hermesAvatarUri = preferences.getString(KEY_HERMES_PROFILE_AVATAR, "").orEmpty(),
        )
    }.getOrDefault(UserProfilePreferences())

    fun saveUserProfile(value: UserProfilePreferences) {
        preferences.edit {
            putString(KEY_PROFILE_NAME, value.displayName.trim())
            putString(KEY_PROFILE_BIO, value.bio.trim())
            putString(KEY_PROFILE_AVATAR, value.avatarUri)
            putString(KEY_HERMES_PROFILE_NAME, value.hermesDisplayName.trim().ifBlank { "Hermes" })
            putString(KEY_HERMES_PROFILE_AVATAR, value.hermesAvatarUri)
        }
    }

    fun readActiveHermesProfile(): String = runCatching {
        preferences.getString(KEY_ACTIVE_HERMES_PROFILE, "default").orEmpty().ifBlank { "default" }
    }.getOrDefault("default")

    fun saveActiveHermesProfile(value: String) {
        preferences.edit { putString(KEY_ACTIVE_HERMES_PROFILE, value.trim().ifBlank { "default" }) }
    }

    fun readDraft(profile: String, sessionId: String): String = runCatching {
        preferences.getString(draftKey(profile, sessionId), "").orEmpty().take(MAX_DRAFT_LENGTH)
    }.getOrDefault("")

    fun saveDraft(profile: String, sessionId: String, value: String) {
        if (profile.isBlank() || sessionId.isBlank()) return
        preferences.edit {
            if (value.isBlank()) remove(draftKey(profile, sessionId))
            else putString(draftKey(profile, sessionId), value.take(MAX_DRAFT_LENGTH))
        }
    }

    fun clearDraft(profile: String, sessionId: String) {
        if (profile.isBlank() || sessionId.isBlank()) return
        preferences.edit { remove(draftKey(profile, sessionId)) }
    }

    fun readUnreadSessionIds(): Set<String> = runCatching {
        preferences.getStringSet(KEY_UNREAD_SESSION_IDS, emptySet()).orEmpty().toSet()
    }.getOrDefault(emptySet())

    fun saveUnreadSessionIds(value: Set<String>) {
        preferences.edit { putStringSet(KEY_UNREAD_SESSION_IDS, value.toSet()) }
    }

    fun readCronSnapshot(): Map<String, String> = runCatching {
        val raw = preferences.getString(KEY_CRON_SNAPSHOT, null).orEmpty()
        if (raw.isBlank()) return@runCatching emptyMap()
        raw.lineSequence().mapNotNull { line ->
            val index = line.indexOf('\t')
            if (index <= 0) null else line.substring(0, index) to line.substring(index + 1)
        }.toMap()
    }.getOrDefault(emptyMap())

    fun saveCronSnapshot(value: Map<String, String>) {
        preferences.edit {
            putString(KEY_CRON_SNAPSHOT, value.entries.joinToString("\n") { "${it.key}\t${it.value}" })
        }
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))
        val payload = cipher.iv + encrypted
        return Base64.encodeToString(payload, Base64.NO_WRAP)
    }

    private fun decrypt(payload: String): String {
        val bytes = Base64.decode(payload, Base64.NO_WRAP)
        require(bytes.size > IV_SIZE_BYTES) { "Invalid encrypted configuration" }
        val iv = bytes.copyOfRange(0, IV_SIZE_BYTES)
        val encrypted = bytes.copyOfRange(IV_SIZE_BYTES, bytes.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
        return String(cipher.doFinal(encrypted), StandardCharsets.UTF_8)
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build(),
        )
        return generator.generateKey()
    }

    private fun draftKey(profile: String, sessionId: String): String {
        val scope = "$profile::$sessionId".toByteArray(StandardCharsets.UTF_8)
        return KEY_DRAFT_PREFIX + Base64.encodeToString(scope, Base64.NO_WRAP or Base64.URL_SAFE)
    }

    private companion object {
        const val PREFERENCES_NAME = "hermes_secure_connection"
        const val KEY_GATEWAY_URL = "gateway_url"
        const val KEY_USERNAME = "gateway_username"
        const val KEY_AUTH_COOKIES = "gateway_auth_cookies"
        const val LEGACY_KEY_BASE_URL = "base_url"
        const val LEGACY_KEY_API_KEY = "api_key"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_SKIN_MODE = "skin_mode"
        const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        const val KEY_NOTIFICATION_MESSAGES = "notification_messages"
        const val KEY_NOTIFICATION_TASKS = "notification_tasks"
        const val KEY_NOTIFICATION_SOUND = "notification_sound"
        const val KEY_NOTIFICATION_VIBRATION = "notification_vibration"
        const val KEY_NOTIFICATION_BADGE = "notification_badge"
        const val KEY_VOICE_ENABLED = "voice_enabled"
        const val KEY_VOICE_LANGUAGE = "voice_language"
        const val KEY_VOICE_AUTO_SEND = "voice_auto_send"
        const val KEY_PROFILE_NAME = "profile_display_name"
        const val KEY_PROFILE_BIO = "profile_bio"
        const val KEY_PROFILE_AVATAR = "profile_avatar_uri"
        const val KEY_HERMES_PROFILE_NAME = "hermes_profile_display_name"
        const val KEY_HERMES_PROFILE_AVATAR = "hermes_profile_avatar_uri"
        const val KEY_ACTIVE_HERMES_PROFILE = "active_hermes_profile"
        const val KEY_DRAFT_PREFIX = "chat_draft_"
        const val MAX_DRAFT_LENGTH = 50_000
        const val KEY_UNREAD_SESSION_IDS = "unread_session_ids"
        const val KEY_CRON_SNAPSHOT = "cron_snapshot"
        const val KEY_ALIAS = "hermes_companion_api_key"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_SIZE_BYTES = 12
        const val GCM_TAG_BITS = 128
    }
}
