@file:Suppress("TooManyFunctions")

package cloud.poche.feature.capture

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cloud.poche.core.model.MemoType
import cloud.poche.core.ui.R
import coil3.compose.AsyncImage
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CaptureScreen(
    memoType: MemoType,
    onCaptureComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CaptureViewModel = hiltViewModel(),
) {
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val photoUiState by viewModel.photoUiState.collectAsStateWithLifecycle()
    val voiceUiState by viewModel.voiceUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CaptureEvent.SaveSuccess -> {
                    snackbarHostState.showSnackbar(context.getString(R.string.memo_saved))
                    onCaptureComplete()
                }

                is CaptureEvent.ShowError ->
                    snackbarHostState.showSnackbar(event.message.asString(context))
            }
        }
    }

    val titleRes = when (memoType) {
        MemoType.TEXT -> R.string.capture_title_text
        MemoType.PHOTO -> R.string.capture_title_photo
        MemoType.VOICE -> R.string.capture_title_voice
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(titleRes)) },
                navigationIcon = {
                    IconButton(onClick = onCaptureComplete) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        when (memoType) {
            MemoType.TEXT -> MemoCaptureContent(
                isSaving = isSaving,
                onSave = viewModel::saveMemo,
                modifier = Modifier.padding(innerPadding),
            )

            MemoType.PHOTO -> PhotoCaptureContent(
                uiState = photoUiState,
                onPhotoCaptured = viewModel::onPhotoCaptured,
                onClearPhoto = viewModel::clearPhoto,
                onSave = viewModel::savePhoto,
                modifier = Modifier.padding(innerPadding),
            )

            MemoType.VOICE -> VoiceCaptureContent(
                uiState = voiceUiState,
                onStartRecording = { viewModel.startRecording(it) },
                onStopRecording = viewModel::stopRecording,
                onPlay = viewModel::playRecording,
                onPause = viewModel::pausePlayback,
                onDiscard = viewModel::discardRecording,
                onSave = viewModel::saveVoiceMemo,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

// region Memo

@Composable
private fun MemoCaptureContent(isSaving: Boolean, onSave: (String) -> Unit, modifier: Modifier = Modifier) {
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
            placeholder = { Text(stringResource(R.string.capture_input_placeholder)) },
            enabled = !isSaving,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onSave(content) },
            modifier = Modifier.fillMaxWidth(),
            enabled = content.isNotBlank() && !isSaving,
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Text(stringResource(R.string.common_save))
            }
        }
    }
}

// endregion

// region Photo

@Composable
private fun PhotoCaptureContent(
    uiState: PhotoCaptureUiState,
    onPhotoCaptured: (Uri) -> Unit,
    onClearPhoto: () -> Unit,
    onSave: (Uri, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is PhotoCaptureUiState.Picker -> PhotoPickerView(
            onPhotoCaptured = onPhotoCaptured,
            modifier = modifier,
        )

        is PhotoCaptureUiState.Preview -> PhotoPreviewView(
            imageUri = uiState.imageUri,
            onRetake = onClearPhoto,
            onSave = onSave,
            modifier = modifier,
        )

        is PhotoCaptureUiState.Saving -> {
            Column(
                modifier = modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun PhotoPickerView(onPhotoCaptured: (Uri) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            tempImageUri?.let { onPhotoCaptured(it) }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let { onPhotoCaptured(it) }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            val file = File.createTempFile("photo_", ".jpg", context.cacheDir)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )
            tempImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.AddAPhoto,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.capture_photo_take))
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = { galleryLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.capture_photo_pick))
        }
    }
}

@Composable
private fun PhotoPreviewView(
    imageUri: Uri,
    onRetake: () -> Unit,
    onSave: (Uri, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var caption by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize(),
    ) {
        AsyncImage(
            model = imageUri,
            contentDescription = stringResource(R.string.capture_title_photo),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            contentScale = ContentScale.Fit,
        )
        OutlinedTextField(
            value = caption,
            onValueChange = { caption = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text(stringResource(R.string.capture_caption_placeholder)) },
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            OutlinedButton(
                onClick = {
                    caption = ""
                    onRetake()
                },
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.capture_photo_retake))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = { onSave(imageUri, caption) },
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.common_save))
            }
        }
    }
}

// endregion

