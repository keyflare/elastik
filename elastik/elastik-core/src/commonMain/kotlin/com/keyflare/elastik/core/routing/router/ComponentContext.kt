package com.keyflare.elastik.core.routing.router

import com.keyflare.elastik.core.routing.back.BackHandler
import com.keyflare.elastik.core.routing.lifecycle.Lifecycle

data class ComponentContext(
    val router: BaseRouter,
    val destinationId: String,
    val entryId: Int,
    val backHandler: BackHandler,
    val lifecycle: Lifecycle,
)
