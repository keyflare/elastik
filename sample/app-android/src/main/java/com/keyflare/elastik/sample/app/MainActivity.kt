package com.keyflare.elastik.sample.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.keyflare.sample.shared.core.ElastikSampleApp

class MainActivity : ComponentActivity() {

    private val rootRouter = ElastikSampleApp.rootRouter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Text(text = "Hello Android!")
        }
    }
}
