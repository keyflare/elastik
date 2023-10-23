package com.keyflare.elastik.core.routing

import com.keyflare.elastik.core.ElastikContext
import com.keyflare.elastik.core.render.Render
import com.keyflare.elastik.core.routing.router.BaseRouter
import com.keyflare.elastik.core.routing.router.StaticRouter
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class RoutingContextTest {

    private var context: RoutingContext = ElastikContext.create()
    private var routerStub: BaseRouter = object : StaticRouter(ElastikContext.create()) {}
    private val renderStub: Render = object : Render {}

    @BeforeTest
    fun beforeEach() {
        context = ElastikContext.create()
        routerStub = object : StaticRouter(ElastikContext.create()) {}
    }

    @Test
    fun `Unique backstack entry id calculation test`() {
        (0 until 100)
            .map { context.obtainIdForNewBackstackEntry() }
            .toSet()
            .let { ids ->
                assertEquals(
                    expected = 100,
                    actual = ids.size,
                    message = "Some of the calculated ids are not unique",
                )
            }
    }

    @Test
    fun `Providing data for a new router test`() {
        (0 until 100).map { iteration ->
            context.rememberDataForNewRouter(
                destinationId = iteration.toString(),
                backstackEntryId = iteration,
                parent = routerStub,
                render = renderStub,
            )
            assertEquals(
                expected = NewRouterData(
                    destinationId = iteration.toString(),
                    backstackEntryId = iteration,
                    parent = routerStub,
                    render = renderStub,
                ),
                actual = context.getNewRouterData(),
                message = "Invalid data prepared for the new router (check iteration #$iteration)"
            )
        }
    }

    @Test
    fun `Validation of destinationId uniqueness test`() {
        (0 until 100)
            .toMutableList()
            .apply { set(45, 0) } // not unique id on 45 index
            .apply { set(32, 17) } // not unique id on 32 index
            .apply { set(95, 50) } // not unique id on 95 index
            .map { it.toString() }
            .map { context.isDestinationAlreadyExist(it) }
            .forEachIndexed { index, exists ->
                if (index in listOf(32, 45, 95)) {
                    assertTrue { exists }
                } else {
                    assertFalse { exists }
                }
            }
    }
}
