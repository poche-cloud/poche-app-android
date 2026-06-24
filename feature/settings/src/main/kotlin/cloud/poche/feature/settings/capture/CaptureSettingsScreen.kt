package cloud.poche.feature.settings.capture

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cloud.poche.core.ui.R

@Composable
internal fun CaptureSettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CaptureSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CaptureSettingsScreenContent(
        uiState = uiState,
        onTypeSelected = viewModel::setDefaultCaptureType,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CaptureSettingsScreenContent(
    uiState: CaptureSettingsUiState,
    onTypeSelected: (String?) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.capture_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when (uiState) {
            is CaptureSettingsUiState.Loading -> Unit

            is CaptureSettingsUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .selectableGroup(),
                ) {
                    Text(
                        text = stringResource(R.string.capture_settings_default_type),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                    )
                    CaptureTypeRadioTile(
                        title = stringResource(R.string.capture_settings_type_selection),
                        subtitle = stringResource(R.string.capture_settings_type_selection_desc),
                        selected = uiState.defaultCaptureType == null,
                        onClick = { onTypeSelected(null) },
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                    CaptureTypeRadioTile(
                        title = stringResource(R.string.capture_settings_type_text),
                        subtitle = stringResource(R.string.capture_settings_type_text_desc),
                        selected = uiState.defaultCaptureType == "TEXT",
                        onClick = { onTypeSelected("TEXT") },
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                    CaptureTypeRadioTile(
                        title = stringResource(R.string.capture_settings_type_photo),
                        subtitle = stringResource(R.string.capture_settings_type_photo_desc),
                        selected = uiState.defaultCaptureType == "PHOTO",
                        onClick = { onTypeSelected("PHOTO") },
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                    CaptureTypeRadioTile(
                        title = stringResource(R.string.capture_settings_type_voice),
                        subtitle = stringResource(R.string.capture_settings_type_voice_desc),
                        selected = uiState.defaultCaptureType == "VOICE",
                        onClick = { onTypeSelected("VOICE") },
                    )
                }
            }
        }
    }
}

@Composable
private fun CaptureTypeRadioTile(
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
        Spacer(modifier = Modifier.width(16.dp))
        RadioButton(
            selected = selected,
            onClick = null,
        )
    }
}
