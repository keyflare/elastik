package com.keyflare.elastik.core.setup.component

import com.keyflare.elastik.core.routing.lifecycle.LifecycleEvent

sealed interface TestScreenComponentEvent {
    val destinationId: String
    val entryId: Int

    data class LifecycleEventReceived(
        override val entryId: Int,
        override val destinationId: String,
        val lifecycleEvent: LifecycleEvent,
    ) : TestScreenComponentEvent
}
