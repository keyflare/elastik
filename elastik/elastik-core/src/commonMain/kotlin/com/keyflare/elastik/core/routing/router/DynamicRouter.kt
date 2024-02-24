package com.keyflare.elastik.core.routing.router

import com.keyflare.elastik.core.state.Arguments
import com.keyflare.elastik.core.state.Stack
import com.keyflare.elastik.core.state.Entry
import com.keyflare.elastik.core.state.Single
import com.keyflare.elastik.core.context.ElastikContext
import com.keyflare.elastik.core.routing.navigation.DynamicNavigatorScope
import com.keyflare.elastik.core.routing.navigation.DynamicNavigatorScopeImpl
import com.keyflare.elastik.core.routing.tree.Destination
import com.keyflare.elastik.core.routing.tree.DynamicRouterTreeBuilder
import com.keyflare.elastik.core.routing.tree.DynamicRouterTreeBuilderDelegate
import com.keyflare.elastik.core.state.EmptyArguments
import com.keyflare.elastik.core.state.StackTransaction
import com.keyflare.elastik.core.state.StackTransformation

abstract class DynamicRouter(context: ElastikContext) :
    BaseRouter(context),
    DynamicRouterTreeBuilder by DynamicRouterTreeBuilderDelegate() {

    fun <Args : Arguments> navigateTo(destination: Destination<Args>, args: Args) {
        mutateState { it + createEntry(destination, args) }
    }

    fun navigateTo(destination: Destination<EmptyArguments>) {
        navigateTo(destination, EmptyArguments)
    }

    fun navigate(body: DynamicNavigatorScope.() -> Unit) {
        DynamicNavigatorScopeImpl(router = this).apply {
            body(this)
            apply()
        }
    }

    override fun onHandleBack(): Boolean {
        val children = children
        val childrenSize = children.size

        return when {
            childrenSize > 1 -> {
                navigate { pop(1) }
                true
            }

            parent == null || childrenSize == 0 || parent is DynamicRouter -> {
                false
            }

            parent.moreThanOneSingleLeft() -> {
                navigate { pop(1) }
                true
            }

            else -> {
                false
            }
        }
    }

    internal fun mutateState(
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

    internal fun createEntry(
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
