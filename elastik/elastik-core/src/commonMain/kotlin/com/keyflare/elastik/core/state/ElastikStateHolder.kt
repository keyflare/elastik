package com.keyflare.elastik.core.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.jvm.JvmInline

@JvmInline
internal value class BackstackTransaction(
    val transformations: List<BackstackTransformation>
)

internal class BackstackTransformation(
    val backstackId: Int,
    val transformation: (List<BackstackEntry>) -> List<BackstackEntry>,
)

// TODO MVP solution!
//  - maybe mark constructor as internal
internal class ElastikStateHolder(initial: Backstack = initialState) {
    private val _state = MutableStateFlow(initial)
    val state: StateFlow<Backstack> = _state.asStateFlow()

    // TODO MVP solution!
    //  Seems that it's not needed to be a stateFlow, just need to be a concurrent-friendly
    private val blockingSubscribers = MutableStateFlow<List<(Backstack) -> Unit>>(emptyList())

    // TODO MVP solution! needs to be optimized
    //  - maybe use fold
    fun pushTransaction(transaction: BackstackTransaction) {
        val updated = transaction
            .transformations
            .let { operations ->
                var updated = state.value
                operations.forEach { op ->
                    updated = updated.transform(op.backstackId, op.transformation)
                }
                updated
            }
        _state.value = updated

        blockingSubscribers.value.forEach { it(updated) }
    }

    fun subscribeBlocking(onEach: (Backstack) -> Unit) {
        blockingSubscribers.update { it + onEach }
    }

    companion object {
        private const val ROOT_ID = -1
        private const val ROOT_DESTINATION_ID = "root"

        @PublishedApi
        internal val initialState = Backstack(
            id = ROOT_ID,
            args = EmptyArguments,
            destinationId = ROOT_DESTINATION_ID,
            entries = emptyList(),
        )
    }
}
