package com.keyflare.elastik.core.render

import com.keyflare.elastik.core.Errors
import com.keyflare.elastik.core.state.Stack
import com.keyflare.elastik.core.state.ElastikStateHolder
import com.keyflare.elastik.core.util.requireNotNull
import kotlinx.coroutines.flow.StateFlow

interface RenderContext {
    val rootStack: StateFlow<Stack>

    fun addSingleRender(
        entryId: Int,
        render: SingleRender,
    )

    fun addStackRender(
        entryId: Int,
        render: StackRender,
    )

    fun removeSingleRender(entryId: Int)

    fun removeStackRender(entryId: Int)

    fun getSingleRender(entryId: Int): SingleRender

    fun getStackRender(entryId: Int): StackRender
}

internal class RenderContextImpl(state: ElastikStateHolder) : RenderContext {

    private val singleRenderMap = mutableMapOf<Int, SingleRender>()
    private val stackRenderMap = mutableMapOf<Int, StackRender>()

    override val rootStack: StateFlow<Stack> = state.state

    override fun addSingleRender(
        entryId: Int,
        render: SingleRender,
    ) {
        singleRenderMap[entryId] = render
    }

    override fun addStackRender(
        entryId: Int,
        render: StackRender,
    ) {
        stackRenderMap[entryId] = render
    }

    override fun removeSingleRender(entryId: Int) {
        singleRenderMap.remove(entryId)
    }

    override fun removeStackRender(entryId: Int) {
        stackRenderMap.remove(entryId)
    }

    override fun getSingleRender(entryId: Int): SingleRender {
        return singleRenderMap[entryId].requireNotNull {
            Errors.renderNotFound(entryId)
        }
    }

    override fun getStackRender(entryId: Int): StackRender {
        return stackRenderMap[entryId].requireNotNull {
            Errors.renderNotFound(entryId)
        }
    }
}
