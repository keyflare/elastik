package com.keyflare.elastik.core

import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.addCallback
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.keyflare.elastik.core.context.ElastikContext
import com.keyflare.elastik.core.context.ElastikPlatform
import com.keyflare.elastik.core.context.BackEventsSource
import com.keyflare.elastik.core.context.LifecycleEventsSource
import com.keyflare.elastik.core.routing.lifecycle.LifecycleEvent

fun <T> T.attachElastikContext(context: ElastikContext) where
        T : OnBackPressedDispatcherOwner,
        T : LifecycleOwner {

    lifecycle.addObserver(
        ElastikLifecycleObserver(
            elastikContext = context,
            onBackPressedDispatcherOwner = this,
            lifecycle = lifecycle,
        )
    )
}

private class ElastikLifecycleObserver(
    elastikContext: ElastikContext,
    lifecycle: androidx.lifecycle.Lifecycle,
    onBackPressedDispatcherOwner: OnBackPressedDispatcherOwner,
) : DefaultLifecycleObserver {

    private val lifecycleEventsSource = AndroidLifecycleEventsSource()
    private val backEventsSource = AndroidBackEventsSource(onBackPressedDispatcherOwner)

    init {
        lifecycle.addObserver(lifecycleEventsSource)

        elastikContext.attachPlatform(
            ElastikPlatform(
                backEventsSource = backEventsSource,
                lifecycleEventsSource = lifecycleEventsSource,
            )
        )
    }
}

private class AndroidBackEventsSource(
    private val onBackPressedDispatcherOwner: OnBackPressedDispatcherOwner,
) : BackEventsSource {

    override fun subscribe(callback: () -> Boolean) {
        val dispatcher = onBackPressedDispatcherOwner.onBackPressedDispatcher
        dispatcher.addCallback {
            val handled = callback()
            if (!handled) {
                isEnabled = false
                dispatcher.onBackPressed()
                isEnabled = true
            }
        }
    }
}

private class AndroidLifecycleEventsSource : LifecycleEventsSource, DefaultLifecycleObserver {

    private var callback: ((LifecycleEvent) -> Unit)? = null

    override fun subscribe(callback: (LifecycleEvent) -> Unit) {
        this.callback = callback
    }

    override fun onCreate(owner: LifecycleOwner) {
        callback?.invoke(LifecycleEvent.CREATED)
    }

    override fun onStart(owner: LifecycleOwner) {
        callback?.invoke(LifecycleEvent.STARTED)
    }

    override fun onResume(owner: LifecycleOwner) {
        callback?.invoke(LifecycleEvent.RESUMED)
    }

    override fun onPause(owner: LifecycleOwner) {
        callback?.invoke(LifecycleEvent.PAUSED)
    }

    override fun onStop(owner: LifecycleOwner) {
        callback?.invoke(LifecycleEvent.STOPPED)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        callback?.invoke(LifecycleEvent.DESTROYED)
    }
}
