package cloud.poche.core.ui

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Phone", device = "spec:width=411dp,height=891dp")
@Preview(name = "Phone - Dark", device = "spec:width=411dp,height=891dp", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Tablet", device = "spec:width=1280dp,height=800dp,dpi=240")
annotation class DevicePreviews
