package xyz.betterorg.backend_poc.data.database.entity

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("users")
public class User(
        val name: String,
        val email: String,
        val password: String?,
        val title: String,
        val aboutYou: String,
        val token: String,
        val response: String,
        val syncStatus: String = "NOT_SYNCED",
        val createdAt: Instant,
        @Id
        @JsonSerialize(using= ToStringSerializer::class)
        val id: ObjectId = ObjectId.get()
)
