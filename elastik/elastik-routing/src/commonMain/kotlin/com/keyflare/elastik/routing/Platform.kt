package com.keyflare.elastik.routing

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform