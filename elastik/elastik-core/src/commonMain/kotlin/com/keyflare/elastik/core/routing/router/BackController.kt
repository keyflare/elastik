package com.keyflare.elastik.core.routing.router

interface BackHandler {
    fun onHandleBack(callback: () -> Boolean)
}

internal interface BackDispatcher {
    fun dispatchBackEvent(): Boolean
}

internal interface BackController : BackDispatcher, BackHandler

internal class BackControllerImpl : BackController {
    // TODO MVP solution. It is need to do it thread-safe
    private var callback: (() -> Boolean)? = null

    override fun dispatchBackEvent(): Boolean {
        return callback?.invoke() ?: false
    }

    override fun onHandleBack(callback: () -> Boolean) {
        this.callback = callback
    }
}
