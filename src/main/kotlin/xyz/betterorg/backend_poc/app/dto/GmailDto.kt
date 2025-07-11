package xyz.betterorg.backend_poc.app.dto

data class GmailDto(
    val id: String,
    val email: String,
    val token: String,
    val response: String
) {
    constructor() : this("", "", "", "")
}

data class GmailSyncStatus(
    val id: String,
    val email: String,
    val token: String
) {
    constructor() : this("", "", "")
}