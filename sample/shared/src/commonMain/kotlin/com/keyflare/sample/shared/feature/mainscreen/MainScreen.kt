package com.keyflare.sample.shared.feature.mainscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.keyflare.sample.shared.resources.Strings

@Composable
fun MainScreen(component: MainScreenComponent) {
    MainScreenView(
        onContainersClick = { component.onContainersClick() },
        onAnimationsClick = { component.onAnimationsClick() },
        onTechClick = { component.onTechClick() },
        onSearchClick = { component.onSearchClick() },
    )
}

@Composable
fun MainScreenView(
    onContainersClick: () -> Unit,
    onAnimationsClick: () -> Unit,
    onTechClick: () -> Unit,
    onSearchClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colors.background)
    ) {
        MainScreenHeader(
            onSearchClick = onSearchClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        MainScreenDescription(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        MainScreenBody(
            onContainersClick = onContainersClick,
            onAnimationsClick = onAnimationsClick,
            onTechClick = onTechClick,
        )
    }
}

@Composable
private fun MainScreenHeader(
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        Text(
            text = Strings.mainScreenTitle,
            style = MaterialTheme.typography.h4,
        )
    }
}

@Composable
private fun MainScreenDescription(
    modifier: Modifier = Modifier,
) {
    Text(
        text = Strings.mainScreenDescription,
        style = MaterialTheme.typography.body1,
        modifier = modifier,
    )
}

@Composable
private fun MainScreenBody(
    onContainersClick: () -> Unit,
    onAnimationsClick: () -> Unit,
    onTechClick: () -> Unit,
) {
    MainScreenButton(
        text = Strings.mainScreenContainersButtonText,
        onClick = onContainersClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    )
    MainScreenButton(
        text = Strings.mainScreenAnimationsButtonText,
        onClick = onAnimationsClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    )
    MainScreenButton(
        text = Strings.mainScreenTechButtonText,
        onClick = onTechClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    )
}

@Composable
private fun MainScreenButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color = MaterialTheme.colors.surface)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.h5,
        )
    }
}
