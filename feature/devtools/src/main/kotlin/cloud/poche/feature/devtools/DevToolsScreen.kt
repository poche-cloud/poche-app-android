package cloud.poche.feature.devtools

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
internal fun DevToolsScreen(
    modifier: Modifier = Modifier,
) {
    DevToolsScreenContent(modifier = modifier)
}

@Composable
private fun DevToolsScreenContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "Dev Tools")
    }
}
