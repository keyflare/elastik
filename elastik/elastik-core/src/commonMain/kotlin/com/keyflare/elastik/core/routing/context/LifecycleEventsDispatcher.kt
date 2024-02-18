package com.keyflare.elastik.core.routing.context

import com.keyflare.elastik.core.routing.component.LifecycleImpl

enum class LifecycleEvent {
    CREATED,
    STARTED,
    RESUMED,
    PAUSED,
    STOPPED,
    DESTROYED,
}

internal class LifecycleEventsDispatcher {

    private val lifecycles = mutableMapOf<Int, LifecycleImpl>()

    fun dispatch(event: LifecycleEvent) {
        lifecycles.values.forEach {
            when (event) {
                LifecycleEvent.CREATED -> it.onCreate()
                LifecycleEvent.STARTED -> it.onStart()
                LifecycleEvent.RESUMED -> it.onResume()
                LifecycleEvent.PAUSED -> it.onPause()
                LifecycleEvent.STOPPED -> it.onStop()
                LifecycleEvent.DESTROYED -> it.onDestroy()
            }
            it.onAny(event)
        }
    }

    fun addLifecycle(key: Int, lifecycle: LifecycleImpl) {
        lifecycles[key] = lifecycle
    }

    fun removeLifecycle(key: Int) {
        lifecycles.remove(key)
    }
}
