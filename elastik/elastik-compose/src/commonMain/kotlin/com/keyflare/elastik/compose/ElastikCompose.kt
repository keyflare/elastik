package com.keyflare.elastik.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.keyflare.elastik.core.render.RenderContext

@Composable
fun ElastikCompose(context: RenderContext) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Hello from compose multiplatform!",
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
