@file:Suppress("MemberVisibilityCanBePrivate")

package com.keyflare.elastik.core.setup.router

import com.keyflare.elastik.core.context.ElastikContext
import com.keyflare.elastik.core.routing.router.BaseRouter
import com.keyflare.elastik.core.routing.router.DynamicRouter
import com.keyflare.elastik.core.routing.router.StaticRouter

internal open class TestStaticRouter(
    context: ElastikContext,
) : StaticRouter(context) {
    internal val logicDelegate by lazy { TestRouterLogicDelegate(this) }

    override fun onInterceptBack(): Boolean {
        return logicDelegate.onInterceptBack()
    }
}

internal open class TestDynamicRouter(
    context: ElastikContext,
) : DynamicRouter(context) {
    val logicDelegate by lazy { TestRouterLogicDelegate(this) }

    override fun onInterceptBack(): Boolean {
        return logicDelegate.onInterceptBack()
    }
}

internal class TestRouterLogicDelegate(val router: BaseRouter) {

    private var backEventsToInterceptNumber = 0

    fun enableInterceptingBackEvents(n: Int) {
        backEventsToInterceptNumber = n
    }

    fun onInterceptBack(): Boolean {
        return if (backEventsToInterceptNumber == 0) {
            false
        } else {
            backEventsToInterceptNumber--
            true
        }
    }
}
