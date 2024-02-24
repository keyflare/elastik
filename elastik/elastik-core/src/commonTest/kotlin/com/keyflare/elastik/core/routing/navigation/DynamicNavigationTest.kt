package com.keyflare.elastik.core.routing.navigation

import com.keyflare.elastik.core.routing.tree.DynamicSingleDestination
import com.keyflare.elastik.core.setup.platform.TestPlatform
import com.keyflare.elastik.core.setup.visualization.assertAsString
import com.keyflare.elastik.core.state.EmptyArguments
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(DelicateCoroutinesApi::class)
class DynamicNavigationTest {

    private val scope = GlobalScope
    private var testPlatform = TestPlatform(scope)

    @BeforeTest
    fun beforeEach() {
        testPlatform = TestPlatform(scope)
    }

    @Test
    fun `navigateTo with and without args`() {
        DynamicNavigationTestRouter(testPlatform.elastikContext).apply {
            navigateTo(screenWithoutArgs.destination)
            assertAsString("root(screenWithoutArgs)")

            val args = ArgsStub("tag")
            navigateTo(screenWithArgs.destination, args)
            assertAsString("root(screenWithoutArgs-screenWithArgs)")
            assertEquals(
                expected = args,
                actual = stack.entries.last().args,
            )
        }
    }

    @Test
    fun `popTo destinationId inclusive and not inclusive`() {
        // Not inclusive
        createRouter { listOf(screenA, screenB, screenC, screenD) }.apply {
            navigate { popTo(screenB.destination.destinationId) }
            assertAsString("root(A-B)")
        }
        // Inclusive
        createRouter { listOf(screenA, screenB, screenC, screenD) }.apply {
            navigate { popTo(screenB.destination.destinationId, inclusive = true) }
            assertAsString("root(A)")
        }
    }

    @Test
    fun `popTo destinationId corner cases`() {
        createRouter { listOf(screenA) }.apply {
            // Entry isn't found
            navigate { popTo(screenB.destination.destinationId) }
            navigate { popTo(screenB.destination.destinationId, inclusive = true) }
            assertAsString("root(A)")
            // Last entry, inclusive=false
            navigate { popTo(screenA.destination.destinationId) }
            assertAsString("root(A)")
            // Last entry, inclusive=true
            navigate { popTo(screenA.destination.destinationId, inclusive = true) }
            assertAsString("root()")
            // Empty stack
            navigate { popTo(screenA.destination.destinationId) }
            navigate { popTo(screenA.destination.destinationId, inclusive = true) }
            assertAsString("root()")
        }
    }

    @Test
    fun `popTo entryId inclusive and not inclusive`() {
        // Not inclusive
        createRouter { listOf(screenA, screenB, screenC, screenD) }.apply {
            val entryId = stack.entries[1].entryId
            navigate { popTo(entryId) }
            assertAsString("root(A-B)")
        }
        // Inclusive
        createRouter { listOf(screenA, screenB, screenC, screenD) }.apply {
            val entryId = stack.entries[1].entryId
            navigate { popTo(entryId, inclusive = true) }
            assertAsString("root(A)")
        }
    }

    @Test
    fun `popTo entryId corner cases`() {
        createRouter { listOf(screenA) }.apply {
            val entryId = stack.entries[0].entryId
            // Entry isn't found
            navigate { popTo(100) }
            navigate { popTo(100, inclusive = true) }
            assertAsString("root(A)")
            // Last entry, inclusive=false
            navigate { popTo(entryId) }
            assertAsString("root(A)")
            // Last entry, inclusive=true
            navigate { popTo(entryId, inclusive = true) }
            assertAsString("root()")
            // Empty stack
            navigate { popTo(entryId) }
            navigate { popTo(entryId, inclusive = true) }
            assertAsString("root()")
        }
    }

    @Test
    fun `pop n entries`() {
        createRouter { listOf(screenA, screenB, screenC, screenD) }.apply {
            navigate { pop(0) }
            assertAsString("root(A-B-C-D)")
            navigate { pop(1) }
            assertAsString("root(A-B-C)")
            navigate { pop(2) }
            assertAsString("root(A)")
        }
    }

    @Test
    fun `pop n entries corner cases`() {
        createRouter { listOf(screenA, screenB, screenC, screenD) }.apply {
            // Wrong number
            navigate { pop(-1) }
            assertAsString("root(A-B-C-D)")
            // Pop all remaining entries
            navigate { pop(4) }
            assertAsString("root()")
            // Pop last entry
            navigateTo(screenA.destination)
            assertAsString("root(A)")
            navigate { pop(1) }
            assertAsString("root()")
            // Pop more entries than there are
            navigateTo(screenA.destination)
            assertAsString("root(A)")
            navigate { pop(2) }
            assertAsString("root()")
        }
    }

    @Test
    fun `push destination with and without args`() {
        createRouter { listOf() }.apply {
            navigate { push(screenWithoutArgs.destination) }
            assertAsString("root(screenWithoutArgs)")
            val args = ArgsStub("tag")
            navigate { push(screenWithArgs.destination, args) }
            assertAsString("root(screenWithoutArgs-screenWithArgs)")
            assertEquals(
                expected = args,
                actual = stack.entries.last().args,
            )
        }
    }

    @Test
    fun `insert destination with and without args`() {
        createRouter { listOf(screenA, screenC) }.apply {
            navigate { free { insert(screenB.destination, index = 1) } }
            assertAsString("root(A-B-C)")
            val args = ArgsStub("tag")
            navigate { free { insert(screenWithArgs.destination, args, index = 1) } }
            assertAsString("root(A-screenWithArgs-B-C)")
            assertEquals(
                expected = args,
                actual = stack.entries[1].args,
            )
        }
    }

