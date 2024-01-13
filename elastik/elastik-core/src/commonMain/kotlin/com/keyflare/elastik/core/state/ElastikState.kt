package com.keyflare.elastik.core.state

sealed interface Entry {
    val entryId: Int
    val args: Arguments
    val destinationId: String
}

data class Single(
    override val entryId: Int,
    override val args: Arguments,
    override val destinationId: String,
) : Entry

data class Stack(
    override val entryId: Int,
    override val args: Arguments,
    override val destinationId: String,
    val entries: List<Entry>,
) : Entry

interface Arguments

data object EmptyArguments : Arguments
