package com.keyflare.elastik.core.routing

import com.keyflare.elastik.core.Errors
import com.keyflare.elastik.core.context.ElastikPlatform
import com.keyflare.elastik.core.render.StackRender
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

    fun obtainNewEntryId(): Int

    fun sendSingleRender(
        entryId: Int,
        render: SingleRender,
    )

    fun sendStackRender(
        entryId: Int,
        render: StackRender,
    )

    fun onSingleDestroyed(entryId: Int)

    fun onStackDestroyed(entryId: Int)

    fun rememberNewRouterData(
        destinationId: String,
        entryId: Int,
        parent: BaseRouter,
    )

    fun getNewRouterData(): NewRouterData

    fun clearNewRouterData()

    fun dispatchBackEvent()

    interface RenderReceiver {

        fun receiveSingleRender(
            entryId: Int,
            render: SingleRender,
        )

        fun receiveStackRender(
            entryId: Int,
            render: StackRender,
        )

        fun onSingleEntryDestroyed(entryId: Int)

        fun onStackDestroyed(entryId: Int)
    }
}

internal class RoutingContextImpl(
    override val state: ElastikStateHolder,
) : RoutingContext {

    private var entryIdIncrement = 0
        get() = field++

    private var dataForNewRouter: NewRouterData? = ROOT_ROUTER_DATA

    private var renderReceiver: RoutingContext.RenderReceiver? = null

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

    override fun obtainNewEntryId(): Int {
        // TODO checkMainThread()
        return entryIdIncrement
    }

    override fun sendSingleRender(
        entryId: Int,
        render: SingleRender,
    ) {
        renderReceiver?.receiveSingleRender(entryId, render)
    }

    override fun sendStackRender(
        entryId: Int,
        render: StackRender,
    ) {
        renderReceiver?.receiveStackRender(entryId, render)
    }

    override fun onSingleDestroyed(entryId: Int) {
        renderReceiver?.onSingleEntryDestroyed(entryId)
    }

    override fun onStackDestroyed(entryId: Int) {
        renderReceiver?.onStackDestroyed(entryId)
    }

    override fun rememberNewRouterData(
        destinationId: String,
        entryId: Int,
        parent: BaseRouter,
    ) {
        dataForNewRouter = NewRouterData(
            destinationId = destinationId,
            entryId = entryId,
            parent = parent,
        )
    }

    override fun getNewRouterData(): NewRouterData {
        return dataForNewRouter ?: error(message = Errors.getNewRouterDataError())
    }

    override fun clearNewRouterData() {
        dataForNewRouter = null
    }

    override fun dispatchBackEvent() {

    }
}

internal data class NewRouterData(
    val destinationId: String,
    val entryId: Int,
    val parent: BaseRouter?,
)

internal val ROOT_ROUTER_DATA = NewRouterData(
    destinationId = "root",
    entryId = -1,
    parent = null,
)
