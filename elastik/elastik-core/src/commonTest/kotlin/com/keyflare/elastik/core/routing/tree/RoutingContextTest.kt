package com.keyflare.elastik.core.routing.tree

import com.keyflare.elastik.core.context.ElastikContext
import com.keyflare.elastik.core.render.NoRender
import com.keyflare.elastik.core.routing.context.NewRouterData
import com.keyflare.elastik.core.routing.router.BaseRouter
import com.keyflare.elastik.core.routing.router.StaticRouter
import com.keyflare.elastik.core.state.EmptyArguments
import com.keyflare.elastik.core.state.Stack
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

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
    fun `Unique entry id calculation test`() {
        (0 until 100)
            .map { context.routingContext.obtainNewEntryId() }
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
            context.routingContext.rememberNewRouterData(
                stack = Stack(
                    destinationId = iteration.toString(),
                    entryId = iteration,
                    args = EmptyArguments,
                    entries = emptyList(),
                ),
                parent = routerStub,
            )
            assertEquals(
                expected = NewRouterData(
                    stack = Stack(
                        destinationId = iteration.toString(),
                        entryId = iteration,
                        args = EmptyArguments,
                        entries = emptyList(),
                    ),
                    parent = routerStub,
                ),
                actual = context.routingContext.getNewRouterData(),
                message = "Invalid data prepared for the new router (check iteration #$iteration)"
            )
        }
    }
}
