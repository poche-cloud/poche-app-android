package cloud.poche.feature.onboarding

import app.cash.turbine.test
import cloud.poche.core.domain.usecase.CompleteOnboardingUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var completeOnboardingUseCase: CompleteOnboardingUseCase
    private lateinit var viewModel: OnboardingViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        completeOnboardingUseCase = mockk()
        viewModel = OnboardingViewModel(completeOnboardingUseCase)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onNextPage should increment current page`() = runTest {
        viewModel.currentPage.test {
            assertEquals(0, awaitItem())
            viewModel.onNextPage()
            assertEquals(1, awaitItem())
        }
    }

    @Test
    fun `onSkip should emit NavigateToHome and call use case`() = runTest {
        coEvery { completeOnboardingUseCase() } returns Unit

        viewModel.events.test {
            viewModel.onSkip()
            assertEquals(OnboardingEvent.NavigateToHome, awaitItem())
        }

        coVerify { completeOnboardingUseCase() }
    }

    @Test
    fun `onGetStarted should emit NavigateToHome and call use case`() = runTest {
        coEvery { completeOnboardingUseCase() } returns Unit

        viewModel.events.test {
            viewModel.onGetStarted()
            assertEquals(OnboardingEvent.NavigateToHome, awaitItem())
        }

        coVerify { completeOnboardingUseCase() }
    }
}
