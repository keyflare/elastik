package com.keyflare.elastik.core

sealed interface BackstackEntry {
    val id: Int
    val label: String
    val args: Args?
}

class SingleEntry(
    override val id: Int,
    override val label: String,
    override val args: Args?,
) : BackstackEntry {

    override fun equals(other: Any?): Boolean =
        other is SingleEntry && other.id == id && other.args == args

    override fun hashCode(): Int =
        id.hashCode() * (args?.hashCode() ?: 1)

    override fun toString(): String =
        "SingleEntry(id=$id, label=\"$label\", args=$args)"
}

class Backstack(
    override val id: Int,
    override val label: String,
    override val args: Args?,
    val entries: List<BackstackEntry>,
) : BackstackEntry {

    override fun equals(other: Any?): Boolean =
        other is Backstack && other.id == id && other.args == args && other.entries == entries

    override fun hashCode(): Int {
        return id.hashCode() * (args?.hashCode() ?: 1)
    }

    override fun toString(): String =
        "Backstack(id=$id, label=\"$label\", args=$args, entries=$entries)"
}

interface Args
