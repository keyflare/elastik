package com.keyflare.elastik.core.util

import com.keyflare.elastik.core.context.ElastikContext
import com.keyflare.elastik.core.render.NoRender
import com.keyflare.elastik.core.routing.router.BaseRouter
import com.keyflare.elastik.core.routing.router.DynamicRouter
import com.keyflare.elastik.core.routing.router.StaticRouter
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertIs
import kotlin.test.assertTrue

class NavigationUtilsTest {

    private var elastikContext: ElastikContext = ElastikContext.create(NoRender)

    @BeforeTest
    fun beforeEach() {
        elastikContext = ElastikContext.create(NoRender)
    }

    @Test
    fun `static router simple navigation test`() {
        val root = elastikContext.createStaticRoot {
            single()
        }

        assertFails {
            root.applyNavigation {
                router("root") navigate "A"
            }
        }
    }

    @Test
    fun `dynamic router simple navigation test`() {
        val root = elastikContext.createDynamicRoot {
            single()
        }
        assertTrue { root.children!!.isEmpty() }
        root.applyNavigation {
            router("root") navigate "A"
        }

        assertTrue {
            val onlyOneChildren = root.children!!.size == 1
            val hasEntryA = root.children!![0]!!.cast<TestScreenComponent>().destinationId == "A"
            onlyOneChildren && hasEntryA
        }
    }

    @Test
    fun `navigate from single test`() {
        val root = elastikContext.createStaticRoot {
            single()
        }

        assertFails {
            root.applyNavigation {
                router("A") navigate "A"
            }
        }
    }

    @Test
    fun `multiple navigation actions test`() {
        val root = elastikContext.createDynamicRoot {
            single() // A
            dynamic { // B
                single() // C
                dynamic { // D
                    single() // E
                }
                static { // F
                    single() // G
                }
            }
            single() // H
        }

        root.applyNavigation {
            router("root") navigate "A"
            router("root") navigate "A"
            router("root") navigate "B"
            router("B") navigate "F"
            router("B") navigate "D"
            router("D") navigate "E"
        }

        val rootChildren = root.children!!
        val b = rootChildren[2]!!.cast<TestDynamicRouter>()
        val bChildren = b.children!!
        val d = bChildren[1]!!.cast<TestDynamicRouter>()
        val dChildren = d.children!!
        val f = bChildren[0]!!.cast<TestStaticRouter>()

        rootChildren[0]!!.check(dstId = "A", single = true)
        rootChildren[1]!!.check(dstId = "A", single = true)
        b.check(dstId = "B", single = false, static = false)
        f.check(dstId = "F", single = false, static = true)
        d.check(dstId = "D", single = false, static = false)
        dChildren[0]!!.check(dstId = "E", single = true)
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
