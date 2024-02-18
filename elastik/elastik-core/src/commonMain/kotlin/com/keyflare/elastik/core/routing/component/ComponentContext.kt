package com.keyflare.elastik.core.routing.component

import com.keyflare.elastik.core.routing.router.BaseRouter

data class ComponentContext(
    val router: BaseRouter,
    val destinationId: String,
    val entryId: Int,
    val backHandler: BackHandler,
    val lifecycle: Lifecycle,
)
