package com.keyflare.elastik.core.context

import com.keyflare.elastik.core.render.BackstackRender
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

    internal fun detachPlatform() {
        routingContext.detachPlatform()
    }

    private class RenderReceiverImpl(
        private val renderContext: RenderContext,
    ) : RoutingContext.RenderReceiver {

        override fun receiveSingleRender(backstackEntryId: Int, render: SingleRender) {
            renderContext.addSingleRender(backstackEntryId, render)
        }

        override fun receiveBackstackRender(backstackEntryId: Int, render: BackstackRender) {
            renderContext.addBackstackRender(backstackEntryId, render)
        }

        override fun onSingleEntryDestroyed(backstackEntryId: Int) {
            renderContext.removeSingleRender(backstackEntryId)
        }

        override fun onBackstackDestroyed(backstackEntryId: Int) {
            renderContext.removeBackstackRender(backstackEntryId)
        }
    }

    companion object {

        // TODO The initial idea was to define all navigation-connected stuff
        //  in a one graph-like place (hierarchy of routers). But here we
        //  define rootRender in a separate place - before hierarchy of routers
        //  starts building. It is need to refactor this approach.
        fun create(rootRender: BackstackRender): ElastikContext {
            val elastikState = ElastikStateHolder()
            return ElastikContext(
                routingContext = RoutingContextImpl(elastikState),
                renderContext = RenderContextImpl(elastikState)
            ).apply {
                renderContext.addBackstackRender(
                    backstackEntryId = ElastikStateHolder.ROOT_BACKSTACK_ENTRY_ID,
                    render = rootRender,
                )
            }
        }
    }
}
