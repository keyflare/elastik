package com.keyflare.elastik.compose

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform