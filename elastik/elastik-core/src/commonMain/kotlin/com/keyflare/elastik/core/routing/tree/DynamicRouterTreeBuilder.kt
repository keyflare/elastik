package com.keyflare.elastik.core.routing.tree

import com.keyflare.elastik.core.state.Arguments
import com.keyflare.elastik.core.state.EmptyArguments
import com.keyflare.elastik.core.state.find
import com.keyflare.elastik.core.routing.router.BaseRouter
import com.keyflare.elastik.core.context.ElastikContext
import com.keyflare.elastik.core.Errors
import com.keyflare.elastik.core.render.StackRender
import com.keyflare.elastik.core.render.SingleRender
import com.keyflare.elastik.core.routing.component.ComponentContext

class DynamicStackDestination<Args : Arguments, Router : BaseRouter> internal constructor(
    val destination: Destination<Args>,
    private val parent: BaseRouter,
) {
    fun peekRouter(): Router {
        return runCatching { requireNotNull(peekRouterOrNull()) }
            .getOrElse { error(Errors.routerNotFound(destination.destinationId)) }
    }

    fun peekRouterOrNull(): Router? {
        val entryId = parent.state.state.value
            .find { it.destinationId == destination.destinationId }
            ?.entryId
            ?: return null

        @Suppress("UNCHECKED_CAST")
        return parent.findRouterOrNull(entryId) as? Router
    }
}

class DynamicSingleDestination<Args : Arguments, Component : Any> internal constructor(
    val destination: Destination<Args>,
    private val parent: BaseRouter,
) {
    fun peekComponent(): Component {
        return runCatching { requireNotNull(peekComponentOrNull()) }
            .getOrElse { error(Errors.componentNotFound(destination.destinationId)) }
    }

    fun peekComponentOrNull(): Component? {
        val entryId = parent.state.state.value
            .find { it.destinationId == destination.destinationId }
            ?.entryId
            ?: return null

        @Suppress("UNCHECKED_CAST")
        return parent.findComponentOrNull(entryId) as? Component
    }
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

        return DynamicSingleDestination(
            destination = Destination(destinationId = destinationId, single = true),
            parent = this,
        )
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

        return DynamicStackDestination(
            destination = Destination(destinationId = destinationId, single = false),
            parent = this,
        )
    }
}
