package com.keyflare.elastik.core.routing.diff

fun <T> differenceOf(
    original: List<T>,
    updated: List<T>,
    detectMoves: Boolean = true
) = DiffGenerator.generateDiff(original, updated, detectMoves)
