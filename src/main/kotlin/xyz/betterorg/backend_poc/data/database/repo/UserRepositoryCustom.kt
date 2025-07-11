package xyz.betterorg.backend_poc.data.database.repo

interface UserRepositoryCustom {
    fun updateSyncStatusById(userId: String, syncStatus: String): Long
}

