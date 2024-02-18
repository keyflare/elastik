package com.keyflare.elastik.core.setup.visualization

import com.keyflare.elastik.core.routing.router.BaseRouter
import com.keyflare.elastik.core.routing.router.StaticRouter
import com.keyflare.elastik.core.state.Entry
import com.keyflare.elastik.core.state.Single
import com.keyflare.elastik.core.state.Stack
import com.keyflare.elastik.core.util.requireNotNull
import kotlin.test.assertEquals

fun BaseRouter.assertAsString(expected: String) {
    assertEquals(
        expected = expected,
        actual = asString(),
        message = "Router tree as string doesn't look as expected",
    )
}

fun BaseRouter.asString(): String {
    return stack.asString(router = this)
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
