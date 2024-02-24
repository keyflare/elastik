package com.keyflare.elastik.core.routing.navigation

import com.keyflare.elastik.core.context.ElastikContext
import com.keyflare.elastik.core.render.NoRender
import com.keyflare.elastik.core.routing.router.DynamicRouter
import com.keyflare.elastik.core.state.Arguments

class DynamicNavigationTestRouter(context: ElastikContext) : DynamicRouter(context) {

    val screenWithoutArgs = singleNoArgs(
        destinationId = "screenWithoutArgs",
        componentFactory = {},
        renderFactory = { NoRender },
    )

    val screenWithArgs = single<ArgsStub, Unit>(
        destinationId = "screenWithArgs",
        componentFactory = {},
        renderFactory = { NoRender },
    )

    val screenA = singleNoArgs(
        destinationId = "A",
        componentFactory = {},
        renderFactory = { NoRender },
    )

    val screenB = singleNoArgs(
        destinationId = "B",
        componentFactory = {},
        renderFactory = { NoRender },
    )

    val screenC = singleNoArgs(
        destinationId = "C",
        componentFactory = {},
        renderFactory = { NoRender },
    )

    val screenD = singleNoArgs(
        destinationId = "D",
        componentFactory = {},
        renderFactory = { NoRender },
    )
}

data class ArgsStub(val tag: String) : Arguments
