package com.keyflare.sample.shared.ui

import androidx.compose.ui.window.ComposeUIViewController
import com.keyflare.elastik.core.ElastikContext

@Suppress("unused", "FunctionName")
fun ElastikSampleAppViewIos(context: ElastikContext) = ComposeUIViewController {
    ElastikSampleAppView(context)
}
