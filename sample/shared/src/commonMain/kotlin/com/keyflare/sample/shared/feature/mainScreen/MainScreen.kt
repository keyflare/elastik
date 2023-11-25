package com.keyflare.sample.shared.feature.mainScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun MainScreen(component: MainScreenComponent) {
    Box(
        modifier = Modifier.fillMaxSize()
            .background(color = Color.Green)
    ) {
        Text(
            text = "Main Screen",
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
