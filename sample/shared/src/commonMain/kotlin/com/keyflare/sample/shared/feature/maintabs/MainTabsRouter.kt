package com.keyflare.sample.shared.feature.maintabs

import com.keyflare.elastik.compose.render.ComposeSingleRender
import com.keyflare.elastik.core.ElastikContext
import com.keyflare.elastik.core.routing.router.StaticRouter
import com.keyflare.sample.shared.core.RootRouter
import com.keyflare.sample.shared.feature.mainscreen.MainScreen
import com.keyflare.sample.shared.feature.mainscreen.MainScreenComponent

class MainTabsRouter(context: ElastikContext) : StaticRouter(context) {

    private val rootRouter: RootRouter = parent as RootRouter

    val mainScreen = singleNoArgs(
        destinationId = "main",
        componentFactory = { MainScreenComponent(rootRouter) },
        renderFactory = ComposeSingleRender.factory { MainScreen(it) },
    )
}
