package cloud.poche.core.domain.usecase

import app.cash.turbine.test
import cloud.poche.core.model.DarkThemeConfig
import cloud.poche.core.model.UserData
import cloud.poche.core.domain.repository.UserDataRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class IsOnboardingCompletedUseCaseTest {

    private val repository = mockk<UserDataRepository>()
    private val useCase = IsOnboardingCompletedUseCase(repository)

    @Test
    fun `invoke returns false when onboarding is not completed`() = runTest {
        every { repository.userData } returns flowOf(
            UserData(
                isOnboardingCompleted = false,
                userId = null,
                darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
            ),
        )

        useCase().test {
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `invoke returns true when onboarding is completed`() = runTest {
        every { repository.userData } returns flowOf(
            UserData(
                isOnboardingCompleted = true,
                userId = null,
                darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
            ),
        )

        useCase().test {
            assertTrue(awaitItem())
            awaitComplete()
        }
    }
}
