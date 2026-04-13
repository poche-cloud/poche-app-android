package cloud.poche.feature.memo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cloud.poche.core.model.MemoType
import cloud.poche.core.ui.AudioPlayer
import cloud.poche.core.ui.R
import cloud.poche.core.ui.UiText
import coil3.compose.AsyncImage

@Composable
internal fun MemoDetailScreen(
    @Suppress("UnusedParameter") memoId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MemoDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MemoDetailEvent.Saved ->
                    snackbarHostState.showSnackbar(context.getString(R.string.memo_saved))

                is MemoDetailEvent.ShowError ->
                    snackbarHostState.showSnackbar(event.message.asString(context))
            }
        }
    }

    MemoDetailScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onSave = viewModel::updateContent,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemoDetailScreenContent(
    uiState: MemoDetailUiState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onSave: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isEditing by rememberSaveable { mutableStateOf(false) }
    var editContent by rememberSaveable { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditing) {
                            stringResource(R.string.memo_edit_title)
                        } else {
                            stringResource(R.string.memo_detail_title)
                        },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    if (uiState is MemoDetailUiState.Success) {
                        if (isEditing) {
                            IconButton(onClick = { isEditing = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.cancel),
                                )
                            }
                        } else {
                            IconButton(onClick = {
                                editContent = uiState.memo.content
                                isEditing = true
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.memo_edit_title),
                                )
                            }
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (isEditing) {
                ExtendedFloatingActionButton(
                    onClick = {
                        onSave(editContent)
                        isEditing = false
                    },
                    icon = { Icon(Icons.Default.Save, contentDescription = null) },
                    text = { Text(stringResource(R.string.memo_save)) },
                )
            }
        },
    ) { innerPadding ->
        when (uiState) {
            is MemoDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is MemoDetailUiState.Success -> {
                if (isEditing) {
                    OutlinedTextField(
                        value = editContent,
                        onValueChange = { editContent = it },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp),
                        placeholder = { Text(stringResource(R.string.memo_input_placeholder)) },
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                    ) {
                        if (uiState.memo.type == MemoType.PHOTO && uiState.memo.filePath != null) {
                            AsyncImage(
                                model = uiState.memo.filePath,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .padding(bottom = 16.dp),
                                contentScale = ContentScale.Crop,
                            )
                        }

                        if (uiState.memo.type == MemoType.VOICE && uiState.memo.filePath != null) {
                            AudioPlayer(
                                filePath = uiState.memo.filePath!!,
                                modifier = Modifier.padding(bottom = 16.dp),
                            )
                        }

                        Text(
                            text = uiState.memo.content,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }

            is MemoDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.message.asString(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBackClick) {
                            Text(stringResource(R.string.back))
                        }
                    }
                }
            }
        }
    }
}
