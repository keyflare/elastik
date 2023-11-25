package com.keyflare.sample.shared.core

import co.touchlab.kermit.Logger
import com.keyflare.elastik.compose.render.ComposeSingleRender
import com.keyflare.elastik.core.ElastikContext
import com.keyflare.elastik.core.routing.router.DynamicRouter
import com.keyflare.sample.shared.feature.mainScreen.MainScreen
import com.keyflare.sample.shared.feature.mainScreen.MainScreenComponent
import com.keyflare.sample.shared.feature.splash.SplashComponent
import com.keyflare.sample.shared.feature.splash.SplashScreen

class RootRouter(context: ElastikContext) : DynamicRouter(context) {

    val splash = singleNoArgs(
        destinationId = "splash",
        componentFactory = { SplashComponent(rootRouter = this) },
        renderFactory = ComposeSingleRender.factory { SplashScreen(it) },
    )

    val mainScreen = singleNoArgs(
        destinationId = "main",
        componentFactory = { MainScreenComponent() },
        renderFactory = ComposeSingleRender.factory { MainScreen(it) },
    )

    init {
        Logger.d { "Root Router" }
        navigateTo(splash.destination)
    }
}