// region Voice

@Composable
private fun VoiceCaptureContent(
    uiState: VoiceCaptureUiState,
    onStartRecording: (Context) -> Unit,
    onStopRecording: () -> Unit,
    onPlay: (String, Long) -> Unit,
    onPause: (String, Long) -> Unit,
    onDiscard: () -> Unit,
    onSave: (String, Long, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasPermission = granted
        if (granted) {
            onStartRecording(context)
        }
    }

    when (uiState) {
        is VoiceCaptureUiState.Ready -> VoiceReadyView(
            onStartRecording = {
                if (hasPermission) {
                    onStartRecording(context)
                } else {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            modifier = modifier,
        )

        is VoiceCaptureUiState.Recording -> VoiceRecordingView(
            elapsedMs = uiState.elapsedMs,
            onStop = onStopRecording,
            modifier = modifier,
        )

        is VoiceCaptureUiState.Recorded -> VoiceRecordedView(
            filePath = uiState.filePath,
            durationMs = uiState.durationMs,
            onPlay = onPlay,
            onDiscard = onDiscard,
            onSave = onSave,
            modifier = modifier,
        )

        is VoiceCaptureUiState.Playing -> VoicePlayingView(
            filePath = uiState.filePath,
            durationMs = uiState.durationMs,
            positionMs = uiState.positionMs,
            onPause = onPause,
            onDiscard = onDiscard,
            onSave = onSave,
            modifier = modifier,
        )

        is VoiceCaptureUiState.Saving -> {
            Column(
                modifier = modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun VoiceReadyView(onStartRecording: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.capture_voice_ready),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Surface(
            onClick = onStartRecording,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(80.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = stringResource(R.string.capture_voice_start),
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier
                    .padding(20.dp)
                    .size(40.dp),
            )
        }
    }
}

@Composable
private fun VoiceRecordingView(elapsedMs: Long, onStop: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = formatDuration(elapsedMs),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.capture_voice_recording),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Surface(
            onClick = onStop,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
                3.dp,
                MaterialTheme.colorScheme.error,
            ),
            modifier = Modifier.size(80.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = stringResource(R.string.capture_voice_stop),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .padding(20.dp)
                    .size(40.dp),
            )
        }
    }
}

@Composable
private fun VoiceRecordedView(
    filePath: String,
    durationMs: Long,
    onPlay: (String, Long) -> Unit,
    onDiscard: () -> Unit,
    onSave: (String, Long, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var caption by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Default.AudioFile,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = formatDuration(durationMs),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(24.dp))
        FilledIconButton(
            onClick = { onPlay(filePath, durationMs) },
            modifier = Modifier.size(64.dp),
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = stringResource(R.string.capture_voice_play),
                modifier = Modifier.size(36.dp),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        OutlinedTextField(
            value = caption,
            onValueChange = { caption = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.capture_caption_placeholder)) },
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = {
                    caption = ""
                    onDiscard()
                },
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.capture_voice_retake))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = { onSave(filePath, durationMs, caption) },
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.common_save))
            }
        }
    }
}

@Composable
private fun VoicePlayingView(
    filePath: String,
    durationMs: Long,
    positionMs: Long,
    onPause: (String, Long) -> Unit,
    onDiscard: () -> Unit,
    onSave: (String, Long, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var caption by rememberSaveable { mutableStateOf("") }
    val progress = if (durationMs > 0) positionMs.toFloat() / durationMs else 0f

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Default.AudioFile,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "${formatDuration(positionMs)} / ${formatDuration(durationMs)}",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(24.dp))
        FilledIconButton(
            onClick = { onPause(filePath, durationMs) },
            modifier = Modifier.size(64.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Pause,
                contentDescription = stringResource(R.string.capture_voice_pause),
                modifier = Modifier.size(36.dp),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        OutlinedTextField(
            value = caption,
            onValueChange = { caption = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.capture_caption_placeholder)) },
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = {
                    caption = ""
                    onDiscard()
                },
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.capture_voice_retake))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = { onSave(filePath, durationMs, caption) },
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.common_save))
            }
        }
    }
}

// endregion

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = (totalSeconds / 60).toString().padStart(2, '0')
    val seconds = (totalSeconds % 60).toString().padStart(2, '0')
    return "$minutes:$seconds"
}
