package cloud.poche.feature.home.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cloud.poche.core.model.MemoType
import cloud.poche.core.ui.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CaptureActionSheet(onDismiss: () -> Unit, onSelected: (MemoType) -> Unit, modifier: Modifier = Modifier) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        Column {
            CaptureOptionItem(
                icon = Icons.Default.NoteAlt,
                title = stringResource(R.string.home_capture_option_text),
                subtitle = stringResource(R.string.home_capture_option_text_desc),
                onClick = { onSelected(MemoType.TEXT) },
            )
            CaptureOptionItem(
                icon = Icons.Default.CameraAlt,
                title = stringResource(R.string.home_capture_option_photo),
                subtitle = stringResource(R.string.home_capture_option_photo_desc),
                onClick = { onSelected(MemoType.PHOTO) },
            )
            CaptureOptionItem(
                icon = Icons.Default.Mic,
                title = stringResource(R.string.home_capture_option_voice),
                subtitle = stringResource(R.string.home_capture_option_voice_desc),
                onClick = { onSelected(MemoType.VOICE) },
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CaptureOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        modifier = modifier.clickable(onClick = onClick),
        tonalElevation = 0.dp,
    )
}
