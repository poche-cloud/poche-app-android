package cloud.poche.feature.devtools.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cloud.poche.core.ui.R

@Composable
internal fun EnvironmentInfoSection(
    appVersion: String,
    buildNumber: String,
    packageName: String,
    buildType: String,
    flavor: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionHeader(title = stringResource(R.string.environment_info_title))
        InfoTile(
            icon = Icons.Outlined.Info,
            title = stringResource(R.string.environment_info_app_version),
            value = appVersion,
        )
        InfoDivider()
        InfoTile(
            icon = Icons.Outlined.Build,
            title = stringResource(R.string.environment_info_build_number),
            value = buildNumber,
        )
        InfoDivider()
        InfoTile(
            icon = Icons.Outlined.Inventory2,
            title = stringResource(R.string.environment_info_package_name),
            value = packageName,
        )
        InfoDivider()
        InfoTile(
            icon = Icons.Outlined.Fingerprint,
            title = stringResource(R.string.environment_info_build_type),
            value = buildType,
        )
        InfoDivider()
        InfoTile(
            icon = Icons.AutoMirrored.Outlined.Label,
            title = stringResource(R.string.environment_info_flavor),
            value = flavor,
        )
    }
}

@Composable
internal fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun InfoTile(icon: ImageVector, title: String, value: String, modifier: Modifier = Modifier) {
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
            SelectionContainer {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun InfoDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier.padding(start = 56.dp))
}
