package com.keyflare.elastik.core.state

import kotlin.test.Test
import kotlin.test.assertEquals

class ElastikStateTest {

    private val testStack = with(TestStackBuilder) {
        stack(
            id = 0,
            destinationId = "root",
            entries = listOf(
                stack(
                    id = 1,
                    destinationId = "level 1 (0)",
                    entries = listOf(
                        stack(
                            id = 2,
                            destinationId = "level 2 (0)",
                            entries = listOf(
                                single(id = 3, destinationId = "level 3 (0)"),
                                single(id = 4, destinationId = "level 3 (1)"),
                            )
                        ),
                        single(id = 5, destinationId = "level 2 (1)"),
                    )
                ),
                stack(
                    id = 6,
                    destinationId = "level 1 (1)",
                    entries = listOf(
                        single(id = 7, destinationId = "level 2 (2)"),
                        single(id = 8, destinationId = "level 2 (3)"),
                    )
                ),
                single(id = 9, destinationId = "level 1 (2)"),
                single(id = 10, destinationId = "level 1 (3)"),
            )
        )
    }

    private val testStackTransformed = with(TestStackBuilder) {
        stack(
            id = 0,
            destinationId = "root",
            entries = listOf(
                stack(
                    id = 1,
                    destinationId = "level 1 (0)",
                    entries = listOf(
                        stack(
                            id = 2,
                            destinationId = "level 2 (0)",
                            entries = listOf(
                                single(id = 3, destinationId = "level 3 (0)"),
                                single(id = 13, destinationId = "level 3 (1) (replaced)"),
                            )
                        ),
                        single(id = 5, destinationId = "level 2 (1)"),
                        single(id = 12, destinationId = "level 2 (4) (added)"),
                    )
                ),
                single(id = 9, destinationId = "level 1 (2)"),
                single(id = 10, destinationId = "level 1 (3)"),
                single(id = 11, destinationId = "level 1 (4) (added)")
            )
        )
    }

    @Test
    fun `Find Entry method test`() {
        assertEquals(
            expected = "root",
            actual = testStack.find { it.entryId == 0 }?.destinationId,
            message = "Error when trying to find \"root\" entry",
        )
        assertEquals(
            expected = "level 1 (0)",
            actual = testStack.find { it.entryId == 1 }?.destinationId,
            message = "Error when trying to find \"level 1 (0)\" entry",
        )
        assertEquals(
            expected = "level 2 (0)",
            actual = testStack.find { it.entryId == 2 }?.destinationId,
            message = "Error when trying to find \"level 2 (0)\" entry",
        )
        assertEquals(
            expected = "level 3 (0)",
            actual = testStack.find { it.entryId == 3 }?.destinationId,
            message = "Error when trying to find \"level 3 (0)\" entry",
        )
        assertEquals(
            expected = "level 3 (1)",
            actual = testStack.find { it.entryId == 4 }?.destinationId,
            message = "Error when trying to find \"level 3 (1)\" entry",
        )
        assertEquals(
            expected = "level 2 (1)",
            actual = testStack.find { it.entryId == 5 }?.destinationId,
            message = "Error when trying to find \"level 2 (1)\" entry",
        )
        assertEquals(
            expected = "level 1 (1)",
            actual = testStack.find { it.entryId == 6 }?.destinationId,
            message = "Error when trying to find \"level 1 (1)\" entry",
        )
        assertEquals(
            expected = "level 2 (2)",
            actual = testStack.find { it.entryId == 7 }?.destinationId,
            message = "Error when trying to find \"level 2 (2)\" entry",
        )
        assertEquals(
            expected = "level 2 (3)",
            actual = testStack.find { it.entryId == 8 }?.destinationId,
            message = "Error when trying to find \"level 2 (3)\" entry",
        )
        assertEquals(
            expected = "level 1 (2)",
            actual = testStack.find { it.entryId == 9 }?.destinationId,
            message = "Error when trying to find \"level 1 (2)\" entry",
        )
        assertEquals(
            expected = "level 1 (3)",
            actual = testStack.find { it.entryId == 10 }?.destinationId,
            message = "Error when trying to find \"level 1 (3)\" entry",
        )
    }

