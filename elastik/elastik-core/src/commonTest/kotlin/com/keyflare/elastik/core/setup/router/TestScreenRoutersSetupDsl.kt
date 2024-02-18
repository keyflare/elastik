package com.keyflare.elastik.core.setup.router

import com.keyflare.elastik.core.routing.router.BaseRouter

internal fun BaseRouter.setupRouters(body: SetupRoutersScope.() -> Unit): BaseRouter {
    SetupRoutersScopeImpl(root = this).apply {
        body()
    }
    return this
}

internal interface SetupRoutersScope {
    fun router(destinationId: String, body: TestRouterLogicDelegate.() -> Unit)
}

private class SetupRoutersScopeImpl(
    private val root: BaseRouter,
) : SetupRoutersScope {

    override fun router(destinationId: String, body: TestRouterLogicDelegate.() -> Unit) {
        var routers = listOf(root)

        while (routers.isNotEmpty()) {
            val staticRouters = routers.filterIsInstance<TestStaticRouter>()
            val dynamicRouters = routers.filterIsInstance<TestDynamicRouter>()

            val logicDelegate = staticRouters
                .firstOrNull { it.destinationId == destinationId }
                ?.logicDelegate
                ?: dynamicRouters
                    .firstOrNull { it.destinationId == destinationId }
                    ?.logicDelegate


            if (logicDelegate != null) {
                logicDelegate.body()
                return
            }

            routers = routers.flatMap { it.childRouters }
        }
    }
}
