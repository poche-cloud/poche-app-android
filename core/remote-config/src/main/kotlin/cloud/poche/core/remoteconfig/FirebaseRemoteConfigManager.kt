package cloud.poche.core.remoteconfig

import cloud.poche.core.domain.repository.RemoteConfigManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseRemoteConfigManager @Inject constructor(private val remoteConfig: FirebaseRemoteConfig) :
    RemoteConfigManager {

    override suspend fun fetchAndActivate(): Boolean = remoteConfig.fetchAndActivate().await()

    override fun getMinAppVersion(): String = remoteConfig.getString(KEY_MIN_APP_VERSION)

    private companion object {
        const val KEY_MIN_APP_VERSION = "min_app_version"
    }
}
