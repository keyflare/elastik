package com.keyflare.elastik.core.util.component

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class TestScreenComponentsReporter {
    private val _events = MutableStateFlow<List<TestScreenComponentEvent>>(emptyList())

    val lastEvent: Flow<TestScreenComponentEvent?> = _events.map { it.lastOrNull() }
    val allEvents: List<TestScreenComponentEvent> get() = _events.value

    fun report(event: TestScreenComponentEvent) {
        _events.update { it + event }
    }

    fun clear() {
        _events.value = emptyList()
    }
}
