package com.keyflare.elastik.core.routing.back

internal class BackDispatcher {

    private var callback: (() -> Boolean)? = null

    fun subscribe(callback: () -> Boolean) {
        this.callback = callback
    }

    fun dispatch(): Boolean {
        return callback?.invoke() ?: false
    }
}
