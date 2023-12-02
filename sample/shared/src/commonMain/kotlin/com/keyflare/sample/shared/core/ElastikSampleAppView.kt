package com.keyflare.sample.shared.core

import androidx.compose.runtime.Composable
import com.keyflare.elastik.compose.view.ElastikCompose
import com.keyflare.elastik.core.context.ElastikContext

@Composable
fun ElastikSampleAppView(context: ElastikContext) {
    ElastikCompose(context.renderContext)
}
