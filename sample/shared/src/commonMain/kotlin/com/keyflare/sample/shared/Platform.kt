package com.keyflare.sample.shared

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform