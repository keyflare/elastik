package com.keyflare.elastik.core.routing.router

import com.keyflare.elastik.core.state.Arguments

data class Destination<Args : Arguments> internal constructor(
    val id: String,
    val isSingle: Boolean,
)
