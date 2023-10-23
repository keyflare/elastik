package com.keyflare.elastik.core

// TODO Make it more idiomatic (find/findOrNull)
inline fun Backstack.find(predicate: (BackstackEntry) -> Boolean): BackstackEntry? {
    var step: List<BackstackEntry> = listOf(this)
    while (true) {
        val nextStep = mutableListOf<BackstackEntry>()
        step.forEach { entry ->
            when {
                predicate(entry) -> return entry
                entry is Backstack -> nextStep += entry.entries
            }
        }
        if (nextStep.size == 0) return null
        step = nextStep
    }
}

inline fun Backstack.transform(
    transformation: (List<BackstackEntry>) -> List<BackstackEntry>,
): Backstack {
    return Backstack(
        id = this.id,
        args = this.args,
        destinationId = this.destinationId,
        entries = transformation(entries)
    )
}

// MVP solution!
// TODO needs to be optimized
//  - get rid of the recursion
//  - idea: add int mask to id which tells about depth of an exact backstack in a tree
fun Backstack.transform(
    id: Int,
    transformation: (List<BackstackEntry>) -> List<BackstackEntry>,
): Backstack {
    return if (this.id == id) {
        transform(transformation)
    } else {
        Backstack(
            id = this.id,
            args = this.args,
            destinationId = this.destinationId,
            entries = entries.map { entry ->
                when (entry) {
                    is SingleEntry -> entry
                    is Backstack -> entry.transform(id, transformation)
                }
            },
        )
    }
}

inline fun ElastikStateHolder.backstack(id: Int): Backstack? =
    state.value.find { it.id == id } as? Backstack

inline fun ElastikStateHolder.single(id: Int): SingleEntry? =
    state.value.find { it.id == id } as? SingleEntry
