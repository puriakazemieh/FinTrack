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
}
