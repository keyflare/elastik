package com.keyflare.elastik.core.routing

import com.keyflare.elastik.core.Errors
import com.keyflare.elastik.core.render.BackstackRender
import com.keyflare.elastik.core.render.SingleRender
import com.keyflare.elastik.core.routing.router.BaseRouter
import com.keyflare.elastik.core.state.ElastikStateHolder

// TODO ability to get path for every destination by id
internal interface RoutingContext {

    val state: ElastikStateHolder

    fun obtainIdForNewBackstackEntry(): Int

    fun <Component : Any> sendSingleRenderBinding(
        destinationId: String,
        renderFactory: (Component) -> SingleRender,
    )

    fun <Router : BaseRouter> sendBackstackRenderBinding(
        destinationId: String,
        renderFactory: (Router) -> BackstackRender,
    )

    fun rememberDataForNewRouter(
        destinationId: String,
        backstackEntryId: Int,
        parent: BaseRouter,
    )

    fun getNewRouterData(): NewRouterData

    fun clearNewRouterData()

    fun isDestinationAlreadyExist(destinationId: String): Boolean
}

internal class RoutingContextImpl(override val state: ElastikStateHolder) : RoutingContext {

    private var backstackEntryIdIncrement = 0
        get() = field++

    private var dataForNewRouter: NewRouterData? = ROOT_ROUTER_DATA

    private val addedDestinations = mutableSetOf<String>()

    override fun obtainIdForNewBackstackEntry(): Int {
        // TODO checkMainThread()
        return backstackEntryIdIncrement
    }

    override fun <Component : Any> sendSingleRenderBinding(
        destinationId: String,
        renderFactory: (Component) -> SingleRender
    ) {
        error("Should be overridden")
    }

    override fun <Router : BaseRouter> sendBackstackRenderBinding(
        destinationId: String,
        renderFactory: (Router) -> BackstackRender
    ) {
        error("Should be overridden")
    }

    override fun rememberDataForNewRouter(
        destinationId: String,
        backstackEntryId: Int,
        parent: BaseRouter,
    ) {
        dataForNewRouter = NewRouterData(
            destinationId = destinationId,
            backstackEntryId = backstackEntryId,
            parent = parent,
        )
    }

    override fun getNewRouterData(): NewRouterData {
        return dataForNewRouter ?: error(message = Errors.getNewRouterDataError())
    }

    override fun clearNewRouterData() {
        dataForNewRouter = null
    }

    override fun isDestinationAlreadyExist(destinationId: String): Boolean {
        return !addedDestinations.add(destinationId)
    }
}

internal data class NewRouterData(
    val destinationId: String,
    val backstackEntryId: Int,
    val parent: BaseRouter?,
)

internal val ROOT_ROUTER_DATA = NewRouterData(
    destinationId = "root",
    backstackEntryId = -1,
    parent = null,
)
