package cloud.poche.feature.capture

import android.content.Intent
import java.util.regex.Pattern

object ShareIntentHandler {

    private val urlPattern = Pattern.compile("https?://[^\\s<>\"']+", Pattern.CASE_INSENSITIVE)
    private val tagPattern = Pattern.compile("<[^>]+>")

    fun extractUrl(intent: Intent): String? {
        if (intent.action != Intent.ACTION_SEND || !intent.type.isSupportedTextType()) return null
        val sharedText = intent.getStringExtra(Intent.EXTRA_HTML_TEXT)
            ?: intent.getStringExtra(Intent.EXTRA_TEXT)
        return extractUrl(sharedText)
    }

    fun extractUrl(sharedText: CharSequence?): String? {
        val text = sharedText?.toString()?.trim().orEmpty()
        if (text.isEmpty()) return null

        val decodedText = text.decodeHtmlEntities()
        return decodedText.findFirstUrl() ?: decodedText.stripTags().findFirstUrl()
    }

    private fun String?.isSupportedTextType(): Boolean = this == "text/plain" || this == "text/html"

    private fun String.decodeHtmlEntities(): String = replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")

    private fun String.stripTags(): String = tagPattern.matcher(this).replaceAll(" ")

    private fun String.findFirstUrl(): String? {
        val matcher = urlPattern.matcher(this)
        if (!matcher.find()) return null
        return matcher.group().trimEnd('.', ',', ';', '!', '?', ')')
    }
}
