package com.keyflare.elastik.core.util

import com.keyflare.elastik.core.context.ElastikContext
import com.keyflare.elastik.core.render.NoRender
import com.keyflare.elastik.core.routing.router.BaseRouter
import com.keyflare.elastik.core.routing.router.DynamicRouter
import com.keyflare.elastik.core.routing.router.StaticRouter
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class RouterTreeBuilderDslTest {

    private var elastikContext: ElastikContext = ElastikContext.create(NoRender)

    @BeforeTest
    fun beforeEach() {
        elastikContext = ElastikContext.create(NoRender)
    }

    @Test
    fun `static root creation test`() {
        val root = elastikContext.createStaticRoot {}
        assertEquals(expected = "root", actual = root.destinationId)
        assertTrue { root is StaticRouter }
    }

    @Test
    fun `dynamic root creation test`() {
        val root = elastikContext.createDynamicRoot {}
        assertEquals(expected = "root", actual = root.destinationId)
        assertTrue { root is DynamicRouter }
    }

    @Test
    fun `dsl test`() {
        val root = elastikContext.createStaticRoot {
            single() // A
            static { // B
                single() // C
                static {} // D
                static { // E
                    single() // F
                }
            }
            dynamic { // GA
                single() // H
                dynamic {} // I
            }
            single() // J
        }

        // Let's check if the actual structure is correct

        root.children!![0]!!.check(dstId = "A", single = true)
        root.children!![3]!!.check(dstId = "J", single = true)

        root.children!![1]!!.let { rootChild ->
            rootChild.check(dstId = "B", single = false, static = true)
            rootChild.cast<BaseRouter>().children!![0]!!.check(dstId = "C", single = true)

            rootChild.children!![1]!!.let {
               it.check(dstId = "D", single = false, static = true)
               assertTrue { it.cast<BaseRouter>().children!!.isEmpty() }
            }

            rootChild.children!![2]!!.let {
                it.check(dstId = "E", single = false, static = true)
                it.cast<BaseRouter>().children!![0]!!.check(dstId = "F", single = true)
            }
        }

        root.children!![2]!!.let { rootChild ->
            rootChild.check(dstId = "G", single = false, static = false)
            rootChild.cast<BaseRouter>().children!![0]!!.check(dstId = "H", single = true)

            rootChild.children!![1]!!.let {
                it.check(dstId = "I", single = false, static = false)
                assertTrue { it.cast<BaseRouter>().children!!.isEmpty() }
            }
        }
    }

    private fun Any.check(
        dstId: String,
        single: Boolean,
        static: Boolean = false,
    ) {
        if (single) {
            assertIs<TestScreenComponent>(
                value = this,
                message = "For single child TestScreenComponent expected",
            )

            assertEquals(
                expected = dstId,
                actual = this.destinationId,
                message = "Child [$this] has unexpected destination id"
            )
        } else {
            if (static) {
                assertIs<StaticRouter>(
                    value = this,
                    message = "For static stack child StaticRouter expected",
                )
            } else {
                assertIs<DynamicRouter>(
                    value = this,
                    message = "For dynamic stack child DynamicRouter expected",
                )
            }

            assertEquals(
                expected = dstId,
                actual = this.cast<BaseRouter>().destinationId,
                message = "Child [$this] has unexpected destination id",
            )
        }
    }
}
