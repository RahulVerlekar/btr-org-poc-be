package xyz.betterorg.backend_poc.data.database.entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("emails")
data class Email(
    @Id val id: ObjectId = ObjectId.get(),
    val userId: ObjectId,
    val messageId: String,
    val subject: String,
    val from: String,
    val body: String,
    val attachments: List<String>,
    val receivedAt: Instant
)