    @Test
    fun `Transform Entry method test`() {
        val transformed = with(TestStackBuilder) {
            with(testStack) {
                this
                    // add one single entry to "root"
                    .transform(entryId = 0) { it + single(
                        id = 11,
                        destinationId = "level 1 (4) (added)"
                    )
                    }
                    // add one single entry to "level 1 (0)"
                    .transform(entryId = 1) { it + single(
                        id = 12,
                        destinationId = "level 2 (4) (added)"
                    )
                    }
                    // replace "level 3 (1)" with level 3 (1) (replaced)
                    .transform(entryId = 2) {
                        it.dropLast(1) + single(id = 13, destinationId = "level 3 (1) (replaced)")
                    }
                    // delete "level 1 (1)"
                    .transform(entryId = 0) { entries -> entries.filter { it.destinationId != "level 1 (1)" } }
                    // try to transform stack which is not existing
                    .transform(entryId = 123) { emptyList() }
            }
        }
        assertEquals(
            expected = testStackTransformed,
            actual = transformed,
            message = "Error applying the transformations",
        )
    }

    @Test
    fun `Transactions pushing method test`() {
        val stateHolder = ElastikStateHolder(testStack)
        val testTransaction = with(TestStackBuilder) {
            StackTransaction(
                transformations = listOf(
                    // add one single entry to "root"
                    StackTransformation(
                        entryId = 0,
                        transformation = {
                            it + single(
                                id = 11,
                                destinationId = "level 1 (4) (added)"
                            )
                        },
                    ),
                    // add one single entry to "level 1 (0)"
                    StackTransformation(
                        entryId = 1,
                        transformation = {
                            it + single(
                                id = 12,
                                destinationId = "level 2 (4) (added)"
                            )
                        },
                    ),
                    // replace "level 3 (1)" with level 3 (1) (replaced)
                    StackTransformation(
                        entryId = 2,
                        transformation = {
                            it.dropLast(1) + single(
                                id = 13,
                                destinationId = "level 3 (1) (replaced)"
                            )
                        },
                    ),
                    // delete "level 1 (1)"
                    StackTransformation(
                        entryId = 0,
                        transformation = { entries -> entries.filter { it.destinationId != "level 1 (1)" } },
                    ),
                    // try to transform stack which is not existing
                    StackTransformation(
                        entryId = 123,
                        transformation = { emptyList() },
                    ),
                )
            )
        }

        // trying to push empty transaction
        stateHolder.pushTransaction(StackTransaction(emptyList()))
        assertEquals(
            expected = testStack,
            actual = stateHolder.state.value,
            message = "Error when trying to push empty transaction",
        )

        // trying to push transaction with transformation
        stateHolder.pushTransaction(testTransaction)
        assertEquals(
            expected = testStackTransformed,
            actual = stateHolder.state.value,
            message = "Error when trying to push transaction with transformations",
        )
    }

    @Test
    fun `Blocking subscription test`() {
        val stateHolder = ElastikStateHolder(testStack)
        var result: Stack? = null
        stateHolder.subscribeBlocking { result = it }
        stateHolder.pushTransaction(StackTransaction(emptyList()))
        assertEquals(
            expected = stateHolder.state.value,
            actual = result,
            message = "onTransactionApplied callback has not been executed",
        )
    }
}

private object TestStackBuilder {
    fun stack(id: Int, destinationId: String, entries: List<Entry>): Stack {
        return Stack(
            entryId = id,
            args = EmptyArguments,
            destinationId = destinationId,
            entries = entries,
        )
    }

    fun single(id: Int, destinationId: String): Single {
        return Single(
            entryId = id,
            args = EmptyArguments,
            destinationId = destinationId,
        )
    }
}
