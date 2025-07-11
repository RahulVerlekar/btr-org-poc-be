package xyz.betterorg.backend_poc.app.controllers

import org.bson.types.ObjectId
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import xyz.betterorg.backend_poc.app.dto.AppendUserToGmailTokenRequest
import xyz.betterorg.backend_poc.app.dto.AuthResponse
import xyz.betterorg.backend_poc.app.dto.CreateUserRequest
import xyz.betterorg.backend_poc.app.security.HashEncoder
import xyz.betterorg.backend_poc.app.security.JwtService
import xyz.betterorg.backend_poc.app.service.GmailService
import xyz.betterorg.backend_poc.data.database.entity.User
import xyz.betterorg.backend_poc.data.database.repo.UserRepository
import java.time.Instant

@RestController
@RequestMapping("/user")
class UserController(
    private val repo: UserRepository,
    private val gmailService: GmailService,
    private val authService: JwtService,
    private val hashEncoder: HashEncoder
) {

    @PostMapping
    fun save(
        @RequestBody body: CreateUserRequest
    ): AuthResponse {
        val user = repo.save(
            User(
                name = body.name,
                email = body.email,
                title = body.title,
                aboutYou = body.aboutYou,
                token = "",
                response = "",
                password = hashEncoder.encode(body.password),
                createdAt = Instant.now(),
                id = ObjectId.get()
            )
        )
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

    @GetMapping
    fun getAllUsers(): List<User> {
        return repo.findAll()
    }


    @PostMapping(
        path = ["/link-gmail"]
    )
    fun linkGmail(
        @RequestBody body: AppendUserToGmailTokenRequest
    ): Any? {
        val user = repo.findById(ObjectId(body.id))
        if (user.isPresent) {
            val user = user.get()
            val updatedUser = repo.save(
                User(
                    name = user.name,
                    email = user.email,
                    title = user.title,
                    aboutYou = user.aboutYou,
                    token = body.token,
                    response = body.response,
                    createdAt = user.createdAt,
                    password = user.password,
                    id = user.id
                )
            )
            repo.save(updatedUser)
            val emails = gmailService.withToken(body.token).fetchAndSaveEmails(user.id.toString(), 200L)
            return emails

        }
        return "User not found"
    }

    @DeleteMapping(path = ["/{id}"])
    fun deleteById(
        @PathVariable id: String
    ): String {
        val userId = ObjectId(id)
        if (repo.existsById(userId)) {
            repo.deleteById(userId)
            return "User deleted successfully"
        }
        return "User not found"
    }

    @GetMapping("sync-status")
    fun getSyncStatus(): SyncStatusResponse {
        val ownerId = SecurityContextHolder.getContext().authentication.principal as String
        val userId = ObjectId(ownerId)
        val user = repo.findById(userId)
        return if (user.isPresent) {
            SyncStatusResponse(user.get().syncStatus)
        } else {
            SyncStatusResponse("User not found")
        }
    }

}


data class SyncStatusResponse(
    val status: String
)