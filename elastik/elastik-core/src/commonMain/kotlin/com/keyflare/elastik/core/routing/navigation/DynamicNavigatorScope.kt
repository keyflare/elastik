package com.keyflare.elastik.core.routing.navigation

import com.keyflare.elastik.core.routing.navigation.DynamicNavigatorScope.DynamicStateMutatorFreeScope
import com.keyflare.elastik.core.routing.router.DynamicRouter
import com.keyflare.elastik.core.routing.tree.Destination
import com.keyflare.elastik.core.state.Arguments
import com.keyflare.elastik.core.state.EmptyArguments
import com.keyflare.elastik.core.state.Entry

interface DynamicNavigatorScope {
    val currentState: List<Entry>

    fun popTo(destinationId: String, inclusive: Boolean = false)
    fun popTo(entryId: Int, inclusive: Boolean = false)
    fun pop(n: Int)
    fun <Args : Arguments> push(destination: Destination<Args>, args: Args)
    fun push(destination: Destination<EmptyArguments>)
    fun free(body: DynamicStateMutatorFreeScope.() -> Unit)

    interface DynamicStateMutatorFreeScope {
        fun <Args : Arguments> insert(destination: Destination<Args>, args: Args, index: Int)
        fun insert(destination: Destination<EmptyArguments>, index: Int)
        fun removeFirst(destinationId: String)
        fun removeLast(destinationId: String)
        fun removeAll(destinationId: String)
        fun removeAt(index: Int)
        fun removeEntry(entryId: Int)
        fun swapAt(firstIndex: Int, secondIndex: Int)
        fun swapEntries(firstEntryId: Int, secondEntryId: Int)
    }
}

internal class DynamicNavigatorScopeImpl(
    private val router: DynamicRouter,
) : DynamicNavigatorScope {
    private val state = router.stack.entries.toMutableList()

    override val currentState: List<Entry> get() = state.toList()

    fun apply() {
        router.mutateState { state }
    }

    override fun popTo(destinationId: String, inclusive: Boolean) {
        val index = state.indexOfLast { it.destinationId == destinationId }
        if (index != -1 && state.isNotEmpty()) {
            val dropN = state.lastIndex - index + if (inclusive) 1 else 0
            repeat(dropN) { state.removeLast() }
        }
    }

    override fun popTo(entryId: Int, inclusive: Boolean) {
        val index = state.indexOfFirst { it.entryId == entryId }
        if (index != -1 && state.isNotEmpty()) {
            val dropN = state.lastIndex - index + if (inclusive) 1 else 0
            repeat(dropN) { state.removeLast() }
        }
    }

    override fun pop(n: Int) {
        if (state.isNotEmpty()) {
            val dropN = n.coerceIn(0..state.size)
            repeat(dropN) { state.removeLast() }
        }
    }

    override fun <Args : Arguments> push(destination: Destination<Args>, args: Args) {
        state += router.createEntry(destination, args)
    }

    override fun push(destination: Destination<EmptyArguments>) {
        push(destination, EmptyArguments)
    }

    override fun free(body: DynamicStateMutatorFreeScope.() -> Unit) {
        DynamicStateMutatorFreeScopeImpl().apply(body)
    }

    private inner class DynamicStateMutatorFreeScopeImpl : DynamicStateMutatorFreeScope {

        override fun <Args : Arguments> insert(
            destination: Destination<Args>,
            args: Args,
            index: Int
        ) {
            val safeIndex = if (state.isEmpty()) 0 else index.coerceIn(state.indices)
            state.add(index = safeIndex, element = router.createEntry(destination, args))
        }

        override fun insert(destination: Destination<EmptyArguments>, index: Int) {
            insert(destination, EmptyArguments, index)
        }

        override fun removeFirst(destinationId: String) {
            val index = state.indexOfFirst { it.destinationId == destinationId }
            if (index != -1) {
                state.removeAt(index)
            }
        }

        override fun removeLast(destinationId: String) {
            val index = state.indexOfLast { it.destinationId == destinationId }
            if (index != -1) {
                state.removeAt(index)
            }
        }

        override fun removeAll(destinationId: String) {
            state.removeAll { it.destinationId == destinationId }
        }

        override fun removeAt(index: Int) {
            if (state.lastIndex >= index) {
                state.removeAt(index)
            }
        }

        override fun removeEntry(entryId: Int) {
            val index = state.indexOfFirst { it.entryId == entryId }
            if (index != -1) {
                state.removeAt(index)
            }
        }

        override fun swapAt(firstIndex: Int, secondIndex: Int) {
            if (firstIndex in state.indices && secondIndex in state.indices) {
                state[firstIndex] = state[secondIndex]
                        .also { state[secondIndex] = state[firstIndex] }
            }
        }

        override fun swapEntries(firstEntryId: Int, secondEntryId: Int) {
            val firstIndex = state.indexOfFirst { it.entryId == firstEntryId }
            val secondIndex = state.indexOfFirst { it.entryId == secondEntryId }
            if (firstIndex != -1 && secondIndex != -1 && firstIndex != secondIndex) {
                state[firstIndex] = state[secondIndex]
                        .also { state[secondIndex] = state[firstIndex] }
            }
        }
    }
}
