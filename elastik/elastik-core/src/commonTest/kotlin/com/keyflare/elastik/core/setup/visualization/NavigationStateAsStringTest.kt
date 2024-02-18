package com.keyflare.elastik.core.setup.visualization

import com.keyflare.elastik.core.context.ElastikContext
import com.keyflare.elastik.core.render.NoRender
import com.keyflare.elastik.core.setup.navigation.applyNavigation
import com.keyflare.elastik.core.setup.tree.createDynamicRoot
import com.keyflare.elastik.core.setup.tree.createStaticRoot
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class NavigationStateAsStringTest {

    private var elastikContext: ElastikContext = ElastikContext.create(NoRender)

    @BeforeTest
    fun beforeEach() {
        elastikContext = ElastikContext.create(NoRender)
    }

    @Test
    fun `empty root`() {
        val staticRoot = elastikContext.createStaticRoot {}
        val dynamicRoot = elastikContext.createDynamicRoot {}

        assertEquals(
            expected = "root*()",
            actual = staticRoot.asString(),
        )
        assertEquals(
            expected = "root()",
            actual = dynamicRoot.asString(),
        )
    }

    @Test
    fun `complicated tree` () {
        val root = elastikContext.createStaticRoot {
            single() // A
            single() // B
            dynamic { // C
                dynamic {} // D
                single() // E
                static { // F
                    single() // G
                }
            }
            static { // H
                single() // I
            }
        }

        assertEquals(
            expected = "root*(A-B-C()-H*(I))",
            actual = root.asString(),
        )

        root.applyNavigation {
            router("C") navigate "D"
            router("C") navigate "E"
            router("C") navigate "E"
            router("C") navigate "F"
        }

        assertEquals(
            expected = "root*(A-B-C(D()-E-E-F*(G))-H*(I))",
            actual = root.asString(),
        )
    }
}
