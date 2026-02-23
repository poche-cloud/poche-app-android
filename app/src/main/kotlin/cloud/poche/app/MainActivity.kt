package cloud.poche.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cloud.poche.app.navigation.PocheNavHost
import cloud.poche.core.designsystem.theme.PocheTheme
import cloud.poche.core.model.DarkThemeConfig
import cloud.poche.feature.home.navigation.HomeRoute
import cloud.poche.feature.home.navigation.navigateToHome
import cloud.poche.feature.settings.navigation.SettingsRoute
import cloud.poche.feature.settings.navigation.navigateToSettings
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkThemeConfig by viewModel.darkThemeConfig.collectAsStateWithLifecycle()

            val darkTheme = when (darkThemeConfig) {
                DarkThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme()
                DarkThemeConfig.LIGHT -> false
                DarkThemeConfig.DARK -> true
            }

            PocheTheme(darkTheme = darkTheme) {
                PocheApp()
            }
        }
    }
}

@Composable
fun PocheApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.hasRoute<HomeRoute>() == true ||
        currentDestination?.hasRoute<SettingsRoute>() == true

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
            modifier = Modifier.padding(innerPadding),
        )
    }
}
