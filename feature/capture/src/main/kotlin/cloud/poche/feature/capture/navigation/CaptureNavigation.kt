package cloud.poche.feature.capture.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import cloud.poche.feature.capture.CaptureScreen
import kotlinx.serialization.Serializable

@Serializable
data object CaptureRoute

fun NavController.navigateToCapture(navOptions: NavOptions? = null) {
    navigate(CaptureRoute, navOptions)
}

fun NavGraphBuilder.captureScreen(
    onCaptureComplete: () -> Unit,
) {
    composable<CaptureRoute> {
        CaptureScreen(onCaptureComplete = onCaptureComplete)
    }
}
