package com.keyflare.elastik.compose.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.keyflare.elastik.compose.render.ComposeBackstackRender
import com.keyflare.elastik.compose.render.ComposeSingleRender
import com.keyflare.elastik.core.render.NoRender
import com.keyflare.elastik.core.render.RenderContext
import com.keyflare.elastik.core.state.Backstack
import com.keyflare.elastik.core.state.BackstackEntry

@Composable
fun ElastikCompose(context: RenderContext) {
    CompositionLocalProvider(
        LocalRenderContext provides context,
    ) {
        val rootBackstackState = context.rootBackstack.collectAsState()
        ElastikBinder(
            rootBackstackState,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
fun ElastikBinder(
    backstackEntry: State<BackstackEntry>,
    modifier: Modifier = Modifier,
) {
    val context = LocalRenderContext.current
    // TODO Now this function makes an assumption that backstack entry it was called
    //  with is always the same (backstack entry id is never changed). So now remember
    //  does not have a key. It is needed to add some kind of check for this assumption
    //  or get rid of this assumption.
    val isBackstack = remember { backstackEntry.value is Backstack }
    val backstackEntryId = remember { backstackEntry.value.id }

    // TODO Get rid of calling ElastikBinder at all for NoRender entries
    if (isBackstack) {
        val render = context.getBackstackRender(backstackEntryId)
        if (render is NoRender) return
        val entries = remember { derivedStateOf { (backstackEntry.value as Backstack).entries } }
        // TODO MVP solution. Think of replacing Box with something else
        Box(modifier) {
            render as ComposeBackstackRender
            render.container.composable(entries)
        }
    } else {
        val render = context.getSingleRender(backstackEntryId)
        if (render is NoRender) return
        Box(modifier) {
            render as ComposeSingleRender
            render.content.composable()
        }
    }
}
