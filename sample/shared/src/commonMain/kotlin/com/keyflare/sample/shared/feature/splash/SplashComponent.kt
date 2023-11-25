package com.keyflare.sample.shared.feature.splash

import androidx.compose.runtime.Stable
import co.touchlab.kermit.Logger
import com.keyflare.sample.shared.core.RootRouter
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Stable
class SplashComponent(rootRouter: RootRouter) {

    init {
        Logger.d { "Splash Component" }

        // Temporary solution: use lifecycle scope after it will be implemented
        MainScope().launch {
            delay(2000)
            rootRouter.navigateTo(rootRouter.mainScreen.destination)
        }
    }
}
