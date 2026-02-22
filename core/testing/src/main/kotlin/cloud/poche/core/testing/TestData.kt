package cloud.poche.core.testing

import cloud.poche.core.model.Memo
import cloud.poche.core.model.MemoType

object TestData {
    val sampleMemos = listOf(
        Memo(
            id = "1",
            content = "テストメモ 1",
            type = MemoType.TEXT,
            createdAt = 1700000000000L,
            updatedAt = 1700000000000L,
        ),
        Memo(
            id = "2",
            content = "テストメモ 2",
            type = MemoType.TEXT,
            createdAt = 1700000001000L,
            updatedAt = 1700000001000L,
        ),
    )
}
