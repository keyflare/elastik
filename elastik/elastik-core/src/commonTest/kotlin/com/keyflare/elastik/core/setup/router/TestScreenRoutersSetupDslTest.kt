package com.keyflare.elastik.core.setup.router

import com.keyflare.elastik.core.setup.navigation.applyNavigation
import com.keyflare.elastik.core.setup.platform.TestPlatform
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(DelicateCoroutinesApi::class)
class TestScreenRoutersSetupDslTest {

    @Test
    fun `router setup dsl test`() {
        val foundRoutersDestinations = mutableListOf<String>()

        TestPlatform(GlobalScope)
            .createStaticRoot {
                static { // A
                    dynamic {} // B
                }
                dynamic { // C
                    static {} // D
                }
            }
            .applyNavigation {
                router("C") navigate "D"
            }
            .setupRouters {
                router("root") { foundRoutersDestinations.add(router.destinationId) }
                router("A") { foundRoutersDestinations.add(router.destinationId) }
                router("B") { foundRoutersDestinations.add(router.destinationId) }
                router("C") { foundRoutersDestinations.add(router.destinationId) }
                router("D") { foundRoutersDestinations.add(router.destinationId) }
                router("E") { foundRoutersDestinations.add(router.destinationId) }
            }

        assertEquals(
            expected = listOf("root", "A", "B", "C", "D"),
            actual = foundRoutersDestinations,
        )
    }
}
