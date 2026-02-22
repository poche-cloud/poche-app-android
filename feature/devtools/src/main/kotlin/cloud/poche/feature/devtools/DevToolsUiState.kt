package cloud.poche.feature.devtools

internal data class DevToolsUiState(
    val appVersion: String = "",
    val buildNumber: String = "",
    val packageName: String = "",
    val buildType: String = "",
    val flavor: String = "",
    val featureFlags: Map<String, Boolean> = emptyMap(),
)
