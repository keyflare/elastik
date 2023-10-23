package com.keyflare.elastik.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ElastikStateTest {

    private val testBackstackTree = with(TestBackstackTreeHelper) {
        backstack(
            id = 0,
            destinationId = "root",
            entries = listOf(
                backstack(
                    id = 1,
                    destinationId = "level 1 (0)",
                    entries = listOf(
                        backstack(
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
                backstack(
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

    private val testBackstackTreeTransformed = with(TestBackstackTreeHelper) {
        backstack(
            id = 0,
            destinationId = "root",
            entries = listOf(
                backstack(
                    id = 1,
                    destinationId = "level 1 (0)",
                    entries = listOf(
                        backstack(
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
    fun `Find BackstackEntry method test`() {
        assertEquals(
            expected = "root",
            actual = testBackstackTree.find { it.id == 0 }?.destinationId,
            message = "Error when trying to find \"root\" entry",
        )
        assertEquals(
            expected = "level 1 (0)",
            actual = testBackstackTree.find { it.id == 1 }?.destinationId,
            message = "Error when trying to find \"level 1 (0)\" entry",
        )
        assertEquals(
            expected = "level 2 (0)",
            actual = testBackstackTree.find { it.id == 2 }?.destinationId,
            message = "Error when trying to find \"level 2 (0)\" entry",
        )
        assertEquals(
            expected = "level 3 (0)",
            actual = testBackstackTree.find { it.id == 3 }?.destinationId,
            message = "Error when trying to find \"level 3 (0)\" entry",
        )
        assertEquals(
            expected = "level 3 (1)",
            actual = testBackstackTree.find { it.id == 4 }?.destinationId,
            message = "Error when trying to find \"level 3 (1)\" entry",
        )
        assertEquals(
            expected = "level 2 (1)",
            actual = testBackstackTree.find { it.id == 5 }?.destinationId,
            message = "Error when trying to find \"level 2 (1)\" entry",
        )
        assertEquals(
            expected = "level 1 (1)",
            actual = testBackstackTree.find { it.id == 6 }?.destinationId,
            message = "Error when trying to find \"level 1 (1)\" entry",
        )
        assertEquals(
            expected = "level 2 (2)",
            actual = testBackstackTree.find { it.id == 7 }?.destinationId,
            message = "Error when trying to find \"level 2 (2)\" entry",
        )
        assertEquals(
            expected = "level 2 (3)",
            actual = testBackstackTree.find { it.id == 8 }?.destinationId,
            message = "Error when trying to find \"level 2 (3)\" entry",
        )
        assertEquals(
            expected = "level 1 (2)",
            actual = testBackstackTree.find { it.id == 9 }?.destinationId,
            message = "Error when trying to find \"level 1 (2)\" entry",
        )
        assertEquals(
            expected = "level 1 (3)",
            actual = testBackstackTree.find { it.id == 10 }?.destinationId,
            message = "Error when trying to find \"level 1 (3)\" entry",
        )
    }

    @Test
    fun `Transform BackstackEntry method test`() {
        val transformed = with(TestBackstackTreeHelper) {
            with(testBackstackTree) {
                this
                    // add one single entry to "root"
                    .transform(id = 0) { it + single(id = 11, destinationId = "level 1 (4) (added)") }
                    // add one single entry to "level 1 (0)"
                    .transform(id = 1) { it + single(id = 12, destinationId = "level 2 (4) (added)") }
                    // replace "level 3 (1)" with level 3 (1) (replaced)
                    .transform(id = 2) {
                        it.dropLast(1) + single(id = 13, destinationId = "level 3 (1) (replaced)")
                    }
                    // delete "level 1 (1)"
                    .transform(id = 0) { entries -> entries.filter { it.destinationId != "level 1 (1)" } }
                    // try to transform backstack which is not existing
                    .transform(id = 123) { emptyList() }
            }
        }
        assertEquals(
            expected = testBackstackTreeTransformed,
            actual = transformed,
            message = "Error applying the transformations",
        )
    }

    @Test
    fun `Transactions pushing method test`() {
        val stateHolder = ElastikStateHolder(testBackstackTree)
        val testTransaction = with(TestBackstackTreeHelper) {
            BackstackTransaction(
                transformations = listOf(
                    // add one single entry to "root"
                    BackstackTransformation(
                        backstackId = 0,
                        transformation = { it + single(id = 11, destinationId = "level 1 (4) (added)") },
                    ),
                    // add one single entry to "level 1 (0)"
                    BackstackTransformation(
                        backstackId = 1,
                        transformation = { it + single(id = 12, destinationId = "level 2 (4) (added)") },
                    ),
                    // replace "level 3 (1)" with level 3 (1) (replaced)
                    BackstackTransformation(
                        backstackId = 2,
                        transformation = {
                            it.dropLast(1) + single(id = 13, destinationId = "level 3 (1) (replaced)")
                        },
                    ),
                    // delete "level 1 (1)"
                    BackstackTransformation(
                        backstackId = 0,
                        transformation = { entries -> entries.filter { it.destinationId != "level 1 (1)" } },
                    ),
                    // try to transform backstack which is not existing
                    BackstackTransformation(
                        backstackId = 123,
                        transformation = { emptyList() },
                    ),
                )
            )
        }

        // trying to push empty transaction
        stateHolder.pushTransaction(BackstackTransaction(emptyList()))
        assertEquals(
            expected = testBackstackTree,
            actual = stateHolder.state.value,
            message = "Error when trying to push empty transaction",
        )

        // trying to push transaction with transformation
        stateHolder.pushTransaction(testTransaction)
        assertEquals(
            expected = testBackstackTreeTransformed,
            actual = stateHolder.state.value,
            message = "Error when trying to push transaction with transformations",
        )
    }

    @Test
    fun `Blocking subscription test`() {
        val stateHolder = ElastikStateHolder(testBackstackTree)
        var result: Backstack? = null
        stateHolder.subscribeBlocking { result = it }
        stateHolder.pushTransaction(BackstackTransaction(emptyList()))
        assertEquals(
            expected = stateHolder.state.value,
            actual = result,
            message = "onTransactionApplied callback has not been executed",
        )
    }
}

private object TestBackstackTreeHelper {
    fun backstack(id: Int, destinationId: String, entries: List<BackstackEntry>): Backstack {
        return Backstack(
            id = id,
            args = EmptyArguments,
            destinationId = destinationId,
            entries = entries,
        )
    }

    fun single(id: Int, destinationId: String): SingleEntry {
        return SingleEntry(
            id = id,
            args = EmptyArguments,
            destinationId = destinationId,
        )
    }
}
