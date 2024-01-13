package com.keyflare.elastik.compose.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.keyflare.elastik.compose.render.ComposeStackRender
import com.keyflare.elastik.compose.render.ComposeSingleRender
import com.keyflare.elastik.core.render.NoRender
import com.keyflare.elastik.core.render.RenderContext
import com.keyflare.elastik.core.state.Stack
import com.keyflare.elastik.core.state.Entry
import com.keyflare.elastik.core.state.Single

@Composable
fun ElastikCompose(context: RenderContext) {
    CompositionLocalProvider(
        LocalRenderContext provides context,
    ) {
        val rootStackState = context.rootStack.collectAsState()
        ElastikBinder(
            entryState = rootStackState,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
fun ElastikBinder(
    entryState: State<Entry>,
    modifier: Modifier = Modifier,
) {
    val context = LocalRenderContext.current

    val isSingle by remember { derivedStateOf { entryState.value is Single }  }
    val entryId by remember { derivedStateOf { entryState.value.entryId } }

    // TODO Get rid of calling ElastikBinder at all for NoRender entries
    if (isSingle) {
        val render = context.getSingleRender(entryId)
        if (render is NoRender) return
        Box(modifier) {
            render as ComposeSingleRender
            render.content.composable()
        }
    } else {
        val render = context.getStackRender(entryId)
        if (render is NoRender) return
        val entries = remember { derivedStateOf { (entryState.value as Stack).entries } }
        // TODO MVP solution. Think of replacing Box with something else
        Box(modifier) {
            render as ComposeStackRender
            render.container.composable(entries)
        }
    }
}
