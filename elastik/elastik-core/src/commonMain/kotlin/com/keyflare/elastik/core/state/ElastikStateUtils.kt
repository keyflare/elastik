package com.keyflare.elastik.core.state

import com.keyflare.elastik.core.Errors
import com.keyflare.elastik.core.util.castOrError

// TODO Make it more idiomatic (find/findOrNull)
internal inline fun Stack.find(predicate: (Entry) -> Boolean): Entry? {
    var step: List<Entry> = listOf(this)
    while (true) {
        val nextStep = mutableListOf<Entry>()
        step.forEach { entry ->
            when {
                predicate(entry) -> return entry
                entry is Stack -> nextStep += entry.entries
            }
        }
        if (nextStep.size == 0) return null
        step = nextStep
    }
}

internal inline fun Stack.transform(
    transformation: (List<Entry>) -> List<Entry>,
): Stack {
    return Stack(
        entryId = this.entryId,
        args = this.args,
        destinationId = this.destinationId,
        entries = transformation(entries),
    )
}

// MVP solution!
// TODO needs to be optimized
//  - get rid of the recursion
//  - idea: add int mask to id which tells about depth of an exact stack in a tree
internal fun Stack.transform(
    entryId: Int,
    transformation: (List<Entry>) -> List<Entry>,
): Stack {
    return if (this.entryId == entryId) {
        transform(transformation)
    } else {
        Stack(
            entryId = this.entryId,
            args = this.args,
            destinationId = this.destinationId,
            entries = entries.map { entry ->
                when (entry) {
                    is Single -> entry
                    is Stack -> entry.transform(entryId, transformation)
                }
            },
        )
    }
}

internal fun ElastikStateHolder.stack(id: Int): Stack? {
    return state.value
        .find { it.entryId == id }
        ?.castOrError<Stack> {
            Errors.entryUnexpectedType(
                entryId = id,
                stackExpected = true,
            )
        }
}

internal fun ElastikStateHolder.single(id: Int): Single? {
    return state.value
        .find { it.entryId == id }
        ?.castOrError<Single> {
            Errors.entryUnexpectedType(
                entryId = id,
                stackExpected = false,
            )
        }
}
