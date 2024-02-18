package com.keyflare.elastik.core.routing.backhandle

import app.cash.turbine.test
import com.keyflare.elastik.core.context.ElastikContext
import com.keyflare.elastik.core.render.NoRender
import com.keyflare.elastik.core.util.applyNavigation
import com.keyflare.elastik.core.util.assertAsString
import com.keyflare.elastik.core.util.cast
import com.keyflare.elastik.core.util.createDynamicRoot
import com.keyflare.elastik.core.util.createStaticRoot
import com.keyflare.elastik.core.util.platform.TestBackEventsSource
import com.keyflare.elastik.core.util.platform.createTestPlatform
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

@OptIn(DelicateCoroutinesApi::class)
class BackHandlingTest {

    private val scope = GlobalScope
    private var elastikContext = ElastikContext.create(NoRender)
    private val testPlatform = createTestPlatform(scope)
    private val backEventsSource = testPlatform.backEventsSource.cast<TestBackEventsSource>()

    @BeforeTest
    fun beforeEach() {
        elastikContext = ElastikContext
            .create(NoRender)
            .apply { attachPlatform(testPlatform) }
    }

    @Test
    fun `back when empty static root stack`() = runBlocking {
        elastikContext.createStaticRoot {}

        backEventsSource.handleResultFlow().test(timeout = 1.seconds) {
            backEventsSource.fireEvent()
            assertEquals(expected = false, actual = awaitItem())
        }
    }

    @Test
    fun `back when static root stack has two screens`() = runBlocking {
        val root = elastikContext.createStaticRoot {
            single()
        }

        backEventsSource.handleResultFlow().test(timeout = 1.seconds) {
            root.assertAsString("root*(A)")

            backEventsSource.fireEvent()
            assertEquals(expected = false, actual = awaitItem())
            root.assertAsString("root*(A)")
        }
    }

    @Test
    fun `back when empty dynamic root stack`() = runBlocking {
        elastikContext.createDynamicRoot {}

        backEventsSource.handleResultFlow().test(timeout = 1.seconds) {
            backEventsSource.fireEvent()
            assertEquals(expected = false, actual = awaitItem())
        }
    }

    @Test // root(A-B)
    fun `back when dynamic root stack has two screens`() = runBlocking {
        val root = elastikContext
            .createDynamicRoot {
                single() // A
                single() // B
            }.applyNavigation {
                router("root") navigate "A"
                router("root") navigate "B"
            }

        backEventsSource.handleResultFlow().test(timeout = 1.seconds) {
            root.assertAsString("root(A-B)")

            backEventsSource.fireEvent()
            assertEquals(expected = true, actual = awaitItem())
            root.assertAsString("root(A)")

            backEventsSource.fireEvent()
            assertEquals(expected = false, actual = awaitItem())
            root.assertAsString("root(A)")
        }
    }

    @Test // root(A-B(C-D)-E)
    fun `back when dynamic stack in the middle`() = runBlocking {
        val root = elastikContext
            .createDynamicRoot {
                single() // A
                dynamic { // B
                    single() // C
                    single() // D
                }
                single() // E
            }.applyNavigation {
                router("root") navigate "A"
                router("root") navigate "B"
                router("B") navigate "C"
                router("B") navigate "D"
                router("root") navigate "E"
            }

        backEventsSource.handleResultFlow().test(timeout = 1.seconds) {
            root.assertAsString("root(A-B(C-D)-E)")

            backEventsSource.fireEvent()
            assertEquals(expected = true, actual = awaitItem())
            root.assertAsString("root(A-B(C-D))")

            backEventsSource.fireEvent()
            assertEquals(expected = true, actual = awaitItem())
            root.assertAsString("root(A-B(C))")

            backEventsSource.fireEvent()
            assertEquals(expected = true, actual = awaitItem())
            root.assertAsString("root(A)")

            backEventsSource.fireEvent()
            assertEquals(expected = false, actual = awaitItem())
            root.assertAsString("root(A)")
        }
    }

    @Test // root*(A(B-C)-D(E-F))
    fun `back when static root and dynamic stack in the middle`() = runBlocking {
        val root = elastikContext
            .createStaticRoot {
                dynamic { // A
                    single() // B
                    single() // C
                }
                dynamic { // D
                    single() // E
                    single() // F
                }
            }.applyNavigation {
                router("A") navigate "B"
                router("A") navigate "C"
                router("D") navigate "E"
                router("D") navigate "F"
            }

        backEventsSource.handleResultFlow().test(timeout = 1.seconds) {
            root.assertAsString("root*(A(B-C)-D(E-F))")

            backEventsSource.fireEvent()
            assertEquals(expected = true, actual = awaitItem())
            root.assertAsString("root*(A(B-C)-D(E))")

            backEventsSource.fireEvent()
            assertEquals(expected = true, actual = awaitItem())
            root.assertAsString("root*(A(B-C)-D())")

            backEventsSource.fireEvent()
            assertEquals(expected = true, actual = awaitItem())
            root.assertAsString("root*(A(B)-D())")

            backEventsSource.fireEvent()
            assertEquals(expected = false, actual = awaitItem())
            root.assertAsString("root*(A(B)-D())")
        }
    }

