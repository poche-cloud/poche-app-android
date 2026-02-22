package cloud.poche.feature.settings.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cloud.poche.core.model.DarkThemeConfig

@Composable
internal fun ThemeSettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ThemeSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ThemeSettingsScreen(
        uiState = uiState,
        onThemeSelected = viewModel::setTheme,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ThemeSettingsScreen(
    uiState: ThemeSettingsUiState,
    onThemeSelected: (DarkThemeConfig) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("テーマ選択") },
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
    ) { innerPadding ->
        when (uiState) {
            is ThemeSettingsUiState.Loading -> Unit
            is ThemeSettingsUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .selectableGroup(),
                ) {
                    ThemeRadioTile(
                        icon = Icons.Default.SettingsSuggest,
                        title = "システム設定に従う",
                        subtitle = "デバイスのテーマ設定を使用します",
                        selected = uiState.darkThemeConfig == DarkThemeConfig.FOLLOW_SYSTEM,
                        onClick = { onThemeSelected(DarkThemeConfig.FOLLOW_SYSTEM) },
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                    ThemeRadioTile(
                        icon = Icons.Default.LightMode,
                        title = "ライト",
                        subtitle = "常にライトテーマを使用します",
                        selected = uiState.darkThemeConfig == DarkThemeConfig.LIGHT,
                        onClick = { onThemeSelected(DarkThemeConfig.LIGHT) },
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                    ThemeRadioTile(
                        icon = Icons.Default.DarkMode,
                        title = "ダーク",
                        subtitle = "常にダークテーマを使用します",
                        selected = uiState.darkThemeConfig == DarkThemeConfig.DARK,
                        onClick = { onThemeSelected(DarkThemeConfig.DARK) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeRadioTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        RadioButton(
            selected = selected,
            onClick = null,
        )
    }
}
