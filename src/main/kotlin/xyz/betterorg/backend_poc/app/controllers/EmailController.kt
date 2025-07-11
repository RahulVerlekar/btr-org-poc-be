package xyz.betterorg.backend_poc.app.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.beans.factory.annotation.Autowired
import xyz.betterorg.backend_poc.data.database.repo.EmailRepository
import com.fasterxml.jackson.databind.ObjectMapper
import java.nio.charset.StandardCharsets

@RestController
class EmailController @Autowired constructor(
    private val emailRepository: EmailRepository,
    private val objectMapper: ObjectMapper
) {
    @GetMapping("/emails/download")
    fun downloadAllEmails(): ResponseEntity<ByteArray> {
        val emails = emailRepository.findAll()
        val json = objectMapper.writeValueAsString(emails)
        val filename = "emails.json"
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_OCTET_STREAM
            set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\"")
        }
        return ResponseEntity
            .ok()
            .headers(headers)
            .body(json.toByteArray(StandardCharsets.UTF_8))
    }
}

