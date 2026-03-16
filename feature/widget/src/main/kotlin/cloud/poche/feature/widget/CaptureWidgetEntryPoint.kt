package cloud.poche.feature.widget

import cloud.poche.core.domain.repository.MemoRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface CaptureWidgetEntryPoint {
    fun memoRepository(): MemoRepository
}
