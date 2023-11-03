package com.keyflare.elastik.core.render

import com.keyflare.elastik.core.routing.router.BaseRouter
import com.keyflare.elastik.core.state.ElastikStateHolder

internal interface RenderContext {

    fun <Component : Any> addSingleRenderBinding(
        destinationId: String,
        renderFactory: (Component) -> SingleRender,
    )

    fun <Router : BaseRouter> addBackstackRenderBinding(
        destinationId: String,
        renderFactory: (Router) -> BackstackRender,
    )
}

internal class RenderContextImpl(state: ElastikStateHolder) : RenderContext {

    private val singleDestinationsRender =
        mutableMapOf<String, SingleDestinationRenderBinding>()

    private val backstackDestinationsRender =
        mutableMapOf<String, BackstackDestinationRenderBinding>()

    override fun <Component : Any> addSingleRenderBinding(
        destinationId: String,
        renderFactory: (Component) -> SingleRender
    ) {
        val renderFactoryImpl = { component: Any ->
            @Suppress("UNCHECKED_CAST")
            renderFactory(component as Component)
        }
        singleDestinationsRender[destinationId] = SingleDestinationRenderBinding(
            destinationId = destinationId,
            renderFactory = renderFactoryImpl,
        )
    }

    override fun <Router : BaseRouter> addBackstackRenderBinding(
        destinationId: String,
        renderFactory: (Router) -> BackstackRender,
    ) {
        val renderFactoryImpl = { router: BaseRouter ->
            @Suppress("UNCHECKED_CAST")
            renderFactory(router as Router)
        }
        backstackDestinationsRender[destinationId] = BackstackDestinationRenderBinding(
            destinationId = destinationId,
            renderFactory = renderFactoryImpl,
        )
    }

    private class SingleDestinationRenderBinding(
        destinationId: String,
        renderFactory: (Any) -> SingleRender
    )

    private class BackstackDestinationRenderBinding(
        destinationId: String,
        renderFactory: (BaseRouter) -> BackstackRender
    )
}
