package cloud.poche.feature.settings.licenses

import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OssLicensesScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val dependencies = remember {
        buildList {
            add("Jetpack Compose" to "Apache License 2.0")
            add("Material 3" to "Apache License 2.0")
            add("Hilt" to "Apache License 2.0")
            add("Room" to "Apache License 2.0")
            add("Ktor" to "Apache License 2.0")
            add("Kotlinx Serialization" to "Apache License 2.0")
            add("Kotlinx Coroutines" to "Apache License 2.0")
            add("Coil" to "Apache License 2.0")
            add("Firebase" to "Apache License 2.0")
            add("Glance" to "Apache License 2.0")
            add("Timber" to "Apache License 2.0")
            add("Turbine" to "Apache License 2.0")
            add("MockK" to "Apache License 2.0")
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("オープンソースライセンス") },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            items(dependencies) { (name, license) ->
                ListItem(
                    headlineContent = { Text(name) },
                    supportingContent = {
                        Text(
                            text = license,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )
                HorizontalDivider()
            }
        }
    }
}
