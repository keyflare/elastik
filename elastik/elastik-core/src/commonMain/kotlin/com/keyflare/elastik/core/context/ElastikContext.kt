package com.keyflare.elastik.core.context

import com.keyflare.elastik.core.render.StackRender
import com.keyflare.elastik.core.render.RenderContext
import com.keyflare.elastik.core.render.RenderContextImpl
import com.keyflare.elastik.core.render.SingleRender
import com.keyflare.elastik.core.routing.RoutingContext
import com.keyflare.elastik.core.routing.RoutingContextImpl
import com.keyflare.elastik.core.state.ElastikStateHolder

class ElastikContext private constructor(
    val renderContext: RenderContext,
    internal val routingContext: RoutingContext,
) {
    init {
        routingContext.registerRenderReceiver(RenderReceiverImpl(renderContext))
    }

    internal fun attachPlatform(platform: ElastikPlatform) {
        routingContext.attachPlatform(platform)
    }

    private class RenderReceiverImpl(
        private val renderContext: RenderContext,
    ) : RoutingContext.RenderReceiver {

        override fun receiveSingleRender(entryId: Int, render: SingleRender) {
            renderContext.addSingleRender(entryId, render)
        }

        override fun receiveStackRender(entryId: Int, render: StackRender) {
            renderContext.addStackRender(entryId, render)
        }

        override fun onSingleEntryDestroyed(entryId: Int) {
            renderContext.removeSingleRender(entryId)
        }

        override fun onStackDestroyed(entryId: Int) {
            renderContext.removeStackRender(entryId)
        }
    }

    companion object {

        // TODO The initial idea was to define all navigation-connected stuff
        //  in a one graph-like place (hierarchy of routers). But here we
        //  define rootRender in a separate place - before hierarchy of routers
        //  starts building. It is need to refactor this approach.
        fun create(rootRender: StackRender): ElastikContext {
            val elastikState = ElastikStateHolder()
            return ElastikContext(
                routingContext = RoutingContextImpl(elastikState),
                renderContext = RenderContextImpl(elastikState)
            ).apply {
                renderContext.addStackRender(
                    entryId = ElastikStateHolder.ROOT_ENTRY_ID,
                    render = rootRender,
                )
            }
        }
    }
}
