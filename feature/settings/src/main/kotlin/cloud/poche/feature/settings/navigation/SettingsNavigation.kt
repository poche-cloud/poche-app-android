package cloud.poche.feature.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import cloud.poche.feature.settings.SettingsScreen
import cloud.poche.feature.settings.capture.CaptureSettingsScreen
import cloud.poche.feature.settings.data.DataManagementScreen
import cloud.poche.feature.settings.language.LanguageSettingsScreen
import cloud.poche.feature.settings.licenses.OssLicensesScreen
import cloud.poche.feature.settings.notification.NotificationSettingsScreen
import cloud.poche.feature.settings.theme.ThemeSettingsScreen
import kotlinx.serialization.Serializable

@Serializable
data object SettingsRoute

@Serializable
internal data object ThemeSettingsRoute

@Serializable
internal data object NotificationSettingsRoute

@Serializable
internal data object LanguageSettingsRoute

@Serializable
internal data object CaptureSettingsRoute

@Serializable
internal data object DataManagementRoute

@Serializable
internal data object OssLicensesRoute

fun NavController.navigateToSettings(navOptions: NavOptions? = null) {
    navigate(SettingsRoute, navOptions)
}

fun NavGraphBuilder.settingsScreen(
    navController: NavController,
    onSignedOut: () -> Unit,
    onAccountDeleted: () -> Unit,
    onDevToolsClick: () -> Unit = {},
) {
    composable<SettingsRoute> {
        SettingsScreen(
            onNavigateToTheme = { navController.navigate(ThemeSettingsRoute) },
            onNavigateToNotifications = { navController.navigate(NotificationSettingsRoute) },
            onNavigateToLanguage = { navController.navigate(LanguageSettingsRoute) },
            onNavigateToLicenses = { navController.navigate(OssLicensesRoute) },
            onNavigateToCaptureSettings = { navController.navigate(CaptureSettingsRoute) },
            onNavigateToDataManagement = { navController.navigate(DataManagementRoute) },
            onNavigateToDevTools = onDevToolsClick,
            onSignedOut = onSignedOut,
            onAccountDeleted = onAccountDeleted,
        )
    }
    composable<ThemeSettingsRoute> {
        ThemeSettingsScreen(
            onBackClick = { navController.popBackStack() },
        )
    }
    composable<NotificationSettingsRoute> {
        NotificationSettingsScreen(
            onBackClick = { navController.popBackStack() },
        )
    }
    composable<LanguageSettingsRoute> {
        LanguageSettingsScreen(
            onBackClick = { navController.popBackStack() },
        )
    }
    composable<CaptureSettingsRoute> {
        CaptureSettingsScreen(
            onBackClick = { navController.popBackStack() },
        )
    }
    composable<DataManagementRoute> {
        DataManagementScreen(
            onBackClick = { navController.popBackStack() },
        )
    }
    composable<OssLicensesRoute> {
        OssLicensesScreen(
            onBackClick = { navController.popBackStack() },
        )
    }
}
