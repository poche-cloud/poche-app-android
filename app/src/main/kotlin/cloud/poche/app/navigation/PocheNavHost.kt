package cloud.poche.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import cloud.poche.feature.capture.navigation.captureScreen
import cloud.poche.feature.capture.navigation.navigateToCapture
import cloud.poche.feature.home.navigation.HomeRoute
import cloud.poche.feature.home.navigation.homeScreen
import cloud.poche.feature.memo.navigation.memoDetailScreen
import cloud.poche.feature.memo.navigation.navigateToMemoDetail
import cloud.poche.feature.onboarding.navigation.onboardingScreen
import cloud.poche.feature.settings.navigation.settingsScreen

@Composable
fun PocheNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
    ) {
        homeScreen(
            onMemoClick = navController::navigateToMemoDetail,
        )
        captureScreen(
            onCaptureComplete = { navController.popBackStack() },
        )
        memoDetailScreen(
            onBackClick = { navController.popBackStack() },
        )
        settingsScreen()
        onboardingScreen(
            onComplete = { navController.popBackStack() },
        )
    }
}
