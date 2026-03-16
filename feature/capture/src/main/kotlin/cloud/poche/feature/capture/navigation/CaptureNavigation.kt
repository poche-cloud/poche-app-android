package cloud.poche.feature.capture.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import cloud.poche.core.model.MemoType
import cloud.poche.feature.capture.CaptureScreen
import kotlinx.serialization.Serializable

@Serializable
data class CaptureRoute(val memoType: String = MemoType.TEXT.name)

fun NavController.navigateToCapture(
    memoType: MemoType = MemoType.TEXT,
    navOptions: NavOptions? = null,
) {
    navigate(CaptureRoute(memoType = memoType.name), navOptions)
}

fun NavGraphBuilder.captureScreen(
    onCaptureComplete: () -> Unit,
) {
    composable<CaptureRoute>(
        deepLinks = listOf(
            navDeepLink { uriPattern = "poche://capture?type={memoType}" },
        ),
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<CaptureRoute>()
        val memoType = runCatching { MemoType.valueOf(route.memoType) }
            .getOrDefault(MemoType.TEXT)
        CaptureScreen(
            memoType = memoType,
            onCaptureComplete = onCaptureComplete,
        )
    }
}
