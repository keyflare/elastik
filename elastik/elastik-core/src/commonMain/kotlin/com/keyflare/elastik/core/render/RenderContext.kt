package com.keyflare.elastik.core.render

import com.keyflare.elastik.core.Errors
import com.keyflare.elastik.core.state.Backstack
import com.keyflare.elastik.core.state.ElastikStateHolder
import com.keyflare.elastik.core.util.requireNotNull
import kotlinx.coroutines.flow.StateFlow

interface RenderContext {
    val rootBackstack: StateFlow<Backstack>

    fun addSingleRender(
        backstackEntryId: Int,
        render: SingleRender,
    )

    fun addBackstackRender(
        backstackEntryId: Int,
        render: BackstackRender,
    )

    fun removeSingleRender(backstackEntryId: Int)
    fun removeBackstackRender(backstackEntryId: Int)
    fun getSingleRender(backstackEntryId: Int): SingleRender
    fun getBackstackRender(backstackEntryId: Int): BackstackRender
}

internal class RenderContextImpl(state: ElastikStateHolder) : RenderContext {

    private val singleRenders = mutableMapOf<Int, SingleRender>()
    private val backstackRenders = mutableMapOf<Int, BackstackRender>()

    override val rootBackstack: StateFlow<Backstack> = state.state

    override fun addSingleRender(
        backstackEntryId: Int,
        render: SingleRender,
    ) {
        singleRenders[backstackEntryId] = render
    }

    override fun addBackstackRender(
        backstackEntryId: Int,
        render: BackstackRender,
    ) {
        backstackRenders[backstackEntryId] = render
    }

    override fun removeSingleRender(backstackEntryId: Int) {
        singleRenders.remove(backstackEntryId)
    }

    override fun removeBackstackRender(backstackEntryId: Int) {
        backstackRenders.remove(backstackEntryId)
    }

    override fun getSingleRender(backstackEntryId: Int): SingleRender {
        return singleRenders[backstackEntryId].requireNotNull {
            Errors.renderNotFound(backstackEntryId)
        }
    }

    override fun getBackstackRender(backstackEntryId: Int): BackstackRender {
        return backstackRenders[backstackEntryId].requireNotNull {
            Errors.renderNotFound(backstackEntryId)
        }
    }
}
