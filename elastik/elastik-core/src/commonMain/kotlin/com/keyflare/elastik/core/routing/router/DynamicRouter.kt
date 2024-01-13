package com.keyflare.elastik.core.routing.router

import com.keyflare.elastik.core.state.Arguments
import com.keyflare.elastik.core.state.Stack
import com.keyflare.elastik.core.state.Entry
import com.keyflare.elastik.core.state.StackTransaction
import com.keyflare.elastik.core.state.StackTransformation
import com.keyflare.elastik.core.state.EmptyArguments
import com.keyflare.elastik.core.state.Single
import com.keyflare.elastik.core.context.ElastikContext
import com.keyflare.elastik.core.routing.tree.DynamicRouterTreeBuilder
import com.keyflare.elastik.core.routing.tree.DynamicRouterTreeBuilderDelegate

interface TransactionManager {

    fun navigateTo(destination: Destination<EmptyArguments>)

    fun <Args : Arguments> navigateTo(
        destination: Destination<Args>,
        args: Args,
    )
}

abstract class DynamicRouter(context: ElastikContext) :
    BaseRouter(context),
    DynamicRouterTreeBuilder by DynamicRouterTreeBuilderDelegate(),
    TransactionManager {

    override fun navigateTo(destination: Destination<EmptyArguments>) {
        navigateTo(destination, EmptyArguments)
    }

    override fun <Args : Arguments> navigateTo(
        destination: Destination<Args>,
        args: Args,
    ) {
        navigate { it + createStackEntry(destination, args) }
    }

    override fun onHandleBack() {
        navigate { it.dropLast(1) }
    }

    protected fun navigate(
        transformation: (List<Entry>) -> List<Entry>,
    ) {
        state.pushTransaction(
            StackTransaction(
                transformations = listOf(
                    StackTransformation(
                        entryId = entryId,
                        transformation = transformation,
                    )
                )
            )
        )
    }

    private fun createStackEntry(
        destination: Destination<*>,
        args: Arguments,
    ): Entry {
        val id = routingContext.obtainNewEntryId()
        return if (destination.single) {
            Single(
                entryId = id,
                destinationId = destination.destinationId,
                args = args,
            )
        } else {
            Stack(
                entryId = id,
                destinationId = destination.destinationId,
                args = args,
                entries = emptyList(),
            )
        }
    }
}
