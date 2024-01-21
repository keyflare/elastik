package com.keyflare.elastik.core.util

import com.keyflare.elastik.core.context.ElastikContext
import com.keyflare.elastik.core.render.NoRender
import com.keyflare.elastik.core.routing.router.BaseRouter
import com.keyflare.elastik.core.routing.router.DynamicRouter
import com.keyflare.elastik.core.routing.router.StaticRouter

fun ElastikContext.createStaticRoot(
    body: RouterTreeBuilderScope.() -> Unit,
): BaseRouter {
    return BackHandleTestRootRouterBuilder(this, body, isStatic = true).build()
}

fun ElastikContext.createDynamicRoot(
    body: RouterTreeBuilderScope.() -> Unit,
): BaseRouter {
    return BackHandleTestRootRouterBuilder(this, body, isStatic = false).build()
}

interface RouterTreeBuilderScope {
    fun single()
    fun static(body: RouterTreeBuilderScope.() -> Unit)
    fun dynamic(body: RouterTreeBuilderScope.() -> Unit)
}

class BaseTestStaticRouter(
    context: ElastikContext,
) : StaticRouter(context)

class BaseTestDynamicRouter(
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
            while(n >= 0) {
                result = "${alphabet[n % l]}$result"
                n = n / l - 1
            }
            return result
        }
    }
}

private class BackHandleTestRootRouterBuilder(
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
            BaseTestStaticRouter(context)
        } else {
            BaseTestDynamicRouter(context)
        }

        entriesToBuild.forEach { buildEntry(it, root) }

        return root
    }

    private fun buildEntry(entry: DestinationBuilderData, parent: BaseRouter) {
        when (parent) {
            is StaticRouter -> parent.apply {
                when {
                    entry is SingleBuilderData ->
                        testSingle(entry.destinationId)

                    entry is StackBuilderData && entry.isStatic ->
                        testStaticStack(entry.destinationId) {
                            entry.children.forEach { buildEntry(it, parent = this) }
                        }

                    entry is StackBuilderData && !entry.isStatic ->
                        testDynamicStack(entry.destinationId) {
                            entry.children.forEach { buildEntry(it, parent = this) }
                        }
                }
            }

            is DynamicRouter -> parent.apply {
                when {
                    entry is SingleBuilderData -> {
                        val destination = testSingle(entry.destinationId)
                        navigateTo(destination.destination)
                    }

                    entry is StackBuilderData && entry.isStatic -> {
                        val destination = testStaticStack(entry.destinationId) {
                            entry.children.forEach { buildEntry(it, parent = this) }
                        }
                        navigateTo(destination.destination)
                    }

                    entry is StackBuilderData && !entry.isStatic -> {
                        val destination = testDynamicStack(entry.destinationId) {
                            entry.children.forEach { buildEntry(it, parent = this) }
                        }
                        navigateTo(destination.destination)
                    }
                }
            }
        }
    }

    private fun StaticRouter.testSingle(destinationId: String) = singleNoArgs(
        destinationId = destinationId,
        renderFactory = { NoRender },
        componentFactory = { TestScreenComponent(destinationId) }
    )

    private fun StaticRouter.testStaticStack(
        destinationId: String,
        entriesFactory: StaticRouter.() -> Unit,
    ) = stackNoArgs(
        destinationId = destinationId,
        renderFactory = { NoRender },
        routerFactory = { BaseTestStaticRouter(it).apply { entriesFactory() } }
    )

    private fun StaticRouter.testDynamicStack(
        destinationId: String,
        entriesFactory: DynamicRouter.() -> Unit,
    ) = stackNoArgs(
        destinationId = destinationId,
        renderFactory = { NoRender },
        routerFactory = { BaseTestDynamicRouter(it).apply { entriesFactory() } }
    )

    private fun DynamicRouter.testSingle(destinationId: String) = singleNoArgs(
        destinationId = destinationId,
        renderFactory = { NoRender },
        componentFactory = { TestScreenComponent(destinationId) }
    )

    private fun DynamicRouter.testStaticStack(
        destinationId: String,
        entriesFactory: StaticRouter.() -> Unit,
    ) = stackNoArgs(
        destinationId = destinationId,
        renderFactory = { NoRender },
        routerFactory = { context -> BaseTestStaticRouter(context).apply { entriesFactory() } }
    )

    private fun DynamicRouter.testDynamicStack(
        destinationId: String,
        entriesFactory: DynamicRouter.() -> Unit,
    ) = stackNoArgs(
        destinationId = destinationId,
        renderFactory = { NoRender },
        routerFactory = { BaseTestDynamicRouter(it).apply { entriesFactory() } }
    )
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
