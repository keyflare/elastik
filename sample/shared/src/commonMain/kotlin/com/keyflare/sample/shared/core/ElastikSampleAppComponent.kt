package com.keyflare.sample.shared.core

import com.keyflare.elastik.core.ElastikContext

class ElastikSampleAppComponent {
    val elastikContext = ElastikContext.create()
    val rootRouter = RootRouter(elastikContext)
}
