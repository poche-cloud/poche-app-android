package cloud.poche.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cloud.poche.app.navigation.PocheNavHost
import cloud.poche.core.designsystem.theme.PocheTheme
import cloud.poche.core.domain.usecase.ForceUpdateStatus
import cloud.poche.core.model.DarkThemeConfig
import cloud.poche.core.model.MemoType
import cloud.poche.core.ui.ForceUpdateDialog
import cloud.poche.feature.capture.ShareIntentHandler
import cloud.poche.feature.capture.navigation.navigateToCapture
import cloud.poche.feature.devtools.component.DebugTagOverlay
import cloud.poche.feature.home.navigation.HomeRoute
import cloud.poche.feature.home.navigation.navigateToHome
import cloud.poche.feature.settings.navigation.SettingsRoute
import cloud.poche.feature.settings.navigation.navigateToSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()
    private val incomingShareUrl = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIncomingShare(intent)
        enableEdgeToEdge()
        val currentVersion = packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0.0"

        setContent {
            val darkThemeConfig by viewModel.darkThemeConfig.collectAsStateWithLifecycle()
            val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle()
            val forceUpdateStatus by viewModel.forceUpdateStatus.collectAsStateWithLifecycle()
            val sharedUrl by incomingShareUrl.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                viewModel.checkForceUpdate(currentVersion)
            }

            val darkTheme = when (darkThemeConfig) {
                DarkThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme()
                DarkThemeConfig.LIGHT -> false
                DarkThemeConfig.DARK -> true
            }

            PocheTheme(darkTheme = darkTheme) {
                if (forceUpdateStatus == ForceUpdateStatus.UPDATE_REQUIRED) {
                    ForceUpdateDialog(packageName = packageName)
                } else if (isOnboardingCompleted != null) {
                    PocheApp(
                        isOnboardingCompleted = isOnboardingCompleted!!,
                        sharedUrl = sharedUrl,
                        onShareHandled = { incomingShareUrl.value = null },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingShare(intent)
    }

    private fun handleIncomingShare(intent: Intent?) {
        incomingShareUrl.value = intent?.let(ShareIntentHandler::extractUrl)
    }
}

@Composable
fun PocheApp(isOnboardingCompleted: Boolean, sharedUrl: String? = null, onShareHandled: () -> Unit = {}) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    LaunchedEffect(sharedUrl, isOnboardingCompleted) {
        if (sharedUrl != null && isOnboardingCompleted) {
            navController.navigateToCapture(
                memoType = MemoType.TEXT,
                sharedUrl = sharedUrl,
                navOptions = androidx.navigation.navOptions {
                    launchSingleTop = true
                },
            )
            onShareHandled()
        }
    }

    val showBottomBar = currentDestination?.hasRoute<HomeRoute>() == true ||
        currentDestination?.hasRoute<SettingsRoute>() == true

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentDestination.hasRoute<HomeRoute>(),
                            onClick = {
                                navController.navigateToHome(
                                    androidx.navigation.navOptions {
                                        popUpTo<HomeRoute> { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    },
                                )
                            },
                            icon = { Icon(Icons.Default.Home, contentDescription = "ホーム") },
                            label = { Text("ホーム") },
                        )
                        NavigationBarItem(
                            selected = currentDestination.hasRoute<SettingsRoute>(),
                            onClick = {
                                navController.navigateToSettings(
                                    androidx.navigation.navOptions {
                                        popUpTo<HomeRoute> { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    },
                                )
                            },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "設定") },
                            label = { Text("設定") },
                        )
                    }
                }
            },
        ) { innerPadding ->
            PocheNavHost(
                navController = navController,
                isOnboardingCompleted = isOnboardingCompleted,
                modifier = Modifier.padding(innerPadding),
            )
        }

        if (BuildConfig.DEBUG) {
            DebugTagOverlay(
                flavor = BuildConfig.FLAVOR,
                version = BuildConfig.VERSION_NAME,
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }
    }
}
