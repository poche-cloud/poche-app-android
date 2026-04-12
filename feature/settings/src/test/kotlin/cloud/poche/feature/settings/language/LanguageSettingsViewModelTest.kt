package cloud.poche.feature.settings.language

import androidx.appcompat.app.AppCompatDelegate
import cloud.poche.core.domain.usecase.GetLocaleUseCase
import cloud.poche.core.domain.usecase.SetLocaleUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LanguageSettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getLocaleUseCase: GetLocaleUseCase
    private lateinit var setLocaleUseCase: SetLocaleUseCase
    private lateinit var viewModel: LanguageSettingsViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getLocaleUseCase = mockk()
        setLocaleUseCase = mockk()
        mockkStatic(AppCompatDelegate::class)
        every { AppCompatDelegate.getApplicationLocales() } returns mockk(relaxed = true)
        every { AppCompatDelegate.setApplicationLocales(any()) } returns Unit
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(AppCompatDelegate::class)
    }

    @Test
    fun `setLocale should persist locale and update application locales`() = runTest {
        coEvery { setLocaleUseCase(any()) } returns Unit
        coEvery { getLocaleUseCase() } returns flowOf("ja")

        viewModel = LanguageSettingsViewModel(getLocaleUseCase, setLocaleUseCase)

        viewModel.setLocale("en")

        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { setLocaleUseCase("en") }
        coVerify { AppCompatDelegate.setApplicationLocales(any()) }
    }
}
