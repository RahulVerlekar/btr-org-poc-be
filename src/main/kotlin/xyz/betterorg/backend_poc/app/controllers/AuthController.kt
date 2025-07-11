package xyz.betterorg.backend_poc.app.controllers

import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import xyz.betterorg.backend_poc.app.dto.AuthResponse
import xyz.betterorg.backend_poc.app.dto.LoginUserRequest
import xyz.betterorg.backend_poc.app.security.HashEncoder
import xyz.betterorg.backend_poc.app.security.JwtService
import xyz.betterorg.backend_poc.app.service.GmailService
import xyz.betterorg.backend_poc.data.database.entity.AuthCode
import xyz.betterorg.backend_poc.data.database.repo.AuthCodeRepository
import xyz.betterorg.backend_poc.data.database.repo.UserRepository

@RestController
@RequestMapping("/auth")
class AuthController(
    val authService: JwtService,
    val gmailService: GmailService,
    val userRepo: UserRepository,
    val authRepo: AuthCodeRepository,
    val encoder: HashEncoder
) {

    @PostMapping("/login")
    fun login(
        @RequestBody request: LoginUserRequest
    ): AuthResponse {
        val user = userRepo.findByEmail(request.email)
            ?: throw BadCredentialsException("Invalid email or password")
        if (!encoder.matches(request.password, user.password)) {
            throw BadCredentialsException("Invalid email or password")
        }
        val token = authService.generateAccessToken(user.id.toHexString())
        val refreshToken = authService.generateRefreshToken(user.id.toHexString())
        val response = AuthResponse(
            accessToken = token,
            refreshToken = refreshToken,
            id = user.id.toString(),
            email = user.email,
            name = user.name
        )
        return response
    }

    @GetMapping("/gmail/callback")
    fun gmailCallback(
        @RequestParam("code") code: String,
        @RequestParam("state") state: String
    ): AuthCode {
        val authCode = gmailService.fetchTokensFromAuthCode(code, state)
        gmailService.asyncFetchEmails(state, authCode.accessToken ?: "")
        return authCode;
    }

}