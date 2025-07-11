package xyz.betterorg.backend_poc.data.database.entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id

data class AuthCode(
    @Id val id: ObjectId = ObjectId.get(),
    val userId: ObjectId,
    val accessToken: String?,
    val refreshToken: String?,
    val expiresIn: Long?,
    val scope: String?,
    val tokenType: String?,
    val idToken: String?
)