package com.keyflare.elastik.compose.view

import androidx.compose.runtime.staticCompositionLocalOf
import com.keyflare.elastik.core.render.RenderContext

// TODO: Getting access to this composition local in every
//  navigation node may be not optimal solution. Research and refactor.
internal val LocalRenderContext = staticCompositionLocalOf<RenderContext> {
    error("No RenderContext provided")
}
