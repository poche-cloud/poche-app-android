package cloud.poche.feature.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dagger.hilt.android.EntryPointAccessors

class CaptureWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            CaptureWidgetEntryPoint::class.java,
        )
        val memoCount = entryPoint.memoRepository().getMemoCount()

        provideContent {
            GlanceTheme {
                CaptureWidgetContent(
                    memoCount = memoCount,
                    context = context,
                )
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun CaptureWidgetContent(
    memoCount: Int,
    context: Context,
) {
    Column(
        modifier = GlanceModifier.fillMaxWidth().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = context.getString(R.string.widget_memo_count, memoCount),
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.onSurface,
            ),
        )
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(top = 8.dp),
        ) {
            CaptureButton(
                label = context.getString(R.string.widget_memo),
                deepLink = "poche://capture?type=TEXT",
                modifier = GlanceModifier.defaultWeight(),
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            CaptureButton(
                label = context.getString(R.string.widget_photo),
                deepLink = "poche://capture?type=PHOTO",
                modifier = GlanceModifier.defaultWeight(),
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            CaptureButton(
                label = context.getString(R.string.widget_voice),
                deepLink = "poche://capture?type=VOICE",
                modifier = GlanceModifier.defaultWeight(),
            )
        }
    }
}

@androidx.compose.runtime.Composable
private fun CaptureButton(
    label: String,
    deepLink: String,
    modifier: GlanceModifier = GlanceModifier,
) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
    Text(
        text = label,
        modifier = modifier
            .padding(8.dp)
            .clickable(actionStartActivity(intent)),
        style = TextStyle(
            color = GlanceTheme.colors.primary,
        ),
    )
}
