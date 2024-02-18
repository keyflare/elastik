package com.keyflare.elastik.core.routing.router

import com.keyflare.elastik.core.state.Arguments
import com.keyflare.elastik.core.state.Stack
import com.keyflare.elastik.core.state.Entry
import com.keyflare.elastik.core.state.ElastikStateHolder
import com.keyflare.elastik.core.state.Single
import com.keyflare.elastik.core.state.find
import com.keyflare.elastik.core.context.ElastikContext
import com.keyflare.elastik.core.Errors
import com.keyflare.elastik.core.render.StackRender
import com.keyflare.elastik.core.render.SingleRender
import com.keyflare.elastik.core.routing.context.RoutingContext
import com.keyflare.elastik.core.routing.component.BackHandlerImpl
import com.keyflare.elastik.core.routing.component.ComponentContext
import com.keyflare.elastik.core.util.castOrError
import com.keyflare.elastik.core.util.requireNotNull
import com.keyflare.elastik.core.routing.diff.differenceOf
import com.keyflare.elastik.core.routing.component.LifecycleImpl
import com.keyflare.elastik.core.routing.router.BaseRouter.DestinationBinding.SingleDestinationBinding
import com.keyflare.elastik.core.routing.router.BaseRouter.DestinationBinding.StackDestinationBinding

// TODO maybe create an interface
sealed class BaseRouter(context: ElastikContext) {

    private val singleDestinationBindings = mutableMapOf<String, SingleDestinationBinding>()
    private val stackDestinationBindings = mutableMapOf<String, StackDestinationBinding>()
    private val singleChildren = mutableMapOf<Int, Child.SingleChild>()
    private val stackChildren = mutableMapOf<Int, Child.StackChild>()
    private val addedDestinations: MutableSet<String> = mutableSetOf()
    private var lastSyncEntries: List<EntryWrapper> = emptyList()
    private val elastikContext: ElastikContext
    private var currentStack: Stack

    internal val state: ElastikStateHolder
    internal val routingContext: RoutingContext

    val stack: Stack get() = currentStack

    val destinationId: String

    val entryId: Int

    val parent: BaseRouter?

    val children: List<Any>
        get() = stack.entries.mapNotNull {
            singleChildren[it.entryId]?.component ?: stackChildren[it.entryId]?.router
        }

    val childComponents: List<Any>
        get() = stack.entries.mapNotNull { singleChildren[it.entryId]?.component }

    val childRouters: List<BaseRouter>
        get() = stack.entries.mapNotNull { stackChildren[it.entryId]?.router }

    // TODO make more readable (maybe value class or something)
    //  restrict adding destinations in runtime (error or warning) and get rid of get()
    val destinations: List<String> get() = addedDestinations.toList()

    val singleDestinations: List<String> get() = singleDestinationBindings.map { it.key }

    val stackDestinations: List<String> get() = stackDestinationBindings.map { it.key }

    init {
        elastikContext = context
        routingContext = elastikContext.routingContext
        state = routingContext.state
        val data = routingContext.getNewRouterData()
        currentStack = data.stack
        destinationId = stack.destinationId
        entryId = stack.entryId
        parent = data.parent

        setupAsRootIfAppropriate()
        validateData()
        syncState()
    }

    // TODO think in case of synchronicity
    fun onBack() {
        routingContext.backDispatcher.dispatch()
    }

    fun root(): BaseRouter = when (parent) {
        null -> this
        else -> parent.root()
    }

    fun moreThanOneSingleLeft(): Boolean {
        var currentRouters: List<BaseRouter> = listOf(this)
        var singlesCounter = 0

        while (currentRouters.isNotEmpty()) {
            val nextIterationRouters = mutableListOf<BaseRouter>()
            for (r in currentRouters) {
                singlesCounter += r.singleChildren.size
                if (singlesCounter > 1) {
                    return true
                }
                nextIterationRouters += r.stackChildren.map { it.value.router }
            }
            currentRouters = nextIterationRouters
        }

        return false
    }

    protected open fun onHandleBack(): Boolean {
        // Does not handle by default
        return false
    }

    protected open fun onInterceptBack(): Boolean {
        return false
    }

    private fun handleBack(): Boolean {
        // The back event has been intercepted from children
        if (onInterceptBack()) {
            return true
        }

        val entries = stack.entries

        val handledByChild = entries.reversed().asSequence()
            .map { entry ->
                when (entry) {
                    is Single -> {
                        singleChildren[entry.entryId]
                            .requireNotNull { Errors.entryNotFound(entry.entryId) }
                            .backHandler
                            .onHandleBack()
                    }

                    is Stack -> {
                        val childRouter = stackChildren[entry.entryId]
                            .requireNotNull { Errors.entryNotFound(entry.entryId) }
                            .router

                        when {
                            childRouter.handleBack() -> true
                            childRouter.children.isEmpty() -> null
                            else -> false
                        }
                    }
                }
            }
            .firstOrNull { it != null }
            ?: false

        // A child has handled back event, so we don't need to do anything here
        if (handledByChild) {
            return true
        }

        // The event has not been handled by children, therefore,
        // this router must handle it by itself
        return onHandleBack()
    }

    internal fun addSingleDestinationBinding(
        destinationId: String,
        componentFactory: ((ComponentContext) -> Any),
        renderFactory: ((Any) -> SingleRender),
    ) {
        require(addedDestinations.add(destinationId)) {
            Errors.destinationAlreadyExists(destinationId)
        }
        singleDestinationBindings[destinationId] = SingleDestinationBinding(
            destinationId = destinationId,
            componentFactory = componentFactory,
            renderFactory = renderFactory,
        )
    }

    internal fun addStackDestinationBinding(
        destinationId: String,
        routerFactory: (ElastikContext) -> BaseRouter,
        renderFactory: (BaseRouter) -> StackRender,
    ) {
        require(addedDestinations.add(destinationId)) {
            Errors.destinationAlreadyExists(destinationId)
        }
        stackDestinationBindings[destinationId] = StackDestinationBinding(
            destinationId = destinationId,
            routerFactory = routerFactory,
            renderFactory = renderFactory,
        )
    }

    private fun syncState() {
        // TODO MVP Solution!!! Refactor this
        //  (optimize and get rid of the third party diff solution)
        state.subscribeBlocking { root ->
            val (updatedStack, newEntries) = root.extractAssociatedEntries()
            if (updatedStack == null || newEntries == null) {
                return@subscribeBlocking
            }
            currentStack = updatedStack
            val oldEntries = lastSyncEntries
            lastSyncEntries = newEntries

            applyStateDiff(oldEntries, newEntries)
        }
    }

    private fun Stack.extractAssociatedEntries(): Pair<Stack?, List<EntryWrapper>?> {
        val stack = find { it.entryId == this@BaseRouter.entryId }?.castOrError<Stack> {
            Errors.entryUnexpectedType(
                entryId = this.entryId,
                stackExpected = true
            )
        }

        return stack to stack?.entries
            ?.map {
                // Ignore entries inside child stack when comparing
                EntryWrapper(
                    entryId = it.entryId,
                    destinationId = it.destinationId,
                    args = it.args,
                    entry = it,
                )
            }
    }

    private fun applyStateDiff(
        oldEntries: List<EntryWrapper>,
        newEntries: List<EntryWrapper>,
    ) {
        differenceOf(
            original = oldEntries,
            updated = newEntries,
            detectMoves = false,
        ).applyDiff(
            remove = { index ->
                when (val entryToRemove = oldEntries[index].entry) {
                    is Single -> onRemoveSingleEntry(entryToRemove.entryId)
                    is Stack -> onRemoveStackEntry(entryToRemove.entryId)
                }
            },
            insert = { entryWrapper, _ ->
                when (val entryToInsert = entryWrapper.entry) {
                    is Single -> onInsertSingleEntry(entryToInsert)
                    is Stack -> onInsertStackEntry(entryToInsert)
                }
            },
            move = { _, _ -> }
        )
    }

