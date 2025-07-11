package xyz.betterorg.backend_poc.data.database.repo

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import xyz.betterorg.backend_poc.data.database.entity.User

interface UserRepository : MongoRepository<User, ObjectId>, UserRepositoryCustom {
    fun findByEmail(email: String): User?
}