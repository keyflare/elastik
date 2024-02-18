package com.keyflare.elastik.core.routing.tree

import com.keyflare.elastik.core.state.Arguments
import com.keyflare.elastik.core.context.ElastikContext
import com.keyflare.elastik.core.render.NoRender
import com.keyflare.elastik.core.routing.ROOT_ROUTER_DATA
import com.keyflare.elastik.core.routing.router.BaseRouter
import com.keyflare.elastik.core.routing.router.StaticRouter
import com.keyflare.elastik.core.setup.navigation.applyNavigation
import com.keyflare.elastik.core.setup.tree.createDynamicRoot
import com.keyflare.elastik.core.setup.tree.createStaticRoot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class RoutersTreeTest {

    init {
        @OptIn(ExperimentalCoroutinesApi::class)
        Dispatchers.setMain(StandardTestDispatcher())
    }

    private var elastikContext: ElastikContext = ElastikContext.create(NoRender)

    @BeforeTest
    fun beforeEach() {
        elastikContext = ElastikContext.create(NoRender)
    }

    @Test
    fun `Synchronicity of navigation test`() = runTest {
        val root = RootRouter(elastikContext)
        assertEquals(
            expected = RootRouter(ElastikContext.create(NoRender)).mainRouter.router.destinationId,
            actual = root.mainRouter.destination.destinationId,
            message = "After routers tree creation entry in static stack are still unavailable",
        )

        assertNotNull(
            actual = RootRouter(ElastikContext.create(NoRender))
                .mainRouter
                .router
                .splashScreen
                .peekComponentOrNull(),
            message = "After routers tree creation entry navigated immediately in dynamic stack (component) are still unavailable",
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
            message = "After navigation entry in dynamic stack is still unavailable",
        )
        assertNotNull(
            actual = settingsRouter,
            message = "After navigation entry (stack) in static stack just navigated is still unavailable",
        )
        assertNotNull(
            actual = dashboardComponent,
            message = "After navigation entry (single) in static stack just navigated is still unavailable",
        )
    }

    @Test
    fun `NewRouterData cleared correctly test`() {
        val root = RootRouter(elastikContext)
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
        val root = RootRouter(elastikContext)
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
            object : StaticRouter(ElastikContext.create(NoRender)) {
                init {
                    singleNoArgs(
                        destinationId = destinationId,
                        renderFactory = { NoRender },
                        componentFactory = { ComponentStub() },
                    )
                    singleNoArgs(
                        destinationId = destinationId,
                        renderFactory = { NoRender },
                        componentFactory = { ComponentStub() },
                    )
                }
            }
        }
        assertFails {
            object : StaticRouter(ElastikContext.create(NoRender)) {
                init {
                    single(
                        destinationId = destinationId,
                        args = ArgsStub(),
                        renderFactory = { NoRender },
                        componentFactory = { ComponentStub() },
                    )
                    single(
                        destinationId = destinationId,
                        args = ArgsStub(),
                        renderFactory = { NoRender },
                        componentFactory = { ComponentStub() },
                    )
                }
            }
        }
        assertFails {
            object : StaticRouter(ElastikContext.create(NoRender)) {
                init {
                    stackNoArgs(
                        destinationId = destinationId,
                        renderFactory = { NoRender },
                        routerFactory = { MainRouter(it) },
                    )
                    stackNoArgs(
                        destinationId = destinationId,
                        renderFactory = { NoRender },
                        routerFactory = { MainRouter(it) },
                    )
                }
            }
        }
        assertFails {
            object : StaticRouter(ElastikContext.create(NoRender)) {
                init {
                    stack(
                        destinationId = destinationId,
                        args = ArgsStub(),
                        renderFactory = { NoRender },
                        routerFactory = { MainRouter(it) },
                    )
                    stack(
                        destinationId = destinationId,
                        args = ArgsStub(),
                        renderFactory = { NoRender },
                        routerFactory = { MainRouter(it) },
                    )
                }
            }
        }
    }

    @Test
    fun `root finding test`() {
        val root = elastikContext
            .createStaticRoot {
                single() // A
                dynamic { // B
                    single() // C
                    static { // D
                        dynamic { // E
                            single() // F
                        }
                        dynamic { // G
                            single() // H
                        }
                    }
                    dynamic { // I
                        single() // J
                    }
                }
            }
            .apply {
                applyNavigation {
                    router("B") navigate "D"
                    router("B") navigate "I"
                }
            }

        val routerB = root.childRouters.first { it.destinationId == "B" }
        val routerD = routerB.childRouters.first { it.destinationId == "D" }
        val routerI = routerB.childRouters.first { it.destinationId == "I" }
        val routerE = routerD.childRouters.first { it.destinationId == "E" }
        val routerG = routerD.childRouters.first { it.destinationId == "G" }

        assertEquals(root, root.root())
        assertEquals(root, routerB.root())
        assertEquals(root, routerD.root())
        assertEquals(root, routerI.root())
        assertEquals(root, routerE.root())
        assertEquals(root, routerG.root())
    }

    @Test
    fun `Last single left method test`() {
        fun createRoot(): BaseRouter {
            elastikContext = ElastikContext.create(NoRender)
            return elastikContext.createDynamicRoot {
                single() // A
                static { // B
                    single() // C
                }
                dynamic { // D
                    static { // E
                        single() // F
                    }
                }
            }
        }

        // Simple test - zero singles, one single, two singles
        var root = createRoot()
        assertFalse { root.moreThanOneSingleLeft() }
        root.applyNavigation {
            router("root") navigate "A"
        }
        assertFalse { root.moreThanOneSingleLeft() }
        root.applyNavigation {
            router("root") navigate "A"
        }
        assertTrue { root.moreThanOneSingleLeft() }

        // Complicated hierarchy test
        root = createRoot()
        root.applyNavigation {
            router("root") navigate "D"
        }
        assertFalse { root.moreThanOneSingleLeft() }
        root.applyNavigation {
            router("D") navigate "E"
        }
        assertFalse { root.moreThanOneSingleLeft() }
        root.applyNavigation {
            router("root") navigate "B"
        }
        assertTrue { root.moreThanOneSingleLeft() }
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
