package com.keyflare.elastik.core.render

import com.keyflare.elastik.core.state.ElastikStateHolder

internal interface RenderContext

internal class RenderContextImpl(state: ElastikStateHolder) : RenderContext
