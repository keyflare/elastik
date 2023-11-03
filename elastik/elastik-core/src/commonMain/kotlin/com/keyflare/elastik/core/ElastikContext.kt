package com.keyflare.elastik.core

import com.keyflare.elastik.core.render.BackstackRender
import com.keyflare.elastik.core.render.RenderContext
import com.keyflare.elastik.core.render.RenderContextImpl
import com.keyflare.elastik.core.render.SingleRender
import com.keyflare.elastik.core.routing.RoutingContext
import com.keyflare.elastik.core.routing.RoutingContextImpl
import com.keyflare.elastik.core.routing.router.BaseRouter
import com.keyflare.elastik.core.state.ElastikStateHolder

class ElastikContext private constructor(
    state: ElastikStateHolder,
) :
    RenderContext by RenderContextImpl(state),
    RoutingContext by RoutingContextImpl(state) {

    override fun <Component : Any> sendSingleRenderBinding(
        destinationId: String,
        renderFactory: (Component) -> SingleRender
    ) {
        addSingleRenderBinding(destinationId, renderFactory)
    }

    override fun <Router : BaseRouter> sendBackstackRenderBinding(
        destinationId: String,
        renderFactory: (Router) -> BackstackRender
    ) {
        addBackstackRenderBinding(destinationId, renderFactory)
    }

    internal companion object {

        fun create(): ElastikContext {
            return ElastikContext(state = ElastikStateHolder())
        }
    }
}
