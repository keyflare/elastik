package com.keyflare.elastik.core.routing.router

import com.keyflare.elastik.core.state.Arguments
import com.keyflare.elastik.core.state.Backstack
import com.keyflare.elastik.core.state.BackstackEntry
import com.keyflare.elastik.core.state.ElastikStateHolder
import com.keyflare.elastik.core.state.SingleEntry
import com.keyflare.elastik.core.state.backstack
import com.keyflare.elastik.core.state.find
import com.keyflare.elastik.core.context.ElastikContext
import com.keyflare.elastik.core.Errors
import com.keyflare.elastik.core.render.BackstackRender
import com.keyflare.elastik.core.render.SingleRender
import com.keyflare.elastik.core.routing.RoutingContext
import com.keyflare.elastik.core.util.castOrError
import com.keyflare.elastik.core.util.requireNotNull
import com.keyflare.elastik.core.routing.diff.differenceOf

// TODO maybe create an interface
sealed class BaseRouter(context: ElastikContext) {

    private val singleDestinationBindings = mutableMapOf<String, SingleDestinationBinding>()
    private val backstackDestinationBindings = mutableMapOf<String, BackstackDestinationBinding>()
    private val childSingleEntriesData = mutableMapOf<Int, BackstackEntryData.SingleEntryData>()
    private val childBackstacksData = mutableMapOf<Int, BackstackEntryData.BackstackData>()
    private var lastSyncEntries: List<BackstackEntryWrapper> = emptyList()
    private val elastikContext: ElastikContext

    internal val state: ElastikStateHolder
    internal val routingContext: RoutingContext

    val destinationId: String

    val backstackEntryId: Int

    val parent: BaseRouter?

    val backstack: Backstack? get() = state.backstack(backstackEntryId)

    init {
        elastikContext = context
        routingContext = elastikContext.routingContext
        state = routingContext.state
        val data = routingContext.getNewRouterData()
        destinationId = data.destinationId
        backstackEntryId = data.backstackEntryId
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

        val entries = backstack?.entries ?: return false

        // The back event need to be handled by parent
        if (entries.size <= 1) {
            return false
        }

        val handledByChild = when (val topEntry = entries.lastOrNull()) {
            null -> false

            is SingleEntry -> {
                childSingleEntriesData[topEntry.id]
                    .requireNotNull { Errors.entryNotFound(topEntry.id) }
                    .backDispatcher
                    .dispatchBackEvent()
            }

            is Backstack -> {
                childBackstacksData[topEntry.id]
                    .requireNotNull { Errors.entryNotFound(topEntry.id) }
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

    internal fun addBackstackDestinationBinding(
        destinationId: String,
        routerFactory: (ElastikContext) -> BaseRouter,
        renderFactory: (BaseRouter) -> BackstackRender,
    ) {
        require(!routingContext.isDestinationAlreadyExist(destinationId)) {
            Errors.destinationAlreadyExists(destinationId)
        }
        backstackDestinationBindings[destinationId] = BackstackDestinationBinding(
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

    private fun Backstack.extractAssociatedEntries(): List<BackstackEntryWrapper>? {
        return find { it.id == backstackEntryId }
            ?.castOrError<Backstack> {
                Errors.backstackEntryUnexpectedType(
                    backstackEntryId = backstackEntryId,
                    backstackExpected = true
                )
            }
            ?.entries
            ?.map {
                // Ignore entries inside child backstack when comparing
                BackstackEntryWrapper(
                    backstackEntryId = it.id,
                    destinationId = it.destinationId,
                    args = it.args,
                    entry = it,
                )
            }
    }

    private fun applyStateDiff(
        oldEntries: List<BackstackEntryWrapper>,
        newEntries: List<BackstackEntryWrapper>,
    ) {
        differenceOf(
            original = oldEntries,
            updated = newEntries,
            detectMoves = false,
        ).applyDiff(
            remove = { index ->
                when (val entryToRemove = oldEntries[index].entry) {
                    is SingleEntry -> onRemoveSingleEntry(entryToRemove.id)
                    is Backstack -> onRemoveBackstackEntry(entryToRemove.id)
                }
            },
            insert = { entryWrapper, _ ->
                when (val entryToInsert = entryWrapper.entry) {
                    is SingleEntry -> onInsertSingleEntry(entryToInsert)
                    is Backstack -> onInsertBackstackEntry(entryToInsert)
                }
            },
            move = { _, _ -> }
        )
    }

    private fun onInsertSingleEntry(entry: SingleEntry) {
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

        childSingleEntriesData[entry.id] = BackstackEntryData.SingleEntryData(
            backstackEntry = entry,
            backDispatcher = backController,
            component = component,
        )

        routingContext.sendSingleRender(
            backstackEntryId = entry.id,
            render = destinationBinding.renderFactory(component),
        )
    }

    private fun onInsertBackstackEntry(entry: Backstack) {
        @Suppress("ReplaceGetOrSet")
        val destinationBinding = backstackDestinationBindings
            .get(entry.destinationId)
            .requireNotNull {
                Errors.destinationBindingNotFound(
                    destinationId = entry.destinationId,
                    isSingle = false,
                )
            }

        routingContext.rememberDataForNewRouter(
            destinationId = entry.destinationId,
            backstackEntryId = entry.id,
            parent = this,
        )
        val router = destinationBinding.routerFactory(elastikContext)
        routingContext.clearNewRouterData()

        childBackstacksData[entry.id] = BackstackEntryData.BackstackData(
            backstackEntry = entry,
            router = router,
        )

        routingContext.sendBackstackRender(
            backstackEntryId = entry.id,
            render = destinationBinding.renderFactory(router)
        )
    }

    private fun onRemoveSingleEntry(backstackEntryId: Int) {
        childSingleEntriesData.remove(backstackEntryId)
        routingContext.onSingleDestroyed(backstackEntryId)
    }

    private fun onRemoveBackstackEntry(backstackEntryId: Int) {
        childBackstacksData.remove(backstackEntryId)
        routingContext.onBackstackDestroyed(backstackEntryId)
    }

    internal fun findComponentOrNull(backstackEntryId: Int): Any? {
        return childSingleEntriesData[backstackEntryId]?.component
    }

    internal fun findRouterOrNull(backstackEntryId: Int): BaseRouter? {
        return childBackstacksData[backstackEntryId]?.router
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

    private sealed interface BackstackEntryData {
        val backstackEntry: BackstackEntry

        class SingleEntryData(
            override val backstackEntry: SingleEntry,
            val backDispatcher: BackDispatcher,
            val component: Any,
        ) : BackstackEntryData

        data class BackstackData(
            override val backstackEntry: Backstack,
            val router: BaseRouter,
        ) : BackstackEntryData
    }

    private class SingleDestinationBinding(
        val componentFactory: (BackHandler) -> Any,
        val renderFactory: (Any) -> SingleRender,
    )

    private class BackstackDestinationBinding(
        val routerFactory: (ElastikContext) -> BaseRouter,
        val renderFactory: (BaseRouter) -> BackstackRender,
    )

    private class BackstackEntryWrapper(
        val backstackEntryId: Int,
        val destinationId: String,
        val args: Arguments,
        val entry: BackstackEntry,
    ) {
        override fun equals(other: Any?): Boolean {
            return other is BackstackEntryWrapper &&
                    backstackEntryId == other.backstackEntryId &&
                    destinationId == other.destinationId &&
                    args == other.args
        }

        override fun hashCode(): Int {
            return backstackEntryId.hashCode() *
                    destinationId.hashCode() *
                    args.hashCode()
        }

        override fun toString(): String {
            return entry.toString()
        }
    }
}
