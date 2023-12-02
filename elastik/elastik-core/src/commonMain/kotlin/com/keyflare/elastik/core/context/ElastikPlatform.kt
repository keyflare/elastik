package com.keyflare.elastik.core.context

internal class ElastikPlatform(
    val lifecycleEventsSource: LifecycleEventsSource,
    val backEventsSource: BackEventsSource
)

internal interface BackEventsSource {
    fun subscribe(callback: () -> Boolean)
}

internal interface LifecycleEventsSource {
    fun subscribe(callback: (LifecycleEvent) -> Boolean)

    enum class LifecycleEvent {
        CREATED,
        STARTED,
        RESUMED,
        PAUSED,
        STOPPED,
        DESTROYED,
    }
}
