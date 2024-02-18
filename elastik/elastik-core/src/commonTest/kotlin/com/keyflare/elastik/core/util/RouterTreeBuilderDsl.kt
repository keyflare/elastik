package com.keyflare.elastik.core.util

import com.keyflare.elastik.core.context.ElastikContext
import com.keyflare.elastik.core.render.NoRender
import com.keyflare.elastik.core.routing.router.BaseRouter
import com.keyflare.elastik.core.routing.router.DynamicRouter
import com.keyflare.elastik.core.routing.router.StaticRouter
import com.keyflare.elastik.core.util.component.TestScreenComponent
import com.keyflare.elastik.core.util.component.TestScreenComponentsReporter

fun ElastikContext.createStaticRoot(
    testScreenComponentsReporter: TestScreenComponentsReporter = TestScreenComponentsReporter(),
    body: RouterTreeBuilderScope.() -> Unit,
): BaseRouter {
    return RouterTreeBuilder(
        context = this,
        body = body,
        isStatic = true,
        testScreenComponentsReporter = testScreenComponentsReporter,
    ).build()
}

fun ElastikContext.createDynamicRoot(
    testScreenComponentsReporter: TestScreenComponentsReporter = TestScreenComponentsReporter(),
    body: RouterTreeBuilderScope.() -> Unit,
): BaseRouter {
    return RouterTreeBuilder(
        context = this,
        body = body,
        isStatic = false,
        testScreenComponentsReporter = testScreenComponentsReporter,
    ).build()
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
    private val testScreenComponentsReporter: TestScreenComponentsReporter,
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
                    entriesToBuild.forEach { destination(it, testScreenComponentsReporter) }
                }
            }
        } else {
            object : TestDynamicRouter(context) {
                init {
                    entriesToBuild.forEach { destination(it, testScreenComponentsReporter) }
                }
            }
        }

        return root
    }

    private fun BaseRouter.destination(
        entry: DestinationBuilderData,
        testScreenComponentsReporter: TestScreenComponentsReporter,
    ) {
        when {
            entry is SingleBuilderData ->
                testSingle(entry.destinationId, testScreenComponentsReporter)

            entry is StackBuilderData && entry.isStatic ->
                testStaticStack(entry.destinationId) {
                    entry.children.forEach { destination(it, testScreenComponentsReporter) }
                }

            entry is StackBuilderData && !entry.isStatic ->
                testDynamicStack(entry.destinationId) {
                    entry.children.forEach { destination(it, testScreenComponentsReporter) }
                }
        }
    }

    private fun BaseRouter.testSingle(
        destinationId: String,
        testReporter: TestScreenComponentsReporter,
    ) {
        when (this) {
            is StaticRouter -> singleNoArgs(
                destinationId = destinationId,
                renderFactory = { NoRender },
                componentFactory = { TestScreenComponent(it, testReporter) }
            )

            is DynamicRouter -> singleNoArgs(
                destinationId = destinationId,
                renderFactory = { NoRender },
                componentFactory = { TestScreenComponent(it, testReporter) }
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
