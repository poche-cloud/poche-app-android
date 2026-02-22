package cloud.poche.feature.devtools.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import cloud.poche.feature.devtools.DevToolsScreen
import kotlinx.serialization.Serializable

@Serializable
data object DevToolsRoute

fun NavController.navigateToDevTools(navOptions: NavOptions? = null) {
    navigate(DevToolsRoute, navOptions)
}

fun NavGraphBuilder.devToolsScreen() {
    composable<DevToolsRoute> {
        DevToolsScreen()
    }
}
