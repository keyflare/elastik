package com.keyflare.elastik.routing

import com.keyflare.elastik.core.Arguments
import com.keyflare.elastik.render.RenderStub
import com.keyflare.elastik.routing.context.ElastikContext
import com.keyflare.elastik.routing.context.ROOT_ROUTER_DATA
import com.keyflare.elastik.routing.router.BaseRouter
import com.keyflare.elastik.routing.router.StaticRouter
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
            message = "After routers tree creation static backstack entry are still unavailable",
        )

        val mainRouter = root.mainRouter.router
        mainRouter.navigateTo(
            mainRouter.bottomNavigationScreen.destination,
            BottomNavRouter.OpenedTab.DASHBOARD,
        )
        val bottomNavigationScreenRouter = mainRouter.bottomNavigationScreen.peekRouterOrNull()
        val settingsRouter = bottomNavigationScreenRouter?.settingsTab?.router
        assertNotNull(
            actual = bottomNavigationScreenRouter,
            message = "After navigation dynamic backstack entry is still unavailable",
        )
        assertNotNull(
            actual = settingsRouter,
            message = "After navigation static backstack entry is still unavailable",
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
                )
                val b = singleNoArgs(
                    destinationId = destinationId,
                    render = RenderStub,
                )
            }
        }
        assertFails {
            object : StaticRouter(ElastikContext.create()) {
                val a = single(
                    destinationId = destinationId,
                    args = ArgsStub(),
                    render = RenderStub,
                )
                val b = single(
                    destinationId = destinationId,
                    args = ArgsStub(),
                    render = RenderStub,
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
