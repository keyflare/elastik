package com.keyflare.elastik.routing.context

import com.keyflare.elastik.core.ElastikStateHolder

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
