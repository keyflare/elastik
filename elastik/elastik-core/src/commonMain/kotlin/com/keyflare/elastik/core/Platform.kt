package com.keyflare.elastik.core

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform