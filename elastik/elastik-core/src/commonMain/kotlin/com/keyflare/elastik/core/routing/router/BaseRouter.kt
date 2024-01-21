package com.keyflare.elastik.core.routing.router

import com.keyflare.elastik.core.state.Arguments
import com.keyflare.elastik.core.state.Stack
import com.keyflare.elastik.core.state.Entry
import com.keyflare.elastik.core.state.ElastikStateHolder
import com.keyflare.elastik.core.state.Single
import com.keyflare.elastik.core.state.stack
import com.keyflare.elastik.core.state.find
import com.keyflare.elastik.core.context.ElastikContext
import com.keyflare.elastik.core.Errors
import com.keyflare.elastik.core.render.StackRender
import com.keyflare.elastik.core.render.SingleRender
import com.keyflare.elastik.core.routing.RoutingContext
import com.keyflare.elastik.core.util.castOrError
import com.keyflare.elastik.core.util.requireNotNull
import com.keyflare.elastik.core.routing.diff.differenceOf

// TODO maybe create an interface
sealed class BaseRouter(context: ElastikContext) {

    private val singleDestinationBindings = mutableMapOf<String, SingleDestinationBinding>()
    private val stackDestinationBindings = mutableMapOf<String, StackDestinationBinding>()
    private val singleChildren = mutableMapOf<Int, Child.SingleChild>()
    private val stackChildren = mutableMapOf<Int, Child.StackChild>()
    private var lastSyncEntries: List<EntryWrapper> = emptyList()
    private val elastikContext: ElastikContext

    internal val state: ElastikStateHolder
    internal val routingContext: RoutingContext

    val destinationId: String

    val entryId: Int

    val parent: BaseRouter?

    // TODO make not null
    val stack: Stack? get() = state.stack(entryId)

    // TODO make not null
    val children: List<Any?>? get() = stack?.entries
        ?.map { singleChildren[it.entryId]?.component ?: stackChildren[it.entryId]?.router }

    init {
        elastikContext = context
        routingContext = elastikContext.routingContext
        state = routingContext.state
        val data = routingContext.getNewRouterData()
        destinationId = data.destinationId
        entryId = data.entryId
        parent = data.parent

        setupAsRootIfAppropriate()
        validateData()
        syncState()
    }

    fun onBack() {
        routingContext.backEventsDispatcher.dispatch()
    }

    protected open fun onHandleBack() {
        // Do nothing by default
    }

    protected open fun onInterceptBack(): Boolean {
        return false
    }

    private fun handleBack(): Boolean {
        // The back event has been intercepted from children
        if (onInterceptBack()) {
            return true
        }

        val entries = stack?.entries ?: return false

        // The back event need to be handled by parent
        if (entries.size <= 1) {
            return false
        }

        val handledByChild = when (val topEntry = entries.lastOrNull()) {
            null -> false

            is Single -> {
                singleChildren[topEntry.entryId]
                    .requireNotNull { Errors.entryNotFound(topEntry.entryId) }
                    .backDispatcher
                    .dispatchBackEvent()
            }

            is Stack -> {
                stackChildren[topEntry.entryId]
                    .requireNotNull { Errors.entryNotFound(topEntry.entryId) }
                    .router
                    .handleBack()
            }
        }

        // A child has handled back event, so we don't need to do anything here
        if (handledByChild) {
            return true
        }

        // The event has not been handled by children, therefore,
        // this router must handle it by itself
        onHandleBack()

        return true
    }

    internal fun addSingleDestinationBinding(
        destinationId: String,
        componentFactory: ((BackHandler) -> Any),
        renderFactory: ((Any) -> SingleRender),
    ) {
        require(!routingContext.isDestinationAlreadyExist(destinationId)) {
            Errors.destinationAlreadyExists(destinationId)
        }
        singleDestinationBindings[destinationId] = SingleDestinationBinding(
            componentFactory = componentFactory,
            renderFactory = renderFactory,
        )
    }

    internal fun addStackDestinationBinding(
        destinationId: String,
        routerFactory: (ElastikContext) -> BaseRouter,
        renderFactory: (BaseRouter) -> StackRender,
    ) {
        require(!routingContext.isDestinationAlreadyExist(destinationId)) {
            Errors.destinationAlreadyExists(destinationId)
        }
        stackDestinationBindings[destinationId] = StackDestinationBinding(
            routerFactory = routerFactory,
            renderFactory = renderFactory,
        )
    }

    private fun syncState() {
        // TODO MVP Solution!!! Refactor this
        //  (optimize and get rid of the third party diff solution)
        state.subscribeBlocking { root ->
            val newEntries = root.extractAssociatedEntries() ?: return@subscribeBlocking
            val oldEntries = lastSyncEntries
            lastSyncEntries = newEntries

            applyStateDiff(oldEntries, newEntries)
        }
    }

    private fun Stack.extractAssociatedEntries(): List<EntryWrapper>? {
        return find { it.entryId == this@BaseRouter.entryId }
            ?.castOrError<Stack> {
                Errors.entryUnexpectedType(
                    entryId = this.entryId,
                    stackExpected = true
                )
            }
            ?.entries
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

    private fun onInsertSingleEntry(entry: Single) {
        @Suppress("ReplaceGetOrSet")
        val destinationBinding = singleDestinationBindings
            .get(entry.destinationId)
            .requireNotNull {
                Errors.destinationBindingNotFound(
                    destinationId = entry.destinationId,
                    isSingle = true,
                )
            }
        val backController = BackControllerImpl()
        val component = destinationBinding.componentFactory(backController)

        singleChildren[entry.entryId] = Child.SingleChild(
            entry = entry,
            backDispatcher = backController,
            component = component,
        )

        routingContext.sendSingleRender(
            entryId = entry.entryId,
            render = destinationBinding.renderFactory(component),
        )
    }

    private fun onInsertStackEntry(entry: Stack) {
        @Suppress("ReplaceGetOrSet")
        val destinationBinding = stackDestinationBindings
            .get(entry.destinationId)
            .requireNotNull {
                Errors.destinationBindingNotFound(
                    destinationId = entry.destinationId,
                    isSingle = false,
                )
            }

        routingContext.rememberNewRouterData(
            destinationId = entry.destinationId,
            entryId = entry.entryId,
            parent = this,
        )
        val router = destinationBinding.routerFactory(elastikContext)
        routingContext.clearNewRouterData()

        stackChildren[entry.entryId] = Child.StackChild(
            entry = entry,
            router = router,
        )

        routingContext.sendStackRender(
            entryId = entry.entryId,
            render = destinationBinding.renderFactory(router)
        )
    }

    private fun onRemoveSingleEntry(entryId: Int) {
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
            .backEventsDispatcher
            .subscribe(::handleBack)
    }

    private sealed interface Child {
        val entry: Entry

        class SingleChild(
            override val entry: Single,
            val backDispatcher: BackDispatcher,
            val component: Any,
        ) : Child

        data class StackChild(
            override val entry: Stack,
            val router: BaseRouter,
        ) : Child
    }

    private class SingleDestinationBinding(
        val componentFactory: (BackHandler) -> Any,
        val renderFactory: (Any) -> SingleRender,
    )

    private class StackDestinationBinding(
        val routerFactory: (ElastikContext) -> BaseRouter,
        val renderFactory: (BaseRouter) -> StackRender,
    )

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
