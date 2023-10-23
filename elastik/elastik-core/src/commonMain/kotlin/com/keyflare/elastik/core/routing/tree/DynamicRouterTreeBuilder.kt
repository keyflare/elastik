package com.keyflare.elastik.core.routing.tree

import com.keyflare.elastik.core.state.Arguments
import com.keyflare.elastik.core.state.EmptyArguments
import com.keyflare.elastik.core.state.find
import com.keyflare.elastik.core.routing.router.BaseRouter
import com.keyflare.elastik.core.routing.router.Destination
import com.keyflare.elastik.core.ElastikContext
import com.keyflare.elastik.core.Errors
import com.keyflare.elastik.core.render.Render

abstract class DynamicRouterDestination<Args : Arguments, Router : BaseRouter>(
    val destination: Destination<Args>,
) {
    abstract fun peekRouter(): Router
    abstract fun peekRouterOrNull(): Router?
}

interface DynamicRouterTreeBuilder {

    fun BaseRouter.singleNoArgs(
        destinationId: String,
        render: Render,
    ): Destination<EmptyArguments>

    fun <Args : Arguments> BaseRouter.single(
        destinationId: String,
        render: Render,
    ): Destination<Args>

    fun <Router : BaseRouter> BaseRouter.backstackNoArgs(
        destinationId: String,
        render: Render,
        routerFactory: (ElastikContext) -> Router,
    ): DynamicRouterDestination<EmptyArguments, Router>

    fun <Args : Arguments, Router : BaseRouter> BaseRouter.backstack(
        destinationId: String,
        render: Render,
        routerFactory: (ElastikContext) -> Router,
    ): DynamicRouterDestination<Args, Router>
}

class DynamicRouterTreeBuilderDelegate : DynamicRouterTreeBuilder {

    override fun BaseRouter.singleNoArgs(
        destinationId: String,
        render: Render
    ): Destination<EmptyArguments> {
        return single(destinationId, render)
    }

    override fun <Args : Arguments> BaseRouter.single(
        destinationId: String,
        render: Render,
    ): Destination<Args> {
        routingContext.addRenderBinding(destinationId, render)
        addSingleDestinationBinding(destinationId)
        return Destination(id = destinationId, isSingle = true)
    }

    override fun <Router : BaseRouter> BaseRouter.backstackNoArgs(
        destinationId: String,
        render: Render,
        routerFactory: (ElastikContext) -> Router
    ): DynamicRouterDestination<EmptyArguments, Router> {
        return backstack(destinationId, render, routerFactory)
    }

    override fun <Args : Arguments, Router : BaseRouter> BaseRouter.backstack(
        destinationId: String,
        render: Render,
        routerFactory: (ElastikContext) -> Router,
    ): DynamicRouterDestination<Args, Router> {

        addBackstackDestinationBinding(destinationId, routerFactory)
        routingContext.addRenderBinding(destinationId, render)

        // TODO MVP Solution!!! Refactor this approach
        return object : DynamicRouterDestination<Args, Router>(
            destination = Destination(id = destinationId, isSingle = false),
        ) {
            override fun peekRouter(): Router {
                return runCatching { requireNotNull(peekRouterOrNull()) }
                    .getOrElse { error(Errors.routerNotFound(destinationId)) }
            }

            override fun peekRouterOrNull(): Router? {
                val backstackEntryId = state.state.value
                    .find { it.destinationId == destinationId }
                    ?.id
                    ?: return null

                @Suppress("UNCHECKED_CAST")
                return findRouterOrNull(backstackEntryId) as? Router
            }
        }
    }
}
