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
        componentFactory = { SplashScreenComponent() },
    )

    val bottomNavigationScreen = backstack<BottomNavRouter.OpenedTab, BottomNavRouter>(
        destinationId = "bottomNavigationScreen",
        render = RenderStub,
        routerFactory = { BottomNavRouter(it) }
    )

    init {
        navigateTo(splashScreen.destination)
    }
}

class BottomNavRouter(context: ElastikContext) : StaticRouter(context) {

    val dashboardTab = singleNoArgs(
        destinationId = "dashboardTab",
        render = RenderStub,
        componentFactory = { DashboardComponent() },
    )

    val profileTab = singleNoArgs(
        destinationId = "profileTab",
        render = RenderStub,
        componentFactory = { ProfileComponent() },
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
        componentFactory = { MainSettingsComponent() },
    )

    val debugSettingsScreen = singleNoArgs(
        destinationId = "debugSettingsScreen",
        render = RenderStub,
        componentFactory = { DebugSettingsComponent() },
    )

    init {
        navigateTo(mainSettingsScreen.destination)
    }
}

class BottomSheetRouter(context: ElastikContext) : DynamicRouter(context) {

    val rateAppSheet = singleNoArgs(
        destinationId = "rateAppSheet",
        render = RenderStub,
        componentFactory = { RateAppComponent() },
    )

    val updateAppSheet = singleNoArgs(
        destinationId = "updateAppSheet",
        render = RenderStub,
        componentFactory = { UpdateAppComponent() },
    )
}

class DialogRouter(context: ElastikContext) : DynamicRouter(context) {

    val networkErrorDialog = singleNoArgs(
        destinationId = "networkErrorDialog",
        render = RenderStub,
        componentFactory = { NetworkErrorDialogComponent() },
    )

    val alertDialog = single<AlertDialogArgs, AlertDialogComponent>(
        destinationId = "alertDialog",
        render = RenderStub,
        componentFactory = { AlertDialogComponent() },
    )
}

data class AlertDialogArgs(
    val text: String,
) : Arguments

class SplashScreenComponent
class DashboardComponent
class ProfileComponent
class MainSettingsComponent
class DebugSettingsComponent
class RateAppComponent
class UpdateAppComponent
class NetworkErrorDialogComponent
class AlertDialogComponent
