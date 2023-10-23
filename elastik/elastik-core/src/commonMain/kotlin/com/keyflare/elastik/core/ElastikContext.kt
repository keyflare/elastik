package com.keyflare.elastik.core

import com.keyflare.elastik.core.render.RenderContext
import com.keyflare.elastik.core.render.RenderContextImpl
import com.keyflare.elastik.core.routing.RoutingContext
import com.keyflare.elastik.core.routing.RoutingContextImpl
import com.keyflare.elastik.core.state.ElastikStateHolder

class ElastikContext private constructor(
    state: ElastikStateHolder,
) :
    RenderContext by RenderContextImpl(state),
    RoutingContext by RoutingContextImpl(state) {

    internal companion object {

        fun create(): ElastikContext {
            return ElastikContext(state = ElastikStateHolder())
        }
    }
}
