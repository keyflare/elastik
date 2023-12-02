package com.keyflare.sample.shared.feature.containers

import com.keyflare.sample.shared.core.RootRouter

class ContainersComponent(
    private val rootRouter: RootRouter,
) {
    fun onBackClick() {
        rootRouter.onBack()
    }
}
