package cloud.poche.core.domain.usecase

import cloud.poche.core.domain.repository.RemoteConfigManager
import javax.inject.Inject

class CheckForceUpdateUseCase @Inject constructor(private val remoteConfigManager: RemoteConfigManager) {
    suspend operator fun invoke(currentVersion: String): ForceUpdateStatus = try {
        remoteConfigManager.fetchAndActivate()
        val minVersion = remoteConfigManager.getMinAppVersion()
        if (compareVersions(currentVersion, minVersion) < 0) {
            ForceUpdateStatus.UPDATE_REQUIRED
        } else {
            ForceUpdateStatus.UP_TO_DATE
        }
    } catch (_: Exception) {
        ForceUpdateStatus.CHECK_FAILED
    }

    private fun compareVersions(current: String, min: String): Int {
        val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
        val minParts = min.split(".").map { it.toIntOrNull() ?: 0 }
        val maxLength = maxOf(currentParts.size, minParts.size)
        for (i in 0 until maxLength) {
            val c = currentParts.getOrElse(i) { 0 }
            val m = minParts.getOrElse(i) { 0 }
            if (c != m) return c.compareTo(m)
        }
        return 0
    }
}

enum class ForceUpdateStatus {
    UPDATE_REQUIRED,
    UP_TO_DATE,
    CHECK_FAILED,
}