    @Test
    fun `removeFirst destinationId`() {
        createRouter { listOf(screenA, screenC, screenB, screenC, screenD) }.apply {
            // Target entry is not the first in stack entry
            navigate { free { removeFirst(screenC.destination.destinationId) } }
            assertAsString("root(A-B-C-D)")
            // Target entry is the first in stack entry
            navigate { free { removeFirst(screenA.destination.destinationId) } }
            assertAsString("root(B-C-D)")
            // Target entry is not presented
            navigate { free { removeFirst(screenA.destination.destinationId) } }
            assertAsString("root(B-C-D)")
            // Target entry is the last in stack entry
            navigate { free { removeFirst(screenD.destination.destinationId) } }
            assertAsString("root(B-C)")
            // Target entry is the last remaining entry
            navigate { free { removeFirst(screenB.destination.destinationId) } }
            navigate { free { removeFirst(screenC.destination.destinationId) } }
            assertAsString("root()")
        }
    }

    @Test
    fun `removeLast destinationId`() {
        createRouter { listOf(screenA, screenC, screenB, screenC, screenD) }.apply {
            // Target entry is not the last in stack entry
            navigate { free { removeLast(screenC.destination.destinationId) } }
            assertAsString("root(A-C-B-D)")
            // Target entry is the last in stack entry
            navigate { free { removeLast(screenD.destination.destinationId) } }
            assertAsString("root(A-C-B)")
            // Target entry is not presented
            navigate { free { removeLast(screenD.destination.destinationId) } }
            assertAsString("root(A-C-B)")
            // Target entry is the first in stack entry
            navigate { free { removeLast(screenA.destination.destinationId) } }
            assertAsString("root(C-B)")
            // Target entry is the last remaining entry
            navigate { free { removeLast(screenB.destination.destinationId) } }
            navigate { free { removeLast(screenC.destination.destinationId) } }
            assertAsString("root()")
        }
    }

    @Test
    fun `removeAll entryId`() {
        createRouter { listOf(screenA, screenB, screenA, screenB, screenA, screenB) }.apply {
            navigate { free { removeAll(screenA.destination.destinationId) } }
            assertAsString("root(B-B-B)")
            navigate { free { removeAll(screenB.destination.destinationId) } }
            assertAsString("root()")
        }
    }

    @Test
    fun `removeAt index`() {
        createRouter { listOf(screenA, screenB, screenC, screenD) }.apply {
            navigate { free { removeAt(1) } }
            assertAsString("root(A-C-D)")
            navigate { free { removeAt(0) } }
            assertAsString("root(C-D)")
            navigate { free { removeAt(100) } }
            assertAsString("root(C-D)")
            navigate { free { removeAt(1) } }
            assertAsString("root(C)")
            navigate { free { removeAt(0) } }
            assertAsString("root()")
            navigate { free { removeAt(0) } }
            assertAsString("root()")
        }
    }

    @Test
    fun `removeEntry entryId`() {
        createRouter { listOf(screenA, screenB, screenC, screenD) }.apply {
            val entryA = stack.entries[0].entryId
            val entryB = stack.entries[1].entryId
            val entryC = stack.entries[2].entryId
            val entryD = stack.entries[3].entryId
            navigate { free { removeEntry(entryB) } }
            assertAsString("root(A-C-D)")
            navigate { free { removeEntry(100) } }
            assertAsString("root(A-C-D)")
            navigate { free { removeEntry(entryA) } }
            assertAsString("root(C-D)")
            navigate { free { removeEntry(entryD) } }
            assertAsString("root(C)")
            navigate { free { removeEntry(entryC) } }
            assertAsString("root()")
            navigate { free { removeEntry(100) } }
            assertAsString("root()")
        }
    }

    @Test
    fun `swap by index`() {
        createRouter { listOf(screenA, screenB, screenC, screenD) }.apply {
            navigate { free { swapAt(0, 1) } }
            assertAsString("root(B-A-C-D)")
            navigate { free { swapAt(1, 2) } }
            assertAsString("root(B-C-A-D)")
            navigate { free { swapAt(2, 3) } }
            assertAsString("root(B-C-D-A)")
            navigate { free { swapAt(3, 0) } }
            assertAsString("root(A-C-D-B)")
            navigate { free { swapAt(100, 0) } }
            assertAsString("root(A-C-D-B)")
            navigate { free { swapAt(0, 100) } }
            assertAsString("root(A-C-D-B)")
        }
    }

    @Test
    fun `swap entries`() {
        createRouter { listOf(screenA, screenB, screenC, screenD) }.apply {
            val entryA = stack.entries[0].entryId
            val entryB = stack.entries[1].entryId
            val entryC = stack.entries[2].entryId
            val entryD = stack.entries[3].entryId
            navigate { free { swapEntries(entryA, entryB) } }
            assertAsString("root(B-A-C-D)")
            navigate { free { swapEntries(entryB, entryC) } }
            assertAsString("root(C-A-B-D)")
            navigate { free { swapEntries(entryC, entryD) } }
            assertAsString("root(D-A-B-C)")
            navigate { free { swapEntries(entryD, entryA) } }
            assertAsString("root(A-D-B-C)")
            navigate { free { swapEntries(entryA, 100) } }
            assertAsString("root(A-D-B-C)")
            navigate { free { swapEntries(100, entryA) } }
            assertAsString("root(A-D-B-C)")
        }
    }

    private fun createRouter(
        destinations: DynamicNavigationTestRouter.() -> List<DynamicSingleDestination<EmptyArguments, *>>,
    ): DynamicNavigationTestRouter {
        beforeEach()
        return DynamicNavigationTestRouter(testPlatform.elastikContext).apply {
            destinations().forEach { destination -> navigateTo(destination.destination) }
        }
    }
}
