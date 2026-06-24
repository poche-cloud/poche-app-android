package cloud.poche.feature.capture

import android.content.Intent
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ShareIntentHandlerTest {

    @Test
    fun `extractUrl returns first URL from shared plain text`() {
        val intent = shareIntent(
            type = "text/plain",
            text = "Save this https://example.com/articles/123 for later",
        )

        val url = ShareIntentHandler.extractUrl(intent)

        assertEquals("https://example.com/articles/123", url)
    }

    @Test
    fun `extractUrl returns URL from shared html content`() {
        val intent = shareIntent(
            type = "text/html",
            text = null,
            htmlText = """<p><a href="https://example.com/path?a=1&amp;b=2">Example</a></p>""",
        )

        val url = ShareIntentHandler.extractUrl(intent)

        assertEquals("https://example.com/path?a=1&b=2", url)
    }

    @Test
    fun `extractUrl returns null when shared text has no URL`() {
        val intent = shareIntent(type = "text/plain", text = "No link here")

        val url = ShareIntentHandler.extractUrl(intent)

        assertNull(url)
    }

    private fun shareIntent(type: String, text: String?, htmlText: String? = null): Intent {
        val intent = mockk<Intent>()
        every { intent.action } returns Intent.ACTION_SEND
        every { intent.type } returns type
        every { intent.getStringExtra(Intent.EXTRA_HTML_TEXT) } returns htmlText
        every { intent.getStringExtra(Intent.EXTRA_TEXT) } returns text
        return intent
    }
}
