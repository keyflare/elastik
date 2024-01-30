package com.keyflare.elastik.core.util

import com.keyflare.elastik.core.routing.router.BaseRouter
import com.keyflare.elastik.core.routing.router.Destination
import com.keyflare.elastik.core.routing.router.DynamicRouter
import com.keyflare.elastik.core.routing.router.StaticRouter
import com.keyflare.elastik.core.util.ApplyNavigationScope.RouterToNavigateWith

fun BaseRouter.applyNavigation(body: ApplyNavigationScope.() -> Unit): BaseRouter {
    ApplyNavigationScopeImpl(root = this).apply {
        body()
        applyNavigation()
    }
    return this
}

interface ApplyNavigationScope {
    fun router(destinationId: String): RouterToNavigateWith
    infix fun RouterToNavigateWith.navigate(destinationId: String)

    data class RouterToNavigateWith(val destinationId: String)
}

private class ApplyNavigationScopeImpl(
    private val root: BaseRouter,
) : ApplyNavigationScope {

    private val actions = mutableListOf<NavigationAction>()

    override fun router(destinationId: String): RouterToNavigateWith {
        return RouterToNavigateWith(destinationId)
    }

    override fun RouterToNavigateWith.navigate(destinationId: String) {
        actions.add(
            NavigationAction(
                routerDestinationId = this.destinationId,
                toDestinationId = destinationId,
            )
        )
    }

    fun applyNavigation() {
        actions.forEach { applyAction(it) }
    }

    private fun applyAction(action: NavigationAction) {
        var routers = listOf(root)
        var router: BaseRouter? = null

        while (routers.isNotEmpty()) {
            val r = routers.find { it.destinationId == action.routerDestinationId }
            if (r != null) {
                router = r
                break
            }
            routers = routers.flatMap { it.childRouters }
        }

        when (router) {
            null -> error("No such a router (destinationId=${action.routerDestinationId})")
            is StaticRouter -> error("Router with destinationId=${action.routerDestinationId} is static")
            is DynamicRouter -> {
                router.navigateTo(
                    destination = Destination(
                        destinationId = action.toDestinationId,
                        single = router.singleDestinations.contains(action.toDestinationId)
                    )
                )
            }

            else -> error("This should never happen (destinationId=${action.routerDestinationId})")
        }
    }

    private data class NavigationAction(
        val routerDestinationId: String,
        val toDestinationId: String,
    )
}
