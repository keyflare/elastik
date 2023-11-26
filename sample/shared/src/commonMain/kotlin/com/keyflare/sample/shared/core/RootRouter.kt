package com.keyflare.sample.shared.core

import co.touchlab.kermit.Logger
import com.keyflare.elastik.compose.render.ComposeBackstackRender
import com.keyflare.elastik.compose.render.ComposeSingleRender
import com.keyflare.elastik.core.ElastikContext
import com.keyflare.elastik.core.routing.router.DynamicRouter
import com.keyflare.sample.shared.feature.animations.AnimationsComponent
import com.keyflare.sample.shared.feature.animations.AnimationsScreen
import com.keyflare.sample.shared.feature.containers.ContainersComponent
import com.keyflare.sample.shared.feature.tech.TechComponent
import com.keyflare.sample.shared.feature.containers.ContainersScreen
import com.keyflare.sample.shared.feature.maintabs.MainTabsRouter
import com.keyflare.sample.shared.feature.splash.SplashComponent
import com.keyflare.sample.shared.feature.splash.SplashScreen
import com.keyflare.sample.shared.feature.tech.TechScreen

class RootRouter(context: ElastikContext) : DynamicRouter(context) {

    val splash = singleNoArgs(
        destinationId = "splash",
        componentFactory = { SplashComponent(rootRouter = this) },
        renderFactory = ComposeSingleRender.factory { SplashScreen(it) },
    )

    val mainTabs = backstackNoArgs(
        destinationId = "mainTabs",
        routerFactory = { MainTabsRouter(it) },
        renderFactory = { ComposeBackstackRender() },
    )

    val containers = singleNoArgs(
        destinationId = "containers",
        componentFactory = { ContainersComponent(rootRouter = this) },
        renderFactory = ComposeSingleRender.factory { ContainersScreen(it) },
    )

    val animations = singleNoArgs(
        destinationId = "animations",
        componentFactory = { AnimationsComponent(rootRouter = this) },
        renderFactory = ComposeSingleRender.factory { AnimationsScreen(it) },
    )

    val tech = singleNoArgs(
        destinationId = "tech",
        componentFactory = { TechComponent(rootRouter = this) },
        renderFactory = ComposeSingleRender.factory { TechScreen(it) },
    )

    init {
        Logger.d { "Root Router" }
        navigateTo(splash.destination)
    }
}
