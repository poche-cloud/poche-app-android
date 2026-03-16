package cloud.poche.feature.devtools

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
internal class DevToolsViewModel @Inject constructor(@ApplicationContext private val context: Context) : ViewModel() {

    private val _events = MutableSharedFlow<DevToolsEvent>()
    val events = _events.asSharedFlow()

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<DevToolsUiState> = _uiState.asStateFlow()

    private fun createInitialState(): DevToolsUiState {
        val packageInfo = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0),
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }

        val versionCode = packageInfo?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                it.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                it.versionCode.toString()
            }
        } ?: "unknown"

        return DevToolsUiState(
            appVersion = packageInfo?.versionName ?: "unknown",
            buildNumber = versionCode,
            packageName = context.packageName,
            buildType = inferBuildType(),
            flavor = inferFlavor(),
        )
    }

    private fun inferBuildType(): String =
        if (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            "debug"
        } else {
            "release"
        }

    private fun inferFlavor(): String {
        val packageName = context.packageName
        return when {
            packageName.endsWith(".dev") || packageName.contains(".dev.") -> "dev"
            packageName.endsWith(".stg") || packageName.contains(".stg.") -> "stg"
            else -> "prod"
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            try {
                context.cacheDir.deleteRecursively()
                context.codeCacheDir.deleteRecursively()

                val externalCacheDir: File? = context.externalCacheDir
                externalCacheDir?.deleteRecursively()

                _events.emit(DevToolsEvent.CacheClearSuccess)
            } catch (_: Exception) {
                _events.emit(DevToolsEvent.CacheClearError)
            }
        }
    }
}

internal sealed interface DevToolsEvent {
    data object CacheClearSuccess : DevToolsEvent
    data object CacheClearError : DevToolsEvent
}
