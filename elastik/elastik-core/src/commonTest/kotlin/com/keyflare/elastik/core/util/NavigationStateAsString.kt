package com.keyflare.elastik.core.util

import com.keyflare.elastik.core.routing.router.BaseRouter
import com.keyflare.elastik.core.routing.router.StaticRouter
import com.keyflare.elastik.core.state.Entry
import com.keyflare.elastik.core.state.Single
import com.keyflare.elastik.core.state.Stack

fun BaseRouter.asString(): String {
    return stack!!.asString(router = this)
}

private fun Entry.asString(router: BaseRouter?): String {
    return when (this) {
        is Single -> destinationId
        is Stack -> {
            val static = router.requireNotNull() is StaticRouter
            val content = entries.joinToString(separator = "-") { entry ->
                val entryRouter = if (entry is Stack) {
                    router
                        .requireNotNull()
                        .childRouters
                        .requireNotNull()
                        .filterNotNull()
                        .find { it.entryId == entry.entryId }
                } else {
                    null
                }
                entry.asString(entryRouter)
            }

            return "$destinationId${if (static) "*" else ""}($content)"
        }
    }
}
