package cloud.poche.feature.capture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cloud.poche.core.model.MemoType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CaptureScreen(
    memoType: MemoType,
    onCaptureComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = when (memoType) {
        MemoType.TEXT -> "メモ"
        MemoType.PHOTO -> "写真"
        MemoType.VOICE -> "音声メモ"
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onCaptureComplete) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "戻る",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when (memoType) {
            MemoType.TEXT -> MemoCaptureContent(
                onCaptureComplete = onCaptureComplete,
                modifier = Modifier.padding(innerPadding),
            )
            MemoType.PHOTO -> PlaceholderCaptureContent(
                icon = Icons.Default.CameraAlt,
                label = "写真撮影は準備中です",
                modifier = Modifier.padding(innerPadding),
            )
            MemoType.VOICE -> PlaceholderCaptureContent(
                icon = Icons.Default.Mic,
                label = "音声録音は準備中です",
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun MemoCaptureContent(
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
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            placeholder = { Text("メモを入力...") },
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onCaptureComplete,
            modifier = Modifier.fillMaxWidth(),
            enabled = content.isNotBlank(),
        ) {
            Text("保存")
        }
    }
}

@Composable
private fun PlaceholderCaptureContent(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
