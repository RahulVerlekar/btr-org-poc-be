package xyz.betterorg.backend_poc.data.database.repo

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl @Autowired constructor(
    private val mongoTemplate: MongoTemplate
) : UserRepositoryCustom {
    override fun updateSyncStatusById(userId: String, syncStatus: String): Long {
        val query = Query(Criteria.where("_id").`is`(userId))
        val update = Update().set("syncStatus", syncStatus)
        val result = mongoTemplate.updateFirst(query, update, "users")
        return result.modifiedCount
    }
}
