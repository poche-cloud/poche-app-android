package cloud.poche.feature.memo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun MemoDetailScreen(
    memoId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MemoDetailContent(
        memoId = memoId,
        modifier = modifier,
    )
}

@Composable
private fun MemoDetailContent(
    memoId: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopStart,
    ) {
        Text(text = "Memo: $memoId")
    }
}
