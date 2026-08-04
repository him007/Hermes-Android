package com.qingyu.hermescompanion.diagnostics

import android.content.Context
import android.os.Build
import android.os.Process
import com.qingyu.hermescompanion.BuildConfig
import java.io.PrintWriter
import java.io.StringWriter
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean

/** Persists fatal runtime errors so a real-device crash never becomes an unexplained flash exit. */
object CrashDiagnostics {
    private const val PREFERENCES_NAME = "hermes_crash_diagnostics"
    private const val KEY_LAST_CRASH = "last_crash"
    private const val MAX_REPORT_LENGTH = 24_000
    private val installed = AtomicBoolean(false)

    fun install(context: Context) {
        if (!installed.compareAndSet(false, true)) return
        val appContext = context.applicationContext
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                appContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(KEY_LAST_CRASH, buildReport(thread, throwable).take(MAX_REPORT_LENGTH))
                    .commit()
            }
            if (previous != null) {
                previous.uncaughtException(thread, throwable)
            } else {
                Process.killProcess(Process.myPid())
            }
        }
    }

    fun read(context: Context): String? =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LAST_CRASH, null)
            ?.takeIf(String::isNotBlank)

    fun clear(context: Context) {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_LAST_CRASH)
            .apply()
    }

    private fun buildReport(thread: Thread, throwable: Throwable): String {
        val stack = StringWriter().also { writer ->
            throwable.printStackTrace(PrintWriter(writer))
        }
        return buildString {
            appendLine("Hermes ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            appendLine("Time: ${Instant.now()}")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
            appendLine("Thread: ${thread.name}")
            appendLine()
            append(stack.toString())
        }
    }
}
