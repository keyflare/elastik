package com.keyflare.elastik.sample.app.core

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.keyflare.elastik.core.attachElastikContext
import com.keyflare.sample.shared.core.ElastikSampleAppView

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        attachElastikContext(sharedApp.elastikContext)

        setContent {
            ElastikSampleAppView(context = sharedApp.elastikContext)
        }
    }
}
