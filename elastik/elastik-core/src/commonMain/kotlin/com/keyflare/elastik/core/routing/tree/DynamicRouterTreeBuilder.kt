package com.keyflare.elastik.core.routing.tree

import com.keyflare.elastik.core.state.Arguments
import com.keyflare.elastik.core.state.EmptyArguments
import com.keyflare.elastik.core.state.find
import com.keyflare.elastik.core.routing.router.BaseRouter
import com.keyflare.elastik.core.routing.router.Destination
import com.keyflare.elastik.core.ElastikContext
import com.keyflare.elastik.core.Errors
import com.keyflare.elastik.core.render.BackstackRender
import com.keyflare.elastik.core.render.SingleRender

abstract class DynamicBackstackDestination<Args : Arguments, Router : BaseRouter>(
    val destination: Destination<Args>,
) {
    abstract fun peekRouter(): Router
    abstract fun peekRouterOrNull(): Router?
}

abstract class DynamicSingleDestination<Args : Arguments, Component : Any>(
    val destination: Destination<Args>
) {
    abstract fun peekComponent(): Component
    abstract fun peekComponentOrNull(): Component?
}

interface DynamicRouterTreeBuilder {

    fun <Component : Any> BaseRouter.singleNoArgs(
        destinationId: String,
        componentFactory: () -> Component,
        renderFactory: (Component) -> SingleRender,
    ): DynamicSingleDestination<EmptyArguments, Component>

    fun <Args : Arguments, Component : Any> BaseRouter.single(
        destinationId: String,
        componentFactory: () -> Component,
        renderFactory: (Component) -> SingleRender,
    ): DynamicSingleDestination<Args, Component>

    fun <Router : BaseRouter> BaseRouter.backstackNoArgs(
        destinationId: String,
        routerFactory: (ElastikContext) -> Router,
        renderFactory: (Router) -> BackstackRender,
    ): DynamicBackstackDestination<EmptyArguments, Router>

    fun <Args : Arguments, Router : BaseRouter> BaseRouter.backstack(
        destinationId: String,
        routerFactory: (ElastikContext) -> Router,
        renderFactory: (Router) -> BackstackRender,
    ): DynamicBackstackDestination<Args, Router>
}

internal class DynamicRouterTreeBuilderDelegate : DynamicRouterTreeBuilder {

    override fun <Component : Any> BaseRouter.singleNoArgs(
        destinationId: String,
        componentFactory: () -> Component,
        renderFactory: (Component) -> SingleRender,
    ): DynamicSingleDestination<EmptyArguments, Component> {
        return single(destinationId, componentFactory, renderFactory)
    }

    override fun <Args : Arguments, Component : Any> BaseRouter.single(
        destinationId: String,
        componentFactory: () -> Component,
        renderFactory: (Component) -> SingleRender,
    ): DynamicSingleDestination<Args, Component> {

        addSingleDestinationBinding(destinationId, componentFactory)
        routingContext.sendSingleRenderBinding(destinationId, renderFactory)

        // TODO MVP Solution!!! Refactor this approach
        return object : DynamicSingleDestination<Args, Component>(
            destination = Destination(id = destinationId, isSingle = true)
        ) {
            override fun peekComponent(): Component {
                return runCatching { requireNotNull(peekComponentOrNull()) }
                    .getOrElse { error(Errors.componentNotFound(destinationId)) }
            }

            override fun peekComponentOrNull(): Component? {
                val backstackEntryId = state.state.value
                    .find { it.destinationId == destinationId }
                    ?.id
                    ?: return null

                @Suppress("UNCHECKED_CAST")
                return findComponentOrNull(backstackEntryId) as? Component
            }
        }
    }

    override fun <Router : BaseRouter> BaseRouter.backstackNoArgs(
        destinationId: String,
        routerFactory: (ElastikContext) -> Router,
        renderFactory: (Router) -> BackstackRender,
    ): DynamicBackstackDestination<EmptyArguments, Router> {
        return backstack(destinationId, routerFactory, renderFactory)
    }

    override fun <Args : Arguments, Router : BaseRouter> BaseRouter.backstack(
        destinationId: String,
        routerFactory: (ElastikContext) -> Router,
        renderFactory: (Router) -> BackstackRender,
    ): DynamicBackstackDestination<Args, Router> {

        addBackstackDestinationBinding(destinationId, routerFactory)
        routingContext.sendBackstackRenderBinding(destinationId, renderFactory)

        // TODO MVP Solution!!! Refactor this approach
        return object : DynamicBackstackDestination<Args, Router>(
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
