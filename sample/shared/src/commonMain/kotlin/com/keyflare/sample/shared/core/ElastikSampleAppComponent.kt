package com.keyflare.sample.shared.core

import com.keyflare.elastik.compose.render.ComposeStackRender
import com.keyflare.elastik.core.context.ElastikContext

class ElastikSampleAppComponent {
    val elastikContext = ElastikContext.create(rootRender = ComposeStackRender())
    val rootRouter = RootRouter(elastikContext)
}
