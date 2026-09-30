package com.example.data.model

data class SheetConfig(
    val scriptUrl: String = DEFAULT_SCRIPT_URL,
    val sheetId: String = "",
    val isConfigured: Boolean = true,
    val lastSyncedAt: String = "",
    val autoSync: Boolean = true
) {
    companion object {
        const val DEFAULT_SCRIPT_URL = "https://script.google.com/macros/s/AKfycbwjFCJPgy3ppPuCKr7hryWKpY_Fqs-cprrmAoALKYnXxlm-0mDz2Sd5jz6o1RXuGZ25fg/exec"
    }
}
