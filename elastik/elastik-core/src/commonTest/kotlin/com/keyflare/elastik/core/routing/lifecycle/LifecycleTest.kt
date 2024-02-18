package com.keyflare.elastik.core.routing.lifecycle

import com.keyflare.elastik.core.setup.navigation.applyNavigation
import com.keyflare.elastik.core.setup.visualization.assertAsString
import com.keyflare.elastik.core.setup.component.TestScreenComponentEvent.LifecycleEventReceived
import com.keyflare.elastik.core.setup.platform.TestPlatform
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(DelicateCoroutinesApi::class)
class LifecycleTest {

    private val scope = GlobalScope
    private var testPlatform = TestPlatform(scope)

    @BeforeTest
    fun beforeEach() {
        testPlatform = TestPlatform(scope)
    }

    @Test
    fun `lifecycle events dispatching test`() {
        testPlatform.createStaticRoot {
            single() // A
            static { // B
                static {  // C
                    single() // D
                }
                single() // E
            }
            dynamic { // F
                dynamic { // G
                    single() // H
                }
                single() // I
            }
        }.applyNavigation {
            router("F") navigate "G"
            router("F") navigate "I"
            router("G") navigate "H"
        }.apply {
            assertAsString("root*(A-B*(C*(D)-E)-F(G(H)-I))")
        }

        val destinationIds = listOf("A", "D", "E", "H", "I")

        listOf(
            LifecycleEvent.CREATED,
            LifecycleEvent.STARTED,
            LifecycleEvent.RESUMED,
            LifecycleEvent.PAUSED,
            LifecycleEvent.STOPPED,
            LifecycleEvent.DESTROYED,
        )
            .forEach { event ->
                testPlatform.lifecycleEventsSource.fireEvent(event)
                assertLifecycleCallbackReceived(destinationIds, event)
                testPlatform.testScreenComponentsReporter.clear()
            }
    }

    private fun assertLifecycleCallbackReceived(
        destinationIds: List<String>,
        event: LifecycleEvent,
    ) {
        testPlatform.testScreenComponentsReporter
            .allEvents
            .filterIsInstance<LifecycleEventReceived>()
            .filter { it.lifecycleEvent == event }
            .map { it.destinationId }
            .let {
                assertEquals(
                    expected = destinationIds.sorted(),
                    actual = it.sorted(),
                    message = "Not all expected components received lifecycle event: $event",
                )
            }
    }
}
