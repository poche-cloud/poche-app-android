package cloud.poche.core.model

data class UserData(
    val isOnboardingCompleted: Boolean,
    val userId: String?,
    val darkThemeConfig: DarkThemeConfig,
    val aiConsent: Boolean = false,
)
