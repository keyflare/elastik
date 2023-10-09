package com.keyflare.elastik.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.jvm.JvmInline

@JvmInline
value class BackstackTransaction(
    val transformations: List<BackstackTransformation>
)

class BackstackTransformation(
    val backstackId: Int,
    val transformation: (List<BackstackEntry>) -> List<BackstackEntry>,
)

class ElastikStateHolder(initial: Backstack = initialState) {
    private val _state = MutableStateFlow(initial)
    val state: StateFlow<Backstack> = _state.asStateFlow()

    // MVP solution!
    // TODO needs to be optimized
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
    }

    companion object {
        private const val ROOT_ID = -1
        private const val ROOT_LABEL = "root"

        internal val initialState = Backstack(
            id = ROOT_ID,
            label = ROOT_LABEL,
            args = null,
            entries = emptyList(),
        )
    }
}
