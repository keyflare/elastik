package com.keyflare.sample.shared.core

import com.keyflare.elastik.core.ElastikContext

object ElastikSampleApp {
    val elastikContext = ElastikContext.create()
    val rootRouter = RootRouter(elastikContext)
}
