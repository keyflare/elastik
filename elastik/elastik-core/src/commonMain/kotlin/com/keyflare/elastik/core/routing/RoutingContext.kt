package com.keyflare.elastik.core.routing

import com.keyflare.elastik.core.Errors
import com.keyflare.elastik.core.context.ElastikPlatform
import com.keyflare.elastik.core.render.BackstackRender
import com.keyflare.elastik.core.render.SingleRender
import com.keyflare.elastik.core.routing.backevents.BackEventsDispatcher
import com.keyflare.elastik.core.routing.router.BaseRouter
import com.keyflare.elastik.core.state.ElastikStateHolder

// TODO ability to get path for every destination by id
internal interface RoutingContext {

    val state: ElastikStateHolder
    val backEventsDispatcher: BackEventsDispatcher

    fun registerRenderReceiver(receiver: RenderReceiver)

    fun attachPlatform(platform: ElastikPlatform)

    fun detachPlatform()

    fun obtainIdForNewBackstackEntry(): Int

    fun sendSingleRender(
        backstackEntryId: Int,
        render: SingleRender,
    )

    fun sendBackstackRender(
        backstackEntryId: Int,
        render: BackstackRender,
    )

    fun onSingleDestroyed(backstackEntryId: Int)

    fun onBackstackDestroyed(backstackEntryId: Int)

    fun rememberDataForNewRouter(
        destinationId: String,
        backstackEntryId: Int,
        parent: BaseRouter,
    )

    fun getNewRouterData(): NewRouterData

    fun clearNewRouterData()

    fun isDestinationAlreadyExist(destinationId: String): Boolean

    fun dispatchBackEvent()

    interface RenderReceiver {

        fun receiveSingleRender(
            backstackEntryId: Int,
            render: SingleRender,
        )

        fun receiveBackstackRender(
            backstackEntryId: Int,
            render: BackstackRender,
        )

        fun onSingleEntryDestroyed(backstackEntryId: Int)

        fun onBackstackDestroyed(backstackEntryId: Int)
    }
}

internal class RoutingContextImpl(
    override val state: ElastikStateHolder,
) : RoutingContext {

    private var backstackEntryIdIncrement = 0
        get() = field++

    private var dataForNewRouter: NewRouterData? = ROOT_ROUTER_DATA

    private var renderReceiver: RoutingContext.RenderReceiver? = null

    private val addedDestinations = mutableSetOf<String>()

    override val backEventsDispatcher = BackEventsDispatcher()

    override fun registerRenderReceiver(receiver: RoutingContext.RenderReceiver) {
        renderReceiver = receiver
    }

    override fun attachPlatform(platform: ElastikPlatform) {
        platform.backEventsSource.subscribe {
            backEventsDispatcher.dispatch()
        }
    }

    override fun detachPlatform() {

    }

    override fun obtainIdForNewBackstackEntry(): Int {
        // TODO checkMainThread()
        return backstackEntryIdIncrement
    }

    override fun sendSingleRender(
        backstackEntryId: Int,
        render: SingleRender,
    ) {
        renderReceiver?.receiveSingleRender(backstackEntryId, render)
    }

    override fun sendBackstackRender(
        backstackEntryId: Int,
        render: BackstackRender,
    ) {
        renderReceiver?.receiveBackstackRender(backstackEntryId, render)
    }

    override fun onSingleDestroyed(backstackEntryId: Int) {
        renderReceiver?.onSingleEntryDestroyed(backstackEntryId)
    }

    override fun onBackstackDestroyed(backstackEntryId: Int) {
        renderReceiver?.onBackstackDestroyed(backstackEntryId)
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

    override fun dispatchBackEvent() {

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
