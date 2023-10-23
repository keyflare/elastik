package com.keyflare.elastik.core.routing.router

import com.keyflare.elastik.core.state.Arguments
import com.keyflare.elastik.core.state.Backstack
import com.keyflare.elastik.core.state.BackstackEntry
import com.keyflare.elastik.core.state.BackstackTransaction
import com.keyflare.elastik.core.state.BackstackTransformation
import com.keyflare.elastik.core.state.EmptyArguments
import com.keyflare.elastik.core.state.SingleEntry
import com.keyflare.elastik.core.ElastikContext
import com.keyflare.elastik.core.routing.tree.DynamicRouterTreeBuilder
import com.keyflare.elastik.core.routing.tree.DynamicRouterTreeBuilderDelegate

interface TransactionManager {

    fun navigateTo(destination: Destination<EmptyArguments>)

    fun <Args : Arguments> navigateTo(
        destination: Destination<Args>,
        args: Args,
    )

    fun navigateBack()
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
        navigate { it + createBackstackEntry(destination, args) }
    }

    override fun navigateBack() {
        navigate { it.dropLast(1) }
    }

    protected fun navigate(
        transformation: (List<BackstackEntry>) -> List<BackstackEntry>,
    ) {
        state.pushTransaction(
            BackstackTransaction(
                transformations = listOf(
                    BackstackTransformation(
                        backstackId = backstackEntryId,
                        transformation = transformation,
                    )
                )
            )
        )
    }

    private fun createBackstackEntry(
        destination: Destination<*>,
        args: Arguments,
    ): BackstackEntry {
        val id = routingContext.obtainIdForNewBackstackEntry()
        return if (destination.isSingle) {
            SingleEntry(
                id = id,
                destinationId = destination.id,
                args = args,
            )
        } else {
            Backstack(
                id = id,
                destinationId = destination.id,
                args = args,
                entries = emptyList(),
            )
        }
    }
}
