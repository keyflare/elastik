package com.keyflare.elastik.core.setup.platform

import com.keyflare.elastik.core.context.ElastikContext
import com.keyflare.elastik.core.context.ElastikPlatform
import com.keyflare.elastik.core.render.NoRender
import com.keyflare.elastik.core.routing.router.BaseRouter
import com.keyflare.elastik.core.setup.component.TestScreenComponentsReporter
import com.keyflare.elastik.core.setup.tree.RouterTreeBuilderScope
import com.keyflare.elastik.core.setup.tree.createDynamicRoot
import com.keyflare.elastik.core.setup.tree.createStaticRoot
import kotlinx.coroutines.CoroutineScope

@Suppress("MemberVisibilityCanBePrivate")
internal class TestPlatform(scope: CoroutineScope) {
    val lifecycleEventsSource = TestLifecycleEventsSource()
    val backEventsSource = TestBackEventsSource(scope)

    val platform = ElastikPlatform(
        lifecycleEventsSource = lifecycleEventsSource,
        backEventsSource = backEventsSource,
    )

    val elastikContext = ElastikContext
        .create(NoRender)
        .apply { attachPlatform(platform) }

    val testScreenComponentsReporter = TestScreenComponentsReporter()

    fun createStaticRoot(body: RouterTreeBuilderScope.() -> Unit): BaseRouter {
        return elastikContext.createStaticRoot(testScreenComponentsReporter, body)
    }

    fun createDynamicRoot(body: RouterTreeBuilderScope.() -> Unit): BaseRouter {
        return elastikContext.createDynamicRoot(testScreenComponentsReporter, body)
    }
}
