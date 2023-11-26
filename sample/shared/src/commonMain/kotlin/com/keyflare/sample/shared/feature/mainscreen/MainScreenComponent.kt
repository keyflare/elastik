package com.keyflare.sample.shared.feature.mainscreen

import androidx.compose.runtime.Stable
import co.touchlab.kermit.Logger
import com.keyflare.sample.shared.core.RootRouter

@Stable
class MainScreenComponent(
    private val rootRouter: RootRouter
) {

    init {
        Logger.d { "Main Screen Component" }
    }

    fun onContainersClick() {
        rootRouter.navigateTo(rootRouter.containers.destination)
    }

    fun onAnimationsClick() {
        rootRouter.navigateTo(rootRouter.animations.destination)
    }

    fun onTechClick() {
        rootRouter.navigateTo(rootRouter.tech.destination)
    }

    fun onSearchClick() {

    }
}
