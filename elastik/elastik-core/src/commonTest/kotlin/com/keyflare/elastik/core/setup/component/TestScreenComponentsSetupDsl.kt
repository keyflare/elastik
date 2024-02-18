package com.keyflare.elastik.core.setup.component

import com.keyflare.elastik.core.routing.router.BaseRouter
import com.keyflare.elastik.core.util.cast

fun BaseRouter.setupComponents(body: SetupComponentsScope.() -> Unit): BaseRouter {
    SetupComponentsScopeImpl(root = this).apply {
        body()
    }
    return this
}

interface SetupComponentsScope {
    fun component(destinationId: String, body: TestScreenComponent.() -> Unit)
}

private class SetupComponentsScopeImpl(
    private val root: BaseRouter,
) : SetupComponentsScope {

    override fun component(destinationId: String, body: TestScreenComponent.() -> Unit) {
        var routers = listOf(root)

        while (routers.isNotEmpty()) {
            val component = routers
                .flatMap { it.childComponents }
                .firstOrNull { it.cast<TestScreenComponent>().destinationId == destinationId }
                ?.cast<TestScreenComponent>()

            if (component != null) {
                component.body()
                return
            }

            routers = routers.flatMap { it.childRouters }
        }
    }
}
