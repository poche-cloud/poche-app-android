package cloud.poche.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import cloud.poche.feature.capture.navigation.captureScreen
import cloud.poche.feature.capture.navigation.navigateToCapture
import cloud.poche.feature.devtools.navigation.devToolsScreen
import cloud.poche.feature.devtools.navigation.navigateToDevTools
import cloud.poche.feature.home.navigation.HomeRoute
import cloud.poche.feature.home.navigation.homeScreen
import cloud.poche.feature.home.navigation.navigateToHome
import cloud.poche.feature.memo.navigation.memoDetailScreen
import cloud.poche.feature.memo.navigation.navigateToMemoDetail
import cloud.poche.feature.onboarding.navigation.OnboardingRoute
import cloud.poche.feature.onboarding.navigation.onboardingScreen
import cloud.poche.feature.settings.navigation.settingsScreen

@Composable
fun PocheNavHost(
    navController: NavHostController,
    isOnboardingCompleted: Boolean,
    modifier: Modifier = Modifier,
) {
    val startDestination: Any = if (isOnboardingCompleted) HomeRoute else OnboardingRoute

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        homeScreen(
            onMemoClick = navController::navigateToMemoDetail,
            onNavigateToCapture = { memoType ->
                navController.navigateToCapture(memoType = memoType)
            },
        )
        captureScreen(
            onCaptureComplete = { navController.popBackStack() },
        )
        memoDetailScreen(
            onBackClick = { navController.popBackStack() },
        )
        settingsScreen(
            navController = navController,
            onSignedOut = {
                navController.navigateToHome(
                    androidx.navigation.navOptions {
                        popUpTo<HomeRoute> { inclusive = true }
                    },
                )
            },
            onAccountDeleted = {
                navController.navigateToHome(
                    androidx.navigation.navOptions {
                        popUpTo<HomeRoute> { inclusive = true }
                    },
                )
            },
            onDevToolsClick = { navController.navigateToDevTools() },
        )
        onboardingScreen(
            onComplete = {
                navController.navigateToHome(
                    androidx.navigation.navOptions {
                        popUpTo<OnboardingRoute> { inclusive = true }
                    },
                )
            },
        )
        devToolsScreen(
            onBackClick = { navController.popBackStack() },
        )
    }
}
