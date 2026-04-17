package cloud.poche.feature.settings

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cloud.poche.core.auth.AuthManager
import cloud.poche.core.domain.usecase.GetUserDataUseCase
import cloud.poche.core.domain.usecase.SetAiConsentUseCase
import cloud.poche.core.ui.R
import cloud.poche.core.ui.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    getUserDataUseCase: GetUserDataUseCase,
    private val setAiConsentUseCase: SetAiConsentUseCase,
    private val authManager: AuthManager,
    @ApplicationContext context: Context,
) : ViewModel() {

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events = _events.asSharedFlow()

    private val appVersion: String = try {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
        packageInfo.versionName ?: "unknown"
    } catch (_: PackageManager.NameNotFoundException) {
        "unknown"
    }

    private val isDebugBuild: Boolean =
        context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0

    val uiState: StateFlow<SettingsUiState> =
        combine(
            getUserDataUseCase(),
            authManager.isSignedIn,
        ) { userData, isSignedIn ->
            SettingsUiState.Success(
                userId = userData.userId ?: authManager.currentUserId,
                isSignedIn = isSignedIn,
                darkThemeConfig = userData.darkThemeConfig,
                aiConsent = userData.aiConsent,
                appVersion = appVersion,
                isDebugBuild = isDebugBuild,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState.Loading,
        )

    fun setAiConsent(consented: Boolean) {
        viewModelScope.launch {
            setAiConsentUseCase(consented)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                authManager.signOut()
                _events.emit(SettingsEvent.SignedOut)
            } catch (e: Exception) {
                _events.emit(SettingsEvent.ShowError(UiText.StringResource(R.string.settings_sign_out_subtitle)))
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                authManager.deleteAccount()
                _events.emit(SettingsEvent.AccountDeleted)
            } catch (e: Exception) {
                _events.emit(SettingsEvent.ShowError(UiText.StringResource(R.string.settings_delete_account)))
            }
        }
    }
}

sealed interface SettingsEvent {
    data object SignedOut : SettingsEvent
    data object AccountDeleted : SettingsEvent
    data class ShowError(val message: UiText) : SettingsEvent
}
