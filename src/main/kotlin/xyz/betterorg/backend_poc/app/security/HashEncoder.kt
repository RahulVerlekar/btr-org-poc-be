package xyz.betterorg.backend_poc.app.security

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class HashEncoder {

    private val bcrypt = BCryptPasswordEncoder()

    fun encode(input: String): String {
        return bcrypt.encode(input)
    }

    fun matches(raw: String, hash: String?): Boolean = bcrypt.matches(raw, hash)
}