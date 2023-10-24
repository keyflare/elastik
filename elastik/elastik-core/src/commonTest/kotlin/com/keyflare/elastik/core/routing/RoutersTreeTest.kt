package com.keyflare.elastik.core.routing

import com.keyflare.elastik.core.state.Arguments
import com.keyflare.elastik.core.ElastikContext
import com.keyflare.elastik.core.render.RenderStub
import com.keyflare.elastik.core.routing.router.BaseRouter
import com.keyflare.elastik.core.routing.router.StaticRouter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

internal class RoutersTreeTest {

    init {
        @OptIn(ExperimentalCoroutinesApi::class)
        Dispatchers.setMain(StandardTestDispatcher())
    }

    private lateinit var root: RootRouter

    @BeforeTest
    fun beforeEach() {
        root = RootRouter(ElastikContext.create())
    }

    @Test
    fun `Synchronicity of navigation test`() = runTest {
        assertEquals(
            expected = RootRouter(ElastikContext.create()).mainRouter.router.destinationId,
            actual = root.mainRouter.destination.id,
            message = "After routers tree creation static backstack entry (router) are still unavailable",
        )

        assertNotNull(
            actual = RootRouter(ElastikContext.create())
                .mainRouter
                .router
                .splashScreen
                .peekComponentOrNull(),
            message = "After routers tree creation dynamic backstack entry navigated immediately (component) are still unavailable",
        )

        val mainRouter = root.mainRouter.router
        mainRouter.navigateTo(
            mainRouter.bottomNavigationScreen.destination,
            BottomNavRouter.OpenedTab.DASHBOARD,
        )
        val bottomNavigationScreenRouter = mainRouter.bottomNavigationScreen.peekRouterOrNull()
        val settingsRouter = bottomNavigationScreenRouter?.settingsTab?.router
        val dashboardComponent = bottomNavigationScreenRouter?.dashboardTab?.component
        assertNotNull(
            actual = bottomNavigationScreenRouter,
            message = "After navigation dynamic backstack entry (router) is still unavailable",
        )
        assertNotNull(
            actual = settingsRouter,
            message = "After navigation static backstack entry (router) is still unavailable",
        )
        assertNotNull(
            actual = dashboardComponent,
            message = "After navigation static backstack entry (component) is still unavailable",
        )
    }

    @Test
    fun `NewRouterData cleared correctly test`() {
        assertNull(
            actual = runCatching { root.routingContext.getNewRouterData() }.getOrNull(),
            message = "NewRouterData was not cleared after routers tree created",
        )
        root.mainRouter.router.navigateTo(
            root.mainRouter.router.bottomNavigationScreen.destination,
            BottomNavRouter.OpenedTab.DASHBOARD,
        )
        assertNull(
            actual = runCatching { root.routingContext.getNewRouterData() }.getOrNull(),
            message = "NewRouterData was not cleared after navigation",
        )
    }

    @Test
    fun `Router tree parents test`() {
        val mainRouter = root.mainRouter.router
        val dialogRouter = root.dialogRouter.router
        val bottomSheetRouter = root.bottomSheetRouter.router

        // Check parents right after creation
        assertParentIsCorrect(root, ROOT_ROUTER_DATA.parent)
        assertParentIsCorrect(mainRouter, root)
        assertParentIsCorrect(dialogRouter, root)
        assertParentIsCorrect(bottomSheetRouter, root)

        mainRouter.navigateTo(
            mainRouter.bottomNavigationScreen.destination,
            BottomNavRouter.OpenedTab.DASHBOARD,
        )
        val bottomNavigationScreenRouter = mainRouter.bottomNavigationScreen.peekRouter()
        val settingsRouter = bottomNavigationScreenRouter.settingsTab.router
        // Check parents of new routers after navigation
        assertParentIsCorrect(bottomNavigationScreenRouter, mainRouter)
        assertParentIsCorrect(settingsRouter, bottomNavigationScreenRouter)
    }

    @Test
    fun `Destination ID validation test`() {
        val destinationId = "destination"
        class ArgsStub : Arguments

        // Check that providing not a unique
        // destination ID will cause an error

        assertFails {
            object : StaticRouter(ElastikContext.create()) {
                val a = singleNoArgs(
                    destinationId = destinationId,
                    render = RenderStub,
                    componentFactory = ::ComponentStub,
                )
                val b = singleNoArgs(
                    destinationId = destinationId,
                    render = RenderStub,
                    componentFactory = ::ComponentStub,
                )
            }
        }
        assertFails {
            object : StaticRouter(ElastikContext.create()) {
                val a = single(
                    destinationId = destinationId,
                    args = ArgsStub(),
                    render = RenderStub,
                    componentFactory = ::ComponentStub,
                )
                val b = single(
                    destinationId = destinationId,
                    args = ArgsStub(),
                    render = RenderStub,
                    componentFactory = ::ComponentStub,
                )
            }
        }
        assertFails {
            object : StaticRouter(ElastikContext.create()) {
                val a = backstackNoArgs(
                    destinationId = destinationId,
                    render = RenderStub,
                    routerFactory = { MainRouter(it) },
                )
                val b = backstackNoArgs(
                    destinationId = destinationId,
                    render = RenderStub,
                    routerFactory = { MainRouter(it) },
                )
            }
        }
        assertFails {
            object : StaticRouter(ElastikContext.create()) {
                val a = backstack(
                    destinationId = destinationId,
                    render = RenderStub,
                    args = ArgsStub(),
                    routerFactory = { MainRouter(it) },
                )
                val b = backstack(
                    destinationId = destinationId,
                    render = RenderStub,
                    args = ArgsStub(),
                    routerFactory = { MainRouter(it) },
                )
            }
        }
    }
}

private fun assertParentIsCorrect(router: BaseRouter, parentRouter: BaseRouter?) {
    assertSame(
        expected = parentRouter,
        actual = router.parent,
        message = "Incorrect parent set for router \"${router.destinationId}\""
    )
}

class ComponentStub
