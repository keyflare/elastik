package com.keyflare.elastik.compose.container

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import com.keyflare.elastik.core.state.BackstackEntry

@Composable
fun DefaultContainer(
    entries: State<List<BackstackEntry>>,
) {
    BasicReplaceEntryContainer(
        entry = entries.value.last(),
        modifier = Modifier.fillMaxSize(),
        transitionSpec = { fadeIn() togetherWith fadeOut() },
    )
}
