package com.keyflare.elastik.sample.app.core

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.keyflare.sample.shared.ui.ElastikSampleAppView

class MainActivity : ComponentActivity() {

    private val rootRouter = sharedApp.rootRouter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ElastikSampleAppView(context = sharedApp.elastikContext)
        }
    }
}
