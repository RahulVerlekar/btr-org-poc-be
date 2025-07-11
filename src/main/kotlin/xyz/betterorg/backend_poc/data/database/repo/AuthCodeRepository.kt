package xyz.betterorg.backend_poc.data.database.repo

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import xyz.betterorg.backend_poc.data.database.entity.AuthCode

interface AuthCodeRepository : MongoRepository<AuthCode, ObjectId> {
    fun findByUserId(id: ObjectId): AuthCode?
}