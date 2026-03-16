package cloud.poche.core.domain.usecase

import cloud.poche.core.model.DarkThemeConfig
import cloud.poche.core.model.UserData
import cloud.poche.core.domain.repository.UserDataRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class CompleteOnboardingUseCaseTest {

    private val repository = mockk<UserDataRepository>(relaxed = true)
    private val useCase = CompleteOnboardingUseCase(repository)

    @Test
    fun `invoke calls setOnboardingCompleted with true`() = runTest {
        coEvery { repository.setOnboardingCompleted(any()) } returns Unit

        useCase()

        coVerify(exactly = 1) { repository.setOnboardingCompleted(true) }
    }
}
