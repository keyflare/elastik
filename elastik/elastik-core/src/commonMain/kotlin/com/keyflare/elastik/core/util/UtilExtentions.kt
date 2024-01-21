@file:OptIn(ExperimentalContracts::class)

package com.keyflare.elastik.core.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

//TODO Review before first release and remove all methods that are not used

internal inline fun <reified T> Any.cast(): T {
    contract { returns() implies (this@cast is T) }
    return this as T
}

internal inline fun <reified T> Any?.castOrNull(): T? =
    this as? T

internal inline fun <reified T> Any?.castOrError(lazyMessage: (() -> String)): T =
    this as? T ?: error(lazyMessage())

internal inline fun <reified T> T?.requireNotNull(): T =
    requireNotNull(this)

internal inline fun <reified T> T?.requireNotNull(lazyMessage: (() -> Any)): T =
    requireNotNull(this, lazyMessage)
