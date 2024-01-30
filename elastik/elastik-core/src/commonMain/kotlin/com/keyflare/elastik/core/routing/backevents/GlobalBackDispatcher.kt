package com.keyflare.elastik.core.routing.backevents

internal class GlobalBackDispatcher {

    private var callback: (() -> Boolean)? = null

    fun subscribe(callback: () -> Boolean) {
        this.callback = callback
    }

    fun dispatch(): Boolean {
        return callback?.invoke() ?: false
    }
}
