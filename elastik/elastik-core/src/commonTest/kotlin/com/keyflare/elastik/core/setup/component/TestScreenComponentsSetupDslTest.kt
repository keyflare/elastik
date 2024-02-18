package com.keyflare.elastik.core.setup.component

import com.keyflare.elastik.core.setup.navigation.applyNavigation
import com.keyflare.elastik.core.setup.platform.TestPlatform
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(DelicateCoroutinesApi::class)
class TestScreenComponentsSetupDslTest {

    @Test
    fun `component setup dsl test`() {
        val foundComponentsDestinations = mutableListOf<String>()

        TestPlatform(GlobalScope)
            .createStaticRoot {
                single() // A
                static { // B
                    single() // C
                }
                dynamic { // D
                    single() // E
                }
            }
            .applyNavigation {
                router("D") navigate "E"
            }
            .setupComponents {
                component("A") { foundComponentsDestinations.add(destinationId) }
                component("B") { foundComponentsDestinations.add(destinationId) }
                component("C") { foundComponentsDestinations.add(destinationId) }
                component("D") { foundComponentsDestinations.add(destinationId) }
                component("E") { foundComponentsDestinations.add(destinationId) }
                component("F") { foundComponentsDestinations.add(destinationId) }
            }

        assertEquals(
            expected = listOf("A", "C", "E"),
            actual = foundComponentsDestinations,
        )
    }
}
