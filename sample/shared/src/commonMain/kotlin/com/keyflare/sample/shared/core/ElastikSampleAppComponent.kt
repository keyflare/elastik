package com.keyflare.sample.shared.core

import com.keyflare.elastik.compose.render.ComposeBackstackRender
import com.keyflare.elastik.core.ElastikContext

class ElastikSampleAppComponent {
    val elastikContext = ElastikContext.create(rootRender = ComposeBackstackRender())
    val rootRouter = RootRouter(elastikContext)
}
