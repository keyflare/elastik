package com.keyflare.elastik.routing.util

//TODO Review before first release and remove all methods that are not used

internal inline fun <reified T> Any.cast(): T =
    this as T

internal inline fun <reified T> Any?.castOrNull(): T? =
    this as? T

internal inline fun <reified T> Any?.castOrError(message: String): T =
    this as? T ?: error(message)

internal inline fun <reified T> T?.requireNotNull(): T =
    requireNotNull(this)

internal inline fun <reified T> T?.requireNotNull(lazyMessage: (() -> Any)): T =
    requireNotNull(this, lazyMessage)
