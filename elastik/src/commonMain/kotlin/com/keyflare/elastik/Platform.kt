package com.keyflare.elastik

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform