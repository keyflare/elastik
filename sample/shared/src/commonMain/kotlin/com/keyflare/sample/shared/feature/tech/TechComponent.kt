package com.keyflare.sample.shared.feature.tech

import com.keyflare.sample.shared.core.RootRouter

class TechComponent(
    private val rootRouter: RootRouter,
) {

    fun onBackClick() {
        rootRouter.navigateBack()
    }
}
