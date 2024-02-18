package com.keyflare.elastik.core.routing.navigation

import com.keyflare.elastik.core.routing.tree.Destination
import com.keyflare.elastik.core.state.Arguments
import com.keyflare.elastik.core.state.EmptyArguments

interface Navigator {

    fun navigateTo(destination: Destination<EmptyArguments>)

    fun <Args : Arguments> navigateTo(
        destination: Destination<Args>,
        args: Args,
    )
}
