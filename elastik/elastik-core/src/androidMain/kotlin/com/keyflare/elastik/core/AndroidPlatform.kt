package com.keyflare.elastik.core

import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.addCallback
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.keyflare.elastik.core.context.ElastikContext
import com.keyflare.elastik.core.context.ElastikPlatform
import com.keyflare.elastik.core.context.BackEventsSource
import com.keyflare.elastik.core.context.LifecycleEventsSource

fun <T> T.attachElastikContext(context: ElastikContext) where
        T : OnBackPressedDispatcherOwner,
        T : LifecycleOwner {

    lifecycle.addObserver(
        ElastikLifecycleObserver(
            elastikContext = context,
            onBackPressedDispatcherOwner = this,
        )
    )
}

private class ElastikLifecycleObserver(
    private val elastikContext: ElastikContext,
    onBackPressedDispatcherOwner: OnBackPressedDispatcherOwner,
) : DefaultLifecycleObserver, LifecycleEventsSource {

    init {
        elastikContext.attachPlatform(
            ElastikPlatform(
                backEventsSource = AndroidBackEventsSource(onBackPressedDispatcherOwner),
                lifecycleEventsSource = this,
            )
        )
    }

    override fun onDestroy(owner: LifecycleOwner) {
        elastikContext.detachPlatform()
    }

    override fun subscribe(callback: (LifecycleEventsSource.LifecycleEvent) -> Boolean) {
        TODO("Not yet implemented")
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
