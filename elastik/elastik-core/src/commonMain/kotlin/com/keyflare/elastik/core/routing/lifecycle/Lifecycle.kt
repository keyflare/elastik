package com.keyflare.elastik.core.routing.lifecycle

interface Lifecycle {

    // TODO MVP solution.
    //  Now it's hard to decompose lifecycle events handling from a component.
    fun subscribe(
        onCreate: (() -> Unit)? = null,
        onStart: (() -> Unit)? = null,
        onResume: (() -> Unit)? = null,
        onPause: (() -> Unit)? = null,
        onStop: (() -> Unit)? = null,
        onDestroy: (() -> Unit)? = null,
        onAny: ((LifecycleEvent) -> Unit)? = null,
    )
}

internal class LifecycleImpl : Lifecycle {
    private var onCreate: (() -> Unit)? = null
    private var onStart: (() -> Unit)? = null
    private var onResume: (() -> Unit)? = null
    private var onPause: (() -> Unit)? = null
    private var onStop: (() -> Unit)? = null
    private var onDestroy: (() -> Unit)? = null
    private var onAny: ((LifecycleEvent) -> Unit)? = null

    // TODO MVP solution.
    //  Each subscribe() call rewrites already passed callbacks
    override fun subscribe(
        onCreate: (() -> Unit)?,
        onStart: (() -> Unit)?,
        onResume: (() -> Unit)?,
        onPause: (() -> Unit)?,
        onStop: (() -> Unit)?,
        onDestroy: (() -> Unit)?,
        onAny: ((LifecycleEvent) -> Unit)?
    ) {
        onCreate?.let { this.onCreate = it }
        onStart?.let { this.onStart = it }
        onResume?.let { this.onResume = it }
        onPause?.let { this.onPause = it }
        onStop?.let { this.onStop = it }
        onDestroy?.let { this.onDestroy = it }
        onAny?.let { this.onAny = it }
    }

    fun onCreate() {
        onCreate?.invoke()
    }

    fun onStart() {
        onStart?.invoke()
    }

    fun onResume() {
        onResume?.invoke()
    }

    fun onPause() {
        onPause?.invoke()
    }

    fun onStop() {
        onStop?.invoke()
    }

    fun onDestroy() {
        onDestroy?.invoke()
    }

    fun onAny(event: LifecycleEvent) {
        onAny?.invoke(event)
    }
}
