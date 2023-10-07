package com.keyflare.elastik.render

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform