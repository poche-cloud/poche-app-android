package cloud.poche.feature.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cloud.poche.core.ui.R
import cloud.poche.core.designsystem.component.PocheTopAppBar

@Composable
internal fun SettingsScreen(
    onNavigateToTheme: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onNavigateToLicenses: () -> Unit,
    onNavigateToCaptureSettings: () -> Unit,
    onNavigateToDataManagement: () -> Unit,
    onNavigateToDevTools: () -> Unit,
    onSignedOut: () -> Unit,
    onAccountDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.SignedOut -> onSignedOut()
                is SettingsEvent.AccountDeleted -> onAccountDeleted()
                is SettingsEvent.ShowError -> snackbarHostState.showSnackbar(event.message.asString(context))
            }
        }
    }

    SettingsScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNavigateToTheme = onNavigateToTheme,
        onNavigateToNotifications = onNavigateToNotifications,
        onNavigateToLanguage = onNavigateToLanguage,
        onNavigateToLicenses = onNavigateToLicenses,
        onNavigateToCaptureSettings = onNavigateToCaptureSettings,
        onNavigateToDataManagement = onNavigateToDataManagement,
        onNavigateToDevTools = onNavigateToDevTools,
        onSignOutClick = viewModel::signOut,
        onDeleteAccountClick = viewModel::deleteAccount,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    uiState: SettingsUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateToTheme: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onNavigateToLicenses: () -> Unit,
    onNavigateToCaptureSettings: () -> Unit,
    onNavigateToDataManagement: () -> Unit,
    onNavigateToDevTools: () -> Unit,
    onSignOutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            PocheTopAppBar(
                title = stringResource(id = cloud.poche.core.ui.R.string.settings_title),
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        when (uiState) {
            is SettingsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is SettingsUiState.Success -> {
                SettingsContent(
                    uiState = uiState,
                    onNavigateToTheme = onNavigateToTheme,
                    onNavigateToNotifications = onNavigateToNotifications,
                    onNavigateToLanguage = onNavigateToLanguage,
                    onNavigateToLicenses = onNavigateToLicenses,
                    onNavigateToCaptureSettings = onNavigateToCaptureSettings,
                    onNavigateToDataManagement = onNavigateToDataManagement,
                    onNavigateToDevTools = onNavigateToDevTools,
                    onSignOutClick = onSignOutClick,
                    onDeleteAccountClick = onDeleteAccountClick,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState.Success,
    onNavigateToTheme: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onNavigateToLicenses: () -> Unit,
    onNavigateToCaptureSettings: () -> Unit,
    onNavigateToDataManagement: () -> Unit,
    onNavigateToDevTools: () -> Unit,
    onSignOutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showSignOutDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteAccountDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // App settings section
        SettingsSectionHeader(title = stringResource(R.string.settings_section_app_settings), isFirst = true)
        SettingsListTile(
            icon = Icons.Default.Notifications,
            title = stringResource(R.string.settings_notification),
            subtitle = stringResource(R.string.settings_notification_subtitle),
            onClick = onNavigateToNotifications,
        )
        SettingsDivider()
        SettingsListTile(
            icon = Icons.Default.Palette,
            title = stringResource(R.string.settings_theme),
            subtitle = stringResource(R.string.settings_theme_subtitle),
            onClick = onNavigateToTheme,
        )
        SettingsDivider()
        SettingsListTile(
            icon = Icons.Default.Language,
            title = stringResource(R.string.settings_language),
            subtitle = stringResource(R.string.settings_language_subtitle),
            onClick = onNavigateToLanguage,
        )
        SettingsDivider()
        SettingsListTile(
            icon = Icons.Default.FlashOn,
            title = stringResource(R.string.settings_capture),
            subtitle = stringResource(R.string.settings_capture_subtitle),
            onClick = onNavigateToCaptureSettings,
        )
        SettingsDivider()
        SettingsListTile(
            icon = Icons.Default.Storage,
            title = stringResource(R.string.settings_data_management),
            subtitle = stringResource(R.string.settings_data_management_subtitle),
            onClick = onNavigateToDataManagement,
        )

        // Legal section
        SettingsSectionHeader(title = stringResource(R.string.settings_section_legal))
        SettingsListTile(
            icon = Icons.Default.Description,
            title = stringResource(R.string.settings_terms_of_service),
            subtitle = stringResource(R.string.settings_terms_of_service_subtitle),
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://poche.cloud/terms"))
                context.startActivity(intent)
            },
        )
        SettingsDivider()
        SettingsListTile(
            icon = Icons.Default.PrivacyTip,
            title = stringResource(R.string.settings_privacy_policy),
            subtitle = stringResource(R.string.settings_privacy_policy_subtitle),
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://poche.cloud/privacy"))
                context.startActivity(intent)
            },
        )

        SettingsSectionHeader(title = stringResource(R.string.settings_language))
        SettingsListTile(
            icon = Icons.Default.Language,
            title = stringResource(R.string.settings_language),
            subtitle = stringResource(R.string.settings_language_subtitle),
            onClick = onNavigateToLanguage
        )
        SettingsDivider()
        SettingsListTile(
            icon = Icons.Default.Description,
            title = stringResource(R.string.settings_oss_licenses),
            subtitle = stringResource(R.string.settings_oss_licenses_subtitle),
            onClick = onNavigateToLicenses,
        )
        SettingsDivider()
        SettingsInfoTile(
            icon = Icons.Default.Info,
            title = stringResource(R.string.settings_version),
            subtitle = uiState.appVersion,
        )

        // Developer tools section (debug build only)
        if (uiState.isDebugBuild) {
            SettingsSectionHeader(title = stringResource(R.string.settings_section_developer))
            SettingsListTile(
                icon = Icons.Default.DeveloperMode,
                title = stringResource(R.string.settings_devtools),
                subtitle = stringResource(R.string.settings_devtools_subtitle),
                onClick = onNavigateToDevTools,
            )
        }

        // Account section
        if (uiState.isSignedIn) {
            SettingsSectionHeader(title = stringResource(R.string.settings_section_account))
            SettingsListTile(
                icon = Icons.AutoMirrored.Filled.Logout,
                title = stringResource(R.string.settings_sign_out),
                subtitle = stringResource(R.string.settings_sign_out_subtitle),
                onClick = { showSignOutDialog = true },
                isDestructive = true,
            )
            SettingsDivider()
            SettingsListTile(
                icon = Icons.Default.DeleteForever,
                title = stringResource(id = cloud.poche.core.ui.R.string.settings_delete_account),
                subtitle = stringResource(R.string.settings_delete_account_subtitle),
                onClick = { showDeleteAccountDialog = true },
                isDestructive = true,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showSignOutDialog) {
        ConfirmDialog(
            title = stringResource(R.string.settings_dialog_sign_out_title),
            message = stringResource(R.string.settings_dialog_sign_out_message),
            confirmText = stringResource(R.string.settings_dialog_sign_out_confirm),
            onConfirm = {
                showSignOutDialog = false
                onSignOutClick()
            },
            onDismiss = { showSignOutDialog = false },
        )
    }

    if (showDeleteAccountDialog) {
        ConfirmDialog(
            title = stringResource(id = cloud.poche.core.ui.R.string.settings_delete_account),
            message = stringResource(R.string.settings_dialog_delete_account_message),
            confirmText = stringResource(R.string.settings_dialog_delete_account_confirm),
            onConfirm = {
                showDeleteAccountDialog = false
                onDeleteAccountClick()
            },
            onDismiss = { showDeleteAccountDialog = false },
        )
    }
}

// region Components

@Composable
private fun SettingsSectionHeader(title: String, modifier: Modifier = Modifier, isFirst: Boolean = false) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(
            start = 16.dp,
            top = if (isFirst) 16.dp else 24.dp,
            bottom = 8.dp,
        ),
    )
}

@Composable
private fun SettingsListTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
) {
    val contentColor = if (isDestructive) {
        MaterialTheme.colorScheme.error
    } else {
        Color.Unspecified
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (isDestructive) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (isDestructive) {
                    MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
        if (!isDestructive) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingsInfoTile(icon: ImageVector, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
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
    }
}

@Composable
private fun SettingsDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(start = 56.dp),
    )
}

@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        },
    )
}

// endregion
