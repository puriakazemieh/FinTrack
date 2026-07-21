package com.kazemieh.network

/**
 * Central, overridable configuration for server sync. Values default to the local
 * development server (the Android emulator maps host localhost to 10.0.2.2). A real
 * deployment should override [baseUrl]/[apiKey] at startup and set a stable [userId]
 * (e.g. from the signed-in account or a persisted device id).
 */
object SyncConfig {
    var baseUrl: String = ""
    var apiKey: String = ""
    var userId: String = "default_user"

    /**
     * Whether a real sync server is configured. Defaults to false so the app never tries to
     * reach a server until the user has entered their own URL + token in settings.
     */
    var enabled: Boolean = false

    /** Preference keys the UI persists these values under. */
    const val PREF_SERVER_URL = "pref_sync_server_url"
    const val PREF_TOKEN = "pref_sync_token"
    const val PREF_ENABLED = "pref_sync_enabled"

    /** Applies user-entered settings; sync is only truly enabled when a URL and token are present. */
    fun apply(url: String, token: String, wantEnabled: Boolean) {
        baseUrl = url.trim()
        apiKey = token.trim()
        enabled = wantEnabled && baseUrl.isNotBlank() && apiKey.isNotBlank()
    }
}
