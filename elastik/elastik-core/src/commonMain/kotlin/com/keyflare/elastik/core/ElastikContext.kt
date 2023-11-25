package com.keyflare.elastik.core

import com.keyflare.elastik.core.render.BackstackRender
import com.keyflare.elastik.core.render.RenderContext
import com.keyflare.elastik.core.render.RenderContextImpl
import com.keyflare.elastik.core.render.SingleRender
import com.keyflare.elastik.core.routing.RoutingContext
import com.keyflare.elastik.core.routing.RoutingContextImpl
import com.keyflare.elastik.core.state.ElastikStateHolder

class ElastikContext private constructor(
    state: ElastikStateHolder,
) :
    RenderContext by RenderContextImpl(state),
    RoutingContext by RoutingContextImpl(state) {

    override fun sendSingleRender(
        backstackEntryId: Int,
        render: SingleRender,
    ) {
        addSingleRender(backstackEntryId, render)
    }

    override fun sendBackstackRender(
        backstackEntryId: Int,
        render: BackstackRender,
    ) {
        addBackstackRender(backstackEntryId, render)
    }

    override fun onSingleDestroyed(backstackEntryId: Int) {
        removeSingleRender(backstackEntryId)
    }

    override fun onBackstackDestroyed(backstackEntryId: Int) {
        removeBackstackRender(backstackEntryId)
    }

    companion object {

        // TODO The initial idea was to define all navigation-connected stuff
        //  in a one graph-like place (hierarchy of routers). But here we
        //  define rootRender in a separate place - before hierarchy of routers
        //  starts building. It is need to refactor this approach.
        fun create(rootRender: BackstackRender): ElastikContext {
            return ElastikContext(state = ElastikStateHolder()).apply {
                addBackstackRender(
                    backstackEntryId = ElastikStateHolder.ROOT_BACKSTACK_ENTRY_ID,
                    render = rootRender,
                )
            }
        }
    }
}
