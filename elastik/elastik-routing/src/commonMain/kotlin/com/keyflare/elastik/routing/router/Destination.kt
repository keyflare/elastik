package com.keyflare.elastik.routing.router

import com.keyflare.elastik.core.Arguments

data class Destination<Args : Arguments> internal constructor(
    val id: String,
    val isSingle: Boolean,
)
