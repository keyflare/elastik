package com.keyflare.elastik.core.util

import com.keyflare.elastik.core.context.ElastikContext
import com.keyflare.elastik.core.render.NoRender
import com.keyflare.elastik.core.routing.router.BaseRouter
import com.keyflare.elastik.core.routing.router.DynamicRouter
import com.keyflare.elastik.core.routing.router.StaticRouter

fun ElastikContext.createStaticRoot(
    body: RouterTreeBuilderScope.() -> Unit,
): BaseRouter {
    return RouterTreeBuilder(this, body, isStatic = true).build()
}

fun ElastikContext.createDynamicRoot(
    body: RouterTreeBuilderScope.() -> Unit,
): BaseRouter {
    return RouterTreeBuilder(this, body, isStatic = false).build()
}

interface RouterTreeBuilderScope {
    fun single()
    fun static(body: RouterTreeBuilderScope.() -> Unit)
    fun dynamic(body: RouterTreeBuilderScope.() -> Unit)
}

open class TestStaticRouter(
    context: ElastikContext,
) : StaticRouter(context)

open class TestDynamicRouter(
    context: ElastikContext,
) : DynamicRouter(context)

data class TestScreenComponent(val destinationId: String)

private class RouterTreeBuilderScopeImpl(
    private val idGenerator: DestinationIdGenerator = DestinationIdGenerator(),
) : RouterTreeBuilderScope {

    private val _destinations = mutableListOf<DestinationBuilderData>()
    val destinations: Any = _destinations

    override fun single() {
        _destinations.add(SingleBuilderData(destinationId = idGenerator.get()))
    }

    override fun static(
        body: RouterTreeBuilderScope.() -> Unit,
    ) {
        stack(body, static = true)
    }

    override fun dynamic(
        body: RouterTreeBuilderScope.() -> Unit,
    ) {
        stack(body, static = false)
    }

    private fun stack(
        body: RouterTreeBuilderScopeImpl.() -> Unit,
        static: Boolean,
    ) {
        _destinations.add(
            StackBuilderData(
                destinationId = idGenerator.get(),
                isStatic = static,
                children = RouterTreeBuilderScopeImpl(idGenerator).run {
                    body()
                    build()
                }
            )
        )
    }

    private fun build(): List<DestinationBuilderData> = _destinations

    class DestinationIdGenerator(private val alphabet: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZ") {
        private var currentIndex = 0

        fun get(): String {
            var n = currentIndex++
            val l = alphabet.length
            var result = ""
            while (n >= 0) {
                result = "${alphabet[n % l]}$result"
                n = n / l - 1
            }
            return result
        }
    }
}

private class RouterTreeBuilder(
    private val context: ElastikContext,
    private val body: RouterTreeBuilderScopeImpl.() -> Unit,
    private val isStatic: Boolean,
) {
    fun build(): BaseRouter {
        val entriesToBuild = RouterTreeBuilderScopeImpl().run {
            body()
            @Suppress("UNCHECKED_CAST")
            destinations as List<DestinationBuilderData>
        }

        val root = if (isStatic) {
            object : TestStaticRouter(context) {
                init {
                    entriesToBuild.forEach { destination(it) }
                }
            }
        } else {
            object : TestDynamicRouter(context) {
                init {
                    entriesToBuild.forEach { destination(it) }
                }
            }
        }

        return root
    }

    private fun BaseRouter.destination(entry: DestinationBuilderData) {
        when {
            entry is SingleBuilderData ->
                testSingle(entry.destinationId)

            entry is StackBuilderData && entry.isStatic ->
                testStaticStack(entry.destinationId) {
                    entry.children.forEach { destination(it) }
                }

            entry is StackBuilderData && !entry.isStatic ->
                testDynamicStack(entry.destinationId) {
                    entry.children.forEach { destination(it) }
                }
        }
    }

    private fun BaseRouter.testSingle(destinationId: String) {
        when (this) {
            is StaticRouter -> singleNoArgs(
                destinationId = destinationId,
                renderFactory = { NoRender },
                componentFactory = { TestScreenComponent(destinationId) }
            )

            is DynamicRouter -> singleNoArgs(
                destinationId = destinationId,
                renderFactory = { NoRender },
                componentFactory = { TestScreenComponent(destinationId) }
            )
        }
    }

    private fun BaseRouter.testStaticStack(
        destinationId: String,
        entriesFactory: StaticRouter.() -> Unit,
    ) {
        when (this) {
            is StaticRouter -> stackNoArgs(
                destinationId = destinationId,
                renderFactory = { NoRender },
                routerFactory = {
                    object : TestStaticRouter(it) {
                        init {
                            entriesFactory()
                        }
                    }
                }
            )

            is DynamicRouter -> stackNoArgs(
                destinationId = destinationId,
                renderFactory = { NoRender },
                routerFactory = {
                    object : TestStaticRouter(it) {
                        init {
                            entriesFactory()
                        }
                    }
                }
            )
        }
    }

    private fun BaseRouter.testDynamicStack(
        destinationId: String,
        entriesFactory: DynamicRouter.() -> Unit,
    ) {
        when(this) {
            is StaticRouter -> stackNoArgs(
                destinationId = destinationId,
                renderFactory = { NoRender },
                routerFactory = {
                    object : TestDynamicRouter(it) {
                        init {
                            entriesFactory()
                        }
                    }
                }
            )

            is DynamicRouter -> stackNoArgs(
                destinationId = destinationId,
                renderFactory = { NoRender },
                routerFactory = {
                    object : TestDynamicRouter(it) {
                        init {
                            entriesFactory()
                        }
                    }
                }
            )
        }
    }
}

private sealed interface DestinationBuilderData

private data class SingleBuilderData(
    val destinationId: String
) : DestinationBuilderData

private data class StackBuilderData(
    val destinationId: String,
    val children: List<DestinationBuilderData>,
    val isStatic: Boolean,
) : DestinationBuilderData
