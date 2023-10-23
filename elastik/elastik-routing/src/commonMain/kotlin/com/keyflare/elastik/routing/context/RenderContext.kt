package com.keyflare.elastik.routing.context

import com.keyflare.elastik.core.ElastikStateHolder

internal interface RenderContext

internal class RenderContextImpl(state: ElastikStateHolder) : RenderContext