    @Test // root*(A(B-C)-D(E-F))
    fun `back when dynamic root and two static stacks in it`() = runBlocking {
        val root = elastikContext
            .createDynamicRoot {
                dynamic { // A
                    single() // B
                    single() // C
                }
                dynamic { // D
                    single() // E
                    single() // F
                }
            }.applyNavigation {
                router("root") navigate "A"
                router("root") navigate "D"
                router("A") navigate "B"
                router("A") navigate "C"
                router("D") navigate "E"
                router("D") navigate "F"
            }

        backEventsSource.handleResultFlow().test(timeout = 1.seconds) {
            root.assertAsString("root(A(B-C)-D(E-F))")

            backEventsSource.fireEvent()
            assertEquals(expected = true, actual = awaitItem())
            root.assertAsString("root(A(B-C)-D(E))")

            backEventsSource.fireEvent()
            assertEquals(expected = true, actual = awaitItem())
            root.assertAsString("root(A(B-C))")

            backEventsSource.fireEvent()
            assertEquals(expected = true, actual = awaitItem())
            root.assertAsString("root(A(B))")

            backEventsSource.fireEvent()
            assertEquals(expected = false, actual = awaitItem())
            root.assertAsString("root(A(B))")
        }
    }

    @Test // root(A-B*(C(D-E))-F)
    fun `back when dynamic root and static stack in the middle`() = runBlocking {
        val root = elastikContext
            .createDynamicRoot {
                single() // A
                static { // B
                    dynamic { // C
                        single() // D
                        single() // E
                    }
                }
                single() // F
            }.applyNavigation {
                router("root") navigate "A"
                router("root") navigate "B"
                router("C") navigate "D"
                router("C") navigate "E"
                router("root") navigate "F"
            }

        backEventsSource.handleResultFlow().test(timeout = 1.seconds) {
            root.assertAsString("root(A-B*(C(D-E))-F)")

            backEventsSource.fireEvent()
            assertEquals(expected = true, actual = awaitItem())
            root.assertAsString("root(A-B*(C(D-E)))")

            backEventsSource.fireEvent()
            assertEquals(expected = true, actual = awaitItem())
            root.assertAsString("root(A-B*(C(D)))")

            backEventsSource.fireEvent()
            assertEquals(expected = true, actual = awaitItem())
            root.assertAsString("root(A)")

            backEventsSource.fireEvent()
            assertEquals(expected = false, actual = awaitItem())
            root.assertAsString("root(A)")
        }
    }

    @Test // root(A-B(*C(D(E)))-F)
    fun `back when dynamic in static in dynamic`() = runBlocking {
        val root = elastikContext
            .createDynamicRoot {
                single() // A
                dynamic { // B
                    static { // C
                        dynamic {  // D
                            single() // E
                        }
                    }
                }
                single() // F
            }.applyNavigation {
                router("root") navigate "A"
                router("root") navigate "B"
                router("root") navigate "F"
                router("B") navigate "C"
                router("D") navigate "E"
            }

        backEventsSource.handleResultFlow().test(timeout = 1.seconds) {
            root.assertAsString("root(A-B(C*(D(E)))-F)")

            backEventsSource.fireEvent()
            assertEquals(expected = true, actual = awaitItem())
            root.assertAsString("root(A-B(C*(D(E))))")

            backEventsSource.fireEvent()
            assertEquals(expected = true, actual = awaitItem())
            root.assertAsString("root(A)")

            backEventsSource.fireEvent()
            assertEquals(expected = false, actual = awaitItem())
            root.assertAsString("root(A)")
        }
    }

    @Test // root*(A*(B))
    fun `back when static with one single in static`() = runBlocking {
        val root = elastikContext.createStaticRoot {
            static { // A
                single() // B
            }
        }

        backEventsSource.handleResultFlow().test(timeout = 1.seconds) {
            root.assertAsString("root*(A*(B))")

            backEventsSource.fireEvent()
            assertEquals(expected = false, actual = awaitItem())
            root.assertAsString("root*(A*(B))")
        }
    }
}
