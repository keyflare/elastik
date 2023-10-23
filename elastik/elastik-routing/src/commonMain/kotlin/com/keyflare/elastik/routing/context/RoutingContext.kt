package com.keyflare.elastik.routing.context

import com.keyflare.elastik.core.ElastikStateHolder
import com.keyflare.elastik.render.Render
import com.keyflare.elastik.render.RenderStub
import com.keyflare.elastik.routing.Errors
import com.keyflare.elastik.routing.router.BaseRouter

// TODO ability to get path for every destination by id
internal interface RoutingContext {

    val state: ElastikStateHolder

    fun obtainIdForNewBackstackEntry(): Int

    fun addRenderBinding(
        destinationId: String,
        render: Render,
    )

    fun rememberDataForNewRouter(
        destinationId: String,
        backstackEntryId: Int,
        parent: BaseRouter,
        render: Render,
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

    override fun addRenderBinding(destinationId: String, render: Render) {
        // TODO implement
    }

    override fun rememberDataForNewRouter(
        destinationId: String,
        backstackEntryId: Int,
        parent: BaseRouter,
        render: Render
    ) {
        dataForNewRouter = NewRouterData(
            destinationId = destinationId,
            backstackEntryId = backstackEntryId,
            parent = parent,
            render = render,
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
    val render: Render,
)

internal val ROOT_ROUTER_DATA = NewRouterData(
    destinationId = "root",
    backstackEntryId = -1,
    parent = null,
    render = RenderStub,
)
