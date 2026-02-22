package cloud.poche.feature.memo.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import cloud.poche.feature.memo.MemoDetailScreen
import kotlinx.serialization.Serializable

@Serializable
data class MemoDetailRoute(val memoId: String)

fun NavController.navigateToMemoDetail(memoId: String, navOptions: NavOptions? = null) {
    navigate(MemoDetailRoute(memoId), navOptions)
}

fun NavGraphBuilder.memoDetailScreen(
    onBackClick: () -> Unit,
) {
    composable<MemoDetailRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<MemoDetailRoute>()
        MemoDetailScreen(
            memoId = route.memoId,
            onBackClick = onBackClick,
        )
    }
}
