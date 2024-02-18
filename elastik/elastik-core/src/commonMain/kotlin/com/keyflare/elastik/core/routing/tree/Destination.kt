package com.keyflare.elastik.core.routing.tree

import com.keyflare.elastik.core.state.Arguments

data class Destination<Args : Arguments> internal constructor(
    val destinationId: String,
    val single: Boolean,
)
