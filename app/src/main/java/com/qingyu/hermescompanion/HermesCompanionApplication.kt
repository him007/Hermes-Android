package com.qingyu.hermescompanion

import android.app.Application
import com.qingyu.hermescompanion.diagnostics.CrashDiagnostics
import com.qingyu.hermescompanion.notification.HermesNotifications
import com.qingyu.hermescompanion.storage.SecureConfigStore

class HermesCompanionApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashDiagnostics.install(this)
        val preferences = SecureConfigStore(this).readNotificationPreferences()
        // Notification setup should never keep the core client from opening on an OEM device.
        runCatching {
            HermesNotifications.applyPreferences(this, preferences)
            HermesNotifications.scheduleCronPolling(this, preferences.enabled && preferences.taskAlerts)
        }
    }
}
