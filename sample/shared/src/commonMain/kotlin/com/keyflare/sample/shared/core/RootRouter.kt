package com.keyflare.sample.shared.core

import co.touchlab.kermit.Logger
import com.keyflare.elastik.core.ElastikContext
import com.keyflare.elastik.core.render.NoRender
import com.keyflare.elastik.core.routing.router.DynamicRouter
import com.keyflare.sample.shared.feature.mainScreen.MainScreenComponent
import com.keyflare.sample.shared.feature.splash.SplashComponent

class RootRouter(context: ElastikContext) : DynamicRouter(context) {

    val splash = singleNoArgs(
        destinationId = "splash",
        componentFactory = { SplashComponent(rootRouter = this) },
        renderFactory = { NoRender },
    )

    val mainScreen = singleNoArgs(
        destinationId = "main",
        componentFactory = { MainScreenComponent() },
        renderFactory = { NoRender },
    )

    init {
        Logger.d { "Root Router" }
        navigateTo(splash.destination)
    }
}
