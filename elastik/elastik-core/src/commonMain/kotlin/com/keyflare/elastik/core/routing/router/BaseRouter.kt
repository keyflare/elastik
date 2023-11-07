package com.keyflare.elastik.core.routing.router

import com.keyflare.elastik.core.state.Arguments
import com.keyflare.elastik.core.state.Backstack
import com.keyflare.elastik.core.state.BackstackEntry
import com.keyflare.elastik.core.state.ElastikStateHolder
import com.keyflare.elastik.core.state.SingleEntry
import com.keyflare.elastik.core.state.backstack
import com.keyflare.elastik.core.state.find
import com.keyflare.elastik.core.ElastikContext
import com.keyflare.elastik.core.Errors
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

    internal val state: ElastikStateHolder
    internal val routingContext: RoutingContext

    // TODO refactor: handle whole BackstackEntry entity, not ids, and validate it not a single
    val destinationId: String

    val backstackEntryId: Int

    val parent: BaseRouter?

    val backstack
        get() = state.backstack(backstackEntryId)
            ?: error(Errors.noBackstackAssociated(backstackEntryId))

    init {
        routingContext = context
        state = routingContext.state
        val data = routingContext.getNewRouterData()
        destinationId = data.destinationId
        backstackEntryId = data.backstackEntryId
        parent = data.parent

        validateData()
        syncState()
    }

    internal fun addSingleDestinationBinding(
        destinationId: String,
        componentFactory: (() -> Any),
    ) {
        require(!routingContext.isDestinationAlreadyExist(destinationId)) {
            Errors.destinationAlreadyExists(destinationId)
        }
        singleDestinationBindings[destinationId] = SingleDestinationBinding(componentFactory)
    }

    internal fun addBackstackDestinationBinding(
        destinationId: String,
        routerFactory: (ElastikContext) -> BaseRouter,
    ) {
        require(!routingContext.isDestinationAlreadyExist(destinationId)) {
            Errors.destinationAlreadyExists(destinationId)
        }
        backstackDestinationBindings[destinationId] = BackstackDestinationBinding(routerFactory)
    }

    private fun syncState() {
        // TODO MVP Solution!!! Refactor this
        //  (optimize and get rid of the third party diff solution)
        state.subscribeBlocking { root ->
            val oldEntries = lastSyncEntries
            val newEntries = root.extractAssociatedEntries()
            lastSyncEntries = newEntries

            applyStateDiff(oldEntries, newEntries)
        }
    }

    private fun Backstack.extractAssociatedEntries(): List<BackstackEntryWrapper> {
        return find { it.id == backstackEntryId }
            .requireNotNull { Errors.noBackstackAssociated(backstackEntryId) }
            .castOrError<Backstack>(Errors.backstackEntryUnexpectedType(backstackEntryId, true))
            .entries
            .map {
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
            detectMoves = true,
        ).applyDiff(
            remove = { index ->
                // TODO onDestroy callback
                when (val entryToRemove = oldEntries[index].entry) {
                    is SingleEntry -> childSingleEntriesData.remove(entryToRemove.id)
                    is Backstack -> childBackstacksData.remove(entryToRemove.id)
                }
            },
            insert = { entryWrapper, _ ->
                when (entryWrapper.entry) {
                    is SingleEntry -> {
                        childSingleEntriesData[entryWrapper.backstackEntryId] =
                            BackstackEntryData.SingleEntryData(
                                backstackEntry = entryWrapper.entry,
                                component = createComponentForBackstackEntry(entryWrapper.entry),
                            )
                    }

                    is Backstack -> {
                        childBackstacksData[entryWrapper.backstackEntryId] =
                            BackstackEntryData.BackstackData(
                                backstackEntry = entryWrapper.entry,
                                router = createRouterForBackstackEntry(entryWrapper.entry),
                            )
                    }
                }
            },
            move = { _, _ -> }
        )
    }

    private fun createComponentForBackstackEntry(backstackEntry: BackstackEntry): Any {
        return singleDestinationBindings[backstackEntry.destinationId]
            .requireNotNull()
            .componentFactory()
    }

    private fun createRouterForBackstackEntry(backstackEntry: BackstackEntry): BaseRouter {
        routingContext.rememberDataForNewRouter(
            backstackEntryId = backstackEntry.id,
            destinationId = backstackEntry.destinationId,
            parent = this,
        )
        val router = backstackDestinationBindings[backstackEntry.destinationId]
            .requireNotNull()
            .routerFactory(routingContext as ElastikContext)

        routingContext.clearNewRouterData()
        return router
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

    private sealed interface BackstackEntryData {
        val backstackEntry: BackstackEntry

        class SingleEntryData(
            override val backstackEntry: SingleEntry,
            val component: Any,
        ) : BackstackEntryData

        data class BackstackData(
            override val backstackEntry: Backstack,
            val router: BaseRouter,
        ) : BackstackEntryData
    }

    private class SingleDestinationBinding(
        val componentFactory: (() -> Any),
    )

    private class BackstackDestinationBinding(
        val routerFactory: (ElastikContext) -> BaseRouter,
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
