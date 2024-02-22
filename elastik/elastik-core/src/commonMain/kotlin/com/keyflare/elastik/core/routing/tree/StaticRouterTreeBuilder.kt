package com.keyflare.elastik.core.routing.tree

import com.keyflare.elastik.core.state.Arguments
import com.keyflare.elastik.core.state.Stack
import com.keyflare.elastik.core.state.StackTransaction
import com.keyflare.elastik.core.state.StackTransformation
import com.keyflare.elastik.core.state.EmptyArguments
import com.keyflare.elastik.core.state.Single
import com.keyflare.elastik.core.state.find
import com.keyflare.elastik.core.context.ElastikContext
import com.keyflare.elastik.core.Errors
import com.keyflare.elastik.core.render.StackRender
import com.keyflare.elastik.core.render.SingleRender
import com.keyflare.elastik.core.routing.router.BaseRouter
import com.keyflare.elastik.core.routing.component.ComponentContext
import com.keyflare.elastik.core.util.requireNotNull

class StaticStackDestination<Args : Arguments, Router : BaseRouter> internal constructor(
    val destination: Destination<Args>,
    val router: Router,
)

class StaticSingleDestination<Args : Arguments, Component : Any> internal constructor(
    val destination: Destination<Args>,
    val component: Component,
)

interface StaticRouterTreeBuilder {

    fun <Component : Any> BaseRouter.singleNoArgs(
        destinationId: String,
        componentFactory: (ComponentContext) -> Component,
        renderFactory: (Component) -> SingleRender,
    ): StaticSingleDestination<EmptyArguments, Component>

    fun <Args : Arguments, Component : Any> BaseRouter.single(
        destinationId: String,
        args: Args,
        componentFactory: (ComponentContext) -> Component,
        renderFactory: (Component) -> SingleRender,
    ): StaticSingleDestination<Args, Component>

    fun <Router : BaseRouter> BaseRouter.stackNoArgs(
        destinationId: String,
        routerFactory: (ElastikContext) -> Router,
        renderFactory: (Router) -> StackRender,
    ): StaticStackDestination<EmptyArguments, Router>

    fun <Args : Arguments, Router : BaseRouter> BaseRouter.stack(
        destinationId: String,
        args: Args,
        routerFactory: (ElastikContext) -> Router,
        renderFactory: (Router) -> StackRender,
    ): StaticStackDestination<Args, Router>
}

internal class StaticRouterTreeBuilderDelegate : StaticRouterTreeBuilder {

    override fun <Component : Any> BaseRouter.singleNoArgs(
        destinationId: String,
        componentFactory: (ComponentContext) -> Component,
        renderFactory: (Component) -> SingleRender,
    ): StaticSingleDestination<EmptyArguments, Component> {
        return single(destinationId, EmptyArguments, componentFactory, renderFactory)
    }

    override fun <Args : Arguments, Component : Any> BaseRouter.single(
        destinationId: String,
        args: Args,
        componentFactory: (ComponentContext) -> Component,
        renderFactory: (Component) -> SingleRender,
    ): StaticSingleDestination<Args, Component> {

        @Suppress("UNCHECKED_CAST")
        val renderFactoryImpl = { component: Any -> renderFactory(component as Component) }
        addSingleDestinationBinding(destinationId, componentFactory, renderFactoryImpl)

        val addEntryTransformation = StackTransformation(
            entryId = stack.entryId,
            transformation = { entries ->
                val newEntry = Single(
                    entryId = routingContext.obtainNewEntryId(),
                    args = args,
                    destinationId = destinationId,
                )
                entries + newEntry
            },
        )
        state.pushTransaction(StackTransaction(listOf(addEntryTransformation)))

        val component = state.state.value
            .find { it.destinationId == destinationId }
            .requireNotNull()
            .let {
                val component = findComponentOrNull(it.entryId)
                    ?: error(Errors.componentNotFound(it.destinationId))
                @Suppress("UNCHECKED_CAST")
                component as Component
            }

        return StaticSingleDestination(
            destination = Destination(destinationId = destinationId, single = true),
            component = component,
        )
    }

    override fun <Router : BaseRouter> BaseRouter.stackNoArgs(
        destinationId: String,
        routerFactory: (ElastikContext) -> Router,
        renderFactory: (Router) -> StackRender,
    ): StaticStackDestination<EmptyArguments, Router> {
        return stack(destinationId, EmptyArguments, routerFactory, renderFactory)
    }

    override fun <Args : Arguments, Router : BaseRouter> BaseRouter.stack(
        destinationId: String,
        args: Args,
        routerFactory: (ElastikContext) -> Router,
        renderFactory: (Router) -> StackRender,
    ): StaticStackDestination<Args, Router> {
        // TODO check main thread

        @Suppress("UNCHECKED_CAST")
        val renderFactoryImpl = { router: BaseRouter -> renderFactory(router as Router) }
        addStackDestinationBinding(destinationId, routerFactory, renderFactoryImpl)

        val addEntryTransformation = StackTransformation(
            entryId = stack.entryId,
            transformation = { entries ->
                val newEntry = Stack(
                    entryId = routingContext.obtainNewEntryId(),
                    args = args,
                    destinationId = destinationId,
                    entries = emptyList(),
                )
                entries + newEntry
            },
        )
        state.pushTransaction(StackTransaction(listOf(addEntryTransformation)))

        val router = state.state.value
            .find { it.destinationId == destinationId }
            .requireNotNull()
            .let {
                val router = findRouterOrNull(it.entryId)
                    ?: error(Errors.routerNotFound(it.destinationId))
                @Suppress("UNCHECKED_CAST")
                router as Router
            }

        return StaticStackDestination(
            destination = Destination(destinationId = destinationId, single = false),
            router = router,
        )
    }
}
