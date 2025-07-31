package xyz.betterorg.backend_poc.app.service

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.Message
import com.google.api.services.gmail.model.MessagePart
import jakarta.validation.constraints.Future
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import xyz.betterorg.backend_poc.app.config.RequestLoggingInterceptor
import xyz.betterorg.backend_poc.data.database.entity.AuthCode
import xyz.betterorg.backend_poc.data.database.entity.Email
import xyz.betterorg.backend_poc.data.database.repo.AuthCodeRepository
import xyz.betterorg.backend_poc.data.database.repo.EmailRepository
import xyz.betterorg.backend_poc.data.database.repo.UserRepository
import java.time.Instant
import java.util.concurrent.CompletableFuture


@Service
class GmailService(
    @Value("\${google.client.id}") private val id: String,
    @Value("\${google.client.secret}") private val secret: String,
    private val emailRepository: EmailRepository,
    private val userRepository: UserRepository,
    private val mediaService: MediaService,
    private val authRepo: AuthCodeRepository
) {

    private var token: String? = ""
    private var gmail: Gmail? = null
    private val logger: Logger = LoggerFactory.getLogger(RequestLoggingInterceptor::class.java)

    fun withToken(token: String): GmailService {
        this.token = token
        buildGmailClient()
        return this
    }

    fun withUser(userId: String): GmailService {
        userRepository.findById(ObjectId(userId)).let {
            if (it.isEmpty) {
                throw IllegalArgumentException("User with ID $userId not found")
            } else {
                it.get().let { user ->
                    if (user.token.isEmpty()) {
                        throw IllegalArgumentException("User with ID $userId has no linked Gmail account")
                    } else {
                        this.token = user.token
                        buildGmailClient()
                        return this
                    }
                }
            }
        }
    }

    private fun buildGmailClient(): Gmail {
        val credential = GoogleCredential().setAccessToken(token)
        gmail = Gmail.Builder(
            com.google.api.client.http.javanet.NetHttpTransport(),
            GsonFactory(),
            credential
        ).setApplicationName("BetterOrg").build()
        return gmail ?: throw IllegalStateException("Gmail client not initialized")
    }


    @Async
    fun fetchAndSaveEmails(userId: String, limit: Long = 10): List<Email> {
        val emails = mutableListOf<Email>()
        gmail?.let { client ->
            val messages = client.users().messages().list("me")
                .setMaxResults(limit)
                .execute()
                .messages ?: return emails

            for ((i, msg) in messages.withIndex()) {
                val message: Message = client.users().messages().get("me", msg.id).setFormat("full").execute()
                val headers = message.payload.headers.associateBy { it.name }
                val subject = headers["Subject"]?.value ?: ""
                val from = headers["From"]?.value ?: ""
                val body = getEmailBody(message)
                val receivedAt = Instant.ofEpochMilli((message.internalDate ?: 0L))
                val attachments = mutableListOf<String>()
                val parts = getAllParts(message)


                for (part in parts) {
//                    val key = mediaService.upload(part)
//                    if (key.isNotEmpty()) {
//                        attachments.add(key)
//                    }
                }

                val email = Email(
                    userId = ObjectId(userId),
                    subject = subject,
                    messageId = message.id,
                    from = from,
                    body = body,
                    receivedAt = receivedAt,
                    attachments = attachments
                )
                emails.add(emailRepository.save(email))
                userRepository.updateSyncStatusById(userId, "Synced ${i + 1} of ${messages.size} emails")
            }
            userRepository.updateSyncStatusById(userId, "SYNC_COMPLETED")
        }

        return emails
    }

    fun getAllParts(message: Message): List<UploadablePart> {
        val parts = mutableListOf<UploadablePart>()
        fun getAllPartsRecursive(payload: MessagePart?, parts: MutableList<UploadablePart>) {
            payload?.let {
                println("MessageId: ${message.id} PartId: ${it.partId} ${it.mimeType}")
                if (it.body.size > 0 && it.body?.attachmentId != null) {
                    val attachmentId = it.body.attachmentId
                    val filename = it.filename
                    val contentType = it.mimeType ?: "application/octet-stream"
                    val metadata = it.headers.associate { header -> header.name to header.value }

                    val data =
                        gmail?.users()?.messages()?.attachments()?.get("me", message.id, attachmentId)?.execute()?.decodeData()
                            ?: throw IllegalStateException("Attachment data not found for ID: $attachmentId")

                    parts.add(
                        UploadablePart(
                            attachmentId = attachmentId,
                            messageId = message.id,
                            filename = filename,
                            contentType = contentType,
                            metadata = metadata,
                            data = data
                        )
                    )
                }
                it.parts?.forEach { part ->
                    getAllPartsRecursive(part, parts)
                }
            }
        }
        getAllPartsRecursive(message.payload, parts)
        return parts
    }


    fun fetchTokensFromAuthCode(authCode: String, userId: String): AuthCode {
        val tokenResponse = GoogleAuthorizationCodeTokenRequest(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            id,
            secret,
            authCode,
            "https://java.specifics.fyi/auth/gmail/callback"
        ).execute()

        val authCode = AuthCode(
            userId = ObjectId(userId),
            accessToken = tokenResponse.accessToken,
            refreshToken = tokenResponse.refreshToken,
            expiresIn = tokenResponse.expiresInSeconds,
            scope = tokenResponse.scope,
            tokenType = tokenResponse.tokenType,
            idToken = tokenResponse.idToken
        )
        return authRepo.save(authCode)
    }

    @Async
    fun asyncFetchEmails(userId: String, token: String): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            withToken(token).fetchAndSaveEmails(userId, 2000L)
        }.exceptionally { ex ->
            ex.printStackTrace()
            logger.error(ex.message)
            println("Error fetching emails: ${ex.message}")
            null
        }
    }
}

fun getEmailBody(message: Message): String {
    fun getBodyFromPart(part: MessagePart?): String {
        if (part == null) return ""
        return when {
            part.mimeType == "text/plain" || part.mimeType == "text/html" -> {
                part.body?.data?.let { String(java.util.Base64.getUrlDecoder().decode(it)) } ?: ""
            }
            part.parts != null -> {
                part.parts.joinToString("") { getBodyFromPart(it) }
            }
            else -> ""
        }
    }
    return getBodyFromPart(message.payload)
}



data class UploadablePart(
    val attachmentId: String,
    val messageId: String,
    val filename: String? = null,
    val contentType: String? = null,
    val metadata: Map<String, String>,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UploadablePart

        if (attachmentId != other.attachmentId) return false
        if (filename != other.filename) return false
        if (contentType != other.contentType) return false
        if (metadata != other.metadata) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = attachmentId.hashCode()
        result = 31 * result + (filename?.hashCode() ?: 0)
        result = 31 * result + (contentType?.hashCode() ?: 0)
        result = 31 * result + metadata.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }

}