package com.example.dnd_quedamos

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform