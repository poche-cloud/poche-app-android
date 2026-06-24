package cloud.poche.feature.devtools

import android.content.Context
import android.content.pm.ApplicationInfo
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DevToolsViewModelTest {

    private lateinit var context: Context
    private lateinit var viewModel: DevToolsViewModel

    @BeforeEach
    fun setup() {
        context = mockk(relaxed = true)
        every { context.packageName } returns "cloud.poche.app.dev"
        val appInfo = ApplicationInfo().apply {
            flags = ApplicationInfo.FLAG_DEBUGGABLE
        }
        every { context.applicationInfo } returns appInfo
    }

    @Test
    fun `uiState should contain correct build info`() = runTest {
        viewModel = DevToolsViewModel(context)

        val state = viewModel.uiState.value
        assertEquals("dev", state.flavor)
        assertEquals("debug", state.buildType)
        assertEquals("cloud.poche.app.dev", state.packageName)
    }
}
