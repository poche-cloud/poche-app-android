package cloud.poche.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import cloud.poche.core.model.MemoType
import cloud.poche.feature.home.HomeScreen
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

fun NavController.navigateToHome(navOptions: NavOptions? = null) {
    navigate(HomeRoute, navOptions)
}

fun NavGraphBuilder.homeScreen(onMemoClick: (String) -> Unit, onNavigateToCapture: (MemoType) -> Unit) {
    composable<HomeRoute> {
        HomeScreen(
            onMemoClick = onMemoClick,
            onNavigateToCapture = onNavigateToCapture,
        )
    }
}
