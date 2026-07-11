package com.kazemieh.network

/**
 * Central, overridable configuration for server sync. Values default to the local
 * development server (the Android emulator maps host localhost to 10.0.2.2). A real
 * deployment should override [baseUrl]/[apiKey] at startup and set a stable [userId]
 * (e.g. from the signed-in account or a persisted device id).
 */
object SyncConfig {
    var baseUrl: String = "http://10.0.2.2:8080/sync"
    var apiKey: String = "fintrack_secret_token_2026"
    var userId: String = "default_user"

    /**
     * Whether a real sync server is configured. Defaults to false so the app never tries to
     * reach the development URL (which would fail with a cleartext-HTTP error). A real
     * deployment sets this to true after pointing [baseUrl] at its server.
     */
    var enabled: Boolean = false
}
