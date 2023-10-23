package com.keyflare.elastik.routing.router

import com.keyflare.elastik.core.Arguments
import com.keyflare.elastik.core.Backstack
import com.keyflare.elastik.core.BackstackEntry
import com.keyflare.elastik.core.BackstackTransaction
import com.keyflare.elastik.core.BackstackTransformation
import com.keyflare.elastik.core.EmptyArguments
import com.keyflare.elastik.core.SingleEntry
import com.keyflare.elastik.routing.context.ElastikContext
import com.keyflare.elastik.routing.tree.DynamicRouterTreeBuilder
import com.keyflare.elastik.routing.tree.DynamicRouterTreeBuilderDelegate

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