    private fun onInsertSingleEntry(newSingle: Single) {
        @Suppress("ReplaceGetOrSet")
        val destinationBinding = singleDestinationBindings
            .get(newSingle.destinationId)
            .requireNotNull {
                Errors.destinationBindingNotFound(
                    destinationId = newSingle.destinationId,
                    isSingle = true,
                )
            }

        val backHandler = BackHandlerImpl()

        val lifecycle = LifecycleImpl()
        routingContext.lifecycleEventsDispatcher.addLifecycle(newSingle.entryId, lifecycle)

        val componentContext = ComponentContext(
            router = this,
            destinationId = newSingle.destinationId,
            entryId = newSingle.entryId,
            backHandler = backHandler,
            lifecycle = lifecycle,
        )
        val component = destinationBinding.componentFactory(componentContext)

        singleChildren[newSingle.entryId] = Child.SingleChild(
            entry = newSingle,
            backHandler = backHandler,
            component = component,
        )

        routingContext.sendSingleRender(
            entryId = newSingle.entryId,
            render = destinationBinding.renderFactory(component),
        )
    }

    private fun onInsertStackEntry(newStack: Stack) {
        @Suppress("ReplaceGetOrSet")
        val destinationBinding = stackDestinationBindings
            .get(newStack.destinationId)
            .requireNotNull {
                Errors.destinationBindingNotFound(
                    destinationId = newStack.destinationId,
                    isSingle = false,
                )
            }

        routingContext.rememberNewRouterData(
            stack = newStack,
            parent = this,
        )
        val router = destinationBinding.routerFactory(elastikContext)
        routingContext.clearNewRouterData()

        stackChildren[newStack.entryId] = Child.StackChild(
            entry = newStack,
            router = router,
        )

        routingContext.sendStackRender(
            entryId = newStack.entryId,
            render = destinationBinding.renderFactory(router)
        )
    }

    private fun onRemoveSingleEntry(entryId: Int) {
        routingContext.lifecycleEventsDispatcher.removeLifecycle(entryId)
        singleChildren.remove(entryId)
        routingContext.onSingleDestroyed(entryId)
    }

    private fun onRemoveStackEntry(entryId: Int) {
        stackChildren.remove(entryId)
        routingContext.onStackDestroyed(entryId)
    }

    internal fun findComponentOrNull(entryId: Int): Any? {
        return singleChildren[entryId]?.component
    }

    internal fun findRouterOrNull(entryId: Int): BaseRouter? {
        return stackChildren[entryId]?.router
    }

    private fun validateData() {
        if (parent != null) {
            require(parent.routingContext == routingContext) {
                Errors.parentRouterContextMismatch(destinationId)
            }
        }
    }

    private fun setupAsRootIfAppropriate() {
        if (parent != null) return

        routingContext
            .backDispatcher
            .subscribe(::handleBack)
    }

    private sealed interface Child {
        val entry: Entry

        class SingleChild(
            override val entry: Single,
            val backHandler: BackHandlerImpl,
            val component: Any,
        ) : Child

        data class StackChild(
            override val entry: Stack,
            val router: BaseRouter,
        ) : Child
    }

    private sealed interface DestinationBinding {
        val destinationId: String

        class SingleDestinationBinding(
            override val destinationId: String,
            val componentFactory: (ComponentContext) -> Any,
            val renderFactory: (Any) -> SingleRender,
        ) : DestinationBinding

        class StackDestinationBinding(
            override val destinationId: String,
            val routerFactory: (ElastikContext) -> BaseRouter,
            val renderFactory: (BaseRouter) -> StackRender,
        ) : DestinationBinding
    }

    private class EntryWrapper(
        val entryId: Int,
        val destinationId: String,
        val args: Arguments,
        val entry: Entry,
    ) {
        override fun equals(other: Any?): Boolean {
            return other is EntryWrapper &&
                    entryId == other.entryId &&
                    destinationId == other.destinationId &&
                    args == other.args
        }

        override fun hashCode(): Int {
            return entryId.hashCode() *
                    destinationId.hashCode() *
                    args.hashCode()
        }

        override fun toString(): String {
            return entry.toString()
        }
    }
}
