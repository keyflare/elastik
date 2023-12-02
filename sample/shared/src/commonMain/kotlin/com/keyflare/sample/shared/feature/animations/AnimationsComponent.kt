package com.keyflare.sample.shared.feature.animations

import com.keyflare.sample.shared.core.RootRouter

class AnimationsComponent(
    private val rootRouter: RootRouter,
) {

    fun onBackClick() {
        rootRouter.onBack()
    }
}
