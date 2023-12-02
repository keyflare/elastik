package com.keyflare.elastik.core.routing

import com.keyflare.elastik.core.context.ElastikContext
import com.keyflare.elastik.core.render.NoRender
import com.keyflare.elastik.core.routing.router.BaseRouter
import com.keyflare.elastik.core.routing.router.StaticRouter
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class RoutingContextTest {

    private var context: ElastikContext = ElastikContext.create(NoRender)
    private var routerStub: BaseRouter = object : StaticRouter(context) {}

    @BeforeTest
    fun beforeEach() {
        val elastikContext = ElastikContext.create(NoRender)
        context = elastikContext
        routerStub = object : StaticRouter(elastikContext) {}
    }

    @Test
    fun `Unique backstack entry id calculation test`() {
        (0 until 100)
            .map { context.routingContext.obtainIdForNewBackstackEntry() }
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
            context.routingContext.rememberDataForNewRouter(
                destinationId = iteration.toString(),
                backstackEntryId = iteration,
                parent = routerStub,
            )
            assertEquals(
                expected = NewRouterData(
                    destinationId = iteration.toString(),
                    backstackEntryId = iteration,
                    parent = routerStub,
                ),
                actual = context.routingContext.getNewRouterData(),
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
            .map { context.routingContext.isDestinationAlreadyExist(it) }
            .forEachIndexed { index, exists ->
                if (index in listOf(32, 45, 95)) {
                    assertTrue { exists }
                } else {
                    assertFalse { exists }
                }
            }
    }
}
