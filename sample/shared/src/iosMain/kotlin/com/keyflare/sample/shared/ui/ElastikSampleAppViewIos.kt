package com.keyflare.sample.shared.ui

import androidx.compose.ui.window.ComposeUIViewController
import com.keyflare.elastik.core.context.ElastikContext
import com.keyflare.sample.shared.core.ElastikSampleAppView

@Suppress("unused", "FunctionName")
fun ElastikSampleAppViewIos(context: ElastikContext) = ComposeUIViewController {
    ElastikSampleAppView(context)
}
