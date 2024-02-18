package com.keyflare.elastik.core.routing.back

interface BackHandler {
    fun handleBack(callback: () -> Boolean)
}

internal class BackHandlerImpl : BackHandler {
    // TODO MVP solution. It is need to do it thread-safe
    private var callback: (() -> Boolean)? = null

    override fun handleBack(callback: () -> Boolean) {
        this.callback = callback
    }

    internal fun onHandleBack(): Boolean {
        return callback?.invoke() ?: false
    }
}
