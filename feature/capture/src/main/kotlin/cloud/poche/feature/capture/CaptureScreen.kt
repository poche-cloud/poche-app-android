package cloud.poche.feature.capture

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun CaptureScreen(
    onCaptureComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CaptureScreenContent(
        onCaptureComplete = onCaptureComplete,
        modifier = modifier,
    )
}

@Composable
private fun CaptureScreenContent(
    onCaptureComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var content by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("メモを入力") },
            minLines = 5,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onCaptureComplete,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("保存")
        }
    }
}
