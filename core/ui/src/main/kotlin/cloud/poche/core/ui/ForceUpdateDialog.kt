package cloud.poche.core.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

@Composable
fun ForceUpdateDialog(
    packageName: String,
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = { /* non-dismissable */ },
        title = { Text(stringResource(R.string.force_update_title)) },
        text = { Text(stringResource(R.string.force_update_message)) },
        confirmButton = {
            TextButton(
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$packageName"),
                    )
                    context.startActivity(intent)
                },
            ) {
                Text(stringResource(R.string.force_update_button))
            }
        },
    )
}
