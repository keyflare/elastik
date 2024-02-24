package com.keyflare.elastik.core.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.jvm.JvmInline

// TODO Simplify this. It turns out that it's not needed to be such a complicated structure:
//  Transaction <- multiple operations. It's enough to have a single operation per transaction.
@JvmInline
internal value class StackTransaction(
    val transformations: List<StackTransformation>
)

internal class StackTransformation(
    val entryId: Int,
    val transformation: (List<Entry>) -> List<Entry>,
)

internal class ElastikStateHolder(initial: Stack = initialState) {
    private val _state = MutableStateFlow(initial)
    val state: StateFlow<Stack> = _state.asStateFlow()

    // TODO MVP solution!
    //  Seems that it's not needed to be a stateFlow, just need to be a concurrent-friendly
    private val blockingSubscribers = MutableStateFlow<List<(Stack) -> Unit>>(emptyList())

    // TODO MVP solution! needs to be optimized
    //  - maybe use fold
    fun pushTransaction(transaction: StackTransaction) {
        val updated = transaction
            .transformations
            .let { operations ->
                var updated = state.value
                operations.forEach { op ->
                    updated = updated.transform(op.entryId, op.transformation)
                }
                updated
            }
        _state.value = updated

        blockingSubscribers.value.forEach { it(updated) }
    }

    fun subscribeBlocking(onEach: (Stack) -> Unit) {
        blockingSubscribers.update { it + onEach }
    }

    companion object {
        internal const val ROOT_ENTRY_ID = -1

        // TODO maybe rename to something rare to minimise a chance
        //  that some user's destination will conflict with this id.
        private const val ROOT_DESTINATION_ID = "root"

        internal val initialState = Stack(
            entryId = ROOT_ENTRY_ID,
            args = EmptyArguments,
            destinationId = ROOT_DESTINATION_ID,
            entries = emptyList(),
        )
    }
}
