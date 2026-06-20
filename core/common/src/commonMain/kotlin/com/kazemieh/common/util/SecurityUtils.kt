package com.kazemieh.common.util

// A very simple XOR-based encryption for KMP demonstration without native libs.
// In a real app, use AES via expect/actual or a dedicated library.
object SecurityUtils {
    private const val APP_SECRET = "FinTrack_2026_Secret_Key"

    fun encrypt(data: String): String {
        return data.mapIndexed { index, char ->
            (char.code xor APP_SECRET[index % APP_SECRET.length].code).toChar()
        }.joinToString("")
    }

    fun decrypt(data: String): String {
        return encrypt(data) // XOR is reversible
    }
}
