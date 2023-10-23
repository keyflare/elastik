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

abstract class StaticRouterDestination<Args : Arguments, Router : BaseRouter>(
    val destination: Destination<Args>,
) {
    abstract val router: Router
}

interface StaticRouterTreeBuilder {

    fun BaseRouter.singleNoArgs(
        destinationId: String,
        render: Render,
    ): Destination<EmptyArguments>

    fun <Args : Arguments> BaseRouter.single(
        destinationId: String,
        args: Args,
        render: Render,
    ): Destination<Args>

    fun <Router : BaseRouter> BaseRouter.backstackNoArgs(
        destinationId: String,
        render: Render,
        routerFactory: (ElastikContext) -> Router,
    ): StaticRouterDestination<EmptyArguments, Router>

    fun <Args : Arguments, Router : BaseRouter> BaseRouter.backstack(
        destinationId: String,
        args: Args,
        render: Render,
        routerFactory: (ElastikContext) -> Router,
    ): StaticRouterDestination<Args, Router>
}

class StaticRouterTreeBuilderDelegate : StaticRouterTreeBuilder {

    override fun BaseRouter.singleNoArgs(
        destinationId: String,
        render: Render,
    ): Destination<EmptyArguments> {
        return single(destinationId, EmptyArguments, render)
    }

    override fun <Args : Arguments> BaseRouter.single(
        destinationId: String,
        args: Args,
        render: Render,
    ): Destination<Args> {
        routingContext.addRenderBinding(destinationId, render)
        addSingleDestinationBinding(destinationId)

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

        return Destination(id = destinationId, isSingle = true)
    }

    override fun <Router : BaseRouter> BaseRouter.backstackNoArgs(
        destinationId: String,
        render: Render,
        routerFactory: (ElastikContext) -> Router
    ): StaticRouterDestination<EmptyArguments, Router> {
        return backstack(destinationId, EmptyArguments, render, routerFactory)
    }

    override fun <Args : Arguments, Router : BaseRouter> BaseRouter.backstack(
        destinationId: String,
        args: Args,
        render: Render,
        routerFactory: (ElastikContext) -> Router,
    ): StaticRouterDestination<Args, Router> {
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
        return object : StaticRouterDestination<Args, Router>(
            destination = Destination(id = destinationId, isSingle = false),
        ) {
            @Suppress("UNCHECKED_CAST")
            override val router: Router
                get() = state.state.value
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
