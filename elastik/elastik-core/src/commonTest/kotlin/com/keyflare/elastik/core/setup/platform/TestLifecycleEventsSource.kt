package com.keyflare.elastik.core.setup.platform

import com.keyflare.elastik.core.context.LifecycleEventsSource
import com.keyflare.elastik.core.routing.context.LifecycleEvent

class TestLifecycleEventsSource : LifecycleEventsSource {
    private var callback: ((LifecycleEvent) -> Unit)? = null

    override fun subscribe(callback: (LifecycleEvent) -> Unit) {
        this.callback = callback
    }

    fun fireEvent(event: LifecycleEvent) {
        callback?.invoke(event)
    }
}
