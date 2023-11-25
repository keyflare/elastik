package com.keyflare.elastik.compose.container

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.keyflare.elastik.compose.view.ElastikBinder
import com.keyflare.elastik.core.state.BackstackEntry

@Composable
fun BasicReplaceEntryContainer(
    entry: BackstackEntry,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    transitionSpec: AnimatedContentTransitionScope<Int>.() -> ContentTransform =
        { EnterTransition.None togetherWith ExitTransition.None },
) {
    var current by remember { mutableStateOf(entry) }
    var previous by remember { mutableStateOf(entry) }
    var animationKey by remember { mutableStateOf(entry.id) }

    if (entry.id != current.id) {
        previous = current
        animationKey = entry.id
    }
    if (current != entry) {
        current = entry
    }

    AnimatedContent(
        targetState = animationKey,
        contentAlignment = contentAlignment,
        transitionSpec = transitionSpec,
        modifier = modifier,
        label = "ReplaceEntryContainer",
    ) { key ->
        val actualState = rememberUpdatedState(if (current.id == key) current else previous)
        ElastikBinder(actualState)
    }
}
