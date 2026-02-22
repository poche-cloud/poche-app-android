package cloud.poche.feature.onboarding.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import cloud.poche.feature.onboarding.OnboardingScreen
import kotlinx.serialization.Serializable

@Serializable
data object OnboardingRoute

fun NavController.navigateToOnboarding(navOptions: NavOptions? = null) {
    navigate(OnboardingRoute, navOptions)
}

fun NavGraphBuilder.onboardingScreen(
    onComplete: () -> Unit,
) {
    composable<OnboardingRoute> {
        OnboardingScreen(onComplete = onComplete)
    }
}
