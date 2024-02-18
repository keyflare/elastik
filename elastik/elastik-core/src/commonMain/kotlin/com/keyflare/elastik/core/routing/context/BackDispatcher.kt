package com.keyflare.elastik.core.routing.context

internal class BackDispatcher {

    private var callback: (() -> Boolean)? = null

    fun subscribe(callback: () -> Boolean) {
        this.callback = callback
    }

    fun dispatch(): Boolean {
        return callback?.invoke() ?: false
    }
}
