package com.keyflare.sample.shared.feature.tech

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.keyflare.sample.shared.ui.view.Toolbar

@Composable
fun TechScreen(component: TechComponent) {

    TechScreenView(
        onBackClick = { component.onBackClick() },
    )
}

@Composable
fun TechScreenView(
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Toolbar(
            onBackClick = onBackClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
