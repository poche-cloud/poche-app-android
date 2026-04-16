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
data class CaptureRoute(val memoType: String = MemoType.TEXT.name, val sharedUrl: String? = null)

fun NavController.navigateToCapture(
    memoType: MemoType = MemoType.TEXT,
    sharedUrl: String? = null,
    navOptions: NavOptions? = null,
) {
    navigate(CaptureRoute(memoType = memoType.name, sharedUrl = sharedUrl), navOptions)
}

fun NavGraphBuilder.captureScreen(onCaptureComplete: () -> Unit) {
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
            sharedUrl = route.sharedUrl,
        )
    }
}
