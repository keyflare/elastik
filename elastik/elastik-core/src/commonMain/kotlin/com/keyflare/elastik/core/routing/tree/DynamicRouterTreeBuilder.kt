package com.keyflare.elastik.core.routing.tree

import com.keyflare.elastik.core.state.Arguments
import com.keyflare.elastik.core.state.EmptyArguments
import com.keyflare.elastik.core.state.find
import com.keyflare.elastik.core.routing.router.BaseRouter
import com.keyflare.elastik.core.routing.router.Destination
import com.keyflare.elastik.core.context.ElastikContext
import com.keyflare.elastik.core.Errors
import com.keyflare.elastik.core.render.StackRender
import com.keyflare.elastik.core.render.SingleRender
import com.keyflare.elastik.core.routing.router.ComponentContext

abstract class DynamicStackDestination<Args : Arguments, Router : BaseRouter>(
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
        componentFactory: (ComponentContext) -> Component,
        renderFactory: (Component) -> SingleRender,
    ): DynamicSingleDestination<EmptyArguments, Component>

    fun <Args : Arguments, Component : Any> BaseRouter.single(
        destinationId: String,
        componentFactory: (ComponentContext) -> Component,
        renderFactory: (Component) -> SingleRender,
    ): DynamicSingleDestination<Args, Component>

    fun <Router : BaseRouter> BaseRouter.stackNoArgs(
        destinationId: String,
        routerFactory: (ElastikContext) -> Router,
        renderFactory: (Router) -> StackRender,
    ): DynamicStackDestination<EmptyArguments, Router>

    fun <Args : Arguments, Router : BaseRouter> BaseRouter.stack(
        destinationId: String,
        routerFactory: (ElastikContext) -> Router,
        renderFactory: (Router) -> StackRender,
    ): DynamicStackDestination<Args, Router>
}

internal class DynamicRouterTreeBuilderDelegate : DynamicRouterTreeBuilder {

    override fun <Component : Any> BaseRouter.singleNoArgs(
        destinationId: String,
        componentFactory: (ComponentContext) -> Component,
        renderFactory: (Component) -> SingleRender,
    ): DynamicSingleDestination<EmptyArguments, Component> {
        return single(destinationId, componentFactory, renderFactory)
    }

    override fun <Args : Arguments, Component : Any> BaseRouter.single(
        destinationId: String,
        componentFactory: (ComponentContext) -> Component,
        renderFactory: (Component) -> SingleRender,
    ): DynamicSingleDestination<Args, Component> {

        @Suppress("UNCHECKED_CAST")
        val renderFactoryImpl = { component: Any -> renderFactory(component as Component) }
        addSingleDestinationBinding(destinationId, componentFactory, renderFactoryImpl)

        // TODO MVP Solution!!! Refactor this approach
        return object : DynamicSingleDestination<Args, Component>(
            destination = Destination(destinationId = destinationId, single = true)
        ) {
            override fun peekComponent(): Component {
                return runCatching { requireNotNull(peekComponentOrNull()) }
                    .getOrElse { error(Errors.componentNotFound(destinationId)) }
            }

            override fun peekComponentOrNull(): Component? {
                val entryId = state.state.value
                    .find { it.destinationId == destinationId }
                    ?.entryId
                    ?: return null

                @Suppress("UNCHECKED_CAST")
                return findComponentOrNull(entryId) as? Component
            }
        }
    }

    override fun <Router : BaseRouter> BaseRouter.stackNoArgs(
        destinationId: String,
        routerFactory: (ElastikContext) -> Router,
        renderFactory: (Router) -> StackRender,
    ): DynamicStackDestination<EmptyArguments, Router> {
        return stack(destinationId, routerFactory, renderFactory)
    }

    override fun <Args : Arguments, Router : BaseRouter> BaseRouter.stack(
        destinationId: String,
        routerFactory: (ElastikContext) -> Router,
        renderFactory: (Router) -> StackRender,
    ): DynamicStackDestination<Args, Router> {

        @Suppress("UNCHECKED_CAST")
        val renderFactoryImpl = { router: BaseRouter -> renderFactory(router as Router) }
        addStackDestinationBinding(destinationId, routerFactory, renderFactoryImpl)

        // TODO MVP Solution!!! Refactor this approach
        return object : DynamicStackDestination<Args, Router>(
            destination = Destination(destinationId = destinationId, single = false),
        ) {
            override fun peekRouter(): Router {
                return runCatching { requireNotNull(peekRouterOrNull()) }
                    .getOrElse { error(Errors.routerNotFound(destinationId)) }
            }

            override fun peekRouterOrNull(): Router? {
                val entryId = state.state.value
                    .find { it.destinationId == destinationId }
                    ?.entryId
                    ?: return null

                @Suppress("UNCHECKED_CAST")
                return findRouterOrNull(entryId) as? Router
            }
        }
    }
}
