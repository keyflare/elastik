package com.keyflare.elastik.core.setup.platform

import com.keyflare.elastik.core.context.BackEventsSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

internal class TestBackEventsSource(private val scope: CoroutineScope) : BackEventsSource {
    private val events = MutableSharedFlow<Unit>()
    private val results = MutableSharedFlow<Boolean>()

    override fun subscribe(callback: () -> Boolean) {
        events
            .onEach { results.emit(callback()) }
            .launchIn(scope)
    }

    fun handleResultFlow(): Flow<Boolean> = results

    suspend fun fireEvent() {
        results
            .onStart { events.emit(Unit) }
            .launchIn(scope)
    }
}
