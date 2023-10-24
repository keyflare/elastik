package com.keyflare.elastik.core.routing.tree

import com.keyflare.elastik.core.state.Arguments
import com.keyflare.elastik.core.state.Backstack
import com.keyflare.elastik.core.state.BackstackTransaction
import com.keyflare.elastik.core.state.BackstackTransformation
import com.keyflare.elastik.core.state.EmptyArguments
import com.keyflare.elastik.core.state.SingleEntry
import com.keyflare.elastik.core.state.find
import com.keyflare.elastik.core.ElastikContext
import com.keyflare.elastik.core.Errors
import com.keyflare.elastik.core.render.Render
import com.keyflare.elastik.core.routing.router.BaseRouter
import com.keyflare.elastik.core.routing.router.Destination
import com.keyflare.elastik.core.util.requireNotNull

abstract class StaticBackstackDestination<Args : Arguments, Router : BaseRouter>(
    val destination: Destination<Args>,
) {
    abstract val router: Router
}

abstract class StaticSingleDestination<Args : Arguments, Component : Any>(
    val destination: Destination<Args>,
) {
    abstract val component: Component
}

interface StaticRouterTreeBuilder {

    fun <Component : Any> BaseRouter.singleNoArgs(
        destinationId: String,
        render: Render,
        componentFactory: () -> Component,
    ): StaticSingleDestination<EmptyArguments, Component>

    fun <Args : Arguments, Component : Any> BaseRouter.single(
        destinationId: String,
        args: Args,
        render: Render,
        componentFactory: () -> Component,
    ): StaticSingleDestination<Args, Component>

    fun <Router : BaseRouter> BaseRouter.backstackNoArgs(
        destinationId: String,
        render: Render,
        routerFactory: (ElastikContext) -> Router,
    ): StaticBackstackDestination<EmptyArguments, Router>

    fun <Args : Arguments, Router : BaseRouter> BaseRouter.backstack(
        destinationId: String,
        args: Args,
        render: Render,
        routerFactory: (ElastikContext) -> Router,
    ): StaticBackstackDestination<Args, Router>
}

class StaticRouterTreeBuilderDelegate : StaticRouterTreeBuilder {

    override fun <Component : Any> BaseRouter.singleNoArgs(
        destinationId: String,
        render: Render,
        componentFactory: () -> Component,
    ): StaticSingleDestination<EmptyArguments, Component> {
        return single(destinationId, EmptyArguments, render, componentFactory)
    }

    override fun <Args : Arguments, Component : Any> BaseRouter.single(
        destinationId: String,
        args: Args,
        render: Render,
        componentFactory: () -> Component,
    ): StaticSingleDestination<Args, Component> {
        routingContext.addRenderBinding(destinationId, render)
        addSingleDestinationBinding(destinationId, componentFactory)

        val addEntryTransformation = BackstackTransformation(
            backstackId = backstack.id,
            transformation = { entries ->
                val newEntry = SingleEntry(
                    id = routingContext.obtainIdForNewBackstackEntry(),
                    args = args,
                    destinationId = destinationId,
                )
                entries + newEntry
            },
        )
        state.pushTransaction(BackstackTransaction(listOf(addEntryTransformation)))

        return object : StaticSingleDestination<Args, Component>(
            destination = Destination(id = destinationId, isSingle = true),
        ) {
            override val component: Component by lazy {
                @Suppress("UNCHECKED_CAST")
                state.state.value
                    .find { it.destinationId == destinationId }
                    .requireNotNull()
                    .let {
                        val component = findComponentOrNull(it.id)
                            ?: error(Errors.componentNotFound(it.destinationId))
                        component as Component
                    }
            }
        }
    }

    override fun <Router : BaseRouter> BaseRouter.backstackNoArgs(
        destinationId: String,
        render: Render,
        routerFactory: (ElastikContext) -> Router
    ): StaticBackstackDestination<EmptyArguments, Router> {
        return backstack(destinationId, EmptyArguments, render, routerFactory)
    }

    override fun <Args : Arguments, Router : BaseRouter> BaseRouter.backstack(
        destinationId: String,
        args: Args,
        render: Render,
        routerFactory: (ElastikContext) -> Router,
    ): StaticBackstackDestination<Args, Router> {
        // TODO check main thread

        addBackstackDestinationBinding(destinationId, routerFactory)
        routingContext.addRenderBinding(destinationId, render)

        val backstackEntryId = routingContext.obtainIdForNewBackstackEntry()
        val addEntryTransformation = BackstackTransformation(
            backstackId = backstack.id,
            transformation = { entries ->
                val newEntry = Backstack(
                    id = backstackEntryId,
                    args = args,
                    destinationId = destinationId,
                    entries = emptyList(),
                )
                entries + newEntry
            },
        )

        state.pushTransaction(BackstackTransaction(listOf(addEntryTransformation)))

        // TODO MVP Solution!!! Refactor this approach
        return object : StaticBackstackDestination<Args, Router>(
            destination = Destination(id = destinationId, isSingle = false),
        ) {
            override val router: Router by lazy {
                @Suppress("UNCHECKED_CAST")
                state.state.value
                    .find { it.destinationId == destinationId }
                    .requireNotNull()
                    .let {
                        val router = findRouterOrNull(it.id)
                            ?: error(Errors.routerNotFound(it.destinationId))
                        router as Router
                    }
            }
        }
    }
}
