package com.keyflare.elastik.core.routing.tree

import com.keyflare.elastik.core.state.Arguments
import com.keyflare.elastik.core.state.Backstack
import com.keyflare.elastik.core.state.BackstackTransaction
import com.keyflare.elastik.core.state.BackstackTransformation
import com.keyflare.elastik.core.state.EmptyArguments
import com.keyflare.elastik.core.state.SingleEntry
import com.keyflare.elastik.core.state.find
import com.keyflare.elastik.core.context.ElastikContext
import com.keyflare.elastik.core.Errors
import com.keyflare.elastik.core.render.BackstackRender
import com.keyflare.elastik.core.render.SingleRender
import com.keyflare.elastik.core.routing.router.BackHandler
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
        componentFactory: (BackHandler) -> Component,
        renderFactory: (Component) -> SingleRender,
    ): StaticSingleDestination<EmptyArguments, Component>

    fun <Args : Arguments, Component : Any> BaseRouter.single(
        destinationId: String,
        args: Args,
        componentFactory: (BackHandler) -> Component,
        renderFactory: (Component) -> SingleRender,
    ): StaticSingleDestination<Args, Component>

    fun <Router : BaseRouter> BaseRouter.backstackNoArgs(
        destinationId: String,
        routerFactory: (ElastikContext) -> Router,
        renderFactory: (Router) -> BackstackRender,
    ): StaticBackstackDestination<EmptyArguments, Router>

    fun <Args : Arguments, Router : BaseRouter> BaseRouter.backstack(
        destinationId: String,
        args: Args,
        routerFactory: (ElastikContext) -> Router,
        renderFactory: (Router) -> BackstackRender,
    ): StaticBackstackDestination<Args, Router>
}

internal class StaticRouterTreeBuilderDelegate : StaticRouterTreeBuilder {

    override fun <Component : Any> BaseRouter.singleNoArgs(
        destinationId: String,
        componentFactory: (BackHandler) -> Component,
        renderFactory: (Component) -> SingleRender,
    ): StaticSingleDestination<EmptyArguments, Component> {
        return single(destinationId, EmptyArguments, componentFactory, renderFactory)
    }

    override fun <Args : Arguments, Component : Any> BaseRouter.single(
        destinationId: String,
        args: Args,
        componentFactory: (BackHandler) -> Component,
        renderFactory: (Component) -> SingleRender,
    ): StaticSingleDestination<Args, Component> {

        @Suppress("UNCHECKED_CAST")
        val renderFactoryImpl = { component: Any -> renderFactory(component as Component) }
        addSingleDestinationBinding(destinationId, componentFactory, renderFactoryImpl)

        val addEntryTransformation = BackstackTransformation(
            backstackId = backstack?.id ?: error(Errors.noBackstackAssociated(backstackEntryId)),
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
        routerFactory: (ElastikContext) -> Router,
        renderFactory: (Router) -> BackstackRender,
    ): StaticBackstackDestination<EmptyArguments, Router> {
        return backstack(destinationId, EmptyArguments, routerFactory, renderFactory)
    }

    override fun <Args : Arguments, Router : BaseRouter> BaseRouter.backstack(
        destinationId: String,
        args: Args,
        routerFactory: (ElastikContext) -> Router,
        renderFactory: (Router) -> BackstackRender,
    ): StaticBackstackDestination<Args, Router> {
        // TODO check main thread

        @Suppress("UNCHECKED_CAST")
        val renderFactoryImpl = { router: BaseRouter -> renderFactory(router as Router) }
        addBackstackDestinationBinding(destinationId, routerFactory, renderFactoryImpl)

        val addEntryTransformation = BackstackTransformation(
            backstackId = backstack?.id ?: error(Errors.noBackstackAssociated(backstackEntryId)),
            transformation = { entries ->
                val newEntry = Backstack(
                    id = routingContext.obtainIdForNewBackstackEntry(),
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
