package xyz.betterorg.backend_poc.app.dto

data class AuthUserRequest(
    val email: String,
    val password: String
)

data class CreateUserRequest(
    val name: String,
    val email: String,
    val password: String,
    val title: String,
    val aboutYou: String,
)

data class DeleteUserRequest(
    val id: String
)

data class AppendUserToGmailTokenRequest(
    val id: String,
    val token: String,
    val response: String
)