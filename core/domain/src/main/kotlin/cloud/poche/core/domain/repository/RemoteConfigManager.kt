package cloud.poche.core.domain.repository

interface RemoteConfigManager {
    suspend fun fetchAndActivate(): Boolean
    fun getMinAppVersion(): String
}
