package com.keyflare.elastik.core.routing

import com.keyflare.elastik.core.state.Arguments
import com.keyflare.elastik.core.ElastikContext
import com.keyflare.elastik.core.render.NoRender
import com.keyflare.elastik.core.routing.router.DynamicRouter
import com.keyflare.elastik.core.routing.router.StaticRouter

class RootRouter(context: ElastikContext) : StaticRouter(context) {

    val mainRouter = backstackNoArgs(
        destinationId = "main",
        routerFactory = { MainRouter(it) },
        renderFactory = { NoRender },
    )

    val bottomSheetRouter = backstackNoArgs(
        destinationId = "bottomSheet",
        routerFactory = { BottomSheetRouter(it) },
        renderFactory = { NoRender },
    )

    val dialogRouter = backstackNoArgs(
        destinationId = "dialog",
        routerFactory = { DialogRouter(it) },
        renderFactory = { NoRender },
    )
}

class MainRouter(context: ElastikContext) : DynamicRouter(context) {

    val splashScreen = singleNoArgs(
        destinationId = "splash",
        componentFactory = { SplashScreenComponent() },
        renderFactory = { NoRender },
    )

    val bottomNavigationScreen = backstack<BottomNavRouter.OpenedTab, BottomNavRouter>(
        destinationId = "bottomNavigationScreen",
        routerFactory = { BottomNavRouter(it) },
        renderFactory = { NoRender },
    )

    init {
        navigateTo(splashScreen.destination)
    }
}

class BottomNavRouter(context: ElastikContext) : StaticRouter(context) {

    val dashboardTab = singleNoArgs(
        destinationId = "dashboardTab",
        componentFactory = { DashboardComponent() },
        renderFactory = { NoRender },
    )

    val profileTab = singleNoArgs(
        destinationId = "profileTab",
        componentFactory = { ProfileComponent() },
        renderFactory = { NoRender },
    )

    val settingsTab = backstackNoArgs(
        destinationId = "settingsTab",
        routerFactory = { SettingsRouter(it) },
        renderFactory = { NoRender },
    )

    enum class OpenedTab : Arguments {
        DASHBOARD, PROFILE, SETTINGS
    }
}

class SettingsRouter(context: ElastikContext) : DynamicRouter(context) {

    val mainSettingsScreen = singleNoArgs(
        destinationId = "mainSettingsScreen",
        componentFactory = { MainSettingsComponent() },
        renderFactory = { NoRender },
    )

    val debugSettingsScreen = singleNoArgs(
        destinationId = "debugSettingsScreen",
        componentFactory = { DebugSettingsComponent() },
        renderFactory = { NoRender },
    )

    init {
        navigateTo(mainSettingsScreen.destination)
    }
}

class BottomSheetRouter(context: ElastikContext) : DynamicRouter(context) {

    val rateAppSheet = singleNoArgs(
        destinationId = "rateAppSheet",
        componentFactory = { RateAppComponent() },
        renderFactory = { NoRender },
    )

    val updateAppSheet = singleNoArgs(
        destinationId = "updateAppSheet",
        componentFactory = { UpdateAppComponent() },
        renderFactory = { NoRender },
    )
}

class DialogRouter(context: ElastikContext) : DynamicRouter(context) {

    val networkErrorDialog = singleNoArgs(
        destinationId = "networkErrorDialog",
        componentFactory = { NetworkErrorDialogComponent() },
        renderFactory = { NoRender },
    )

    val alertDialog = single<AlertDialogArgs, AlertDialogComponent>(
        destinationId = "alertDialog",
        componentFactory = { AlertDialogComponent() },
        renderFactory = { NoRender },
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
