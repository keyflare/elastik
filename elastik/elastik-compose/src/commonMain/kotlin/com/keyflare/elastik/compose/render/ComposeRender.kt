package com.keyflare.elastik.compose.render

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import com.keyflare.elastik.compose.container.DefaultContainer
import com.keyflare.elastik.core.render.BackstackRender
import com.keyflare.elastik.core.render.SingleRender
import com.keyflare.elastik.core.render.Transition
import com.keyflare.elastik.core.state.BackstackEntry
import kotlin.jvm.JvmInline

@Immutable
class ComposeSingleRender @PublishedApi internal constructor(
    override val content: ComposeSingleContent,
    override val transition: ComposeTransition?,
) : SingleRender {

    companion object {

        // TODO This is MVP solution! Review carefully and refactor if needed
        inline fun <Component : Any> factory(
            crossinline content: @Composable (component: Component) -> Unit,
        ): (Component) -> SingleRender {
            return { component: Component ->
                ComposeSingleRender(
                    content = ComposeSingleContent { content(component) },
                    transition = null,
                )
            }
        }
    }
}

@JvmInline
@Immutable
value class ComposeSingleContent(
    val composable: @Composable () -> Unit
) : SingleRender.Content

@Immutable
class ComposeBackstackRender(
    override val transition: ComposeTransition? = null,
    override val defaultChildrenTransition: ComposeTransition? = null,
    container: @Composable (entries: State<List<BackstackEntry>>) -> Unit = { DefaultContainer(it) },
) : BackstackRender {

    override val container: ComposeContainer = ComposeContainer(container)
}

@JvmInline
@Immutable
value class ComposeContainer(
    val composable: @Composable (entries: State<List<BackstackEntry>>) -> Unit
) : BackstackRender.Container

@Immutable
class ComposeTransition : Transition
