package cloud.poche.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cloud.poche.core.model.Memo
import cloud.poche.core.model.MemoType
import cloud.poche.core.ui.MemoCard
import cloud.poche.feature.home.component.CaptureActionSheet
import cloud.poche.feature.home.component.QuickCaptureBar

@Composable
internal fun HomeScreen(
    onMemoClick: (String) -> Unit,
    onNavigateToCapture: (MemoType) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is HomeEvent.CaptureSuccess -> Unit
            }
        }
    }

    HomeScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onMemoClick = onMemoClick,
        onDeleteMemo = viewModel::deleteMemo,
        onQuickCapture = viewModel::quickCapture,
        onRetry = viewModel::retry,
        onNavigateToCapture = onNavigateToCapture,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    snackbarHostState: SnackbarHostState,
    onMemoClick: (String) -> Unit,
    onDeleteMemo: (String) -> Unit,
    onQuickCapture: (String) -> Unit,
    onRetry: () -> Unit,
    onNavigateToCapture: (MemoType) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showActionSheet by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("Poche") })
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Box(modifier = Modifier.weight(1f)) {
                when (uiState) {
                    is HomeUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is HomeUiState.Success -> {
                        if (uiState.memos.isEmpty()) {
                            EmptyState()
                        } else {
                            MemoList(
                                memos = uiState.memos,
                                onMemoClick = onMemoClick,
                                onDeleteMemo = onDeleteMemo,
                            )
                        }
                    }
                    is HomeUiState.Error -> {
                        ErrorState(
                            message = uiState.message,
                            onRetry = onRetry,
                        )
                    }
                }
            }

            QuickCaptureBar(
                onSubmit = onQuickCapture,
                onPlusPressed = { showActionSheet = true },
                isLoading = uiState is HomeUiState.Loading,
            )
        }
    }

    if (showActionSheet) {
        CaptureActionSheet(
            onDismiss = { showActionSheet = false },
            onSelected = { type ->
                showActionSheet = false
                onNavigateToCapture(type)
            },
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.NoteAdd,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "メモがありません",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "下のバーからメモを追加しましょう",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text(text = "再試行")
            }
        }
    }
}

@Composable
private fun MemoList(
    memos: List<Memo>,
    onMemoClick: (String) -> Unit,
    onDeleteMemo: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(memos.size) {
        if (memos.isNotEmpty()) {
            listState.animateScrollToItem(memos.lastIndex)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(memos, key = { it.id }) { memo ->
            MemoCard(
                memo = memo,
                onClick = { onMemoClick(memo.id) },
                onDelete = { onDeleteMemo(memo.id) },
            )
        }
    }
}
