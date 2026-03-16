package cloud.poche.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cloud.poche.core.domain.usecase.CompleteOnboardingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(private val completeOnboardingUseCase: CompleteOnboardingUseCase) :
    ViewModel() {

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _events = MutableSharedFlow<OnboardingEvent>()
    val events = _events.asSharedFlow()

    fun onNextPage() {
        val current = _currentPage.value
        if (current < PAGE_COUNT - 1) {
            _currentPage.value = current + 1
        }
    }

    fun onSkip() {
        completeAndNavigate()
    }

    fun onGetStarted() {
        completeAndNavigate()
    }

    private fun completeAndNavigate() {
        viewModelScope.launch {
            completeOnboardingUseCase()
            _events.emit(OnboardingEvent.NavigateToHome)
        }
    }

    companion object {
        const val PAGE_COUNT = 3
    }
}

sealed interface OnboardingEvent {
    data object NavigateToHome : OnboardingEvent
}
