package com.keyflare.elastik.core.routing

import com.keyflare.elastik.core.state.Arguments
import com.keyflare.elastik.core.ElastikContext
import com.keyflare.elastik.core.render.RenderStub
import com.keyflare.elastik.core.routing.router.DynamicRouter
import com.keyflare.elastik.core.routing.router.StaticRouter

class RootRouter(context: ElastikContext) : StaticRouter(context) {

    val mainRouter = backstackNoArgs(
        destinationId = "main",
        render = RenderStub,
        routerFactory = { MainRouter(it) },
    )

    val bottomSheetRouter = backstackNoArgs(
        destinationId = "bottomSheet",
        render = RenderStub,
        routerFactory = { BottomSheetRouter(it) },
    )

    val dialogRouter = backstackNoArgs(
        destinationId = "dialog",
        render = RenderStub,
        routerFactory = { DialogRouter(it) },
    )
}

class MainRouter(context: ElastikContext) : DynamicRouter(context) {

    val splashScreen = singleNoArgs(
        destinationId = "splash",
        render = RenderStub,
    )

    val bottomNavigationScreen = backstack<BottomNavRouter.OpenedTab, BottomNavRouter>(
        destinationId = "bottomNavigationScreen",
        render = RenderStub,
        routerFactory = { BottomNavRouter(it) }
    )

    init {
        navigateTo(splashScreen)
    }
}

class BottomNavRouter(context: ElastikContext) : StaticRouter(context) {

    val dashboardTab = singleNoArgs(
        destinationId = "dashboardTab",
        render = RenderStub,
    )

    val profileTab = singleNoArgs(
        destinationId = "profileTab",
        render = RenderStub,
    )

    val settingsTab = backstackNoArgs(
        destinationId = "settingsTab",
        render = RenderStub,
        routerFactory = { SettingsRouter(it) }
    )

    enum class OpenedTab : Arguments {
        DASHBOARD, PROFILE, SETTINGS
    }
}

class SettingsRouter(context: ElastikContext) : DynamicRouter(context) {

    val mainSettingsScreen = singleNoArgs(
        destinationId = "mainSettingsScreen",
        render = RenderStub,
    )

    val debugSettingsScreen = singleNoArgs(
        destinationId = "debugSettingsScreen",
        render = RenderStub,
    )

    init {
        navigateTo(mainSettingsScreen)
    }
}

class BottomSheetRouter(context: ElastikContext) : DynamicRouter(context) {

    val rateAppSheet = singleNoArgs(
        destinationId = "rateAppSheet",
        render = RenderStub,
    )

    val updateAppSheet = singleNoArgs(
        destinationId = "updateAppSheet",
        render = RenderStub,
    )
}

class DialogRouter(context: ElastikContext) : DynamicRouter(context) {

    val networkErrorDialog = singleNoArgs(
        destinationId = "networkErrorDialog",
        render = RenderStub,
    )

    val alertDialog = single<AlertDialogArgs>(
        destinationId = "alertDialog",
        render = RenderStub,
    )
}

data class AlertDialogArgs(
    val text: String,
) : Arguments
