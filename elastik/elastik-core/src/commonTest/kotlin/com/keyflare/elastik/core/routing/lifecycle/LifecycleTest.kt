package com.keyflare.elastik.core.routing.lifecycle

import com.keyflare.elastik.core.context.ElastikContext
import com.keyflare.elastik.core.render.NoRender
import com.keyflare.elastik.core.util.applyNavigation
import com.keyflare.elastik.core.util.assertAsString
import com.keyflare.elastik.core.util.cast
import com.keyflare.elastik.core.util.component.TestScreenComponentEvent.LifecycleEventReceived
import com.keyflare.elastik.core.util.component.TestScreenComponentsReporter
import com.keyflare.elastik.core.util.createStaticRoot
import com.keyflare.elastik.core.util.platform.TestLifecycleEventsSource
import com.keyflare.elastik.core.util.platform.createTestPlatform
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(DelicateCoroutinesApi::class)
class LifecycleTest {

    private val scope = GlobalScope
    private var elastikContext = ElastikContext.create(NoRender)
    private val testPlatform = createTestPlatform(scope)
    private var testScreenComponentsReporter = TestScreenComponentsReporter()

    private val lifecycleEventsSource = testPlatform
        .lifecycleEventsSource
        .cast<TestLifecycleEventsSource>()

    @BeforeTest
    fun beforeEach() {
        testScreenComponentsReporter = TestScreenComponentsReporter()
        elastikContext = ElastikContext
            .create(NoRender)
            .apply { attachPlatform(testPlatform) }
    }

    @Test
    fun `lifecycle events dispatching test`() {
        elastikContext.createStaticRoot(testScreenComponentsReporter) {
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
                lifecycleEventsSource.fireEvent(event)
                assertLifecycleCallbackReceived(destinationIds, event)
                testScreenComponentsReporter.clear()
            }
    }

    private fun assertLifecycleCallbackReceived(
        destinationIds: List<String>,
        event: LifecycleEvent,
    ) {
        testScreenComponentsReporter.allEvents
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
