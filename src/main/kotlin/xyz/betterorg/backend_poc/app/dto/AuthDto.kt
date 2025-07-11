package xyz.betterorg.backend_poc.app.dto

class AuthDto {
}

data class LoginUserRequest(
    val email: String,
    val password: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val id: String,
    val email: String,
    val name: String
)

