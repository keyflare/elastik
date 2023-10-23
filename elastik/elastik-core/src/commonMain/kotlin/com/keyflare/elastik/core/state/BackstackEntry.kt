package com.keyflare.elastik.core.state

sealed interface BackstackEntry {
    val id: Int
    val args: Arguments
    val destinationId: String
}

data class SingleEntry(
    override val id: Int,
    override val args: Arguments,
    override val destinationId: String,
) : BackstackEntry


data class Backstack(
    override val id: Int,
    override val args: Arguments,
    override val destinationId: String,
    val entries: List<BackstackEntry>,
) : BackstackEntry

interface Arguments

data object EmptyArguments : Arguments
