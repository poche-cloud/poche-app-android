package cloud.poche.feature.capture

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cloud.poche.core.domain.usecase.SaveMemoUseCase
import cloud.poche.core.domain.usecase.SavePhotoMemoUseCase
import cloud.poche.core.domain.usecase.SaveVoiceMemoUseCase
import cloud.poche.core.model.Memo
import cloud.poche.core.model.MemoType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
internal class CaptureViewModel @Inject constructor(
    private val saveMemoUseCase: SaveMemoUseCase,
    private val savePhotoMemoUseCase: SavePhotoMemoUseCase,
    private val saveVoiceMemoUseCase: SaveVoiceMemoUseCase,
) : ViewModel() {

    private val _events = MutableSharedFlow<CaptureEvent>()
    val events = _events.asSharedFlow()

    // region Memo
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    fun saveMemo(content: String) {
        if (content.isBlank()) {
            viewModelScope.launch {
                _events.emit(CaptureEvent.ShowError("内容を入力してください"))
            }
            return
        }
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val memo = Memo(
                    id = UUID.randomUUID().toString(),
                    content = content.trim(),
                    type = MemoType.TEXT,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                )
                saveMemoUseCase(memo)
                _events.emit(CaptureEvent.SaveSuccess)
            } catch (e: Exception) {
                _events.emit(CaptureEvent.ShowError("保存に失敗しました"))
            } finally {
                _isSaving.value = false
            }
        }
    }
    // endregion

    // region Photo
    private val _photoUiState = MutableStateFlow<PhotoCaptureUiState>(PhotoCaptureUiState.Picker)
    val photoUiState: StateFlow<PhotoCaptureUiState> = _photoUiState.asStateFlow()

    fun onPhotoCaptured(uri: Uri) {
        _photoUiState.value = PhotoCaptureUiState.Preview(uri)
    }

    fun clearPhoto() {
        _photoUiState.value = PhotoCaptureUiState.Picker
    }

    fun savePhoto(uri: Uri, caption: String) {
        viewModelScope.launch {
            _photoUiState.value = PhotoCaptureUiState.Saving
            try {
                savePhotoMemoUseCase(
                    filePath = uri.toString(),
                    caption = caption.trim(),
                )
                _events.emit(CaptureEvent.SaveSuccess)
            } catch (e: Exception) {
                _photoUiState.value = PhotoCaptureUiState.Preview(uri)
                _events.emit(CaptureEvent.ShowError("保存に失敗しました"))
            }
        }
    }
    // endregion

    // region Voice
    private val _voiceUiState = MutableStateFlow<VoiceCaptureUiState>(VoiceCaptureUiState.Ready)
    val voiceUiState: StateFlow<VoiceCaptureUiState> = _voiceUiState.asStateFlow()

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var recordingTimerJob: Job? = null
    private var playbackTimerJob: Job? = null
    private var recordingStartTimeMs: Long = 0L
    private var currentRecordingPath: String? = null

    fun startRecording(context: Context) {
        viewModelScope.launch {
            try {
                val outputFile = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
                currentRecordingPath = outputFile.absolutePath

                @Suppress("DEPRECATION")
                recorder = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    MediaRecorder()
                }).apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioSamplingRate(44100)
                    setAudioEncodingBitRate(128000)
                    setOutputFile(outputFile.absolutePath)
                    prepare()
                    start()
                }

                recordingStartTimeMs = System.currentTimeMillis()
                recordingTimerJob = viewModelScope.launch {
                    while (true) {
                        val elapsed = System.currentTimeMillis() - recordingStartTimeMs
                        _voiceUiState.value = VoiceCaptureUiState.Recording(elapsedMs = elapsed)
                        delay(100)
                    }
                }
            } catch (e: Exception) {
                _events.emit(CaptureEvent.ShowError("録音の開始に失敗しました"))
                _voiceUiState.value = VoiceCaptureUiState.Ready
            }
        }
    }

    fun stopRecording() {
        recordingTimerJob?.cancel()
        recordingTimerJob = null
        val durationMs = System.currentTimeMillis() - recordingStartTimeMs

        try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
        } catch (e: Exception) {
            recorder = null
        }

        val path = currentRecordingPath
        if (path != null) {
            _voiceUiState.value = VoiceCaptureUiState.Recorded(
                filePath = path,
                durationMs = durationMs,
            )
        } else {
            _voiceUiState.value = VoiceCaptureUiState.Ready
            viewModelScope.launch {
                _events.emit(CaptureEvent.ShowError("録音に失敗しました"))
            }
        }
    }

    fun playRecording(filePath: String, totalDurationMs: Long) {
        viewModelScope.launch {
            try {
                player?.release()
                player = MediaPlayer().apply {
                    setDataSource(filePath)
                    prepare()
                    start()
                }

                playbackTimerJob = viewModelScope.launch {
                    while (player?.isPlaying == true) {
                        val position = player?.currentPosition?.toLong() ?: 0L
                        _voiceUiState.value = VoiceCaptureUiState.Playing(
                            filePath = filePath,
                            durationMs = totalDurationMs,
                            positionMs = position,
                        )
                        delay(100)
                    }
                    _voiceUiState.value = VoiceCaptureUiState.Recorded(
                        filePath = filePath,
                        durationMs = totalDurationMs,
                    )
                }

                player?.setOnCompletionListener {
                    playbackTimerJob?.cancel()
                    _voiceUiState.value = VoiceCaptureUiState.Recorded(
                        filePath = filePath,
                        durationMs = totalDurationMs,
                    )
                }
            } catch (e: Exception) {
                _events.emit(CaptureEvent.ShowError("再生に失敗しました"))
            }
        }
    }

    fun pausePlayback(filePath: String, totalDurationMs: Long) {
        playbackTimerJob?.cancel()
        player?.apply {
            if (isPlaying) pause()
            release()
        }
        player = null
        _voiceUiState.value = VoiceCaptureUiState.Recorded(
            filePath = filePath,
            durationMs = totalDurationMs,
        )
    }

    fun discardRecording() {
        playbackTimerJob?.cancel()
        player?.release()
        player = null
        currentRecordingPath?.let { File(it).delete() }
        currentRecordingPath = null
        _voiceUiState.value = VoiceCaptureUiState.Ready
    }

    fun saveVoiceMemo(filePath: String, durationMs: Long, caption: String) {
        viewModelScope.launch {
            _voiceUiState.value = VoiceCaptureUiState.Saving
            try {
                saveVoiceMemoUseCase(
                    filePath = filePath,
                    durationMs = durationMs,
                    caption = caption.trim(),
                )
                _events.emit(CaptureEvent.SaveSuccess)
            } catch (e: Exception) {
                _voiceUiState.value = VoiceCaptureUiState.Recorded(
                    filePath = filePath,
                    durationMs = durationMs,
                )
                _events.emit(CaptureEvent.ShowError("保存に失敗しました"))
            }
        }
    }
    // endregion

    override fun onCleared() {
        super.onCleared()
        recordingTimerJob?.cancel()
        playbackTimerJob?.cancel()
        recorder?.release()
        player?.release()
    }
}

internal sealed interface CaptureEvent {
    data object SaveSuccess : CaptureEvent
    data class ShowError(val message: String) : CaptureEvent
}

internal sealed interface PhotoCaptureUiState {
    data object Picker : PhotoCaptureUiState
    data class Preview(val imageUri: Uri) : PhotoCaptureUiState
    data object Saving : PhotoCaptureUiState
}

internal sealed interface VoiceCaptureUiState {
    data object Ready : VoiceCaptureUiState
    data class Recording(val elapsedMs: Long) : VoiceCaptureUiState
    data class Recorded(val filePath: String, val durationMs: Long) : VoiceCaptureUiState
    data class Playing(val filePath: String, val durationMs: Long, val positionMs: Long) : VoiceCaptureUiState
    data object Saving : VoiceCaptureUiState
}
