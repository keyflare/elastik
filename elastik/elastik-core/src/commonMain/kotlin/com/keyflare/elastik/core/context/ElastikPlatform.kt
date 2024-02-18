package com.keyflare.elastik.core.context

import com.keyflare.elastik.core.routing.lifecycle.LifecycleEvent

internal class ElastikPlatform(
    val lifecycleEventsSource: LifecycleEventsSource,
    val backEventsSource: BackEventsSource
)

internal interface BackEventsSource {
    fun subscribe(callback: () -> Boolean)
}

internal interface LifecycleEventsSource {
    fun subscribe(callback: (LifecycleEvent) -> Unit)
}
