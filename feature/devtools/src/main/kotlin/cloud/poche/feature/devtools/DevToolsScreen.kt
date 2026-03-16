package cloud.poche.feature.devtools

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cloud.poche.feature.devtools.component.CacheActionsSection
import cloud.poche.feature.devtools.component.EnvironmentInfoSection
import cloud.poche.feature.devtools.component.FeatureFlagsSection

@Composable
internal fun DevToolsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DevToolsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DevToolsEvent.CacheClearSuccess ->
                    snackbarHostState.showSnackbar("キャッシュをクリアしました")

                is DevToolsEvent.CacheClearError ->
                    snackbarHostState.showSnackbar("キャッシュのクリアに失敗しました")
            }
        }
    }

    DevToolsScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onClearCache = viewModel::clearCache,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DevToolsScreenContent(
    uiState: DevToolsUiState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onClearCache: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("開発者ツール") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "戻る",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            EnvironmentInfoSection(
                appVersion = uiState.appVersion,
                buildNumber = uiState.buildNumber,
                packageName = uiState.packageName,
                buildType = uiState.buildType,
                flavor = uiState.flavor,
            )
            HorizontalDivider()
            FeatureFlagsSection(flags = uiState.featureFlags)
            HorizontalDivider()
            CacheActionsSection(onClearCache = onClearCache)
        }
    }
}
